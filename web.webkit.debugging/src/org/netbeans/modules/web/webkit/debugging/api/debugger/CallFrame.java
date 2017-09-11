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
