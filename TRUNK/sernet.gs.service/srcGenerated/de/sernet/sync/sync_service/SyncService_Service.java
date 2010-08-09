
/*
 * 
 */

package de.sernet.sync.sync_service;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.9
 * Mon Aug 09 16:57:31 CEST 2010
 * Generated source version: 2.2.9
 * 
 */


@WebServiceClient(name = "sync-service", 
                  wsdlLocation = "file:src/sernet/verinice/service/sync/sync-service.wsdl",
                  targetNamespace = "http://www.sernet.de/sync/sync-service") 
public class SyncService_Service extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://www.sernet.de/sync/sync-service", "sync-service");
    public final static QName SyncService = new QName("http://www.sernet.de/sync/sync-service", "sync-service");
    static {
        URL url = null;
        try {
            url = new URL("file:src/sernet/verinice/service/sync/sync-service.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:src/sernet/verinice/service/sync/sync-service.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public SyncService_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public SyncService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SyncService_Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     * 
     * @return
     *     returns SyncService
     */
    @WebEndpoint(name = "sync-service")
    public SyncService getSyncService() {
        return super.getPort(SyncService, SyncService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SyncService
     */
    @WebEndpoint(name = "sync-service")
    public SyncService getSyncService(WebServiceFeature... features) {
        return super.getPort(SyncService, SyncService.class, features);
    }

}
