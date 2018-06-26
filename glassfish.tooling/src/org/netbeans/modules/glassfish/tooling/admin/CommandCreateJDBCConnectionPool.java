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
 * Command that creates JDBC connection pool on server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateJDBCConnectionPool.class)
@RunnerRestClass(runner=RunnerRestCreateJDBCConnectionPool.class)
public class CommandCreateJDBCConnectionPool extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create JDBC connection pool command. */
    private static final String COMMAND = "create-jdbc-connection-pool";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE
            = "Create JDBC connection pool failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * create JDBC connection pool.
     * <p/>
     * @param server GlassFish server entity.
     * @param connectionPoolId    Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC data
     *                            source resource manager.
     * @param resType             Resource type.
     * @param properties          Optional properties for configuring the pool.
     * @return Create JDBC connection pool task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString createJDBCConnectionPool(
            final GlassFishServer server, final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties) throws GlassFishIdeException {
        Command command = new CommandCreateJDBCConnectionPool(connectionPoolId,
                dataSourceClassName, resType, properties);
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
     * Create JDBC connection pool.
     * <p/>
     * @param server GlassFish server entity.
     * @param connectionPoolId    Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC data
     *                            source resource manager.
     * @param resType             Resource type.
     * @param properties          Optional properties for configuring the pool.
     * @param timeout             Administration command execution timeout [ms].
     * @return Create JDBC connection pool task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString createJDBCConnectionPool(
            final GlassFishServer server, final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties, final long timeout)
            throws GlassFishIdeException {
        Command command = new CommandCreateJDBCConnectionPool(connectionPoolId,
                dataSourceClassName, resType, properties);
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

    /** The name of the vendor-supplied JDBC data source resource manager. */
    final String dataSourceClassName;

    /** Resource type. */
    final String resType;

    /** Optional properties for configuring the pool.
     * <p/><table>
     * <tr><td><b>user</b></td><td>Specifies the user name for connecting to
     * the database.</td></tr>
     * <tr><td><b>password</b></td><td>Specifies the password for connecting
     * to the database.</td></tr>
     * <tr><td><b>databaseName</b></td><td>Specifies the database for this
     * connection pool.</td></tr>
     * <tr><td><b>serverName</b></td><td>Specifies the database server for
     * this connection pool.</td></tr>
     * <tr><td><b>port</b></td><td>Specifies the port on which the database
     * server listens for requests.</td></tr>
     * <tr><td><b>networkProtocol</b></td><td>Specifies the communication
     * protocol.</td></tr>
     * <tr><td><b>roleName</b></td><td>Specifies the initial SQL role
     * name.</td></tr>
     * <tr><td><b>datasourceName</b></td><td>Specifies an underlying
     * XADataSource, or a ConnectionPoolDataSource if connection pooling
     * is done.</td></tr>
     * <tr><td><b>description</b></td><td>Specifies a text
     * description.</td></tr>
     * <tr><td><b>url</b></td><td>Specifies the URL for this connection pool.
     * Although this is not a standard property, it is commonly used.</td></tr>
     * <tr><td><b>dynamic-reconfiguration-wait-timeout-in-seconds</b></td>
     * <td>Used to enable dynamic reconfiguration of the connection pool
     * transparently to the applications that are using the pool, so that
     * applications need not be re-enabled for the attribute or property changes
     * to the pool to take effect. Any in-flight transaction's connection
     * requests will be allowed to complete with the old pool configuration
     * as long as the connection requests are within the timeout period,
     * so as to complete the transaction. New connection requests will wait
     * for the pool reconfiguration to complete and connections will be acquired
     * using the modified pool configuration.</td></tr>
     * <tr><td><b>LazyConnectionEnlistment</b></td><td><i>Deprecated.</i>
     * Use the equivalent attribute. The default value is false.</td></tr>
     * <tr><td><b>LazyConnectionAssociation</b></td><td>Deprecated.</i> Use
     * the equivalent attribute. The default value is false.</td></tr>
     * <tr><td><b>AssociateWithThread</b></td><td><i>Deprecated.</i> Use
     * the equivalent attribute. The default value is false.</td></tr>
     * <tr><td><b>MatchConnections</b></td><td><i>Deprecated.</i> Use
     * the equivalent attribute. The default value is true.</td></tr>
     * <tr><td><b>Prefer-Validate-Over-Recreate</b></td><td>Specifies whether
     * pool resizer should validate idle connections before destroying and
     * recreating them. The default value is true.</td></tr>
     * <tr><td><b>time-to-keep-queries-in-minutes</b></td><td>Specifies
     * the number of minutes that will be cached for use in calculating
     * frequently used queries. Takes effect when SQL tracing and monitoring
     * are enabled for the JDBC connection pool. The default value is
     * 5 minutes.</td></tr>
     * <tr><td><b>number-of-top-queries-to-report</b></td><td>Specifies
     * the number of queries to list when reporting the top and most frequently
     * used queries. Takes effect when SQL tracing and monitoring are enabled
     * for the JDBC connection pool. The default value is 10 queries.</td></tr>
     * </table> */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create JDBC connection pool
     * command entity.
     * <p/>
     * @param connectionPoolId    Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC
     *                            data source resource manager.
     * @param resType             Resource type.
     * @param properties          Optional properties for configuring the pool.
     */
    public CommandCreateJDBCConnectionPool(final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties) {
        super(COMMAND);
        this.connectionPoolId = connectionPoolId;
        this.dataSourceClassName = dataSourceClassName;
        this.resType = resType;
        this.properties = properties;
    }

}
