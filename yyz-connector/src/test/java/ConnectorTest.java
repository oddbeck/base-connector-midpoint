import no.yyz.YyzConnector;
import no.yyz.hibernateutil.services.SessionFactoryService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class ConnectorTest {
  @Test
  public void Test() throws Exception {
    YyzConnector connector = new YyzConnector();
    connector.test();
    connector.close();
  }

  @Test
  public void Fail() throws SQLException, Exception {

    SessionFactoryService service = new SessionFactoryService();

    try (var session = service.sessionFactory.openSession()) {
      User user = new User("odd", "oddbeck@gmail.com");
      var tran = session.beginTransaction();
      session.persist(user);
      tran.commit();
      User newUser = session.get(User.class, user.getId());
      Assert.assertEquals(user.getId(), newUser.getId());
    }
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

  @Test
  public void UsersAndGroupTest() {
    var service = new SessionFactoryService();
    try (var session = service.sessionFactory.openSession()) {
      User user = new User("oddis", "oddis@yyz.no");
      Group group = new Group("oddis", "oddis@yyz.no");

      session.persist(user);
      session.persist(group);
//      group.getUsers().add(user);
      user.getGroups().add(group);

      var tran = session.beginTransaction();
      session.persist(user);
      session.persist(group);
      tran.commit();

      var newUser = session.get(User.class, user.getId());
      assertEquals(user.getId(), newUser.getId());
      assertTrue(newUser.getGroups().contains(group));
    }

  }
}
