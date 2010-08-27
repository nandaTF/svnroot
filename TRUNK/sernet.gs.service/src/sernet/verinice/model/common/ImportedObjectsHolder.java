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
package sernet.verinice.model.common;

import sernet.verinice.model.bsi.IBSIStrukturKategorie;

@SuppressWarnings("serial")
public class ImportedObjectsHolder extends CnATreeElement implements IBSIStrukturKategorie {
	
	public static final String TYPE_ID = ImportedObjectsHolder.class.getSimpleName();

	public ImportedObjectsHolder(CnATreeElement model) {
		super(model);
	}

	protected ImportedObjectsHolder() {
	}

	@Override
	public String getTitle() {
		return "importierte Objekte";
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public boolean canContain(Object obj) {
		return true;
	}
	
}
