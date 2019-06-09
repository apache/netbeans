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
package org.netbeans.modules.payara.tooling.admin.cloud;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * This class provides convenience methods for working with cloud (CPAS server).
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CloudTasks {

    /**
     * Deploy task that deploys application on server.
     *
     * @param server - server to deploy on
     * @param account - which account the application is deployed under
     * @param application - File object representing archive or directory where
     * the application is
     * @param listener - listener, that listens to command execution events
     * @return result object with task status and message
     */
    public static ResultString deploy(final PayaraServer server,
            final String account, final File application,
            final TaskStateListener listener) {
        Command command = new CommandCloudDeploy(account, application);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command, listener);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PayaraIdeException(
                    "Instance or cluster stop failed.", e);
        }
    }
}
