package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class NKKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "netzkategorie"; //$NON-NLS-1$
	

	public NKKategorie(CnATreeElement model) {
		super(model);
	}
	
	protected NKKategorie() {
		
	}
	@Override
	public String getTitel() {
		return "Netzverbindungen";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof NetzKomponente )
			return true;
		return false;
	}
}
