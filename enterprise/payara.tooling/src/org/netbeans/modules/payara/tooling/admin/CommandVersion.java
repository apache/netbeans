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
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Server Version Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass
@RunnerRestClass
public class CommandVersion extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandVersion.class);

    /** Command string for version command. */
    private static final String COMMAND = "version";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve version from server.
     * <p/>
     * @param server Payara server entity.
     * @return Payara command result containing version string returned
     *         by server.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString getVersion(final PayaraServer server)
            throws PayaraIdeException {
        final String METHOD = "getVersion";
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, new CommandVersion());
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new CommandException(LOGGER.excMsg(METHOD, "exception"),
                    ee.getLocalizedMessage());
        }
    }

    /**
     * Retrieve version from server.
     * <p/>
     * @param server Payara server entity.
     * @return Payara command result containing {@link PayaraVersion}
     *         object retrieved from server or <code>null</code> if no
     *         version was returned.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static PayaraVersion getPayaraVersion(
            final PayaraServer server) {
        ResultString result;
        try {
            result = getVersion(server);
        } catch (CommandException ce) {
            return null;
        }
        String value = result != null
                ? ServerUtils.getVersionString(result.getValue()) : null;
        if (value != null) {
            return PayaraVersion.toValue(value);
        } else {
            return null;
        }
    }

    /**
     * Verifies if domain directory returned by version command result matches
     * domain directory of provided Payara server entity.
     * <p/>
     * @param result Version command result.
     * @param server Payara server entity.
     * @return For local server value of <code>true</code> means that server
     *         major and minor version value matches values returned by version
     *         command and value of <code>false</code> that they differs.
     */
    public static boolean verifyResult(
            final ResultString result, final PayaraServer server) {
        boolean verifyResult = false;
        String value = ServerUtils.getVersionString(result.getValue());
        if (value != null) {
            PayaraVersion valueVersion = PayaraVersion.toValue(value);
            PayaraVersion serverVersion = server.getVersion();
            if (valueVersion != null && serverVersion != null) {
                verifyResult = serverVersion.equals(valueVersion);
            }
        }
        return verifyResult;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server version command entity.
     */
    public CommandVersion() {
        super(COMMAND);
    }

}
