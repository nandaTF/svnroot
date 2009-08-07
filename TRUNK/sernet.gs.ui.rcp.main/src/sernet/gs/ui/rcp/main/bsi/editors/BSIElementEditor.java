/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.hibernate.StaleObjectStateException;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;

/**
 * Editor for all BSI elements with attached HUI entities.
 * 
 * Uses the HUI framework to edit all properties defined
 * in the entity's xml description (SNCA.xml)
 * 
 * @author koderman@sernet.de
 *
 */
public class BSIElementEditor extends EditorPart {
	public static final String EDITOR_ID = "sernet.gs.ui.rcp.main.bsi.editors.bsielementeditor";
	private HitroUIComposite huiComposite;
	private boolean isModelModified = false;
	
	private IEntityChangedListener modelListener = new IEntityChangedListener() {

		public void dependencyChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			// not relevant
		}
		
		public void selectionChanged(IMLPropertyType arg0, IMLPropertyOption arg1) {
			modelChanged();
		}

		public void propertyChanged(PropertyChangedEvent evt) {
			modelChanged();
		}

	};
	private CnATreeElement cnAElement;
	
	
	public void doSave(IProgressMonitor monitor) {
		if (isModelModified) {
			monitor.beginTask("Speichern", IProgressMonitor.UNKNOWN);
			save(true);
			monitor.done();
		}
	}
	
	

	private void save(boolean completeRefresh) {
		BSIElementEditorInput editorinput = (BSIElementEditorInput) getEditorInput();
		try {
			CnAElementHome.getInstance().update(cnAElement);
			isModelModified = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
			
			// notify all views of change:
			CnAElementFactory.getLoadedModel().childChanged(cnAElement.getParent(), cnAElement);
			
			// cause complete refresh, necessary for viewers to call getchildren etc.
			if (completeRefresh)
				CnAElementFactory.getLoadedModel().refreshAllListeners(IBSIModelListener.SOURCE_EDITOR);
			
		} catch (StaleObjectStateException se) {
			// close editor, loosing changes:
			ExceptionUtil.log(se, "Fehler beim Speichern.");
		}
		catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Speichern.");
		}
	}

	@Override
	public void doSaveAs() {
		// not supported
	}
	
	void modelChanged() {
		boolean wasDirty = isDirty();
		isModelModified = true;
		
		if (!wasDirty)
			firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (! (input instanceof BSIElementEditorInput))
			throw new PartInitException("invalid input");
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}
	
	private void initContent() {
		try {
			cnAElement = ((BSIElementEditorInput)getEditorInput()).getCnAElement();
			RefreshElement<CnATreeElement> command = new RefreshElement<CnATreeElement>(cnAElement);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			cnAElement = command.getElement();
			
			Entity entity = cnAElement.getEntity();
			EntityType entityType = HitroUtil.getInstance().getTypeFactory()
				.getEntityType(entity.getEntityType());
			// add listener to mark editor as dirty on changes:
			entity.addChangeListener(this.modelListener);
			huiComposite.createView(entity, true, true);
			InputHelperFactory.setInputHelpers(entityType, huiComposite);
			huiComposite.resetInitialFocus();
		} catch (Exception e) {
			ExceptionUtil.log(e, "Konnte BSI Element Editor nicht öffnen");
		}
		
	}
	
	



	@Override
	public boolean isDirty() {
		return isModelModified;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		huiComposite = new HitroUIComposite(parent, SWT.NULL, false);
		initContent();
		// if opened the first time, save initialized entity:
		if (isDirty())
			save(false);
			
	}
	
	public boolean isNotAskAndSave() {
		return true;
	}

	@Override
	public void setFocus() {
		//huiComposite.setFocus();
		huiComposite.resetInitialFocus();
	}
	
	@Override
	public void dispose() {
		huiComposite.closeView();
		cnAElement.getEntity().removeListener(modelListener);
		EditorRegistry.getInstance().closeEditor(
				( (BSIElementEditorInput)getEditorInput() ).getId()
				);
		super.dispose();
	}

}
