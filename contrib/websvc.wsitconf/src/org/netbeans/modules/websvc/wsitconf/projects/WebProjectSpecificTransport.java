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

import java.io.FileOutputStream;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.spi.ProjectSpecificTransport;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 *
 * @author snajper
 */
public class WebProjectSpecificTransport extends ProjectSpecificTransport {

    private WebWsitProvider wsitProvider;

    private static final String TCP_GF_NONJSR109 = "com.sun.xml.ws.transport.tcp.server.glassfish.WSStartupServlet";   //NOI18N
    private static final String TCP_TOMCAT =       "com.sun.xml.ws.transport.http.servlet.WSServletContextListener";   //NOI18N
    
    public WebProjectSpecificTransport(Project p, WebWsitProvider provider) {
        this.project = p;
        this.wsitProvider = provider;
    }

    @Override
    public void setTCPUrl(String name, String serviceName, String implClass, boolean tomcat) {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            try {
                WebApp wApp = DDProvider.getDefault ().getDDRoot(wm.getDeploymentDescriptor());
                if (wsitProvider.isJsr109Project()) {
                    Servlet servlet = Util.getServlet(wApp, implClass);
                    if (servlet == null) {      //NOI18N
                        try {
                            servlet = (Servlet)wApp.addBean("Servlet", new String[]{WebWsitProvider.SERVLET_NAME,"ServletClass"},    //NOI18N
                                    new Object[]{name,implClass}, WebWsitProvider.SERVLET_NAME);
                            servlet.setLoadOnStartup(new java.math.BigInteger("1"));                            //NOI18N
                            if (serviceName == null) {
                                serviceName = implClass.substring(implClass.lastIndexOf('.')+1) + JavaWsdlMapper.SERVICE;
                            }
                            wApp.addBean("ServletMapping", new String[]{WebWsitProvider.SERVLET_NAME,"UrlPattern"}, //NOI18N
                                    new Object[]{name, "/" + serviceName}, WebWsitProvider.SERVLET_NAME);
                            wApp.write(wm.getDeploymentDescriptor());
                        } catch (NameAlreadyUsedException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                    }
                } else {
                    String listener = tomcat ? TCP_TOMCAT : TCP_GF_NONJSR109;
                    if (!isTcpListener(wApp, listener)) {
                        try {
                            wApp.addBean("Listener", new String[]{"ListenerClass"},  //NOI18N
                                    new Object[]{listener}, "ListenerClass");        //NOI18N
                            wApp.write(wm.getDeploymentDescriptor());
                        } catch (NameAlreadyUsedException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (tomcat) {
                        addConnector(project);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean isTcpListener(WebApp wa, String listener) {
        Listener[] listeners = wa.getListener();
        for (Listener l : listeners) {
            if (listener.equals(l.getListenerClass())) {
                return true;
            }
        }
        return false;
    }

    private static final String PROP_CONNECTOR = "Connector"; // NOI18N
    private static final String CONN_PROTOCOL = "com.sun.xml.ws.transport.tcp.server.tomcat.grizzly10.WSTCPGrizzly10ProtocolHandler";
    private static final String CONN_PORT = "5773";
    private static final String CONN_TIMEOUT = "20000";
    private static final String CONN_REDIRECT_PORT = "8080";

    /**
     * Make some Tomcat specific changes in server.xml.
     */
    public static void addConnector(Project p) {
        FileObject serverXml = ServerUtils.getServerXml(p);
        if (serverXml != null) {
            try {
                XMLDataObject dobj = (XMLDataObject)DataObject.find(serverXml);
                org.w3c.dom.Document doc = dobj.getDocument();
                org.w3c.dom.Element root = doc.getDocumentElement();
                org.w3c.dom.NodeList list = root.getElementsByTagName("Service"); //NOI18N
                int size=list.getLength();
                if (size>0) {
                    org.w3c.dom.Element service=(org.w3c.dom.Element)list.item(0);
                    org.w3c.dom.NodeList cons = service.getElementsByTagName(PROP_CONNECTOR);
                    for (int i=0;i<cons.getLength();i++) {
                        org.w3c.dom.Element con=(org.w3c.dom.Element)cons.item(i);
                        String protocol = con.getAttribute("protocol");
                        if (CONN_PROTOCOL.equals(protocol))return;
                    }

                    Element e = doc.createElement(PROP_CONNECTOR);
                    e.setAttribute("port", CONN_PORT);
                    e.setAttribute("connectionTimeout", CONN_TIMEOUT);
                    e.setAttribute("protocol", CONN_PROTOCOL);
                    e.setAttribute("redirectHttpPort", CONN_REDIRECT_PORT);
                    e.setAttribute("redirectPort", CONN_REDIRECT_PORT);

                    service.appendChild(e);

                    XMLUtil.write(doc, new FileOutputStream(FileUtil.toFile(serverXml)), "UTF-8");
                }
            } catch(org.xml.sax.SAXException ex){
                Exceptions.printStackTrace(ex);
            } catch(org.openide.loaders.DataObjectNotFoundException ex){
                Exceptions.printStackTrace(ex);
            } catch(java.io.IOException ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
