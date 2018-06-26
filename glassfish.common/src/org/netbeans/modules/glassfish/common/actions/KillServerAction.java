/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.actions;

import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.ui.WarnPanel;
import org.netbeans.modules.glassfish.common.utils.ServerUtils;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Kill running local server process.
 * <p/>
 * @author Tomas Kraus
 */
public class KillServerAction extends NodeAction {

    /**
     * Retrieve GlassFish support instance from nodes.
     * <p/>
     * @param nodes Current activated nodes, may be empty but
     *        not <code>null</code>.
     * @return First GlassFish instance found in nodes or <code>null</code>
     *         when no GlassFish instance was found.
     */
    private GlassfishModule getGlassfishModuleFromNodes(final Node[] nodes) {
        for (Node node : nodes) {
            GlassfishModule commonSupport
                    = node.getLookup().lookup(GlassfishModule.class);
            if (commonSupport != null) {
                return commonSupport;
            }
        }
        return null;
    }

    /**
     * Retrieve GlassFish instance from nodes.
     * <p/>
     * @param nodes Current activated nodes, may be empty but
     *        not <code>null</code>.
     * @return First GlassFish instance found in nodes or <code>null</code>
     *         when no GlassFish instance was found.
     */
    private GlassfishInstance getInstanceFromNodes(final Node[] nodes) {
        GlassfishModule commonSupport = getGlassfishModuleFromNodes(nodes);
        return commonSupport != null
                ? (GlassfishInstance)commonSupport.getInstance() : null;
    }

    /**
     * Get a human presentable name of kill server action.
     * <p/>
     * This will be presented as an item in a menu.
     * @return The name of kill server action.
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(
                KillServerAction.class, "KillServerAction.label");
    }

    /**
    * Perform the action based on the currently activated nodes.
    * <p/>
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    * <p/>
    * @param activatedNodes Current activated nodes, may be empty but
    *                       not <code>null</code>
    */
    @Override
    protected void performAction(final Node[] activatedNodes) {
        GlassfishModule commonSupport
                = getGlassfishModuleFromNodes(activatedNodes);
        if (commonSupport != null) {
            performActionImpl(commonSupport);
        }
    }

    /**
     * Action implementation code.
     * <p/>
     * @param commonSupport Interface implemented by common server support.
     */
    private static void performActionImpl(GlassfishModule commonSupport) {
        if (WarnPanel.gfKillWarning(commonSupport.getInstance().getName())) {
            commonSupport.killServer(null);
        }
    }

    /**
     * Test whether the action should be enabled based on the currently
     * activated nodes.
     * <p/>
     * @param activatedNodes Current activated nodes, may be empty but
     *        not <code>null</code>.
     * @return <code>true</code> to be enabled, <code>false</code> to be disabled
     */
    @Override
    protected boolean enable(final Node[] activatedNodes) {
        GlassfishInstance instance = getInstanceFromNodes(activatedNodes);
        Process process = instance != null ? instance.getProcess() : null;
        if (process == null) {
            return false;
        }
        return ServerUtils.isProcessRunning(process);
    }

    /**
     * Get help context for kill server action.
     * <p/>
     * @return Help context for kill server action.
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Action will be performed synchronously as called in the event thread.
     * <p/>
     * @return Always returns <code>false</code>.
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }

}
