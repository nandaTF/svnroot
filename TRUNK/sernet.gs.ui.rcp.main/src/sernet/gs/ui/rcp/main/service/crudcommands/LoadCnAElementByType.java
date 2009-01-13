package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadCnAElementByType<T extends CnATreeElement> extends GenericCommand {


	private List<T> elements;
	private Class<T> clazz;

	public LoadCnAElementByType(Class<T> type) {
		this.clazz = type;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = getDaoFactory().getDAO(clazz);
		elements = dao.findAll();
		HydratorUtil.hydrateElements(dao, elements, false);
	}

	public List<T> getElements() {
		return elements;
	}
	
	

}
