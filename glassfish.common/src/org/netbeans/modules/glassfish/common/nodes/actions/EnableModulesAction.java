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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * @author vince kraemer
 *
 * Based on UndeployModuleAction
 * @author Michal Mocnak
 * @author Peter Williams
 */
public class EnableModulesAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        targets.clear();
        for (Node n : nodes) {
            targets.add(n.getDisplayName());
        }
        String aDup = getDup(targets);
        if (null != aDup) {
            // open dialog
            NotifyDescriptor m = new NotifyDescriptor.Message(NbBundle.getMessage(EnableModulesAction.class, "ERR_HAS_DUPS", aDup),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(m);
            return;
        }

        RequestProcessor enabler = new RequestProcessor("gf-enable-module");
        
        for(Node node : nodes) {
            EnableModulesCookie uCookie = node.getCookie(EnableModulesCookie.class);

            if(uCookie != null) {
                final Future<ResultString> result = uCookie.enableModule();
                final Node pNode = node.getParentNode().getParentNode();
                final Node fnode = node;

                enabler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.get(ServerUtilities.ACTION_TIMEOUT, ServerUtilities.ACTION_TIMEOUT_UNIT);
                        } catch(TimeoutException ex) {
                            Logger.getLogger("glassfish").log(Level.WARNING, "Enable action timed out for " + fnode.getDisplayName());
                        } catch (InterruptedException ie) {
                            // we can ignore this
                        }catch(Exception ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                        if(pNode != null) {
                            Node[] nodes = pNode.getChildren().getNodes();
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

    private List<String> targets = new ArrayList<String>();

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            EnableModulesCookie cookie = node.getCookie(EnableModulesCookie.class);
            if(cookie == null || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EnableModulesAction.class, "LBL_EnableAction");
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    static String getDup(List<String> targets) {
        Map<String,String> uniqTargets = new HashMap<String,String>();
        if (null == targets) {
            return null;
        }
        for (String target : targets) {
            int colon = target.indexOf(":");
            if (-1 == colon) {
                colon = target.length();
            }
            String shortName = target.substring(0,colon);
            if (uniqTargets.containsKey(shortName)) {
                return shortName;
            } else {
                uniqTargets.put(shortName,shortName);
            }
        }
        return null;
    }
}
