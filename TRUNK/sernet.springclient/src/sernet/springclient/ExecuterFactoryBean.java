/*******************************************************************************
 * Copyright (c) 2015 verinice.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     verinice <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import sernet.verinice.service.auth.KerberosStatusService;

/**
 * Creates the HTTP Executer. The verinice client supports SSO on windows
 * machines and therefore the client must extend the HTTP with an
 * authentification token and needs a specialized HTTP executer.
 * 
 * If the kerberos plugin is available and active, this factory returns an the
 * kerberos executer.
 * 
 * @see {@linkplain KerberosExecuter}
 * @see {@linkplain CommonsExecuter}
 * 
 * @author Benjamin Wei�enfels <bw[at]sernet[dot]de>
 *
 */
public class ExecuterFactoryBean {

    public AbstractExecuter getExecuter() throws Exception {
        KerberosStatusService kerberosStatusService = SpringClientPlugin.getDefault().getKerberosStatusService();

        if (kerberosStatusService != null && kerberosStatusService.isActive()) {
            KerberosExecuter executer = new KerberosExecuter();
            executer.init();
            return executer;
        } else {
            CommonsExecuter commonsExecuter = new CommonsExecuter();
            commonsExecuter.init();
            return commonsExecuter;
        }
    }

}
