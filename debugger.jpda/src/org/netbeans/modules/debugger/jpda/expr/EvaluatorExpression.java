/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
