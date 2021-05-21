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
package org.netbeans.modules.payara.tooling.admin.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of the response returned by REST administration service.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RestActionReport implements ActionReport {

    /** Top part of the message, can be the only one.*/
    MessagePart topMessagePart = new MessagePart();
    /** Nested reports.*/
    List<? extends ActionReport> subActions = new ArrayList<>();
    /** Exit code returned by server.*/
    ExitCode exitCode = ActionReport.ExitCode.NA;
    /** Description of command which is the report related to.*/
    String actionDescription;

    public List<? extends ActionReport> getSubActionsReport() {
        return subActions;
    }

    @Override
    public ExitCode getExitCode() {
        return exitCode;
    }

    @Override
    public String getMessage() {
        return topMessagePart.getMessage();
    }

    @Override
    public String getCommand() {
        return actionDescription;
    }

    void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    void setMessage(String message) {
        topMessagePart.setMessage(message);
    }

    public boolean isSuccess() {
        return getExitCode().equals(ExitCode.SUCCESS);
    }

    public MessagePart getTopMessagePart() {
        return topMessagePart;
    }
    
}
