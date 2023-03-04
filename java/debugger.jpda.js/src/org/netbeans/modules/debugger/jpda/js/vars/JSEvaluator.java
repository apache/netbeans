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

package org.netbeans.modules.debugger.jpda.js.vars;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.Evaluator;

/**
 *
 * @author Martin
 */
@Evaluator.Registration(language=JSUtils.JS_STRATUM)
public class JSEvaluator implements Evaluator<JSExpression> {

    private final JPDADebugger debugger;

    public JSEvaluator (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst (null, JPDADebugger.class);
    }

    @Override
    public Result evaluate(Expression<JSExpression> expression, Context context) throws InvalidExpressionException {
        ObjectVariable contextVariable = context.getContextVariable();
        if (contextVariable != null) {
            // String value of the context variable is 
            if ("toString()".equals(expression.getExpression())) {              // NOI18N
                return new Result(DebuggerSupport.getVarStringValueAsVar(debugger, contextVariable));
            }
        }
        JSExpression expr = expression.getPreprocessedObject();
        if (expr == null) {
            expr = JSExpression.parse(expression.getExpression());
            expression.setPreprocessedObject(expr);
        }
        Variable ret = evaluateIn(expr, context.getCallStackFrame(), contextVariable);
        return new Result(ret);
    }

    private Variable evaluateIn(JSExpression expr, CallStackFrame callStackFrame, ObjectVariable contextVar) throws InvalidExpressionException {
        return DebuggerSupport.evaluate(debugger, callStackFrame, expr.getExpression(), contextVar);
    }
    
}
