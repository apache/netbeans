/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.rest.nodes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.WsdlRetriever;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class TestResourceUriAction extends NodeAction  {

    public String getName() {
        return NbBundle.getMessage(TestResourceUriAction.class, "LBL_TestRestServicesUri");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        ResourceUriProvider resourceUriProvider = activatedNodes[0].getLookup().lookup(ResourceUriProvider.class);
        if (resourceUriProvider == null) {
            return false;
        } else {
            String resourceUri = resourceUriProvider.getResourceUri();
            if (resourceUri == null || resourceUri.length() == 0) {
                return false;
            }
        } 
        Project prj = activatedNodes[0].getLookup().lookup(Project.class);
        if (prj == null || prj.getLookup().lookup(J2eeModuleProvider.class) == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        String uri = activatedNodes[0].getLookup().lookup(ResourceUriProvider.class).getResourceUri();
        if (!uri.startsWith("/")) {          //NOI18N
            uri = "/"+uri;                   //NOI18N
        }
        String resourceURL = getResourceURL(activatedNodes[0].getLookup().lookup(Project.class), uri);
        try {
            URL url = new URL(resourceURL);
            if (url != null) {
                boolean connectionOK = false;
                try {
                    URLConnection connection = url.openConnection();
                    if (connection instanceof HttpURLConnection) {
                        HttpURLConnection httpConnection = (HttpURLConnection) connection;
                        try {
                            httpConnection.setRequestMethod("GET"); //NOI18N
                            httpConnection.connect();
                            int responseCode = httpConnection.getResponseCode();
                                                        
                            // test if response hasn't been redirected (response code 302)
                            if (HttpURLConnection.HTTP_MOVED_TEMP == responseCode) {
                                // get new location
                                String location = httpConnection.getHeaderField("Location"); //NOI18N
                                if (location != null) {
                                    httpConnection.disconnect();
                                    url = new URL(location);
                                    connection = url.openConnection();
                                    if (connection instanceof HttpURLConnection) {
                                        // test for secured https connection
                                        if (connection instanceof HttpsURLConnection) {
                                            SSLSocketFactory sf = getSSLSocketFactory();
                                            ((HttpsURLConnection)connection).setSSLSocketFactory(sf);
                                            ((HttpsURLConnection)connection).setHostnameVerifier(new HostnameVerifier() {
                                                @Override
                                                public boolean verify(String string, SSLSession sSLSession) {
                                                    // accept all hosts
                                                    return true;
                                                }
                                            });
                                        }
                                        
                                        ((HttpURLConnection)connection).setRequestMethod("GET"); //NOI18N
                                        connection.connect();
                                        responseCode = ((HttpURLConnection)connection).getResponseCode();
                                    }
                                }
                            }
                            
                            if (HttpURLConnection.HTTP_OK == responseCode
                                    || HttpURLConnection.HTTP_BAD_METHOD == responseCode) {
                                connectionOK = true;
                            }
                        } catch (java.io.IOException ex) {
                            Logger.getLogger(TestResourceUriAction.class.getName()).log(Level.INFO, "URLConnection problem", ex); //NOI18N
                        } finally {
                            httpConnection.disconnect();
                        }
                        // logging usage of action
                        Object[] params = new Object[2];
                        params[0] = LogUtils.WS_STACK_JAXRS;
                        params[1] = "TEST RESOURCE"; // NOI18N
                        LogUtils.logWsAction(params);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(TestResourceUriAction.class.getName()).log(Level.INFO, "URLConnection problem", ex); //NOI18N
                }
                if (connectionOK) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } else {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                NbBundle.getMessage(TestResourceUriAction.class, "MSG_UNABLE_TO_OPEN_TEST_PAGE", url),
                                NotifyDescriptor.WARNING_MESSAGE));
                }
            }
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestResourceUriAction.class,
                    "TXT_ResourceUrl", resourceURL));   //NOI18N
        }
    }

    private String getResourceURL(Project project, String uri) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null) {
            Logger.getLogger(TestResourceUriAction.class.getName()).log(Level.INFO, "Can not detect target J2EE server"); //NOI18N
            return "";
        }
        // getting port and host name
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        String portNumber = "8080"; //NOI18N
        String hostName = "localhost"; //NOI18N
        try {
            ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
            if (instanceDescriptor != null) {
                int port = instanceDescriptor.getHttpPort();
                portNumber = port == 0 ? "8080" : String.valueOf(port); //NOI18N
                String hstName = instanceDescriptor.getHostname();
                if (hstName != null) {
                    hostName = hstName;
                }
            }
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Removed ServerInstance", ex); //NOI18N
        }

        String contextRoot = null;
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();

        if (J2eeModule.Type.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if (contextRoot != null && contextRoot.startsWith("/")) { //NOI18N
                //NOI18N
                contextRoot = contextRoot.substring(1);
            }
        }

        String applicationPath = "resources"; //NOI18N
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            applicationPath = restSupport.getApplicationPath();
        }
        StringBuilder builder = new StringBuilder("http://");   // NOI18N
        builder.append(hostName);
        builder.append(':');
        builder.append(portNumber);
        builder.append('/');
        if ( contextRoot != null && contextRoot.length()>0  ){
            builder.append( contextRoot );
        }
        builder.append( '/');
        builder.append( applicationPath );
        // Fix for BZ#200724 - Testing RESTful WebService fails when clicking "Test Resource Uri" from witin NetBeans 
        if ( uri.startsWith("/") && applicationPath.length() ==0 ){             // NOI18N
            builder.append( uri.substring( 1 ) );
        }
        else {
            builder.append( uri );
        }

        return builder.toString();
    }

    @Override
    public boolean asynchronous() {
        return true;
    }
    
    // Install the trust manager
    private SSLSocketFactory getSSLSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
                    // ask user to accept the unknown certificate
                    if (certs!=null) {
                        for (int i=0; i<certs.length; i++) {
                            DialogDescriptor desc = new DialogDescriptor(Retriever.getCertificationPanel(certs[i]),
                                    NbBundle.getMessage(WsdlRetriever.class,"TTL_CertifiedWebSite"),
                                    true,
                                    DialogDescriptor.YES_NO_OPTION,
                                    DialogDescriptor.YES_OPTION,
                                    null);
                            DialogDisplayer.getDefault().notify(desc);
                            if (!DialogDescriptor.YES_OPTION.equals(desc.getValue())) {
                                throw new CertificateException(
                                        NbBundle.getMessage(WsdlRetriever.class,"ERR_NotTrustedCertificate"));
                            }
                        } // end for
                    }
                }
            }
        };
        
        
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL"); //NOI18N
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (java.security.GeneralSecurityException e) {
            Logger.getLogger(TestResourceUriAction.class.getName()).log(Level.WARNING, "Can not init SSL Context", e);
            return null;
        }
    
    }

}

