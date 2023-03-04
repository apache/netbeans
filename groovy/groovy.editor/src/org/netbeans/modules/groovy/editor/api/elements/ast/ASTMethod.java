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
package org.netbeans.modules.groovy.editor.api.elements.ast;

import groovy.lang.MetaMethod;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement;

public class ASTMethod extends ASTElement implements MethodElement {

    private List<MethodParameter> parameters;
    private String returnType;
    private Class clz;
    private MetaMethod method;
    private boolean isGDK;

    
    public ASTMethod(ASTNode node) {
        this(node, null);
    }

    public ASTMethod(ASTNode node, String in) {
        super(node, in);
    }
    
    // We need this variant to drag the Class to which this Method belongs with us.
    // This is used in the CodeCompleter complete/document pair.
    
    public ASTMethod(ASTNode node, Class clz, MetaMethod method, boolean isGDK) {
        super(node);
        this.clz = clz;
        this.method = method;
        this.isGDK = isGDK;
    }

    public boolean isGDK() {
        return isGDK;
    }

    public MetaMethod getMethod() {
        return method;
    }
    
    public Class getClz() {
        return clz;
    }

    @Override
    public List<MethodParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
            if (method != null) {
                for (CachedClass pt : method.getParameterTypes()) {
                    String n = pt.getName();
                    int idx = n.lastIndexOf('$');
                    if (idx > 0) {
                        n = n.substring(idx + 1);
                    } else if ((idx = n.lastIndexOf('.')) > 0) {
                        n = n.substring(idx + 1);
                    }
                    MethodParameter mp = new MethodParameter(pt.getName(), n, null);
                    parameters.add(mp);
                }
            } else {
                for (Parameter parameter : ((MethodNode) node).getParameters()) {
                    String paramName = parameter.getName();
                    String fqnType = parameter.getType().getName();
                    String type = ASTUtils.getSimpleName(parameter.getType());

                    parameters.add(new MethodParameter(fqnType, type, paramName));
                }
            }
        }
        return parameters;
    }

    @Override
    public List<String> getParameterTypes() {
        List<String> paramTypes = new ArrayList<>();

        for (MethodParameter parameter : getParameters()) {
            paramTypes.add(parameter.getType());
        }
        return paramTypes;
    }
    
    @Override
    public String getSignature() {
        if (signature == null) {
            if (method != null) {
                String s = method.getSignature();
                int sp = s.indexOf(' ');
                signature = sp == -1 ? s : s.substring(sp + 1).trim();
            } else {
                StringBuilder builder = new StringBuilder(super.getSignature());
                List<MethodParameter> params = getParameters();
                if (params.size() > 0) {
                    builder.append("("); // NOI18N
                    for (MethodParameter parameter : params) {
                        builder.append(parameter.getFqnType());
                        builder.append(","); // NOI18N
                    }
                    builder.setLength(builder.length() - 1);
                    builder.append(")"); // NOI18N
                }
                signature = builder.toString();
            }
        }
        return signature;
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ConstructorNode) {
                name = ASTUtils.getSimpleName(((ConstructorNode) node).getDeclaringClass());
            } else if (node instanceof MethodNode) {
                name = ((MethodNode) node).getName();
            }
            
            if (name == null) {
                name = node.toString();
            }
        }
        return name;
    }
    
    @Override
    public String getReturnType() {
        if (returnType == null) {
            returnType = ASTUtils.getSimpleName(((MethodNode) node).getReturnType());
        }
        return returnType;
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ConstructorNode) {
            return ElementKind.CONSTRUCTOR;
        } else if (node instanceof MethodNode) {
            return ElementKind.METHOD;
        } else {
            return ElementKind.OTHER;
        }
    }
}
