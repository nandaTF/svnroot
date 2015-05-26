/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * Copyright (c) 2014 Daniel Murygin <dm@sernet.de>
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
 *     Andreas Becker <andreas.r.becker[at]rub[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster[a]tarent[dot]de> - removal of JDom API use
 *     Daniel Murygin <dm@sernet.de> - Reintroduction of the disabled import delete feature
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;

/**
 * Search for objects within database, which have previously been synced
 * from the given sourceId, but not listed any more. Delete those objects
 * from the database!
 * 
 * Be VERY CAREFUL with this command, since it DELETES STUFF!! This should
 * only be used, if the delete-flag has been explicitly set (default:
 * false) by the user within the sync process...
 * 
 * @author Andreas Becker <andreas.r.becker@rub.de>
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("serial")
public class SyncDeleteCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(SyncDeleteCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SyncDeleteCommand.class);
        }
        return log;
    }
    
    private String sourceId;
    private SyncData syncData;

    private List<String> errors;

    private int deleted = 0;

    public int getDeleted() {
        return deleted;
    }

    public SyncDeleteCommand(String sourceId, SyncData syncData, List<String> errorList) {
        this.sourceId = sourceId;
        this.syncData = syncData;
        this.errors = errorList;
    }

    /* Search for objects within database, which have previously been synced
     * from the given sourceId, but not listed any more. Delete those objects
     * from the database.
     */
    @Override
    public void execute() {
        LoadCnAElementsBySourceID command = new LoadCnAElementsBySourceID(sourceId);

        try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            getLog().error("Error while loading elements by source-id: " + sourceId, e);
            errors.add("Fehler beim Ausführen von LoadCnAElementsBySourceID mit der sourceId = " + sourceId);
            return;
        }

        List<CnATreeElement> dbElements = command.getElements();

        // create a hash map, which contains a token for all
        // extId's which are present in the sync Data:
        HashSet<String> currentExtIds = new HashSet<String>();

        collectExtIds(syncData.getSyncObject(), currentExtIds);

        // find objects in the db, which have been synched from
        // this sourceId in the past, but are missing in the current list:
        for (CnATreeElement e : dbElements) {
            if (!currentExtIds.contains(e.getExtId())) {
                deleteElement(e);
            }
        }
    }

    private void deleteElement(CnATreeElement e) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Element with source-id: " + sourceId + " not found in VNA / XML, will be deleted now, type: " + e.getTypeId() + ", uuid: " + e.getUuid() + ", ext-id: " + e.getExtId() + "...");
        }
        // delete this object from the database:
        RemoveElement<?> cmdRemove = new RemoveElement<CnATreeElement>(e);
        try {
            cmdRemove = getCommandService().executeCommand(cmdRemove);
            deleted++;
        } catch (CommandException ex) {
            getLog().error("Error while deleting element, uuid: " + e.getUuid(), ex);
            errors.add("Konnte Objekt ( id=" + e.getId() + ", externalId=" + e.getExtId() + ") nicht löschen.");
        }
    }

    private void collectExtIds(List<SyncObject> syncObjectList, HashSet<String> currentExtIds) {
        for (SyncObject so : syncObjectList) {
            // store a token for the extId of every <syncObject> in the sync
            // data:
            currentExtIds.add(so.getExtId());
            collectExtIds(so.getChildren(), currentExtIds);
        }
    }
}
