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
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;

/**
 * WebKit response.
 */
public final class Response {
    
    private JSONObject response;
    private TransportStateException transportEx;

    public Response(JSONObject response) {
        this(response, null);
    }
    
    public Response(TransportStateException transportEx) {
        this(null, transportEx);
    }

    public Response(JSONObject response, TransportStateException transportEx) {
        this.response = response;
        this.transportEx = transportEx;
    }
    
    public int getID() {
        if (response == null) {
            return -1;
        }
        Object o = response.get(Command.COMMAND_ID);
        if (o == null) {
            return -1;
        }
        assert o instanceof Number;
        return ((Number)o).intValue();
    }

    public JSONObject getResult() {
        if (response == null) {
            return null;
        }
        return (JSONObject)response.get(Command.COMMAND_RESULT);
    }

    public String getMethod() {
        if (response == null) {
            return null;
        }
        return (String)response.get(Command.COMMAND_METHOD);
    }

    public JSONObject getParams() {
        if (response == null) {
            return null;
        }
        return (JSONObject)response.get(Command.COMMAND_PARAMS);
    }

    public JSONObject getResponse() {
        return response;
    }
    
    public TransportStateException getException() {
        return transportEx;
    }

    @Override
    public String toString() {
        if (response != null) {
            return response.toJSONString();
        } else {
            return transportEx.toString();
        }
    }

    
}
