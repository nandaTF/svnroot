package sernet.gs.ui.rcp.main.bsi.model;

import java.util.regex.Pattern;

import sernet.hui.common.connect.Property;

public abstract class Schutzbedarf {

	public static final String VERTRAULICHKEIT 	= "_vertraulichkeit";
	public static final String VERFUEGBARKEIT 		= "_verfuegbarkeit";
	public static final String INTEGRITAET 		= "_integritaet";
	
	private static Pattern pat_vertraulichkeit = Pattern.compile(".*" + VERTRAULICHKEIT + "$");
	private static Pattern pat_verfuegbarkeit = Pattern.compile(".*" + VERFUEGBARKEIT + "$");
	private static Pattern pat_integritaet = Pattern.compile(".*" + INTEGRITAET + "$");

	public static final String VERTRAULICHKEIT_BEGRUENDUNG	= "_vertraulichkeit_begruendung";
	public static final String VERFUEGBARKEIT_BEGRUENDUNG	= "_verfuegbarkeit_begruendung";
	public static final String INTEGRITAET_BEGRUENDUNG		= "_integritaet_begruendung";

	public static final String SUFFIX_NONE 		= "";
	public static final String SUFFIX_NORMAL 		= "_normal";
	public static final String SUFFIX_HOCH 		= "_hoch";
	public static final String SUFFIX_SEHRHOCH 	= "_sehrhoch";
	
	
	public static final int UNDEF 		= 0;
	public static final int NORMAL 	= 1;
	public static final int HOCH 		= 2; 
	public static final int SEHRHOCH	= 3;
	
	public static final String MAXIMUM = "Maximumprinzip";

	public static final String ERGAENZENDEANALYSE = "_ergaenzendeanalyse";
	private static final String ERGAENZENDEANALYSE_NOETIG = "_modell";
	
	
	public static int toInt(String option) {
		if (option.indexOf(SUFFIX_SEHRHOCH) > -1)
			return SEHRHOCH;
		if (option.indexOf(SUFFIX_HOCH) > -1)
			return HOCH;
		return NORMAL;
	}
	
	public static boolean isMaximumPrinzip(String description) {
		return description.indexOf(Schutzbedarf.MAXIMUM) != -1;
	}

	public static boolean isVerfuegbarkeit(Property prop) {
		return pat_verfuegbarkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isVertraulichkeit(Property prop) {
		return pat_vertraulichkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isIntegritaet(Property prop) {
		return pat_integritaet.matcher(prop.getPropertyTypeID()).matches();
	}

	public static String toOption(String type_id, String schutzbedarf, int level) {
		StringBuffer buf = new StringBuffer();
		buf.append(type_id);
		buf.append(schutzbedarf);
		buf.append(getLevel(level));
		return buf.toString();
	}

	private static String getLevel(int i) {
		switch(i) {
		case NORMAL:
			return SUFFIX_NORMAL;
		case HOCH:
			return SUFFIX_HOCH;
		case SEHRHOCH:
			return SUFFIX_SEHRHOCH;
		}
		return SUFFIX_NONE;
	}

	
	
	public static boolean isVerfuegbarkeitBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(VERFUEGBARKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVertraulichkeitBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(VERTRAULICHKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isIntegritaetBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(INTEGRITAET_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVerfuegbarkeitBegruendung(String prop) {
		return prop.indexOf(VERFUEGBARKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVertraulichkeitBegruendung(String prop) {
		return prop.indexOf(VERTRAULICHKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isIntegritaetBegruendung(String prop) {
		return prop.indexOf(INTEGRITAET_BEGRUENDUNG) != -1;
	}

	public static boolean isMgmtReviewNeeded(String propertyValue) {
		return propertyValue.indexOf(ERGAENZENDEANALYSE_NOETIG)!=-1;
	}
}
