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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.glassfish.common.nodes.actions;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 * @author Peter Williams
 */
public class UndeployModuleAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        RequestProcessor undeployer = new RequestProcessor("gf-undeployer");
        
        for(Node node : nodes) {
            UndeployModuleCookie uCookie = node.getCookie(UndeployModuleCookie.class);

            if(uCookie != null) {
                final Future<ResultString> result = uCookie.undeploy();
                Node pNode = node.getParentNode();
                final Node gpNode = null != pNode ? pNode.getParentNode() : null;
                final Node fnode = node;

                undeployer.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.get(ServerUtilities.ACTION_TIMEOUT, ServerUtilities.ACTION_TIMEOUT_UNIT);
                        } catch(TimeoutException ex) {
                            Logger.getLogger("glassfish").log(Level.WARNING, "Undeploy action timed out for " + fnode.getDisplayName());
                        } catch (InterruptedException ie) {
                            // we can ignore this
                        }catch(Exception ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                        if(gpNode != null) {
                            Node[] nodes = gpNode.getChildren().getNodes();
                            for(Node node : nodes) {
                                RefreshModulesCookie cookie = node.getCookie(RefreshModulesCookie.class);
                                if(cookie != null) {
                                    cookie.refresh(null, fnode.getDisplayName());
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            UndeployModuleCookie cookie = node.getCookie(UndeployModuleCookie.class);
            if(cookie == null || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(UndeployModuleAction.class, "LBL_UndeployAction");
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
