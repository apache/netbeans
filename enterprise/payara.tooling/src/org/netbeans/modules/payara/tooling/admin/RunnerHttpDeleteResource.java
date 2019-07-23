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

import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpDeleteResource extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(RunnerHttpDeleteResource.class);

    /** Deploy command <code>DEFAULT</code> parameter name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /**
     * Creates query string from command object properties.
     * <p/>
     * @param command Payara server administration command entity.
     * @return Query string from command object properties.
     */
    private static String query(CommandDeleteResource command) {
        StringBuilder query = new StringBuilder(128);
        query.append(DEFAULT_PARAM);
        query.append('=');
        query.append(command.name);
        if (null != command.target) {
            query.append(PARAM_SEPARATOR);
            query.append("target=");
            query.append(command.target);
        }
        return query.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttpDeleteResource(final PayaraServer server,
            final Command command) {
        super(server, command, query((CommandDeleteResource)command));
    }
}
