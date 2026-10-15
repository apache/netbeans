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

package org.netbeans.modules.db.runtime;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * This class managers the list of registered database runtimes. Database runtimes
 * encapsulate instances of a database server which can be automatically started 
 * by the IDE when a connection is being made to this server.
 *
 * @see org.netbeans.spi.db.explorer.DatabaseRuntime
 *
 * @author Nam Nguyen, Andrei Badea
 */
public final class DatabaseRuntimeManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseRuntimeManager.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    /**
     * The path where the runtimes are registered in the SystemFileSystem.
     */
    private static final String RUNTIMES_PATH = "Databases/Runtimes"; // NOI18N
    
    /**
     * The singleton database runtime manager instance.
     */
    private static DatabaseRuntimeManager DEFAULT = null;
    
    /**
     * The Lookup.Result instance containing all the DatabaseRuntime instances.
     */
    private Lookup.Result<DatabaseRuntime> result = getLookupResult();

    /**
     * Returns the singleton database runtime manager instance.
     */
    public static synchronized DatabaseRuntimeManager getDefault() {
        if (DEFAULT == null) {
            LOGGER.finest("Instantiated DatabaseRuntimeManager.");
            DEFAULT = new DatabaseRuntimeManager();
        }
        return DEFAULT;
    }
    
    public DatabaseRuntime[] getRuntimes() {
        Collection<? extends DatabaseRuntime> runtimes = result.allInstances();
        return runtimes.toArray(new DatabaseRuntime[0]);
    }
    
    public static synchronized boolean isInstantiated() {
        LOGGER.finest("Is DatabaseRuntimeManager instantiated? " + (DEFAULT != null));
        return DEFAULT != null;
    }

    /**
     * Returns the runtimes registered for the specified JDBC driver.
     *
     * @param jdbcDriverClassName the JDBC driver to search for; must not be null.
     *
     * @return the runtime registered for the specified JDBC driver or null
     *         if no runtime is registered for this driver.
     *
     * @throws NullPointerException if the specified JDBC driver is null.
     */
    public DatabaseRuntime[] getRuntimes(String jdbcDriverClassName) {
        if (jdbcDriverClassName == null) {
            throw new NullPointerException();
        }
        List<DatabaseRuntime> runtimeList = new LinkedList<DatabaseRuntime>();
        for (DatabaseRuntime runtime : result.allInstances()) {
            if (LOG) {
                LOGGER.log(Level.FINE, "Runtime: " + runtime.getClass().getName() + " for driver " + runtime.getJDBCDriverClass()); // NOI18N
            }
            if (jdbcDriverClassName.equals(runtime.getJDBCDriverClass())) {
                runtimeList.add(runtime);
            }
        }
        return runtimeList.toArray(new DatabaseRuntime[0]);
    }
    
    private synchronized Lookup.Result<DatabaseRuntime> getLookupResult() {
        return Lookups.forPath(RUNTIMES_PATH).lookupResult(DatabaseRuntime.class);
    }
}
