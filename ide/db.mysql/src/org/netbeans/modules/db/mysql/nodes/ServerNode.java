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

package org.netbeans.modules.db.mysql.nodes;

import java.awt.Image;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.actions.DisconnectServerAction;
import org.netbeans.modules.db.mysql.actions.PropertiesAction;
import org.netbeans.modules.db.mysql.actions.StopAction;
import org.netbeans.modules.db.mysql.actions.StartAction;
import org.netbeans.modules.db.mysql.util.Utils;
import org.netbeans.modules.db.mysql.actions.CreateDatabaseAction;
import org.netbeans.modules.db.mysql.actions.ConnectServerAction;
import org.netbeans.modules.db.mysql.actions.AdministerAction;
import org.netbeans.modules.db.mysql.DatabaseServer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.actions.RefreshServerAction;
import org.netbeans.modules.db.mysql.impl.ServerNodeProvider;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a MySQL Server instance.  
 * 
 * @author David Van Couvering
 */
public class ServerNode extends AbstractNode implements ChangeListener, Comparable {  
    private final DatabaseServer server;
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON = "org/netbeans/modules/db/mysql/resources/catalog.gif";
    
    private static final HelpCtx HELP_CONTEXT =
            new HelpCtx(ServerNode.class.getName());
            
    public static ServerNode create(DatabaseServer server) {
        ChildFactory factory = new ChildFactory(server);
        return new ServerNode(factory, server);
    }
    
    private ServerNode(ChildFactory factory, DatabaseServer server) {
        super(Children.create(factory, true));
        this.server = server;
        
        setName(""); // NOI18N
        setDisplayName(server.getDisplayName());
        setShortDescription(server.getShortDescription());
        setIconBaseWithExtension(ICON);
        
        registerListeners();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node.Cookie getCookie(Class cls) {
        if ( cls == DatabaseServer.class ) {
            return server;
        } else {
            return super.getCookie(cls);
        }
        
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HELP_CONTEXT;
    }    
    
    private void registerListeners() {
        DatabaseServer server = DatabaseServerManager.getDatabaseServer();
        server.addChangeListener(
                WeakListeners.create(ChangeListener.class, this, server));
        
        stateChanged(new ChangeEvent(server));
    }
    

    public void stateChanged(ChangeEvent evt) {
        // The display name changes depending on the 
        // state of the server instance
        String oldName = getDisplayName();
        String oldShortDescription = getShortDescription();
        setDisplayName(server.getDisplayName());
        fireNameChange(oldName, getDisplayName());
        setShortDescription(server.getShortDescription());
        fireShortDescriptionChange(oldShortDescription, getShortDescription());
        fireIconChange();
    }
                
    @Override
    public Action[] getActions(boolean context) {
        if ( context ) {
            return super.getActions(context);
        } else {
            return new SystemAction[] {
                SystemAction.get(CreateDatabaseAction.class),
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(ConnectServerAction.class),
                SystemAction.get(DisconnectServerAction.class),
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RefreshServerAction.class),
                SystemAction.get(AdministerAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        }
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public Image getIcon(int type) {
        return server.getIcon();
    }
    
    @Override
    public void destroy() {
       ServerNodeProvider.getDefault().setRegistered(false);
    }
            
    private static class ChildFactory 
            extends org.openide.nodes.ChildFactory<Database> 
            implements ChangeListener {
        
        private static final Comparator<Database> COMPARATOR = 
                new InstanceComparator();

        private final DatabaseServer server;


        public ChildFactory(DatabaseServer server) {            
            super();
            
            this.server = server;
            
            server.addChangeListener(
                WeakListeners.create(ChangeListener.class, this, server));
            stateChanged(new ChangeEvent(server));
        }

        @Override
        protected Node createNodeForKey(Database db) {
            return new DatabaseNode(db);
        }

        @Override
        protected boolean createKeys(List<Database> toPopulate) {
            List<Database> fresh = new ArrayList<Database>();

            try {
                fresh.addAll(server.getDatabases());
            } catch (DatabaseException ex) {
                Utils.displayError(Utils.getMessage( 
                        "MSG_UnableToGetDatabaseList"), ex);
                return true;
            }

            fresh.sort(COMPARATOR);

            toPopulate.addAll(fresh);
            
            return true;
        }

        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }

    private static class InstanceComparator 
            implements Comparator<Database>, Serializable {

        public int compare(Database o1, Database o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }

    public int compareTo(Object other) {
        Node othernode = (Node)other;
        return this.getDisplayName().compareTo(othernode.getDisplayName());
    }

}
