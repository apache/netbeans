/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * Command that retrieves list of components defined on server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpListComponents.class)
@RunnerRestClass(runner=RunnerRestListApplications.class, command="list-applications")
public class CommandListComponents extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for list components command. */
    private static final String COMMAND = "list-components";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE = "List components failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * List components.
     * <p/>
     * @param server GlassFish server entity.
     * @param target Target server instance or cluster.
     * @return List components task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultMap<String, List<String>> listComponents(
            final GlassFishServer server, final String target)
            throws GlassFishIdeException {
        Command command = new CommandListComponents(target);
        Future<ResultMap<String, List<String>>> future = ServerAdmin
                .<ResultMap<String, List<String>>>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server list components
     * command entity.
     * <p/>
     * @param target  Target GlassFish instance or cluster.
     */
    public CommandListComponents(final String target) {
        super(COMMAND, target);
    }

}
