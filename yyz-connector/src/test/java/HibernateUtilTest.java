import jakarta.persistence.EntityManager;
import no.yyz.hibernateutil.HibernateUtil;
import no.yyz.hibernateutil.services.GroupService;
import no.yyz.hibernateutil.services.UserGroupsService;
import no.yyz.hibernateutil.services.UserService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import no.yyz.models.models.UserGroup;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HibernateUtilTest {
    @Test
    public void FindOneGroupByName() {
        try (GroupService service = new GroupService()) {
            Group group = service.findGroupByName("amdmins");
            if (group == null) {
                Group amdmins = new Group("amdmins", "All teh admins");
                group = service.persist(amdmins);
                Assert.assertNotNull(group);
            }
            group.setGroupName("Testings");
            var gr = service.update(group, group.getId());
            Assert.assertEquals(gr.getGroupName(), "Testings");
            Assert.assertEquals(gr.getId(), group.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void TestCompoundClass() {

        try (UserGroupsService service = new UserGroupsService();
             UserService userService = new UserService();
             GroupService groupService = new GroupService()) {
            for (int i = 0; i < 10; i++) {
                User user = new User("odd", "email");
                user = userService.persist(user);
                Group group = new Group("amdmins", "All teh adminz");
                group = groupService.persist(group);
                if (user != null && group != null) {
                    UserGroup userGroup = new UserGroup(user.getId(), group.getId());
                    var newGroupMapping = service.persist(userGroup);
                    Assert.assertNotNull(newGroupMapping);
                    Assert.assertEquals(newGroupMapping.getUserId(), user.getId());
                    Assert.assertEquals(newGroupMapping.getGroupId(), group.getId());
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UtilTest() {
        var userid = 0;
        var sessionFactory = HibernateUtil.createSessionFactory("jdbc:sqlite:test.sqlite", "org.sqlite.JDBC", "org.hibernate.community.dialect.SQLiteDialect", "username", "password");
        UserService userService = new UserService();
        var us = new User("odd", "email");
        var us2 = userService.persist(us);

        userid = us2.getId();
        try (EntityManager em = sessionFactory.createEntityManager()) {
            User us3 = em.find(User.class, userid);
        } catch (Exception e) {

        }

        User argh = new User("odd", "email");
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(argh);
            transaction.commit();
            userid = argh.getId();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try (Session session = sessionFactory.openSession()) {
            User user2 = session.byId(User.class).load(userid);
            Assert.assertNotNull(user2);
            Assert.assertEquals(user2.getId(), userid);
        }
        sessionFactory.close();
    }
}
