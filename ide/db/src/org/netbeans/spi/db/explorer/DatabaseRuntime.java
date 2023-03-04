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

package org.netbeans.spi.db.explorer;

/**
 * Represents an instance of a database server, typically a local installation which can be
 * started when a connection to this server is being made, or stopped upon IDE shutdown.
 *
 * <p>Implementations of this class should be put in the Databases/Runtimes folder
 * in the default filesystem.</p>
 *
 * @author Nam Nguyen, Andrei Badea
 */
public interface DatabaseRuntime {

    /**
     * Returns the JDBC driver class which is used to make connections to the
     * represented database server instance.
     *
     * <p>When a connection is being made, only the database runtimes which have
     * the same JDBC driver as the driver used by this connection are considered 
     * for further usage (e.g., starting the database server instance).</p>
     * 
     * @return the fully-qualified class name of the driver used to make
     * connections to the represented database server instance.
     */
    public String getJDBCDriverClass();
    
    /**
     * Returns whether this runtime accepts this database URL (the database URL
     * would cause a connection to be made to the database server instance 
     * represented by this runtime).
     *
     * @param url the database URL
     * 
     * @return true if the runtime accepts this database URL; false otherwise.
     */
    boolean acceptsDatabaseURL(String url);
    
    /**
     * Returns the state (running/not running) of the represented database server
     * instance.
     *
     * @return true if the database server instance is running; false otherwise.
     */
    boolean isRunning();
    
    /**
     * Returns whether the database server instance can be started by a call to the 
     * {@link #start} method.
     *
     * @return true if the database server instance can be started; false
     * otherwise.
     */
    public boolean canStart();

    /**
     * Starts the database server instance represented by this runtime.
     */
    void start();
    
    /**
     * Stops the database server instance represented by this runtime.
     */
    void stop();
}
