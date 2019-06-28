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

import java.io.File;
import java.util.Map;

/**
 * Payara Server Redeploy Command Entity.
 * <p>
 * Holds data for command. Objects of this class are created by API user.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpRedeploy.class)
@RunnerRestClass(runner=RunnerRestDeploy.class)
public class CommandRedeploy extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for deploy command. */
    private static final String COMMAND = "redeploy";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Deployed application context root. */
    final String contextRoot;

    /** Deployment properties. */
    final Map<String,String> properties;

    /** Deployment libraries. */
    final File[] libraries;

    /** Keep state. */
    final boolean keepState;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server deploy command entity.
     * <p/>
     * @param name        Name of module/cluster/instance to modify.
     * @param target      Target Payara instance or cluster where
     *                    <code>name</code> is stored.
     * @param contextRoot Deployed application context root.
     * @param properties  Deployment properties.
     * @param libraries   Deployment libraries.
     * @param keepState   Keep state.
     */
    public CommandRedeploy(final String name, final String target,
            final String contextRoot, final Map<String,String> properties,
            final File[] libraries, final boolean keepState) {
        super(COMMAND, name, target);
        this.contextRoot = contextRoot;
        this.properties = properties;
        this.libraries = libraries;
        this.keepState = keepState;
    }

}
