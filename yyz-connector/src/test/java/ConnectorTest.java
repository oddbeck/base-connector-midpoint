import no.yyz.YyzConnector;
import no.yyz.hibernateutil.services.UserService;
import no.yyz.models.models.User;
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
  public void Test() throws Exception {
    YyzConnector connector = new YyzConnector();
    connector.test();
    connector.close();
  }

  @Test
  public void Fail() throws SQLException, Exception {

    UserService storage = new UserService();
    User user = storage.persist(new User("odd", "oddbeck@gmail.com"));
    User newUser = storage.getById(user.getId());
    Assert.assertEquals(user.getId(), newUser.getId());
    storage.close();
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

    ObjectClass oclass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    conn.create(oclass, attributeSet, null);
  }

//  @Test
//  public void UsersAndGroupTest() {
//    UserGroupsService service = new UserGroupsService();
//    try (var session = service.sessionFactory.openSession()) {
//      User user = new User("oddis", "oddis@yyz.no");
//      Group group = new Group("oddis", "oddis@yyz.no");
//
//      session.persist(user);
//      session.persist(group);
//
//      session.persist(new UserGroup(user.getId(), group.getId()));
//    }
//
//  }
}
