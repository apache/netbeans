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

import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.RunnerHttp;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara cloud administration command execution using HTTP interface.
 * <p/>
 * HTTP interface is not supported with cloud commands.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCloud extends RunnerHttp {

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * This constructor prototype is called from factory class and should
     * remain public in all child classes.
     * <p/>
     * @param server  Payara cloud entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttpCloud(final PayaraServer server,
            final CommandCloud command) {
        super(server, command, "/command/cloud/", null);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param query   Query string for this command.
     */
    RunnerHttpCloud(final PayaraServer server, final Command command,
            final String query) {
        super(server, command, "/command/cloud/", query);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param path    Path which builds URL we speak to.
     * @param query   Query string for this command.
     */
    RunnerHttpCloud(final PayaraServer server, final Command command,
            final String path, final String query) {
        super(server, command, path, query);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

}
