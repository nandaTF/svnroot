package sernet.verinice.server.ldap;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoMock implements IPersonDao {

	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter) {
		ArrayList<PersonInfo> personList = new ArrayList<PersonInfo>();
		
		PersonIso person = new PersonIso();
		person.setSurname("Müller");
		personList.add(new PersonInfo(person, "am", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Mayer");
		personList.add(new PersonInfo(person, "tm", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Schmidt");
		personList.add(new PersonInfo(person, "ms", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Peters");
		personList.add(new PersonInfo(person, "gp", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Wagner");
		personList.add(new PersonInfo(person, "rw", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Rudolph");
		personList.add(new PersonInfo(person, "mr", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Koch");
		personList.add(new PersonInfo(person, "tk", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Richard");
		personList.add(new PersonInfo(person, "sr", null, null, null));
		
		person = new PersonIso();
		person.setSurname("Schuster");
		personList.add(new PersonInfo(person, "ds", null, null, null));
		
		return personList;		
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ldap.IPersonDao#getPersonList(sernet.verinice.interfaces.ldap.PersonParameter, boolean)
     */
    @Override
    public List<PersonInfo> getPersonList(PersonParameter parameter, boolean importToITGS) {
        return getPersonList(parameter);
    }

}
