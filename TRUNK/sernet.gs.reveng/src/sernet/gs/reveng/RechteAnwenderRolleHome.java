package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:34 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class RechteAnwenderRolle.
 * @see sernet.gs.reveng.RechteAnwenderRolle
 * @author Hibernate Tools
 */
public class RechteAnwenderRolleHome {

	private static final Log log = LogFactory
			.getLog(RechteAnwenderRolleHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(RechteAnwenderRolle transientInstance) {
		log.debug("persisting RechteAnwenderRolle instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(RechteAnwenderRolle instance) {
		log.debug("attaching dirty RechteAnwenderRolle instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(RechteAnwenderRolle instance) {
		log.debug("attaching clean RechteAnwenderRolle instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(RechteAnwenderRolle persistentInstance) {
		log.debug("deleting RechteAnwenderRolle instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public RechteAnwenderRolle merge(RechteAnwenderRolle detachedInstance) {
		log.debug("merging RechteAnwenderRolle instance");
		try {
			RechteAnwenderRolle result = (RechteAnwenderRolle) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public RechteAnwenderRolle findById(
			sernet.gs.reveng.RechteAnwenderRolleId id) {
		log.debug("getting RechteAnwenderRolle instance with id: " + id);
		try {
			RechteAnwenderRolle instance = (RechteAnwenderRolle) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.RechteAnwenderRolle", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(RechteAnwenderRolle instance) {
		log.debug("finding RechteAnwenderRolle instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.RechteAnwenderRolle")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
