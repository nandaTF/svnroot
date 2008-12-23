package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;


public interface IBaseDao<T, ID extends Serializable> {

		 public void saveOrUpdate(T entity);
		 
		 public T merge(T entity);

		 public void delete(T entity);

		 public T findById(ID id);

		 public List<T> findAll();
		 
		 public List findByQuery(String hqlQuery, Object[] params);
		 
		 public void refresh(T element);

		 public void initialize(Object collection);
		 
		 public void flush();
		   
}
