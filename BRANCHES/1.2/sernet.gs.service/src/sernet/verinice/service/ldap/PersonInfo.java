package sernet.verinice.service.ldap;

import java.io.Serializable;

import sernet.verinice.model.iso27k.PersonIso;

public class PersonInfo implements Serializable{
	
	private PersonIso person;
	private String loginName;
	
	public PersonInfo(PersonIso person, String loginName) {
		super();
		this.person = person;
		this.loginName = loginName;
	}

	public PersonIso getPerson() {
		return person;
	}

	public void setPerson(PersonIso person) {
		this.person = person;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonInfo other = (PersonInfo) obj;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		return true;
	}
	
	
	
	
	
}
