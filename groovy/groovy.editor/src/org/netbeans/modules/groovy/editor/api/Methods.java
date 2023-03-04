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

package org.netbeans.modules.groovy.editor.api;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;

/**
 * Utilities related to methods
 * 
 * @author Martin Adamek
 */
public class Methods {

    public static boolean isSameMethod(ExecutableElement javaMethod, MethodCallExpression methodCall) {
        ConstantExpression methodName = (ConstantExpression) methodCall.getMethod();
        if (javaMethod.getSimpleName().contentEquals(methodName.getText())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (getParameterCount(methodCall) == javaMethod.getParameters().size()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameMethod(MethodNode methodNode, MethodCallExpression methodCall) {
        if (methodNode.getName().equals(methodCall.getMethodAsString())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (getParameterCount(methodCall) == methodNode.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameMethod(MethodNode methodNode1, MethodNode methodNode2) {
        if (methodNode1.getName().equals(methodNode2.getName())) {
            Parameter[] params1 = methodNode1.getParameters();
            Parameter[] params2 = methodNode2.getParameters();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    ClassNode type1 = params1[i].getType();
                    ClassNode type2 = params2[i].getType();
                    if (!type1.equals(type2)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isSameMethod(MethodCallExpression methodCall1, MethodCallExpression methodCall2) {
        String method1 = methodCall1.getMethodAsString();
        if (method1 != null && method1.equals(methodCall2.getMethodAsString())) {
            int size1 = getParameterCount(methodCall1);
            int size2 = getParameterCount(methodCall2);
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (size1 >= 0 && size1 == size2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstructor(ConstructorNode constructor, ConstructorCallExpression call) {
        if (constructor.getDeclaringClass().getNameWithoutPackage().equals(call.getType().getNameWithoutPackage())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (getParameterCount(call) == constructor.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstuctor(ConstructorCallExpression call1, ConstructorCallExpression call2) {
        String constructor1 = call1.getType().getNameWithoutPackage();
        if (constructor1 != null && constructor1.equals(call2.getType().getNameWithoutPackage())) {
            int size1 = getParameterCount(call1);
            int size2 = getParameterCount(call2);
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (size1 >= 0 && size1 == size2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstructor(ConstructorNode constructor1, ConstructorNode constructor2) {
        return isSameMethod(constructor1, constructor2);
    }

    /**
     * Tries to calculate number of method parameters.
     *
     * @param methodCall called method
     * @return number of method parameters,
     * 1 in case of named parameters represented by map,
     * or -1 if it is unknown
     */
    private static int getParameterCount(MethodCallExpression methodCall) {
        Expression expression = methodCall.getArguments();
        if (expression instanceof ArgumentListExpression) {
            return ((ArgumentListExpression) expression).getExpressions().size();
        } else if (expression instanceof NamedArgumentListExpression) {
            // this is in fact map acting as named parameters
            // lets return size 1
            return 1;
        } else {
            return -1;
        }
    }
    
    private static int getParameterCount(ConstructorCallExpression constructorCall) {
        Expression expression = constructorCall.getArguments();
        if (expression instanceof ArgumentListExpression) {
            return ((ArgumentListExpression) expression).getExpressions().size();
        } else if (expression instanceof NamedArgumentListExpression) {
            // this is in fact map acting as named parameters
            // lets return size 1
            return 1;
        } else {
            return -1;
        }
    }

    public static boolean hasSameParameters(IndexedMethod indexedMethod, MethodNode method) {
        return isSameList(getMethodParams(indexedMethod), getMethodParams(method));
    }

    public static boolean hasSameParameters(IndexedMethod indexedMethod, MethodCallExpression methodCall) {
        return isSameList(getMethodParams(indexedMethod), getMethodParams(methodCall));
    }

    public static boolean hasSameParameters(MethodNode methodNode, MethodCallExpression methodCall) {
        return isSameList(getMethodParams(methodNode), getMethodParams(methodCall));
    }

    public static boolean isSameList(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<String> getMethodParams(IndexedMethod indexedMethod) {
        List<String> paramTypes = new ArrayList<>();

        if (indexedMethod != null) {
            List<MethodParameter> parameters = indexedMethod.getParameters();

            for (MethodParameter param : parameters) {
                paramTypes.add(param.getType());
            }
        }
        return paramTypes;
    }

    private static List<String> getMethodParams(MethodNode methodNode) {
        final List<String> params = new ArrayList<>();
        for (Parameter param : methodNode.getParameters()) {
            params.add(ElementUtils.getTypeName(param));
        }
        return params;

    }

    private static List<String> getMethodParams(MethodCallExpression methodCall) {
        final List<String> params = new ArrayList<>();
        final Expression arguments = methodCall.getArguments();

        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentList = ((ArgumentListExpression) arguments);
            if (argumentList.getExpressions().size() > 0) {
                for (Expression argument : argumentList.getExpressions()) {
                    params.add(ElementUtils.getTypeName(argument.getType()));
                }
            }
        }
        return params;
    }
}
