package sernet.gs.reveng;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MSchutzbedarfkategTxt entities. Transaction control of the save(), update()
 * and delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MSchutzbedarfkategTxt
 * @author MyEclipse Persistence Tools
 */

public class MSchutzbedarfkategTxtDAO extends BaseHibernateDAO {
	private static final Log log = LogFactory
			.getLog(MSchutzbedarfkategTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String CHANGED_BY = "changedBy";
	public static final String NOTIZ_ID = "notizId";

	public void save(MSchutzbedarfkategTxt transientInstance) {
		log.debug("saving MSchutzbedarfkategTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MSchutzbedarfkategTxt persistentInstance) {
		log.debug("deleting MSchutzbedarfkategTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MSchutzbedarfkategTxt findById(
			sernet.gs.reveng.MSchutzbedarfkategTxtId id) {
		log.debug("getting MSchutzbedarfkategTxt instance with id: " + id);
		try {
			MSchutzbedarfkategTxt instance = (MSchutzbedarfkategTxt) getSession()
					.get("sernet.gs.reveng.MSchutzbedarfkategTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MSchutzbedarfkategTxt instance) {
		log.debug("finding MSchutzbedarfkategTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MSchutzbedarfkategTxt").add(
					Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding MSchutzbedarfkategTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MSchutzbedarfkategTxt as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByName(Object name) {
		return findByProperty(NAME, name);
	}

	public List findByBeschreibung(Object beschreibung) {
		return findByProperty(BESCHREIBUNG, beschreibung);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByChangedBy(Object changedBy) {
		return findByProperty(CHANGED_BY, changedBy);
	}

	public List findByNotizId(Object notizId) {
		return findByProperty(NOTIZ_ID, notizId);
	}

	public List findAll() {
		log.debug("finding all MSchutzbedarfkategTxt instances");
		try {
			String queryString = "from MSchutzbedarfkategTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MSchutzbedarfkategTxt merge(MSchutzbedarfkategTxt detachedInstance) {
		log.debug("merging MSchutzbedarfkategTxt instance");
		try {
			MSchutzbedarfkategTxt result = (MSchutzbedarfkategTxt) getSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MSchutzbedarfkategTxt instance) {
		log.debug("attaching dirty MSchutzbedarfkategTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MSchutzbedarfkategTxt instance) {
		log.debug("attaching clean MSchutzbedarfkategTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}