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
import org.netbeans.modules.glassfish.tooling.utils.Utils;

/**
 * GlassFish Server <code>redeploy</code> Admin Command Execution
 * using HTTP interface.
 * <p/>
 * Class implements GlassFish server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpRedeploy extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Deploy command <code>target</code> parameter name. */
    private static final String TARGET_PARAM = "target";

    /** Deploy command <code>name</code> parameter name. */
    private static final String NAME_PARAM = "name";

    /** Deploy command <code>contextroot</code> parameter name. */
    private static final String CTXROOT_PARAM = "contextroot";

    /** Deploy command <code>properties</code> parameter name. */
    private static final String PROPERTIES_PARAM = "properties";

    /** Deploy command <code>libraries</code> parameter name. */
    private static final String LIBRARIES_PARAM = "libraries";

    /** Deploy command <code>keepState</code> parameter name. */
    private static final String KEEP_STATE_PARAM = "keepState";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds redeploy query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;path&gt; <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "name" '=' &lt;name&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "target" '=' &lt;target&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "contextroot" '=' &lt;contextRoot&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "keepState" '=' true | false ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     *                                                  { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "libraries" '=' &lt;lname&gt; '=' &lt;lvalue&gt;
     *                                                  { ':' &lt;lname&gt; '=' &lt;lvalue&gt;} ]</code>
     * <p/>
     * @param command GlassFish server administration deploy command entity.
     * @return Redeploy query string for given command.
     */
    private static String query(final Command command) {
        String name; 
        String target;
        String ctxRoot;
        String keepState;
        if (command instanceof CommandRedeploy) {
            name = Utils.sanitizeName(((CommandRedeploy)command).name);
            target = ((CommandRedeploy)command).target;
            ctxRoot = ((CommandRedeploy)command).contextRoot;
            keepState = Boolean.toString(((CommandRedeploy)command).keepState);
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        // Calculate StringBuilder initial length to avoid resizing
        boolean first = true;
        StringBuilder sb = new StringBuilder(
                queryPropertiesLength(
                        ((CommandRedeploy)command).properties, PROPERTIES_PARAM)
                + queryLibrariesLength(
                        ((CommandRedeploy)command).libraries, LIBRARIES_PARAM)
                + ( NAME_PARAM.length() + 1 + name.length() )
                + ( target != null
                        ? 1 + TARGET_PARAM.length() + 1 + target.length() : 0 )
                + ( ctxRoot != null && ctxRoot.length() > 0
                        ? 1 + CTXROOT_PARAM.length() + 1 + ctxRoot.length()
                        : 0 )
                + ( ((CommandRedeploy)command).keepState
                        ? KEEP_STATE_PARAM.length() + 1 + keepState.length()
                        : 0 )
                );
        sb.append(NAME_PARAM).append(PARAM_ASSIGN_VALUE).append(name);
        if (target != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(TARGET_PARAM).append(PARAM_ASSIGN_VALUE).append(target);            
        }
        if (ctxRoot != null && ctxRoot.length() > 0) {
            sb.append(PARAM_SEPARATOR);
            sb.append(CTXROOT_PARAM).append(PARAM_ASSIGN_VALUE).append(ctxRoot);
        }
        if (((CommandRedeploy)command).keepState) {
            sb.append(PARAM_SEPARATOR);
            sb.append(KEEP_STATE_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(keepState);
        }
        // Add properties into query string.
        queryPropertiesAppend(sb, ((CommandRedeploy)command).properties,
                PROPERTIES_PARAM, true);
        queryLibrariesAppend(sb, ((CommandRedeploy)command).libraries,
                LIBRARIES_PARAM, true);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandRedeploy command;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpRedeploy(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
        this.command = (CommandRedeploy)command;
    }

}
