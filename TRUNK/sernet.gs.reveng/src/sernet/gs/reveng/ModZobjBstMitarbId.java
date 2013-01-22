package sernet.gs.reveng;

/**
 * ModZobjBstMitarbId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMitarbId implements java.io.Serializable {

	// Fields

	private Integer zobImpId;
	private Integer zobId;
	private Integer bauImpId;
	private Integer bauId;
	private Integer zobIdMit;

	// Constructors

	/** default constructor */
	public ModZobjBstMitarbId() {
	}

	/** full constructor */
	public ModZobjBstMitarbId(Integer zobImpId, Integer zobId,
			Integer bauImpId, Integer bauId, Integer zobIdMit) {
		this.zobImpId = zobImpId;
		this.zobId = zobId;
		this.bauImpId = bauImpId;
		this.bauId = bauId;
		this.zobIdMit = zobIdMit;
	}

	// Property accessors

	public Integer getZobImpId() {
		return this.zobImpId;
	}

	public void setZobImpId(Integer zobImpId) {
		this.zobImpId = zobImpId;
	}

	public Integer getZobId() {
		return this.zobId;
	}

	public void setZobId(Integer zobId) {
		this.zobId = zobId;
	}

	public Integer getBauImpId() {
		return this.bauImpId;
	}

	public void setBauImpId(Integer bauImpId) {
		this.bauImpId = bauImpId;
	}

	public Integer getBauId() {
		return this.bauId;
	}

	public void setBauId(Integer bauId) {
		this.bauId = bauId;
	}

	public Integer getZobIdMit() {
		return this.zobIdMit;
	}

	public void setZobIdMit(Integer zobIdMit) {
		this.zobIdMit = zobIdMit;
	}

	public boolean equals(Object other) {
		if ((this == other)){
			return true;
		}
		if ((other == null)){
			return false;
		}
		if (!(other instanceof ModZobjBstMitarbId)){
			return false;
		}
		ModZobjBstMitarbId castOther = (ModZobjBstMitarbId) other;

		return ((this.getZobImpId() == castOther.getZobImpId()) || (this
				.getZobImpId() != null
				&& castOther.getZobImpId() != null && this.getZobImpId()
				.equals(castOther.getZobImpId())))
				&& ((this.getZobId() == castOther.getZobId()) || (this
						.getZobId() != null
						&& castOther.getZobId() != null && this.getZobId()
						.equals(castOther.getZobId())))
				&& ((this.getBauImpId() == castOther.getBauImpId()) || (this
						.getBauImpId() != null
						&& castOther.getBauImpId() != null && this
						.getBauImpId().equals(castOther.getBauImpId())))
				&& ((this.getBauId() == castOther.getBauId()) || (this
						.getBauId() != null
						&& castOther.getBauId() != null && this.getBauId()
						.equals(castOther.getBauId())))
				&& ((this.getZobIdMit() == castOther.getZobIdMit()) || (this
						.getZobIdMit() != null
						&& castOther.getZobIdMit() != null && this
						.getZobIdMit().equals(castOther.getZobIdMit())));
	}

	public int hashCode() {
		int result = 17;

		final int prime_factor = 37;
		
		result = prime_factor * result
				+ (getZobImpId() == null ? 0 : this.getZobImpId().hashCode());
		result = prime_factor * result
				+ (getZobId() == null ? 0 : this.getZobId().hashCode());
		result = prime_factor * result
				+ (getBauImpId() == null ? 0 : this.getBauImpId().hashCode());
		result = prime_factor * result
				+ (getBauId() == null ? 0 : this.getBauId().hashCode());
		result = prime_factor * result
				+ (getZobIdMit() == null ? 0 : this.getZobIdMit().hashCode());
		return result;
	}

}