package sernet.gs.reveng.importData;

import sernet.gs.reveng.MbGefaehr;
import sernet.gs.reveng.MbGefaehrTxt;
import sernet.gs.reveng.NZielobjekt;

public class RAGefaehrdungenResult {
/* 	+ "select z, g, gtxt, rabtxt.kurz"
		+ "from " 
		+ "	MbGefaehr g, MbGefaehrTxt gtxt,"
		+ "	RaZobGef rzg, MsRaBehandTxt rabtxt, NZielobjekt z" +
		"where  rzg.id.zobId = z.id.zobId"
		*/

		private NZielobjekt zielobjekt;
		private MbGefaehr gefaehrdung;
		private MbGefaehrTxt gefaehrdungTxt;
		private char risikobehandlungABCD;
		
		public RAGefaehrdungenResult(NZielobjekt zielobjekt,
				MbGefaehr gefaehrdung, MbGefaehrTxt gefaehrdungTxt,
				char risikobehandlungABCD) {
			super();
			this.zielobjekt = zielobjekt;
			this.gefaehrdung = gefaehrdung;
			this.gefaehrdungTxt = gefaehrdungTxt;
			this.risikobehandlungABCD = risikobehandlungABCD;
		}

		public NZielobjekt getZielobjekt() {
			return zielobjekt;
		}

		public void setZielobjekt(NZielobjekt zielobjekt) {
			this.zielobjekt = zielobjekt;
		}

		public MbGefaehr getGefaehrdung() {
			return gefaehrdung;
		}

		public void setGefaehrdung(MbGefaehr gefaehrdung) {
			this.gefaehrdung = gefaehrdung;
		}

		public MbGefaehrTxt getGefaehrdungTxt() {
			return gefaehrdungTxt;
		}

		public void setGefaehrdungTxt(MbGefaehrTxt gefaehrdungTxt) {
			this.gefaehrdungTxt = gefaehrdungTxt;
		}

		public char getRisikobehandlungABCD() {
			return risikobehandlungABCD;
		}

		public void setRisikobehandlungABCD(char risikobehandlungABCD) {
			this.risikobehandlungABCD = risikobehandlungABCD;
		}
		
}
