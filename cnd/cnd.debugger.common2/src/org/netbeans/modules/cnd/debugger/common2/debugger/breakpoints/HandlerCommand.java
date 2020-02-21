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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

/**
 * Holds the command string created by HandlerExpert or any 
 * error condition.
 *
 * We used to use plain String's for that but in time we needed to pass
 * error stuff, so the String became a HandlerCommand.
 */

public class HandlerCommand {
    private final String data;
    private final boolean error;

    /**
     * Create a HandlerCommand which is not an error.
     */
    public static HandlerCommand makeCommand(String command) {
	return new HandlerCommand(command, false);
    }

    /**
     * Create a HandlerCommand which is an error.
     */
    public static HandlerCommand makeError(String error) {
	return new HandlerCommand(error, true);
    }

    protected HandlerCommand(String data, boolean error) {
	this.data = data;
	this.error = error;
    }

    public boolean isError() {
	return error;
    }

    public String getData() {
	return data;
    }
}
