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

package org.netbeans.modules.db.explorer.node;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.TestCase;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.test.Util;
import org.openide.nodes.Node;

/**
 *
 * @author Rob Englander
 */
public class RootNodeTest extends TestCase {

    private static final AtomicBoolean CONNECTIONS_CHANGE_FIRED = new AtomicBoolean(false);

    public RootNodeTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        Util.clearConnections();
        Util.deleteDriverFiles();
    }
    /**
     * Use case: create the root node, and verify that the expected
     * hierarchy of nodes are created
     */
    public void testRootHierarchy() throws Exception {
        // Initialize the tree with a driver and a connection
        JDBCDriver driver = Util.createDummyDriver();
        JDBCDriverManager.getDefault().addDriver(driver);

        addListener();
        DatabaseConnection conn = DatabaseConnection.create(
                driver, "jdbc:mark//twain", "tomsawyer", null, "whitewash", true);
        ConnectionManager.getDefault().addConnection(conn);
        waitChanged();

        RootNode rootNode = RootNode.instance();

        // Need to force a refresh because otherwise it happens asynchronously
        // and this test does not pass reliably
        RootNode.instance().getChildNodesSync();
        
        checkConnection(rootNode, conn);
        checkNodeChildren(rootNode);
    }

    /**
     * Ensure, that the connection list stays sorted, if the displayName
     * which is the sorting criterium, is changed
     */
    public void testSortingAfterDisplayNameChange() throws Exception {
        // Initialize the tree with a driver and a connection
        JDBCDriver driver = Util.createDummyDriver();
        JDBCDriverManager.getDefault().addDriver(driver);

        addListener();
        DatabaseConnection conn2 = DatabaseConnection.create(
                driver, "jdbc:mark//twain/conn2", "tomsawyer", null, "whitewash", true, "B2");
        ConnectionManager.getDefault().addConnection(conn2);
        waitChanged();

        DatabaseConnection conn = DatabaseConnection.create(
                driver, "jdbc:mark//twain/conn", "tomsawyer", null, "whitewash", true, "A1");
        ConnectionManager.getDefault().addConnection(conn);
        waitChanged();
        
        RootNode rootNode = RootNode.instance();

        List<? extends Node> children = new ArrayList<>(rootNode.getChildNodesSync());

        assertEquals("A1", children.get(1).getDisplayName());
        assertEquals("B2",
                children.get(2).getDisplayName());

        Method m = conn.getClass().getDeclaredMethod("getDelegate", new Class<?>[]{});
        m.setAccessible(true);

        org.netbeans.modules.db.explorer.DatabaseConnection dc =
                (org.netbeans.modules.db.explorer.DatabaseConnection) m.invoke(conn, new Object[]{});

        dc.setDisplayName("C3");

        children = new ArrayList(rootNode.getChildNodesSync());

        assertEquals("B2", children.get(1).getDisplayName());
        assertEquals("C3", children.get(2).getDisplayName());
    }

    private void checkNodeChildren(RootNode root) throws Exception {
        Collection<? extends Node> children = root.getChildNodesSync();
        assertTrue(children.size() == 2);

        // we should find 1 DriverListNode and 1 ConnectionNode
        int driverListCount = 0;
        int connectionCount = 0;
        for (Node child : children) {
            if (child instanceof DriverListNode) {
                driverListCount++;
            } else if (child instanceof ConnectionNode) {
                connectionCount++;
            }
        }

        assertTrue(driverListCount == 1);
        assertTrue(connectionCount == 1);
    }

    private void checkConnection(RootNode root,
            DatabaseConnection expected) throws Exception {

        Collection<? extends Node> children = root.getChildNodesSync();
        for (Iterator it = children.iterator() ; it.hasNext() ; ) {
            Object next = it.next();
            if (next instanceof ConnectionNode) {
                ConnectionNode cNode = (ConnectionNode)next;
                DatabaseConnection conn = cNode.getDatabaseConnection().getDatabaseConnection();
                assertTrue(conn != null);
                assertTrue(conn.getDatabaseURL().equals(expected.getDatabaseURL()));
                assertTrue(conn.getUser().equals(expected.getUser()));
                assertTrue(conn.getPassword().equals(expected.getPassword()));
                assertTrue(conn.getDriverClass().equals(expected.getDriverClass()));
                return;
            }
        }
    }

    private static final ConnectionListener CONN_LISTENER = new ConnectionListener() {
        @Override
        public void connectionsChanged() {
            synchronized (CONNECTIONS_CHANGE_FIRED) {
                CONNECTIONS_CHANGE_FIRED.notifyAll();
                CONNECTIONS_CHANGE_FIRED.set(true);
            }
        }
    };
    
    private static void addListener() {
        CONNECTIONS_CHANGE_FIRED.set(false);
        ConnectionManager.getDefault().removeConnectionListener(CONN_LISTENER);
        ConnectionManager.getDefault().addConnectionListener(CONN_LISTENER);
    }

    /**
     * Waits until lookup result is not refreshed and subsequently
     * ConnectionList.fireListeners is called
     *
     * @throws InterruptedException
     */
    private static void waitChanged() throws InterruptedException {
        synchronized (CONNECTIONS_CHANGE_FIRED) {
            if (!CONNECTIONS_CHANGE_FIRED.get()) {
                CONNECTIONS_CHANGE_FIRED.wait(10000);
            }
        }
        // reset property
        CONNECTIONS_CHANGE_FIRED.set(false);
    }
}
