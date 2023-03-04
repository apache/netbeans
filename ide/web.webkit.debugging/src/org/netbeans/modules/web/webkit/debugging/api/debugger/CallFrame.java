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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;

/**
 * See Debugger.CallFrame
 */
public class CallFrame extends AbstractObject {
    
    private TransportHelper transport;
    
    CallFrame(JSONObject frame, WebKitDebugging webkit, TransportHelper transport) {
        super(frame, webkit);
        this.transport = transport;
    }

    public String getFunctionName() {
        return (String)getObject().get("functionName");
    }
    
    public String getCallFrameID() {
        return (String)getObject().get("callFrameId");
    }
    
    public Script getScript() {
        return getWebkit().getDebugger().getScript((String)getLocation().get("scriptId"));
    }
    
    private JSONObject getLocation() {
        return (JSONObject)getObject().get("location");
    }
    public int getLineNumber() {
        return ((Number)getLocation().get("lineNumber")).intValue();
    }
    
    public int getColumnNumber() {
        return ((Number)getLocation().get("columnNumber")).intValue();
    }
    
    public List<Scope> getScopes() {
        List<Scope> l = new ArrayList<Scope>();
        JSONArray array = (JSONArray)getObject().get("scopeChain");
        for (Object o : array) {
            l.add(new Scope((JSONObject)o, getWebkit()));
        }
        return l;
    }

    @SuppressWarnings("unchecked")    
    public RemoteObject evaluate(String expression) {
        JSONObject params = new JSONObject();
        params.put("callFrameId", getCallFrameID());
        params.put("expression", expression);
        params.put("returnByValue", false);
        params.put("objectGroup", TransportHelper.OBJECT_GROUP_NAME);
        boolean includeCommandLineAPI = true;
        do {
            params.put("includeCommandLineAPI", includeCommandLineAPI);
            Response response = transport.sendBlockingCommand(new Command("Debugger.evaluateOnCallFrame", params));
            if (response != null) {
                JSONObject jresponse = response.getResponse();
                if (jresponse != null) {
                    JSONObject result = (JSONObject)jresponse.get("result");
                    if (result != null) {
                        result = (JSONObject) result.get("result");
                        if (result != null) {
                            if (includeCommandLineAPI &&
                                "Error".equals(result.get("className")) &&
                                result.get("description") instanceof String &&
                                ((String) result.get("description")).startsWith(
                                    "SyntaxError: \'with\' statements are not valid")) {
                                
                                includeCommandLineAPI = false;
                                // Try to evaluate it again, whithout command line API
                                continue;
                            }
                            return new RemoteObject(result, getWebkit());
                        }
                    }
                }
            }
            break;
        } while (true);
        return null; // Evaluation failed
    }
}
