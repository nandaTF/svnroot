package sernet.gs.scraper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;

/**
 * Scraper to extract modules and safeguards from BSI's HTML Files using XQuery
 * FLWOR expressions.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class GSScraper {
	

	private final Map<String, String[]> BROKEN_ROLES = new HashMap<String, String[]>();
	
	private IGSPatterns patterns;

	private String stand;

	private IGSSource source;
	private Configuration config;
	private XQueryExpression getBausteineExp;
	private DynamicQueryContext bausteinContext;

	private XQueryExpression getMassnahmenExp;
	private XQueryExpression getGefaehrdungenExp;
	private DynamicQueryContext massnahmenContext;

	private XQueryExpression getTitleExp;
	private DynamicQueryContext titleContext;

	private DynamicQueryContext gefaehrdungenContext;

	private XQueryExpression massnahmenVerantwortlicheExp;

	private DynamicQueryContext massnahmenVerantowrtlicheContext;
	
	private Pattern trailingwhitespace = Pattern.compile("\\s*$");
	private Pattern leadingwhitespace = Pattern.compile("^\\s*");

	public GSScraper(IGSSource source, IGSPatterns patterns)
			throws GSServiceException {

		try {
			BROKEN_ROLES.put("Behörden-/Unter-nehmensleitung",
					new String[] {"Behörden-/Unternehmensleitung"});
			BROKEN_ROLES.put("IT-Sicherheits-management",
					new String[] {"IT-Sicherheitsmanagement"});
			BROKEN_ROLES.put("IT-Sicherheitsmanagement-Team", 
					new String[] {"IT-Sicherheitsmanagement"});
			BROKEN_ROLES.put("IT-Sicherheitsmanagement Administrator", 
					new String[] {"IT-Sicherheitsmanagement", "Administrator"});
			BROKEN_ROLES.put("Leiter IT Administrator", 
					new String[] {"Leiter IT", "Administrator"});
			BROKEN_ROLES.put("Leiter IT IT-Sicherheitsmanagement", 
					new String[] {"Leiter IT", "IT-Sicherheitsmanagement"});
			
			
			this.patterns = patterns;
			this.source = source;
			config = new Configuration();
			StaticQueryContext staticContext;
			staticContext = new StaticQueryContext(config);

			getBausteineExp = staticContext.compileQuery(patterns
					.getBausteinPattern());
			bausteinContext = new DynamicQueryContext(config);

			getMassnahmenExp = staticContext.compileQuery(patterns
					.getMassnahmePattern());
			massnahmenContext = new DynamicQueryContext(config);

			getGefaehrdungenExp = staticContext.compileQuery(patterns
					.getGefaehrdungPattern());
			gefaehrdungenContext = new DynamicQueryContext(config);

			getTitleExp = staticContext
					.compileQuery(patterns.getTitlePattern());
			titleContext = new DynamicQueryContext(config);

			massnahmenVerantwortlicheExp = staticContext.compileQuery(patterns
					.getMassnahmeVerantwortlichePattern());
			massnahmenVerantowrtlicheContext = new DynamicQueryContext(config);

		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}

	}

	public List<Baustein> getBausteine(String kapitel)
			throws GSServiceException {
		ArrayList<Baustein> result = new ArrayList<Baustein>();
		try {

			Node root = source.parseBausteinDocument(kapitel);
			getStand(kapitel, root);

			bausteinContext.setContextItem(new DocumentWrapper(root, kapitel,
					config));
			// dynamicContext.setContextItem(
			// staticContext.buildDocument(
			// new StreamSource(new File("test.xml"))));

			SequenceIterator iterator = getBausteineExp
					.iterator(bausteinContext);

			while (true) {
				NodeInfo baust = (NodeInfo) iterator.next();
				if (baust == null)
					break;
				String found = baust.getStringValue();
				found = found.replaceAll("\n", "");
				found = found.replaceAll(".htm", "");

				Matcher matcher = patterns.getBaustPat().matcher(found);
				if (matcher.matches()) {
					Baustein b = new Baustein();
					b.setStand(stand);
					b.setId(matcher.group(1));
					b.setTitel(matcher.group(2));
					b.setUrl(matcher.group(3));

					Matcher schichtMatcher = patterns.getSchichtPat().matcher(
							matcher.group(1));
					String schicht = "0";
					if (schichtMatcher.find())
						schicht = schichtMatcher.group(1);
					b.setSchicht(Integer.parseInt(schicht));

					result.add(b);

				}
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result;
	}

	private void getStand(String kapitel, Node root) throws XPathException {
		// if (stand != null)
		// return;

		titleContext.setContextItem(new DocumentWrapper(root, kapitel, config));
		SequenceIterator iterator = getTitleExp.iterator(titleContext);
		NodeInfo title = (NodeInfo) iterator.next();
		if (title != null) {
			Matcher matcher = patterns.getStandPat().matcher(
					title.getStringValue());
			if (matcher.find()) {
				stand = matcher.group(1);
			}
		}
	}

	public List<Massnahme> getMassnahmen(String baustein)
			throws GSServiceException {
		List<Massnahme> result = new ArrayList<Massnahme>();
		try {
			Node root = source.parseBausteinDocument(baustein);
			getStand(baustein, root);
			massnahmenContext.setContextItem(new DocumentWrapper(root,
					baustein, config));

			SequenceIterator iterator = getMassnahmenExp
					.iterator(massnahmenContext);

			// normal pattern for massnahmen:
			Pattern pat = Pattern.compile("(.*)°(.*)°(.+)°(.*)°\\((.*)\\)");

			// sometimes the 3rd column is missing:
			Pattern pat2 = Pattern
					.compile("(.*)°(.*)°°(.*)°\\s*\\((.)\\)\\s*(.*)");
			// i.e.: Planung und Konzeption°M 2.343°°m02343°(C) Absicherung
			// eines SAP Systems im Portal-Szenario

			while (true) {
				NodeInfo mnNode = (NodeInfo) iterator.next();
				if (mnNode == null)
					break;
				String found = mnNode.getStringValue();
				// clear up paths, remove relative paths (don't work in zipfile)
				found = found.replaceAll("\n", "");
				found = found.replaceAll(".htm", "");
				found = found.replaceAll("\\.\\./m/", "");
				found = found.replaceAll("\\.\\./\\.\\./", "");
				// System.out.println(found);

				Matcher matcher = pat.matcher(found);
				if (matcher.matches()) {
					Massnahme mn = new Massnahme();
					mn.setStand(stand);
					setLebenszyklus(mn, matcher.group(1));
					mn.setId(matcher.group(2));
					mn.setTitel(matcher.group(3));
					mn.setUrl(matcher.group(4));
					if (matcher.group(5) != null
							&& matcher.group(5).length() > 0)
						mn.setSiegelstufe(matcher.group(5).charAt(0));
					else {
						Logger.getLogger(this.getClass()).error(
								"Konnte Siegelstufe nicht bestimmen für: "
										+ mn.getId()
										+ "\n Setze auf Stufe A (höchste).");
						mn.setSiegelstufe('A');
					}
					addRoles(mn);
					result.add(mn);
				} else {
					// sometimes, 3rd column is missing
					// siegel included in 4th column:
					matcher = pat2.matcher(found);
					if (matcher.matches()) {
						Massnahme mn = new Massnahme();
						mn.setStand(stand);
						setLebenszyklus(mn, matcher.group(1));
						mn.setId(matcher.group(2));
						mn.setUrl(matcher.group(3));
						mn.setTitel(matcher.group(5));
						if (matcher.group(4) != null
								&& matcher.group(4).length() > 0)
							mn.setSiegelstufe(matcher.group(4).charAt(0));
						else {
							Logger
									.getLogger(this.getClass())
									.error(
											"Konnte Siegelstufe nicht bestimmen für: "
													+ mn.getId()
													+ "\n Setze auf Stufe A (höchste).");
							mn.setSiegelstufe('A');
						}
						addRoles(mn);
						result.add(mn);
					}
				}

			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result;

	}

	private void addRoles(Massnahme mn) throws GSServiceException,
			XPathException {
		Node root = source.parseMassnahmenDocument(mn.getUrl());
		
//		XML2String out = new XML2String();
//		String writeXML = out.writeXML((Document) root);
//		System.out.println(writeXML);
		
		massnahmenVerantowrtlicheContext.setContextItem(new DocumentWrapper(
				root, mn.getUrl(), config));
		SequenceIterator iterator = massnahmenVerantwortlicheExp
				.iterator(massnahmenVerantowrtlicheContext);
		
		int foundItems = 0;
		
		while (true) {
			NodeInfo roleNode = (NodeInfo) iterator.next();
			if (roleNode == null) 
				break;
			
			foundItems++;
			String allRoles = roleNode.getStringValue();
			allRoles = allRoles.replaceAll("\n", "");
			

			if (allRoles != null && allRoles.length() > 0) {
				String[] rolesInit = allRoles.split(", *");
				for (String role : rolesInit) {
					role = trailingwhitespace.matcher(role).replaceFirst("");
					role = leadingwhitespace.matcher(role).replaceFirst("");
					String[] repairedRoles = repairBrokenRole(role);
					for (String repairedRole : repairedRoles) {
						switch (foundItems) {
						case 1:
							mn.addVerantwortlicheInitiierung(repairedRole);
							break;
						case 2:
							mn.addVerantwortlicheUmsetzung(repairedRole);
							break;
						}
					}
				}
			}
		}
		
	}

	/** 
	 * Repair some roles with dashes etc. Split roles missing commas in GS-Catalogues etc.
	 * 
	 * @param role
	 * @return
	 */
	private String[] repairBrokenRole(String role) {
		String[] repairedRole = this.BROKEN_ROLES.get(role);
		if (repairedRole != null)
			return repairedRole;
		else
			return new String[] {role};
		
	}

	private void setLebenszyklus(Massnahme mn, String lzString) {
		if (lzString.equals(Massnahme.LZ_STRING_Ausonderung))
			mn.setLebenszyklus(Massnahme.LZ_AUSSONDERUNG);

		else if (lzString.equals(Massnahme.LZ_STRING_Beschaffung))
			mn.setLebenszyklus(Massnahme.LZ_BESCHAFFUNG);

		else if (lzString.equals(Massnahme.LZ_STRING_Betrieb))
			mn.setLebenszyklus(Massnahme.LZ_BETRIEB);

		else if (lzString.equals(Massnahme.LZ_STRING_Notfall))
			mn.setLebenszyklus(Massnahme.LZ_NOTFALL);

		else if (lzString.equals(Massnahme.LZ_STRING_Planung))
			mn.setLebenszyklus(Massnahme.LZ_PLANUNG);

		else if (lzString.equals(Massnahme.LZ_STRING_Umsetzung))
			mn.setLebenszyklus(Massnahme.LZ_UMSETZUNG);
	}

	public InputStream getBausteinText(String url, String stand)
			throws GSServiceException {
		// if (!stand.equals(this.stand))
		// throw new
		// GSServiceException("Versionstand des Bausteins weicht von geladenen "
		// +
		// "Grundschutz-Katalogen ab.");
		return source.getBausteinAsStream(url);
	}

	public InputStream getMassnahme(String url, String stand)
			throws GSServiceException {
		// if (!stand.equals(this.stand))
		// throw new
		// GSServiceException("Versionstand der Massnahme weicht von geladenen "
		// +
		// "Grundschutz-Katalogen ab.");
		return source.getMassnahmeAsStream(url);
	}

	public InputStream getGefaehrdung(String url, String stand)
			throws GSServiceException {
		// if (!stand.equals(this.stand))
		// throw new
		// GSServiceException("Versionstand des Bausteins weicht von geladenen "
		// +
		// "Grundschutz-Katalogen ab.");
		return source.getGefaehrdungAsStream(url);
	}

	public List<Gefaehrdung> getGefaehrdungen(String baustein)
			throws GSServiceException {
		List<Gefaehrdung> result = new ArrayList<Gefaehrdung>();
		try {
			Node root = source.parseBausteinDocument(baustein);
			getStand(baustein, root);
			gefaehrdungenContext.setContextItem(new DocumentWrapper(root,
					baustein, config));

			// return kategorie, id, titel, url
			SequenceIterator iterator = getGefaehrdungenExp
					.iterator(gefaehrdungenContext);
			Pattern pat = Pattern.compile("(.*)°(.*)°(.*)°(.*)");

			while (true) {
				NodeInfo gfNode = (NodeInfo) iterator.next();
				if (gfNode == null)
					break;
				String found = gfNode.getStringValue();
				found = found.replaceAll("\n", "");
				found = found.replaceAll(".htm", "");
				found = found.replaceAll("../g/", "");
				// System.out.println(found);

				Matcher matcher = pat.matcher(found);
				if (matcher.matches()) {
					Gefaehrdung gef = new Gefaehrdung();
					gef.setStand(stand);
					gef.setKategorie(Gefaehrdung.kategorieAsInt(matcher
							.group(1)));
					gef.setId(matcher.group(2));
					gef.setTitel(matcher.group(3));
					gef.setUrl(matcher.group(4));
					result.add(gef);
				}
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result;

	}

}
