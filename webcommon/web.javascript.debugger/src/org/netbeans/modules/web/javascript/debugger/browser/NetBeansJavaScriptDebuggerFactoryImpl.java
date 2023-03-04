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
package org.netbeans.modules.web.javascript.debugger.browser;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.javascript.debugger.DebuggerConstants;
import org.netbeans.modules.web.javascript.debugger.EngineDestructorProvider;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.JavaScriptDebuggerFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=JavaScriptDebuggerFactory.class)
public class NetBeansJavaScriptDebuggerFactoryImpl implements JavaScriptDebuggerFactory {

    @Override
    public Session createDebuggingSession(WebKitDebugging webkit, Lookup projectContext) {
        Debugger debugger = webkit.getDebugger();
        ProjectContext pc = new ProjectContext(projectContext);
        EngineDestructorProvider edp = new EngineDestructorProvider();
        
        DebuggerInfo di = DebuggerInfo.create(DebuggerConstants.DEBUGGER_INFO,
                new Object[]{webkit, debugger, pc, edp});
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().startDebugging(di)[0];
        Session session = engine.lookupFirst(null, Session.class);
        return session;
    }

    @Override
    public void stopDebuggingSession(Session session) {
        DebuggerEngine engine = session.lookupFirst(null, DebuggerEngine.class);
        if (engine == null) {
            return ; // No engine, nothing to stop.
        }
        Debugger debugger = engine.lookupFirst(null, Debugger.class);
        if ((debugger != null) && debugger.isEnabled()) {
            debugger.disable();
        }
        session.kill();
        engine.lookupFirst(null, EngineDestructorProvider.class).getDestructor().killEngine();
        MiscEditorUtil.unregisterSourceMapsTranslator(debugger);
    }

}
