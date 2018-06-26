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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.api.elements.ast;

import groovy.lang.MetaMethod;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.modules.csl.api.ElementKind;
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
            for (Parameter parameter : ((MethodNode) node).getParameters()) {
                String paramName = parameter.getName();
                String fqnType = parameter.getType().getName();
                String type = parameter.getType().getNameWithoutPackage();

                parameters.add(new MethodParameter(fqnType, type, paramName));
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
        return signature;
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ConstructorNode) {
                name = ((ConstructorNode) node).getDeclaringClass().getNameWithoutPackage();
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
            returnType = ((MethodNode) node).getReturnType().getNameWithoutPackage();
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
