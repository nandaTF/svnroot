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
package sernet.gs.ui.rcp.office;

import java.io.Serializable;

public interface IOOTableRow extends Serializable {
	public static final int CELL_TYPE_STRING = 0;
	public static final int CELL_TYPE_DOUBLE = 1;
	
	// row styles, names must match style definition in OpenOffice template:
	public static final String ROW_STYLE_ELEMENT = "Element";
	public static final String ROW_STYLE_SUBHEADER = "ElementHeader";
	public static final String ROW_STYLE_HEADER = "Kategorie";
	
	public String getCellAsString(int column);
	public double getCellAsDouble(int column);
	public int getCellType(int column);
	public int getNumColumns();
	public String getRowStyle();
}
