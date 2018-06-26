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
package org.netbeans.modules.maven.jaxws.actions;

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
import org.netbeans.modules.maven.jaxws.WSStackUtils;
import org.netbeans.modules.maven.jaxws.nodes.JaxWsNode;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class WsTesterPageAction extends NodeAction {
    @Override
    public String getName() {
        return NbBundle.getMessage(WsTesterPageAction.class, "LBL_TesterPageAction");
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        JaxWsNode wsNode = activatedNodes[0].getLookup().lookup(JaxWsNode.class);
        String wsdlURL = wsNode.getTesterPageURL();
        try {
            final URL url = new URL(wsdlURL);
            if (url!=null) {  
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        boolean connectionOK=false;
                        try {
                            URLConnection connection = url.openConnection();
                            if (connection instanceof HttpURLConnection) {
                                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                                try {
                                    httpConnection.setRequestMethod("GET"); //NOI18N
                                    httpConnection.connect();
                                    int responseCode = httpConnection.getResponseCode();
                                    // for secured web services the response code is 405: we should allow to show the response
                                    if (HttpURLConnection.HTTP_OK == responseCode || HttpURLConnection.HTTP_BAD_METHOD == responseCode)
                                        connectionOK=true;
                                } catch (java.net.ConnectException ex) {
                                    // doing nothing - display warning
                                } finally {
                                    httpConnection.disconnect();
                                }
                                // logging usage of action
                                Object[] params = new Object[2];
                                params[0] = LogUtils.WS_STACK_JAXWS;
                                params[1] = "TEST SOAP"; // NOI18N
                                LogUtils.logWsAction(params);
                            }

                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                        if (connectionOK) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } else {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(
                                        NbBundle.getMessage(WsTesterPageAction.class,"MSG_UNABLE_TO_OPEN_TEST_PAGE",url),
                                        NotifyDescriptor.WARNING_MESSAGE));
                        }
                    }
                    
                });
            }
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WsTesterPageAction.class,
                    "TXT_TesterPageUrl", wsdlURL));   //NOI18N
        }
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes==null || activatedNodes.length==0) return false;
        FileObject srcRoot = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (srcRoot!=null) {
            Project project = FileOwnerQuery.getOwner(srcRoot);
            if (project!=null) {
                return isTesterPageSupported(project);
            }
        }
        return false;
    }
    
    private boolean isTesterPageSupported(Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            String serverInstanceId = provider.getServerInstanceID();
            if (serverInstanceId != null && !WSStackUtils.DEVNULL.equals(serverInstanceId)) { //NOI18N
                //- dev null means, there's no server defined.. somehow it cannot really be null..
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
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
