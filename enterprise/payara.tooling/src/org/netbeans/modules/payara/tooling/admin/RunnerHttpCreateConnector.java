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
 * Payara server create connector resource administration command execution
 * using HTTP interface.
 * <p/>
 * Contains code for create connector resource command.
 * Class implements Payara server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateConnector extends RunnerHttp {


    /** Create connector resource command <code>jndi_name</code>
     *  parameter name. */
    private static final String JNDI_NAME_PARAM = "jndi_name";

    /** Create connector resource command <code>poolname</code>
     *  parameter name. */
    private static final String POOL_NAME_PARAM = "poolname";

    /** Create connector resource command <code>property</code>
     *  parameter name. */
    private static final String PROPERTY_PARAM = "property";

    /** Create connector resource command <code>enabled</code>
     *  parameter name. */
    private static final String ENABLED_PARAM = "enabled";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds create connector resource query string for given command.
     * <p/>
     * <code>QUERY :: "jndi_name" '=' &lt;jndiName&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "poolname" '=' &lt;poolName&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     * { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "enabled" '=' true|false ]<br/>
     * <p/>
     * @param command Payara server administration command entity.
     *                <code>CommandCreateConnector</code> instance
     *                is expected.
     * @return Create connector resource query string for given command.
     */
    private static String query(final Command command) {
        String jndiName;
        String poolName;
        boolean enabled;
        if (command instanceof CommandCreateConnector) {
            jndiName = ((CommandCreateConnector)command).jndiName;
            poolName = ((CommandCreateConnector)command).poolName;
            enabled = ((CommandCreateConnector)command).enabled;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        boolean isPoolName = poolName != null && poolName.length() > 0;
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                JNDI_NAME_PARAM.length() + 1 + jndiName.length()
                + ENABLED_PARAM.length() + 1 + toString(enabled).length()
                + ( isPoolName
                        ? POOL_NAME_PARAM.length() + 1 + poolName.length()
                        : 0)
                + queryPropertiesLength(
                        ((CommandCreateConnector)command).properties,
                        PROPERTY_PARAM));
        // Build query string
        sb.append(JNDI_NAME_PARAM).append(PARAM_ASSIGN_VALUE);
        sb.append(jndiName);
        sb.append(PARAM_SEPARATOR).append(ENABLED_PARAM);
        sb.append(PARAM_ASSIGN_VALUE).append(toString(enabled));
        if (isPoolName) {
             sb.append(PARAM_SEPARATOR).append(POOL_NAME_PARAM);
             sb.append(PARAM_ASSIGN_VALUE).append(poolName);
        }
        queryPropertiesAppend(sb,
                ((CommandCreateConnector)command).properties,
                PROPERTY_PARAM, true);
        return sb.toString();
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
    public RunnerHttpCreateConnector(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }
}
