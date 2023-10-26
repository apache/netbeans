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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 * @author Radek Matous
 */
final class MethodScopeImpl extends FunctionScopeImpl implements MethodScope, VariableNameFactory, LazyBuild {

    private static final Set<String> MAGIC_METHODS = new HashSet<>();

    private final String classNormName;
    private boolean scanned;
    private MethodDeclaration originalNode;
    private ModelVisitor visitor;

    static {
        for (String methodName : PredefinedSymbols.MAGIC_METHODS) {
            MAGIC_METHODS.add(methodName.toLowerCase());
        }
    }

    //new contructors
    MethodScopeImpl(Scope inScope, String returnType, MethodDeclarationInfo nodeInfo, ModelVisitor visitor, boolean isDeprecated) {
        super(inScope, nodeInfo, returnType, isDeprecated);
        assert inScope instanceof TypeScope;
        classNormName = inScope.getNormalizedName();
        scanned = false;
        originalNode = nodeInfo.getOriginalNode();
        this.visitor = visitor;
    }

    MethodScopeImpl(Scope inScope, String returnType, MagicMethodDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, returnType, inScope.isDeprecated());
        assert inScope instanceof TypeScope : inScope.getClass().toString();
        classNormName = inScope.getNormalizedName();
        scanned = true;
    }

    MethodScopeImpl(Scope inScope, BaseFunctionElement element) {
        super(inScope, element, PhpElementKind.METHOD);
        assert inScope instanceof TypeScope : inScope.getClass().toString();
        classNormName = inScope.getNormalizedName();
        scanned = true;
    }

    public static MethodScopeImpl createElement(Scope scope, PHPDocMethodTag node) {
        MagicMethodDeclarationInfo nodeInfo = MagicMethodDeclarationInfo.create(node);
        assert nodeInfo != null;
        String qualifiedReturnType = VariousUtils.qualifyTypeNames(nodeInfo.getReturnType(), nodeInfo.getOriginalNode().getStartOffset(), scope);
        return new MethodScopeImpl(scope, qualifiedReturnType, nodeInfo);
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        scan();
        final Scope inScope = getInScope();
        if (inScope instanceof ClassScope || inScope instanceof TraitScope) {
            if (inScope instanceof VariableScope) {
                VariableScope variableScope = (VariableScope) inScope;
                return ModelUtils.merge(variableScope.getDeclaredVariables(), super.getDeclaredVariables());
            }
        }
        return super.getDeclaredVariables();
    }

    @Override
    public String getDeclaredReturnType() {
        scan();
        return super.getDeclaredReturnType();
    }

    @Override
    public Collection<? extends TypeScope> getReturnTypes() {
        scan();
        return super.getReturnTypes();
    }

    @Override
    public Collection<? extends TypeScope> getReturnTypes(boolean resolve, Collection<? extends TypeScope> callerTypes) {
        assert callerTypes != null;
        scan();
        return super.getReturnTypes(resolve, callerTypes);
    }

    @Override
    public boolean isReturnUnionType() {
        scan();
        return super.isReturnUnionType();
    }

    @Override
    public boolean isReturnIntersectionType() {
        scan();
        return super.isReturnIntersectionType();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" "); //NOI18N
        sb.append(super.toString());
        return sb.toString();
    }

    @Override
    public boolean isMagic() {
        return MAGIC_METHODS.contains(getName().toLowerCase());
    }

    @Override
    public boolean isInitiator() {
        return isConstructor() || getName().contains("setUp"); //NOI18N
    }

    @Override
    public boolean isConstructor() {
        return isMagic() ? getName().equalsIgnoreCase("__construct") : false; //NOI18N
    }

    @Override
    public TypeScope getTypeScope() {
        return (TypeScope) getInScope();
    }

    @Override
    public String getNormalizedName() {
        return classNormName + super.getNormalizedName();
    }

    @Override
    public String getClassSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" "); //NOI18N
        sb.append("function").append(" ").append(getName()); //NOI18N
        sb.append("("); //NOI18N
        List<? extends ParameterElement> parameterList = getParameters();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                if (i > 0) {
                    sb.append(", "); //NOI18N
                }
                final ParameterElement param = parameterList.get(i);
                if (param.hasDeclaredType()) {
                    Set<TypeResolver> types = param.getTypes();
                    if (types.size() == 1) {
                        for (TypeResolver typeResolver : types) {
                            if (typeResolver.isResolved()) {
                                sb.append(typeResolver.getTypeName(false)).append(' '); //NOI18N
                            }
                        }
                    }
                }

                sb.append(param.getName());
                if (!param.isMandatory()) {
                    String defaultValue = param.getDefaultValue();
                    if (defaultValue != null) {
                        sb.append(" = ").append(defaultValue); //NOI18N
                    }
                }
            }
        }

        sb.append(")"); //NOI18N
        sb.append("{\n}"); //NOI18N
        return sb.toString();
    }

    @Override
    public String getInterfaceSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" "); //NOI18N
        sb.append("function").append(" ").append(getName()); //NOI18N
        sb.append("("); //NOI18N
        List<? extends String> parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.size(); i++) {
            String param = parameterNames.get(i);
            if (i > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(param);
        }
        sb.append(")"); //NOI18N
        sb.append(";\n"); //NOI18N
        return sb.toString();
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_METHOD, getIndexSignature(), true, true);
        if (isConstructor()) {
            indexDocument.addPair(PHPIndexer.FIELD_CONSTRUCTOR, getConstructorIndexSignature(), false, true);
        }
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getSignatureLastPart());
        return sb.toString();
    }

    @Override
    public String getConstructorIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String typeName = getInScope().getName();
        sb.append(typeName.toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(typeName).append(Signature.ITEM_DELIMITER);
        sb.append(getSignatureLastPart());
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    private String getSignatureLastPart() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        List<? extends ParameterElement> parameters = getParameters();
        for (int idx = 0; idx < parameters.size(); idx++) {
            ParameterElementImpl parameter = (ParameterElementImpl) parameters.get(idx);
            if (idx > 0) {
                sb.append(','); //NOI18N
            }
            sb.append(parameter.getSignature());
        }
        sb.append(Signature.ITEM_DELIMITER);
        String returnType = getReturnType();
        if (returnType != null) {
            sb.append(returnType);
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        sb.append(isReturnUnionType() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(isReturnIntersectionType() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append((getDeclaredReturnType() != null) ? getDeclaredReturnType() : "").append(Signature.ITEM_DELIMITER); // NOI18N
        return sb.toString();
    }

    @Override
    public boolean isScanned() {
        return scanned;
    }

    @Override
    public void scan() {
        if (!scanned && visitor != null) {
            scanned = true;
            visitor.scanNoLazy(originalNode, this);
        }

    }
}
