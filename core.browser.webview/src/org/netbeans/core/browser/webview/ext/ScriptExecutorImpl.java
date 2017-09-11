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
