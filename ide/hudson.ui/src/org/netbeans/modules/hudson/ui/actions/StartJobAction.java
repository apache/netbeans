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

package org.netbeans.modules.hudson.ui.actions;

import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action which displays selected job in browser.
 *
 * @author Michal Mocnak
 */
public class StartJobAction extends NodeAction {
    
    protected void performAction(final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (Node node : nodes) {
                    HudsonJob job = node.getLookup().lookup(HudsonJob.class);
                    if (job != null) {
                        job.start();
                    }
                }
            }
        });
    }
    
    protected boolean enable(Node[] nodes) {
        for (Node node : nodes) {
            HudsonJob job = node.getLookup().lookup(HudsonJob.class);
            
            if (null == job || job.isInQueue() || !job.isBuildable())
                return false;
        }
        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(StartJobAction.class, "LBL_StartJobAction"); // NOI18N
    }
    
    protected @Override boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
