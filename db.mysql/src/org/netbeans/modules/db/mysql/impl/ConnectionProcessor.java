/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

    private final AtomicReference<Thread> taskThreadRef = new AtomicReference<Thread>();;

    synchronized void setConnection(Connection conn) throws DatabaseException {
        this.conn = conn;
        setQuoter();
    }
    
    synchronized private void setQuoter() throws DatabaseException {
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
