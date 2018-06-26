/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassFishState;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
public class ViewAdminConsoleAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        CommonServerSupport commonSupport
                = (CommonServerSupport)activatedNodes[0]
                .getLookup().lookup(GlassfishModule.class);
        if(commonSupport != null) {
            if (GlassFishState.isOnline(commonSupport.getInstance())) {
                try {
                    Map<String, String> ip = commonSupport.getInstanceProperties();
                    StringBuilder urlBuilder = new StringBuilder(128);
                    String port = !("false".equals(System.getProperty("glassfish.useadminport"))) ?
                        ip.get(GlassfishModule.ADMINPORT_ATTR) : ip.get(GlassfishModule.HTTPPORT_ATTR);
                    String host = ip.get(GlassfishModule.HOSTNAME_ATTR);
                    String uri = ip.get(GlassfishModule.URL_ATTR);
                    if (uri == null || !uri.contains("ee6wc")) {
                        urlBuilder.append(Utils.getHttpListenerProtocol(host, port));
                    } else {
                        urlBuilder.append("http");
                    }
                    urlBuilder.append("://"); // NOI18N
                    urlBuilder.append(ip.get(GlassfishModule.HOSTNAME_ATTR));
                    urlBuilder.append(":");
                    urlBuilder.append(port);
                    if("false".equals(System.getProperty("glassfish.useadminport"))) {
                        // url for admin gui when on http port (8080)
                        urlBuilder.append("/admin");
                    }
                    URL url = new URL(urlBuilder.toString());
                    URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
                }
            } else {
                String message = NbBundle.getMessage(ViewAdminConsoleAction.class, 
                        "MSG_ServerMustBeRunning"); // NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.DEFAULT_OPTION);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length == 1;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewAdminConsoleAction.class, "CTL_ViewAdminConsoleAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
