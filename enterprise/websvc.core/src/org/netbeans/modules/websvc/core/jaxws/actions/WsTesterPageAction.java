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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class WsTesterPageAction extends NodeAction {
    public String getName() {
        return NbBundle.getMessage(WsTesterPageAction.class, "LBL_TesterPageAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) {
        JaxWsTesterCookie cookie =
           activatedNodes[0].getLookup().lookup(JaxWsTesterCookie.class);
        String wsdlURL = cookie.getTesterPageURL();
        try {
            final URL url = new URL(wsdlURL);
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
                            // for secured web services the response code is 405: we should allow to show the response
                            if (HttpURLConnection.HTTP_OK == responseCode
                                    || HttpURLConnection.HTTP_BAD_METHOD == responseCode) {
                                connectionOK = true;
                            }

                        } catch (java.io.IOException ex) {
                            Logger.getLogger(WsTesterPageAction.class.getName()).log(Level.INFO, "URLConnection problem", ex); //NOI18N
                        } finally {
                            httpConnection.disconnect();
                        }
                        
                        // logging usage of action
                        Object[] params = new Object[2];
                        params[0] = LogUtils.WS_STACK_JAXWS;
                        params[1] = "TEST"; // NOI18N
                        LogUtils.logWsAction(params);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(WsTesterPageAction.class.getName()).log(Level.INFO, "URLConnection problem", ex); //NOI18N
                }
                if (connectionOK) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } else {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                NbBundle.getMessage(WsTesterPageAction.class, "MSG_UNABLE_TO_OPEN_TEST_PAGE", url),
                                NotifyDescriptor.WARNING_MESSAGE));
                }
            }
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WsTesterPageAction.class,
                    "TXT_TesterPageUrl", wsdlURL));   //NOI18N
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }
        FileObject srcRoot = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
        if (srcRoot != null) {
            Project project = FileOwnerQuery.getOwner(srcRoot);
            if (project != null) {
                return isTesterPageSupported(project);
            }
        }
        return false;
    }

    private boolean isTesterPageSupported(Project project) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            String serverInstanceId = provider.getServerInstanceID();
            if (serverInstanceId != null) {
                try {
                    J2eePlatform j2eePlatform =
                            Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
                    WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(j2eePlatform);
                    return wsStack != null && wsStack.isFeatureSupported(JaxWs.Feature.TESTER_PAGE);
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
                }
            }
        }
        return false;
    }


}
