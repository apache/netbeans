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

import java.io.File;
import java.util.concurrent.*;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command registers resources defined in provided xml file on specified target.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
@RunnerHttpClass(runner=RunnerHttpAddResources.class)
@RunnerRestClass(runner=RunnerRestAddResources.class)
public class CommandAddResources extends CommandTarget {

    // Class attributes                                                       //
    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandAddResources.class);

    /** Command string for create-cluster command. */
    private static final String COMMAND = "add-resources";
    
    // Static methods                                                         //
    /**
     * Add resource to target server.
     * <p/>
     * @param server              GlassFish server entity.
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString addResource(final GlassFishServer server,
            final File xmlResourceFile, final String target)
            throws GlassFishIdeException {
        final String METHOD = "addResource";
        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Add resource to target server.
     * <p/>
     * @param server              GlassFish server entity.
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     * @param timeout             Administration command execution timeout [ms].
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString addResource(final GlassFishServer server,
            final File xmlResourceFile, final String target, final long timeout)
            throws GlassFishIdeException {
        final String METHOD = "addResource";
        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD,
                    "exceptionWithTimeout", Long.toString(timeout)), te);
        }
    }

    // Instance attributes                                                    //
    /** File object pointing to xml file that contains resources to be added. */
    File xmlResFile;

    // Constructors                                                           //
    /**
     * Constructs an instance of GlassFish server add-resources command entity.
     * <p/>
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     */
    public CommandAddResources(
            final File xmlResourceFile, final String target) {
        super(COMMAND, target);
        xmlResFile = xmlResourceFile;
    }
}
