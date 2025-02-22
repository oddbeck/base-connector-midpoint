package no.yyz;

import no.yyz.hibernateutil.services.UserService;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.List;
import java.util.Random;
import java.util.Set;


@ConnectorClass(
        displayNameKey = "UI_CONNECTOR_NAME",
        configurationClass = YyzConfiguration.class
)
public class YyzConnector implements AutoCloseable, org.identityconnectors.framework.api.operations.TestApiOp,
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
        ObjectClassInfoBuilder accountBuilder = new ObjectClassInfoBuilder();
        accountBuilder.setType(ObjectClass.ACCOUNT_NAME);

        AttributeInfo username = new AttributeInfoBuilder("username", String.class).setRequired(true).build();

        accountBuilder.addAttributeInfo(username);

        accountBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("email", String.class));

        accountBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("givenName", String.class));

        accountBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("lastName", String.class));

        accountBuilder.addAttributeInfo(
                AttributeInfoBuilder.build("fullName", String.class));


        schemaBuilder.defineObjectClass(accountBuilder.build());

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

        return new Uid(Integer.toString(new Random().nextInt()));
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
    }

    @Override
    public void close() throws Exception {
        this.userservice.close();
    }
}
