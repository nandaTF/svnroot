/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 *     Benjamin Wei�enfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;

import sernet.verinice.service.auth.KerberosTicketService;
import static sernet.verinice.service.auth.KerberosTicketService.*;

public class KerberosExecuter extends AbstractExecuter {

    private static final Logger LOG = Logger.getLogger(KerberosExecuter.class);

    private String clientToken;

    private boolean isClientTokenInit;

    private KerberosTicketService kerberosTicketService;

    public KerberosExecuter() {
        super();
    }

    public void init() {
        kerberosTicketService = SpringClientPlugin.getDefault().getKerberosTicketService();
        super.init();
    }

   

    @Override
    protected void validateResponse(HttpInvokerClientConfiguration config, PostMethod postMethod) throws IOException {

        if (postMethod.getStatusCode() == 200) {
            return;
        }

        if (postMethod.getStatusCode() == 301) {
            return;
        }

        if (postMethod.getStatusCode() == 401) {

            if (isClientTokenInit) {
                updateClientToken(postMethod);
            } else {
                initClientToken();
            }

            LOG.info("client token: " + clientToken);
        } else {
            super.validateResponse(config, postMethod);
        }
    }

    private void updateClientToken(PostMethod postMethod) {

        Header negotiateParam = postMethod.getResponseHeader(WWW_AUTHENTICATE);

        // server does not want to talk with us
        if (negotiateParam == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("response header " + WWW_AUTHENTICATE + " is not set");
            return;
        }

        // update client token
        String negotiate = postMethod.getResponseHeader(WWW_AUTHENTICATE).getValue();
        negotiate = negotiate.substring(SECURITY_PACKAGE.length() + 1);
        clientToken = kerberosTicketService.updateClientToken(negotiate);
    }

    /**
     * Read the initial token.
     */
    private void initClientToken() {
        clientToken = kerberosTicketService.getClientToken();
        isClientTokenInit = true;
    }

    @Override
    protected void executePostMethod(HttpInvokerClientConfiguration config, HttpClient httpClient, PostMethod postMethod) throws IOException {
        postMethod.addRequestHeader(KerberosTicketService.HEADER_NAME_AUTHORIZATION, clientToken);
        super.executePostMethod(config, httpClient, postMethod);
    }
}
