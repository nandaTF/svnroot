/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

public class UpdateAction extends Action implements IAction {
    private IWorkbenchWindow window;

    public UpdateAction(IWorkbenchWindow window) {
        this.window = window;
        setId("sernet.gs.ui.rcp.main.actions.updateaction"); //$NON-NLS-1$
        setText(Messages.UpdateAction_1);
        setToolTipText(Messages.UpdateAction_2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
            public void run() {
                UpdateJob job = new UpdateJob(Messages.UpdateAction_3, false, false);
                UpdateManagerUI.openInstaller(window.getShell(), job);
            }
        });
    }
}
