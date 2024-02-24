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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.ArrowFunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.LambdaFunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
class FunctionScopeImpl extends ScopeImpl implements FunctionScope, VariableNameFactory {

    private static final Logger LOGGER = Logger.getLogger(FunctionScopeImpl.class.getName());
    private static final String TYPE_SEPARATOR_REGEXP = "\\|"; //NOI18N
    private static final String TYPE_SEPARATOR_INTERSECTION_REGEXP = "\\&"; //NOI18N
    private List<? extends ParameterElement> paremeters;
    private final boolean hasDeclaredReturnType;
    //@GuardedBy("this")
    @NullAllowed
    private String returnType;
    @NullAllowed
    private final String declaredReturnType;
    private final boolean isReturnUnionType;
    private final boolean isReturnIntersectionType;

    //new contructors
    FunctionScopeImpl(Scope inScope, FunctionDeclarationInfo info, String returnType, boolean isDeprecated) {
        super(inScope, info, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), info.getOriginalNode().getBody(), isDeprecated);
        this.paremeters = info.getParameters();
        this.returnType = returnType;
        hasDeclaredReturnType = !info.getReturnTypes().isEmpty();
        this.declaredReturnType = hasDeclaredReturnType ? CodeUtils.extractQualifiedName(info.getOriginalNode().getReturnType()) : null;
        isReturnUnionType = info.getOriginalNode().getReturnType() instanceof UnionType;
        isReturnIntersectionType = info.getOriginalNode().getReturnType() instanceof IntersectionType;
    }

    FunctionScopeImpl(Scope inScope, LambdaFunctionDeclarationInfo info) {
        super(inScope, info, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), info.getOriginalNode().getBody(), inScope.isDeprecated());
        this.paremeters = info.getParameters();
        isReturnUnionType = info.getOriginalNode().getReturnType() instanceof UnionType;
        isReturnIntersectionType = info.getOriginalNode().getReturnType() instanceof IntersectionType;
        this.hasDeclaredReturnType = info.getOriginalNode().getReturnType() != null;
        if (this.hasDeclaredReturnType) {
            this.returnType = CodeUtils.extractQualifiedName(info.getOriginalNode().getReturnType());
            this.declaredReturnType = returnType;
        } else {
            this.returnType = null;
            this.declaredReturnType = null;
        }
    }

    FunctionScopeImpl(Scope inScope, ArrowFunctionDeclarationInfo info, Block block) {
        super(inScope, info, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), block, inScope.isDeprecated());
        this.paremeters = info.getParameters();
        isReturnUnionType = info.getOriginalNode().getReturnType() instanceof UnionType;
        isReturnIntersectionType = info.getOriginalNode().getReturnType() instanceof IntersectionType;
        this.hasDeclaredReturnType = info.getOriginalNode().getReturnType() != null;
        if (this.hasDeclaredReturnType) {
            this.returnType = CodeUtils.extractQualifiedName(info.getOriginalNode().getReturnType());
            this.declaredReturnType = returnType;
        } else {
            this.returnType = null;
            this.declaredReturnType = null;
        }
    }

    protected FunctionScopeImpl(Scope inScope, MethodDeclarationInfo info, String returnType, boolean isDeprecated) {
        super(inScope, info, info.getAccessModifiers(), info.getOriginalNode().getFunction().getBody(), isDeprecated);
        this.paremeters = info.getParameters();
        this.returnType = returnType;
        hasDeclaredReturnType = info.getOriginalNode().getFunction().getReturnType() != null;
        this.declaredReturnType = hasDeclaredReturnType ? CodeUtils.extractQualifiedName(info.getOriginalNode().getFunction().getReturnType()) : null;
        isReturnUnionType = info.getOriginalNode().getFunction().getReturnType() instanceof UnionType;
        isReturnIntersectionType = info.getOriginalNode().getFunction().getReturnType() instanceof IntersectionType;
    }

    protected FunctionScopeImpl(Scope inScope, MagicMethodDeclarationInfo info, String returnType, boolean isDeprecated) {
        super(inScope, info, info.getAccessModifiers(), null, isDeprecated);
        this.paremeters = info.getParameters();
        this.returnType = returnType;
        this.declaredReturnType = null;
        hasDeclaredReturnType = false;
        isReturnUnionType = false;
        isReturnIntersectionType = false;
    }

    FunctionScopeImpl(Scope inScope, BaseFunctionElement indexedFunction) {
        this(inScope, indexedFunction, PhpElementKind.FUNCTION);
    }

    protected FunctionScopeImpl(Scope inScope, final BaseFunctionElement element, PhpElementKind kind) {
        super(inScope, element, kind);
        this.paremeters = element.getParameters();
        this.returnType =  element.asString(PrintAs.ReturnSemiTypes);
        this.declaredReturnType = element.getDeclaredReturnType();
        this.hasDeclaredReturnType = StringUtils.hasText(declaredReturnType);
        isReturnUnionType = element.isReturnUnionType();
        isReturnIntersectionType = element.isReturnIntersectionType();
    }

    public static FunctionScopeImpl createElement(Scope scope, LambdaFunctionDeclaration node) {
        return new FunctionScopeImpl(scope, LambdaFunctionDeclarationInfo.create(node)) {
            @Override
            public boolean isAnonymous() {
                return true;
            }
        };
    }

    public static FunctionScopeImpl createElement(Scope scope, ArrowFunctionDeclaration node) {
        Expression expression = node.getExpression();
        int startOffset = expression.getStartOffset();
        int endOffset = expression.getEndOffset();
        if (expression instanceof ASTErrorExpression) {
            // increase the end offset if there are white spaces behind it
            // e.g. fn($x) => ;
            endOffset += getWhitespacesBehindASTErrorExpression(scope, endOffset);
        }
        Block block = new Block(startOffset, endOffset, Collections.emptyList(), false);
        return new ArrowFunctionScopeImpl(scope, ArrowFunctionDeclarationInfo.create(node), block);
    }

    private static int getWhitespacesBehindASTErrorExpression(Scope scope, int endOffset) {
        FileObject fileObject = scope.getFileObject();
        int wsCount = 0;
        if (fileObject != null) {
            BaseDocument document = GsfUtilities.getDocument(fileObject, true);
            Scope inScope = scope.getInScope();
            if (document != null && inScope != null) {
                int length = inScope.getBlockRange().getEnd() - endOffset;
                if (length > 0) {
                    try {
                        char[] chars = document.getChars(endOffset, length);
                        for (char c : chars) {
                            if (c == ' ' || c == '\t') {
                                wsCount++;
                            } else {
                                break;
                            }
                        }
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.WARNING, "Invalid offset: " + ex.offsetRequested(), ex); // NOI18N
                    }
                }
            }
        }
        return wsCount;
    }

    //old contructors

    /**
     * Add new return type but <b>only if the return type is not defined
     * in its declaration already</b> (in such a case, this new return type
     * is simply ignored).
     * @param type return type to be added
     */
    public void addReturnType(String type) {
        if (hasDeclaredReturnType) {
            return;
        }
        synchronized (this) {
            if (!StringUtils.hasText(returnType)) {
                returnType = type;
            } else {
                Set<String> distinctTypes = new HashSet<>();
                distinctTypes.addAll(Arrays.asList(returnType.split(TYPE_SEPARATOR_REGEXP)));
                distinctTypes.add(type);
                returnType = Type.asUnionType(distinctTypes);
            }
        }
    }

    protected synchronized String getReturnType() {
        return returnType;
    }

    @CheckForNull
    @Override
    public String getDeclaredReturnType() {
        return declaredReturnType;
    }

    @Override
    public Collection<? extends TypeScope> getReturnTypes() {
        return getReturnTypesDescriptor(getReturnType(), false).getModifiedResult(Collections.<TypeScope>emptyList());
    }

    @Override
    public synchronized Collection<? extends String> getReturnTypeNames() {
        Collection<String> retval = Collections.<String>emptyList();
        String type = getReturnType();
        if (type != null && type.length() > 0) {
            retval = new ArrayList<>();
            String[] typeNames = isReturnIntersectionType ? type.split(TYPE_SEPARATOR_INTERSECTION_REGEXP) : type.split(TYPE_SEPARATOR_REGEXP);
            for (String typeName : typeNames) {
                if (!VariousUtils.isSemiType(typeName)) {
                    retval.add(typeName);
                }
            }
        }
        return retval;
    }

    @Override
    public Collection<? extends TypeScope> getReturnTypes(boolean resolveSemiTypes, Collection<? extends TypeScope> callerTypes) {
        assert callerTypes != null;
        String types = getReturnType();
        // NETBEANS-5062
        Scope inScope = getInScope();
        Set<TypeScope> cTypes = new HashSet<>();
        List<String> typeNames = Arrays.asList(Type.splitTypes(types));
        if (typeNames.contains(Type.STATIC)
                && inScope instanceof TypeScope) {
            TypeScope typeScope = (TypeScope) inScope;
            for (TypeScope callerType : callerTypes) {
                if (callerType.isSubTypeOf(typeScope)) {
                    cTypes.add(callerType);
                } else {
                    cTypes.add(typeScope);
                }
            }
        } else {
            cTypes.addAll(callerTypes);
        }
        Collection<? extends TypeScope> result = getReturnTypesDescriptor(types, resolveSemiTypes, cTypes).getModifiedResult(cTypes);
        if (!hasDeclaredReturnType) {
            updateReturnTypes(types, result);
        }
        return result;
    }

    @Override
    public boolean isReturnUnionType() {
        return isReturnUnionType;
    }

    @Override
    public boolean isReturnIntersectionType() {
        return isReturnIntersectionType;
    }

    private static Set<String> recursionDetection = new HashSet<>(); //#168868

    private ReturnTypesDescriptor getReturnTypesDescriptor(String types, boolean resolveSemiTypes) {
        return getReturnTypesDescriptor(types, resolveSemiTypes, Collections.emptyList());
    }

    private ReturnTypesDescriptor getReturnTypesDescriptor(String types, boolean resolveSemiTypes, Collection<? extends TypeScope> callerTypes) {
        ReturnTypesDescriptor result = ReturnTypesDescriptor.NONE;
        if (StringUtils.hasText(types)) {
            final String[] typeNames = Type.splitTypes(types);
            Collection<TypeScope> retval = new HashSet<>();
            for (int i = 0; i < typeNames.length; i++) {
                String typeName = typeNames[i];
                if (CodeUtils.isNullableType(typeName)) {
                    typeName = typeName.substring(1);
                }
                if (Type.STATIC.equals(typeName)) {
                    typeName = "\\" + typeName; // NOI18N
                }
                if (isSpecialTypeName(typeName)) {
                    typeNames[i] = typeName;
                    continue;
                }
                if (typeName.trim().length() > 0) {
                    boolean added = false;
                    try {
                        added = recursionDetection.add(typeName);
                        if (added && recursionDetection.size() < 15) {
                            if (resolveSemiTypes && VariousUtils.isSemiType(typeName)) {
                                retval.addAll(VariousUtils.getType(this, typeName, getLastValidMethodOffset(), false, callerTypes));
                            } else {
                                String modifiedTypeName = typeName;
                                if (typeName.indexOf("[") != -1) { //NOI18N
                                    modifiedTypeName = typeName.replaceAll("\\[.*\\]", ""); //NOI18N
                                }
                                retval.addAll(IndexScopeImpl.getTypes(QualifiedName.create(modifiedTypeName), this));
                            }
                        }
                    } finally {
                        if (added) {
                            recursionDetection.remove(typeName);
                        }
                    }
                }
            }
            Arrays.sort(typeNames); // must sort
            Scope inScope = getInScope();
            if (canBeSelfDependent(inScope, typeNames)) {
                retval.add(((TypeScope) inScope));
            }

            if (canBeParentDependent(inScope, typeNames)) {
                if (inScope instanceof ClassScope) {
                    ClassScope classScope = (ClassScope) inScope;
                    classScope.getSuperClasses().forEach(superClass -> retval.add(superClass));
                } else if (inScope instanceof TraitScope) {
                    for (TypeScope callerType : callerTypes) {
                        if (callerType instanceof ClassScope) {
                            ClassScope classScope = (ClassScope) callerType;
                            if (classScope.getTraits().contains((TraitScope) inScope)) {
                                classScope.getSuperClasses().forEach(superClass -> retval.add(superClass));
                            }
                        }
                    }
                }
            }

            if (canBeCallerDependent(inScope, typeNames)) {
                result = new CallerDependentTypesDescriptor(retval);
            } else {
                result = new CommonTypesDescriptor(retval);
            }
        }
        return result;
    }

    private static boolean canBeSelfDependent(Scope scope, String[] types) {
        if (scope instanceof ClassScope || scope instanceof InterfaceScope) { // not trait
            return containsSelfDependentType(types);
        }
        return false;
    }

    private static boolean canBeParentDependent(Scope scope, String[] types) {
        if (scope instanceof ClassScope || scope instanceof TraitScope) {
            return containsParentDependentType(types);
        }
        return false;
    }

    private static boolean canBeCallerDependent(Scope scope, String[] types) {
        if (scope instanceof TraitScope) {
            return containsCallerDependentType(types) || containsSelfDependentType(types);
        }
        return containsCallerDependentType(types);
    }

    private int getLastValidMethodOffset() {
        int result = getOffset();
        List<? extends ModelElement> elements = ModelUtils.getElements(this, true);
        if (elements != null && !elements.isEmpty()) {
            elements.sort(new ModelElementsPositionComparator());
            result = elements.get(0).getNameRange().getEnd();
        }
        return result;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static final class ModelElementsPositionComparator implements Comparator<ModelElement> {

        @Override
        public int compare(ModelElement o1, ModelElement o2) {
            int o1End = o1.getNameRange().getEnd();
            int o2End = o2.getNameRange().getEnd();
            // furthest first
            if (o1End < o2End) {
                return 1;
            } else if (o1End > o2End) {
                return -1;
            }
            return 0;
        }

    }

    private static boolean containsCallerDependentType(String[] typeNames) {
        return (Arrays.binarySearch(typeNames, "\\this") >= 0) || (Arrays.binarySearch(typeNames, "\\static") >= 0); //NOI18N
    }

    private static boolean containsSelfDependentType(String[] typeNames) {
        return (Arrays.binarySearch(typeNames, "\\self") >= 0) || (Arrays.binarySearch(typeNames, Type.OBJECT) >= 0); //NOI18N
    }

    private static boolean containsParentDependentType(String[] typeNames) {
        return (Arrays.binarySearch(typeNames, "\\parent") >= 0); // NOI18N
    }

    private static boolean isSpecialTypeName(String typeName) {
        return typeName.equals("\\this") //NOI18N
                || typeName.equals("\\static") //NOI18N
                || typeName.equals("\\self") //NOI18N
                || typeName.equals("\\parent") //NOI18N
                || typeName.equals(Type.OBJECT);
    }

    private void updateReturnTypes(String oldTypes, Collection<? extends TypeScope> resolvedReturnTypes) {
        if (VariousUtils.isSemiType(oldTypes)) {
            updateSemiReturnTypes(oldTypes, resolvedReturnTypes);
        }
    }

    private void updateSemiReturnTypes(String oldTypes, Collection<? extends TypeScope> resolvedReturnTypes) {
        StringBuilder sb = new StringBuilder();
        for (TypeScope typeScope : resolvedReturnTypes) {
            if (sb.length() != 0) {
                sb.append(Type.SEPARATOR);
            }
            sb.append(typeScope.getNamespaceName().append(typeScope.getName()).toString());
        }
        updateReturnTypesIfNotChanged(oldTypes, sb.toString());
    }

    private synchronized void updateReturnTypesIfNotChanged(String oldTypes, String newTypes) {
        if (oldTypes.equals(getReturnType()) && StringUtils.hasText(newTypes)) {
            returnType = newTypes;
        }
    }

    @NonNull
    @Override
    public List<? extends String> getParameterNames() {
        assert paremeters != null;
        List<String> parameterNames = new ArrayList<>();
        for (ParameterElement parameter : paremeters) {
            parameterNames.add(parameter.getName());
        }
        return parameterNames;
    }

    @NonNull
    @Override
    public List<? extends ParameterElement> getParameters() {
        return Collections.unmodifiableList(paremeters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append('('); // NOI18N
        List<? extends String> parameters = getParameterNames();
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            if (i > 0) {
                sb.append(',').append(' '); // NOI18N
            }
            sb.append(param);
        }
        sb.append(')'); // NOI18N
        sb.append(':'); // NOI18N
        boolean first = true;
        if (hasDeclaredReturnType) {
            sb.append(' ').append(getDeclaredReturnType());
        } else {
            Collection<? extends TypeScope> returnTypes = getReturnTypes();
            for (TypeScope typeScope : returnTypes) {
                if (first) {
                    first = false;
                    sb.append(' '); // NOI18N
                } else {
                    sb.append(Type.getTypeSeparator(isReturnIntersectionType));
                }
                sb.append(typeScope.getName());
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.VARIABLE);
            }
        });
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_BASE, getIndexSignature(), true, true);
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
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
        String type = getReturnType();
        if (type != null) {
            sb.append(type);
        }
        sb.append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        sb.append(isReturnUnionType() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(isReturnIntersectionType()? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append((getDeclaredReturnType() != null) ? getDeclaredReturnType() : "").append(Signature.ITEM_DELIMITER); // NOI18N
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof FunctionElement) {
            FunctionElement indexedFunction = (FunctionElement) indexedElement;
            return indexedFunction.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }


    private interface ReturnTypesDescriptor {
        ReturnTypesDescriptor NONE = new ReturnTypesDescriptor() {

            @Override
            public Collection<? extends TypeScope> getModifiedResult(Collection<? extends TypeScope> callerTypes) {
                return Collections.emptyList();
            }
        };

        Collection<? extends TypeScope> getModifiedResult(Collection<? extends TypeScope> callerTypes);

    }

    private static final class CommonTypesDescriptor implements ReturnTypesDescriptor {
        private final Collection<? extends TypeScope> rawTypes;

        public CommonTypesDescriptor(Collection<? extends TypeScope> rawTypes) {
            assert rawTypes != null;
            this.rawTypes = rawTypes;
        }

        @Override
        public Collection<? extends TypeScope> getModifiedResult(Collection<? extends TypeScope> callerTypes) {
            assert callerTypes != null;
            return rawTypes;
        }

    }

    private static final class CallerDependentTypesDescriptor implements ReturnTypesDescriptor {

        private final Collection<? extends TypeScope> rawTypes;

        public CallerDependentTypesDescriptor(Collection<? extends TypeScope> rawTypes) {
            assert rawTypes != null;
            this.rawTypes = rawTypes;
        }

        @Override
        public Collection<? extends TypeScope> getModifiedResult(Collection<? extends TypeScope> callerTypes) {
            assert callerTypes != null;
            HashSet<TypeScope> types = new HashSet<>(rawTypes);
            types.addAll(callerTypes);
            return types;
        }

    }
}
