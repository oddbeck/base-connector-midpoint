import jakarta.persistence.EntityManager;
import no.yyz.hibernate.HibernateUtil;
import no.yyz.models.User;
import no.yyz.services.UserService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HibernateUtilTest {
    @Test
    public void UtilTest() {
        var userid = 0;
        UserService userService = new UserService();
        var us = new User("odd", "email");
        var us2 = userService.persist(us);

        userid = us2.getId();
        var sessionFactory = HibernateUtil.getSessionFactory();
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
    }
}
