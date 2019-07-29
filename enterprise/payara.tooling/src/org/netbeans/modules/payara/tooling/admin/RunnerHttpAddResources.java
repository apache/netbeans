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

import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Runner executes add-resources command via HTTP interface.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpAddResources extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds add-resources query string for given command.
     * <p/>
     * @param command Payara server administration command entity.
     *                <code>CommandAddResources</code> instance is expected.
     * @return Add-resources query string for given command.
     */
    private static String query(Command command) {
        CommandAddResources cmd = (CommandAddResources) command;
        StringBuilder sb = new StringBuilder();
        sb.append("xml_file_name=");
        sb.append(cmd.xmlResFile.getAbsolutePath());
        if (cmd.target != null) {
            sb.append("&target=");
            sb.append(cmd.target);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttpAddResources(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }
}
