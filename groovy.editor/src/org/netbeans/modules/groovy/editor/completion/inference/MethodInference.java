/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
