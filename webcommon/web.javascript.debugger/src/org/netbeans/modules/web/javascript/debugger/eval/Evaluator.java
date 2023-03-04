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

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;

/**
 * Evaluates expressions with results caching.
 * 
 * @author Martin
 */
public final class Evaluator {
    
    private Evaluator() {}
    
    public static VariablesModel.ScopedRemoteObject evaluateExpression(
            CallFrame frame, String expression, boolean doCacheResults) {
        
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        EvaluatorService evaluator = currentEngine.lookupFirst(null, EvaluatorService.class);
        if (evaluator == null) {
            return null;
        }
        return evaluator.evaluateExpression(frame, expression, doCacheResults);
    }
}
