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

package org.netbeans.modules.derby;

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
 * @author Jiri Rechtacek
 */
class DerbyDatabaseNode extends AbstractNode implements Comparable {
    
    private static final String ICON_BASE = "org/netbeans/modules/derby/resources/database.gif";

    private String database;
    private DerbyDatabasesImpl server;
    
    public DerbyDatabaseNode(String dbName, DerbyDatabasesImpl server) {
        super(Children.LEAF);
        this.database = dbName;
        this.server = server;
        setName(dbName);
        setDisplayName(dbName);
        setShortDescription(NbBundle.getMessage(DerbyDatabaseNode.class, "DerbyDatabaseNode_ShortDescription", dbName, DerbyOptions.getDefault().getLocation()));
        setIconBaseWithExtension(ICON_BASE);
    }
        
   
    @Override
    public Action[] getActions(boolean context) {
        if ( context ) {
            return super.getActions(context);
        } else {
            return new SystemAction[] {
                SystemAction.get(ConnectDatabaseAction.class),
                SystemAction.get(DeleteAction.class)
            };
        }
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    @NbBundle.Messages({
        "# {0} - Database name",
        "MSG_Confirm_DB_Delete=Really delete database {0}?",
        "MSG_Confirm_DB_Delete_Title=Delete Database"})
    public void destroy() {
        NotifyDescriptor d =
                new NotifyDescriptor.Confirmation(
                Bundle.MSG_Confirm_DB_Delete(database),
                Bundle.MSG_Confirm_DB_Delete_Title(),
                NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        if (NotifyDescriptor.OK_OPTION.equals(result)) {
            server.dropDatabase(database);
        }
    }
    
    @Override
    public int compareTo(Object other) {
        Node othernode = (Node)other;
        return this.getDisplayName().compareTo(othernode.getDisplayName());
    }

}
