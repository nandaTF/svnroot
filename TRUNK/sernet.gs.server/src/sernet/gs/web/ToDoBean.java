/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package sernet.gs.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.convert.Converter;

import org.apache.log4j.Logger;
import org.openfaces.util.FacesUtil;

import sernet.gs.server.RuntimeLogger;
import sernet.gs.server.ServerInitializer;
import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForITVerbund;
import sernet.hui.common.VeriniceContext;

/**
 * @author $Autor$
 *
 */
public class ToDoBean {

	final static Logger LOG = Logger.getLogger(ToDoBean.class);
	
	List<ITVerbund> itVerbundList;
	
	ITVerbund selectedItVerbund;
	
	String selectedItVerbundTitel;
	
	List<TodoViewItem> todoList;
	
	TodoViewItem selectedItem;
	
	MassnahmenUmsetzung massnahmeUmsetzung;
	
	Converter umsetzungConverter = new UmsetzungConverter();
	
	Converter itVerbundConverter = new ItVerbundConverter(this);
	
	List<String> executionList;
	
	boolean executionNo = true;
	boolean executionYes = false;
	boolean executionPartly = true;
	boolean executionDispensable = false;					    
	boolean executionUntreated = true;
	
	boolean sealA = true;
	boolean sealB = true;
	boolean sealC = true;
	boolean sealZ = true;
		
	public ToDoBean() {
		super();
		executionList = Arrays.asList(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH,MassnahmenUmsetzung.P_UMSETZUNG_JA,MassnahmenUmsetzung.P_UMSETZUNG_NEIN,MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE,MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET);
		loadItVerbundList();
	}
	
	public void loadItVerbundList() {
		ServerInitializer.inheritVeriniceContextState();
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		try {
			service.executeCommand(command);
			setItVerbundList(command.getElements());
		} catch (CommandException e) {
			LOG.error("Error while loading IT-Verbuende", e);
		}	
	}

	public void loadToDoList() {
		setSelectedItVerbund((ITVerbund) itVerbundConverter.getAsObject(null, null, getSelectedItVerbundTitel()));
		setSelectedItem(null);
		setMassnahmeUmsetzung(null);
		Integer itVerbundId = (getSelectedItVerbund()==null) ? null : getSelectedItVerbund().getDbId();
		if(itVerbundId!=null) {
			ServerInitializer.inheritVeriniceContextState();
			ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
			FindMassnahmenForITVerbund command = new FindMassnahmenForITVerbund(itVerbundId);
			command.setExecutionSet(createExecutionSet());
			command.setSealSet(createSealSet());
			try {
				service.executeCommand(command);
				setTodoList(command.getAll());
			} catch (CommandException e) {
				LOG.error("Error while loading todos for id: " + itVerbundId, e);
			}
			setTodoList(command.getAll());
		}
	}
	
