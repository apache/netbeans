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

package org.netbeans.modules.groovy.editor.completion.inference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;

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
    public static ClassNode findCallerType(@NonNull ASTNode expression, @NonNull AstPath path, BaseDocument baseDocument, int offset) {
        // In case if the method call is chained with another method call
        // For example: someInteger.toString().^
        if (expression instanceof MethodCallExpression) {
            MethodCallExpression methodCall = (MethodCallExpression) expression;
            Object o = expression.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
            if (o instanceof MethodNode) {
                MethodNode mn = (MethodNode)o;
                if (mn.getReturnType() != null && mn.getReturnType().isResolved()) {
                    return mn.getReturnType();
                }
            }
            ClassNode callerType = findCallerType(methodCall.getObjectExpression(), path, baseDocument, offset);
            if (callerType != null) {
                return findReturnTypeFor(callerType.redirect(), methodCall.getMethodAsString(), methodCall.getArguments(), path, false, baseDocument, offset);
            }
        }

        // In case if the method call is directly on a variable
        if (expression instanceof VariableExpression) {
            int newOffset = ASTUtils.getOffset(baseDocument, expression.getLineNumber(), expression.getColumnNumber());
            AstPath newPath = new AstPath(path.root(), newOffset, baseDocument);
            TypeInferenceVisitor tiv = new TypeInferenceVisitor(((ModuleNode)path.root()).getContext(), newPath, baseDocument, newOffset);
            tiv.collect();
            return tiv.getGuessedType();

            }
        if (expression instanceof ConstantExpression) {
            return ((ConstantExpression) expression).getType();
        }
        if (expression instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression)expression;
            ClassNode cn = ce.getType();
            if (cn != null && cn.isResolved()) {
                cn = cn.redirect();
            }
            return cn.isResolved() ? cn :
                   // note: this is just a stabu
                   ClassHelper.make(((ClassExpression) expression).getType().getName());
        }

        if (expression instanceof StaticMethodCallExpression) {
            StaticMethodCallExpression staticMethodCall = (StaticMethodCallExpression) expression;

            return findReturnTypeFor(staticMethodCall.getOwnerType().redirect(), staticMethodCall.getMethod(), staticMethodCall.getArguments(), path, true, baseDocument, offset);
        }
        return null;
    }
    
    @CheckForNull
    private static ClassNode findReturnTypeFor(
            @NonNull ClassNode callerType, 
            @NonNull String methodName,
            @NonNull Expression arguments,
            @NonNull AstPath path,
            @NonNull boolean isStatic,
            @NonNull BaseDocument baseDocument,
            @NonNull int offset
            ) {

        List<ClassNode> paramTypes = new ArrayList<>();
        ArgumentListExpression argExpression = null;
        if (arguments instanceof ArgumentListExpression) {
            argExpression = (ArgumentListExpression) arguments;
            for (Expression e : argExpression.getExpressions()) {
                ClassNode cn = GroovyUtils.findInferredType(e);
                if (cn != null && cn.isResolved()) {
                    paramTypes.add(cn);
                    continue;
                }
                if (e instanceof VariableExpression) {
                    ModuleNode moduleNode = (ModuleNode) path.root();
                    int newOffset = ASTUtils.getOffset(baseDocument, e.getLineNumber(), e.getColumnNumber());
                    AstPath newPath = new AstPath(moduleNode, newOffset, baseDocument);
                    TypeInferenceVisitor tiv = new TypeInferenceVisitor(moduleNode.getContext(), newPath, baseDocument, newOffset);
                    tiv.collect();
                    ClassNode guessedType = tiv.getGuessedType();
                    if (null == guessedType) {
                        System.out.println("Bad guessed type");
                    } else {
                        paramTypes.add(tiv.getGuessedType());
                    }
                } else if(e instanceof ConstantExpression) {
                    paramTypes.add(((ConstantExpression)e).getType());
                } else if (e instanceof MethodCallExpression) {
                    paramTypes.add(findCallerType(e, path, baseDocument, offset));
                } else if (e instanceof BinaryExpression) {
                    BinaryExpression binExpression = (BinaryExpression) e;
                    paramTypes.add(binExpression.getType());
                } else if (e instanceof ClassExpression) {
                    ClassExpression classExpression = (ClassExpression) e;
                    // This should be Class<classExpression.getType()>
                    paramTypes.add(GenericsUtils.makeClassSafeWithGenerics(Class.class, classExpression.getType()));
                } else {
                    System.out.println(e.getClass());
                }
            }
        }
        
        MethodNode possibleMethod = tryFindPossibleMethod(callerType, methodName, paramTypes, isStatic, argExpression);
        if (possibleMethod != null) {
            return possibleMethod.getReturnType();
        }
        return null;
    }

    private static MethodNode tryFindPossibleMethod(ClassNode callerType, String methodName, List<ClassNode> paramTypes, boolean isStatic, ArgumentListExpression paramExpr) {
        int count = paramTypes.size();

        MethodNode res = null;
        ClassNode node = callerType;
        Queue<ClassNode> tq = new ArrayDeque<>();
        tq.add(callerType.redirect());
        while ((node = tq.poll()) != null) {
            for (ClassNode in : node.getInterfaces()) {
                // search also in interfaces
                tq.add(in.redirect());
            }
            for (MethodNode method : node.getMethods(methodName)) {
                if (isStatic && !method.isStatic()) {
                    continue;
                }
                if (method.getParameters().length == count) {
                    boolean match = true;
                    for (int i = 0; i != count; ++i) {
                        if (!paramTypes.get(i).isDerivedFrom(method.getParameters()[i].getType())) {
                            // do a thorough test in addition to plain type-equals.
                            if (!StaticTypeCheckingSupport.checkCompatibleAssignmentTypes(
                                    method.getParameters()[i].getType(),
                                    paramTypes.get(i),
                                    paramExpr.getExpression(i))) {

                                match = false;
                                break;
                            }
                        }
                    }

                    if (match) {
                        if (res == null) {
                            res = method;
                        } else {
                            if (res.getParameters().length != count) {
                                return null;
                            }
                            if (node.equals(callerType)) {
                                return null;
                            }

                            match = true;
                            for (int i = 0; i != count; ++i) {
                                if (!res.getParameters()[i].getType().equals(method.getParameters()[i].getType())) {
                                    match = false;
                                    break;
                                }
                            }
                            if (!match) {
                                return null;
                            }
                        }
                    }
                }
            }
            node = node.getSuperClass();
            if (node != null) {
                tq.add(node.redirect());
            }
        };

        return res;
    }
}
