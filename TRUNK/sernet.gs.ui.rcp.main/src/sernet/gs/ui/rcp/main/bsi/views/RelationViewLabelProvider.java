/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	private IRelationTable view;

	/**
	 * @param viewer
	 */
	public RelationViewLabelProvider(IRelationTable view) {
		this.view = view;
	}

	public String getColumnText(Object obj, int index) {
		if (obj instanceof PlaceHolder) {
			if (index != 1)
				return ""; //$NON-NLS-1$
			PlaceHolder pl = (PlaceHolder) obj;
			return pl.getTitle();
		}

		CnALink link = (CnALink) obj;
		HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getRelationId());

		switch (index) {
		case 0:
			return ""; // image only //$NON-NLS-1$
		case 1:
			// if we can't find a real name for the relation, we just display
			// "depends on" or "necessary for":
			if (CnALink.isDownwardLink(view.getInputElmt(), link))
				return (relation != null) ? relation.getName() : Messages.RelationViewLabelProvider_2;
			else
				return (relation != null) ? relation.getReversename() : Messages.RelationViewLabelProvider_3;
		case 2:
			return ""; // image only //$NON-NLS-1$
		case 3:
			return CnALink.getRelationObjectTitle(view.getInputElmt(), link);
		default:
			return ""; //$NON-NLS-1$
		}
	}

	public Image getColumnImage(Object obj, int index) {
		if (obj instanceof PlaceHolder)
			return null;

		CnALink link = (CnALink) obj;
		switch (index) {
		case 0:
			if (CnALink.isDownwardLink(view.getInputElmt(), link))
				return ImageCache.getInstance().getImage(ImageCache.LINK_DOWN);
			else
				return ImageCache.getInstance().getImage(ImageCache.LINK_UP);
		case 2:
			if (CnALink.isDownwardLink(view.getInputElmt(), link))
				return getObjTypeImage(link.getDependency());
			else
				return getObjTypeImage(link.getDependant());
		default:
			return null;
		}

	}

	/**
	 * @param link
	 * @return
	 */
	private Image getObjTypeImage(CnATreeElement elmt) {
		return ImageCache.getInstance().getObjectTypeImage(elmt.getTypeId());
	}

}
