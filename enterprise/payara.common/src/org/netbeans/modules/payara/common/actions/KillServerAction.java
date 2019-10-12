/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.common.actions;

import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.ui.WarnPanel;
import org.netbeans.modules.payara.common.utils.ServerUtils;
import org.netbeans.modules.payara.spi.PayaraModule;
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
     * Retrieve Payara support instance from nodes.
     * <p/>
     * @param nodes Current activated nodes, may be empty but
     *        not <code>null</code>.
     * @return First Payara instance found in nodes or <code>null</code>
     *         when no Payara instance was found.
     */
    private PayaraModule getPayaraModuleFromNodes(final Node[] nodes) {
        for (Node node : nodes) {
            PayaraModule commonSupport
                    = node.getLookup().lookup(PayaraModule.class);
            if (commonSupport != null) {
                return commonSupport;
            }
        }
        return null;
    }

    /**
     * Retrieve Payara instance from nodes.
     * <p/>
     * @param nodes Current activated nodes, may be empty but
     *        not <code>null</code>.
     * @return First Payara instance found in nodes or <code>null</code>
     *         when no Payara instance was found.
     */
    private PayaraInstance getInstanceFromNodes(final Node[] nodes) {
        PayaraModule commonSupport = getPayaraModuleFromNodes(nodes);
        return commonSupport != null
                ? (PayaraInstance)commonSupport.getInstance() : null;
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
        PayaraModule commonSupport
                = getPayaraModuleFromNodes(activatedNodes);
        if (commonSupport != null) {
            performActionImpl(commonSupport);
        }
    }

    /**
     * Action implementation code.
     * <p/>
     * @param commonSupport Interface implemented by common server support.
     */
    private static void performActionImpl(PayaraModule commonSupport) {
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
        PayaraInstance instance = getInstanceFromNodes(activatedNodes);
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
