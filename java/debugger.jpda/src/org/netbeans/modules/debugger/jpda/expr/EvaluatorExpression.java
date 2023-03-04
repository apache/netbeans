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

package org.netbeans.modules.debugger.jpda.expr;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.JavaEvaluator;
import org.netbeans.spi.debugger.jpda.Evaluator;

/**
 *
 * @author Martin Entlicher
 */
public final class EvaluatorExpression implements CompilationInfoHolder {

    private final String expression;
    private final Map<Evaluator, AssociatedExpression<?>> associatedExpressions = new HashMap<Evaluator, AssociatedExpression<?>>();
    private Object parsedData;

    public EvaluatorExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public Evaluator.Result evaluate(Evaluator<?> e, Evaluator.Context context) throws InvalidExpressionException {
        AssociatedExpression<?> ae = associatedExpressions.get(e);
        if (ae == null) {
            ae = new AssociatedExpression(e, expression);
            associatedExpressions.put(e, ae);
        }
        if (e instanceof JavaEvaluator) {
            return ((JavaEvaluator) e).evaluate((Evaluator.Expression<JavaExpression>) ae.expr,
                                                context, this);
        }
        return ae.evaluate(context);
    }

    @Override
    public Object getParsedData() {
        return parsedData;
    }

    @Override
    public void setParsedData(Object parsedData) {
        this.parsedData = parsedData;
    }

    private static class AssociatedExpression<PI> {
        private final Evaluator<PI> e;
        private final Evaluator.Expression<PI> expr;

        AssociatedExpression(Evaluator<PI> e, String expression) {
            this.e = e;
            this.expr = new Evaluator.Expression<>(expression);
        }

        public Evaluator.Result evaluate(Evaluator.Context context) throws InvalidExpressionException {
            return e.evaluate(expr, context);
        }
        
    }

}
