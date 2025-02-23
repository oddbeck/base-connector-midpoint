package no.yyz;

import no.yyz.hibernateutil.services.GroupService;
import no.yyz.hibernateutil.services.UserGroupsService;
import no.yyz.hibernateutil.services.UserService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
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
                    return;
                }
            }
            if (objectClass.getObjectClassValue().equals(ObjectClass.ACCOUNT_NAME)) {
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
                        return;
                    }
                }
                List<User> list = this.userservice.getAll();
                for (User user : list) {
                    Set<Attribute> attributes = user.toAttributes();
                    ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
                    resultsHandler.handle(obj);
                }
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOG.warn("Unknown object class: " + objectClass.getObjectClassValue());
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
        var membersBuilder = new AttributeBuilder();
        // add new values array with a single value
        membersBuilder.setName("members");
        membersBuilder.addValue(query);
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
                Group user = this.groupService.getById(Integer.parseInt(id), session);
                user.parseAttributes(set);
                this.groupService.persist(user, session);
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
}
