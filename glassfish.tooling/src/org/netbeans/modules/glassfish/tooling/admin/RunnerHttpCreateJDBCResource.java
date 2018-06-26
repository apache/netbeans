/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * GlassFish server create JDBC resource administration command execution
 * using HTTP interface.
 * <p/>
 * Contains code for create JDBC resource command.
 * Class implements GlassFish server administration functionality trough HTTP
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
     * @param command GlassFish server administration command entity.
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
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpCreateJDBCResource(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
