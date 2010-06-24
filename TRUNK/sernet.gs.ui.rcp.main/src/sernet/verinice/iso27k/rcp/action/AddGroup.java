/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.VulnerabilityGroup;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class AddGroup implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	private static final Logger LOG = Logger.getLogger(AddGroup.class);
	
	private static final Map<String, String> TITLE_FOR_TYPE;
	
	static {
		TITLE_FOR_TYPE = new HashMap<String, String>();
		TITLE_FOR_TYPE.put(AssetGroup.TYPE_ID, Messages.getString("AddGroup.0")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(AuditGroup.TYPE_ID, Messages.getString("AddGroup.1")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ControlGroup.TYPE_ID, Messages.getString("AddGroup.2")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(DocumentGroup.TYPE_ID, Messages.getString("AddGroup.3")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(EvidenceGroup.TYPE_ID, Messages.getString("AddGroup.4")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ExceptionGroup.TYPE_ID, Messages.getString("AddGroup.5")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(FindingGroup.TYPE_ID, Messages.getString("AddGroup.6")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(IncidentGroup.TYPE_ID, Messages.getString("AddGroup.7")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(IncidentScenarioGroup.TYPE_ID, Messages.getString("AddGroup.8")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(InterviewGroup.TYPE_ID, Messages.getString("AddGroup.9")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(PersonGroup.TYPE_ID, Messages.getString("AddGroup.10")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ProcessGroup.TYPE_ID, Messages.getString("AddGroup.11")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(RecordGroup.TYPE_ID, Messages.getString("AddGroup.12")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(RequirementGroup.TYPE_ID, Messages.getString("AddGroup.13")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ResponseGroup.TYPE_ID, Messages.getString("AddGroup.14")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ThreatGroup.TYPE_ID, Messages.getString("AddGroup.15")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(VulnerabilityGroup.TYPE_ID, Messages.getString("AddGroup.16")); //$NON-NLS-1$
	}
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		try {
			Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
			CnATreeElement newElement = null;

			if (sel instanceof Group) {
				Group group = (Group) sel;
				// child groups have the same type as parents
				newElement = CnAElementFactory.getInstance().saveNew(group, group.getTypeId(), null);		
			}
			if (newElement != null) {
				EditorFactory.getInstance().openEditor(newElement);
			}
		} catch (Exception e) {
			LOG.error("Could not add element group", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.getString("AddGroup.18")); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if(sel instanceof Group) {
				Group group = (Group) sel;
				// TODO - getChildTypes()[0] might be a problem for more than one type
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(group.getChildTypes()[0])));	
				action.setText( TITLE_FOR_TYPE.get(group.getTypeId())!=null ? TITLE_FOR_TYPE.get(group.getTypeId()) : Messages.getString("AddGroup.19") ); //$NON-NLS-1$
			}		
		}
	}
}
