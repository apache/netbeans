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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.CaseDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassInstanceCreationInfo;
import org.netbeans.modules.php.editor.model.nodes.EnumDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PropertyHookDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.TraitDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.PropertyHookDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;

/**
 *
 * @author Radek Matous
 */
class ModelBuilder {

    private final FileScopeImpl fileScope;
    private final NamespaceScopeImpl defaultNamespaceScope;
    private final Object currentScopeLock = new Object();
    // @GuardedBy("currentScopeLock")
    private final ArrayDeque<ScopeImpl> currentScope;
    private final Map<VariableNameFactory, Map<String, VariableNameImpl>> vars;
    private NamespaceScopeImpl namespaceScope;
    private Program program;

    ModelBuilder(FileScopeImpl fileScope) {
        this.fileScope = fileScope;
        this.currentScope = new ArrayDeque<>();
        this.vars = new HashMap<>();
        setCurrentScope(fileScope);
        defaultNamespaceScope = new NamespaceScopeImpl(fileScope);
        namespaceScope = defaultNamespaceScope;
        setCurrentScope(namespaceScope);
    }

    NamespaceScope build(NamespaceDeclaration node, OccurenceBuilder occurencesBuilder) {
        final NamespaceDeclarationInfo info = NamespaceDeclarationInfo.create(node);

        NamespaceScopeImpl nScope = /*(info.isDefaultNamespace()) ? defaultNamespaceScope :*/ ModelElementFactory.create(info, this);
        if (!nScope.isDefaultNamespace()) {
            setCurrentScope(nScope);
        }
        return nScope;
    }

    ClassScope build(ClassDeclaration node, OccurenceBuilder occurencesBuilder) {
        ClassScopeImpl classScope = ModelElementFactory.create(ClassDeclarationInfo.create(node), this);
        setCurrentScope(classScope);
        occurencesBuilder.prepare(node, classScope);
        return classScope;
    }

    ClassScope build(ClassInstanceCreation node, OccurenceBuilder occurencesBuilder) {
        assert node.isAnonymous() : node;
        ClassScopeImpl classScope = ModelElementFactory.create(ClassInstanceCreationInfo.create(node), this);
        setCurrentScope(classScope);
        occurencesBuilder.prepare(node, classScope);
        return classScope;
    }

    TraitScope build(TraitDeclaration node, OccurenceBuilder occurencesBuilder) {
        TraitScopeImpl traitScope = ModelElementFactory.create(TraitDeclarationInfo.create(node), this);
        setCurrentScope(traitScope);
        occurencesBuilder.prepare(node, traitScope);
        return traitScope;
    }

    EnumScope build(EnumDeclaration node, OccurenceBuilder occurencesBuilder) {
        EnumScopeImpl enumScope = ModelElementFactory.create(EnumDeclarationInfo.create(node), this);
        setCurrentScope(enumScope);
        occurencesBuilder.prepare(node, enumScope);
        return enumScope;
    }

    void build(CaseDeclaration node, OccurenceBuilder occurencesBuilder) {
        CaseDeclarationInfo info = CaseDeclarationInfo.create(node);
        occurencesBuilder.prepare(info, ModelElementFactory.create(info, this));
    }

    void build(FieldsDeclaration node, OccurenceBuilder occurencesBuilder) {
        List<? extends SingleFieldDeclarationInfo> infos = SingleFieldDeclarationInfo.create(node);
        for (SingleFieldDeclarationInfo info : infos) {
            FieldElementImpl field = ModelElementFactory.create(info, this);
            if (field.isHooked()) {
                setCurrentScope(field);
            }
            occurencesBuilder.prepare(info, field);
        }
    }

    void build(PropertyHookDeclaration node, OccurenceBuilder occurencesBuilder) {
        PropertyHookDeclarationInfo info = PropertyHookDeclarationInfo.create(node);
        PropertyHookScopeImpl propertyHookScope = ModelElementFactory.create(info, this);
        setCurrentScope(propertyHookScope);
        occurencesBuilder.prepare(info, propertyHookScope);
    }

    void build(ConstantDeclaration node, OccurenceBuilder occurencesBuilder) {
        List<? extends ClassConstantDeclarationInfo> infos = ClassConstantDeclarationInfo.create(node);
        for (ClassConstantDeclarationInfo nodeInfo : infos) {
            occurencesBuilder.prepare(nodeInfo, ModelElementFactory.create(nodeInfo, this));
        }
    }

