/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.mysql.actions;

import org.netbeans.modules.db.mysql.util.Utils;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseServer.ServerState;
import org.netbeans.modules.db.mysql.impl.StopManager;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog.Tab;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 * @author David Van Couvering
 */
public class StopAction extends CookieAction {
    private static final Class[] COOKIE_CLASSES = 
            new Class[] { DatabaseServer.class };
    
    public StopAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return Utils.getBundle().getString("LBL_StopAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(StopAction.class);
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return false;
        }
        
        DatabaseServer server = activatedNodes[0].getCookie(DatabaseServer.class);
        if (server == null) {
            return false;
        }
        
        ServerState state = server.getState();

        // Don't be too picky about when to enable stop, as we really don't have
        // 100% certainty about what exactly is up with the server
        return state != ServerState.DISCONNECTED && !StopManager.getDefault().isStopRequested();
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        final DatabaseServer server = activatedNodes[0].getCookie(DatabaseServer.class);
        String path = server.getStopPath();
        String message = Utils.getMessage(
                "MSG_NoStopPath");
        PropertiesDialog dialog = new PropertiesDialog(server);


        while ( path == null || path.equals("")) {
            
            if ( ! Utils.displayConfirmDialog(message) ) {
                return;
            }  
            
            if ( ! dialog.displayDialog(Tab.ADMIN) ) {
                return;
            }
            
            path = server.getStopPath();
        }

            StopManager.getDefault().stop(server);
    }
    
    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }
}
