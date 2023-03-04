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
