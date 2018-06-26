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
 * Restart action stops the server and then starts it again in the mode it 
 * was running in before (normal or debug).
 *
 * @author sherold
 */
public class RestartAction extends ControlAction {
    
    public String getName() {
        return NbBundle.getMessage(RestartAction.class, "LBL_Restart");
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
                    String title = NbBundle.getMessage(RestartAction.class, "LBL_Restarting", si.getDisplayName());
                    ProgressUI progressUI = new ProgressUI(title, false);
                    try {
                        progressUI.start();
                        si.restart(progressUI);
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
        if (si == null || !si.canStartServer()) {
            return false;
        }
        int state = si.getServerState();
        if (state != ServerInstance.STATE_RUNNING
            && state != ServerInstance.STATE_DEBUGGING
            && state != ServerInstance.STATE_PROFILING
            && state != ServerInstance.STATE_PROFILER_BLOCKING) {
            return false;
        }
        return true;
    }

    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ServerInstance.StateListener {
    
        private static final String ICON = 
                "org/netbeans/modules/j2ee/deployment/impl/ui/resources/restart.png";  // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private final ServerInstance instance;
        
        public OutputAction(ServerInstance instance) {
            super(NbBundle.getMessage(RestartAction.class, "LBL_RestartOutput"),ImageUtilities.loadImageIcon(ICON, false));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RestartAction.class, "LBL_RestartOutputDesc"));
            this.instance = instance;
            
            // start listening to changes
            instance.addStateListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            performActionImpl(instance);
        }

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
