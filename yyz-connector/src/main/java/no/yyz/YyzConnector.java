package no.yyz;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import no.yyz.hibernateutil.services.GroupService;
import no.yyz.hibernateutil.services.UserGroupsService;
import no.yyz.hibernateutil.services.UserService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import no.yyz.models.models.UserGroup;
import org.hibernate.Session;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@ConnectorClass(
    displayNameKey = "UI_CONNECTOR_NAME",
    configurationClass = YyzConfiguration.class
)
public class YyzConnector implements AutoCloseable, TestApiOp,
                                     PoolableConnector,
                                     CreateOp,
                                     DeleteOp,
                                     UpdateAttributeValuesOp,
                                     UpdateDeltaOp,
                                     SearchOp<Filter>,
                                     SchemaOp {

  private static final Log LOG = Log.getLog(YyzConnector.class);
  private final UserService userservice = new UserService();
  private final GroupService groupService = new GroupService();
  private final UserGroupsService userGroupsService = new UserGroupsService();
  private YyzConfiguration configuration;

  /**
   * Return a list of all group IDs that a user with the given ID is a member of.
   *
   * @param value   The user ID to look up.
   * @param session The Hibernate session to use.
   * @return A list of group IDs that the user is a member of.
   */
  private static List<Integer> getGroupIdsFromUserId(Object value, Session session) {
    CriteriaBuilder allGroupsFromUserid = session.getCriteriaBuilder();
    CriteriaQuery<Integer> groupsQuery = allGroupsFromUserid.createQuery(Integer.class);
    Root<UserGroup> root = groupsQuery.from(UserGroup.class);
    groupsQuery.select(root.get("groupId"))
        .where(allGroupsFromUserid.equal(root.get("userId"), Integer.parseInt(value.toString())));
    return session.createQuery(groupsQuery).getResultList();
  }

  @Override
  public void test() {
  }

  @Override
  public Configuration getConfiguration() {
    return this.configuration;
  }

  @Override
  public void init(Configuration configuration) {
    this.configuration = (YyzConfiguration) configuration;
    LOG.info("This is a test");
  }

  @Override
  public void dispose() {
  }

  @Override
  public void checkAlive() {
    LOG.info("YYZ Is alive");
  }

  @Override
  public Schema schema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder(YyzConnector.class);
    schemaBuilder.defineObjectClass(User.ObjectInfoBuilder().build());
    schemaBuilder.defineObjectClass(Group.ObjectInfoBuilder().build());
    return schemaBuilder.build();
  }

  @Override
  public Uid create(ObjectClass objectClass, Set<Attribute> attributes,
                    OperationOptions operationOptions) {
    if (objectClass == null) {
      LOG.error("Attribute of type ObjectClass not provided.");
      throw new InvalidAttributeValueException("Attribute of type ObjectClass not provided.");
    }
    if (attributes == null) {
      LOG.error("Attribute of type Set<Attribute> not provided.");
      throw new InvalidAttributeValueException("Attribute of type Set<Attribute> not provided.");
    }
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      return handleGroupCreate(attributes);
    }

    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      return handleUserCreate(attributes);
    }
    throw new RuntimeException("Unknown typename: " + typename);
  }

  private Uid handleUserCreate(Set<Attribute> attributes) {
    User user = new User();
    user.parseAttributes(attributes);
    try {
      try (var session = this.userservice.sessionFactory.openSession()) {
        var foundUser = this.userservice.findByName(user.getUsername(), session);
        if (foundUser != null) {
          foundUser.parseAttributes(attributes);
          foundUser = this.userservice.persist(foundUser, session);
          return new Uid(Integer.toString(foundUser.getId()));
        }
        user = this.userservice.persist(user, session);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new Uid(String.valueOf(user.getId()));
  }

  private Uid handleGroupCreate(Set<Attribute> attributes) {
    Group group = new Group();
    group.parseAttributes(attributes);
    try (var session = this.groupService.sessionFactory.openSession()) {
      var foundGroup = this.groupService.findGroupByName(group.getGroupName(), session);
      if (foundGroup != null) {
        foundGroup.parseAttributes(attributes);
        foundGroup = this.groupService.persist(foundGroup, session);
        return new Uid(Integer.toString(foundGroup.getId()));
      }
      try {
        this.groupService.persist(group, session);
        return new Uid(Integer.toString(group.getId()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass,
                                                         OperationOptions operationOptions) {
    return CollectionUtil::newList;
  }

  @Override
  public void executeQuery(ObjectClass objectClass, Filter filter, ResultsHandler resultsHandler,
                           OperationOptions operationOptions) {
    try {
      if (objectClass.getObjectClassValue().equals(ObjectClass.GROUP_NAME)) {
        handleGroupQuery(filter, resultsHandler);
        return;
      }
      if (objectClass.getObjectClassValue().equals(ObjectClass.ACCOUNT_NAME)) {
        handleUserQuery(filter, resultsHandler);
        return;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    LOG.warn("Unknown object class: " + objectClass.getObjectClassValue());
  }

  private void handleUserQuery(Filter filter, ResultsHandler resultsHandler) {
    if (filter instanceof EqualsFilter equalsFilter) {
      var attributeName = equalsFilter.getAttribute();
      if (attributeName.getName().equals(Name.NAME)) {
        try (var session = this.userservice.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var user =
                session
                    .createQuery("from User where username = :username ", User.class).setParameter(
                        "username", value).getSingleResult();
            if (user != null) {
              Set<Attribute> attributes = user.toAttributes();
              ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
              resultsHandler.handle(obj);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return;
      }
      if (attributeName.getName().equals(Uid.NAME)) {
        try (var session = this.userservice.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var user =
                session
                    .createQuery("from User where id = :id ", User.class).setParameter(
                        "id", value).getSingleResult();
            if (user != null) {
              Set<Attribute> attributes = user.toAttributes();
              ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
              resultsHandler.handle(obj);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return;
    }
    List<User> list = this.userservice.getAll();
    for (User user : list) {
      Set<Attribute> attributes = user.toAttributes();
      ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
      resultsHandler.handle(obj);
    }
  }

  private void handleGroupQuery(Filter filter, ResultsHandler resultsHandler) {
    if (filter instanceof ContainsAllValuesFilter) {
      var attributeName = ((ContainsAllValuesFilter) filter).getAttribute();
      if (attributeName.getName().equals("members")) {
        try (var session = this.groupService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {

            var usersGroupsCriteria = session.getCriteriaBuilder();
            var query = usersGroupsCriteria.createQuery(UserGroup.class);
            query.where(usersGroupsCriteria.equal(query.from(UserGroup.class).get("userId"),
                value));
            List<UserGroup> userGroups = session.createQuery(query).getResultList();

            for (UserGroup elem : userGroups) {
              var group = this.groupService.getById(elem.getGroupId(), session);
              Set<Attribute> attributes = group.toAttributes();
              ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
              boolean handlerResult = resultsHandler.handle(obj);
              if (!handlerResult) {
                System.out.println("break, can't handle anymore.");
                break;
              }
            }
            return;
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return;
      }
      return;
    }
    if (filter instanceof EqualsFilter equalsFilter) {
      var attributeName = equalsFilter.getAttribute();
      if (attributeName.getName().equals(Uid.NAME)) {
        try (var session = this.groupService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var group = session.createQuery("from Group where id = :id", Group.class).setParameter(
                "id",
                value).getSingleResult();
            if (group != null) {
              Set<Attribute> attributes = processGroupAttributesWithMembers(group);
              ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
              resultsHandler.handle(obj);
            }
          }
        }
        return;
      }
      if (attributeName.getName().equals(Name.NAME)) {
        try (var session = this.groupService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var group = session.createQuery("from Group where id  = :id", Group.class)
                .setParameter("id", value).getSingleResult();
            if (group != null) {
              Set<Attribute> attributes = processGroupAttributesWithMembers(group);
              ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
              resultsHandler.handle(obj);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      List<Group> list = this.groupService.getAll();
      for (Group group : list) {
        Set<Attribute> attributes = processGroupAttributesWithMembers(group);
        ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
        resultsHandler.handle(obj);
      }
    }
  }

  private Set<Attribute> processGroupAttributesWithMembers(Group group) {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(group.getId())));
    attributes.add(AttributeBuilder.build(Name.NAME, group.getGroupName()));
    attributes.add(AttributeBuilder.build("description", group.getDescription()));
    attributes.add(AttributeBuilder.build("groupName", group.getGroupName()));

    return attributes;
  }

  @Override
  public void close() throws Exception {
    this.userservice.close();
  }

  @Override
  public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> set,
                    OperationOptions operationOptions) {
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      try (var session = this.groupService.sessionFactory.openSession()) {
        var id = uid.getValue().getFirst().toString();
        Group group = this.groupService.getById(Integer.parseInt(id), session);
        group.parseAttributes(set);
        this.groupService.persist(group, session);
        List<Integer> userIds =
            this.userGroupsService.getGroupMembersByGroupId(Integer.parseInt(id), session);

        // delete all elements in table UsersGroups where groupId = id using hibernate query builder
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaDelete<UserGroup> delete = builder.createCriteriaDelete(UserGroup.class);
        Root<UserGroup> root = delete.from(UserGroup.class);
        delete.where(builder.equal(root.get("groupId"), Integer.parseInt(id)));
        session.createMutationQuery(delete).executeUpdate();

        for (Integer userId : userIds) {
          this.userGroupsService.persist(new UserGroup(userId, group.getId()), session);
        }
        this.userGroupsService.getAll();
        return uid;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      var id = uid.getValue().getFirst().toString();
      try (var session = this.userservice.sessionFactory.openSession()) {
        User user = this.userservice.getById(Integer.parseInt(id), session);
        user.parseAttributes(set);
        user = this.userservice.persist(user, session);
        return new Uid(String.valueOf(user.getId()));

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      return null;
    }
  }

  @Override
  public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      try (var session = this.groupService.sessionFactory.openSession()) {
        var id = uid.getValue().getFirst().toString();
        Group user = this.groupService.getById(Integer.parseInt(id), session);
        this.groupService.delete(user, session);
        return;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      var id = uid.getValue().getFirst().toString();
      try (var session = this.userservice.sessionFactory.openSession()) {
        User user = this.userservice.getById(Integer.parseInt(id), session);
        this.userservice.delete(user, session);
        return;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    LOG.warn("Unknown object class: " + objectClass.getObjectClassValue());
  }

  @Override
  public Uid addAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> set,
                                OperationOptions operationOptions) {
    update(objectClass, uid, set, operationOptions);
    return uid;
  }

  @Override
  public Uid removeAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> set,
                                   OperationOptions operationOptions) {
    update(objectClass, uid, set, operationOptions);
    return uid;
  }

  @Override
  public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid,
                                         Set<AttributeDelta> set,
                                         OperationOptions operationOptions) {
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      try (var session = this.groupService.sessionFactory.openSession()) {
        var groupId = uid.getValue().getFirst().toString();
        Group group = this.groupService.getById(Integer.parseInt(groupId), session);
        if (!set.isEmpty()) {
          for (var s : set) {
            if (s.getName().equals(Name.NAME)) {
              if (s.getValuesToAdd() != null && !s.getValuesToAdd().isEmpty()) {
                for (var v : s.getValuesToAdd()) {
                  // use the criteria builder to find the combination of userid and group in the
                  // UserGroups table
                  var q = session.createQuery("from UsersGroups where userId = :userId and " +
                      "groupId" +
                      " = :groupId", UserGroup.class);
                  q.setParameter("userId", Integer.parseInt(v.toString()));
                  q.setParameter("groupId", group.getId());
                  List<UserGroup> result = q.getResultList();
                  if (result.isEmpty()) {
                    this.userGroupsService.persist(new UserGroup(Integer.parseInt(v.toString()),
                        group.getId()), session);
                  }
                }
              }
              if (s.getValuesToRemove() != null && !s.getValuesToRemove().isEmpty()) {
                for (var v : s.getValuesToRemove()) {
                  // check if the combination of userid and group exists in the UserGroups table
                  var foundCombo = session.createQuery("from UsersGroups where userId = :userId " +
                          "and groupId = :groupId", UserGroup.class)
                      .setParameter("userId", Integer.parseInt(v.toString()))
                      .setParameter("groupId", group.getId()).getResultList();
                  if (foundCombo.isEmpty()) {
                    continue;
                  }
                  for (UserGroup userGroup : foundCombo) {
                    session.remove(userGroup);
                  }
                }
              }
              if (s.getValuesToReplace() != null && !s.getValuesToReplace().isEmpty()) {
                var transaction = session.beginTransaction();

                var builder = session.getCriteriaBuilder();
                CriteriaDelete<UserGroup> delete = builder.createCriteriaDelete(UserGroup.class);
                Root<UserGroup> root = delete.from(UserGroup.class);
                delete.where(builder.equal(root.get("groupId"), Integer.parseInt(groupId)));
                session.createMutationQuery(delete).executeUpdate();

                transaction.commit();

                for (var v : s.getValuesToReplace()) {
                  // check if the combination of userid and group exists in the UserGroups table
                  var userGroupCombo = new UserGroup(Integer.parseInt(v.toString()), group.getId());
                  session.persist(userGroupCombo);
                }
              }
            }
          }
          return set;
        }
        return set;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      try (var session = this.userservice.sessionFactory.openSession()) {
        var id = uid.getValue().getFirst().toString();
        User user = this.userservice.getById(Integer.parseInt(id), session);
        user.parseAttributesDelta(set);
        this.userservice.persist(user, session);
        return set;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return Set.of();
  }
}
