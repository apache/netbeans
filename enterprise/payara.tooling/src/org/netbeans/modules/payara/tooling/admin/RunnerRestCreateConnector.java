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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import org.netbeans.modules.payara.tooling.admin.response.ActionReport.ExitCode;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Runner for create connector command via REST interface.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerRestCreateConnector extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerRestCreateConnector(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        CommandCreateConnector cmd = (CommandCreateConnector)command;
        OutputStreamWriter wr =
                new OutputStreamWriter(hconn.getOutputStream());
        StringBuilder data = new StringBuilder();
        data.append("jndi_name=").append(cmd.jndiName);
        data.append("&enabled=").append(cmd.enabled);
        appendIfNotEmpty(data, "&target", cmd.target);
        appendIfNotEmpty(data, "&poolname", cmd.poolName);
        appendProperties(data, cmd.properties, "property", true);
        wr.write(data.toString());
        wr.close();
    }
    
    /**
     * Overridden because server returns WARNING even when it creates the resource.
     */
    @Override
    protected boolean isSuccess() {
        return report.isSuccess() || report.getExitCode().equals(ExitCode.WARNING);
    }
}
