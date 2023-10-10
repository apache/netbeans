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

package org.netbeans.modules.tomcat5.ui.nodes.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.tomcat5.ui.nodes.TomcatWebModule;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class UndeployAction extends NodeAction {

    private static RequestProcessor rp;

    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat app undeployment", 1); // NOI18N
        }
        return rp;
    }

    /** Creates a new instance of Undeploy */
    public UndeployAction() {
    }


    @Override
    public String getName() {
        return NbBundle.getMessage(UndeployAction.class, "LBL_UndeployAction"); //NOI18N
    }

    @Override
    protected void performAction(Node[] nodes) {
        NodeRefreshTask refresh = new NodeRefreshTask(rp());
        for (int i=0; i<nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie) nodes[i].getCookie(TomcatWebModuleCookie.class);
            if (cookie != null) {
                Task task = cookie.undeploy();

                refresh.addPrerequisity(nodes[i].getParentNode(), task);
            }
        }

        rp().post(refresh);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatWebModule module = (TomcatWebModule) nodes[i].getLookup().lookup(TomcatWebModule.class);
            if (module != null) {
                // it should not be allowed to undeploy the /manager application
                if ("/manager".equals(module.getTomcatModule().getPath())) { // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper class supporting the node(s) refresh after the set of prerequisity
     * tasks is finished.
     * <p>
     * Class itself is <i>thread safe</i> (uses intrinsic lock). Refresh
     * itself is performed from dedicated thread so, the refresh must be
     * implemented in thread safe way.
     *
     * @author Petr Hejl
     */
    private static class NodeRefreshTask implements Runnable {

        private final RequestProcessor requestProcessor;

        private Map<Node, Set<Task>> taskMap = new HashMap<>();

        /**
         * Constructs the NodeRefreshTask using the given RequestProcessor.
         *
         * @param requestProcessor will be used for scheduling the refresh tasks
         */
        public NodeRefreshTask(RequestProcessor requestProcessor) {
            Parameters.notNull("requestProcessor", taskMap);

            this.requestProcessor = requestProcessor;
        }

        /**
         * Adds prerequisity task. Defines that the node should be refreshed
         * after the task (and all already added tasks) is finished.
         *
         * @param node node to refresh when the task is finished
         * @param task task to wait for (multiple task can be assigned) by calling this method
         */
        public synchronized void addPrerequisity(Node node, Task task) {
            Parameters.notNull("node", node);
            Parameters.notNull("task", task);

            Set<Task> tasks = taskMap.get(node);
            if (tasks == null) {
                tasks = new HashSet<>();
                taskMap.put(node, tasks);
            }

            tasks.add(task);
        }

        /**
         * Executes this task. For each node added with {@link #addPrerequisity(Node, Task)}
         * it post a new task that waits until all tasks asscociated with the node
         * are finished and after that refreshes the node.
         */
        @Override
        public synchronized void run() {
            for (Map.Entry<Node, Set<Task>> entry : taskMap.entrySet()) {

                final Node node = entry.getKey();
                final Set<Task> tasks = entry.getValue();

                requestProcessor.post( () -> {
                    for (Task task : tasks) {
                        task.waitFinished();
                    }
                    NodeRefreshTask.this.refresh(node);
                });
            }
        }

        private void refresh(Node node) {
            if (node == null) {
                return;
            }

            RefreshWebModulesCookie cookie = node.getLookup().lookup(RefreshWebModulesCookie.class);
            if (cookie != null) {
                cookie.refresh();
            }
        }

    }


}
