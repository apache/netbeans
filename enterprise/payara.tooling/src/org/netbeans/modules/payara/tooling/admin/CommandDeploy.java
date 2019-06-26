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

import org.netbeans.modules.payara.tooling.TaskStateListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Server Deploy Command Entity.
 * <p>
 * Holds data for command. Objects of this class are created by API user.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpDeploy.class)
@RunnerRestClass(runner=RunnerRestDeploy.class)
public class CommandDeploy extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for deploy command. */
    private static final String COMMAND = "deploy";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE
            = "Application deployment failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Deploy task that deploys application on server.
     * <p/>
     * @param server      Payara server entity.
     * @param application File object representing archive or directory
     *                    to be deployed.
     * @param listener    Command execution events listener.
     * @return  Deploy task response.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString deploy(PayaraServer server, File application,
            TaskStateListener listener) throws PayaraIdeException {
        Command command = new CommandDeploy(null, null, application,
                null, null, null);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command, listener);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** File to deploy. */
    final File path;

    /** Deployed application context root. */
    final String contextRoot;

    /** Deployment properties. */
    final Map<String, String> properties;

    /** Deployment libraries. */
    final File[] libraries;

    /** Is this deployment of a directory? */
    final boolean dirDeploy;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server deploy command entity.
     * <p/>
     * @param name        Name of module/cluster/instance to modify.
     * @param target      Target Payara instance or cluster where
     *                    <code>name</code> is stored.
     * @param path        File to deploy.
     * @param contextRoot Deployed application context root.
     * @param properties  Deployment properties.
     * @param libraries   Not used in actual deploy command.
     */
    public CommandDeploy(final String name, final String target,
            final File path, final String contextRoot,
            final Map<String,String> properties, final File[] libraries) {
        super(COMMAND, name, target);
        this.path = path;
        this.contextRoot = contextRoot;
        this.properties = properties;
        this.libraries = libraries;
        this.dirDeploy = path.isDirectory();
    }

}
