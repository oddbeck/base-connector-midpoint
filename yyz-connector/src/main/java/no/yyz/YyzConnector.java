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
import no.yyz.models.models.UserGroups;
import org.hibernate.Session;
import org.hibernate.internal.build.AllowSysOut;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
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
public class YyzConnector implements AutoCloseable, org.identityconnectors.framework.api.operations.TestApiOp,
        PoolableConnector,
        CreateOp,
        UpdateOp,
        DeleteOp,
        UpdateAttributeValuesOp,
        UpdateDeltaOp,
        SearchOp<Filter>,
        SchemaOp {

    private static int LoopCounter = 0;
    private static final Log LOG = Log.getLog(YyzConnector.class);
    private YyzConfiguration configuration;
    private final UserService userservice = new UserService();
    private final GroupService groupService = new GroupService();
    private final UserGroupsService userGroupsService = new UserGroupsService();


    @Override
    public void test() {
        LOG.info("This is a test");
        LOG.warn(("This is a warn"));
        LOG.error("This is an error");
        LOG.error("This is an error");
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (YyzConfiguration) configuration;
        LOG.info("This is a test");
        LoopCounter++;
        if (LoopCounter < 5) {
            LOG.info("Before I call test() manually.");
            test();
        }
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
//        OperationalAttributes attributes;
        return schemaBuilder.build();
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
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
            return handleGroupCreate(attributes, operationOptions);
        }

        if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
            return handleUserCreate(attributes, operationOptions);
        }
        throw new RuntimeException("Unknown typename: " + typename);
    }

    private Uid handleUserCreate(Set<Attribute> attributes, OperationOptions operationOptions) {
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

    private Uid handleGroupCreate(Set<Attribute> attributes, OperationOptions operationOptions) {
        Group group = new Group();
        group.parseAttributes(attributes);
        try (var session = this.groupService.sessionFactory.openSession()) {
            var foundGroup = this.groupService.findGroupByName(group.getGroupName(), session);
            if (foundGroup != null) {
                foundGroup.parseAttributes(attributes);
                foundGroup = this.groupService.persist(foundGroup);
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
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions operationOptions) {
        return new FilterTranslator<Filter>() {
            @Override
            public List<Filter> translate(Filter filter) {
                return CollectionUtil.newList(filter);
            }
        };
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter filter, ResultsHandler resultsHandler, OperationOptions operationOptions) {
        try {
            if (objectClass.getObjectClassValue().equals(ObjectClass.GROUP_NAME)) {
                handleGroupQuery(filter, resultsHandler, operationOptions);
                return;
            }
            if (objectClass.getObjectClassValue().equals(ObjectClass.ACCOUNT_NAME)) {
                handleUserQuery(filter, resultsHandler, operationOptions);
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOG.warn("Unknown object class: " + objectClass.getObjectClassValue());
    }

    private void handleUserQuery(Filter filter, ResultsHandler resultsHandler, OperationOptions operationOptions) {
        if (filter instanceof EqualsFilter equalsFilter) {
            var attributeName = equalsFilter.getAttribute();
            if (attributeName.getName().equals(Uid.NAME)) {
                for (var value : attributeName.getValue()) {
                    var user = this.userservice.getById(Integer.parseInt(value.toString()));
                    if (user != null) {
                        Set<Attribute> attributes = user.toAttributes();
                        ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
                        resultsHandler.handle(obj);
                    }
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

    private void handleGroupQuery(Filter filter, ResultsHandler resultsHandler, OperationOptions operationOptions) {
        if (filter instanceof ContainsAllValuesFilter) {
            var attributeName = ((ContainsAllValuesFilter) filter).getAttribute();
            if (attributeName.getName().equals("members")) {
                try (var session = this.groupService.sessionFactory.openSession()) {
                    for (var value : attributeName.getValue()) {
                        List<Integer> groupIds = getGroupIdsFromUserId(value, session);
                        // get all groups from  groupIds the previous query
                        try {
                            if (groupIds == null || groupIds.isEmpty()) {
                                return;
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        // use the query builder for the code below
                        List<Group> foundGroups = getGroupsByIds(session, groupIds);

                        for (Group group : foundGroups) {
                            Set<Attribute> attributes = processGroupAttributesWithMembers(group);
                            ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
                            boolean handlerResult =resultsHandler.handle(obj);
                            if (!handlerResult) {
                                System.out.println("break, can't handle anymore " + handlerResult);
                                break;
                            }
                            //System.out.println(handlerResult);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return;
        }
        if (filter instanceof EqualsFilter equalsFilter) {
            var attributeName = equalsFilter.getAttribute();
            if (attributeName.getName().equals(Uid.NAME)) {
                for (var value : attributeName.getValue()) {
                    var group = this.groupService.getById(Integer.parseInt(value.toString()));
                    if (group != null) {
                        Set<Attribute> attributes = processGroupAttributesWithMembers(group);
                        ConnectorObject obj = new ConnectorObject(ObjectClass.GROUP, attributes);
                        resultsHandler.handle(obj);
                    }
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

    /**
     * Retrieves a list of Group objects from the database given a list of group IDs.
     * <p>
     * This method takes a list of group IDs and returns a list of the corresponding
     * Group objects. It uses the Criteria API to efficiently query the database.
     * <p>
     * The method takes two parameters: a Hibernate session and a list of group IDs.
     * The session is used to execute the query and retrieve the results. The list
     * of group IDs is used to construct the query.
     * <p>
     * The method returns a list of Group objects. The list is empty if no groups
     * were found with the given IDs.
     *
     * @param session  The Hibernate session to use to execute the query.
     * @param groupIds The list of group IDs to retrieve.
     * @return A list of Group objects that match the given IDs.
     */
    private static List<Group> getGroupsByIds(Session session, List<Integer> groupIds) {
        CriteriaBuilder cbAllGroups = session.getCriteriaBuilder();
        CriteriaQuery<Group> groupQuery = cbAllGroups.createQuery(Group.class);
        Root<Group> groupRoot = groupQuery.from(Group.class);
        var res = groupQuery.select(groupRoot).where(groupRoot.get("id").in(groupIds));
        return session.createQuery(res).getResultList();
    }

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
        Root<UserGroups> root = groupsQuery.from(UserGroups.class);
        groupsQuery.select(root.get("groupId")).where(allGroupsFromUserid.equal(root.get("userId"), Integer.parseInt(value.toString())));
        return session.createQuery(groupsQuery).getResultList();
    }


    private Set<Attribute> processGroupAttributesWithMembers(Group group) {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(group.getId())));
        attributes.add(AttributeBuilder.build(Name.NAME, group.getGroupName()));
        attributes.add(AttributeBuilder.build("description", group.getDescription()));
        attributes.add(AttributeBuilder.build("groupName", group.getGroupName()));
        var membersSearch = this.userGroupsService.sessionFactory.openSession();

        List<Integer> query = membersSearch.createQuery("Select userId from UserGroups where groupId = :groupId", Integer.class)
                .setParameter("groupId", group.getId())
                .getResultList();
//        var membersBuilder = new AttributeBuilder();
//        // add new values array with a single value
//        membersBuilder.setName("members");
//        membersBuilder.addValue(query);
        if (membersSearch.isOpen()) {
            membersSearch.close();
        }
        return attributes;
    }

    @Override
    public void close() throws Exception {
        this.userservice.close();
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> set, OperationOptions operationOptions) {
        var typename = objectClass.getObjectClassValue();
        if (typename.equals(ObjectClass.GROUP_NAME)) {
            try (var session = this.groupService.sessionFactory.openSession()) {
                var id = uid.getValue().getFirst().toString();
                Group group = this.groupService.getById(Integer.parseInt(id), session);
                group.parseAttributes(set);
                this.groupService.persist(group, session);
                List<Integer> userIds = this.userGroupsService.getGroupMembersByGroupId(Integer.parseInt(id), session);

                // delete all elements in table UsersGroups where groupId = id using hibernate query builder
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaDelete<UserGroups> delete = builder.createCriteriaDelete(UserGroups.class);
                Root<UserGroups> root = delete.from(UserGroups.class);
                delete.where(builder.equal(root.get("groupId"), Integer.parseInt(id)));
                session.createMutationQuery(delete).executeUpdate();

                for (Integer userId : userIds) {
                    this.userGroupsService.persist(new UserGroups(userId, group.getId()), session);
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
            var id = uid.getValue().getFirst().toString();
            try (var session = this.userservice.sessionFactory.openSession()) {
                User user = this.userservice.getById(Integer.parseInt(id), session);
                this.userservice.delete(user, session);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Uid addAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> set, OperationOptions operationOptions) {
        update(objectClass, uid, set, operationOptions);
        return uid;
    }

    @Override
    public Uid removeAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> set, OperationOptions operationOptions) {
        update(objectClass, uid, set, operationOptions);
        return uid;
    }

    @Override
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> set, OperationOptions operationOptions) {
        var typename = objectClass.getObjectClassValue();
        if (typename.equals(ObjectClass.GROUP_NAME)) {
            try (var session = this.groupService.sessionFactory.openSession()) {
                var id = uid.getValue().getFirst().toString();
                Group group = this.groupService.getById(Integer.parseInt(id), session);
                //group.parseAttributes(set);
                this.groupService.persist(group, session);
                return Set.of();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (typename.equals(ObjectClass.ACCOUNT_NAME)) {
            try (var session = this.userservice.sessionFactory.openSession()) {
                var id = uid.getValue().getFirst().toString();
                User user = this.userservice.getById(Integer.parseInt(id), session);
                //user.parseAttributes(set);
                this.userservice.persist(user, session);
                return Set.of();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Set.of();
    }
}
