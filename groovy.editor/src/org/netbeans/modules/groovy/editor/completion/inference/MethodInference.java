/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion.inference;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Martin Janicek
 */
public final class MethodInference {

    private MethodInference() {
    }
    
    /**
     * Tries to infer correct {@link ClassNode} representing type of the caller for
     * the given expression. Typically the given parameter is instance of {@link MethodCallExpression}
     * and in that case the return type of the method call is returned.<br/><br/>
     * 
     * The method also handles method chain and in such case the return type of the
     * last method call should be return.
     * 
     * @param expression
     * @return class type of the caller if found, {@code null} otherwise
     */
    @CheckForNull
    public static ClassNode findCallerType(@NonNull ASTNode expression) {
        // In case if the method call is chained with another method call
        // For example: someInteger.toString().^
        if (expression instanceof MethodCallExpression) {
            MethodCallExpression methodCall = (MethodCallExpression) expression;
            
            ClassNode callerType = findCallerType(methodCall.getObjectExpression());
            if (callerType != null) {
                return findReturnTypeFor(callerType, methodCall.getMethodAsString(), methodCall.getArguments());
            }
        }

        // In case if the method call is directly on a variable
        if (expression instanceof VariableExpression) {
            Variable variable = ((VariableExpression) expression).getAccessedVariable();
            if (variable != null) {
                return variable.getType();
            }
        }
        return null;
    }
    
    @CheckForNull
    private static ClassNode findReturnTypeFor(
            @NonNull ClassNode callerType, 
            @NonNull String methodName,
            @NonNull Expression arguments) {
        
        MethodNode possibleMethod = callerType.tryFindPossibleMethod(methodName, arguments);
        if (possibleMethod != null) {
            return possibleMethod.getReturnType();
        }
        return null;
    }
}
