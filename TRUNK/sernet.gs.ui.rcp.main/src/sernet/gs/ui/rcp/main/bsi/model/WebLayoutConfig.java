/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

/**
 * Layout configuration for {@link BSIMassnahmenModel} in a web environment
 * 
 * @author Daniel <dm@sernet.de>
 */
public class WebLayoutConfig implements ILayoutConfig {

	public static final String CSS_FILE_PATH = "../css/screen.css"; 
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.ILayoutConfig#getCssFilePath()
	 */
	public String getCssFilePath() {
		return CSS_FILE_PATH;
	}

}
