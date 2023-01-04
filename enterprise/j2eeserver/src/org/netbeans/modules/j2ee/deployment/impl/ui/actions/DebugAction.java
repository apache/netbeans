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

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.j2ee.deployment.impl.ServerException;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;


/**
 * Debug server action starts the server in the debug mode.
 *
 * @author sherold
 */
public class DebugAction extends ControlAction {
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "LBL_Debug");
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
        for (int i = 0; i < nodes.length; i++) {
            final ServerInstance si = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            performActionImpl(si);
        }
    }

    private static void performActionImpl(final ServerInstance si) {
        if (si != null) {
            RP.post(new Runnable() {
                public void run() {
                    String title = NbBundle.getMessage(DebugAction.class, "LBL_Debugging", si.getDisplayName());
                    ProgressUI progressUI = new ProgressUI(title, false);
                    try {
                        progressUI.start();
                        si.startDebug(progressUI);
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
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            if (!enableImpl(si)) {
                return false;
            }
        }
        return true;
    }

    private static boolean enableImpl(final ServerInstance si) {
        if (si == null || si.getServerState() != ServerInstance.STATE_STOPPED
            || !si.isDebugStartSupported()) {
            return false;
        }
        return true;
    }

    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ServerInstance.StateListener {
    
        private static final String ICON = 
                "org/netbeans/modules/j2ee/deployment/impl/ui/resources/debug.png";  // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private final ServerInstance instance;
        
        public OutputAction(ServerInstance instance) {
            super(NbBundle.getMessage(DebugAction.class, "LBL_DebugOutput"),ImageUtilities.loadImageIcon(ICON, false));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DebugAction.class, "LBL_DebugOutputDesc"));
            this.instance = instance;
            
            // start listening to changes
            instance.addStateListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            performActionImpl(instance);
        }

        @Override
        public boolean isEnabled() {
            return enableImpl(instance);
        }
        
        // ServerInstance.StateListener implementation --------------------------
        
        public void stateChanged(final int oldState, final int newState) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    firePropertyChange(
                        PROP_ENABLED, 
                        null,
                        isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
}
