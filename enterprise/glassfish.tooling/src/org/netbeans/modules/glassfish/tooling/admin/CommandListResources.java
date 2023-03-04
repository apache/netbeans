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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command that retrieves list of JDBC resources defined on server.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpListResources.class)
@RunnerRestClass(runner=RunnerRestListResources.class)
public class CommandListResources extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandListResources.class);

    /** Command string prefix used to construct list JDBC resources
     *  HTTP command. */
    private static final String COMMAND_PREFIX = "list-";

    /** Command string suffix used to construct list JDBC resources
     *  HTTP command. */
    private static final String COMMAND_SUFFIX = "s";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add resource to target server.
     * <p/>
     * @param server    GlassFish server entity.
     * @param cmdSuffix Resource command suffix. Value should not be null.
     * @param target              GlassFish server target.
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultList<String> listResources(final GlassFishServer server,
            final String cmdSuffix, final String target)
            throws GlassFishIdeException {
        final String METHOD = "listResources";
        Command command = new CommandListResources(command(cmdSuffix), target);
        Future<ResultList<String>> future =
                ServerAdmin.<ResultList<String>>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Constructs command string for provided resource command suffix.
     * <p/>
     * @param resourceCmdSuffix Resource command suffix. Value should not
     *                          be null.
     */
    public static String command(String resourceCmdSuffix) {
        StringBuilder sb = new StringBuilder(COMMAND_PREFIX.length()
                + COMMAND_SUFFIX.length() + resourceCmdSuffix.length());
        sb.append(COMMAND_PREFIX);
        sb.append(resourceCmdSuffix);
        sb.append(COMMAND_SUFFIX);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server list JDBC resources
     * command entity.
     * <p/>
     * Command string is supplied as an argument.
     * <p/>
     * @param command Server command represented by this object. Use
     *                <code>command</code> static method to build this string
     *                using resource command suffix.
     * @param target  Target GlassFish instance or cluster.
     */
    public CommandListResources(final String command, final String target) {
        super(command, target);
    }
}
