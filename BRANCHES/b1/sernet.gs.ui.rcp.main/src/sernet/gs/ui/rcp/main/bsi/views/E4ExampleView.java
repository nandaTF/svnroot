/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.TableViewer;
//import javax.inject.Inject;


/**
 *
 */
public class E4ExampleView {

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.E4ExampleView";
    
    private TableViewer viewer;
    
//    @Inject
//    public E4ExampleView(Composite parent) {
//        // TODO Auto-generated constructor stub
//        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
//                | SWT.V_SCROLL);
//        viewer.setContentProvider(ArrayContentProvider.getInstance());
//        TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
//        column.getColumn().setWidth(100);
//        column.setLabelProvider(new ColumnLabelProvider(){
//            @Override
//            public String getText(Object element) {
//                return element.toString();
//            }
//        });
//         
//        // provide the input to the ContentProvider
//        viewer.setInput(new String[] { "One", "Two", "Three" });
//    }
//    
//    @PostConstruct
//    public void createPartControl(Composite parent) {
//
//    }
 
    @Focus
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
}
