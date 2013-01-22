package sernet.gs.reveng;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbBaust entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbBaust implements java.io.Serializable {

	// Fields

	private MbBaustId id;
	private MsCmState msCmState;
	private MbBaust mbBaust;
	private MMetastatus mMetastatus;
	private MbSchicht mbSchicht;
	private MMetatyp mMetatyp;
	private SysImport sysImport;
	private NmbNotiz nmbNotiz;
	private NZielobjekt nZielobjekt;
	private Integer nrNum;
	private String nr;
	private String link;
	private Byte auditrelevantJn;
	private Integer metaVers;
	private Integer obsoletVers;
	private Date loeschDatum;
	private String guid;
	private Date timestamp;
	private Integer usn;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Short impNeu;
	private String guidOrg;
	private Date changedOn;
	private String changedBy;
	private Date cmTimestamp;
	private String cmUsername;
	private Integer cmImpId;
	private Integer cmVerId1;
	private Short cmVerId2;
	private Set modZobjBsts = new HashSet(0);
	private Set mbBausts = new HashSet(0);

	// Constructors

	/** default constructor */
	public MbBaust() {
	}

	/** minimal constructor */
	public MbBaust(MbBaustId id, MMetastatus mMetastatus, MbSchicht mbSchicht,
			SysImport sysImport, NmbNotiz nmbNotiz, NZielobjekt nZielobjekt,
			String nr, Byte auditrelevantJn, Integer metaVers, String guid,
			Date timestamp, Integer usn) {
		this.id = id;
		this.mMetastatus = mMetastatus;
		this.mbSchicht = mbSchicht;
		this.sysImport = sysImport;
		this.nmbNotiz = nmbNotiz;
		this.nZielobjekt = nZielobjekt;
		this.nr = nr;
		this.auditrelevantJn = auditrelevantJn;
		this.metaVers = metaVers;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.usn = usn;
	}

	/** full constructor */
	public MbBaust(MbBaustId id, MsCmState msCmState, MbBaust mbBaust,
			MMetastatus mMetastatus, MbSchicht mbSchicht, MMetatyp mMetatyp,
			SysImport sysImport, NmbNotiz nmbNotiz, NZielobjekt nZielobjekt,
			Integer nrNum, String nr, String link, Byte auditrelevantJn,
			Integer metaVers, Integer obsoletVers, Date loeschDatum,
			String guid, Date timestamp, Integer usn, String erfasstDurch,
			String geloeschtDurch, Short impNeu, String guidOrg,
			Date changedOn, String changedBy, Date cmTimestamp,
			String cmUsername, Integer cmImpId, Integer cmVerId1,
			Short cmVerId2, Set modZobjBsts, Set mbBausts) {
		this.id = id;
		this.msCmState = msCmState;
		this.mbBaust = mbBaust;
		this.mMetastatus = mMetastatus;
		this.mbSchicht = mbSchicht;
		this.mMetatyp = mMetatyp;
		this.sysImport = sysImport;
		this.nmbNotiz = nmbNotiz;
		this.nZielobjekt = nZielobjekt;
		this.nrNum = nrNum;
		this.nr = nr;
		this.link = link;
		this.auditrelevantJn = auditrelevantJn;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.loeschDatum = (loeschDatum != null) ? (Date)loeschDatum.clone() : null;
		this.guid = guid;
		this.timestamp = (timestamp != null) ? (Date)timestamp.clone() : null;
		this.usn = usn;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.changedOn = (changedOn != null) ? (Date)changedOn.clone() : null;
		this.changedBy = changedBy;
		this.cmTimestamp = (cmTimestamp != null) ? (Date)cmTimestamp.clone(): null;
		this.cmUsername = cmUsername;
		this.cmImpId = cmImpId;
		this.cmVerId1 = cmVerId1;
		this.cmVerId2 = cmVerId2;
		this.modZobjBsts = modZobjBsts;
		this.mbBausts = mbBausts;
	}

	// Property accessors

	public MbBaustId getId() {
		return this.id;
	}

	public void setId(MbBaustId id) {
		this.id = id;
	}

	public MsCmState getMsCmState() {
		return this.msCmState;
	}

	public void setMsCmState(MsCmState msCmState) {
		this.msCmState = msCmState;
	}

	public MbBaust getMbBaust() {
		return this.mbBaust;
	}

	public void setMbBaust(MbBaust mbBaust) {
		this.mbBaust = mbBaust;
	}

	public MMetastatus getMMetastatus() {
		return this.mMetastatus;
	}

	public void setMMetastatus(MMetastatus mMetastatus) {
		this.mMetastatus = mMetastatus;
	}

	public MbSchicht getMbSchicht() {
		return this.mbSchicht;
	}

	public void setMbSchicht(MbSchicht mbSchicht) {
		this.mbSchicht = mbSchicht;
	}

	public MMetatyp getMMetatyp() {
		return this.mMetatyp;
	}

	public void setMMetatyp(MMetatyp mMetatyp) {
		this.mMetatyp = mMetatyp;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public NmbNotiz getNmbNotiz() {
		return this.nmbNotiz;
	}

	public void setNmbNotiz(NmbNotiz nmbNotiz) {
		this.nmbNotiz = nmbNotiz;
	}

	public NZielobjekt getNZielobjekt() {
		return this.nZielobjekt;
	}

	public void setNZielobjekt(NZielobjekt nZielobjekt) {
		this.nZielobjekt = nZielobjekt;
	}

	public Integer getNrNum() {
		return this.nrNum;
	}

	public void setNrNum(Integer nrNum) {
		this.nrNum = nrNum;
	}

	public String getNr() {
		return this.nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Byte getAuditrelevantJn() {
		return this.auditrelevantJn;
	}

	public void setAuditrelevantJn(Byte auditrelevantJn) {
		this.auditrelevantJn = auditrelevantJn;
	}

	public Integer getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(Integer metaVers) {
		this.metaVers = metaVers;
	}

	public Integer getObsoletVers() {
		return this.obsoletVers;
	}

	public void setObsoletVers(Integer obsoletVers) {
		this.obsoletVers = obsoletVers;
	}

	public Date getLoeschDatum() {
	        return (this.loeschDatum != null) ? (Date)this.loeschDatum.clone() : null;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = (loeschDatum != null) ? (Date)loeschDatum.clone() : null;
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

	public Integer getUsn() {
		return this.usn;
	}

	public void setUsn(Integer usn) {
		this.usn = usn;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
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

	public Date getCmTimestamp() {
		return (this.timestamp != null) ? (Date)this.cmTimestamp.clone() : null;
	}

	public void setCmTimestamp(Date cmTimestamp) {
		this.cmTimestamp = (cmTimestamp != null) ? (Date)cmTimestamp.clone() : null;
	}

	public String getCmUsername() {
		return this.cmUsername;
	}

	public void setCmUsername(String cmUsername) {
		this.cmUsername = cmUsername;
	}

	public Integer getCmImpId() {
		return this.cmImpId;
	}

	public void setCmImpId(Integer cmImpId) {
		this.cmImpId = cmImpId;
	}

	public Integer getCmVerId1() {
		return this.cmVerId1;
	}

	public void setCmVerId1(Integer cmVerId1) {
		this.cmVerId1 = cmVerId1;
	}

	public Short getCmVerId2() {
		return this.cmVerId2;
	}

	public void setCmVerId2(Short cmVerId2) {
		this.cmVerId2 = cmVerId2;
	}

	public Set getModZobjBsts() {
		return this.modZobjBsts;
	}

	public void setModZobjBsts(Set modZobjBsts) {
		this.modZobjBsts = modZobjBsts;
	}

	public Set getMbBausts() {
		return this.mbBausts;
	}

	public void setMbBausts(Set mbBausts) {
		this.mbBausts = mbBausts;
	}

}