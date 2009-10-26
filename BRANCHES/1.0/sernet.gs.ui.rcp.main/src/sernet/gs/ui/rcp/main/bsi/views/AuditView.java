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
 *     Robert Schuster <r.schuster@tarent.de> - reworked to use common base class
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.views.actions.AuditViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;

/**
 * Shows implemented controls to be reviewed by the auditor.
 * 
 * 
 * @author koderman@sernet.de
 *
 */
public class AuditView extends GenericMassnahmenView {
	public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.auditview"; //$NON-NLS-1$

	private static class AuditLabelProvider extends LabelProvider implements ITableLabelProvider {

		private SimpleDateFormat dateFormat =  new SimpleDateFormat("dd.MM.yy, EE"); //$NON-NLS-1$
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof PlaceHolder) {
				return null;
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			if (columnIndex == 0) {
				return CnAImageProvider.getImage(mn);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {

			if (element instanceof PlaceHolder) {
				if (columnIndex == 1) {
					PlaceHolder ph = (PlaceHolder) element;
					return ph.getTitle();
				}
				return ""; //$NON-NLS-1$
			}
			
			TodoViewItem mn = (TodoViewItem) element;
			switch(columnIndex) {
			case 0: // icon
				return ""; //$NON-NLS-1$
			case 1: // date
				Date date = mn.getNaechsteRevision();
				if (date == null)
					return Messages.TodoView_3;
				return dateFormat.format(date);
			case 2: // bearbeiter
				return mn.getRevisionDurch();
			case 3: // siegelstufe
				return "" + mn.getStufe(); //$NON-NLS-1$
			case 4: // zielobjekt
				return mn.getParentTitle();
			case 5: // title
				return mn.getTitel();
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	private static class AuditSorter extends ViewerSorter {
		public boolean isSorterProperty(Object arg0, String arg1) {
			return arg1.equals("_date"); //$NON-NLS-1$
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			TodoViewItem mn1 = (TodoViewItem) o1;
			TodoViewItem mn2 = (TodoViewItem) o2;
			return sortByDate(mn1.getNaechsteRevision(), mn2.getNaechsteRevision());
		}

		private int sortByDate(Date date1, Date date2) {
	        if (date1 == null)
	            return 1;
	        
	        if (date2 == null)
	            return -1;
	        
			int comp = date1.compareTo(date2);
			// reverse order:
			//comp = (comp < 0) ? 1 : (comp > 0) ? -1 : 0;
			return comp;
	        
		}
		

	}
	
	@Override
	protected void createPartControlImpl(Composite parent) {
		Table table = viewer.getTable();
		
		iconColumn = new TableColumn(table, SWT.LEFT);
		iconColumn.setText(" "); //$NON-NLS-1$
		iconColumn.setWidth(25);
		
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText(Messages.AuditView_8);
		dateColumn.setWidth(200);
		
		bearbeiterColumn = new TableColumn(table, SWT.LEFT);
		bearbeiterColumn.setText(Messages.AuditView_9);
		bearbeiterColumn.setWidth(100);
		
		siegelColumn = new TableColumn(table, SWT.LEFT);
		siegelColumn.setText(Messages.AuditView_10);
		siegelColumn.setWidth(20);
		
		zielColumn = new TableColumn(table, SWT.LEFT);
		zielColumn.setText(Messages.AuditView_11);
		zielColumn.setWidth(150);
		
		titleColumn = new TableColumn(table, SWT.LEFT);
		titleColumn.setText(Messages.AuditView_12);
		titleColumn.setWidth(250);
		
		viewer.setColumnProperties(new String[] {
				"_icon", //$NON-NLS-1$
				"_date", //$NON-NLS-1$
				"_bearbeiter", //$NON-NLS-1$
				"_siegel", //$NON-NLS-1$
				"_ziel", //$NON-NLS-1$
				"_title" //$NON-NLS-1$
		});
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	protected String[] getUmsetzungPattern() {
		return new String[] {
				MassnahmenUmsetzung.P_UMSETZUNG_JA,
				MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH
		};
	}

	@Override
	protected Action createFilterAction(
			MassnahmenUmsetzungFilter umsetzungFilter,
			MassnahmenSiegelFilter siegelFilter) {
		return new AuditViewFilterAction(viewer, Messages.AuditView_19,
				umsetzungFilter, siegelFilter);
	}

	@Override
	protected ILabelProvider createLabelProvider() {
		return new AuditLabelProvider();
	}

	@Override
	protected ViewerSorter createSorter() {
		return new AuditSorter();
	}

	@Override
	protected String getMeasureLoadJobLabel() {
		return Messages.AuditView_4;
	}

	@Override
	protected String getMeasureLoadPlaceholderLabel() {
		return Messages.AuditView_2;
	}

	@Override
	protected String getTaskErrorLabel() {
		return Messages.AuditView_5;
	}

	@Override
	protected String getMeasureLoadTaskLabel() {
		return "Lade Massnahmen";
	}

}
