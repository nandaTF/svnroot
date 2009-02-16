package sernet.gs.reveng;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MsCmState entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MsCmState
 * @author MyEclipse Persistence Tools
 */

public class MsCmStateDAO extends BaseHibernateDAO {
	private static final Log log = LogFactory.getLog(MsCmStateDAO.class);
	// property constants
	public static final String GUID = "guid";

	public void save(MsCmState transientInstance) {
		log.debug("saving MsCmState instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MsCmState persistentInstance) {
		log.debug("deleting MsCmState instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MsCmState findById(java.lang.Short id) {
		log.debug("getting MsCmState instance with id: " + id);
		try {
			MsCmState instance = (MsCmState) getSession().get(
					"sernet.gs.reveng.MsCmState", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MsCmState instance) {
		log.debug("finding MsCmState instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MsCmState").add(Example.create(instance))
					.list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding MsCmState instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MsCmState as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findAll() {
		log.debug("finding all MsCmState instances");
		try {
			String queryString = "from MsCmState";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MsCmState merge(MsCmState detachedInstance) {
		log.debug("merging MsCmState instance");
		try {
			MsCmState result = (MsCmState) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MsCmState instance) {
		log.debug("attaching dirty MsCmState instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MsCmState instance) {
		log.debug("attaching clean MsCmState instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}