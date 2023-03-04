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
package org.netbeans.modules.web.webkit.debugging.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;

/**
 * See Runtime section of WebKit Remote Debugging Protocol for more details.
 */
public class Runtime {
    
    private final TransportHelper transport;
    private final WebKitDebugging webkit;

    private static final Logger LOG = Logger.getLogger( 
            Runtime.class.getCanonicalName());
    
    Runtime(TransportHelper transport, WebKitDebugging webkit) {
        this.transport = transport;
        this.webkit = webkit;
    }
    
    @SuppressWarnings("unchecked")    
    public List<PropertyDescriptor> getRemoteObjectProperties(RemoteObject remoteObject, boolean ownProperties) {
        List<PropertyDescriptor> res = new ArrayList<PropertyDescriptor>();
        JSONObject params = new JSONObject();
        params.put("ownProperties", ownProperties); // NOI18N
        params.put("objectId", remoteObject.getObjectID()); // NOI18N
        Response properties = transport.sendBlockingCommand(new Command("Runtime.getProperties", params));
        if (properties == null) {
            LOG.log(Level.WARNING, "no response from command: "+"Runtime.getProperties "+params.toJSONString() + // NOI18N
                    ", remote object was: "+remoteObject.toString() + " and owning property :"+remoteObject.getOwningProperty()); // NOI18N
            return Collections.emptyList();
        }
        if (properties.getException() != null) {
            LOG.log(Level.WARNING, "transport exception from command: "+"Runtime.getProperties "+params.toJSONString() + // NOI18N
                    ", remote object was: "+remoteObject.toString() + " and owning property :"+remoteObject.getOwningProperty()); // NOI18N
            return Collections.emptyList();
        }
        JSONObject response = properties.getResponse(); // NOI18N
        JSONObject result = (JSONObject)response.get("result"); // NOI18N
        if (result == null) {
            LOG.log(Level.WARNING, "no result in response: "+response.toJSONString() + // NOI18N
                    ". the command was: "+"Runtime.getProperties "+params.toJSONString() + // NOI18N
                    " and remote object was: "+remoteObject.toString() + " and owning property :"+remoteObject.getOwningProperty()); // NOI18N
            return Collections.emptyList();
        }
        JSONArray array = (JSONArray)result.get("result"); // NOI18N
        for (Object o : array) {
            res.add(APIFactory.createPropertyDescriptor((JSONObject)o, webkit));
        }
        return res;
    }

    /**
     * Evaluates the given expression on the global object.
     * 
     * @param expression expression to evaluate.
     * @return result of the expression.
     */
    public RemoteObject evaluate(String expression) {
        RemoteObject remoteObject = null;
        JSONObject params = new JSONObject();
        params.put("expression", expression); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("Runtime.evaluate", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject expressionResult = (JSONObject)result.get("result"); // NOI18N
                remoteObject = new RemoteObject(expressionResult, webkit);
            }
        }
        return remoteObject;
    }

    /**
     * Executes the given script. This method is an asynchronous (i.e. non-blocking)
     * variant of the method {@code evaluate(String)}.
     * 
     * @param script script to execute.
     */
    public void execute(String script) {
        JSONObject params = new JSONObject();
        params.put("expression", script); // NOI18N
        transport.sendCommand(new Command("Runtime.evaluate", params)); // NOI18N
    }

    /**
     * Calls function with given declaration on the given object. The object
     * group of the result is inherited from the target object.
     * 
     * @param object object to call the function on.
     * @param functionDeclaration declaration of the function to call.
     * @return return value of the invoked function.
     */
    public RemoteObject callFunctionOn(RemoteObject object, String functionDeclaration) {
        RemoteObject remoteObject = null;
        JSONObject params = new JSONObject();
        params.put("objectId", object.getObjectID()); // NOI18N
        params.put("functionDeclaration", functionDeclaration); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("Runtime.callFunctionOn", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject expressionResult = (JSONObject)result.get("result"); // NOI18N
                remoteObject = new RemoteObject(expressionResult, webkit);
            }
        }
        return remoteObject;
    }

    /**
     * Calls the procedure with the given declaration on the given object.
     * It is a non-blocking/asynchronous (i.e. it doesn't wait until
     * the execution is finished) variant of {@code callFunctionOn()} method.
     * 
     * @param object object to call the function on.
     * @param procedureDeclaration declaration of the procedure to call.
     */
    public void callProcedureOn(RemoteObject object, String procedureDeclaration) {
        JSONObject params = new JSONObject();
        params.put("objectId", object.getObjectID()); // NOI18N
        params.put("functionDeclaration", procedureDeclaration); // NOI18N
        transport.sendCommand(new Command("Runtime.callFunctionOn", params)); // NOI18N
    }

    /**
     * Releases the given remote object.
     * 
     * @param object remote object to release.
     */
    public void releaseObject(RemoteObject object) {
        String objectId = object.getObjectID();
        if (objectId != null) {
            JSONObject params = new JSONObject();
            params.put("objectId", objectId); // NOI18N
            transport.sendCommand(new Command("Runtime.releaseObject", params)); // NOI18N
        }
    }

    @SuppressWarnings("unchecked")    
    void releaseNetBeansObjectGroup() {
        JSONObject params = new JSONObject();
        params.put("objectGroup", TransportHelper.OBJECT_GROUP_NAME);
        transport.sendCommand(new Command("Runtime.releaseObjectGroup", params));
    }
}
