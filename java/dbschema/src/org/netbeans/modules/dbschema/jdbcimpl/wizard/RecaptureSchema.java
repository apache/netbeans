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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

public class RecaptureSchema {
    private static final Logger LOGGER = Logger.getLogger(
            RecaptureSchema.class.getName());
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.dbschema.recapture.debug");

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    private DBSchemaWizardData data; 
    private Node dbSchemaNode;
    

    public RecaptureSchema(Node dbSchemaNode) {
        this.dbSchemaNode = dbSchemaNode;
        data = new DBSchemaWizardData();
        data.setExistingConn(true);
    }
    
    public void start() throws ClassNotFoundException, SQLException {
        final DBschemaDataObject dobj = (DBschemaDataObject)dbSchemaNode.getCookie(DBschemaDataObject.class);
        final SchemaElement elem = dobj.getSchema();
        //elem.
        //ConnectionProvider cp = new ConnectionProvider(elem.getDriver(), elem.getUrl(), elem.getUsername(), null);
        if (debug) {
            System.out.println("[dbschema] url='" + elem.getUrl() + "'");
        }
        final FileObject fo1 = dobj.getPrimaryFile();
        SchemaElement.removeFromCache(elem.getName().getFullName() + "#" + fo1.toURL().toString()); //NOI18N
        
        TableElement tableAndViewElements[] = elem.getTables();
        // now break down to tables and views
        final LinkedList tables = new LinkedList();
        final LinkedList views = new LinkedList();
        for (int i = 0; i < tableAndViewElements.length; i++) {
            TableElement te = tableAndViewElements[i];
            if (te.isTable()) {
                if (debug) {
                    System.out.println("[dbschema] adding table='" + te.getName() + "'");
                }
                tables.add(te.getName().getName());
            }
            else {
                if (debug) {
                    System.out.println("[dbschema] adding view='" + te.getName() + "'");
                }
                views.add(te.getName().getName());
            }
        }
        
        final boolean conned = data.isConnected();
        final boolean ec = data.isExistingConn();
        final DatabaseConnection dbconn = data.getDatabaseConnection();
//            final String target1 = target;
        final String dbIdentName = elem.getUrl();
            //dbconn.getName();
        if (debug) {
            System.out.println("[dbschema] conned='" + conned+ "'");
            System.out.println("[dbschema] ec='" + ec + "'");
            System.out.println("[dbschema] NEW dbIdentName='" + dbIdentName + "'");
        }
        final ConnectionProvider cp = createConnectionProvider(data, elem);
        try {
            final ConnectionProvider c = cp;
            if (c == null) {
                String message = MessageFormat.format(
                        bundle.getString("EXC_CouldNotCreateConnection"),
                        elem.getUrl());
                
                throw new SQLException(message);
            }
            if (debug) {
                System.out.println("[dbschema] c.getConnection()='" + c.getConnection() + "'");
            }
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    try {
                        StatusDisplayer.getDefault().setStatusText(bundle.getString("CreatingDatabaseSchema")); //NOI18N
                        
                        final ProgressFrame pf = new ProgressFrame();
                        final SchemaElementImpl sei = new SchemaElementImpl(c);
                        
                        PropertyChangeListener listener = new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent event) {
                                String message;
                                
                                if (event.getPropertyName().equals("totalCount")) { //NOI18N
                                    pf.setMaximum(((Integer)event.getNewValue()).intValue());
                                    return;
                                }

                                if (event.getPropertyName().equals("progress")) { //NOI18N
                                    pf.setValue(((Integer)event.getNewValue()).intValue());
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("tableName")) { //NOI18N
                                    message = MessageFormat.format(bundle.
                                            getString("CapturingTable"), 
                                            ((String) event.getNewValue()).toUpperCase()); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKt")) { //NOI18N
                                    message = MessageFormat.format(
                                            bundle.getString("CaptureFK"), 
                                            ((String) event.getNewValue()).toUpperCase(), 
                                            bundle.getString("CaptureFKtable")); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKv")) { //NOI18N
                                    message = MessageFormat.format(
                                            bundle.getString("CaptureFK"), 
                                            ((String) event.getNewValue()).toUpperCase(), 
                                            bundle.getString("CaptureFKview")); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("viewName")) { //NOI18N
                                    message = MessageFormat.format(
                                            bundle.getString("CapturingView"), 
                                            ((String) event.getNewValue()).toUpperCase()); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("cancel")) { //NOI18N
                                    sei.setStop(true);
                                    StatusDisplayer.getDefault().setStatusText(""); //NOI18N
                                    return;
                                }
                            }
                        };
                        
                        pf.propertySupport.addPropertyChangeListener(listener);
                        pf.setVisible(true);
                        
                        sei.propertySupport.addPropertyChangeListener(listener);
                        final SchemaElement se = new SchemaElement(sei);
                        //se.setName(DBIdentifier.create(dbIdentName));
                        se.setName(elem.getName());
                        
                        sei.initTables(c, tables, views, false);
                        pf.finishProgress();

                        if (! sei.isStop()) {
                            fo1.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                public void run() throws java.io.IOException {
                                    //FileObject fo1 = fo.createData(target1, "dbschema"); //NOI18N
                                    if (debug) {
                                        System.out.println("SchemaElement: " + dumpSe(se));
                                    }
                                    FileLock fl = fo1.lock();
                                    java.io.OutputStream out = fo1.getOutputStream(fl);
                                    if (out == null)
                                        throw new java.io.IOException("Unable to open output stream");

                                    pf.setMessage(bundle.getString("SavingDatabaseSchema")); //NOI18N
                                    StatusDisplayer.getDefault().setStatusText(bundle.getString("SavingDatabaseSchema")); //NOI18N

                                    se.save(out);
                                    fl.releaseLock();
                                }
                            });
                            
