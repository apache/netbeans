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
