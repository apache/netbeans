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

package org.netbeans.modules.db.mysql.nodes;

import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.actions.ConnectAction;
import org.netbeans.modules.db.mysql.DatabaseServer;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Represents a database. 
 * 
 * @author David Van Couvering
 */
class DatabaseNode extends AbstractNode implements Comparable {
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON_BASE = "org/netbeans/modules/db/mysql/resources/database.gif";
    
    private final Database model;    
    
    public DatabaseNode(Database model) {
        super(Children.LEAF);
        this.model = model;
        setDisplayName(model.getDisplayName());
        setShortDescription(model.getShortDescription());
        setIconBaseWithExtension(ICON_BASE);
    }
        
   
    @Override
    public Action[] getActions(boolean context) {
        if ( context ) {
            return super.getActions(context);
        } else {
            return new SystemAction[] {
                SystemAction.get(ConnectAction.class),
                SystemAction.get(DeleteAction.class)
            };
        }
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @NbBundle.Messages({
        "# {0} - Database name",
        "MSG_Confirm_DB_Delete=Really delete database {0}?",
        "MSG_Confirm_DB_Delete_Title=Delete Database"})
    @Override
    public void destroy() {
        NotifyDescriptor d =
                new NotifyDescriptor.Confirmation(
                Bundle.MSG_Confirm_DB_Delete(model.getDbName()),
                Bundle.MSG_Confirm_DB_Delete_Title(),
                NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        if (!NotifyDescriptor.OK_OPTION.equals(result)) {
            return;
        }
        DatabaseServer server = model.getServer();
        String dbname = model.getDbName();

        server.dropDatabase(dbname);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Node.Cookie getCookie(Class cls) {
        if ( cls == Database.class ) {
            return model;
        } else {
            return super.getCookie(cls);
        }
        
    }
    
    public int compareTo(Object other) {
        Node othernode = (Node)other;
        return this.getDisplayName().compareTo(othernode.getDisplayName());
    }

        
}
