import no.yyz.YyzConnector;
import no.yyz.models.User;
import no.yyz.services.UserService;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ConnectorTest {
    @Test
    public void Test() {
        YyzConnector connector = new YyzConnector();

        connector.test();
    }

    @Test
    public void Fail() throws SQLException {
        UserService storage = new UserService();
        User user = storage.persist(new User("odd", "oddbeck@gmail.com"));
        User newUser = storage.getById(user.getId());
        Assert.assertEquals(user.getId(), newUser.getId());
    }

    @Test
    public void TestCreate() {
        YyzConnector conn = new YyzConnector();
        Set<Attribute> attributeSet = new HashSet<>();
        attributeSet.add(AttributeBuilder.build("email", "oddis@yyz.no"));
        attributeSet.add(AttributeBuilder.build("givenName", "odd"));
        attributeSet.add(AttributeBuilder.build("lastName", "Beck"));
        attributeSet.add(AttributeBuilder.build("username", "oddb"));
        attributeSet.add(AttributeBuilder.build("fullName", "Odd Beck"));

        ObjectClass oclass = new ObjectClass("User");
        conn.create(oclass, attributeSet, null);
    }
}
