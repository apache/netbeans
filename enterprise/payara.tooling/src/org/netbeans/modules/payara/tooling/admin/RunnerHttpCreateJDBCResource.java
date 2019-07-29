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
 * Payara server create JDBC resource administration command execution
 * using HTTP interface.
 * <p/>
 * Contains code for create JDBC resource command.
 * Class implements Payara server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateJDBCResource extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Create JDBC connection pool command <code>connectionpoolid</code>
     *  parameter name. */
    private static final String CONN_POOL_ID_PARAM = "connectionpoolid";

    /** Create JDBC connection pool command <code>jndi_name</code>
     *  parameter name. */
    private static final String JNDI_NAME_PARAM="jndi_name";

    /** Create JDBC connection pool command <code>target</code>
     *  parameter name. */
    private static final String TARGET_PARAM="target";

    /** Create JDBC connection pool command <code>property</code>
     *  parameter name. */
    private static final String PROPERTY_PARAM = "property";

    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds create JDBC resource query string for given command.
     * <p/>
     * <code>QUERY :: "connectionpoolid" '=' &lt;connectionPoolId&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "jndi_name" '=' &lt;jndiName&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "target" '=' &lt;target&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     * { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * <p/>
     * @param command Payara server administration command entity.
     *                <code>CommandCreateJDBCResource</code> instance
     *                is expected.
     * @return Create JDBC connection pool query string for given command.
     */
    private static String query(final Command command) {
        String connectionPoolId;
        String jndiName;
        String target;
        if (command instanceof CommandCreateJDBCResource) {
            connectionPoolId = ((CommandCreateJDBCResource)command)
                    .connectionPoolId;
            jndiName = ((CommandCreateJDBCResource)command).jndiName;
            target = ((CommandCreateJDBCResource)command).target;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        boolean isJndiName = jndiName != null && jndiName.length() > 0;
        boolean isTarget = target != null && target.length() > 0;
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                CONN_POOL_ID_PARAM.length() + 1 + connectionPoolId.length()
                + ( isJndiName
                        ?  JNDI_NAME_PARAM.length()
                           + 1 + jndiName.length()
                        : 0 )
                + ( isTarget
                        ? TARGET_PARAM.length() + 1 + target.length()
                        : 0)
                + queryPropertiesLength(
                        ((CommandCreateJDBCResource)command).properties,
                        PROPERTY_PARAM));
        // Build query string
        sb.append(CONN_POOL_ID_PARAM).append(PARAM_ASSIGN_VALUE);
        sb.append(connectionPoolId);
        if (isJndiName) {
            sb.append(PARAM_SEPARATOR).append(JNDI_NAME_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(jndiName);            
        }
        if (isTarget) {
            sb.append(PARAM_SEPARATOR).append(TARGET_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(target);            
        }
        queryPropertiesAppend(sb,
                ((CommandCreateJDBCResource)command).properties,
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
    public RunnerHttpCreateJDBCResource(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
