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
package org.netbeans.core.browser.webview.ext;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.browser.spi.ScriptExecutor;

/**
 * Script executor for web-browser pane based on {@code WebView}.
 *
 * @author Jan Stola
 */
public class ScriptExecutorImpl implements ScriptExecutor {
    /** Web-browser tab this executor belongs to. */
    private WebBrowserImpl browserTab;

    /**
     * Creates a new {@code ScriptExecutorImpl}.
     * 
     * @param browserTab web-browser tab this executor belongs to.
     */
    ScriptExecutorImpl(WebBrowserImpl browserTab) {
        this.browserTab = browserTab; 
    }

    @Override
    public Object execute(String script) {
        StringBuilder sb = new StringBuilder();
        // Callback for scripts that want to send some message back to page-inspection.
        // We utilize custom alert handling of WebEngine for this purpose.
        sb.append("postMessageToNetBeans=function(e) {alert('"); // NOI18N
        sb.append(WebBrowserImpl.PAGE_INSPECTION_PREFIX);
        sb.append("'+JSON.stringify(e));};\n"); // NOI18N
        String quoted = '\"'+JSONValue.escape(script)+'\"';
        // We don't want to depend on what is the type of WebBrowser.executeJavaScript()
        // for various types of script results => we stringify the result
        // (i.e. pass strings only through executeJavaScript()). We decode
        // the strigified result then.
        sb.append("JSON.stringify({result : eval(").append(quoted).append(")});"); // NOI18N
        String wrappedScript = sb.toString();
        Object result = browserTab.executeJavaScript(wrappedScript);
        String txtResult = result.toString();
        try {
            JSONObject jsonResult = (JSONObject)JSONValue.parseWithException(txtResult);
            return jsonResult.get("result"); // NOI18N
        } catch (ParseException ex) {
            Logger.getLogger(ScriptExecutorImpl.class.getName()).log(Level.INFO, null, ex);
            return ScriptExecutor.ERROR_RESULT;
        }
    }
    
}