	private Set<String> createExecutionSet() {
		Set<String> executionSet = new HashSet<String>(5);
		if(isExecutionDispensable()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
		}
		if(isExecutionNo()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_NEIN);
		}
		if(isExecutionPartly()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE);
		}
		if(isExecutionUntreated()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET);
		}
		if(isExecutionYes()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_JA);
		}
		return executionSet;
	}
	
	private Set<String> createSealSet() {
		Set<String> sealSet = new HashSet<String>(4);
		if(isSealA()) {
			sealSet.add("A");
		}
		if(isSealB()) {
			sealSet.add("B");
		}
		if(isSealC()) {
			sealSet.add("C");
		}
		if(isSealZ()) {
			sealSet.add("Z");
		}
		
		return sealSet;
	}

	public void loadToDo() {
		LOG.debug("loadToDo");
		ServerInitializer.inheritVeriniceContextState();
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		if(getSelectedItem()!=null) {
			int massnahmeId = getSelectedItem().getdbId();
			LoadCnAElementById command = new LoadCnAElementById(MassnahmenUmsetzung.class,massnahmeId);
			try {
				service.executeCommand(command);
			} catch (CommandException e) {
				LOG.error("Error while loading todos for id: " + massnahmeId, e);
			}
			MassnahmenUmsetzung result = (MassnahmenUmsetzung) command.getFound();
			if(result==null) {
				LOG.warn("No massnahme found with id: " + massnahmeId);
			} else if(LOG.isDebugEnabled()) {
				LOG.debug("Massnahme loaded, id: " + massnahmeId);
			}
			setMassnahmeUmsetzung(result);
			
		}
		else {
			LOG.warn("No todo-item selected. Can not load massnahme.");
		}
	}

	public void save() {
		LOG.debug("save");
		ServerInitializer.inheritVeriniceContextState();
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		if(getMassnahmeUmsetzung()!=null) {
			SaveElement<MassnahmenUmsetzung> command = new SaveElement<MassnahmenUmsetzung>(getMassnahmeUmsetzung());
			try {
				service.executeCommand(command);
				if(LOG.isDebugEnabled()) {
					LOG.debug("Massnahme saved, id: " + getMassnahmeUmsetzung().getDbId());
				}
				loadToDoList();
			} catch (CommandException e) {
				LOG.error("Error while saving massnahme: " + getMassnahmeUmsetzung().getDbId(), e);
			}		
		}
		else {
			LOG.warn("Massnahme is null. Can not save massnahme.");
		}
	}
	
	public void setItVerbundList(List<ITVerbund> itVerbundList) {
		this.itVerbundList = itVerbundList;
	}

	public List<ITVerbund> getItVerbundList() {
		return itVerbundList;
	}

	public void setSelectedItVerbund(ITVerbund selectedItVerbund) {
		this.selectedItVerbund = selectedItVerbund;
	}

	public ITVerbund getSelectedItVerbund() {
		return selectedItVerbund;
	}

	public String getSelectedItVerbundTitel() {
		return selectedItVerbundTitel;
	}

	public void setSelectedItVerbundTitel(String selectedItVerbundId) {
		this.selectedItVerbundTitel = selectedItVerbundId;
	}

	public String getUmsetzung() {
		String umsetzung = null;
		if(getMassnahmeUmsetzung()!=null) {
			umsetzung=getMassnahmeUmsetzung().getUmsetzung();
		}
		return umsetzung;
	}
	
	public void setUmsetzung(String umsetzung) {
		if(getMassnahmeUmsetzung()!=null) {
			getMassnahmeUmsetzung().setUmsetzung(umsetzung);
		}
	}
	
	public int getToDoListSize() {
		return getTodoList().size();
	}
	
	public TodoViewItem getItemById() {
		TodoViewItem result = null;
		int dbId = ((Integer)FacesUtil.getRequestMapValue("rowKey")).intValue();
		for (TodoViewItem item : getTodoList()) {
			if(item.getdbId()==dbId) {
				result = item;
				break;
			}
		}
		return result;
	}
	
	public List<TodoViewItem> getTodoList() {
		return todoList;
	}

	public void setTodoList(List<TodoViewItem> todoList) {
		this.todoList = todoList;
	}
	
	public TodoViewItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(TodoViewItem selectedItem) {
		this.selectedItem = selectedItem;
	}

	public MassnahmenUmsetzung getMassnahmeUmsetzung() {
		return massnahmeUmsetzung;
	}

	public void setMassnahmeUmsetzung(MassnahmenUmsetzung massnahmeUmsetzung) {
		this.massnahmeUmsetzung = massnahmeUmsetzung;
	}

	public Converter getUmsetzungConverter() {
		return umsetzungConverter;
	}

	public void setUmsetzungConverter(Converter umsetzungConverter) {
		this.umsetzungConverter = umsetzungConverter;
	}

	public Converter getItVerbundConverter() {
		return itVerbundConverter;
	}

	public void setItVerbundConverter(Converter itVerbundConverter) {
		this.itVerbundConverter = itVerbundConverter;
	}

	public List<String> getExecutionList() {
		return executionList;
	}

	public void setExecutionList(List<String> umsetzungList) {
		this.executionList = umsetzungList;
	}

	public boolean isExecutionNo() {
		return executionNo;
	}

	public void setExecutionNo(boolean executionNo) {
		this.executionNo = executionNo;
	}

	public boolean isExecutionYes() {
		return executionYes;
	}

	public void setExecutionYes(boolean executionYes) {
		this.executionYes = executionYes;
	}

	public boolean isExecutionPartly() {
		return executionPartly;
	}

	public void setExecutionPartly(boolean executionPartly) {
		this.executionPartly = executionPartly;
	}

	public boolean isExecutionDispensable() {
		return executionDispensable;
	}

	public void setExecutionDispensable(boolean executionDispensable) {
		this.executionDispensable = executionDispensable;
	}

	public boolean isExecutionUntreated() {
		return executionUntreated;
	}

	public void setExecutionUntreated(boolean executionUntreated) {
		this.executionUntreated = executionUntreated;
	}

	public boolean isSealA() {
		return sealA;
	}

	public void setSealA(boolean sealA) {
		this.sealA = sealA;
	}

	public boolean isSealB() {
		return sealB;
	}

	public void setSealB(boolean sealB) {
		this.sealB = sealB;
	}

	public boolean isSealC() {
		return sealC;
	}

	public void setSealC(boolean sealC) {
		this.sealC = sealC;
	}

	public boolean isSealZ() {
		return sealZ;
	}

	public void setSealZ(boolean sealZ) {
		this.sealZ = sealZ;
	}

	public int getSize() {
		return getTodoList()==null ? 0 : getTodoList().size();
	}
}
