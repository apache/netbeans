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

/**
 * Represents response returned from server after command execution.
 * <p>
 * Inspired by ActionReport class from module GF Admin Rest Service.
 * In our case the interface allows just read-only access.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface ActionReport {

    /** Possible exit codes that are sent by server. */
    public enum ExitCode { SUCCESS, WARNING, FAILURE, NA };

    /**
     * Get command execution exit code.
     * <p>
     * @return exit code of the called operation
     */
    public ExitCode getExitCode();

    /**
     * Getter for message included in server response.
     * <p>
     * @return message
     */
    public String getMessage();

    /**
     * Getter for command name (description).
     * <p>
     * @return command name
     */
    public String getCommand();

}
