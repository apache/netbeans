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

import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.CreateDatabasePanel;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author David Van Couvering
 */
public class CreateDatabaseAction extends CookieAction {
    private static Logger LOGGER = Logger.getLogger(CreateDatabaseAction.class.getName());

    private static final Class[] COOKIE_CLASSES = new Class[] {
        DatabaseServer.class
    };

    public CreateDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return Utils.getBundle().getString("LBL_CreateDatabaseAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateDatabaseAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ( activatedNodes.length == 0 ) {
            return false;
        }
        
        Node node = activatedNodes[0];
        
        DatabaseServer server = node.getCookie(DatabaseServer.class);
        if ( server != null && server.isConnected() ) {
            return true;
        }
        
        return false;
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];

        final DatabaseServer server = node.getCookie(DatabaseServer.class);
        try {
            CreateDatabasePanel panel = new CreateDatabasePanel(server);
            panel.showCreateDatabaseDialog();
        } catch (DatabaseException dbe) {
            Utils.displayErrorMessage(dbe.getMessage());
        }
    }
}
