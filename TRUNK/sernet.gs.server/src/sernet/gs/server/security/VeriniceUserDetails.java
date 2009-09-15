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
package sernet.gs.server.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

@SuppressWarnings("serial")
public class VeriniceUserDetails implements UserDetails {

	public VeriniceUserDetails(String user, String pass) {
		super();
		this.user = user;
		this.pass = pass;
	}

	private String user;
	private String pass;
	private List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
	
	public GrantedAuthority[] getAuthorities() {
		return (GrantedAuthority[]) roles.toArray(new GrantedAuthority[roles.size()]);
	}

	public String getPassword() {
		return pass;
	}

	public String getUsername() {
		return user;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public void addRole(String role) {
		roles.add(new GrantedAuthorityImpl(role));
	}
	
}
