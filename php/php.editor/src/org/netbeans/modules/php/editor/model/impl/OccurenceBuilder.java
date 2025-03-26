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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassMemberElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IncludeElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.Occurence.Accuracy;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseAliasElement;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfoUtils;
import org.netbeans.modules.php.editor.model.nodes.CaseDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.EnumDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.TraitDeclarationInfo;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class OccurenceBuilder {

    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int ACCESSING_THREADS = 2;
    private Map<ASTNodeInfo<Scalar>, ConstantElement> constDeclarations;
    private Map<ConstantDeclarationInfo, ConstantElement> constDeclarations53;
    private Map<ASTNodeInfo<Scalar>, Scope> constInvocations;
    private Map<ASTNodeInfo<Expression>, Scope> nsConstInvocations;
    private Map<ASTNodeInfo<Expression>, Scope> nsFunctionInvocations;
    private Map<ASTNodeInfo<FunctionDeclaration>, FunctionScope> fncDeclarations;
    private Map<ASTNodeInfo<MethodDeclaration>, MethodScope> methodDeclarations;
    private Map<MagicMethodDeclarationInfo, MethodScope> magicMethodDeclarations;
    private Map<ASTNodeInfo<MethodInvocation>, Scope> methodInvocations;
    private Map<ASTNodeInfo<Identifier>, ClassConstantElement> classConstantDeclarations;
    private Map<ASTNodeInfo<Identifier>, CaseElement> caseDeclarations;
    private Map<ASTNodeInfo<FunctionInvocation>, Scope> fncInvocations;
    private Map<ASTNodeInfo<StaticMethodInvocation>, Scope> staticMethodInvocations;
    private Map<ASTNodeInfo<StaticFieldAccess>, Scope> staticFieldInvocations;
    private Map<ASTNodeInfo<StaticConstantAccess>, Scope> staticConstantInvocations;
    private Map<ClassDeclarationInfo, ClassScope> clasDeclarations;
    private Map<InterfaceDeclarationInfo, InterfaceScope> ifaceDeclarations;
    private Map<TraitDeclarationInfo, TraitScope> traitDeclarations;
    private Map<EnumDeclarationInfo, EnumScope> enumDeclarations;
    private Map<PhpDocTypeTagInfo, Scope> docTags;
    private Map<ASTNodeInfo<ClassName>, Scope> clasNames;
    private Map<ASTNodeInfo<ClassInstanceCreation>, Scope> clasInstanceCreations;
    private Map<ASTNodeInfo<Expression>, Scope> clasIDs;
    private Map<ASTNodeInfo<Expression>, Scope> ifaceIDs;
    private Map<ASTNodeInfo<Expression>, Scope> traitIDs;
    private Map<ASTNodeInfo<Expression>, Scope> enumIDs;
    private Map<ASTNodeInfo<Variable>, Scope> variables;
    private Map<IncludeInfo, IncludeElement> includes;
    private Map<SingleFieldDeclarationInfo, FieldElementImpl> fldDeclarations;
    private Map<ASTNodeInfo<FieldAccess>, Scope> fieldInvocations;
    private volatile ElementInfo elementInfo;
    private volatile ElementInfo promotedVariableElementInfo;
    private Map<ASTNodeInfo<GotoLabel>, Scope> gotoLabel;
    private Map<ASTNodeInfo<GotoStatement>, Scope> gotoStatement;
    private Map<ASTNodeInfo<Expression>, Scope> useAliases;
    private final List<Occurence> cachedOccurences;

    OccurenceBuilder() {
        this(-1);
    }

    OccurenceBuilder(int offset) {
        this.constInvocations = this.<ASTNodeInfo<Scalar>, Scope>initMap();
        this.nsConstInvocations = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.nsFunctionInvocations = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.constDeclarations = this.<ASTNodeInfo<Scalar>, ConstantElement>initMap();
        this.constDeclarations53 = this.<ConstantDeclarationInfo, ConstantElement>initMap();
        this.includes = this.<IncludeInfo, IncludeElement>initMap();
        this.fncInvocations = this.<ASTNodeInfo<FunctionInvocation>, Scope>initMap();
        this.fncDeclarations = this.<ASTNodeInfo<FunctionDeclaration>, FunctionScope>initMap();
        this.staticMethodInvocations = this.<ASTNodeInfo<StaticMethodInvocation>, Scope>initMap();
        this.methodDeclarations = this.<ASTNodeInfo<MethodDeclaration>, MethodScope>initMap();
        this.magicMethodDeclarations = this.<MagicMethodDeclarationInfo, MethodScope>initMap();
        this.methodInvocations = this.<ASTNodeInfo<MethodInvocation>, Scope>initMap();
        this.fieldInvocations = this.<ASTNodeInfo<FieldAccess>, Scope>initMap();
        this.staticFieldInvocations = this.<ASTNodeInfo<StaticFieldAccess>, Scope>initMap();
        this.staticConstantInvocations = this.<ASTNodeInfo<StaticConstantAccess>, Scope>initMap();
        this.clasDeclarations = this.<ClassDeclarationInfo, ClassScope>initMap();
        this.ifaceDeclarations = this.<InterfaceDeclarationInfo, InterfaceScope>initMap();
        this.traitDeclarations = this.<TraitDeclarationInfo, TraitScope>initMap();
        this.enumDeclarations = this.<EnumDeclarationInfo, EnumScope>initMap();
        this.clasNames = this.<ASTNodeInfo<ClassName>, Scope>initMap();
        this.clasInstanceCreations = this.<ASTNodeInfo<ClassInstanceCreation>, Scope>initMap();
        this.clasIDs = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.ifaceIDs = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.traitIDs = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.enumIDs = this.<ASTNodeInfo<Expression>, Scope>initMap();
        this.classConstantDeclarations = this.<ASTNodeInfo<Identifier>, ClassConstantElement>initMap();
        this.caseDeclarations = this.<ASTNodeInfo<Identifier>, CaseElement>initMap();
        this.variables = this.<ASTNodeInfo<Variable>, Scope>initMap();
        this.fldDeclarations = this.<SingleFieldDeclarationInfo, FieldElementImpl>initMap();
        this.docTags = this.<PhpDocTypeTagInfo, Scope>initMap();
        this.gotoStatement = this.<ASTNodeInfo<GotoStatement>, Scope>initMap();
        this.gotoLabel = this.<ASTNodeInfo<GotoLabel>, Scope>initMap();
        this.useAliases = this.<ASTNodeInfo<Expression>, Scope>initMap();

        this.cachedOccurences = new ArrayList<>();
    }

    private <K, V> Map<K, V> initMap() {
        return new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESSING_THREADS);
    }

    void prepare(GotoStatement statement, ScopeImpl scope) {
        if (canBePrepared(statement, scope)) {
            ASTNodeInfo<GotoStatement> node = ASTNodeInfo.create(statement);
            gotoStatement.put(node, scope);
        }
    }

    void prepare(GotoLabel label, ScopeImpl scope) {
        if (canBePrepared(label, scope)) {
            ASTNodeInfo<GotoLabel> node = ASTNodeInfo.create(label);
            gotoLabel.put(node, scope);
        }
    }

    void prepare(FieldAccess fieldAccess, Scope scope) {
        if (canBePrepared(fieldAccess, scope)) {
            ASTNodeInfo<FieldAccess> node = ASTNodeInfo.create(fieldAccess);
            fieldInvocations.put(node, scope);
        }
    }

    void prepare(Include incl, IncludeElementImpl inclImpl) {
        if (canBePrepared(incl, inclImpl)) {
            IncludeInfo node = IncludeInfo.create(incl);
            includes.put(node, inclImpl);
        }
    }

    void prepare(MethodInvocation methodInvocation, Scope scope) {
        if (canBePrepared(methodInvocation, scope)) {
            ASTNodeInfo<MethodInvocation> node = ASTNodeInfo.create(methodInvocation);
            methodInvocations.put(node, scope);
        }
    }

    void prepare(SingleFieldDeclarationInfo info, FieldElementImpl fei) {
        SingleFieldDeclaration node = info.getOriginalNode();
        if (canBePrepared(node, fei)) {
            fldDeclarations.put(info, fei);
        }
    }

    void prepare(Variable variable, Scope scope) {
        if (canBePrepared(variable, scope)) {
            ASTNodeInfo<Variable> node = ASTNodeInfo.create(variable);
            variables.put(node, scope);
        }
    }

    void prepare(FunctionInvocation functionInvocation, Scope scope) {
        if (canBePrepared(functionInvocation, scope)) {
            ASTNodeInfo<FunctionInvocation> node = ASTNodeInfo.create(functionInvocation);
            fncInvocations.put(node, scope);
        }
    }

    void prepare(StaticMethodInvocation staticMethodInvocation, Scope scope) {
        if (canBePrepared(staticMethodInvocation, scope)) {
            ASTNodeInfo<StaticMethodInvocation> node = ASTNodeInfo.create(staticMethodInvocation);
            this.staticMethodInvocations.put(node, scope);
        }
    }

    void prepare(StaticFieldAccess staticFieldAccess, Scope scope) {
        if (canBePrepared(staticFieldAccess, scope)) {
            ASTNodeInfo<StaticFieldAccess> node = ASTNodeInfo.create(staticFieldAccess);
            staticFieldInvocations.put(node, scope);
        }
    }

    void prepare(StaticConstantAccess staticConstantAccess, Scope scope) {
        if (!staticConstantAccess.isDynamicName() && canBePrepared(staticConstantAccess, scope)) {
            ASTNodeInfo<StaticConstantAccess> node = ASTNodeInfo.create(staticConstantAccess);
            staticConstantInvocations.put(node, scope);
        }
    }

    void prepare(ClassName clsName, Scope scope) {
        if (canBePrepared(clsName, scope)) {
            ASTNodeInfo<ClassName> node = ASTNodeInfo.create(clsName);
            clasNames.put(node, scope);
        }
    }

    void prepare(final NamespaceName namespaceName, final Scope scope) {
        Kind[] kinds = {Kind.CLASS, Kind.IFACE, Kind.ENUM};
        prepare(kinds, namespaceName, scope);
    }

    void prepare(final Kind[] kinds, final NamespaceName namespaceName, final Scope scope) {
        if (canBePrepared(namespaceName, scope)) {
            prepareNamespaceName(kinds, namespaceName, scope);
        }
    }

    private void prepareNamespaceName(final Kind[] kinds, final NamespaceName namespaceName, final Scope scope) {
        QualifiedName qualifiedName = QualifiedName.create(CodeUtils.extractQualifiedName(namespaceName));
        if (!VariousUtils.isSpecialClassName(qualifiedName.toString())) {
            final boolean isAliased = VariousUtils.isAliased(qualifiedName, namespaceName.getStartOffset(), scope);
            if (isAliased) {
                prepareAliasedNamespaceName(kinds, namespaceName, scope);
            } else {
                prepareOccurences(kinds, namespaceName, scope);
            }
        }
    }

    private void prepareAliasedNamespaceName(final Kind[] kinds, final NamespaceName namespaceName, final Scope scope) {
        List<Identifier> segments = namespaceName.getSegments();
        if (segments.size() > 1) {
            prepareOccurences(kinds, namespaceName, scope);
        }
        prepare(Kind.USE_ALIAS, segments.get(0), scope);
    }

    private void prepareOccurences(final Kind[] kinds, final Expression expression, final Scope scope) {
        for (Kind kind : kinds) {
            prepare(kind, expression, scope);
        }
    }

    void prepare(Kind kind, Expression node, Scope scope) {
        ASTNodeInfo<Expression> nodeInfo = null;
        if (node instanceof Identifier) {
            nodeInfo = ASTNodeInfo.create(kind, (Identifier) node);
        } else if (node instanceof NamespaceName) {
            nodeInfo = ASTNodeInfo.create(kind, (NamespaceName) node);
        }
        if (nodeInfo != null && canBePrepared(node, scope)) {
            switch (nodeInfo.getKind()) {
                case CLASS:
                    clasIDs.put(nodeInfo, scope);
                    break;
                case IFACE:
                    ifaceIDs.put(nodeInfo, scope);
                    break;
                case TRAIT:
                    traitIDs.put(nodeInfo, scope);
                    break;
                case ENUM:
                    enumIDs.put(nodeInfo, scope);
                    break;
                case CONSTANT:
                    if (node instanceof NamespaceName || node instanceof Identifier) {
                        nsConstInvocations.put(nodeInfo, scope);
                    }
                    break;
                case FUNCTION:
                    if (node instanceof NamespaceName) {
                        nsFunctionInvocations.put(nodeInfo, scope);
                    }
                    break;
                case USE_ALIAS:
                    useAliases.put(nodeInfo, scope);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    void prepare(ClassInstanceCreation node, Scope scope) {
        ASTNodeInfo<ClassInstanceCreation> nodeInfo = ASTNodeInfo.create(node);
        if (canBePrepared(node, scope)) {
            if (node.isAnonymous()) {
                // XXX anonymous class
            } else {
                clasInstanceCreations.put(nodeInfo, scope);
            }
        }
    }

    void prepare(Kind kind, Scalar scalar, Scope scope) {
        ASTNodeInfo<Scalar> nodeInfo = ASTNodeInfo.create(kind, scalar);
        if (canBePrepared(scalar, scope)) {
            constInvocations.put(nodeInfo, scope);
        }
    }

    void prepare(ASTNodeInfo<Scalar> nodeInfo, ConstantElement constantElement) {
        Scalar scalar = nodeInfo.getOriginalNode();
        if (canBePrepared(scalar, constantElement)) {
            constDeclarations.put(nodeInfo, constantElement);
        }
    }

    void prepare(ConstantDeclarationInfo constantNodeInfo, ConstantElement scope) {
        if (constantNodeInfo != null && canBePrepared(constantNodeInfo.getOriginalNode(), scope)) {
            constDeclarations53.put(constantNodeInfo, scope);
        }
    }

    void prepare(PHPDocTypeTag pHPDocTag, Scope scope) {
        if (canBePrepared(pHPDocTag, scope)) {
            List<? extends PhpDocTypeTagInfo> infos = PhpDocTypeTagInfo.create(pHPDocTag, scope);
            for (PhpDocTypeTagInfo typeTagInfo : infos) {
                docTags.put(typeTagInfo, scope);
            }
        }
    }

    void prepare(ClassDeclaration classDeclaration, ClassScope scope) {
        if (canBePrepared(classDeclaration, scope)) {
            ClassDeclarationInfo node = ClassDeclarationInfo.create(classDeclaration);
            clasDeclarations.put(node, scope);
            QualifiedName superClassName = QualifiedName.create(classDeclaration.getSuperClass());
            if (superClassName != null) {
                if (VariousUtils.isAlias(superClassName, classDeclaration.getStartOffset(), scope)) {
                    prepare(Kind.USE_ALIAS, classDeclaration.getSuperClass(), scope);
                } else {
                    prepare(Kind.CLASS, classDeclaration.getSuperClass(), scope);
                }
            }
            List<Expression> interfaes = classDeclaration.getInterfaces();
            for (Expression iface : interfaes) {
                QualifiedName ifaceName = QualifiedName.create(iface);
                if (ifaceName != null && VariousUtils.isAlias(ifaceName, classDeclaration.getStartOffset(), scope)) {
                    prepare(Kind.USE_ALIAS, iface, scope);
                } else {
                    prepare(Kind.IFACE, iface, scope);
                }
            }
        }
    }

    void prepare(InterfaceDeclaration interfaceDeclaration, InterfaceScope scope) {
        if (canBePrepared(interfaceDeclaration, scope)) {
            InterfaceDeclarationInfo node = InterfaceDeclarationInfo.create(interfaceDeclaration);
            ifaceDeclarations.put(node, scope);
            List<Expression> interfaes = interfaceDeclaration.getInterfaces();
            for (Expression iface : interfaes) {
                prepare(Kind.IFACE, iface, scope);
            }
        }
    }

    void prepare(TraitDeclaration traitDeclaration, TraitScope scope) {
        if (canBePrepared(traitDeclaration, scope)) {
            TraitDeclarationInfo nodeInfo = TraitDeclarationInfo.create(traitDeclaration);
            traitDeclarations.put(nodeInfo, scope);
        }
    }

    void prepare(EnumDeclaration enumDeclaration, EnumScope scope) {
        if (canBePrepared(enumDeclaration, scope)) {
            EnumDeclarationInfo node = EnumDeclarationInfo.create(enumDeclaration);
            enumDeclarations.put(node, scope);
            List<Expression> interfaes = enumDeclaration.getInterfaces();
            for (Expression iface : interfaes) {
                QualifiedName ifaceName = QualifiedName.create(iface);
                if (ifaceName != null && VariousUtils.isAlias(ifaceName, enumDeclaration.getStartOffset(), scope)) {
                    prepare(Kind.USE_ALIAS, iface, scope);
                } else {
                    prepare(Kind.IFACE, iface, scope);
                }
            }
        }
    }

    void prepare(FunctionDeclaration functionDeclaration, FunctionScope scope) {
        if (canBePrepared(functionDeclaration, scope)) {
            FunctionDeclarationInfo node = FunctionDeclarationInfo.create(functionDeclaration);
            fncDeclarations.put(node, scope);
        }
    }

    void prepare(MethodDeclaration methodDeclaration, MethodScope scope) {
        if (canBePrepared(methodDeclaration, scope)) {
            MethodDeclarationInfo node = MethodDeclarationInfo.create(methodDeclaration, scope.getTypeScope());
            methodDeclarations.put(node, scope);
            if (scope.getInScope() instanceof ClassScope) {
                ClassScope classScope = (ClassScope) scope.getInScope();
                String className = classScope.getName();
                if (className != null && className.equals(CodeUtils.extractMethodName(methodDeclaration))) {
                    prepare(Kind.CLASS, methodDeclaration.getFunction().getFunctionName(), scope);
                }
            }
        }
    }

    void prepare(final MagicMethodDeclarationInfo node, MethodScope scope) {
        if (canBePrepared(node.getOriginalNode(), scope)) {
            if (ASTNodeInfoUtils.isMethod(node.getKind())) {
                magicMethodDeclarations.put(node, scope);
            }
        }
    }

    void prepare(ClassConstantDeclarationInfo constantNodeInfo, ClassConstantElement scope) {
        if (constantNodeInfo != null && canBePrepared(constantNodeInfo.getOriginalNode(), scope)) {
            classConstantDeclarations.put(constantNodeInfo, scope);
        }
    }

    void prepare(CaseDeclarationInfo caseNodeInfo, CaseElement scope) {
        if (caseNodeInfo != null && canBePrepared(caseNodeInfo.getOriginalNode(), scope)) {
            caseDeclarations.put(caseNodeInfo, scope);
        }
    }
    /**
     * *
     *
     * @param offset
     * @return true if ElementInfo was set and even more represents different
     * element than the previous one and thus makes sense to recompute
     * occurrences. If false is returned then makes no sense to recompute
     * occurences
     */
    private boolean setElementInfo(final int offset) {
        elementInfo = null;
        final Collection<LazyBuild> scopesToScan = new ArrayList<>();
        for (Entry<ASTNodeInfo<MethodDeclaration>, MethodScope> entry : methodDeclarations.entrySet()) {
            if (entry.getValue() instanceof LazyBuild) {
                LazyBuild scope = (LazyBuild) entry.getValue();
                scopesToScan.add(scope);
            }
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }
        for (LazyBuild lazyBuild : scopesToScan) {
            if (!lazyBuild.isScanned()) {
                lazyBuild.scan();
            }
        }

        for (Entry<ASTNodeInfo<GotoStatement>, Scope> entry : gotoStatement.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<GotoLabel>, Scope> entry : gotoLabel.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<IncludeInfo, IncludeElement> entry : includes.entrySet()) {
            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(entry.getValue());
            if (namespaceScope != null) {
                setOffsetElementInfo(new ElementInfo(entry.getKey(), namespaceScope), offset);
            }
        }

        for (Entry<ASTNodeInfo<FieldAccess>, Scope> entry : fieldInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<MethodInvocation>, Scope> entry : methodInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<SingleFieldDeclarationInfo, FieldElementImpl> entry : fldDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Variable>, Scope> entry : variables.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<FunctionInvocation>, Scope> entry : fncInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<StaticMethodInvocation>, Scope> entry : staticMethodInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<StaticFieldAccess>, Scope> entry : staticFieldInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<StaticConstantAccess>, Scope> entry : staticConstantInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<ClassName>, Scope> entry : clasNames.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<ClassInstanceCreation>, Scope> entry : clasInstanceCreations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : clasIDs.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : ifaceIDs.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : traitIDs.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : enumIDs.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Scalar>, Scope> entry : constInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Scalar>, ConstantElement> entry : constDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ConstantDeclarationInfo, ConstantElement> entry : constDeclarations53.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ClassDeclarationInfo, ClassScope> entry : clasDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<InterfaceDeclarationInfo, InterfaceScope> entry : ifaceDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<TraitDeclarationInfo, TraitScope> entry : traitDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<EnumDeclarationInfo, EnumScope> entry : enumDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<FunctionDeclaration>, FunctionScope> entry : fncDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<MagicMethodDeclarationInfo, MethodScope> entry : magicMethodDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Identifier>, ClassConstantElement> entry : classConstantDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Identifier>, CaseElement> entry : caseDeclarations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : nsConstInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : nsFunctionInvocations.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }

        for (Entry<ASTNodeInfo<Expression>, Scope> entry : useAliases.entrySet()) {
            setOffsetElementInfo(new ElementInfo(entry.getKey(), entry.getValue()), offset);
        }
        return elementInfo != null;
    }

    private boolean setElementInfo(final ModelElement element) {
        elementInfo = new ElementInfo(element);
        return true;
    }

    private void build(FileScopeImpl fileScope) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        ASTNodeInfo.Kind kind = elementInfo != null ? elementInfo.getKind() : null;
        if (elementInfo != null && kind != null) {
            final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
            final Index index = indexScope.getIndex();
            cachedOccurences.clear();
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scanMethodBodies();
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            switch (kind) {
                case GOTO:
                    buildGotoLabels(elementInfo, fileScope, cachedOccurences);
                    buildGotoStatements(elementInfo, fileScope, cachedOccurences);
                    break;
                case FUNCTION:
                    final Set<FunctionElement> functions = index.getFunctions(NameKind.exact(elementInfo.getQualifiedName()));
                    elementInfo.setDeclarations(functions);
                    buildFunctionInvocations(elementInfo, fileScope, cachedOccurences);
                    buildFunctionDeclarations(elementInfo, fileScope, cachedOccurences);
                    break;
                case VARIABLE:
                    buildVariables(elementInfo, fileScope, cachedOccurences);
                    buildDocTagsForVars(elementInfo, fileScope, cachedOccurences);
                    break;
                case STATIC_METHOD:
                    buildStaticMethods(index, fileScope, cachedOccurences);
                    break;
                case FIELD:
                    buildFields(index, fileScope, cachedOccurences);
                    if (promotedVariableElementInfo != null) {
                        // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
                        buildVariables(promotedVariableElementInfo, fileScope, cachedOccurences);
                        buildDocTagsForVars(promotedVariableElementInfo, fileScope, cachedOccurences);
                        promotedVariableElementInfo = null;
                    }
                    break;
                case STATIC_FIELD:
                    buildStaticFields(index, fileScope, cachedOccurences);
                    break;
                case CONSTANT:
                    Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> constants =
                            index.getConstants(NameKind.exact(elementInfo.getQualifiedName()));

                    elementInfo.setDeclarations(constants);
                    cachedOccurences.clear();
                    buildConstantInvocations(elementInfo, fileScope, cachedOccurences);
                    buildConstantDeclarations(elementInfo, fileScope, cachedOccurences);
                    break;
                case CLASS_CONSTANT: // no break;
                case STATIC_CLASS_CONSTANT: // no break
                case ENUM_CASE:
                    buildTypeConstants(index, fileScope, cachedOccurences);
                    buildEnumCases(index, fileScope, cachedOccurences);
                    break;
                case IFACE:
                case CLASS_INSTANCE_CREATION:
                case CLASS:
                case TRAIT:
                case ENUM:
                    final QualifiedName qualifiedName = elementInfo.getNodeInfo() != null
                            ? VariousUtils.getFullyQualifiedName(
                                    elementInfo.getNodeInfo().getQualifiedName(),
                                    elementInfo.getNodeInfo().getOriginalNode().getStartOffset(),
                                    elementInfo.getScope())
                            : elementInfo.getQualifiedName();
                    final Set<TypeElement> types = index.getTypes(NameKind.exact(qualifiedName));
                    if (elementInfo.setDeclarations(types)) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        boolean isClass = false;
                        boolean isInterface = false;
                        boolean isTrait = false;
                        boolean isEnum = false;
                        for (TypeElement type : types) {
                            if (type.isClass()) {
                                isClass = true;
                            } else if (type.isInterface()) {
                                isInterface = true;
                            } else if (type.isTrait()) {
                                isTrait = true;
                            } else if (type.isEnum()) {
                                isEnum = true;
                            } else {
                                assert false : "Unknown type: " + type;
                            }
                        }
                        if (isClass) {
                            buildClassNames(elementInfo, fileScope, cachedOccurences);
                            buildClassIDs(elementInfo, fileScope, cachedOccurences);
                            buildClassDeclarations(elementInfo, fileScope, cachedOccurences);
                        }
                        if (isInterface) {
                            buildInterfaceIDs(elementInfo, fileScope, cachedOccurences);
                            buildInterfaceDeclarations(elementInfo, fileScope, cachedOccurences);
                        }
                        if (isTrait) {
                            buildTraitDeclarations(elementInfo, fileScope, cachedOccurences);
                            buildTraitIDs(elementInfo, fileScope, cachedOccurences);
                        }
                        if (isEnum) {
                            buildEnumDeclarations(elementInfo, fileScope, cachedOccurences);
                            buildEnumIDs(elementInfo, fileScope, cachedOccurences);
                        }
                        if (isClass
                                || isInterface) {
                            buildDocTagsForClasses(elementInfo, fileScope, cachedOccurences);
                            buildClassInstanceCreation(elementInfo, fileScope, cachedOccurences);
                        }
                    }
                    break;
                case METHOD:
                    buildMethods(index, fileScope, cachedOccurences);
                    if (elementInfo.getModelElemnt() != null) {
                        //168149  -  Searching for usages of the __construct method
                        Scope scope = elementInfo.getScope();
                        if (scope instanceof MethodScope && MethodElement.CONSTRUCTOR_NAME.equalsIgnoreCase(elementInfo.getName())) {
                            buildMethods(index, fileScope, cachedOccurences);
                            setElementInfo((TypeScope) scope.getInScope());
                            if (elementInfo.setDeclarations(index.getTypes(NameKind.exact(elementInfo.getQualifiedName())))) {
                                buildClassInstanceCreation(elementInfo, fileScope, cachedOccurences, true);
                            }
                        }
                    }
                    break;
                case INCLUDE:
                    buildIncludes(elementInfo, fileScope, cachedOccurences);
                    break;
                case USE_ALIAS:
                    if (elementInfo.setDeclarations(getUseAliasesDeclarations(elementInfo))) {
                        buildDocTagsForUseAliases(elementInfo, fileScope, cachedOccurences);
                        buildUseAliases(elementInfo, fileScope, cachedOccurences);
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        //return retval;
    }

    private void buildStaticFields(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Collection<? extends TypeElement> types = resolveTypes(index, elementInfo);
        if (!types.isEmpty()) {
            final Exact fieldName = NameKind.exact(elementInfo.getName());
            final Set<FieldElement> fields = new HashSet<>();
            for (TypeElement typeElement : types) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                fields.addAll(ElementFilter.forName(fieldName).filter(index.getAlllFields(typeElement)));
            }
            if (elementInfo.setDeclarations(fields)) {
                occurences.clear();
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                buildStaticFieldInvocations(elementInfo, fileScope, occurences);
                buildFieldDeclarations(elementInfo, fileScope, occurences);
                buildDocTagsForFields(elementInfo, fileScope, occurences);
            }
        }
    }

    private void buildFields(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Set<TypeElement> types = new HashSet<>();
        final Exact fieldName = NameKind.exact(elementInfo.getName());
        Set<FieldElement> fields = new HashSet<>();
        final Scope scope = elementInfo.getScope();
        final ASTNodeInfo nodeInfo = elementInfo.getNodeInfo();
        if (fields.isEmpty()/*
                 * && types.isEmpty()
                 */) {
            String fldName = elementInfo.getName();
            fields = index.getFields(NameKind.exact(fldName.startsWith("$") ? fldName.substring(1) : fldName));
        }

        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Occurence.Accuracy accuracy = Accuracy.NO;
        if (fields.size() == 1) {
            accuracy = (elementInfo.setDeclarations(fields)) ? Accuracy.UNIQUE : null;
            elementInfo.setDeclarations(fields);
        } else {
            if (nodeInfo != null) {
                if (scope instanceof VariableScope) {
                    ASTNode originalNode = nodeInfo.getOriginalNode();
                    if (originalNode instanceof VariableBase) {
                        types.addAll(getClassName((VariableScope) scope, (VariableBase) originalNode));
                    }
                }
            }
            if (scope instanceof FieldElementImpl) {
                final Scope inScope = ((FieldElementImpl) scope).getInScope();
                types.add((TypeElement) inScope);
            }

            if (types.size() > 0) {
                if (!fields.isEmpty()) {
                    fields = new HashSet<>();
                    for (TypeElement typeElement : types) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        fields.addAll(ElementFilter.forName(fieldName).filter(index.getAlllFields(typeElement)));
                    }
                }

                if (fields.isEmpty() && types.size() == 1) {
                    accuracy = (elementInfo.setDeclarations(types)) ? Accuracy.EXACT_TYPE : null;
                    elementInfo.setDeclarations(types);
                } else if (fields.isEmpty() && types.size() > 1) {
                    accuracy = Accuracy.MORE_TYPES;
                    elementInfo.setDeclarations(types);
                } else if (fields.size() == 1) {
                    accuracy = Accuracy.EXACT;
                    elementInfo.setDeclarations(fields);
                } else if (!fields.isEmpty() && !types.isEmpty()) {
                    accuracy = Accuracy.MORE_MEMBERS;
                    elementInfo.setDeclarations(fields);
                }
            } else if (!fields.isEmpty()) {
                accuracy = Accuracy.MORE;
                elementInfo.setDeclarations(fields);
            }
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        if (accuracy != null) {
            occurences.clear();
            if (EnumSet.<Occurence.Accuracy>of(Accuracy.EXACT, Accuracy.EXACT_TYPE,
                    Accuracy.UNIQUE, Accuracy.EXACT_TYPE, Accuracy.MORE_MEMBERS, Accuracy.MORE).contains(accuracy)) {
                buildFieldInvocations(elementInfo, fileScope, accuracy, occurences);
                buildFieldDeclarations(elementInfo, fileScope, occurences);
                buildDocTagsForFields(elementInfo, fileScope, occurences);
            } else if (!accuracy.equals(Accuracy.NO)) {
                //not compute other occurences
                OccurenceImpl occurence2 = new OccurenceImpl(elementInfo.getDeclarations(), nodeInfo.getRange());
                occurence2.setAccuracy(accuracy);
                occurences.add(occurence2);
            }
        }
    }

    private void buildMethods(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Set<TypeElement> types = new HashSet<>();
        final Exact methodName = NameKind.exact(elementInfo.getName());
        Set<MethodElement> methods = new HashSet<>();
        final Scope scope = elementInfo.getScope();
        final ASTNodeInfo nodeInfo = elementInfo.getNodeInfo();
        ModelElement modelElement = elementInfo.getModelElemnt();
        if (methods.isEmpty()) {
            methods = index.getMethods(methodName);
        }

        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Occurence.Accuracy accuracy = Accuracy.NO;
        if (methods.size() == 1) {
            accuracy = (elementInfo.setDeclarations(methods)) ? Accuracy.UNIQUE : null;
            elementInfo.setDeclarations(methods);
        } else {
            if (nodeInfo != null) {
                ASTNode originalNode = nodeInfo.getOriginalNode();
                if (scope instanceof MethodScopeImpl && originalNode instanceof MethodDeclaration) {
                    types.add((TypeElement) scope.getInScope());
                } else if (scope instanceof VariableScope && originalNode instanceof VariableBase) {
                    types.addAll(getClassName((VariableScope) scope, (VariableBase) originalNode));
                }
            } else if (modelElement instanceof MethodScopeImpl && modelElement.getInScope() instanceof TypeScope) {
                types.add((TypeElement) scope.getInScope());
            }

            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (types.size() > 0) {
                if (!methods.isEmpty()) {
                    methods = new HashSet<>();
                    for (TypeElement typeElement : types) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        methods.addAll(ElementFilter.forName(methodName).filter(index.getAllMethods(typeElement)));
                    }
                }

                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (methods.isEmpty() && types.size() == 1) {
                    accuracy = (elementInfo.setDeclarations(types)) ? Accuracy.EXACT_TYPE : null;
                    elementInfo.setDeclarations(types);
                } else if (methods.isEmpty() && types.size() > 1) {
                    accuracy = Accuracy.MORE_TYPES;
                    elementInfo.setDeclarations(types);
                } else if (methods.size() == 1) {
                    accuracy = Accuracy.EXACT;
                    elementInfo.setDeclarations(methods);
                } else if (!methods.isEmpty() && !types.isEmpty()) {
                    accuracy = Accuracy.MORE_MEMBERS;
                    elementInfo.setDeclarations(methods);
                }
            }
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        if (accuracy != null) {
            occurences.clear();
            if (EnumSet.<Occurence.Accuracy>of(Accuracy.EXACT, Accuracy.EXACT_TYPE,
                    Accuracy.UNIQUE, Accuracy.EXACT_TYPE, Accuracy.MORE_MEMBERS, Accuracy.MORE).contains(accuracy)) {
                buildMethodInvocations(elementInfo, fileScope, accuracy, cachedOccurences);
                // these static invocations are valid
                // previous fix: #208309
                // $test->publicStaticMethod();
                // $test::publicStaticMethod();
                // Test::publicStaticMethod();
                buildStaticMethodInvocations(elementInfo, fileScope, cachedOccurences, false);
                buildMethodDeclarations(elementInfo, fileScope, cachedOccurences);
                buildMagicMethodDeclarations(elementInfo, fileScope, cachedOccurences);
            } else if (!accuracy.equals(Accuracy.NO)) {
                //not compute other occurences
                OccurenceImpl occurence2 = new OccurenceImpl(elementInfo.getDeclarations(), nodeInfo.getRange());
                occurence2.setAccuracy(accuracy);
                occurences.add(occurence2);
            }
        }
    }

    private void buildTypeConstants(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Collection<? extends TypeElement> types = resolveTypes(index, elementInfo);
        if (!types.isEmpty()) {
            final Exact typeConstantName = NameKind.exact(elementInfo.getName());
            final Set<TypeConstantElement> constants = new HashSet<>();
            for (TypeElement typeElement : types) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                constants.addAll(ElementFilter.forName(typeConstantName).filter(index.getAllTypeConstants(typeElement)));
            }
            if (elementInfo.setDeclarations(constants)) {
                buildStaticConstantInvocations(elementInfo, fileScope, cachedOccurences);
                buildStaticConstantDeclarations(elementInfo, fileScope, cachedOccurences);
            }
        }
    }

    private void buildEnumCases(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Collection<? extends TypeElement> types = resolveTypes(index, elementInfo);
        if (!types.isEmpty()) {
            final Exact enumCaseName = NameKind.exact(elementInfo.getName());
            final Set<EnumCaseElement> enumCases = new HashSet<>();
            for (TypeElement typeElement : types) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                enumCases.addAll(ElementFilter.forName(enumCaseName).filter(index.getAllEnumCases(typeElement)));
            }
            if (elementInfo.setDeclarations(enumCases)) {
                buildEnumCaseDeclarations(elementInfo, cachedOccurences);
                buildEnumCaseInvocations(elementInfo, fileScope, cachedOccurences);
            }
        }
    }

    private void buildStaticMethods(final Index index, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Collection<? extends TypeElement> types = resolveTypes(index, elementInfo);
        if (!types.isEmpty()) {
            final Exact methodName = NameKind.exact(elementInfo.getName());
            final Set<MethodElement> methods = new HashSet<>();
            for (TypeElement typeElement : types) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                methods.addAll(ElementFilter.forName(methodName).filter(index.getAllMethods(typeElement)));
            }
            if (elementInfo.setDeclarations(methods)) {
                occurences.clear();
                buildStaticMethodInvocations(elementInfo, fileScope, occurences, false);
                if (OptionsUtils.codeCompletionStaticMethods()) {
                    buildMethodInvocations(elementInfo, fileScope, Accuracy.UNIQUE, occurences);
                }
                buildMethodDeclarations(elementInfo, fileScope, occurences);
                buildMagicMethodDeclarations(elementInfo, fileScope, cachedOccurences);
            }
        }
    }

    private void buildMagicMethodDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        for (Entry<MagicMethodDeclarationInfo, MethodScope> entry : magicMethodDeclarations.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            MagicMethodDeclarationInfo nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()));
            }
        }
    }

    private void buildMethodInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, Occurence.Accuracy accuracy, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final Set<? extends PhpElement> declarations = nodeCtxInfo.getDeclarations();
        Map<QualifiedName, TypeElement> notMatchingTypeNames = new HashMap<>();
        Map<QualifiedName, TypeElement> matchingTypeNames = new HashMap<>();
        for (PhpElement phpElement : declarations) {
            if (phpElement instanceof MethodElement) {
                final TypeElement type = ((MethodElement) phpElement).getType();
                matchingTypeNames.put(type.getFullyQualifiedName(), type);
            } else if (phpElement instanceof TypeElement) {
                final TypeElement type = (TypeElement) phpElement;
                matchingTypeNames.put(type.getFullyQualifiedName(), type);
            }
        }
        if (matchingTypeNames.size() > 0) {
            final Exact name = NameKind.exact(nodeCtxInfo.getQualifiedName());
            final ElementFilter nameFilter = ElementFilter.forName(name);
            for (Entry<ASTNodeInfo<MethodInvocation>, Scope> entry : methodInvocations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<MethodInvocation> nodeInfo = entry.getKey();
                if (name.matchesName(PhpElementKind.METHOD, nodeInfo.getQualifiedName())) {
                    final HashSet<TypeScope> types = new HashSet<>(getClassName((VariableScope) entry.getValue(), nodeInfo.getOriginalNode()));
                    if (types.isEmpty() || !createTypeFilter(matchingTypeNames.values(), false).filter(types).isEmpty()) {
                        final OccurenceImpl occurence = new OccurenceImpl(declarations, nodeInfo.getRange());
                        occurence.setAccuracy(accuracy);
                        occurences.add(occurence);
                    } else {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                        final Index index = indexScope.getIndex();
                        for (TypeScope typeScope : types) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (createTypeFilter(notMatchingTypeNames.values(), false).filter(typeScope).isEmpty()) {
                                final ElementFilter typeFilter = createTypeFilter(matchingTypeNames.values(), true);
                                final Set<MethodElement> methods = typeFilter.filter(
                                        nameFilter.filter(index.getAllMethods(typeScope)));
                                if (!methods.isEmpty()) {
                                    matchingTypeNames.put(typeScope.getFullyQualifiedName(), typeScope);
                                    final OccurenceImpl occurence = new OccurenceImpl(declarations, nodeInfo.getRange());
                                    occurence.setAccuracy(accuracy);
                                    occurences.add(occurence);
                                    break;
                                } else {
                                    notMatchingTypeNames.put(typeScope.getFullyQualifiedName(), typeScope);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<PhpElement> getUseAliasesDeclarations(ElementInfo nodeCtxInfo) {
        final Set<PhpElement> aliasDeclarations = new HashSet<>();
        Collection<? extends UseScope> declaredUses = nodeCtxInfo.getNamespaceScope().getAllDeclaredSingleUses();
        for (UseScope useElement : declaredUses) {
            UseAliasElement aliasElement = useElement.getAliasElement();
            if (aliasElement != null && aliasElement.getName().equals(nodeCtxInfo.getName())) {
                aliasDeclarations.add(aliasElement);
            }
        }
        return aliasDeclarations;
    }

    private void buildUseAliases(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof UseAliasElement) {
                UseAliasElement useAliasElement = (UseAliasElement) phpElement;
                for (Entry<ASTNodeInfo<Expression>, Scope> entry : useAliases.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                    if (nodeInfo.getName().equals(useAliasElement.getName())) {
                        occurences.add(new OccurenceImpl(useAliasElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildIncludes(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        String idName = nodeCtxInfo.getName();
        for (Entry<IncludeInfo, IncludeElement> entry : includes.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            IncludeInfo nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()));
            }
        }
    }

    private void buildConstantInvocations(final ElementInfo nodeCtxInfo, final FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            for (Entry<ASTNodeInfo<Scalar>, Scope> entry : constInvocations.entrySet()) {
                ASTNodeInfo<Scalar> nodeInfo = entry.getKey();
                if (nodeInfo.getName().length() > 0 && NameKind.exact(nodeInfo.getName()).matchesName(phpElement)) {
                    occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(elements), nodeInfo.getRange()));
                }
            }

            for (Entry<ASTNodeInfo<Expression>, Scope> entry : nsConstInvocations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                Expression originalNode = nodeInfo.getOriginalNode();
                QualifiedName qualifiedName = null;
                if (originalNode instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) originalNode;
                    qualifiedName = QualifiedName.create(namespaceName);
                } else if (originalNode instanceof Identifier) {
                    Identifier identifier = (Identifier) originalNode;
                    qualifiedName = QualifiedName.create(identifier);
                }
                if (qualifiedName != null) {
                    if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                        if (qualifiedName.getKind().isUnqualified()) {
                            occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(elements), nodeInfo.getRange()));
                        } else {
                            occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }
    }

    private void buildConstantDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        String idName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<Scalar>, ConstantElement> entry : constDeclarations.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ASTNodeInfo<Scalar> nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()));
            }
        }
        for (Entry<ConstantDeclarationInfo, ConstantElement> entry : constDeclarations53.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ClassConstantDeclarationInfo nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()));
            }
        }
    }

    private void buildStaticConstantDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof TypeConstantElement) {
                TypeConstantElement constantElement = (TypeConstantElement) phpElement;
                TypeElement typeElement = constantElement.getType();
                Exact typeName = NameKind.exact(typeElement.getFullyQualifiedName());
                Exact constName = NameKind.exact(constantElement.getName());
                for (Entry<ASTNodeInfo<Identifier>, ClassConstantElement> entry : classConstantDeclarations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<Identifier> nodeInfo = entry.getKey();
                    TypeScope typeScope = (TypeScope) entry.getValue().getInScope();
                    if (typeName.matchesName(typeScope)) {
                        if (constName.matchesName(PhpElementKind.TYPE_CONSTANT, nodeInfo.getName())) {
                            occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }

    }

    private void buildEnumCaseDeclarations(ElementInfo nodeCtxInfo, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof EnumCaseElement) {
                EnumCaseElement enumCaseElement = (EnumCaseElement) phpElement;
                TypeElement typeElement = enumCaseElement.getType();
                Exact typeName = NameKind.exact(typeElement.getFullyQualifiedName());
                Exact enumCaseName = NameKind.exact(enumCaseElement.getName());
                for (Entry<ASTNodeInfo<Identifier>, CaseElement> entry : caseDeclarations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<Identifier> nodeInfo = entry.getKey();
                    TypeScope typeScope = (TypeScope) entry.getValue().getInScope();
                    if (typeName.matchesName(typeScope)) {
                        if (enumCaseName.matchesName(PhpElementKind.ENUM_CASE, nodeInfo.getName())) {
                            occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }
    }

    private void buildFieldInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, Occurence.Accuracy accuracy, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final Set<? extends PhpElement> declarations = nodeCtxInfo.getDeclarations();
        Map<QualifiedName, TypeElement> notMatchingTypeNames = new HashMap<>();
        Map<QualifiedName, TypeElement> matchingTypeNames = new HashMap<>();
        for (PhpElement phpElement : declarations) {
            if (phpElement instanceof FieldElement) {
                final TypeElement type = ((FieldElement) phpElement).getType();
                matchingTypeNames.put(type.getFullyQualifiedName(), type);
            } else if (phpElement instanceof TypeElement) {
                final TypeElement type = (TypeElement) phpElement;
                matchingTypeNames.put(type.getFullyQualifiedName(), type);
            }
        }
        if (matchingTypeNames.size() > 0) {
            final Exact name = NameKind.exact(nodeCtxInfo.getQualifiedName());
            final ElementFilter nameFilter = ElementFilter.forName(name);
            for (Entry<ASTNodeInfo<FieldAccess>, Scope> entry : fieldInvocations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<FieldAccess> nodeInfo = entry.getKey();
                if (name.matchesName(PhpElementKind.FIELD, nodeInfo.getName())) {
                    final HashSet<TypeScope> types = new HashSet<>(getClassName((VariableScope) entry.getValue(), nodeInfo.getOriginalNode()));
                    if (types.isEmpty() || !createTypeFilter(matchingTypeNames.values(), false).filter(types).isEmpty()) {
                        final OccurenceImpl occurence = new OccurenceImpl(declarations, nodeInfo.getRange());
                        occurence.setAccuracy(accuracy);
                        occurences.add(occurence);
                    } else {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                        final Index index = indexScope.getIndex();
                        for (TypeScope typeScope : types) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (createTypeFilter(notMatchingTypeNames.values(), false).filter(typeScope).isEmpty()) {
                                final ElementFilter typeFilter = createTypeFilter(matchingTypeNames.values(), true);
                                final Set<FieldElement> fields = typeFilter.filter(
                                        nameFilter.filter(index.getAlllFields(typeScope)));
                                if (!fields.isEmpty()) {
                                    matchingTypeNames.put(typeScope.getFullyQualifiedName(), typeScope);
                                    final OccurenceImpl occurence = new OccurenceImpl(declarations, nodeInfo.getRange());
                                    occurence.setAccuracy(accuracy);
                                    occurences.add(occurence);
                                    break;
                                } else {
                                    notMatchingTypeNames.put(typeScope.getFullyQualifiedName(), typeScope);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildMethodDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof MethodElement) {
                final MethodElement method = (MethodElement) phpElement;
                final ElementFilter typeFilter = createTypeFilter(method.getType(), false);
                Exact methodName = NameKind.exact(method.getName());
                for (Entry<ASTNodeInfo<MethodDeclaration>, MethodScope> entry : methodDeclarations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<MethodDeclaration> nodeInfo = entry.getKey();
                    if (methodName.matchesName(PhpElementKind.METHOD, nodeInfo.getName())) {
                        if (typeFilter.isAccepted((TypeScope) entry.getValue().getInScope())) {
                            occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }
    }

    private static ElementFilter createTypeFilter(Collection<TypeElement> types, boolean forTypeMembers) {
        List<ElementFilter> typeFilters = new ArrayList<>();
        for (TypeElement typeElement : types) {
            typeFilters.add(createTypeFilter(typeElement, forTypeMembers));
        }
        return ElementFilter.anyOf(typeFilters);
    }

    private static ElementFilter createTypeFilter(final TypeElement typeToCompareWith, boolean forTypeMembers) {
        final ElementFilter typeFilter = new ElementFilter() {

            final ElementFilter filterDelegate = ElementFilter.anyOf(
                    ElementFilter.forEqualTypes(typeToCompareWith)/*
                     * ,
                     * ElementFilter.forSuperInterfaceName(typeToCompareWith.getFullyQualifiedName()),
                    ElementFilter.forSuperClassName(typeToCompareWith.getFullyQualifiedName())
                     */);

            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeElement) {
                    final TypeElement typeElement = (TypeElement) element;
                    if (filterDelegate.isAccepted(element)) {
                        return true;
                    }
                    ElementQuery elementQuery = typeToCompareWith.getElementQuery();
                    if (elementQuery instanceof ElementQuery.Index) {
                        return !ElementFilter.forEqualTypes(typeElement).filter(((Index) elementQuery).getInheritedByTypes(typeToCompareWith)).isEmpty();
                    }
                }
                return false;
            }
        };
        return !forTypeMembers ? typeFilter : new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    return typeFilter.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }

    private void buildStaticFieldInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Collection<QualifiedName> matchingTypeNames = new HashSet<>();
        Collection<QualifiedName> notMatchingTypeNames = new HashSet<>();
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();

        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof FieldElement) {
                FieldElement fieldElement = (FieldElement) phpElement;
                matchingTypeNames.add(fieldElement.getType().getFullyQualifiedName());
                QualifiedName typeQualifiedName = nodeCtxInfo.getTypeQualifiedName();
                if (typeQualifiedName != null) {
                    matchingTypeNames.add(typeQualifiedName);
                }
                Exact fieldName = NameKind.exact(phpElement.getName());
                for (Entry<ASTNodeInfo<StaticFieldAccess>, Scope> entry : staticFieldInvocations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<StaticFieldAccess> nodeInfo = entry.getKey();
                    Expression dispatcher = nodeInfo.getOriginalNode().getDispatcher();
                    if (!CodeUtils.isUniformVariableSyntax(dispatcher)) {
                        QualifiedName clzName = QualifiedName.create(dispatcher);
                        if (clzName == null) {
                            continue;
                        }
                        final Scope scope = entry.getValue().getInScope();
                        clzName = resolveClassName(clzName, scope);
                        if (clzName != null && clzName.toString().length() > 0) {
                            clzName = VariousUtils.getFullyQualifiedName(clzName, nodeInfo.getOriginalNode().getStartOffset(), scope);
                            if (fieldName.matchesName(PhpElementKind.FIELD, nodeInfo.getName())) {
                                QualifiedName fullyQualified = VariousUtils.getFullyQualifiedName(clzName, elementInfo.getRange().getStart(), scope);
                                final Exact typeName = NameKind.exact(fullyQualified);
                                boolean isTheSame = false;
                                //matches with other matching names
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                        isTheSame = true;
                                        break;
                                    }
                                }
                                //if not then query to index
                                if (!isTheSame) {
                                    boolean skipIt = false;
                                    for (QualifiedName notMatchingName : notMatchingTypeNames) {
                                        if (typeName.matchesName(PhpElementKind.CLASS, notMatchingName)) {
                                            skipIt = true;
                                            break;
                                        }
                                    }
                                    if (skipIt) {
                                        continue;
                                    }
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                                    final Index index = indexScope.getIndex();
                                    final ElementFilter forTheSameType = ElementFilter.forMembersOfType(fieldElement.getType());
                                    final Set<FieldElement> expectedFields = forTheSameType.filter(index.getAlllFields(NameKind.exact(clzName), fieldName));
                                    isTheSame = !expectedFields.isEmpty();
                                }
                                if (isTheSame) {
                                    //add into matching names
                                    matchingTypeNames.add(clzName);
                                    occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                } else {
                                    notMatchingTypeNames.add(clzName);
                                }
                            }
                        }
                    } else {
                        // uniform variable syntax
                        if (fieldName.matchesName(PhpElementKind.FIELD, nodeInfo.getName())) {
                            Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), dispatcher);
                            for (TypeScope type : types) {
                                if (CancelSupport.getDefault().isCancelled()) {
                                    return;
                                }
                                QualifiedName fqn = type.getFullyQualifiedName();
                                final Exact typeName = NameKind.exact(fqn);
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                        matchingTypeNames.add(fqn);
                                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                    } else {
                                        notMatchingTypeNames.add(fqn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildStaticMethodInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences, boolean onParentOnly) {
        Collection<QualifiedName> matchingTypeNames = new HashSet<>();
        Collection<QualifiedName> notMatchingTypeNames = new HashSet<>();
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof MethodElement) {
                MethodElement methodElement = (MethodElement) phpElement;
                matchingTypeNames.add(methodElement.getType().getFullyQualifiedName());
                QualifiedName typeQualifiedName = nodeCtxInfo.getTypeQualifiedName();
                if (typeQualifiedName != null) {
                    matchingTypeNames.add(typeQualifiedName);
                }
                Exact methodName = NameKind.exact(phpElement.getName());
                for (Entry<ASTNodeInfo<StaticMethodInvocation>, Scope> entry : staticMethodInvocations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<StaticMethodInvocation> nodeInfo = entry.getKey();
                    final Expression dispatcher = nodeInfo.getOriginalNode().getDispatcher();
                    QualifiedName clzName = QualifiedName.create(dispatcher);
                    if (clzName != null) {
                        final Scope scope = entry.getValue().getInScope();
                        if (onParentOnly
                                && !isParent(clzName)) {
                            continue;
                        }
                        clzName = resolveClassName(clzName, scope);
                        if (clzName != null
                                && clzName.toString().length() > 0
                                && methodName.matchesName(PhpElementKind.METHOD, nodeInfo.getName())) {
                            clzName = VariousUtils.getFullyQualifiedName(clzName, nodeInfo.getOriginalNode().getStartOffset(), scope);
                            final Exact typeName = NameKind.exact(clzName);
                            boolean isTheSame = false;
                            //matches with other matching names
                            for (QualifiedName matchingName : matchingTypeNames) {
                                if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                    isTheSame = true;
                                    break;
                                }
                            }
                            //if not then query to index
                            if (!isTheSame) {
                                boolean skipIt = false;
                                for (QualifiedName notMatchingName : notMatchingTypeNames) {
                                    if (typeName.matchesName(PhpElementKind.CLASS, notMatchingName)) {
                                        skipIt = true;
                                        break;
                                    }
                                }
                                if (skipIt) {
                                    continue;
                                }
                                if (CancelSupport.getDefault().isCancelled()) {
                                    return;
                                }
                                final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                                final Index index = indexScope.getIndex();
                                final ElementFilter forTheSameType = ElementFilter.forMembersOfType(methodElement.getType());
                                final Set<MethodElement> expectedMethods = forTheSameType.filter(index.getAllMethods(NameKind.exact(clzName), methodName));
                                isTheSame = !expectedMethods.isEmpty();
                            }
                            if (isTheSame) {
                                //add into matching names
                                matchingTypeNames.add(clzName);
                                occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                            } else {
                                notMatchingTypeNames.add(clzName);
                            }
                        }
                    } else {
                        // uniform variable syntax
                        assert CodeUtils.isUniformVariableSyntax(dispatcher) : dispatcher;
                        if (methodName.matchesName(PhpElementKind.METHOD, nodeInfo.getName())) {
                            Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), dispatcher);
                            for (TypeScope type : types) {
                                if (CancelSupport.getDefault().isCancelled()) {
                                    return;
                                }
                                QualifiedName fqn = type.getFullyQualifiedName();
                                final Exact typeName = NameKind.exact(fqn);
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                        matchingTypeNames.add(fqn);
                                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                    } else {
                                        notMatchingTypeNames.add(fqn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildEnumCaseInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Collection<QualifiedName> matchingTypeNames = new HashSet<>();
        Collection<QualifiedName> notMatchingTypeNames = new HashSet<>();
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof EnumCaseElement) {
                EnumCaseElement enumCaseElement = (EnumCaseElement) phpElement;
                matchingTypeNames.add(enumCaseElement.getType().getFullyQualifiedName());
                QualifiedName typeQualifiedName = nodeCtxInfo.getTypeQualifiedName();
                if (typeQualifiedName != null) {
                    matchingTypeNames.add(typeQualifiedName);
                }
                final Exact enumCaseName = NameKind.exact(phpElement.getName());
                for (Entry<ASTNodeInfo<StaticConstantAccess>, Scope> entry : staticConstantInvocations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<StaticConstantAccess> nodeInfo = entry.getKey();
                    final Expression dispatcher = nodeInfo.getOriginalNode().getDispatcher();
                    QualifiedName clzName = QualifiedName.create(dispatcher);
                    if (clzName != null) {
                        final TypeScope scope = ModelUtils.getTypeScope(entry.getValue());
                        clzName = resolveClassName(clzName, scope);
                        if (clzName != null && clzName.toString().length() > 0) {
                            clzName = VariousUtils.getFullyQualifiedName(clzName, nodeInfo.getOriginalNode().getStartOffset(), scope);
                            if (enumCaseName.matchesName(PhpElementKind.ENUM_CASE, nodeInfo.getName())) {
                                final Exact typeName = NameKind.exact(clzName);
                                boolean isTheSame = false;
                                //matches with other matching names
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (typeName.matchesName(PhpElementKind.ENUM, matchingName)) {
                                        isTheSame = true;
                                        break;
                                    }
                                }
                                //if not then query to index
                                if (!isTheSame) {
                                    boolean skipIt = false;
                                    for (QualifiedName notMatchingName : notMatchingTypeNames) {
                                        if (typeName.matchesName(PhpElementKind.ENUM, notMatchingName)) {
                                            skipIt = true;
                                            break;
                                        }
                                    }
                                    if (skipIt) {
                                        continue;
                                    }
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                                    final Index index = indexScope.getIndex();
                                    final ElementFilter forTheSameType = ElementFilter.forMembersOfType(enumCaseElement.getType());
                                    final Set<EnumCaseElement> expectedConstants = forTheSameType.filter(index.getAllEnumCases(NameKind.exact(clzName), enumCaseName));
                                    isTheSame = !expectedConstants.isEmpty();
                                }
                                if (isTheSame) {
                                    //add into matching names
                                    matchingTypeNames.add(clzName);
                                    occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                } else {
                                    notMatchingTypeNames.add(clzName);
                                }
                            }
                        }
                    } else {
                        // uniform variable syntax
                        assert CodeUtils.isUniformVariableSyntax(dispatcher) : dispatcher;
                        if (enumCaseName.matchesName(PhpElementKind.ENUM_CASE, nodeInfo.getName())) {
                            Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), dispatcher);
                            for (TypeScope type : types) {
                                if (CancelSupport.getDefault().isCancelled()) {
                                    return;
                                }
                                QualifiedName fqn = type.getFullyQualifiedName();
                                final Exact typeName = NameKind.exact(fqn);
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    if (typeName.matchesName(PhpElementKind.ENUM, matchingName)) {
                                        matchingTypeNames.add(fqn);
                                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                    } else {
                                        notMatchingTypeNames.add(fqn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildStaticConstantInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Collection<QualifiedName> matchingTypeNames = new HashSet<>();
        Collection<QualifiedName> notMatchingTypeNames = new HashSet<>();
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof TypeConstantElement) {
                TypeConstantElement constantElement = (TypeConstantElement) phpElement;
                matchingTypeNames.add(constantElement.getType().getFullyQualifiedName());
                QualifiedName typeQualifiedName = nodeCtxInfo.getTypeQualifiedName();
                if (typeQualifiedName != null) {
                    if (isParent(typeQualifiedName)) {
                        TypeScope scope = ModelUtils.getTypeScope(nodeCtxInfo.getModelElemnt());
                        if (scope != null) {
                            typeQualifiedName = resolveClassName(typeQualifiedName, scope);
                        }
                    }
                    matchingTypeNames.add(typeQualifiedName);
                }
                final Exact constantName = NameKind.exact(phpElement.getName());
                for (Entry<ASTNodeInfo<StaticConstantAccess>, Scope> entry : staticConstantInvocations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<StaticConstantAccess> nodeInfo = entry.getKey();
                    final Expression dispatcher = nodeInfo.getOriginalNode().getDispatcher();
                    QualifiedName clzName = QualifiedName.create(dispatcher);
                    // $this::CONSTANT;
                    if (dispatcher instanceof Variable
                            && "this".equalsIgnoreCase(CodeUtils.extractQualifiedName(((Variable) dispatcher).getName()))) { // NOI18N
                        clzName = QualifiedName.create(Type.SELF);
                    }
                    if (clzName != null) {
                        final TypeScope scope = ModelUtils.getTypeScope(entry.getValue());
                        clzName = resolveClassName(clzName, scope);
                        if (clzName != null && clzName.toString().length() > 0) {
                            clzName = VariousUtils.getFullyQualifiedName(clzName, nodeInfo.getOriginalNode().getStartOffset(), scope);
                            if (constantName.matchesName(PhpElementKind.TYPE_CONSTANT, nodeInfo.getName())) {
                                final Exact typeName = NameKind.exact(clzName);
                                boolean isTheSame = false;
                                //matches with other matching names
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                        isTheSame = true;
                                        break;
                                    }
                                }
                                //if not then query to index
                                if (!isTheSame) {
                                    boolean skipIt = false;
                                    for (QualifiedName notMatchingName : notMatchingTypeNames) {
                                        if (typeName.matchesName(PhpElementKind.CLASS, notMatchingName)) {
                                            skipIt = true;
                                            break;
                                        }
                                    }
                                    if (skipIt) {
                                        continue;
                                    }
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    final IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                                    final Index index = indexScope.getIndex();
                                    final ElementFilter forTheSameType = ElementFilter.forMembersOfType(constantElement.getType());
                                    final Set<TypeConstantElement> expectedConstants = forTheSameType.filter(index.getAllTypeConstants(NameKind.exact(clzName), constantName));
                                    isTheSame = !expectedConstants.isEmpty();
                                }
                                if (isTheSame) {
                                    //add into matching names
                                    matchingTypeNames.add(clzName);
                                    occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                } else {
                                    notMatchingTypeNames.add(clzName);
                                }
                            }
                        }
                    } else {
                        // uniform variable syntax
                        assert CodeUtils.isUniformVariableSyntax(dispatcher) : dispatcher;
                        if (constantName.matchesName(PhpElementKind.TYPE_CONSTANT, nodeInfo.getName())) {
                            Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), dispatcher);
                            for (TypeScope type : types) {
                                if (CancelSupport.getDefault().isCancelled()) {
                                    return;
                                }
                                QualifiedName fqn = type.getFullyQualifiedName();
                                final Exact typeName = NameKind.exact(fqn);
                                for (QualifiedName matchingName : matchingTypeNames) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    if (typeName.matchesName(PhpElementKind.CLASS, matchingName)) {
                                        matchingTypeNames.add(fqn);
                                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                                    } else {
                                        notMatchingTypeNames.add(fqn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildDocTagsForUseAliases(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                final PhpDocTypeTagInfo nodeInfo = entry.getKey();
                if (phpElement.getName().equals(nodeInfo.getName())) {
                    occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                }
            }
        }
    }

    private void buildDocTagsForClasses(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                PhpDocTypeTagInfo nodeInfo = entry.getKey();
                final QualifiedName qualifiedName = VariousUtils.getFullyQualifiedName(nodeInfo.getQualifiedName(), nodeInfo.getOriginalNode().getStartOffset(), entry.getValue());
                final String name = CodeUtils.removeNullableTypePrefix(nodeInfo.getName());
                if (StringUtils.hasText(name) && NameKind.exact(name).matchesName(PhpElementKind.CLASS, phpElement.getName())
                        && NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(elements), nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildClassInstanceCreation(ElementInfo query, FileScopeImpl fileScope, final List<Occurence> occurences) {
        buildClassInstanceCreation(query, fileScope, occurences, false);
    }

    private void buildClassInstanceCreation(ElementInfo query, FileScopeImpl fileScope, final List<Occurence> occurences, boolean forConstructMethod) {
        Set<? extends PhpElement> elements = query.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<ClassInstanceCreation>, Scope> entry : clasInstanceCreations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<ClassInstanceCreation> nodeInfo = entry.getKey();
                final boolean isAliased = VariousUtils.isAliased(nodeInfo.getQualifiedName(), nodeInfo.getOriginalNode().getStartOffset(), entry.getValue());
                // GH-4382: Find usages of the __construct method
                // also add alias of class instance creation
                // e.g.
                // use Example as ExampleAlias;
                // class Example {
                //	public function __construct(){}
                // }
                // $alias = new ExampleAlias();
                // $original = new Example();
                boolean isConstructorAlias = false;
                if (forConstructMethod) {
                    AliasedName aliasedName = VariousUtils.getAliasedName(nodeInfo.getQualifiedName(), nodeInfo.getOriginalNode().getStartOffset(), query.getScope());
                    if (aliasedName != null) {
                        QualifiedName realName = aliasedName.getRealName();
                        isConstructorAlias = phpElement.getName().equals(realName.getName());
                    }
                }
                if (!isAliased || nodeInfo.getQualifiedName().getSegments().size() > 1 || isConstructorAlias) {
                    final QualifiedName qualifiedName = VariousUtils.getFullyQualifiedName(
                            nodeInfo.getQualifiedName(),
                            nodeInfo.getOriginalNode().getStartOffset(),
                            entry.getValue());
                    Set<? extends PhpElement> contextTypes = elements;
                    if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                        if (qualifiedName.getKind().isUnqualified()) {
                            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, nodeInfo.getRange().getStart());
                            if (namespaceScope != null) {
                                Set<QualifiedName> allNames = new HashSet<>();
                                for (QualifiedName qn : VariousUtils.getComposedNames(qualifiedName, namespaceScope)) {
                                    if (!qn.getKind().isUnqualified() && !qn.isDefaultNamespace()) {
                                        allNames.add(qn.toNamespaceName());
                                    }
                                }
                                ElementFilter forTypesFromNamespace = ElementFilter.forTypesFromNamespaces(allNames);
                                contextTypes = forTypesFromNamespace.filter(elements);
                                if (contextTypes.isEmpty()) {
                                    contextTypes = elements;
                                } else if (!contextTypes.contains(phpElement)) {
                                    continue;
                                }
                            }
                        }

                        if (!qualifiedName.getKind().isUnqualified()) {
                            contextTypes = Collections.singleton(phpElement);
                        }
                        final OccurenceImpl occurenceImpl = new OccurenceImpl(
                                ElementFilter.forFiles(fileScope.getFileObject()).prefer(contextTypes), nodeInfo.getRange()) {

                            @Override
                            public Collection<? extends PhpElement> gotoDeclarations() {
                                Collection<PhpElement> result = new ArrayList<>(getAllDeclarations().size());
                                for (PhpElement element : getAllDeclarations()) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return Collections.emptyList();
                                    }
                                    ElementQuery elementQuery = element.getElementQuery();
                                    if (element instanceof TypeElement && elementQuery != null && elementQuery.getQueryScope().isIndexScope()) {
                                        ElementQuery.Index index = (ElementQuery.Index) elementQuery;
                                        Set<MethodElement> declaredMethods =
                                                ElementFilter.forName(NameKind.exact(MethodElement.CONSTRUCTOR_NAME)).filter(index.getDeclaredMethods((TypeElement) element));
                                        if (!declaredMethods.isEmpty()) {
                                            result.addAll(declaredMethods);
                                        }
                                    }
                                }
                                if (result.size() > 0) {
                                    return result;
                                }
                                return super.gotoDeclarations();
                            }
                        };
                        occurences.add(occurenceImpl);

                    }
                }
            }
        }
    }

    private void buildClassNames(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<ClassName>, Scope> entry : clasNames.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<ClassName> nodeInfo = entry.getKey();
                final QualifiedName qualifiedName = nodeInfo.getQualifiedName();
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(elements, nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildInterfaceIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<Expression>, Scope> entry : ifaceIDs.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                Set<? extends PhpElement> contextTypes = elements;
                final QualifiedName qualifiedName = nodeInfo.getQualifiedName();
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, nodeInfo.getRange().getStart());
                        if (namespaceScope != null) {
                            Set<QualifiedName> allNames = new HashSet<>();
                            for (QualifiedName qn : VariousUtils.getComposedNames(qualifiedName, namespaceScope)) {
                                if (!qn.getKind().isUnqualified() && !qn.isDefaultNamespace()) {
                                    allNames.add(qn.toNamespaceName());
                                }
                            }
                            ElementFilter forTypesFromNamespace = ElementFilter.forTypesFromNamespaces(allNames);
                            contextTypes = forTypesFromNamespace.filter(elements);
                            if (contextTypes.isEmpty()) {
                                contextTypes = elements;
                            } else if (!contextTypes.contains(phpElement)) {
                                continue;
                            }
                        }
                    }

                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(contextTypes), nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildTraitIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<Expression>, Scope> entry : traitIDs.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                Set<? extends PhpElement> contextTypes = elements;
                final QualifiedName qualifiedName = nodeInfo.getQualifiedName();
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, nodeInfo.getRange().getStart());
                        if (namespaceScope != null) {
                            Set<QualifiedName> allNames = new HashSet<>();
                            for (QualifiedName qn : VariousUtils.getComposedNames(qualifiedName, namespaceScope)) {
                                if (!qn.getKind().isUnqualified() && !qn.isDefaultNamespace()) {
                                    allNames.add(qn.toNamespaceName());
                                }
                            }
                            ElementFilter forTypesFromNamespace = ElementFilter.forTypesFromNamespaces(allNames);
                            contextTypes = forTypesFromNamespace.filter(elements);
                            if (contextTypes.isEmpty()) {
                                contextTypes = elements;
                            } else if (!contextTypes.contains(phpElement)) {
                                continue;
                            }
                        }
                    }

                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(contextTypes), nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildClassIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<Expression>, Scope> entry : clasIDs.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                final QualifiedName qualifiedName = VariousUtils.getFullyQualifiedName(nodeInfo.getQualifiedName(), nodeInfo.getOriginalNode().getStartOffset(), entry.getValue());
                Set<? extends PhpElement> contextTypes = elements;
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, nodeInfo.getRange().getStart());
                        if (namespaceScope != null) {
                            Set<QualifiedName> allNames = new HashSet<>();
                            for (QualifiedName qn : VariousUtils.getComposedNames(qualifiedName, namespaceScope)) {
                                if (!qn.getKind().isUnqualified() && !qn.isDefaultNamespace()) {
                                    allNames.add(qn.toNamespaceName());
                                }
                            }
                            ElementFilter forTypesFromNamespace = ElementFilter.forTypesFromNamespaces(allNames);
                            contextTypes = forTypesFromNamespace.filter(elements);
                            if (contextTypes.isEmpty()) {
                                contextTypes = elements;
                            } else if (!contextTypes.contains(phpElement)) {
                                continue;
                            }
                        }
                    }

                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(contextTypes),
                                nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildEnumIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<Expression>, Scope> entry : enumIDs.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                final QualifiedName qualifiedName = VariousUtils.getFullyQualifiedName(nodeInfo.getQualifiedName(), nodeInfo.getOriginalNode().getStartOffset(), entry.getValue());
                Set<? extends PhpElement> contextTypes = elements;
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, nodeInfo.getRange().getStart());
                        if (namespaceScope != null) {
                            Set<QualifiedName> allNames = new HashSet<>();
                            for (QualifiedName qn : VariousUtils.getComposedNames(qualifiedName, namespaceScope)) {
                                if (!qn.getKind().isUnqualified() && !qn.isDefaultNamespace()) {
                                    allNames.add(qn.toNamespaceName());
                                }
                            }
                            ElementFilter forTypesFromNamespace = ElementFilter.forTypesFromNamespaces(allNames);
                            contextTypes = forTypesFromNamespace.filter(elements);
                            if (contextTypes.isEmpty()) {
                                contextTypes = elements;
                            } else if (!contextTypes.contains(phpElement)) {
                                continue;
                            }
                        }
                    }

                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(contextTypes),
                                nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildInterfaceDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<InterfaceDeclarationInfo, InterfaceScope> entry : ifaceDeclarations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                InterfaceDeclarationInfo nodeInfo = entry.getKey();
                if (NameKind.exact(nodeInfo.getQualifiedName()).matchesName(phpElement)
                        && nodeInfo.getRange().containsInclusive(phpElement.getOffset())) {
                    if (fileScope.getFileObject() == phpElement.getFileObject()) {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildClassDeclarations(ElementInfo query, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = query.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ClassDeclarationInfo, ClassScope> entry : clasDeclarations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ClassDeclarationInfo nodeInfo = entry.getKey();
                if (NameKind.exact(nodeInfo.getQualifiedName()).matchesName(phpElement)
                        && nodeInfo.getRange().containsInclusive(phpElement.getOffset())) {
                    if (fileScope.getFileObject() == phpElement.getFileObject()) {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildTraitDeclarations(ElementInfo query, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = query.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<TraitDeclarationInfo, TraitScope> entry : traitDeclarations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                TraitDeclarationInfo nodeInfo = entry.getKey();
                if (NameKind.exact(nodeInfo.getQualifiedName()).matchesName(phpElement)
                        && nodeInfo.getRange().containsInclusive(phpElement.getOffset())) {
                    if (fileScope.getFileObject() == phpElement.getFileObject()) {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildEnumDeclarations(ElementInfo query, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = query.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<EnumDeclarationInfo, EnumScope> entry : enumDeclarations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                EnumDeclarationInfo nodeInfo = entry.getKey();
                if (NameKind.exact(nodeInfo.getQualifiedName()).matchesName(phpElement)
                        && nodeInfo.getRange().containsInclusive(phpElement.getOffset())) {
                    if (fileScope.getFileObject() == phpElement.getFileObject()) {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
        }
    }

    private void buildFunctionDeclarations(final ElementInfo nodeCtxInfo, final FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<FunctionDeclaration>, FunctionScope> entry : fncDeclarations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<FunctionDeclaration> nodeInfo = entry.getKey();
                if (NameKind.exact(nodeInfo.getQualifiedName()).matchesName(phpElement)) {
                    occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()));
                }
            }
        }
    }

    private void buildFunctionInvocations(final ElementInfo nodeCtxInfo, final FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            for (Entry<ASTNodeInfo<FunctionInvocation>, Scope> entry : fncInvocations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<FunctionInvocation> nodeInfo = entry.getKey();
                final QualifiedName qualifiedName = nodeInfo.getQualifiedName();
                if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                    if (qualifiedName.getKind().isUnqualified()) {
                        occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(elements), nodeInfo.getRange()));
                    } else {
                        occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                    }
                }
            }
            for (Entry<ASTNodeInfo<Expression>, Scope> entry : nsFunctionInvocations.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                ASTNodeInfo<Expression> nodeInfo = entry.getKey();
                Expression originalNode = nodeInfo.getOriginalNode();
                if (originalNode instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) originalNode;
                    final QualifiedName qualifiedName = QualifiedName.create(namespaceName);
                    if (NameKind.exact(qualifiedName).matchesName(phpElement)) {
                        if (qualifiedName.getKind().isUnqualified()) {
                            occurences.add(new OccurenceImpl(ElementFilter.forFiles(fileScope.getFileObject()).prefer(elements), nodeInfo.getRange()));
                        } else {
                            occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }
    }

    private void buildFieldDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof FieldElement) {
                FieldElement field = (FieldElement) phpElement;
                TypeElement typeElement = field.getType();
                Exact typeName = NameKind.exact(typeElement.getFullyQualifiedName());
                Exact fieldName = NameKind.exact(field.getName());
                for (Entry<SingleFieldDeclarationInfo, FieldElementImpl> entry : fldDeclarations.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    SingleFieldDeclarationInfo nodeInfo = entry.getKey();
                    Scope inScope = entry.getValue().getInScope();
                    if (inScope instanceof TypeScope) {
                        TypeScope typeScope = (TypeScope) inScope;
                        if (typeName.matchesName(typeScope)) {
                            if (fieldName.matchesName(PhpElementKind.FIELD, nodeInfo.getName())) {
                                occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildDocTagsForFields(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        Set<? extends PhpElement> elements = nodeCtxInfo.getDeclarations();
        for (PhpElement phpElement : elements) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (phpElement instanceof FieldElement) {
                FieldElement fieldElement = (FieldElement) phpElement;
                TypeElement typeElement = fieldElement.getType();
                Exact typeName = NameKind.exact(typeElement.getFullyQualifiedName());
                Exact fieldName = NameKind.exact(phpElement.getName());
                for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    PhpDocTypeTagInfo nodeInfo = entry.getKey();
                    Scope scope = entry.getValue();
                    if (Kind.FIELD.equals(nodeInfo.getKind()) && scope instanceof ClassScope) {
                        if (typeName.matchesName(((ClassScope) scope))) {
                            if (fieldName.matchesName(PhpElementKind.FIELD, nodeInfo.getName())) {
                                occurences.add(new OccurenceImpl(phpElement, nodeInfo.getRange()));
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildGotoStatements(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        buildGoto(nodeCtxInfo, gotoStatement, fileScope, occurences);
    }

    private void buildGotoLabels(final ElementInfo nodeCtxInfo, final FileScopeImpl fileScope, final List<Occurence> occurences) {
        buildGoto(nodeCtxInfo, gotoLabel, fileScope, occurences);
    }

    private <T extends ASTNode> void buildGoto(final ElementInfo nodeCtxInfo, final Map<ASTNodeInfo<T>, Scope> entries, FileScopeImpl fileScope, final List<Occurence> occurences) {
        String currentName = nodeCtxInfo.getName();
        Scope currentScope = nodeCtxInfo.getScope();
        for (Entry<ASTNodeInfo<T>, Scope> entry : entries.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ASTNodeInfo<T> nodeInfo = entry.getKey();
            String name = nodeInfo.getName();
            Scope scope = entry.getValue();
            if (currentName.equalsIgnoreCase(name) && currentScope == scope) {
                occurences.add(new OccurenceImpl(entry.getValue(), nodeInfo.getRange()) {

                    @Override
                    public Collection<? extends PhpElement> gotoDeclarations() {
                        return Collections.emptyList();
                    }
                });
            }
        }
    }

    private void buildDocTagsForVars(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        final Scope ctxScope = (nodeCtxInfo.getScope() instanceof VariableName || nodeCtxInfo.getScope() instanceof VarAssignmentImpl)
                ? nodeCtxInfo.getScope().getInScope()
                : nodeCtxInfo.getScope();
        if (!(ctxScope instanceof VariableScope)) {
            return;
        }
        final VariableScope ctxVarScope = (VariableScope) ctxScope;
        final ElementFilter nameFilter = ElementFilter.forName(NameKind.exact(nodeCtxInfo.getName()));
        final Set<VariableName> vars = nameFilter.filter(new HashSet<>(ctxVarScope.getDeclaredVariables()));
        final VariableName var = (vars.size() == 1) ? vars.iterator().next() : null;
        if (var != null) {
            for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                PhpDocTypeTagInfo nodeInfo = entry.getKey();
                Scope scope = entry.getValue();
                if (Kind.VARIABLE.equals(nodeInfo.getKind()) && scope instanceof VariableScope && !nodeInfo.getName().trim().isEmpty()
                        && NameKind.exact(nodeInfo.getName()).matchesName(PhpElementKind.VARIABLE, nodeCtxInfo.getName())) {
                    if (!var.isGloballyVisible()) {
                        Scope nextScope = entry.getValue();
                        if (ctxVarScope.equals(nextScope)) {
                            occurences.add(new OccurenceImpl(var, nodeInfo.getRange()));
                        }
                    } else {
                        Scope nextScope = entry.getValue();
                        if (nextScope instanceof VariableScope) {
                            final Set<VariableName> nextVars = nameFilter.filter(new HashSet<>(((VariableScope) nextScope).getDeclaredVariables()));
                            final VariableName nextVar = (nextVars.size() == 1) ? nextVars.iterator().next() : null;
                            if (nextVar != null && nextVar.isGloballyVisible()) {
                                occurences.add(new OccurenceImpl(var, nodeInfo.getRange()));
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildVariables(ElementInfo nodeCtxInfo, FileScopeImpl fileScope, final List<Occurence> occurences) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Scope ctxScope = nodeCtxInfo.getScope() instanceof VariableName ? nodeCtxInfo.getScope().getInScope() : nodeCtxInfo.getScope();
        if (ctxScope instanceof VarAssignmentImpl) {
            ctxScope = ctxScope.getInScope();
        }
        if (!(ctxScope instanceof VariableScope)) {
            return;
        }
        final VariableScope ctxVarScope = (VariableScope) ctxScope;
        String nodeName = nodeCtxInfo.getName();
        if (StringUtils.hasText(nodeName)) {
            final ElementFilter nameFilter = ElementFilter.forName(NameKind.exact(nodeName));
            final Set<VariableName> vars = nameFilter.filter(new HashSet<>(ctxVarScope.getDeclaredVariables()));
            final VariableName var = (vars.size() == 1) ? vars.iterator().next() : null;
            if (var != null) {
                for (Entry<ASTNodeInfo<Variable>, Scope> entry : variables.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ASTNodeInfo<Variable> nodeInfo = entry.getKey();
                    boolean addOccurence = false;
                    String name = nodeInfo.getName();
                    if (!StringUtils.hasText(name)) {
                        continue;
                    }
                    if (NameKind.exact(name).matchesName(PhpElementKind.VARIABLE, nodeName)) {
                        if (!var.isGloballyVisible()) {
                            Scope nextScope = entry.getValue();
                            if (var.representsThis() && nextScope.getInScope() instanceof TypeScope) {
                                final Scope inScope = ctxVarScope instanceof MethodScope ? ctxVarScope.getInScope() : ctxVarScope;
                                if (nextScope.getInScope().equals(inScope)) {
                                    addOccurence = true;
                                }
                            } else {
                                if (ctxVarScope.equals(nextScope)) {
                                    addOccurence = true;
                                }
                            }
                        } else {
                            Scope nextScope = entry.getValue();
                            if (nextScope instanceof VariableScope) {
                                final Set<VariableName> nextVars = nameFilter.filter(new HashSet<>(((VariableScope) nextScope).getDeclaredVariables()));
                                final VariableName nextVar = (nextVars.size() == 1) ? nextVars.iterator().next() : null;
                                if (nextVar != null && nextVar.isGloballyVisible()) {
                                    addOccurence = true;
                                }
                            }
                        }
                    }
                    if (addOccurence) {
                        if ((var instanceof VariableNameImpl) && (((VariableNameImpl) var).indexedElement instanceof PhpElement)) {
                            final VariableNameImpl nameImpl = (VariableNameImpl) var;
                            occurences.add(new OccurenceImpl(var, nodeInfo.getRange()) {

                                @Override
                                public Collection<? extends PhpElement> gotoDeclarations() {
                                    return Collections.singleton((PhpElement) nameImpl.indexedElement);
                                }
                            });
                        } else {
                            occurences.add(new OccurenceImpl(var, nodeInfo.getRange()));
                        }
                    }
                }
            }
        }
    }

    Occurence build(FileScopeImpl fileScope, final int offset) {
        Occurence retval = findOccurenceByOffset(offset);
        if (retval == null && setElementInfo(offset)) {
            build(fileScope);
            retval = findOccurenceByOffset(offset);
        }
        return retval;
    }

    List<Occurence> build(FileScopeImpl fileScope, final ModelElement element) {
        if (setElementInfo(element)) {
            build(fileScope);
        }
        return new ArrayList<>(cachedOccurences);
    }

    /**
     * This method ensure that all method bodies are scanned, if there were not
     * scanned yet.
     */
    private void scanMethodBodies() {
        for (Entry<ASTNodeInfo<MethodDeclaration>, MethodScope> entry : methodDeclarations.entrySet()) {
            if (entry.getValue() instanceof LazyBuild) {
                LazyBuild scope = (LazyBuild) entry.getValue();
                if (!scope.isScanned()) {
                    scope.scan();
                }
            }
        }
    }

    private Occurence findOccurenceByOffset(final int offset) {
        Occurence retval = null;
        for (Occurence occ : cachedOccurences) {
            assert occ != null;
            if (occ.getOccurenceRange().containsInclusive(offset)) {
                retval = occ;
            }
        }
        return retval;
    }

    private boolean canBePrepared(ASTNode node, ModelElement scope) {
        return scope != null && node != null;
    }

    private void setOffsetElementInfo(ElementInfo nextElementInfo, final int offset) {
        if (nextElementInfo != null && offset >= 0) {
            if (nextElementInfo.getName() != null && nextElementInfo.getName().trim().length() > 0) {
                OffsetRange range = nextElementInfo.getRange();
                if (range != null && range.containsInclusive(offset)) {
                    if (elementInfo != null
                            && elementInfo.getKind() == Kind.FIELD
                            && nextElementInfo.getKind() == Kind.VARIABLE) {
                        // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
                        // public function __construct(private int $x){}
                        // $x is both a field and a parameter
                        // so, we have to check both elements
                        promotedVariableElementInfo = nextElementInfo;
                    } else {
                        elementInfo = nextElementInfo;
                    }
                }
            }
        }
    }

    private static Collection<? extends TypeScope> getClassName(VariableScope scp, VariableBase varBase) {
        String vartype = VariousUtils.extractTypeFroVariableBase(varBase,
                Collections.<String, AssignmentImpl>emptyMap());
        return VariousUtils.getType(scp, vartype, varBase.getStartOffset(), true);
    }

    private static Collection<? extends TypeScope> getClassName(VariableScope scp, Expression expression) {
        String vartype = VariousUtils.extractVariableTypeFromExpression(expression, Collections.emptyMap());
        return VariousUtils.getType(scp, vartype, expression.getStartOffset(), false);
    }

    private static Collection<? extends TypeElement> resolveTypes(Index index, ElementInfo elementInfo) {
        ASTNodeInfo nodeInfo = elementInfo.getNodeInfo();
        if (nodeInfo != null) {
            ASTNode originalNode = nodeInfo.getOriginalNode();
            if (originalNode instanceof StaticDispatch) {
                Expression dispatcher = ((StaticDispatch) originalNode).getDispatcher();
                // possible uniform variable syntax
                if (CodeUtils.isUniformVariableSyntax(dispatcher)) {
                    // uniform variable syntax => try to resolve class
                    return getClassName((VariableScope) elementInfo.getScope(), dispatcher);
                }
            }
        }
        QualifiedName clzName = elementInfo.getTypeQualifiedName();
        assert clzName != null : elementInfo.getQualifiedName();
        TypeScope scope = ModelUtils.getTypeScope(elementInfo.getScope());
        clzName = resolveClassName(clzName, scope);
        if (clzName != null
                && clzName.toString().length() > 0) {
            if (!clzName.getKind().isFullyQualified()) {
                clzName = VariousUtils.getFullyQualifiedName(clzName.toName(), elementInfo.getRange().getStart(), scope);
            }
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return Collections.emptyList();
        }
        if (clzName != null) {
            return index.getTypes(NameKind.exact(clzName));
        }
        return Collections.emptyList();
    }

    private static boolean isNameEquality(ElementInfo query, ASTNodeInfo node, ModelElement nodeScope) {
        String idName = query.getName();
        if (idName.equalsIgnoreCase(node.getName())) {
            QualifiedName queryQN = query.getQualifiedName();
            QualifiedName nodeQN = node.getQualifiedName();
            if (queryQN.equals(nodeQN)) {
                return true;
            }
            final Collection<QualifiedName> queryComposedNames = VariousUtils.getComposedNames(queryQN, query.getNamespaceScope());
            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(nodeScope);
            assert namespaceScope != null;
            final Collection<QualifiedName> nodeQomposedNames = VariousUtils.getComposedNames(nodeQN, namespaceScope);
            queryComposedNames.retainAll(nodeQomposedNames);
            return !queryComposedNames.isEmpty();
        }
        return false;
    }

    // #247082 - 'self', '$this' etc. can be used also inside anonymous functions
    private static QualifiedName resolveClassName(QualifiedName clzName, Scope scope) {
        if (!clzName.getKind().isUnqualified()) {
            return clzName;
        }
        final String name = clzName.getName();
        if (name.equalsIgnoreCase("self") // NOI18N
                || name.equals("static")) {  // NOI18N
            TypeScope typeScope = ModelUtils.getTypeScope(scope);
            if (typeScope != null) {
                clzName = typeScope.getFullyQualifiedName();
            }
        } else if (isParent(clzName)) {
            ClassScope classScope = ModelUtils.getClassScope(scope);
            if (classScope != null) {
                clzName = classScope.getSuperClassName();
            }
        }
        return clzName;
    }

    private static boolean isParent(QualifiedName clzName) {
        if (!clzName.getKind().isUnqualified()) {
            return false;
        }
        return clzName.getName().equalsIgnoreCase("parent"); // NOI18N
    }

    //~ Inner classes

    private class OccurenceImpl implements Occurence {
        private final OffsetRange occurenceRange;
        private final PhpElement declaration;
        private Collection<? extends PhpElement> allDeclarations;
        private Accuracy accuracy = Accuracy.EXACT;

        public OccurenceImpl(Collection<? extends PhpElement> allDeclarations, OffsetRange occurenceRange) {
            this(allDeclarations, ModelUtils.getFirst(allDeclarations), occurenceRange);
        }

        public OccurenceImpl(PhpElement declaration, OffsetRange occurenceRange) {
            this(Collections.<PhpElement>singleton(declaration), occurenceRange);
        }

        private OccurenceImpl(Collection<? extends PhpElement> allDeclarations, PhpElement declaration, OffsetRange occurenceRange) {
            if ((declaration instanceof MethodScope) && ((MethodScope) declaration).isConstructor()) {
                ModelElement modelElement = (ModelElement) declaration;
                this.declaration = modelElement.getInScope();
            } else {
                this.allDeclarations = allDeclarations;
                this.declaration = declaration;
            }
            assert declaration != null;
            this.occurenceRange = occurenceRange;
        }

        @Override
        public PhpElementKind getKind() {
            return declaration.getPhpElementKind();
        }

        @Override
        public OffsetRange getOccurenceRange() {
            return occurenceRange;
        }

        @Override
        public Accuracy degreeOfAccuracy() {
            return accuracy;
        }

        @Override
        public Collection<? extends PhpElement> gotoDeclarations() {
            return new HashSet<>(allDeclarations);
        }

        public void setAccuracy(Accuracy accuracy) {
            this.accuracy = accuracy;
        }

        @Override
        public Collection<? extends PhpElement> getAllDeclarations() {
            return new HashSet<>(allDeclarations);
        }

        @Override
        public Collection<Occurence> getAllOccurences() {
            return cachedOccurences;
        }
    }

    private static class ElementInfo {
        private final Scope scope;
        private final Union2<ASTNodeInfo, ModelElement> element;
        public Set<? extends PhpElement> declarations = Collections.emptySet();

        public ElementInfo(ModelElement element) {
            this.element = Union2.createSecond(element);
            if (element instanceof Scope) {
                scope = (Scope) element;
            } else {
                scope = element.getInScope();
            }
        }

        public ElementInfo(ASTNodeInfo nodeInfo, ModelElement element) {
            this.element = Union2.createFirst(nodeInfo);
            if (element instanceof Scope) {
                scope = (Scope) element;
            } else {
                scope = element.getInScope();
            }
        }

        /**
         * @return the scope
         */
        public Scope getScope() {
            return scope;
        }

        public FileScope getFileScope() {
            return ModelUtils.getFileScope(scope);
        }

        public NamespaceScope getNamespaceScope() {
            return ModelUtils.getNamespaceScope(scope);
        }

        @CheckForNull
        public QualifiedName getTypeQualifiedName() {
            ASTNodeInfo nodeInfo = getNodeInfo();
            QualifiedName qualifiedName;
            if (nodeInfo != null) {
                ASTNode originalNode = nodeInfo.getOriginalNode();
                if ((nodeInfo instanceof ClassConstantDeclarationInfo || nodeInfo instanceof CaseDeclarationInfo)
                        && originalNode instanceof Identifier) {
                    if (getScope() instanceof TypeScope) {
                        return ((TypeScope) getScope()).getFullyQualifiedName();
                    }
                }
                if (originalNode instanceof StaticDispatch) {
                    if (CodeUtils.isUniformVariableSyntax((StaticDispatch) originalNode)) {
                        return null;
                    }
                    QualifiedName pureQualifiedName = ASTNodeInfo.toQualifiedName(originalNode, true);
                    qualifiedName = VariousUtils.getFullyQualifiedName(pureQualifiedName, originalNode.getStartOffset(), getScope());
                } else {
                    if (getScope().getInScope() instanceof TypeScope) {
                        if (originalNode instanceof MethodDeclaration
                                || originalNode instanceof SingleFieldDeclaration
                                || originalNode instanceof PHPDocMethodTag) { // NETBEANS-1861
                            return ((TypeScope) getScope().getInScope()).getFullyQualifiedName();
                        }
                    }
                    qualifiedName = nodeInfo.getQualifiedName();
                }
            } else {
                ModelElement modelElemnt = getModelElemnt();
                final QualifiedName namespaceName = modelElemnt.getNamespaceName();
                Scope inScope = modelElemnt.getInScope();
                if (inScope instanceof TypeScope) {
                    qualifiedName = namespaceName.append(inScope.getName());
                } else {
                    qualifiedName = namespaceName.append(modelElemnt.getName());
                }
            }
            return qualifiedName;
        }

        public QualifiedName getQualifiedName() {
            ASTNodeInfo nodeInfo = getNodeInfo();
            QualifiedName qualifiedName;
            if (nodeInfo != null) {
                qualifiedName = nodeInfo.getQualifiedName();
            } else {
                ModelElement modelElemnt = getModelElemnt();
                if (modelElemnt instanceof ClassMemberElement) {
                    qualifiedName = QualifiedName.createUnqualifiedName(modelElemnt.getName());
                } else {
                    final QualifiedName namespaceName = modelElemnt.getNamespaceName();
                    qualifiedName = namespaceName.append(modelElemnt.getName());
                }
            }
            return qualifiedName;
        }

        public Collection<QualifiedName> getComposedNames() {
            return VariousUtils.getComposedNames(getQualifiedName(), getNamespaceScope());
        }

        public String getName() {
            ASTNodeInfo nodeInfo = getNodeInfo();
            if (nodeInfo != null) {
                return nodeInfo.getName();
            }
            return getModelElemnt().getName();
        }

        public ASTNodeInfo.Kind getKind() {
            ASTNodeInfo nodeInfo = getNodeInfo();
            if (nodeInfo != null) {
                return nodeInfo.getKind();
            }
            ASTNodeInfo.Kind kind = null;
            ModelElement modelElemnt = getModelElemnt();
            PhpElementKind phpElementKind = modelElemnt.getPhpElementKind();
            switch (phpElementKind) {
                case CLASS:
                    kind = Kind.CLASS;
                    break;
                case TYPE_CONSTANT:
                    kind = Kind.CLASS_CONSTANT;
                    break;
                case CONSTANT:
                    kind = Kind.CONSTANT;
                    break;
                case FIELD:
                    kind = modelElemnt.getPhpModifiers().isStatic() ? Kind.STATIC_FIELD : Kind.FIELD;
                    break;
                case FUNCTION:
                    kind = Kind.FUNCTION;
                    break;
                case IFACE:
                    kind = Kind.IFACE;
                    break;
                case INCLUDE:
                    kind = Kind.INCLUDE;
                    break;
                case METHOD:
                    boolean isStatic = modelElemnt.getPhpModifiers().isStatic();
                    kind = isStatic ? Kind.STATIC_METHOD : Kind.METHOD;
                    break;
                case VARIABLE:
                    kind = Kind.VARIABLE;
                    break;
                case USE_ALIAS:
                    kind = Kind.USE_ALIAS;
                    break;
                case TRAIT:
                    kind = Kind.TRAIT;
                    break;
                case ENUM:
                    kind = Kind.ENUM;
                    break;
                case ENUM_CASE:
                    kind = Kind.ENUM_CASE;
                    break;
                default:
                    assert false : phpElementKind;
            }
            assert kind != null : phpElementKind;
            return kind;
        }

        public OffsetRange getRange() {
            ASTNodeInfo nodeInfo = getNodeInfo();
            if (nodeInfo != null) {
                return nodeInfo.getRange();
            }
            return getModelElemnt().getNameRange();
        }

        public Union2<ASTNodeInfo, ModelElement> getRawElement() {
            return element;
        }

        public ASTNodeInfo getNodeInfo() {
            return element.hasFirst() ? element.first() : null;
        }

        private ModelElement getModelElemnt() {
            return element.hasSecond() ? element.second() : null;
        }

        /**
         * @return the declarations
         */
        public Set<? extends PhpElement> getDeclarations() {
            return new HashSet<>(declarations);
        }

        /**
         * @param declarations the declarations to set
         */
        public boolean setDeclarations(Set<? extends PhpElement> declarations) {
            this.declarations = new HashSet<>(declarations);
            return this.declarations != null && !this.declarations.isEmpty();
        }
    }
}
