/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

@SuppressWarnings("serial")
public class LdapUserToken extends AbstractAuthenticationToken {
    
    private Authentication auth;
    private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    /**
     * Construct a new LdapAuthenticationToken, using an existing Authentication
     * object and granting all users a default authority.
     * 
     * @param auth
     * @param defaultAuthority
     */
    public LdapUserToken(Authentication auth, GrantedAuthority defaultAuthority) {
        super(join(auth.getAuthorities(), defaultAuthority));
        this.auth = auth;
        if (auth.getAuthorities() != null) {
            this.authorities.addAll((Collection<? extends GrantedAuthority>) Arrays.asList(auth.getAuthorities()));
        }
        if (defaultAuthority != null) {
            this.authorities.add(defaultAuthority);
        }
        super.setAuthenticated(true);
    }
    
    public LdapUserToken(Collection<? extends GrantedAuthority> authorities){
        super(authorities);
    }
    
    private static List<? extends GrantedAuthority> join(Collection<? extends GrantedAuthority> collection, GrantedAuthority object){
        ArrayList<GrantedAuthority> list = new ArrayList<GrantedAuthority>(collection.size() + 1);
        list.addAll(collection);
        list.add(object);
        return list;
    }

    /**
     * Construct a new LdapAuthenticationToken, using an existing Authentication
     * object and granting all users a default authority.
     * 
     * @param auth
     * @param defaultAuthority
     */
    public LdapUserToken(Authentication auth, String defaultAuthority) {
        this(auth, new GrantedAuthorityImpl(defaultAuthority));
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.authorities;//.toArray(new GrantedAuthority[0]);
    }

    public void addAuthority(GrantedAuthority authority) {
        this.authorities.add(authority);
    }
    
    public void addAuthority(String auth) {
        this.authorities.add(new GrantedAuthorityImpl(auth));
    }

    public Object getCredentials() {
        return auth.getCredentials();
    }

    public Object getPrincipal() {
        return auth.getPrincipal();
    }

   
}
