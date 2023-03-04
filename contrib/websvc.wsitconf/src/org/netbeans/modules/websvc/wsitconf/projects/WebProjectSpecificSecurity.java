/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.wsitconf.projects;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.wsitconf.spi.ProjectSpecificSecurity;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class WebProjectSpecificSecurity extends ProjectSpecificSecurity {

    private static final Logger logger = Logger.getLogger(WebProjectSpecificSecurity.class.getName());

    public boolean unsetSSLAttributes(final WSDLComponent c) {
        SecurityConstraint sc = getSecurityConstraint(c);
        if (sc != null) {
            try {
                FileObject webXmlFO = getDDFO(c);
                if (webXmlFO == null) return false; // currently we only know what to do if it's WebProject and DD exists
                WebApp webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
                if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                    webXmlDD.removeSecurityConstraint(sc);
                    webXmlDD.write(webXmlFO);
                }
                return true;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return false;
    }

    public boolean setSSLAttributes(final WSDLComponent c) {
        if (getSecurityConstraint(c) == null) {
            FileObject webXmlFO = getDDFO(c);
            if (webXmlFO == null) return false; // currently we only know what to do if it's WebProject and DD exists
            try {
                WebApp webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
                if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                    SecurityConstraint sc = (SecurityConstraint) webXmlDD.createBean("SecurityConstraint");

                    AuthConstraint ac = (AuthConstraint) webXmlDD.createBean("AuthConstraint");
                    ac.addRoleName("EMPLOYEE");
                    sc.setAuthConstraint(ac);

                    UserDataConstraint udc = (UserDataConstraint) webXmlDD.createBean("UserDataConstraint");
                    udc.setTransportGuarantee("CONFIDENTIAL");
                    sc.setUserDataConstraint(udc);

                    String serviceName = null;

                    String urlPattern = "/";
                    boolean exit = false;
                    if (c instanceof Binding) {
                        Collection<Service> ss = c.getModel().getDefinitions().getServices();
                        for (Service s : ss) {
                            Collection<Port> pp = s.getPorts();
                            for (Port port : pp) {
                                QName qname = port.getBinding().getQName();
                                String bName = ((Binding)c).getName();
                                if (bName.equals(qname.getLocalPart())) {
                                    serviceName = s.getName();
                                    urlPattern = urlPattern.concat(serviceName + "/*");
                                    exit = true;
                                    break;
                                }
                            }
                            if (exit) break;
                        }
                    }
                    sc.setDisplayName(NbBundle.getMessage(WebProjectSpecificSecurity.class, "LBL_SECCONSTRAINT_DNAME", serviceName));
                    WebResourceCollection wrc = (WebResourceCollection)
                        webXmlDD.createBean("WebResourceCollection");
                    wrc.setHttpMethod(new String[] {"POST"});
                    wrc.setUrlPattern(new String[] {urlPattern});
                    wrc.setWebResourceName("Secure Area");
                    sc.addWebResourceCollection(wrc);

                    webXmlDD.addSecurityConstraint(sc);
                    webXmlDD.write(webXmlFO);
                    return true;
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    private static SecurityConstraint getSecurityConstraint(WSDLComponent c) {
        FileObject webXmlFO = getDDFO(c);
        if (webXmlFO != null) {
            WebApp webXmlDD = null;
            try {
                webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
            } catch (IOException ioe) {
                logger.log(Level.FINE, null, ioe); //ignore
            }

            String urlPattern = null;

            if (c instanceof Binding) {
                Collection<Service> ss = c.getModel().getDefinitions().getServices();
                for (Service s : ss) {
                    Collection<Port> pp = s.getPorts();
                    for (Port port : pp) {
                        QName qname = port.getBinding().getQName();
                        String bName = ((Binding)c).getName();
                        if (bName.equals(qname.getLocalPart())) {
                            urlPattern = s.getName();
                        }
                    }
                }
            }

            if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                SecurityConstraint[] constraints = webXmlDD.getSecurityConstraint();
                for (SecurityConstraint sc : constraints) {
                    WebResourceCollection wrc = sc.getWebResourceCollection(0);
                    if (wrc != null) {
                        String wrcUrlPattern = wrc.getUrlPattern(0);
                        if ((wrcUrlPattern != null) && wrcUrlPattern.contains(urlPattern)) {
                            return sc;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static FileObject getDDFO(WSDLComponent c) {
        WSDLModel model = c.getModel();
        FileObject fo = Util.getFOForModel(model);
        if (fo != null) {
            WebModule wm = WebModule.getWebModule(fo);
            return wm.getDeploymentDescriptor();
        }
        return null;
    }

}
