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

package org.netbeans.jellytools.modules.db.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.db.actions.ConnectAction;
import org.netbeans.jellytools.modules.db.actions.DisconnectAction;
import org.netbeans.jellytools.modules.db.actions.ExecuteCommandAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing "Databases > ${connection}" node in Services tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      ConnectionNode conn = DriversNode.invoke("jdbc:derby:/mydb", "tester", "APP");
 *      conn.connect();
 *      conn.executeCommand();
 *      ....
 *      conn.disconnect();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class ConnectionNode extends Node {
    private static final Action connectAction = new ConnectAction();
    private static final Action disconnectAction = new DisconnectAction();
    private static final Action executeCommandAction = new ExecuteCommandAction();
    private static final Action deleteAction = new DeleteAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new ConnectionNode
     * @param url database URL
     * @param user user name
     * @param schema schema name */
    public ConnectionNode(String url, String user, String schema) {
        super(new RuntimeTabOperator().getRootNode(), DatabasesNode.TREE_PATH+
                "|"+connectionName(url, user, schema));
    }

    /** Finds "Databases > ${connection}" node 
     * @param url database URL
     * @param user user name
     * @param schema schema name */
    public static ConnectionNode invoke(String url, String user, String schema) {
        RuntimeTabOperator.invoke();
        return new ConnectionNode(url, user, schema);
    }

    /** performs ConnectAction with this node */
    public void connect() {
        connectAction.perform(this);
    }

    /** performs DisconnectAction with this node */
    public void disconnect() {
        disconnectAction.perform(this);
    }

    /** performs ExecuteCommandAction with this node */
    public void executeCommand() {
        executeCommandAction.perform(this);
    }

    /** performs DeleteAction with this node */
    public void delete() {
        deleteAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    

    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            connectAction,
            disconnectAction,
            executeCommandAction,
            deleteAction,
            propertiesAction
        });
    }
    
    private static String connectionName(String url, String user, String schema) {
        if (schema == null) {
            schema = Bundle.getStringTrimmed(
                "org.netbeans.modules.db.explorer.Bundle",
                "SchemaIsNotSet");
        }
        return Bundle.getStringTrimmed(
                "org.netbeans.modules.db.explorer.Bundle",
                "ConnectionNodeUniqueName", new Object[] {url, user, schema});
    }

}
