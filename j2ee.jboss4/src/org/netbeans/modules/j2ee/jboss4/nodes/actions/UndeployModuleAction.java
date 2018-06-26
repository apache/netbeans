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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleAction extends NodeAction {

    private static final RequestProcessor PROCESSOR = new RequestProcessor ("JBoss undeploy UI", 1); // NOI18N

    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1) )
            return;
        
        for (int i = 0; i < nodes.length; i++) {
            UndeployModuleCookie uCookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (uCookie != null) {
                final Task t = uCookie.undeploy();
                final Node node = nodes[i].getParentNode();
                
                PROCESSOR.post(new Runnable() {
                    public void run() {
                        t.waitFinished();
                        if(node != null) {
                            Node apps = node.getParentNode();
                            if (apps != null) {
                                Enumeration appTypes = apps.getChildren().nodes();
                                while (appTypes.hasMoreElements()) {
                                    Node appType = (Node)appTypes.nextElement();
                                    RefreshModulesCookie cookie = (RefreshModulesCookie)
                                            appType.getCookie(RefreshModulesCookie.class);
                                    if (cookie != null) {
                                        cookie.refresh();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
    
    protected boolean enable(Node[] nodes) {
        UndeployModuleCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (cookie == null || cookie.isRunning())
                return false;
        }
        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(UndeployModuleAction.class, "LBL_UndeployAction");
    }
    
    protected boolean asynchronous() { return false; }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