    void build(Include node, OccurenceBuilder occurencesBuilder) {
        IncludeElementImpl inclImpl = ModelElementFactory.create(IncludeInfo.create(node), this);
        occurencesBuilder.prepare(node, inclImpl);
    }

    InterfaceScope build(InterfaceDeclaration node, OccurenceBuilder occurencesBuilder) {
        InterfaceScopeImpl classScope = ModelElementFactory.create(InterfaceDeclarationInfo.create(node), this);
        setCurrentScope(classScope);
        occurencesBuilder.prepare(node, classScope);
        return classScope;
    }

    void buildMagicMethod(PHPDocMethodTag node,  OccurenceBuilder occurencesBuilder) {
        MagicMethodDeclarationInfo info = MagicMethodDeclarationInfo.create(node);
        if (info != null) {
            MethodScopeImpl methodScope = ModelElementFactory.create(info, this);
            occurencesBuilder.prepare(info, methodScope);
        }
    }

    MethodScope build(MethodDeclaration node, OccurenceBuilder occurencesBuilder, ModelVisitor visitor) {
        final ScopeImpl scope = getCurrentScope();
        MethodScopeImpl methodScope = ModelElementFactory.create(MethodDeclarationInfo.create(getProgram(), node, (TypeScope) scope), this, visitor);
        setCurrentScope(methodScope);
        occurencesBuilder.prepare(node, methodScope);
        return methodScope;
    }

    void reset() {
        synchronized (currentScopeLock) {
            if (!currentScope.isEmpty()) {
                ScopeImpl createdScope = currentScope.peek();
                if (createdScope instanceof NamespaceScopeImpl) {
                    namespaceScope = defaultNamespaceScope;
                    if (!((NamespaceScopeImpl) createdScope).isDefaultNamespace()) {
                        // don't remove default namespace, it's included in constructor
                        currentScope.pop();
                    }
                } else {
                    currentScope.pop();
                }
            }
        }
    }

    /**
     * This method basically restore stack of scopes for scanning a node
     * that was not scanned during lazy scanning.
     * @param scope
     */
    void prepareForScope(Scope scope) {
        synchronized (currentScopeLock) {
            currentScope.clear();
        }
        while (scope != null) {
            if (scope instanceof NamespaceScopeImpl) {
                namespaceScope = (NamespaceScopeImpl) scope;
            }
            synchronized (currentScopeLock) {
                currentScope.addLast((ScopeImpl) scope);
            }
            scope = scope.getInScope();
        }
    }

    /**
     * @return the fileScope
     */
    FileScopeImpl getFileScope() {
        return fileScope;
    }

    NamespaceScopeImpl getCurrentNameSpace() {
        return namespaceScope;
    }

    /**
     * @return the currentScope or null
     */
    ScopeImpl getCurrentScope() {
        synchronized (currentScopeLock) {
            return currentScope.isEmpty() ? null : currentScope.peek();
        }
    }

    /**
     * @param currentScope the currentScope to set
     */
    final void setCurrentScope(ScopeImpl scope) {
        if (scope instanceof NamespaceScopeImpl) {
            namespaceScope = (NamespaceScopeImpl) scope;
        }
        synchronized (currentScopeLock) {
            currentScope.push(scope);
        }
    }

    /**
     * @return the program
     */
    Program getProgram() {
        assert program != null;
        return program;
    }

    void setProgram(Program program) {
        this.program = program;
        this.defaultNamespaceScope.setBlockRange(program);
    }

    /**
     * @return the vars
     */
    Map<VariableNameFactory, Map<String, VariableNameImpl>> getVars() {
        return vars;
    }

    private static final class ModelElementFactory {

        private ModelElementFactory() {
        }

        static NamespaceScopeImpl create(NamespaceDeclarationInfo nodeInfo, ModelBuilder context) {
            NamespaceScopeImpl namespaceScope = new NamespaceScopeImpl(context.getFileScope(), nodeInfo);
            return namespaceScope;
        }

        static IncludeElementImpl create(IncludeInfo info, ModelBuilder context) {
            return new IncludeElementImpl(context.getCurrentScope(), info);
        }

