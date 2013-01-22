package sernet.gs.reveng;

import java.util.Date;

/**
 * MbZielobjSubtypTxt entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjSubtypTxt implements java.io.Serializable {

	// Fields

	private MbZielobjSubtypTxtId id;
	private String name;
	private String beschreibung;
	private String htmltext;
	private String guid;
	private Date timestamp;
	private Short impNeu;
	private String guidOrg;
	private String abstractText;
	private Date changedOn;
	private String changedBy;

	// Constructors

	/** default constructor */
	public MbZielobjSubtypTxt() {
	}

	/** minimal constructor */
	public MbZielobjSubtypTxt(MbZielobjSubtypTxtId id, String name,
			String guid, Date timestamp) {
		this.id = id;
		this.name = name;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
	}

	/** full constructor */
	public MbZielobjSubtypTxt(MbZielobjSubtypTxtId id, String name,
			String beschreibung, String htmltext, String guid, Date timestamp,
			Short impNeu, String guidOrg, String abstractText, Date changedOn,
			String changedBy) {
		this.id = id;
		this.name = name;
		this.beschreibung = beschreibung;
		this.htmltext = htmltext;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.abstractText = abstractText;
		this.changedOn = (changedOn != null) ? (Date)changedOn.clone() : null;
		this.changedBy = changedBy;
	}

	// Property accessors

	public MbZielobjSubtypTxtId getId() {
		return this.id;
	}

	public void setId(MbZielobjSubtypTxtId id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public String getHtmltext() {
		return this.htmltext;
	}

	public void setHtmltext(String htmltext) {
		this.htmltext = htmltext;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getTimestamp() {
		return (this.timestamp != null) ? (Date)this.timestamp.clone() : null;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
	}

	public Short getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Short impNeu) {
		this.impNeu = impNeu;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public String getAbstractText() {
		return this.abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public Date getChangedOn() {
		return (this.changedOn != null) ? (Date)this.changedOn.clone() : null;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = (changedOn != null) ? (Date)changedOn.clone() : null;
	}

	public String getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

}