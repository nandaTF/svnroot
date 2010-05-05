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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.gsimport.IProgress;
import sernet.gs.ui.rcp.gsimport.ImportNotesTask;
import sernet.gs.ui.rcp.gsimport.ImportTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.GSImportDialog;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;

public class ImportGstoolAction extends Action {

	public static final String ID = "sernet.gs.ui.rcp.main.importgstoolaction";
	private final IWorkbenchWindow window;

    private IModelLoadListener loadListener = new IModelLoadListener() {
        
        /* (non-Javadoc)
         * @see sernet.gs.ui.rcp.main.common.model.IModelLoadListener#closed(sernet.gs.ui.rcp.main.bsi.model.BSIModel)
         */
        public void closed(BSIModel model) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setEnabled(false);
                }
            });
        }

        /* (non-Javadoc)
         * @see sernet.gs.ui.rcp.main.common.model.IModelLoadListener#loaded(sernet.gs.ui.rcp.main.bsi.model.BSIModel)
         */
        public void loaded(final BSIModel model) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setEnabled(true);
                }
            });
        }
    };

    public ImportGstoolAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        setId(ID);
        setEnabled(false);
        CnAElementFactory.getInstance().addLoadListener(loadListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            final GSImportDialog dialog = new GSImportDialog(Display.getCurrent().getActiveShell());
			if (dialog.open() != InputDialog.OK)
                return;

			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();

					ImportTask importTask = new ImportTask(
							dialog.isBausteine(),
							dialog.isMassnahmenPersonen(),
							dialog.isZielObjekteZielobjekte(),
							dialog.isSchutzbedarf(),
							dialog.isRollen(),
							dialog.isKosten(),
							dialog.isUmsetzung(),
							dialog.isBausteinPersonen());
                    try {
                        importTask.execute(ImportTask.TYPE_SQLSERVER, new IProgress() {
                            public void done() {
                                monitor.done();
                            }

                            public void worked(int work) {
                                monitor.worked(work);
                            }

                            public void beginTask(String name, int totalWork) {
                                monitor.beginTask(name, totalWork);
                            }

                            public void subTask(String name) {
                                monitor.subTask(name);
                            }
                        });
                    } catch (Exception e) {
                        ExceptionUtil.log(e, Messages.ImportGstoolAction_1);
                    }
                }
            });
      	
			if (dialog.isNotizen()) {
			    PlatformUI.getWorkbench().getProgressService().
			    busyCursorWhile(new IRunnableWithProgress() {
			        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			            Activator.inheritVeriniceContextState();
			            
			            ImportNotesTask importTask = new ImportNotesTask();
			            try {
			                importTask.execute(ImportTask.TYPE_SQLSERVER, new IProgress() {
			                    public void done() {
			                        monitor.done();
			                    }
			                    public void worked(int work) {
			                        monitor.worked(work);
			                    }
			                    public void beginTask(String name, int totalWork) {
			                        monitor.beginTask(name, totalWork);
			                    }
			                    public void subTask(String name) {
			                        monitor.subTask(name);
			                    }
			                });
			            } catch (Exception e) {
           					ExceptionUtil.log(e.getCause(), Messages.ImportGstoolAction_2);
        				}
			        }
			    });
			}
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e.getCause(), "Import aus dem Gstool fehlgeschlagen.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Import aus dem Gstool fehlgeschlagen.");
        }
    }

}
