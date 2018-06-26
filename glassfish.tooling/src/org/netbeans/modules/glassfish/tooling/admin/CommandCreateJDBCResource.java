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

import java.util.Map;
import java.util.concurrent.*;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * Command that creates JDBC resource on server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateJDBCResource.class)
@RunnerRestClass(runner = RunnerRestCreateJDBCResource.class)
public class CommandCreateJDBCResource extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create JDBC resource command. */
    private static final String COMMAND = "create-jdbc-resource";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE
            = "Create JDBC resource failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JDBC resource.
     * <p/>
     * @param server GlassFish server entity.
     * @param connectionPoolId    Connection pool unique name (and ID).
     * @param jndiName            The JNDI name of this JDBC resource.
     * @param target              Helps specify the target to which you
     *                            are deploying.
     * @param properties          Optional properties for configuring the pool.
     * @return Create JDBC connection pool task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString createJDBCResource(
            final GlassFishServer server, final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties) throws GlassFishIdeException {
        Command command = new CommandCreateJDBCResource(connectionPoolId,
                jndiName, target, properties);
        Future<ResultString> future = ServerAdmin
                .<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    /**
     * Create JDBC resource.
     * <p/>
     * @param server GlassFish server entity.
     * @param connectionPoolId    Connection pool unique name (and ID).
     * @param jndiName            The JNDI name of this JDBC resource.
     * @param target              Helps specify the target to which you
     *                            are deploying.
     * @param properties          Optional properties for configuring the pool.
     * @param timeout             Administration command execution timeout [ms].
     * @return Create JDBC resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString createJDBCResource(
            final GlassFishServer server, final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties, final long timeout)
            throws GlassFishIdeException {
        Command command = new CommandCreateJDBCResource(connectionPoolId,
                jndiName, target, properties);
        Future<ResultString> future = ServerAdmin
                .<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(
                    ERROR_MESSAGE + " in " + timeout + "ms", te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Connection pool unique name (and ID). */
    final String connectionPoolId;

    /** The JNDI name of this JDBC resource. */
    final String jndiName;

    /** Helps specify the target to which you are deploying.
     * <p/>
     * Valid values are:<br/><table>
     * <tr><td><b>server</b></td><td>Deploys the component to the default server
     * instance. This is the default value.</td></tr>
     * <tr><td><b>domain</b></td><td>Deploys the component to
     * the domain.</td></tr>
     * <tr><td><b>cluster_name</b></td><td>Deploys the component to every server
     * instance in the cluster.</td></tr>
     * <tr><td><b>instance_name</b></td><td>Deploys the component to
     * a particular sever instance.</td></tr></table> */
    final String target;
    
    /** Optional properties for configuring the resource. */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create JDBC resource
     * command entity.
     * <p/>
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param jndiName         The JNDI name of this JDBC resource.
     * @param target           Specify the target to which you are deploying.
     * @param properties       Optional properties for configuring the resource.
     */
    public CommandCreateJDBCResource(final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties) {
        super(COMMAND);
        this.connectionPoolId = connectionPoolId;
        this.jndiName = jndiName;
        this.target = target;
        this.properties = properties;
    }

}
