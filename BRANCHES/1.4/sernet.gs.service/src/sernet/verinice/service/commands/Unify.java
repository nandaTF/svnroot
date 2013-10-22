/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Unifies a set of elements defined in a list of mappings.
 * Each mapping contains a source and a destination UUID.
 * This command copies all properties from the source to the destination.
 * 
 * Property types from propertyTypeBlacklist are ignored. 
 * Empty properties from the source will not delete existing value in the destination.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Unify extends ChangeLoggingCommand implements IChangeLoggingCommand {
    
    private transient Logger log = Logger.getLogger(Unify.class);
    
    public static final List<String> PROPERTY_TYPE_BLACKLIST = Arrays.asList(SamtTopic.PROP_DESC,SamtTopic.PROP_NAME,Control.PROP_NAME,Control.PROP_DESC);
    
    private String stationId;
    
    private List<UnifyMapping> mappings;
    
    private List<CnATreeElement> changedElementList;
    
    private List<String> propertyTypeBlacklist;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    private boolean copyLinksEnabled = false;
    private boolean deleteSourceLinksEnabled = false;
    private boolean copyObjectAttributesEnabled = true;
    
    /**
     * @param mappings
     */
    public Unify(List<UnifyMapping> mappings) {
        super();
        this.mappings = mappings;
        this.stationId = ChangeLogEntry.STATION_ID;
        setPropertyTypeBlacklist(PROPERTY_TYPE_BLACKLIST);
    }
    
    /**
     * use this constructor if checkboxes should be considered also
     * first boolean sets copy links of cnatreeelements also
     * second sets deletion of existant links after copy is done (representing a cut operation)
     * @param mappings
     * @param copyLinks
     * @param deleteExistantLinks
     */
    public Unify(List<UnifyMapping> mappings, boolean copyLinks, boolean deleteExistantLinks, boolean copyAttributes){
        this(mappings);
        this.copyLinksEnabled = copyLinks;
        this.deleteSourceLinksEnabled = deleteExistantLinks;
        this.copyObjectAttributesEnabled = copyAttributes;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(mappings!=null) {
            changedElementList = new ArrayList<CnATreeElement>(mappings.size());
            for (UnifyMapping mapping : mappings) {
                UnifyElement source = mapping.getSourceElement();
                UnifyElement destination = mapping.getDestinationElement();
                try{
                    unify(source,destination);
                } catch (CommandException e){
                    getLog().error("Error unifying elements",e);
                }
            }
        }
    }
    
    /**
     * @param source
     * @param destination
     */
    private void unify(UnifyElement source, UnifyElement destination) throws CommandException {
        if(source==null || destination==null) {
            return;
        }
        CnATreeElement sourceElement = getDao().findByUuid(source.getUuid(), RetrieveInfo.getPropertyInstance());
        CnATreeElement destinationElement = getDao().findByUuid(destination.getUuid(), RetrieveInfo.getPropertyInstance());
        if(!copyObjectAttributesEnabled){
            destinationElement.getEntity().copyEntity(sourceElement.getEntity(),propertyTypeBlacklist);
        }
        if(copyLinksEnabled){
            destinationElement = unifyLinks(sourceElement, destinationElement);
        }
        if(deleteSourceLinksEnabled){
            sourceElement = deleteExistantLinks(sourceElement);
            getDao().saveOrUpdate(sourceElement);
            changedElementList.add(sourceElement);
        }
        getDao().saveOrUpdate(destinationElement);
        changedElementList.add(destinationElement);
    }

    /**
     * @param propertyTypeBlacklist the propertyTypeBlacklist to set
     */
    protected void setPropertyTypeBlacklist(List<String> propertyTypeBlacklist) {
        this.propertyTypeBlacklist = propertyTypeBlacklist;
    }

    protected IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {      
        return changedElementList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(Unify.class);
        }
        return log;
    }
    
    private CnATreeElement unifyLinks(CnATreeElement sourceElement, CnATreeElement destinationElement) throws CommandException{
        // downLink links dependant -> dependency
        // upLink links d
        for(CnALink linkDown : sourceElement.getLinksDown()){
            createLink(destinationElement, linkDown.getDependency(), linkDown.getRelationId());
        }
        for(CnALink linkUp : sourceElement.getLinksUp()){
            createLink(linkUp.getDependant(), destinationElement, linkUp.getRelationId());
        }
        return destinationElement;
    }
    
    private CnATreeElement deleteExistantLinks(CnATreeElement elmt) throws CommandException{
        CnALink[] downLinks = elmt.getLinksDown().toArray(new CnALink[elmt.getLinksDown().size()]);
        for(int i = 0; i < downLinks.length; i++){
            removeLink(downLinks[i]);
        }
        CnALink[] upLinks = elmt.getLinksUp().toArray(new CnALink[elmt.getLinksUp().size()]); 
        for(int i = 0; i < upLinks.length; i++){
            removeLink(upLinks[i]);
        }
        return elmt;
    }
    
    private void removeLink(CnALink link)throws sernet.verinice.interfaces.CommandException{
        RemoveLink<CnALink> command = new RemoveLink<CnALink>(link);
        getCommandService().executeCommand(command);
    }
    
    public CnALink createLink(CnATreeElement source, CnATreeElement destination, String relationId) throws CommandException {
        CreateLink command = new CreateLink(source, destination, relationId);
        command = getCommandService().executeCommand(command);
        return command.getLink();
    }
    

}
