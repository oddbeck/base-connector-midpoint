package no.yyz;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import no.yyz.hibernateutil.services.SessionFactoryService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
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

import java.util.ArrayList;
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
//                                     UpdateAttributeValuesOp,
                                     UpdateDeltaOp,
                                     SearchOp<Filter>,
                                     SchemaOp {

  private static final Log LOG = Log.getLog(YyzConnector.class);
  SessionFactoryService sessionFactoryService = new SessionFactoryService();
  //  private final UserService userservice = new UserService();
//  private final GroupService groupService = new GroupService();
  //  private final UserGroupsService userGroupsService = new UserGroupsService();
  private YyzConfiguration configuration;

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
      try (var session = sessionFactoryService.sessionFactory.openSession()) {
        var tran = session.beginTransaction();
        session.persist(user);
        tran.commit();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new Uid(String.valueOf(user.getId()));
  }

  private Uid handleGroupCreate(Set<Attribute> attributes) {
    Group group = new Group();
    group.parseAttributes(attributes);

    try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
      var cb = session.getCriteriaBuilder();
      var builder = cb.createQuery(Group.class);
      builder.where(cb.equal(builder.from(Group.class).get("groupName"), group.getGroupName()));
      var query = session.createQuery(builder);
      var result = query.getResultList();
      if (result.isEmpty()) {
        var tran = session.beginTransaction();
        session.persist(group);
        tran.commit();
        return new Uid(Long.toString(group.getId()));
      }
      return new Uid(Long.toString(result.getFirst().getId()));
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
        try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var user = session
                .createQuery("from User where username = :username ", User.class)
                .setParameter("username", value).getSingleResult();
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
        try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
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
          System.out.println(e.getMessage());
          // throw new RuntimeException(e);
        }
      }
      return;
    }
    var session = this.sessionFactoryService.sessionFactory.openSession();
    HibernateCriteriaBuilder cb = session.getCriteriaBuilder();
    JpaCriteriaQuery<User> query = cb.createQuery(User.class);
    query.from(User.class);
    List<User> resultList = session.createQuery(query).getResultList();

    for (User user : resultList) {
      Set<Attribute> attributes = user.toAttributes();
      ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
      resultsHandler.handle(obj);
    }
  }

  private void handleGroupQuery(Filter filter, ResultsHandler resultsHandler) {
    if (filter instanceof ContainsAllValuesFilter) {
      var attributeName = ((ContainsAllValuesFilter) filter).getAttribute();
      if (attributeName.getName().equals("members")) {
        try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Group> query = cb.createQuery(Group.class);
            Root<Group> groupRoot = query.from(Group.class);

            // Join with users via the many-to-many relationship
            Join<Group, User> userJoin = groupRoot.join("users");

            // WHERE condition: user.id = :userId
            query.select(groupRoot)
                .where(cb.equal(userJoin.get("id"), value));
            List<Group> groups = session.createQuery(query).getResultList();


            for (Group elem : groups) {
              Set<Attribute> attributes = elem.toAttributes();
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
        try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
          for (var value : attributeName.getValue()) {
            var group =
                session.createQuery("from Group where id = :id", Group.class).setParameter(
                "id",
                value).getSingleResult();
            if (group != null) {
              Set<Attribute> attributes = processGroupAttributesWithMembers(group);
              ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
              resultsHandler.handle(obj);
            }
          }
        } catch (NoResultException noresult) {
          System.out.println(noresult.getMessage());
        }
        return;
      }
      if (attributeName.getName().equals(Name.NAME)) {
        try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
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

      try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Group> query = cb.createQuery(Group.class);
        Root<Group> groupRoot = query.from(Group.class);

        // Fetch users for each group (LEFT JOIN)
        groupRoot.fetch("users", JoinType.LEFT);

        query.select(groupRoot).distinct(true);

        List<Group> groups = session.createQuery(query).getResultList();

        for (Group group : groups) {
          {

            cb = session.getCriteriaBuilder();
            query = cb.createQuery(Group.class);
            groupRoot = query.from(Group.class);

            // Fetch users for each group (LEFT JOIN)
            groupRoot.fetch("users", JoinType.LEFT);

            query.select(groupRoot).distinct(true);

            groups = session.createQuery(query).getResultList();

            if (groups.isEmpty()) {
              return;
            }

            Set<Attribute> attributes = processGroupAttributesWithMembers(group);
            ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
            resultsHandler.handle(obj);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  private Set<Attribute> processGroupAttributesWithMembers(Group group) {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(AttributeBuilder.build(Uid.NAME, Long.toString(group.getId())));
    attributes.add(AttributeBuilder.build(Name.NAME, group.getGroupName()));
    attributes.add(AttributeBuilder.build("description", group.getDescription()));
    attributes.add(AttributeBuilder.build("groupName", group.getGroupName()));
    if (group.getUsers() != null && !group.getUsers().isEmpty()) {
      // get all the ids as string from the users and return the list as members
      List<String> members =
          group.getUsers().stream().map(User::getId).map(String::valueOf).toList();
      attributes.add(AttributeBuilder.build("members", members));
    } else {
      attributes.add(AttributeBuilder.build("members", new ArrayList<>()));
    }

    return attributes;
  }

  @Override
  public void close() throws Exception {
    this.sessionFactoryService.sessionFactory.close();
  }

  @Override
  public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
        var tran = session.beginTransaction();
        var id = uid.getValue().getFirst().toString();
        Group group = session.get(Group.class, Long.parseLong(id));
        if (group != null) {
          session.remove(group);
          tran.commit();
        }
        return;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      var id = uid.getValue().getFirst().toString();
      try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
        User user = session.get(User.class, Long.parseLong(id));
        if (user != null) {
          var tran = session.beginTransaction();
          session.remove(user);
          tran.commit();
        }
        return;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    LOG.warn("Unknown object class: " + objectClass.getObjectClassValue());
  }

  @Override
  public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid,
                                         Set<AttributeDelta> set,
                                         OperationOptions operationOptions) {
    var typename = objectClass.getObjectClassValue();
    if (typename.equals(ObjectClass.GROUP_NAME)) {
      return updateGroupAttributes(uid, set);

    }
    if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
      return updateUserAttributes(uid, set);
    }
    return Set.of();
  }

  private Set<AttributeDelta> updateUserAttributes(Uid uid, Set<AttributeDelta> set) {
    try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
      var id = uid.getValue().getFirst().toString();
      User user = session.get(User.class, Long.parseLong(id));
      user.parseAttributesDelta(set);
      var tran = session.beginTransaction();
      session.persist(user);
      tran.commit();
      return set;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Set<AttributeDelta> updateGroupAttributes(Uid uid, Set<AttributeDelta> set) {
    try (var session = this.sessionFactoryService.sessionFactory.openSession()) {
      var groupId = uid.getValue().getFirst().toString();
      if (!set.isEmpty()) {
        for (var s : set) {
          if (s.getName().equals("members")) {
            updateMembersByUidValues(s, session, groupId);
            continue;
          }
          if (s.getName().equals(Name.NAME) || s.getName().equals(Uid.NAME)) {
            handleName(s, session, groupId);
            continue;
          }

          var updateGroup = session.get(Group.class, Long.parseLong(groupId));
          if (updateGroup != null) {
            if (s.getName().equals("groupName")) {
              updateGroup.setGroupName(s.getValuesToReplace().getFirst().toString());
            }
            if (s.getName().equals("description")) {
              updateGroup.setGroupName(s.getValuesToReplace().getFirst().toString());
            }
          }
          var tran = session.beginTransaction();
          session.persist(updateGroup);
          tran.commit();
        }
        return set;
      }
      return set;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void handleName(AttributeDelta s, Session session, String groupName) {
    String queryString = "from UserGroupNames where userName = :userName and groupName = " +
        ":groupName";
    String userNameColumn = "userName";
    String groupNameColumn = "groupName";
    if (s.getValuesToAdd() != null && !s.getValuesToAdd().isEmpty()) {
      for (var v : s.getValuesToAdd()) {
        // use the criteria builder to find the combination of userid and group in the
        // UserGroups table
//        var q = session.createQuery(queryString, UserGroup.class);
//        q.setParameter(userNameColumn, v.toString());
//        q.setParameter(groupNameColumn, groupName);
//        List<UserGroup> result = q.getResultList();
//        if (result.isEmpty()) {
//          session.persist(new UserGroup(v.toString(), groupName));
//        }
      }
    }
    if (s.getValuesToRemove() != null && !s.getValuesToRemove().isEmpty()) {
      for (var v : s.getValuesToRemove()) {
        // check if the combination of userid and group exists in the UserGroups table
//        var foundCombo = session.createQuery(queryString, UserGroup.class)
//            .setParameter(userNameColumn, v.toString())
//            .setParameter(groupNameColumn, groupName).getResultList();
//        if (foundCombo.isEmpty()) {
//          continue;
//        }
//        for (UserGroup userGroup : foundCombo) {
//          session.remove(userGroup);
//        }
      }
    }
  }

  private void updateMembersByUidValues(AttributeDelta s, Session session, String groupId) {
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Group> query = cb.createQuery(Group.class);
    Root<Group> groupRoot = query.from(Group.class);

    // Fetch users along with the group (avoids LazyInitializationException)
    groupRoot.fetch("users", JoinType.LEFT);

    // WHERE condition: Find the specific group
    query.select(groupRoot).where(cb.equal(groupRoot.get("id"), groupId));

    Group group = session.createQuery(query).uniqueResult();

    if (s.getValuesToAdd() != null && !s.getValuesToAdd().isEmpty()) {
      for (var v : s.getValuesToAdd()) {
        if (group.getUsers().stream().anyMatch(user -> user.getId() == Integer.parseInt(s.getValuesToAdd().getFirst().toString()))) {
          return;
        } else {
          var user = session.get(User.class,
              Long.parseLong(s.getValuesToAdd().getFirst().toString()));
          user.getGroups().add(group);
          var tran = session.beginTransaction();
          session.persist(user);
          tran.commit();
        }
      }
    }
    if (s.getValuesToRemove() != null && !s.getValuesToRemove().isEmpty()) {
      for (var v : s.getValuesToRemove()) {
        // check if the combination of userid and group exists in the UserGroups table
        if (group.getUsers().stream().anyMatch(user -> user.getId() == Integer.parseInt(v.toString()))) {
          var tran = session.beginTransaction();
          var userFound = session.get(User.class, Long.parseLong(v.toString()));
          var count = group.getUsers().stream().count();
          userFound.getGroups().remove(group);
          group.getUsers().remove(userFound);
          count = group.getUsers().stream().count();
          session.persist(group);
          tran.commit();
        }
      }
    }
  }
}
