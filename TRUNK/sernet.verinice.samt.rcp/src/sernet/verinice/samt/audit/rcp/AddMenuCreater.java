/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.audit.rcp;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.rcp.action.AddElement;
import sernet.verinice.iso27k.rcp.action.AddGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Creates a pulldown menu in a view toolbar to switch the {@link CnATreeElement}
 * type of the view
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class AddMenuCreater implements IViewActionDelegate, IMenuCreator {
    
    private IAction action;
    private Menu menu;
    private GenericElementView groupView;
    private IAction addElementAction;
    private IAction addGroupAction;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
        if(view instanceof ElementView) {
            this.groupView = (GenericElementView) view;
            final String typeId = groupView.getCommandFactory().getElementTypeId();
            final String groupId = groupView.getCommandFactory().getGroupTypeId();
            // this is not a typo: "groupId"
            String title = AddElement.TITLE_FOR_TYPE.get(groupId);
            addElementAction = new AddAction(typeId, title, groupView);
            addElementAction.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(typeId)));
            title = AddGroup.TITLE_FOR_TYPE.get(groupId);
            addGroupAction = new AddAction(groupId, title, groupView);
            addGroupAction.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(groupId)));         
        }       
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        addElementAction.run();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
      if (action != this.action)
      {
        action.setMenuCreator(this);
        this.action = action;
      }
    }

    // IMenuCreator methods
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose()
    {
      if (menu != null)
      {
        menu.dispose();
      }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent)
    {
      Menu menu = new Menu(parent);
      addActionToMenu(menu, addElementAction);
      addActionToMenu(menu, addGroupAction);
      return menu;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent)
    {
      // Not used
      return null;
    }


    private void addActionToMenu(Menu menu, IAction action)
    {
      ActionContributionItem item= new ActionContributionItem(action);
      item.fill(menu, -1);
    }

  }

