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
package org.netbeans.modules.web.javascript.debugger.eval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.debug.NamesTranslator;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ViewScope;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ScopedRemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Evaluates expressions with results caching.
 */
@DebuggerServiceRegistration(path="javascript-debuggerengine", types=EvaluatorService.class)
public class EvaluatorServiceImpl implements EvaluatorService, Debugger.Listener {
    
    private static final ScopedRemoteObject NULL_SCOPED_REMOTE_OBJECT = 
            new ScopedRemoteObject(null, null, ViewScope.DEFAULT);
    private static final Logger LOG = Logger.getLogger(EvaluatorServiceImpl.class.getName());
    
    private final Debugger debugger;
    private final ProjectContext pc;
    private final Map<String, ScopedRemoteObject> expressionsCache = 
            new HashMap<String, ScopedRemoteObject>();
    
    public EvaluatorServiceImpl(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        pc = contextProvider.lookupFirst(null, ProjectContext.class);
        debugger.addListener(this);
    }

    private static String getKey(CallFrame frame, String expression) {
        return frame.getCallFrameID() +" - "+ expression;
    }
    
    @Override
    public ScopedRemoteObject evaluateExpression(CallFrame frame, String expression,
                                                 boolean doCacheResults) {
        if (doCacheResults) {
            synchronized (expressionsCache) {
                ScopedRemoteObject var = expressionsCache.get(getKey(frame, expression));
                //System.err.println("WatchesModel.evaluateExpression("+getKey(frame, expression)+"), cached var = "+var);
                if (var != null) {
                    if (NULL_SCOPED_REMOTE_OBJECT == var) {
                        return null;
                    }
                    return var;
                }
            }
        }
        String rtExpression = expression;
        VarNamesTranslatorFactory vtf = VarNamesTranslatorFactory.get(frame, debugger, pc.getProject());
        NamesTranslator namesTranslator = vtf.getNamesTranslator();
        if (namesTranslator != null) {
            rtExpression = namesTranslator.reverseTranslate(expression);
        }
        RemoteObject prop = frame.evaluate(rtExpression);
        ScopedRemoteObject var;
        if (prop != null) {
            var = new ScopedRemoteObject(prop, expression, ViewScope.LOCAL);
        } else {
            LOG.log(Level.WARNING, "expression was not evaluated: '"+expression+"'");
            var = null;
        }
        if (doCacheResults) {
            synchronized (expressionsCache) {
                if (var != null) {
                    expressionsCache.put(getKey(frame, expression), var);
                } else {
                    expressionsCache.put(getKey(frame, expression), NULL_SCOPED_REMOTE_OBJECT);
                }
            }
        }
        return var;
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {}

    @Override
    public void resumed() {
        cleanCache();
    }

    @Override
    public void reset() {
        cleanCache();
    }

    @Override
    public void enabled(boolean enabled) {
        cleanCache();
    }
    
    private void cleanCache() {
        synchronized (expressionsCache) {
            expressionsCache.clear();
        }
        
    }
    
}
