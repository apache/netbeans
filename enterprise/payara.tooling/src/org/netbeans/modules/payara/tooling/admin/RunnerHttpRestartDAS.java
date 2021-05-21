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

import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara restart DAS administration command with
 * <code></code> query execution using HTTP interface.
 * <p/>
 * Contains code for command that is called with
 * <code>debug=true|false&force=true|false&kill=true|false</code> query string.
 * <p/>
 * Class implements Payara server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpRestartDAS extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Restart DAS command <code>debug</code> parameter's name. */
    private static final String DEBUG_PARAM = "debug";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds restart DAS query string for given command.
     * <p/>
     * <code>debug=true|false&force=true|false&kill=true|false</code>
     * <p/>
     * @param command Payara Server Administration Command Entity.
     *                <code>CommandRestartDAS</code> instance is expected.
     * @return Restart DAS query string for given command.
     */
    static String query(final Command command) {
        if (command instanceof CommandRestartDAS) {
            boolean debug = ((CommandRestartDAS)command).debug;
            int boolValSize = FALSE_VALUE.length() > TRUE_VALUE.length()
                    ? FALSE_VALUE.length() : TRUE_VALUE.length();
            StringBuilder sb = new StringBuilder(DEBUG_PARAM.length()
                    + boolValSize + 1);
            sb.append(DEBUG_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(debug ? TRUE_VALUE : FALSE_VALUE);
            return sb.toString();
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
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
    public RunnerHttpRestartDAS(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
