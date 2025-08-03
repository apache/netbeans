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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara server stop DAS command entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass
@RunnerRestClass(runner = RunnerRestStopDAS.class)
public class CommandStopDAS extends Command {

    // Class attributes                                                       //
    /** Command string for version command. */
    private static final String COMMAND = "stop-domain";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE = "DAS stop failed.";

    // Static methods                                                         //
    /**
     * Stops running DAS server.
     * <p/>
     * @param server Payara server entity.
     * @return Stop DAS task response.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString stopDAS(final PayaraServer server) throws
            PayaraIdeException {
        Command command = new CommandStopDAS();
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    // Constructors                                                           //
    /**
     * Constructs an instance of Payara server version command entity.
     */
    public CommandStopDAS() {
        super(COMMAND);
    }

}
