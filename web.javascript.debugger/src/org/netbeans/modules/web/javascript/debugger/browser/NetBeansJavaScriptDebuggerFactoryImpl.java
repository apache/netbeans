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
