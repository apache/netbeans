/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.DatabaseModule;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander, Jiri Rechtacek
 */
public class ProcedureNodeProvider extends NodeProvider {
    private static final Logger LOG = Logger.getLogger(ProcedureNodeProvider.class.getName());

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public ProcedureNodeProvider createInstance(Lookup lookup) {
                ProcedureNodeProvider provider = new ProcedureNodeProvider(lookup);
                return provider;
            }
        };
    }

    private final DatabaseConnection connection;
    private final MetadataElementHandle<Schema> schemaHandle;
    private String schemaName;

    @SuppressWarnings("unchecked")
    private ProcedureNodeProvider(Lookup lookup) {
        super(lookup, procedureComparator);
        connection = getLookup().lookup(DatabaseConnection.class);
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    protected synchronized void initialize() {

        final List<Node> newList = new ArrayList<Node>();

        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                    @Override
                        public void run(Metadata metaData) {
                            Schema schema = schemaHandle.resolve(metaData);
                            if (schema != null) {
                                schemaName = schema.getName();
                                Collection<Procedure> procedures = schema.getProcedures();
                                for (Procedure procedure : procedures) {
                                    MetadataElementHandle<Procedure> handle = MetadataElementHandle.create(procedure);
                                    Collection<Node> matches = getNodes(handle);
                                    if (matches.size() > 0) {
                                        newList.addAll(matches);
                                    } else {
                                        NodeDataLookup lookup = new NodeDataLookup();
                                        lookup.add(connection);
                                        lookup.add(handle);

                                        newList.add(ProcedureNode.create(lookup, ProcedureNodeProvider.this, schema.getName()));
                                    }
                                }
                            } else {
                                schemaName = null;
                            }
                        }
                    }
                );
                refreshObjects();
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }

        setNodes(newList);
    }

    private static final Comparator<Node> procedureComparator = new Comparator<Node>() {

        @Override
        public int compare(Node model1, Node model2) {
            return model1.getDisplayName().compareTo(model2.getDisplayName());
        }

    };

    @Override
    public synchronized void refresh() {
        super.refresh();
        refreshObjects();
    }

    private Set<String> validObjects = null;
    private Map<String, ProcedureNode.Type> object2type = null;

    private synchronized void refreshObjects() {
        if (connection != null &&
                DatabaseModule.IDENTIFIER_MYSQL.equalsIgnoreCase(connection.getDriverName())) {
            // MySQL
            boolean connected = connection.isConnected();
            MetadataModel metaDataModel = connection.getMetadataModel();
            if (connected && metaDataModel != null) {
                try {
                    metaDataModel.runReadAction(
                        new Action<Metadata>() {
                            @Override
                            public void run(Metadata metaData) {
                                object2type = new HashMap<>();
                                validObjects = new HashSet<>();
                                String query = "SELECT NAME, TYPE" // NOI18N
                                            + " FROM mysql.proc" // NOI18N
                                            + " WHERE TYPE = 'PROCEDURE' OR TYPE = 'FUNCTION'"; // NOI18N
                                try(Statement stmt = connection.getJDBCConnection().createStatement();
                                    ResultSet rs = stmt.executeQuery(query)) {
                                    while(rs.next()) {
                                        // name of procedure
                                        String objectName = rs.getString("NAME"); // NOI18N
                                        // type of procedure
                                        String objectType = rs.getString("TYPE"); // NOI18N
                                        if ("PROCEDURE".equals(objectType)) { // NOI18N
                                            object2type.put(objectName, ProcedureNode.Type.Procedure);
                                        } else if ("FUNCTION".equals(objectType)) { // NOI18N
                                            object2type.put(objectName, ProcedureNode.Type.Function);
                                        } else {
                                            assert false : "Unknown type " + objectType;
                                        }
                                        // XXX: all procedurec are valid in MySQL
                                        validObjects.add(objectName);
                                    }
                                } catch (SQLException ex) {
                                    LOG.log(Level.INFO, ex + "{0} while refreshStatuses() of triggers in schema {1}", new Object[] {ex, schemaName});
                                }
                                String query2 = "SELECT TRIGGER_NAME" // NOI18N
                                            + " FROM information_schema.triggers"; // NOI18N
                                try (Statement stmt = connection.getJDBCConnection().createStatement();
                                        ResultSet rs = stmt.executeQuery(query2)) {
                                    while (rs.next()) {
                                        // name of procedure
                                        String objectName = rs.getString("TRIGGER_NAME"); // NOI18N
                                        // type of procedure is trigger
                                        object2type.put(objectName, ProcedureNode.Type.Trigger);
                                        // XXX: all triggers are valid in MySQL
                                        validObjects.add(objectName);
                                    }
                                } catch (SQLException ex) {
                                    LOG.log(Level.INFO, ex + "{0} while refreshStatuses() of triggers in schema {1}", new Object[] {ex, schemaName});
                                }
                            }
                        }
                    );
                } catch (MetadataModelException e) {
                    NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
                }
            }
        } else if (connection != null && connection.getDriverName() != null &&
                connection.getDriverName().startsWith(DatabaseModule.IDENTIFIER_ORACLE)) {
            // Oracle
            boolean connected = connection.isConnected();
            MetadataModel metaDataModel = connection.getMetadataModel();
            if (schemaName == null) {
                LOG.log(Level.INFO, "No schema for {0}", this);
                return ;
            }
            if (connected && metaDataModel != null) {
                try {
                    metaDataModel.runReadAction(
                        new Action<Metadata>() {
                            @Override
                            public void run(Metadata metaData) {
                                validObjects = new HashSet<>();
                                object2type = new HashMap<>();
                                String schemaEscaped = schemaName.replace("'", "''");
                                String query = "SELECT OBJECT_NAME, STATUS, OBJECT_TYPE" // NOI18N
                                        + " FROM SYS.ALL_OBJECTS " // NOI18N
                                        + " WHERE OWNER='" + schemaEscaped + "' "// NOI18N
                                        + " AND ( OBJECT_TYPE = 'PROCEDURE' OR OBJECT_TYPE = 'TRIGGER' OR OBJECT_TYPE = 'FUNCTION' )";  // NOI18N
                                try (Statement stmt = connection.getJDBCConnection().createStatement();
                                        ResultSet rs = stmt.executeQuery(query);) {
                                    while(rs.next()) {
                                        // name of procedure
                                        String objectName = rs.getString("OBJECT_NAME"); // NOI18N
                                        // valid or invalid
                                        String status = rs.getString("STATUS"); // NOI18N
                                        boolean valid = "VALID".equals(status); // NOI18N
                                        if (valid) {
                                            validObjects.add(objectName);
                                        }
                                        // type of procedure
                                        String objectType = rs.getString("OBJECT_TYPE"); // NOI18N
                                        if ("PROCEDURE".equals(objectType)) { // NOI18N
                                            object2type.put(objectName, ProcedureNode.Type.Procedure);
                                        } else if ("FUNCTION".equals(objectType)) { // NOI18N
                                            object2type.put(objectName, ProcedureNode.Type.Function);
                                        } else if ("TRIGGER".equals(objectType)) { // NOI18N
                                            object2type.put(objectName, ProcedureNode.Type.Trigger);
                                        } else {
                                            assert false : "Unknown type " + objectType;
                                        }                                    
                                    }
                                } catch (SQLException ex) {
                                    LOG.log(Level.INFO, "{0} while refreshStatuses() of procedures in schema {1}", new Object[] {ex, schemaName});
                                }
                            }
                        }
                    );
                } catch (MetadataModelException e) {
                    NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
                }
            }
        } else {
            // others
        }
    }

    public boolean getStatus(String name) {
        if (validObjects == null) {
            refreshObjects();
        }
        return validObjects.contains(name);
    }

    public ProcedureNode.Type getType(String name) {
        if (object2type == null || object2type.get(name) == null) {
            refreshObjects();
        }
        return object2type.get(name);
    }

}
