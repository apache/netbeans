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
import java.net.URI;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Locations command used to determine locations (installation, domain etc.)
 * where the DAS is running.
 * <p/>
 * Result of the command will be in the form of <code>Map<String, String></code>
 * object. The keys to particular locations are as followed:
 * Installation root - "Base-Root_value"
 * Domain root - "Domain-Root_value"
 * <p/>
 * Minimal <code>__locations</code> command support exists since Payara
 * 3.0.1 where both Base-Root and Domain-Root values are returned.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpLocation.class)
@RunnerRestClass(runner=RunnerRestLocation.class)
public class CommandLocation extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for location command. */
    private static final String COMMAND = "__locations";

    /** Result key to retrieve <code>Domain-Root</code> value. */
    public static final String DOMAIN_ROOT_RESULT_KEY = "Domain-Root_value";

    /** Result key to retrieve <code>Basic-Root</code> value. */
    public static final String BASIC_ROOT_RESULT_KEY = "Base-Root_value";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verifies if domain directory returned by location command result matches
     * domain directory of provided Payara server entity.
     * <p/>
     * @param resultMap Locations command result.
     * @param server    Payara server entity.
     * @return For local server value of <code>true</code> means that domain
     *         directory returned by location command result matches domain
     *         directory of provided Payara server entity and value
     *         of <code>false</code> that they differs. For remote serve this
     *         test makes no sense and value of <code>true</code> is always
     *         returned.
     */
    public static boolean verifyResult(
            final ResultMap<String, String> resultMap,
            final PayaraServer server) {
        if (!server.isRemote()) {
            boolean result = false;
            String domainRootResult
                    = resultMap.getValue().get(DOMAIN_ROOT_RESULT_KEY);
            String domainRootServer = ServerUtils.getDomainPath(server);
            if (domainRootResult != null && domainRootServer != null) {
                URI rootResult = new File(domainRootResult).toURI().normalize();
                URI rootServer = new File(domainRootServer).toURI().normalize();
                if (rootResult != null && rootServer != null) {
                    result = rootServer.equals(rootResult);
                } 
            }
            return result;
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server location command entity.
     */
    public CommandLocation() {
        super(COMMAND);
    }
    
}
