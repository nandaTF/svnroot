package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;

public class OwnGefaehrdungHome {
	
	private ICommandService commandService;
	private static OwnGefaehrdungHome instance;

	private OwnGefaehrdungHome() {
		commandService = ServiceFactory.lookupCommandService();
	}
	
	public synchronized static OwnGefaehrdungHome getInstance() {
		if (instance == null)
			instance = new OwnGefaehrdungHome();
		return instance;
	}
	
	public void save(OwnGefaehrdung gef) throws Exception {
		SaveElement<OwnGefaehrdung> command = new SaveElement<OwnGefaehrdung>(gef);
		commandService.executeCommand(command);
	}
	
	public void remove(OwnGefaehrdung gef) throws Exception {
		RemoveElement<OwnGefaehrdung> command = new RemoveElement<OwnGefaehrdung>(gef);
		commandService.executeCommand(command);
	}
	
	public List<OwnGefaehrdung> loadAll() throws RuntimeException {
		LoadElementByType<OwnGefaehrdung> command = new LoadElementByType<OwnGefaehrdung>(OwnGefaehrdung.class);
		commandService.executeCommand(command);
		return command.getElements();
	}
	
	
}
