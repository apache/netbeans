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

import java.text.MessageFormat;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpSetProperty extends RunnerHttp {

    /**
     * Creates query string from command object properties.
     * <p/>
     * @param command Payara server administration command entity.
     * @return Query string from command object properties.
     */
    private static String query(CommandSetProperty command) {
        return MessageFormat.format(
                command.format, command.property, command.value);
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
    public RunnerHttpSetProperty(final PayaraServer server,
            final Command command) {
        super(server, command, query((CommandSetProperty)command));
    }

    
}
