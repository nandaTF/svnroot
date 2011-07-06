/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveNote;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadBSIModel;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ImportNotesForZielobjekt extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(ImportNotesForZielobjekt.class);

	private String zielobjektName;
    private Map<MbBaust, List<NotizenMassnahmeResult>> notizenMap;

    /**
     * @param name
     * @param notizen
     */
        public ImportNotesForZielobjekt(String name, Map<MbBaust, List<NotizenMassnahmeResult>> notizenMap) {
        this.zielobjektName = name;
        this.notizenMap = notizenMap;
    }


    public void execute() {
	    IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
	    LoadBSIModel command = new LoadBSIModel();
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    BSIModel model = command.getModel();
	    List<CnATreeElement> allElements = model.getAllElementsFlatList(false);
	  
	    for (CnATreeElement cnATreeElement : allElements) {
            if (cnATreeElement.getTitle().equals(zielobjektName)) {
                
                Set<Entry<MbBaust, List<NotizenMassnahmeResult>>> entrySet = notizenMap.entrySet();
                for (Entry<MbBaust, List<NotizenMassnahmeResult>> entry : entrySet) {
                    MbBaust baust = entry.getKey();
                    List<NotizenMassnahmeResult> massnahmenNotizen = entry.getValue();
                    BausteinUmsetzung bstUms = findBausteinUmsetzung(cnATreeElement, baust);
                    if (bstUms != null) {
                        try {
                            List<NotizenMassnahmeResult> remainingNotes = addNotes(bstUms, massnahmenNotizen);
                            if (remainingNotes != null && remainingNotes.size()>0) {
                                addNotes(cnATreeElement, remainingNotes);
                            }
                        } catch (CommandException e) {
                            throw new RuntimeCommandException(e);
                        }
                    }
                }
            }
        }
	}


    /**
     * Add remaining notes to target object.
     * 
     * @param cnATreeElement
     * @param remainingNotes
     * @throws CommandException 
     */
    private void addNotes(CnATreeElement cnATreeElement, List<NotizenMassnahmeResult> remainingNotes) throws CommandException {
        for (NotizenMassnahmeResult notiz : remainingNotes) {
            LOG.debug("Adding note for " + cnATreeElement.getTitle());
            saveNewNote(cnATreeElement.getDbId(), cnATreeElement.getTitle(), cnATreeElement.getTitle(), notiz.notiz.getNotizText());
        }
    }


    /**
     * Add notes to massnahmen of this bausteinumsetznug.
     * @param bstUms
     * @param massnahmenNotizen
     * @return list of all notes that could not be applied
     * @throws CommandException 
     */
    private List<NotizenMassnahmeResult> addNotes(BausteinUmsetzung bstUms, List<NotizenMassnahmeResult> massnahmenNotizen) throws CommandException {
        List<NotizenMassnahmeResult> copy = new ArrayList<NotizenMassnahmeResult>();
        copy.addAll(massnahmenNotizen);
        
        List<MassnahmenUmsetzung> ums = bstUms.getMassnahmenUmsetzungen();
        for (MassnahmenUmsetzung mnums : ums) {
            NotizenMassnahmeResult notizVorlage = TransferData.findMassnahmenVorlageNotiz(mnums, massnahmenNotizen);
            if (notizVorlage != null) {
                copy.remove(notizVorlage);
                
                LOG.debug("Adding note for " + bstUms.getTitle() + ", " + mnums.getKapitel());
                Integer dbId = mnums.getDbId();
                String elmtTitle = mnums.getTitle();
                String noteTitle = "Notiz " + mnums.getKapitel();
                String text = notizVorlage.notiz.getNotizText();
                
                saveNewNote(dbId, elmtTitle, noteTitle, text);
            }
        }
        if (copy.size() > 0) {
            LOG.debug("Notes without target object: ");
            for (NotizenMassnahmeResult note : copy) {
                Logger.getLogger(this.getClass()).debug(note.notiz.getNotizText());
            }
        }
        return copy;
    }


    /**
     * @param dbId
     * @param elmtTitle
     * @param noteTitle
     * @param text
     * @throws CommandException
     */
    private void saveNewNote(Integer dbId, String elmtTitle, String noteTitle, String text) throws CommandException {
        String convertedText;
        try {
            convertedText = TransferData.convertRtf(text);
        } catch (Exception e) {
            convertedText = "!Konvertierungsfehler, Originaltext: " + text;
            LOG.debug(e);
        }
        
        // do not save empty notes:
        Pattern pattern = Pattern.compile("^\\s+$");
        Matcher matcher = pattern.matcher(convertedText);
        if (matcher.matches())
            return;
        
        Note note = new Note();
        note.setCnATreeElementId(dbId);
        note.setCnAElementTitel(elmtTitle);
        note.setTitel(noteTitle);
        note.setText(convertedText);
        SaveNote command = new SaveNote(note);
        getCommandService().executeCommand(command);
    }


    /**
     * @param cnATreeElement
     * @param baust
     * @return
     */
    private BausteinUmsetzung findBausteinUmsetzung(CnATreeElement cnATreeElement, MbBaust baust) {
        Set<CnATreeElement> children = cnATreeElement.getChildren();
        for (CnATreeElement child : children) {
            if (child instanceof BausteinUmsetzung) {
                BausteinUmsetzung bstums = (BausteinUmsetzung) child;
                String id = TransferData.getId(baust);
                if (bstums.getKapitel().equals(id))
                    return bstums;
            }
        }
        return null;
    }
}
