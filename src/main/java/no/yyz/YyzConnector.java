package no.yyz;

import no.yyz.models.User;
import no.yyz.services.UserService;
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
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@ConnectorClass(
        displayNameKey = "UI_CONNECTOR_NAME",
        configurationClass = YyzConfiguration.class
)
public class YyzConnector implements org.identityconnectors.framework.api.operations.TestApiOp,
        PoolableConnector,
        CreateOp,
        SearchOp<Filter>,
        SchemaOp {

    private static int LoopCounter = 0;
    private static final Log LOG = Log.getLog(YyzConnector.class);
    private YyzConfiguration configuration;
    private final UserService userservice = new UserService();

    @Override
    public void test() {
        LOG.info("This is a test");
        LOG.warn(("This is a warn"));
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
        ObjectClassInfoBuilder userBuilder = User.ObjectInfoBuilder();
        schemaBuilder.defineObjectClass(userBuilder.build());
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

        User user = new User();
        for (Attribute attribute : attributes) {
            String name = attribute.getName();
            List<Object> value = attribute.getValue();
            Object firstValue = null;
            if (!value.isEmpty()) {
                firstValue = value.getFirst();
            }
            switch (name.toLowerCase()) {
                case "email": {
                    if (firstValue != null) {
                        user.setEmail(firstValue.toString());
                    }
                    break;
                }
                case "username": {
                    if (firstValue != null) {
                        user.setUsername(firstValue.toString());
                    }
                    break;
                }
                case "givenname": {
                    if (firstValue != null) {
                        user.setGivenName(firstValue.toString());
                    }
                    break;
                }
                case "lastname": {
                    if (firstValue != null) {
                        user.setLastName(firstValue.toString());
                    }
                    break;
                }
                case "fullname": {
                    if (firstValue != null) {
                        user.setFullName(firstValue.toString());
                    }
                    break;
                }
                default:
                    break;
            }
        }
        try {
            user = this.userservice.persist(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int userId = user.getId();
        return new Uid(Integer.toString(userId));
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
            if (filter instanceof EqualsFilter equalsFilter) {
                var attributeName = equalsFilter.getAttribute();
                if (attributeName.getName().equals(Uid.NAME)) {
                    for (var value : attributeName.getValue()) {
                        var user = this.userservice.getById(Integer.parseInt(value.toString()));
                        if (user != null) {
                            Set<Attribute> attributes = new HashSet<>();
                            attributes.add(AttributeBuilder.build("username", user.getUsername()));
                            attributes.add(AttributeBuilder.build("email", user.getEmail()));
                            attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(user.getId())));
                            attributes.add(AttributeBuilder.build(Name.NAME, user.getEmail()));
                            ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
                            resultsHandler.handle(obj);
                        }
                    }
                    return;
                }
            }
            List<User> list = this.userservice.getAll();
            for (User user : list) {
                Set<Attribute> attributes = new HashSet<>();
                attributes.add(AttributeBuilder.build("username", user.getUsername()));
                attributes.add(AttributeBuilder.build("email", user.getEmail()));
                attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(user.getId())));
                attributes.add(AttributeBuilder.build(Name.NAME, user.getEmail()));
                ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
                resultsHandler.handle(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
