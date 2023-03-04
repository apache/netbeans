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

package org.netbeans.modules.db.mysql.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.openide.util.NbBundle;

/**
 * This class encapsulates a database connection and serializes
 * interaction with this connection through a blocking queue.
 *
 * This is a thread-safe class
 *
 * @author David Van Couvering
 */
public final class ConnectionProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());
    final BlockingQueue<Runnable> inqueue;
    
    // INVARIANT: if connection is null or not connected, quoter is null
    // if connection is connected, quoter is set based on DBMD for the connection
    // synchronized on this
    private Connection conn;
    // synchronized on this
    private Quoter quoter;

    private final AtomicReference<Thread> taskThreadRef = new AtomicReference<Thread>();

    synchronized void setConnection(Connection conn) throws DatabaseException {
        this.conn = conn;
        setQuoter();
    }
    
    private synchronized void setQuoter() throws DatabaseException {
        // Enforces the invariant relationship between the database connection
        // and the quoter 
        try {
            if (conn != null && ! conn.isClosed()) {
                this.quoter = SQLIdentifiers.createQuoter(conn.getMetaData());
            } else {
                this.quoter = null;
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
        
    }
    
    synchronized Connection getConnection() {
        return conn;
    }

    synchronized Quoter getQuoter() {
        return quoter;
    }

    synchronized void validateConnection() throws DatabaseException {
        try {
            // A connection only needs to be validated if it already exists.
            // We're trying to see if something went wrong to an existing connection...
            if (conn == null) {
                return;
            }

            if (conn.isClosed()) {
                conn = null;
                throw new DatabaseException(NbBundle.getMessage(ConnectionProcessor.class, "MSG_ConnectionLost"));
            }

            // Send a command to the server, if it fails we know the connection is invalid.
            conn.getMetaData().getTables(null, null, " ", new String[] { "TABLE" }).close();
        } catch (SQLException e) {
            conn = null;
            LOGGER.log(Level.FINE, null, e);
            throw new DatabaseException(NbBundle.getMessage(ConnectionProcessor.class, "MSG_ConnectionLost"), e);
        } finally {
            setQuoter();
        }
    }
    
    synchronized boolean isConnected() {
        return conn != null;
    }

    boolean isConnProcessorThread() {
        return Thread.currentThread().equals(taskThreadRef.get());
    }
    
    public ConnectionProcessor(BlockingQueue<Runnable> inqueue) {
        this.inqueue = inqueue;
    } 
    
    public void run() {
        if (taskThreadRef.getAndSet(Thread.currentThread()) != null) {
            throw new IllegalStateException("Run method called more than once on connection command processor");
        }
        for ( ; ; ) {
            try {              
                Runnable command = inqueue.take();
                
                command.run();                
            } catch ( InterruptedException ie ) {
                LOGGER.log(Level.INFO, null, ie);
                return;
            }
        }
    }    
}
