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

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.commands.LoadLinkedElements;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * View with tree viewer to show {@link CnATreeElement}s of specific types
 * and of {@link Group}s which contains these types.
 * 
 * Elements are loaded by commands created by a {@link ICommandFactory} implementation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class GenericElementView extends ElementView {

    private static final Logger LOG = Logger.getLogger(GenericElementView.class);
    
    static Map<String, ICommandFactory> commandMap;

    static {
        commandMap = new Hashtable<String, ICommandFactory>();
        commandMap.put(Asset.TYPE_ID, new ElementViewCommandFactory(Asset.TYPE_ID,AssetGroup.TYPE_ID));
        commandMap.put(Audit.TYPE_ID, new ElementViewCommandFactory(Audit.TYPE_ID,AuditGroup.TYPE_ID));
        commandMap.put(Finding.TYPE_ID, new ElementViewCommandFactory(Finding.TYPE_ID,FindingGroup.TYPE_ID));
        commandMap.put(Evidence.TYPE_ID, new ElementViewCommandFactory(Evidence.TYPE_ID,EvidenceGroup.TYPE_ID));
        commandMap.put(Control.TYPE_ID, new ElementViewCommandFactory(Control.TYPE_ID,ControlGroup.TYPE_ID));
        commandMap.put(Organization.TYPE_ID, new OrganizationCommandFactory());
    }
    
    private ICommandFactory commandFactory;
    
    public GenericElementView(ICommandFactory commandFactory) {
        super();
        this.commandFactory = commandFactory;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getElementList()
     */
    @Override
    protected List<? extends CnATreeElement> getElementList() throws CommandException {
        List<AssetGroup> elementList = Collections.emptyList();
        checkSelectedGroup(elementList);
        return elementList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getLinkedElements(int)
     */
    @Override
    protected List<? extends CnATreeElement> getLinkedElements(int selectedId) throws CommandException {
        List<CnATreeElement> elementList = Collections.emptyList();
        if(commandFactory!=null) {
            LoadLinkedElements command = commandFactory.getLinkedElementCommand(selectedId);
            command = getCommandService().executeCommand(command);
            elementList = command.getElementList();
        }
        checkSelectedGroup(elementList);
        return elementList;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#initView(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void initView(Composite parent) {
        super.initView(parent);
        Filter filter = new Filter(commandFactory.getElementTypeId());
        viewer.addFilter(filter);
        contentProvider.addFilter(filter);
    }

    protected ICommandFactory getCommandFactory() {
        return commandFactory;
    }

    protected void setCommandFactory(ICommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * @param objectTypeId
     */
    public void switchElement(String objectTypeId) {
        if (commandMap.get(objectTypeId) != null) {
            setCommandFactory(commandMap.get(objectTypeId));
            setIcon(ImageCache.getInstance().getISO27kTypeImage(objectTypeId));
            reload();
            String title = HitroUtil.getInstance().getTypeFactory().getMessage(objectTypeId);
            setViewTitle(title);
        }   
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#checkRelations(sernet.verinice.model.common.CnATreeElement)
     */
    protected boolean checkRelations(CnATreeElement treeElement) {
        boolean result = false; 
        EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(treeElement.getEntity().getEntityType());
        Set<HuiRelation> relationSet = entityType.getPossibleRelations();
        for (HuiRelation huiRelation : relationSet) {
            if(commandFactory.getElementTypeId().equals(huiRelation.getTo())) {
                result = true;
                break;
            }
        }
        return result;
    }

}

class Filter extends ViewerFilter {

    String typeId;
    
    /**
     * @param typeId
     */
    public Filter(String typeId) {
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean result = true;
        if(element instanceof CnATreeElement) {
            CnATreeElement treeElement = (CnATreeElement) element;
            if(!treeElement.getTypeId().equals(typeId) && treeElement instanceof IISO27kGroup) {
                IISO27kGroup group = (IISO27kGroup) element;
                result = Arrays.binarySearch(group.getChildTypes(), this.typeId)>-1;
            }
        }
        return result;
    }
    
}
