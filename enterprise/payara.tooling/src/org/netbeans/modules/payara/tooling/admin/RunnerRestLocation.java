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

import java.util.HashMap;
import java.util.Properties;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestLocation extends RunnerRest {

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandLocation command;

    /** Returned value is map where locations are stored under keys specified in
     * CommandLocation class.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

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
    public RunnerRestLocation(final PayaraServer server,
            final Command command) {
        super(server, command);
        this.command = (CommandLocation)command;
    }

    @Override
    protected Result createResult() {
        return result = new ResultMap<String, String>();
    }

    @Override
    protected boolean processResponse() {
        if (report == null) {
            return false;
        }
        Properties props = report.getTopMessagePart().getProperties();
        result.value = new HashMap<String, String>(props.size());
        for (String key : props.stringPropertyNames()) {
            result.value.put(key, props.getProperty(key));
        }
        return true;
    }
    
}
