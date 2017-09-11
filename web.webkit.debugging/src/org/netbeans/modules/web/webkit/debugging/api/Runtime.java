/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
