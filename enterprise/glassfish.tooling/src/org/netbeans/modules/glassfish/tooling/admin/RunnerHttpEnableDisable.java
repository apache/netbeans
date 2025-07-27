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
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.utils.Utils;

/**
 * GlassFish Server <code>enable</code> and <code>disable</code>
 * administration command execution using HTTP interface.
 * <p/>
 * Contains common code for enable and disable commands. Individual child
 * classes are not needed at this stage.
 * Class implements GlassFish server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpEnableDisable extends RunnerHttp {

    // Class attributes                                                       //
    /** Enable/Disable command <code>DEFAULT</code> param name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /** Enable/Disable command <code>target</code> param name. */
    private static final String TARGET_PARAM = "target";

    // Static methods                                                         //
    /**
     * Builds enable/disable query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;name&gt;
     *                ['&' "target" '=' &lt;target&gt; ]</code>
     * <p/>
     * @param command GlassFish Server Admin Command Entity.
     *                <code>CommandDisable</code> instance is expected.
     * @return Enable/Disable query string for given command.
     */
    private static String query(final Command command) {
        String target;
        String name;
        if (command instanceof CommandTargetName) {
            target = Utils.sanitizeName(((CommandTargetName)command).target);
            if (((CommandTargetName)command).name == null) {
                throw new CommandException(CommandException.ILLEGAL_NULL_VALUE);
            }
            name = Utils.sanitizeName(((CommandTargetName)command).name);
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        StringBuilder sb = new StringBuilder(
                DEFAULT_PARAM.length() + 1 + name.length() + (
                    target != null
                        ? 1 + TARGET_PARAM.length() + 1 + target.length()
                        : 0
                ));
        sb.append(DEFAULT_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(name);
        if (target != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(TARGET_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(target);
        }
        return sb.toString();
    }

    // Constructors                                                           //
    /**
     * Constructs an instance of admin command executor using HTTP interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish Server Admin Command Entity.
     */
    public RunnerHttpEnableDisable(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