        static ClassScopeImpl create(ClassDeclarationInfo nodeInfo, ModelBuilder context) {
            Scope currentScope = context.getCurrentScope();
            if (currentScope == null) {
                currentScope = context.getCurrentNameSpace();
            }
            if (currentScope instanceof FunctionScope) {
                currentScope = currentScope.getInScope();
            }
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            ClassScopeImpl clz = new ClassScopeImpl(currentScope, nodeInfo, isDeprecated);
            return clz;
        }

        static ClassScopeImpl create(ClassInstanceCreationInfo nodeInfo, ModelBuilder context) {
            Scope currentScope = context.getCurrentScope();
            if (currentScope == null) {
                currentScope = context.getCurrentNameSpace();
            }
            if (currentScope instanceof FunctionScope) {
                currentScope = currentScope.getInScope();
            }
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            ClassScopeImpl clz = new ClassScopeImpl(currentScope, nodeInfo, isDeprecated);
            return clz;
        }

        static InterfaceScopeImpl create(InterfaceDeclarationInfo nodeInfo, ModelBuilder context) {
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            return new InterfaceScopeImpl(context.getCurrentScope(), nodeInfo, isDeprecated);
        }

        static TraitScopeImpl create(TraitDeclarationInfo nodeInfo, ModelBuilder context) {
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            return new TraitScopeImpl(context.getCurrentScope(), nodeInfo, isDeprecated);
        }

        static EnumScopeImpl create(EnumDeclarationInfo nodeInfo, ModelBuilder context) {
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            return new EnumScopeImpl(context.getCurrentScope(), nodeInfo, isDeprecated);
        }

        static MethodScopeImpl create(MethodDeclarationInfo nodeInfo, ModelBuilder context, ModelVisitor visitor) {
            String returnType = VariousUtils.getReturnType(context.getProgram(), nodeInfo.getOriginalNode().getFunction());
            boolean isDeprecated = VariousUtils.isDeprecated(context.getFileScope(), context.getProgram(), nodeInfo.getOriginalNode());
            String qualifiedReturnType = VariousUtils.qualifyTypeNames(returnType, nodeInfo.getOriginalNode().getStartOffset(), context.getCurrentScope());
            MethodScopeImpl method = new MethodScopeImpl(context.getCurrentScope(), qualifiedReturnType, nodeInfo, visitor, isDeprecated);
            return method;
        }

        static MethodScopeImpl create(MagicMethodDeclarationInfo nodeInfo, ModelBuilder context) {
            String returnType = nodeInfo.getReturnType();
            String qualifiedReturnType = VariousUtils.qualifyTypeNames(returnType, nodeInfo.getOriginalNode().getStartOffset(), context.getCurrentScope());
            MethodScopeImpl method = new MethodScopeImpl(context.getCurrentScope(), qualifiedReturnType, nodeInfo);
            return method;
        }

        static FieldElementImpl create(SingleFieldDeclarationInfo nodeInfo, ModelBuilder context) {
            String fieldType = nodeInfo.getFieldType();
            if (fieldType == null) {
                fieldType = VariousUtils.getFieldTypeFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            }
            boolean isDeprecated = VariousUtils.isDeprecatedFromPHPDoc(context.getProgram(), nodeInfo.getOriginalNode());
            String fieldFQType = VariousUtils.qualifyTypeNames(fieldType, nodeInfo.getRange().getStart(), context.getCurrentScope());
            FieldElementImpl fei = new FieldElementImpl(context.getCurrentScope(), fieldType, fieldFQType, nodeInfo, isDeprecated, false);
            return fei;
        }

        static PropertyHookScopeImpl create(PropertyHookDeclarationInfo nodeInfo, ModelBuilder context) {
            return new PropertyHookScopeImpl(context.getCurrentScope(), nodeInfo);
        }

        static ClassConstantElementImpl create(ClassConstantDeclarationInfo clsConst, ModelBuilder context) {
            boolean isDeprecated = VariousUtils.isDeprecated(context.getFileScope(), context.getProgram(), clsConst.getConstantDeclaration());
            return new ClassConstantElementImpl(context.getCurrentScope(), clsConst, isDeprecated);
        }

        static CaseElementImpl create(CaseDeclarationInfo enumCase, ModelBuilder context) {
            boolean isDeprecated = VariousUtils.isDeprecated(context.getFileScope(), context.getProgram(), enumCase.getCaseDeclaration());
            return new CaseElementImpl(context.getCurrentScope(), enumCase, isDeprecated);
        }
    }
}
