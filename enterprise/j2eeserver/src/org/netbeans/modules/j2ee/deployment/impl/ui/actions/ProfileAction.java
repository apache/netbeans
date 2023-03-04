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


package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import org.netbeans.modules.j2ee.deployment.impl.ServerException;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Profile server action starts the server in the profile mode.
 *
 * @author sherold
 */
public class ProfileAction extends ControlAction {
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "LBL_Profile");
    }
    
    protected void performAction(Node[] nodes) {
        performActionImpl(nodes);
    }
    
    protected boolean enable(Node[] nodes) {
        return enableImpl(nodes);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() { 
        return false; 
    }
    
    // private helper methods -------------------------------------------------
    
    private static void performActionImpl(Node[] nodes) {
        if (nodes.length != 1) {
            return;
        }
        final ServerInstance si = (ServerInstance)nodes[0].getCookie(ServerInstance.class);
        if (si != null) {
//            Profiler profiler = ServerRegistry.getProfiler();
//            if (profiler == null) {
//                return;
//            }
//            final ProfilerServerSettings settings = profiler.getSettings(si.getUrl());
//            if (settings == null) {
//                return;
//            }
            RP.post(new Runnable() {
                public void run() {
                    String title = NbBundle.getMessage(DebugAction.class, "LBL_Profiling", si.getDisplayName());
                    ProgressUI progressUI = new ProgressUI(title, false);
                    try {
                        progressUI.start();
                        si.startProfile(false, progressUI);
                    } catch (ServerException ex) {
                        String msg = ex.getLocalizedMessage();
                        NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } finally {
                        progressUI.finish();
                    }
                }
            });
        }
    }
    
    private static boolean enableImpl(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        ServerInstance si = (ServerInstance)nodes[0].getCookie(ServerInstance.class);
        if (si == null || si.getServerState() != ServerInstance.STATE_STOPPED 
            || !si.isProfileSupported()) {
            return false;
        }
        return true;
    }
}
