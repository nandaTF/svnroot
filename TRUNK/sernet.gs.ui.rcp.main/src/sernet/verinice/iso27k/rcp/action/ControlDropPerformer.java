/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.rcp.ControlTransformOperation;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.iso27k.service.ITransformer;
import sernet.verinice.iso27k.service.ItemControlTransformer;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class ControlDropPerformer implements DropPerformer {

	private ICommandService commandService;
	private boolean isActive;
	private ViewPart viewPart;
	
	/**
	 * @param view 
	 * @param viewer
	 */
	public ControlDropPerformer(ViewPart view) {
		this.viewPart = view;
	}

	private static final Logger LOG = Logger.getLogger(ControlDropPerformer.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean performDrop(Object data, Object target, Viewer viewer) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("performDrop...");
		}
		boolean success = isActive();
		if(isActive()) {
			// because of validateDrop only Groups can be a target
			ControlTransformOperation operation = new ControlTransformOperation((Group) target);
			try {
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				progressService.run(true, true, operation);
			} catch (Exception e) {
				LOG.error("Error while transforming items to controls", e);
			}
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_CONTROLS_ADDED);
			if(!dontShow) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
						"Status Information", 
						operation.getNumberOfControls() + " controls added to group " + ((Group) target).getTitle(),
						"Don't show this message again (You can change this in the preferences)",
						dontShow,
						preferenceStore,
						PreferenceConstants.INFO_CONTROLS_ADDED);
				preferenceStore.setValue(PreferenceConstants.INFO_CONTROLS_ADDED, dialog.getToggleState());
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@SuppressWarnings("unchecked")
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target);
		}
		boolean valid = false;
		if(target instanceof Group) {
			valid = Arrays.asList(((Group)target).getChildTypes()).contains(Control.TYPE_ID);
		}
		return isActive=valid;
	}
	
	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
	 */
	public boolean isActive() {
		return isActive;
	}

}
