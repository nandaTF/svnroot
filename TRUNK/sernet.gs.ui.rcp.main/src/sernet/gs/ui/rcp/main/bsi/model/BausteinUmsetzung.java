/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyType;

public class BausteinUmsetzung extends CnATreeElement {

	public static final String TYPE_ID = "bstumsetzung"; //$NON-NLS-1$
	public static final String P_NAME = "bstumsetzung_name"; //$NON-NLS-1$
	public static final String P_NR = "bstumsetzung_nr"; //$NON-NLS-1$
	public static final String P_URL = "bstumsetzung_url"; //$NON-NLS-1$
	@Deprecated
	public static final String P_GESPRAECHSPARTNER_OLD= "bstumsetzung_gespraechspartner"; //$NON-NLS-1$
	public static final String P_GESPRAECHSPARTNER_LINK= "bstumsetzung_gespraechspartner_link"; //$NON-NLS-1$
	public static final String P_STAND = "bstumsetzung_stand"; //$NON-NLS-1$
	public static final String P_ERLAEUTERUNG 	= "bstumsetzung_erlaeuterung"; //$NON-NLS-1$
	public static final String P_ERFASSTAM 	= "bstumsetzung_erfasstam"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ERFASSTDURCH_OLD = "bstumsetzung_erfasstdurch"; //$NON-NLS-1$
	public static final String P_ERFASSTDURCH_LINK = "bstumsetzung_erfasstdurch_link"; //$NON-NLS-1$


	private final static Pattern kapitelPattern 
		= Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$

	private final static String[] schichten = new String[] {
		"1", "2", "3", "4", "5" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	};
	
	private final static String[] schichtenBezeichnung = new String[] {
		"Übergeordnete Aspekte",
		"Infrastruktur",
		"IT-Systeme",
		"Netze",
		"Anwendungen"
	};

	
	public BausteinUmsetzung(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
	}
	
	protected BausteinUmsetzung() {
		// hibernate constructor
	}
	
	public void setKapitel(String kap) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_NR), kap);
	}

	public void setStand(String stand) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_STAND), stand);
	}
	
	public void setName(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_NAME), name);
	}
	
	
	@Override
	public String getTitel() {
		return getEntity().getSimpleValue(P_NR) 
			+ " " + getEntity().getSimpleValue(P_NAME); //$NON-NLS-1$
	}
	
	public String getKapitel() {
		return getEntity().getSimpleValue(P_NR);
	}
	
	public int[] getKapitelValue() {
		int[] kapitel = new int[2];
		Matcher m = kapitelPattern.matcher(getKapitel());
		if (m.find()) {
			try {
				kapitel[0] = Integer.parseInt(m.group(1));
				kapitel[1] = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass())
					.error("Kapitelnummer des Bausteins ungültig.", e);
			}
		}
		return kapitel;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	public List<MassnahmenUmsetzung> getMassnahmenUmsetzungen() {
		List<MassnahmenUmsetzung> result = new ArrayList<MassnahmenUmsetzung>(40);
		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			MassnahmenUmsetzung mnu = (MassnahmenUmsetzung) iter.next();
			result.add(mnu);
		}
		return result;
	}
	
	/**
	 * Überprüft anhand der umgesetzten Massnahmen, welche Siegestufe
	 * in diesem Baustein bereits erreicht wurde.
	 * 
	 * @return Siegelstufe als 0, A, B, C
	 */
	public char getErreichteSiegelStufe() {
		// starte mit höchster Stufe und reduziere anhand nicht-umgesetzter Massnahmen:
		char erreichteStufe = 'C'; 
		
		// bausteine ohne massnahmen werden als '0' = "keine umsetzungsstufe" dargestellt:
		if (getChildren().size() == 0)
			return '0';
			
		allMassnahmen: for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof MassnahmenUmsetzung)) {
				// should never happen:
				Logger.getLogger(this.getClass()).error(obj.getClass());
				return '0';
			}
			
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) obj;
			// prüfe nicht umgesetzte Massnahmen:
			if (mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_NEIN)
					|| mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE)
					|| mn.getUmsetzung().equals(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET)) {
				switch (mn.getStufe()) {
				case 'A':
					// reduzieren auf 0 und return:
					erreichteStufe = '0';
					break allMassnahmen;
				case 'B':
					// reduzieren auf A:
					erreichteStufe = 'A';
					break;
				case 'C':
					// reduzieren auf B:
					if (erreichteStufe != 'A')
						erreichteStufe = 'B';
					break;
				default:
					// change nothing (Z nicht umgesetzt, optional)	
				}
			}
		}
		return erreichteStufe;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof MassnahmenUmsetzung)
			return true;
		return false;
	}

	public static String[] getSchichten() {
		return schichten;
	}

	public static String[] getSchichtenBezeichnung() {
		return schichtenBezeichnung;
	}
	
	public static String getSchichtenBezeichnung(String schichtNummer) {
		for (int i = 0; i < schichten.length; i++) {
			if (schichten[i].equals(schichtNummer))
				return schichtenBezeichnung[i];
		}
		return ""; //$NON-NLS-1$
	}

	public String getUrl() {
		return getEntity().getSimpleValue(P_URL);
	}

	public void setUrl(String url2) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(P_URL), url2);
	}

	public MassnahmenUmsetzung getMassnahmenUmsetzung(String url) {
		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) iter.next();
			if (mn.getUrl().equals(url))
				return mn;
		}
		return null;
	}

	public String getStand() {
		return getEntity().getSimpleValue(P_STAND);
	}

	public void addBefragtePersonDurch(Person personToLink) {
		PropertyType propertyType = getEntityType().getPropertyType(P_GESPRAECHSPARTNER_LINK);
		getEntity().createNewProperty(propertyType, personToLink.getEntity().getDbId().toString());
	}

	public void addBefragungDurch(Person person) {
		PropertyType propertyType = getEntityType().getPropertyType(P_ERFASSTDURCH_LINK);
		getEntity().createNewProperty(propertyType, person.getEntity().getDbId().toString());
	}

}
