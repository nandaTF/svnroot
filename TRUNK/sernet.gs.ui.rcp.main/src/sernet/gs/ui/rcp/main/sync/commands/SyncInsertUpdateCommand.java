/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>
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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.sync.commands;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByExternalID;
import sernet.gs.ui.rcp.main.sync.InvalidRequestException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.iso27k.service.commands.LoadImportObjectsHolder;
import sernet.verinice.iso27k.service.commands.LoadModel;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.model.samt.SamtTopic;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncLink;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.data.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;

@SuppressWarnings("serial")
public class SyncInsertUpdateCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(SyncInsertUpdateCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SyncInsertUpdateCommand.class);
        }
        return log;
    }
    
    private static HashMap<String, String> containerTypes = new HashMap<String, String>();
    private static HashMap<String, Class<? extends CnATreeElement>> typeIdClass = new HashMap<String, Class<? extends CnATreeElement>>();

    static {
        typeIdClass.put(Anwendung.TYPE_ID, Anwendung.class);
        typeIdClass.put(Gebaeude.TYPE_ID, Gebaeude.class);
        typeIdClass.put(Client.TYPE_ID, Client.class);
        typeIdClass.put(Server.TYPE_ID, Server.class);
        typeIdClass.put(SonstIT.TYPE_ID, SonstIT.class);
        typeIdClass.put(TelefonKomponente.TYPE_ID, TelefonKomponente.class);
        typeIdClass.put(Person.TYPE_ID, Person.class);
        typeIdClass.put(NetzKomponente.TYPE_ID, NetzKomponente.class);
        typeIdClass.put(Raum.TYPE_ID, Raum.class);
        typeIdClass.put(AnwendungenKategorie.TYPE_ID, AnwendungenKategorie.class);
        typeIdClass.put(GebaeudeKategorie.TYPE_ID, GebaeudeKategorie.class);
        typeIdClass.put(ClientsKategorie.TYPE_ID, ClientsKategorie.class);
        typeIdClass.put(ServerKategorie.TYPE_ID, ServerKategorie.class);
        typeIdClass.put(SonstigeITKategorie.TYPE_ID, SonstigeITKategorie.class);
        typeIdClass.put(TKKategorie.TYPE_ID, TKKategorie.class);
        typeIdClass.put(PersonenKategorie.TYPE_ID, PersonenKategorie.class);
        typeIdClass.put(NKKategorie.TYPE_ID, NKKategorie.class);
        typeIdClass.put(RaeumeKategorie.TYPE_ID, RaeumeKategorie.class);
        typeIdClass.put(BausteinUmsetzung.TYPE_ID, BausteinUmsetzung.class);
        typeIdClass.put(ITVerbund.TYPE_ID, ITVerbund.class);
        typeIdClass.put(MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.class);
        typeIdClass.put(Verarbeitungsangaben.TYPE_ID, Verarbeitungsangaben.class);
        typeIdClass.put(Personengruppen.TYPE_ID, Personengruppen.class);
        typeIdClass.put(VerantwortlicheStelle.TYPE_ID, VerantwortlicheStelle.class);
        typeIdClass.put(StellungnahmeDSB.TYPE_ID, StellungnahmeDSB.class);
        typeIdClass.put(Datenverarbeitung.TYPE_ID, Datenverarbeitung.class);
        
        typeIdClass.put(FinishedRiskAnalysis.TYPE_ID, FinishedRiskAnalysis.class);
        typeIdClass.put(GefaehrdungsUmsetzung.TYPE_ID, GefaehrdungsUmsetzung.class);
        typeIdClass.put(RisikoMassnahmenUmsetzung.TYPE_ID, RisikoMassnahmenUmsetzung.class);

        typeIdClass.put(ResponseGroup.TYPE_ID, ResponseGroup.class);
        typeIdClass.put(ExceptionGroup.TYPE_ID, ExceptionGroup.class);
        typeIdClass.put(VulnerabilityGroup.TYPE_ID, VulnerabilityGroup.class);
        typeIdClass.put(PersonGroup.TYPE_ID, PersonGroup.class);
        typeIdClass.put(IncidentGroup.TYPE_ID, IncidentGroup.class);
        typeIdClass.put(ThreatGroup.TYPE_ID, ThreatGroup.class);
        typeIdClass.put(Organization.TYPE_ID, Organization.class);
        typeIdClass.put(ProcessGroup.TYPE_ID, ProcessGroup.class);
        typeIdClass.put(AuditGroup.TYPE_ID, AuditGroup.class);
        typeIdClass.put(IncidentScenarioGroup.TYPE_ID, IncidentScenarioGroup.class);
        typeIdClass.put(RecordGroup.TYPE_ID, RecordGroup.class);
        typeIdClass.put(RequirementGroup.TYPE_ID, RequirementGroup.class);
        typeIdClass.put(ControlGroup.TYPE_ID, ControlGroup.class);
        typeIdClass.put(DocumentGroup.TYPE_ID, DocumentGroup.class);
        typeIdClass.put(AssetGroup.TYPE_ID, AssetGroup.class);
        typeIdClass.put(EvidenceGroup.TYPE_ID, EvidenceGroup.class);
        typeIdClass.put(InterviewGroup.TYPE_ID, InterviewGroup.class);
        typeIdClass.put(FindingGroup.TYPE_ID, FindingGroup.class);
        
        typeIdClass.put(Response.TYPE_ID, Response.class);
        typeIdClass.put(sernet.verinice.model.iso27k.Exception.TYPE_ID, sernet.verinice.model.iso27k.Exception.class);
        typeIdClass.put(Vulnerability.TYPE_ID, Vulnerability.class);
        typeIdClass.put(PersonIso.TYPE_ID, PersonIso.class);
        typeIdClass.put(Incident.TYPE_ID, Incident.class);
        typeIdClass.put(Threat.TYPE_ID, Threat.class);
        typeIdClass.put(sernet.verinice.model.iso27k.Process.TYPE_ID, sernet.verinice.model.iso27k.Process.class);
        typeIdClass.put(Audit.TYPE_ID, Audit.class);
        typeIdClass.put(IncidentScenario.TYPE_ID, IncidentScenario.class);
        typeIdClass.put(Record.TYPE_ID, Record.class);
        typeIdClass.put(Requirement.TYPE_ID, Requirement.class);
        typeIdClass.put(Control.TYPE_ID, Control.class);
        typeIdClass.put(Document.TYPE_ID, Document.class);
        typeIdClass.put(Asset.TYPE_ID, Asset.class);
        typeIdClass.put(Evidence.TYPE_ID, Evidence.class);
        typeIdClass.put(Interview.TYPE_ID, Interview.class);
        typeIdClass.put(Finding.TYPE_ID, Finding.class);

        typeIdClass.put(SamtTopic.TYPE_ID, SamtTopic.class);
    }

    private String sourceId;
    private SyncMapping syncMapping;
    private SyncData syncData;

    private boolean insert, update;
    private List<String> errorList;

    private int inserted = 0, updated = 0;

    private CnATreeElement container = null;

    private Set<CnATreeElement> elementSet = new HashSet<CnATreeElement>();
    
    private transient Map<String, CnATreeElement> idElementMap = new HashMap<String, CnATreeElement>();
    
    public int getUpdated() {
        return updated;
    }

    public int getInserted() {
        return inserted;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public SyncInsertUpdateCommand(String sourceId, SyncData syncData, SyncMapping syncMapping, boolean insert, boolean update, List<String> errorList) {
        this.sourceId = sourceId;
        this.syncData = syncData;
        this.syncMapping = syncMapping;
        this.insert = insert;
        this.update = update;
        this.errorList = errorList;
    }

    /**
     * Processes the given <syncData> and <syncMapping> elements in order to
     * insert and/or update objects in(to) the database, according to the flags
     * insert & update.
     * 
     * If there already exists an ITVerbund from a past sync session, this one
     * (identified by its sourceID) will be used; otherwise this creates a new
     * one within the BSIModel.
     * 
     * @throws InvalidRequestException
     * @throws CommandException
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    public void execute() {
        for (SyncObject so : syncData.getSyncObject()) {
            importObject(null, so);
        } // for <syncObject>
        for (SyncLink syncLink : syncData.getSyncLink()) {
            importLink(syncLink);
        }
    }

    private void importObject(CnATreeElement parent, SyncObject so) {
        String extId = so.getExtId();
        String extObjectType = so.getExtObjectType();
        if (getLog().isDebugEnabled()) {
            getLog().debug("Importing element type: " + extObjectType + ", extId: " + extId + "...");
        }
        
        boolean setAttributes = false;

        MapObjectType mot = getMap(extObjectType);

        if (mot == null) {
            final String message = "Could not find mapObjectType-Element for XML type: " + extObjectType;
            getLog().error(message);
            errorList.add(message);
            return;
        }

        // this element "knows", which huientitytype is applicable and
        // how the associated properties have to be mapped!
        String veriniceObjectType = mot.getIntId();

        CnATreeElement elementInDB = null;

        try {
            elementInDB = findDbElement(sourceId, extId);
        } catch (CommandException e1) {
            throw new RuntimeCommandException(e1);
        }

        if (elementInDB != null) // object found!
        {
            if (update) // this object should be updated!
            {
                /*** UPDATE: ***/
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db: updating, uuid: " + elementInDB.getUuid());
                }
                setAttributes = true;
                updated++;
            } else {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element found in db, update disabled, uuid: " + elementInDB.getUuid());
                }
                // do not update this object's attributes!
                setAttributes = false;
            }
        }
        
        // If no previous object was found in the database and the 'insert'
        // flag is given, create a new object.
        if (elementInDB == null && insert) {
            // Each new object needs a parent. The top-level element(s) in the
            // import set might not automatically have one. For those objects it is
            // neccessary to use the 'import root object' instead.
            Class clazz = getClassFromTypeId(veriniceObjectType);
            parent = (parent == null) ? accessContainer(clazz) : parent;

            try {
                // create new object in db...
                CreateElement<CnATreeElement> newElement = new CreateElement<CnATreeElement>(parent, clazz, true, false);
                newElement = getCommandService().executeCommand(newElement);
                elementInDB = newElement.getNewElement();

                // ...and set its sourceId and extId:
                elementInDB.setSourceId(sourceId);
                elementInDB.setExtId(extId);
                
                if(elementInDB instanceof Organization || elementInDB instanceof ITVerbund) {
                    addElement(elementInDB);
                }
                
                setAttributes = true;
                inserted++;
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Element inserted, uuid: " + elementInDB.getUuid());
                }
            } catch (Exception e) {
                getLog().error("Error while inserting element, type: " + extObjectType + ", extId: " + extId, e);
                errorList.add("Konnte " + veriniceObjectType + "-Objekt nicht erzeugen.");
            }
        }

        /*
         * Now if we should update an existing object or created a new object,
         * set the associated attributes:
         */
        if (null != elementInDB && setAttributes) {
            // for all <syncAttribute>-Elements below current
            // <syncObject>...
            HUITypeFactory huiTypeFactory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
            for (SyncAttribute sa : so.getSyncAttribute()) {
                String attrExtId = sa.getName();
                List<String> attrValues = sa.getValue();

                MapAttributeType mat = getMapAttribute(mot, attrExtId);

                if (mat == null) {
                    final String message = "Could not find mapObjectType-Element for XML attribute type: " + attrExtId + " of type: " + extObjectType;
                    getLog().error(message);
                    this.errorList.add(message);
                } else {
                    String attrIntId = mat.getIntId();
                    elementInDB.getEntity().importProperties(huiTypeFactory,attrIntId, attrValues);
                    addElement(elementInDB);
                }

            } // for <syncAttribute>
            IBaseDao<CnATreeElement, Serializable> dao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(elementInDB.getTypeId());
            dao.merge(elementInDB);
        } // if( null != ... )

        idElementMap.put(elementInDB.getExtId(), elementInDB);
        
        // Handle all the child objects.
        for (SyncObject child : so.getChildren()) {
            // The object that was created or modified during the course of
            // this method call is the parent for the import of the
            // child elements.
            if (getLog().isDebugEnabled() && child!=null) {
                getLog().debug("Child found, type: " + child.getExtObjectType() + ", extId: " + child.getExtId());
            }
            importObject(elementInDB, child);
        }
    }
    
    /**
     * @param syncLink
     */
    private void importLink(SyncLink syncLink) {
        String dependantId = syncLink.getDependant();
        String dependencyId = syncLink.getDependency();
        CnATreeElement dependant = idElementMap.get(dependantId);
        if(dependant==null) {          
            DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
            crit.add(Restrictions.eq("extId", dependantId));
            List<CnATreeElement> resultList = getDaoFactory().getDAO(CnATreeElement.class).findByCriteria(crit);
            if(resultList!=null && !resultList.isEmpty()) {
               if(resultList.size()>1) {
                   getLog().error("Can not import link. Found more than one dependant element, dependant ext-id: " + dependencyId + " dependency ext-id: " + dependencyId);
                   return;
               }
               dependant = resultList.get(0);
            }
            if(dependant==null) {
                getLog().error("Can not import link. dependant not found in xml file and db, dependant ext-id: " + dependantId + " dependency ext-id: " + dependencyId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependant not found in XML file but in db, ext-id: " + dependantId);
            }
        }
        CnATreeElement dependency = idElementMap.get(dependencyId);
        if(dependency==null) {    
            DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
            crit.add(Restrictions.eq("extId", dependencyId));
            List<CnATreeElement> resultList = getDaoFactory().getDAO(CnATreeElement.class).findByCriteria(crit);
            if(resultList!=null && !resultList.isEmpty()) {
               if(resultList.size()>1) {
                   getLog().error("Can not import link. Found more than one dependency element, dependency ext-id: " + dependencyId + " dependant ext-id: " + dependantId);
                   return;
               }
               dependency = resultList.get(0);
            }
            if(dependency==null) {
                getLog().error("Can not impor tlink. dependency not found in xml file and db, dependency ext-id: " + dependencyId + " dependant ext-id: " + dependantId);
                return;
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("dependency not found in XML file but in db, ext-id: " + dependencyId);
            }
        }
        CnALink link = new CnALink(dependant,dependency,syncLink.getRelationId(),syncLink.getComment());
        dependant.addLinkDown(link);
        dependency.addLinkUp(link);
        getDaoFactory().getDAO(CnALink.class).saveOrUpdate(link);
        
    }

    private MapObjectType getMap(String extObjectType) {
        for (MapObjectType mot : syncMapping.getMapObjectType()) {
            if (extObjectType.equals(mot.getExtId())) {
                return mot;
            }
        }

        return null;
    }

    private SyncMapping.MapObjectType.MapAttributeType getMapAttribute(MapObjectType mot, String extObjectType) {
        for (SyncMapping.MapObjectType.MapAttributeType mat : mot.getMapAttributeType()) {
            if (extObjectType.equals(mat.getExtId())) {
                return mat;
            }
        }

        return null;
    }

    /************************************************************
     * findDbElement()
     * 
     * Query element (by externalID) from DB, which has been previously
     * synchronized from the given sourceID.
     * 
     * @param sourceID
     * @param externalID
     * @return the CnATreeElement from the query or null, if nothing was found
     * @throws CommandException
     ************************************************************/
    private CnATreeElement findDbElement(String sourceID, String externalID) throws CommandException {
        // use a new crudCommand (load by external, source id):
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceID, externalID);
        command = getCommandService().executeCommand(command);

        List<CnATreeElement> foundElements = command.getElements();

        if (foundElements == null || foundElements.size() == 0) {
            return null;
        } else {
            return foundElements.get(0);
        }
    }

    /************************************************************
     * findContainerFor()
     * 
     * Find appropriate Category within the tree for a given object type.
     * 
     * @param verbund
     * @param veriniceObjectType
     * @return the Category - CnATreeElement
     ************************************************************/
    private CnATreeElement findContainerFor(CnATreeElement root, String veriniceObjectType) {

        // If in doubt the root for imported objects should always be used.
        return root;
    }

    /************************************************************
     * getClassFromTypeId()
     * 
     * @param typeId
     * @return the corresponding Class
     ************************************************************/
    @SuppressWarnings("unchecked")
    private Class<CnATreeElement> getClassFromTypeId(String typeId) {
        Class<CnATreeElement> klass = (Class<CnATreeElement>) typeIdClass.get(typeId);
        if (klass == null) {
            throw new IllegalStateException(String.format("Type ID '%s' was not available in type map.", typeId));
        }

        return klass;
    }

    /**
     * If during the import action an object has to be created for which no
     * parent is available (or can be found) the artificial 'rootImportObject'
     * should be used.
     * 
     * <p>
     * This method should <em>onl</em> be called when the 'rootImportObject' is
     * definitely needed and going to be used because the root object is not
     * only created but also automatically persisted in the database. If it were
     * not used later on the user would see an object node in the object tree.
     * </p>
     * @param clazz 
     * 
     * @return
     */
    private CnATreeElement accessContainer(Class clazz) {
        // Create the importRootObject if it does not exist yet
        // and set the 'importRootObject' variable.
        if (container == null) {
            LoadImportObjectsHolder cmdLoadContainer = new LoadImportObjectsHolder(clazz);
            try {
                cmdLoadContainer = ServiceFactory.lookupCommandService().executeCommand(cmdLoadContainer);
            } catch (CommandException e) {
                errorList.add("Fehler beim Ausführen von LoadBSIModel.");
                throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.");
            }
            container = cmdLoadContainer.getHolder();
            if(container==null) {
                container = createContainer(clazz);
            }
            // load the parent
            container.getParent().getTitle();
        }

        return container;
    }

    private CnATreeElement createContainer(Class clazz) {
        if(LoadImportObjectsHolder.isImplementation(clazz, IBSIStrukturElement.class)) {
            return createBsiContainer();
        } else {
            return createIsoContainer();
        }
       
    }
    
    private CnATreeElement createBsiContainer() {
        LoadBSIModel cmdLoadModel = new LoadBSIModel();
        try {
            cmdLoadModel = ServiceFactory.lookupCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            errorList.add("Fehler beim Ausführen von LoadBSIModel.");
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        BSIModel model = cmdLoadModel.getModel();
        try {
            ImportBsiGroup holder = new ImportBsiGroup(model);
            getDaoFactory().getDAO(ImportBsiGroup.class).saveOrUpdate(holder);
            container = holder;
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        return container;
    }
    
    private CnATreeElement createIsoContainer() {
        LoadModel cmdLoadModel = new LoadModel();
        try {
            cmdLoadModel = ServiceFactory.lookupCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            errorList.add("Fehler beim Ausführen von LoadBSIModel.");
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        ISO27KModel model = cmdLoadModel.getModel();
        try {
            ImportIsoGroup holder = new ImportIsoGroup(model);
            getDaoFactory().getDAO(ImportIsoGroup.class).saveOrUpdate(holder);
            container = holder;
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.");
        }
        return container;
    }

    /**
     * Returns the 'import root object'. May be null if it was not created
     * during the import.
     * 
     * @return
     */
    public CnATreeElement getContainer() {
        return container;
    }
    
    protected void addElement(CnATreeElement element) {
        if(elementSet==null) {
            elementSet = new HashSet<CnATreeElement>();
        }
        // load the parent
        element.getParent().getTitle();
        elementSet.add(element);
    }

    public Set<CnATreeElement> getElementSet() {
        return elementSet;
    }

}
