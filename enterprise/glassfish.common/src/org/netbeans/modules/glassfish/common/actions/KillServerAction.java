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
