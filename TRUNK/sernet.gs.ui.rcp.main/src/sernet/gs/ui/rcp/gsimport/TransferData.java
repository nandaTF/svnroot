/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
package sernet.gs.ui.rcp.gsimport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbDringlichkeit;
import sernet.gs.reveng.MbDringlichkeitId;
import sernet.gs.reveng.MbDringlichkeitTxt;
import sernet.gs.reveng.MbGefaehr;
import sernet.gs.reveng.MbRolleTxt;
import sernet.gs.reveng.MsUnj;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.ESAResult;
import sernet.gs.reveng.importData.GSDBConstants;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.reveng.importData.RAGefaehrdungenResult;
import sernet.gs.reveng.importData.RAGefaehrdungsMassnahmenResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Utility class to convert result sets (from gstool databases) to
 * verinice-objects.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class TransferData {

    private static final Logger LOG = Logger.getLogger(TransferData.class);
    
    private static char KEIN_SIEGEL = '-';
    
    // umsetzungs patterns in verinice
    // leaving out "unbearbeitet" since this is the default:
    private static final String[] UMSETZUNG_STATI_VN = new String[] { MassnahmenUmsetzung.P_UMSETZUNG_NEIN, MassnahmenUmsetzung.P_UMSETZUNG_JA, MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, };

    // umsetzungs patterns in gstool:
    private static final String[] UMSETZUNG_STATI_GST = new String[] { "nein", "ja", "teilweise", "entbehrlich", };

    private GSVampire vampire;
    private boolean importRollen;
    private List<MbDringlichkeitTxt> dringlichkeiten;
    private Map<String, String> drgMap;

    public TransferData(GSVampire vampire, boolean importRollen) {
        this.vampire = vampire;
        this.importRollen = importRollen;
    }

    public void transfer(ITVerbund itverbund, ZielobjektTypeResult result) throws CommandException {
        NZielobjekt source = result.zielobjekt;
        itverbund.setTitel(source.getName());
        itverbund.setExtId(result.zielobjekt.getGuid());
        CnAElementHome.getInstance().update(itverbund);
    }

    public static void transferUmsetzung(MassnahmenUmsetzung massnahmenUmsetzung, String gstStatus) {
        for (int i = 0; i < UMSETZUNG_STATI_GST.length; i++) {
            if (UMSETZUNG_STATI_GST[i].equals(gstStatus)) {
                massnahmenUmsetzung.setUmsetzung(UMSETZUNG_STATI_VN[i]);
                return;
            }
        }
    }

    public void transfer(CnATreeElement element, ZielobjektTypeResult result) {
        String typeId = element.getTypeId();
        if (typeId.equals(Anwendung.TYPE_ID)) {
            typedTransfer((Anwendung) element, result);
        }

        else if (typeId.equals(Client.TYPE_ID)) {
            typedTransfer((Client) element, result);
        }

        else if (typeId.equals(Server.TYPE_ID)) {
            typedTransfer((Server) element, result);
        }

        else if (typeId.equals(Person.TYPE_ID)) {
            typedTransfer((Person) element, result);

        }

        else if (typeId.equals(TelefonKomponente.TYPE_ID)) {
            typedTransfer((TelefonKomponente) element, result);
        }

        else if (typeId.equals(SonstIT.TYPE_ID)) {
            typedTransfer((SonstIT) element, result);
        }

        else if (typeId.equals(NetzKomponente.TYPE_ID)) {
            typedTransfer((NetzKomponente) element, result);
        }

        else if (typeId.equals(Gebaeude.TYPE_ID)) {
            typedTransfer((Gebaeude) element, result);
        }

        else if (typeId.equals(Raum.TYPE_ID)) {
            typedTransfer((Raum) element, result);
        }

        // use GSTOOL guid as extId:
        element.setExtId(result.zielobjekt.getGuid());
    }

    /**
     * Transfer fields for "Ergänzende Sicherheitsanalyse" from GSTOOL to
     * existing Zielobjekt.
     * 
     * @param target
     * @param esa
     */
    public void transferESA(CnATreeElement target, ESAResult esa) {
        // Zielobjekt-erg.sich.analyse
        // risikoanalyse J/N nZobEsa esamsunj
        // begründung bes. einsatz J/N Esaeinsatz 0 nein, 1 ja
        // begründung nicht mit bst. J/N esamodellierung
        // Begründung-text esabegründung
        // entscheidung entscheiddurch oder

        setEsaTrue(target, esa.getEinsatz(), esa.getModellierung());
        setRATrueFalse(target, esa.getUnj());

        String begruendung = "";
        if (esa.getEntscheidungDurch() != null && esa.getEntscheidungDurch().length() > 0) {
            begruendung += "Entscheidung durch: " + esa.getEntscheidungDurch() + "\n";
        }
        begruendung += esa.getBegruendung();
        setEsaBegruendung(target, begruendung);
    }

    /**
     * @param target
     * @param begruendung
     */
    private void setEsaBegruendung(CnATreeElement target, String begruendung) {

        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse_begruendung", begruendung);
        }

    }

    private void setRATrueFalse(CnATreeElement target, byte unj) {
        if (unj == GSDBConstants.UNJ_UNBEARBEITET) {
            return;
        }
        if (unj == GSDBConstants.UNJ_JA) {
            setRATrue(target);
        } else if (unj == GSDBConstants.UNJ_NEIN) {
            setRAFalse(target);
        }
    }

    private void setRATrue(CnATreeElement target) {
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse", "raum_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse", "anwendung_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse", "client_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse", "gebaeude_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse", "nkkomponente_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse", "server_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse", "sonstit_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse", "tkkomponente_risikoanalyse_noetig");
        }
    }

    private void setRAFalse(CnATreeElement target) {
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse", "raum_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse", "anwendung_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse", "client_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse", "gebaeude_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse", "nkkomponente_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse", "server_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse", "sonstit_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse", "tkkomponente_risikoanalyse_unnoetig");
        }
    }

    private void setEsaTrue(CnATreeElement target, byte besondererEinsatz, byte nichtModellierbar) {
        // one of the reasons has to be given, if not do nothing:
        if (besondererEinsatz == 0 && nichtModellierbar == 0) {
            return;
        }
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_ergaenzendeanalyse", "raum_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_ergaenzendeanalyse", "anwendung_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_ergaenzendeanalyse", "client_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_ergaenzendeanalyse", "gebaeude_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_ergaenzendeanalyse", "nkkomponente_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_ergaenzendeanalyse", "server_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_ergaenzendeanalyse", "sonstit_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_ergaenzendeanalyse", "tkkomponente_ergaenzendeanalyse_modell");
        }
    }

    /**
     * Transfer "gefaehrdungen" to existing "gefaehrdungsumsetzung" object in a
     * "risikoanalyse" parent.
     * 
     * @param gefaehrdungen
     * @param risikoanalyse
     * @throws IOException
     * @throws SQLException
     */
    public void transferRAGefaehrdungsUmsetzung(GefaehrdungsUmsetzung gefaehrdungsUmsetzung, RAGefaehrdungenResult ragResult) throws IOException {
        // gefährdungsbewertung:

        // vollständigkeit J/N
        // mechanismenstärke J/N
        // zuverlässigkeit J/N
        transferGefaehrdungsBewertung(gefaehrdungsUmsetzung, ragResult.getRzg().getMsUnjByZgVollstaUnjId(), ragResult.getRzg().getMsUnjByZgStaerkeUnjId(), ragResult.getRzg().getMsUnjByZgZuverlaUnjId());

        transferGefaehrdungsBewertungTxt(gefaehrdungsUmsetzung,
                // vollst begr
                // mechan begr
                // zuverl begr
                ragResult.getRzg().getZgVollstaBegr(), ragResult.getRzg().getZgStaerkeBegr(), ragResult.getRzg().getZgZuverlaBegr(),
                // unterschrift liegt vor J/N
                ragResult.getRzg().getMsUnjByZgUnterUnjId(),
                // Risikobehandlung begründung
                ragResult.getRzg().getZgRabBegr());

        // these dates are currently not transferred
        // durchf. Von
        // ragResult.getRzg().getZgDatumVon();
        // durchf. Bis
        // ragResult.getRzg().getZgDatumBis();

        // risikobehandlung A-D
        gefaehrdungsUmsetzung.setAlternative(String.valueOf(ragResult.getRisikobehandlungABCD()));

        // ausreichender schutz J/N
        if (ragResult.getRzg().getMsUnjByZgOkUnjId().getUnjId() == GSDBConstants.UNJ_JA) {
            gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_okay", "gefaehrdungsumsetzung_okay_yes");
        } else if (ragResult.getRzg().getMsUnjByZgOkUnjId().getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_okay", "gefaehrdungsumsetzung_okay_no");
        }

        // we skip these: (from GSTOOL GUI, columns unknown, could be some of
        // the four listed below:)
        // entscheider (link person)
        // datum der entscheidung

        //
        // Ben.def.gs gefährdung
        // --------------------
        // nr
        // katalog
        // typ (bendef) RaZobGef.MyesnoByZgIndivYesId.yesId - wrong it's
        // apparently mbgef.userdef
        // bezeichnung
        // version
        // gef.txt

        String gefNr = translateGefaehrdungsNr(ragResult.getGefaehrdung());
        gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_id", gefNr);

        gefaehrdungsUmsetzung.setDescription(convertClobToString(ragResult.getGefaehrdungTxt().getBeschreibung()));

        gefaehrdungsUmsetzung.setTitel(ragResult.getGefaehrdungTxt().getName());
        String url = transferUrl(ragResult.getGefaehrdung().getLink());
        gefaehrdungsUmsetzung.setUrl(url);
    }

    private String transferUrl(String url) {
        String regex = "(\\\\.*\\\\.*\\\\)(.*)(\\.html)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            url = matcher.group(2);
        }
        return url;
    }

    private void transferGefaehrdungsBewertungTxt(GefaehrdungsUmsetzung gefUms, String zgVollstaBegr, String zgStaerkeBegr, String zgZuverlaBegr, MsUnj unterschriftLiegtVor, String begruendungRisikobehandlung) {
        StringBuilder sb = new StringBuilder();

        if (zgVollstaBegr != null && zgVollstaBegr.length() > 0) {
            sb.append("Bewertung ver Vollständigkeit:\n");
            sb.append(zgVollstaBegr);
        }
        if (zgStaerkeBegr != null && zgStaerkeBegr.length() > 0) {
            sb.append("\n\nBewertung der Mechanismenstärke:\n");
            sb.append(zgStaerkeBegr);

        }
        if (zgZuverlaBegr != null && zgZuverlaBegr.length() > 0) {
            sb.append("\n\nBewertung der Zuverlässigkeit:\n");
            sb.append(zgZuverlaBegr);
        }
        sb.append("\n\n");

        if (unterschriftLiegtVor.getUnjId() == GSDBConstants.UNJ_JA) {
            sb.append("Unterschrift liegt vor.\n\n");
        }
        if (unterschriftLiegtVor.getUnjId() == GSDBConstants.UNJ_NEIN) {
            sb.append("Unterschrift liegt nicht vor.\n\n");
        }

        if (begruendungRisikobehandlung != null && begruendungRisikobehandlung.length() > 0) {
            sb.append("Begründung der Risikobehandlung:\n" + begruendungRisikobehandlung);
        }

        sb.append("\n\n" + gefUms.getEntity().getSimpleValue("gefaehrdungsumsetzung_erlaeuterung"));

        gefUms.setSimpleProperty("gefaehrdungsumsetzung_erlaeuterung", sb.toString());

    }

    private void transferGefaehrdungsBewertung(GefaehrdungsUmsetzung gefUms, MsUnj msUnjByZgVollstaUnjId, MsUnj msUnjByZgStaerkeUnjId, MsUnj msUnjByZgZuverlaUnjId) {
        if (msUnjByZgStaerkeUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_mechanismenstaerke", "gefaehrdungsumsetzung_mechanismenstaerke_ja");
        }
        if (msUnjByZgStaerkeUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_mechanismenstaerke", "gefaehrdungsumsetzung_mechanismenstaerke_nein");
        }

        if (msUnjByZgVollstaUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_vollstaendigkeit", "gefaehrdungsumsetzung_vollstaendigkeit_ja");
        }
        if (msUnjByZgVollstaUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_vollstaendigkeit", "gefaehrdungsumsetzung_vollstaendigkeit_nein");
        }

        if (msUnjByZgZuverlaUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_zuverlaessigkeit", "gefaehrdungsumsetzung_zuverlaessigkeit_ja");
        }
        if (msUnjByZgZuverlaUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_zuverlaessigkeit", "gefaehrdungsumsetzung_zuverlaessigkeit_nein");
        }
    }

    public boolean isUserDefGefaehrdung(MbGefaehr gefaehrdung) {
        return gefaehrdung.getUserdef() == GSDBConstants.USERDEF_YES;
    }

    /**
     * @param gefaehrdung
     * @return
     */
    private String translateGefaehrdungsNr(MbGefaehr gefaehrdung) {
        // this is how the displayed "number" has to be determined:
        if (gefaehrdung.getUserdef() == GSDBConstants.USERDEF_YES) {
            return "bG " + gefaehrdung.getGfkId() + "." + gefaehrdung.getNr();
        } else {
            return "G " + gefaehrdung.getGfkId() + "." + gefaehrdung.getNr();
        }
    }

    /**
     * Some columns are mapped by hibernate-console as CLOBS. This method
     * converts them to strings.
     * 
     * @param clob
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static String convertClobToString(Clob clob) throws IOException {
        try {
            Reader reader = clob.getCharacterStream();               
            OutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(reader, out, "UTF-8");
            return out.toString();
        } catch (SQLException e) {
            LOG.error("Error while converting clob to String", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Transfer all data from one "massnahme" from gstool to existing
     * "gefaehrdung" underneath a risk analysis in verinice.
     * 
     * @param result
     * @param gefaehrdung
     * @throws IOException
     * @throws SQLException
     */
    public void transferRAGefaehrdungsMassnahmen(RAGefaehrdungsMassnahmenResult ragmResult, GefaehrdungsUmsetzung gefUms, MassnahmenUmsetzung mnUms) throws SQLException, IOException {
        // Ben.def.gs massnahme
        // -------------------
        // katalog
        // typ (bendef)
        // nr
        // bezeichnung
        // version
        // maßnahmentext

        String massnahmeNr = translateMassnahmenNr(ragmResult);
        mnUms.setSimpleProperty("mnums_id", massnahmeNr);
        mnUms.setName(ragmResult.getMassnahmeTxt().getName());
        mnUms.setDescription(convertClobToString(ragmResult.getMassnahmeTxt().getBeschreibung()));
        mnUms.setErlaeuterung(ragmResult.getMzbm().getUmsBeschr());
        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_UMSETZUNGBIS, parseDate(ragmResult.getMzbm().getUmsDatBis()));
        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_LETZTEREVISIONAM, parseDate(ragmResult.getMzbm().getRevDat()));
        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_NAECHSTEREVISIONAM, parseDate(ragmResult.getMzbm().getRevDatNext()));
        mnUms.setRevisionBemerkungen(ragmResult.getMzbm().getRevBeschr());
        mnUms.setUrl(transferUrl(ragmResult.getMassnahme().getLink()));
        char siegel = convertToChar(ragmResult.getSiegelTxt().getKurzname());
        if(siegel!=KEIN_SIEGEL) {
            mnUms.setStufe(siegel);
        }
        transferUmsetzung(mnUms, ragmResult.getUmsTxt().getName());

        // may be necessary for user defined bausteine:

        // Massnahme-umsetzung
        // -------------------
        // nr
        // bezeichnung
        // bautein nr (rB 99.10)
        // baustein name
        // priorität
        // erforderlich ab A, b, c...
        // Umsetzung J,n,...
        // Lebenszyklusphase

    }


    /**
     * Convert ">G<", A, B, C, W, "-", "---" to a char
     * ">G<" is converted to G
     * 
     * @param kurzname
     * @return
     */
    private char convertToChar(String kurzname) {
        char result = KEIN_SIEGEL;
        if(kurzname!=null && !kurzname.isEmpty()) {    
            if(kurzname.length()>1)  {
                result = kurzname.toCharArray()[1];   
            } else {
                result = kurzname.toCharArray()[0];
            }
        }
        return result;
    }

    public boolean isUserDefMassnahme(RAGefaehrdungsMassnahmenResult ragmResult) {
        return ragmResult.getMassnahme().getUserdef() == GSDBConstants.USERDEF_YES;
    }

    /**
     * @param ragmResult
     * @return
     */
    private String translateMassnahmenNr(RAGefaehrdungsMassnahmenResult ragmResult) {
        if (ragmResult.getMassnahme().getUserdef() == GSDBConstants.USERDEF_YES) {
            return "bM " + ragmResult.getMassnahme().getMskId() + "." + ragmResult.getMassnahme().getNr();
        } else {
            return "M " + ragmResult.getMassnahme().getMskId() + "." + ragmResult.getMassnahme().getNr();
        }
    }

    private void typedTransfer(Anwendung element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
        element.setVerarbeiteteInformationen(result.zielobjekt.getAnwBeschrInf());
        element.setProzessBeschreibung(result.zielobjekt.getAnwInf2Beschr());
        element.setProzessWichtigkeit(translateDringlichkeit(result.zielobjekt.getMbDringlichkeit()));
        element.setProzessWichtigkeitBegruendung(result.zielobjekt.getAnwInf1Beschr());
    }

    private String translateDringlichkeit(MbDringlichkeit mbDringlichkeit) {
        if (mbDringlichkeit == null) {
            return "";
        }
        if (dringlichkeiten == null) {
            dringlichkeiten = vampire.findDringlichkeitAll();
        }

        if (drgMap == null) {
            drgMap = new HashMap<String, String>();
            drgMap.put("unterstützend", Anwendung.PROP_PROZESSBEZUG_UNTERSTUETZEND);
            drgMap.put("wichtig", Anwendung.PROP_PROZESSBEZUG_WICHTIG);
            drgMap.put("wesentlich", Anwendung.PROP_PROZESSBEZUG_WESENTLICH);
            drgMap.put("hochgradig notwendig", Anwendung.PROP_PROZESSBEZUG_HOCHGRADIG);
        }

        MbDringlichkeitId drgId = mbDringlichkeit.getId();
        String drgName = "";
        for (MbDringlichkeitTxt dringlichkeit : dringlichkeiten) {
            if (dringlichkeit.getId().getSprId() == 1 && dringlichkeit.getId().equals(drgId)) {
                drgName = dringlichkeit.getName();
                return drgMap.get(drgName);
            }
        }
        return "";
    }

    private void typedTransfer(Client element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(Server element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(Person element, ZielobjektTypeResult result) {
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(Person.P_NAME), result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(Person.P_PHONE), result.zielobjekt.getTelefon());
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType("person_orgeinheit"), result.zielobjekt.getAbteilung());
        

        if (importRollen) {
            List<MbRolleTxt> rollen = vampire.findRollenByZielobjekt(result.zielobjekt);
            for (MbRolleTxt rolle : rollen) {
                boolean success = element.addRole(rolle.getName());
                if (!success) {
                    Logger.getLogger(this.getClass()).debug("Rolle konnte nicht übertragen werden: " + rolle.getName());
                } else {
                    Logger.getLogger(this.getClass()).debug("Rolle übertragen: " + rolle.getName() + " für Benutzer " + element.getTitle());
                }
            }
        }

    }

    private void typedTransfer(TelefonKomponente element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(SonstIT element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(NetzKomponente element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(Gebaeude element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(Raum element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    public int translateSchutzbedarf(String name) {
        if (name.equals("normal")) {
            return Schutzbedarf.NORMAL;
        }
        if (name.equals("hoch")) {
            return Schutzbedarf.HOCH;
        }
        if (name.equals("sehr hoch")) {
            return Schutzbedarf.SEHRHOCH;
        }
        return Schutzbedarf.UNDEF;
    }

    /**
     * @param importTask
     * @param searchResult
     * @return
     */
    public Map<MbBaust, List<BausteineMassnahmenResult>> convertBausteinMap(List<BausteineMassnahmenResult> searchResult) {
        // convert list to map: of bausteine and corresponding massnahmen:
        Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = new HashMap<MbBaust, List<BausteineMassnahmenResult>>();
        for (BausteineMassnahmenResult result : searchResult) {
            List<BausteineMassnahmenResult> list = bausteineMassnahmenMap.get(result.baustein);
            if (list == null) {
                list = new ArrayList<BausteineMassnahmenResult>();
                bausteineMassnahmenMap.put(result.baustein, list);
            }
            list.add(result);
        }
        return bausteineMassnahmenMap;
    }

    /**
     * Convert searchResult to map of baustein : list of massnahmen with notes
     * 
     * @param notesResults
     */
    public Map<MbBaust, List<NotizenMassnahmeResult>> convertZielobjektNotizenMap(List<NotizenMassnahmeResult> searchResult) {
        Map<MbBaust, List<NotizenMassnahmeResult>> bausteineMassnahmenMap = new HashMap<MbBaust, List<NotizenMassnahmeResult>>();
        for (NotizenMassnahmeResult result : searchResult) {
            List<NotizenMassnahmeResult> list = bausteineMassnahmenMap.get(result.baustein);
            if (list == null) {
                list = new ArrayList<NotizenMassnahmeResult>();
                bausteineMassnahmenMap.put(result.baustein, list);
            }
            list.add(result);
        }
        return bausteineMassnahmenMap;
    }

    public static String getId(MbBaust mbBaust) {
        Pattern pattern = Pattern.compile("(\\d+)\\.0*(\\d+)");

        Matcher match = pattern.matcher(mbBaust.getNr());
        if (match.matches()) {
            return "B " + match.group(1) + "." + Integer.parseInt(match.group(2));
        }
        // TODO AK if none found return ben.def.baustein number
        return "";
    }

    public static BausteineMassnahmenResult findMassnahmenVorlageBaustein(MassnahmenUmsetzung massnahmenUmsetzung, List<BausteineMassnahmenResult> list) {
        for (BausteineMassnahmenResult result : list) {
            if (massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme.getMskId() && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme.getNr()) {
                return result;
            }
        }
        return null;
    }

    /**
     * @param mnums
     * @param massnahmenNotizen
     */
    public static List<NotizenMassnahmeResult> findMassnahmenVorlageNotiz(MassnahmenUmsetzung massnahmenUmsetzung, List<NotizenMassnahmeResult> list) {
        List<NotizenMassnahmeResult> resultList = new ArrayList<NotizenMassnahmeResult>();

        for (NotizenMassnahmeResult result : list) {
            if (result.massnahme != null && massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme.getMskId() && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme.getNr()) {
                resultList.add(result);
            }
        }
        return resultList;

    }

    public static String convertRtf(String notizText) throws IOException, BadLocationException {
        StringReader reader = new StringReader(notizText);
        RTFEditorKit kit = new RTFEditorKit();
        Document document = kit.createDefaultDocument();
        kit.read(reader, document, 0);
        // return plaintext
        return document.getText(0, document.getLength());
    }

    /**
     * Find notes that are connected to a baustein directly.
     * 
     * @param bstUms
     * @param massnahmenNotizen
     * @return
     */
    public static List<NotizenMassnahmeResult> findBausteinVorlageNotiz(BausteinUmsetzung bstUms, List<NotizenMassnahmeResult> list) {

        List<NotizenMassnahmeResult> resultList = new ArrayList<NotizenMassnahmeResult>();

        for (NotizenMassnahmeResult result : list) {
            if (result.massnahme == null) {
                Integer refZobId = result.zoBst.getRefZobId();
                boolean stop = refZobId != null;
                resultList.add(result);
            }
        }
        return resultList;

    }

    /**
     * @param ownGefaehrdung
     * @param ragResult
     * @throws IOException
     * @throws SQLException
     */
    public void transferOwnGefaehrdung(OwnGefaehrdung ownGefaehrdung, RAGefaehrdungenResult ragResult) throws SQLException, IOException {
        String gefNr = translateGefaehrdungsNr(ragResult.getGefaehrdung());
        ownGefaehrdung.setId(gefNr);
        ownGefaehrdung.setTitel(ragResult.getGefaehrdungTxt().getName());
        ownGefaehrdung.setBeschreibung(convertClobToString(ragResult.getGefaehrdungTxt().getBeschreibung()));
    }
    
    private String parseDate(Date date) {
        if (date != null) {
            return Long.toString(date.getTime());
        }
        return "";
    }

}
