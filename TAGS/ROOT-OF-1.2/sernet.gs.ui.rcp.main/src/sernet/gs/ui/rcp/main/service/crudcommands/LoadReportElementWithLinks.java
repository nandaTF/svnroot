package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads an element with all links from / to it.
 */
public class LoadReportElementWithLinks extends GenericCommand {


	private String typeId;
    private Integer rootElement;
    List<List<String>> result;
    
    public LoadReportElementWithLinks(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	    try {
            command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	    CnATreeElement root = command.getElements().get(0);
	    
	    loadLinks(root);
	    
	}

	/**
     * @param root
     * @param typeId2
     * @return
     */
    private void loadLinks(CnATreeElement root) {
        result = new ArrayList<List<String>>();
        for (CnALink link : root.getLinksDown()) {
            if (typeId == null )
                result.add(makeRow(root, link));
            else {
                if (link.getDependency().getTypeId().equals(typeId))
                    result.add(makeRow(root, link));
            }
        }
        for (CnALink link : root.getLinksUp()) {
            if (typeId == null )
                result.add(makeRow(root, link));
            else {
                if (link.getDependant().getTypeId().equals(typeId))
                    result.add(makeRow(root, link));
            }
        }
    }
    
    public static final String[] COLLUMNS = new String[] {"relationName", "toElement", "riskC", "riskI", "riskA"};

    /**
     * @param root
     * @param link
     * @return
     */
    private List<String> makeRow(CnATreeElement root, CnALink link) {
        String relationName = CnALink.getRelationName(root, link);
        String toElementTitle = CnALink.getRelationObjectTitle(root, link);
        String riskC = Integer.toString(link.getRiskConfidentiality());
        String riskI = Integer.toString(link.getRiskIntegrity());
        String riskA = Integer.toString(link.getRiskAvailability());
        List<String> asList = Arrays.asList(relationName, toElementTitle, riskC, riskI, riskA);
        return asList;
    }

    /**
     * @return the result
     */
    public List<List<String>> getResult() {
        return result;
    }

   

  
	


}
