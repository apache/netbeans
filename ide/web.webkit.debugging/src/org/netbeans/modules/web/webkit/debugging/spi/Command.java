/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.webkit.debugging.spi;

import org.json.simple.JSONObject;

/**
 * WebKit command. Class automatically assigns unique ID to each command.
 */
public final class Command {
    
    private static int uniqueCommandID = 0;
    
    public static final String COMMAND_ID = "id";
    public static final String COMMAND_METHOD = "method";
    public static final String COMMAND_PARAMS = "params";
    public static final String COMMAND_RESULT = "result";
    
    private JSONObject command;

    public Command(String method) {
        this(method, null);
    }

    @SuppressWarnings("unchecked")    
    public Command(String method, JSONObject params) {
        command = new JSONObject();
        command.put(COMMAND_ID, createUniqueID());
        command.put(COMMAND_METHOD, method);
        if (params != null && params.size() > 0) {
            command.put(COMMAND_PARAMS, params);
        }
    }
    
    private static synchronized int createUniqueID() {
        return uniqueCommandID++;
    }
    
    public int getID() {
        Object o = command.get(COMMAND_ID);
        if (o == null) {
            return -1;
        }
        assert o instanceof Number;
        return ((Number)o).intValue();
    }

    public JSONObject getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command.toJSONString();
    }

}
