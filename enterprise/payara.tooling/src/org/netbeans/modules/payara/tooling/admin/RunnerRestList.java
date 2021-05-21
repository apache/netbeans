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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.payara.tooling.admin.response.MessagePart;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Command runner for commands that retrieve some kind of list.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestList extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Result object - contains list of JDBC resources names.*/
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultList<String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerRestList(final PayaraServer server, final Command command) {
        super(server, command);
    }   

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultList</code> object corresponding
     * to server log command execution value to be returned.
     */
    @Override
    protected ResultList<String> createResult() {
        return result = new ResultList<>();
    }

    @Override
    protected boolean processResponse() {
        List<MessagePart> childMessages = report.getTopMessagePart().getChildren();
        if ((childMessages != null) && !childMessages.isEmpty()) {
            result.value = new ArrayList<>(childMessages.size());
            for (MessagePart msg : childMessages) {
                result.getValue().add(msg.getMessage());
            }
        }
        return true;
    }


}