                            // refresh the node
                            SchemaElement.addToCache(se);
                            dobj.setSchemaElementImpl(sei);
                            dobj.setSchema(se);

                            pf.setMessage(bundle.getString("SchemaSaved")); //NOI18N
                            StatusDisplayer.getDefault().setStatusText(bundle.getString("SchemaSaved")); //NOI18N
                            
                            pf.setVisible(false);
                            pf.dispose();                        
                        }
                        
                        //c.closeConnection();
                        if (conned)
                            if (ec)
                                ConnectionManager.getDefault().disconnect(dbconn);
                            else
                                c.closeConnection();
                    } catch (Exception exc) {
                        Exceptions.printStackTrace(exc);
                    }
                }
            }, 0);
        } catch (Exception exc) {
            String message = MessageFormat.format(
                    bundle.getString("UnableToCreateSchema"), 
                    exc.getMessage()); //NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Exception(exc, exc.getMessage()));
            LOGGER.log(Level.INFO, null, exc);
            try {
                if (cp != null)
                    cp.closeConnection();
                    if (data.isConnected())
                        if (data.isExistingConn())
                            ConnectionManager.getDefault().disconnect(data.getDatabaseConnection());
                        else
                            cp.closeConnection();
            } catch (Exception exc1) {
                //unable to disconnect
            }
        }
    }
    
    private String dumpSe(SchemaElement se) {
        StringBuffer s = new StringBuffer();
        s.append("name " + se.getName());
        s.append("\n");
        s.append("driver " + se.getDriverName());
        s.append("\n");
        s.append("username " + se.getUsername());
        s.append("\n");
        TableElement tables[] = se.getTables();
        s.append("tables count " + tables.length);
        s.append("\n");
        for (int i = 0; i < tables.length; i++) {
            s.append("    table " + tables[i].getName());
            s.append("\n");
            ColumnElement columns[] = tables[i].getColumns();
            for (int j = 0; j < columns.length; j++) {
                s.append("        column " + columns[j].getName());
                s.append("\n");
            }
        }
        return s.toString();
    }
    
    public ConnectionProvider createConnectionProvider(DBSchemaWizardData data,
            SchemaElement elem) throws SQLException {
        
        DatabaseConnection dbconn = findDatabaseConnection(elem);
        if (dbconn == null) {
            if (debug) {
                    System.out.println("[dbschema-ccp] not found dbconn='" + dbconn + "'");
                }
            String message = MessageFormat.format(
                    bundle.getString("EXC_CouldNotCreateConnection"),
                    elem.getUrl());

            throw new SQLException(message);
        }
        if (debug) {
            System.out.println("[dbschema-ccp] found dbconn='" + dbconn.getDatabaseURL() + "'");
        }
        data.setDatabaseConnection(dbconn);
        ConnectionHandler ch = new ConnectionHandler(data);
        if (ch.ensureConnection()) {
            dbconn = data.getDatabaseConnection();
            if (debug) {
                System.out.println("[dbschema-ccp] connection ensured ='" + dbconn.getDatabaseURL() + "'"); 
            }
            ConnectionProvider connectionProvider = 
                new ConnectionProvider(dbconn.getJDBCConnection(), dbconn.getDriverClass());
            connectionProvider.setSchema(dbconn.getSchema());
            //String schemaName = cni.getName();
            //schemaElementImpl.setName(DBIdentifier.create(schemaName));
            return connectionProvider;
        }
        if (debug) {
            System.out.println("[dbschema-ccp] connection not ensured, returning null");
        }
        
        String message = MessageFormat.format(
                bundle.getString("EXC_UnableToConnect"), elem.getUrl());
        throw new SQLException(message);
    }
    
    private DatabaseConnection findDatabaseConnection(final SchemaElement elem) {
        DatabaseConnection dbconns[] = ConnectionManager.getDefault().getConnections();
        
        // Trim off connection properties, as in some cases, what dbmd.getUrl()
        // returns is not the same as what is set in the DB Explorer, and
        // we really want to match on the base URL, not on the full
        // set of property strings.  Otherwise you get false negatives,
        // see issue 104259.
        String url = trimUrl(elem.getUrl());
        for (int i = 0; i < dbconns.length; i++) {
            String dburl = dbconns[i].getDatabaseURL();
            if ( dburl != null && dburl.startsWith(url)) {
                return dbconns[i];
            }
        }
        
        // None found, so let the user pick one
        DatabaseConnection conn = 
            Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {

                public DatabaseConnection run() {
                    return ChooseConnectionPanel.showChooseConnectionDialog(
                            elem.getUrl());
                }
            });
            
        return conn;
    }
    
    private static String trimUrl(String url) {
        assert url != null;
        
        // Strip off connection properties
        url = url.split("[\\?\\&;]")[0]; // NOI18N
        
        return url;
    }
    
    private DatabaseConnection createDatabaseConnection(SchemaElement elem)
        throws SQLException {
        final String url = elem.getUrl();
        final String user = elem.getUsername();
        String driver = elem.getDriver();
        JDBCDriver[] jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(driver);
        if ( jdbcDrivers.length == 0 ) {
            String message = MessageFormat.format(
                    bundle.getString("EXC_NoDriverFound"), driver);
            throw new SQLException(message);
        }
        
        final JDBCDriver jdbcDriver = jdbcDrivers[0];
        
        DatabaseConnection conn = Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {
            public DatabaseConnection run() {
                return ConnectionManager.getDefault().
                        showAddConnectionDialogFromEventThread(jdbcDriver, url,
                            user, null);
            }
        });
        
        return conn;        
    }
    
    private static class ConnectionHandler extends DBSchemaTablesPanel {
        public ConnectionHandler(DBSchemaWizardData data) {
            super(data, new ArrayList());
        }
        
        public boolean ensureConnection() {
            return init();
        }
    }
    
}
