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
package org.netbeans.modules.payara.tooling.admin;

import java.util.Map;

/**
 * Command that creates a pool of connections to an enterprise information
 * system (EIS).
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateConnectorConnectionPool.class)
@RunnerRestClass(runner = RunnerRestCreateConnectorPool.class)
public class CommandCreateConnectorConnectionPool extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create EIS connection pool command. */
    private static final String COMMAND = "create-connector-connection-pool";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Connection pool unique name (and ID). */
    final String poolName;

    /** The name of the resource adapter. */
    final String raName;

    /** The name of the connection definition. */
    final String connectionDefinition;

    /** Optional properties for configuring the pool.
     * <p/>
     * <table>
     * <tr><td><b>LazyConnectionEnlistment</b></td><td><i>Deprecated.</i> Use
     * the equivalent option. Default value is false.</td></tr>
     * <tr><td><b>LazyConnectionAssociation</b></td><td><i>Deprecated.</i> Use
     * the equivalent option. Default value is false.</td></tr>
     * <tr><td><b>AssociateWithThread</b></td><td><i>Deprecated.</i> Use
     * the equivalent option. Default value is false.</td></tr>
     * <tr><td><b>MatchConnections</b></td><td><i>Deprecated.</i> Use
     * the equivalent option. Default value is false.</td></tr>
     * </table> */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server create EIS connection pool
     * command entity.
     * <p/>
     * @param poolName             Connection pool unique name (and ID).
     * @param raName               The name of the resource adapter.
     * @param connectionDefinition The name of the connection definition.
     * @param properties           Optional properties for configuring the resource.
     */
    public CommandCreateConnectorConnectionPool(final String poolName,
            final String raName, final String connectionDefinition,
            final Map<String, String> properties) {
        super(COMMAND);
        this.poolName = poolName;
        this.raName = raName;
        this.connectionDefinition = connectionDefinition;
        this.properties = properties;
    }

}
