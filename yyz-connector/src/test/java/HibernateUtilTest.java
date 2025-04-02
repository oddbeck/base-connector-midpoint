import jakarta.persistence.EntityManager;
import no.yyz.hibernateutil.HibernateUtil;
import no.yyz.hibernateutil.services.SessionFactoryService;
import no.yyz.models.models.Group;
import no.yyz.models.models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

public class HibernateUtilTest {

  SessionFactoryService service = new SessionFactoryService();
  @Test
  public void FindOneGroupByName() {
    try (var session = service.sessionFactory.openSession()) {

      var cb = session.getCriteriaBuilder();
      var cq = cb.createQuery(Group.class);

      var root = cq.from(Group.class);
      cq.where(cb.equal(root.get("groupName"), "amdmins"));
      var query = session.createQuery(cq);
      var group = query.getResultList().stream().findFirst().orElse(null);
      System.out.println(group);
      if (group == null) {
        Group amdmins = new Group("amdmins", "All teh admins");
        session.persist(amdmins);
        Assert.assertNotNull(amdmins);
      }
//      group.setGroupName("Testings");
//      var gr = service.update(group, group.getId());
//      Assert.assertEquals(gr.getGroupName(), "Testings");
//      Assert.assertEquals(gr.getId(), group.getId());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void TestUserGroupManyToMany() {

    User u = new User("fettskit", "email@com");
    Group g = new Group("Admins", "description");
    try (var session = service.sessionFactory.openSession()) {
      var transaction = session.beginTransaction();
      session.persist(u);
      session.persist(g);
      transaction.commit();
      transaction.begin();
      if (g.getUsers() == null) {
        g.setUsers(Set.of(u));
      } else {
        g.setUsers(Set.of(u));
      }

      if (u.getGroups() == null) {
        u.setGroups(Set.of(g));
      } else {
        u.setGroups(Set.of(g));
      }

      transaction.commit();
      transaction.begin();

      var hql = "SELECT g FROM Group g JOIN g.users u WHERE g.groupName = :groupName";
      var query = session.createQuery(hql, Group.class);
      query.setParameter("groupName", "Admins");
      var groups = query.getResultList();

      var firstgroup = groups.stream().findFirst();
      Assert.assertNotNull(firstgroup);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void UtilTest() {
    Long userid = 0L;
    var us = new User("odd", "email");
    try (var session = service.sessionFactory.openSession()) {
      var tran = session.beginTransaction();
      session.persist(us);
      tran.commit();
    }

    userid = us.getId();
    try (EntityManager em = service.sessionFactory.createEntityManager()) {
      User us3 = em.find(User.class, userid);
    } catch (Exception e) {

    }

    User argh = new User("odd", "email");
    try (Session session = service.sessionFactory.openSession()) {
      Transaction transaction = session.beginTransaction();
      session.persist(argh);
      transaction.commit();
      userid = argh.getId();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    try (Session session = service.sessionFactory.openSession()) {
      User user2 = session.byId(User.class).load(userid);
      Assert.assertNotNull(user2);
      Assert.assertEquals(user2.getId(), userid);
    }
    service.sessionFactory.close();
  }
}
