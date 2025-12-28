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
package org.netbeans.modules.php.editor.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.actions.IconsUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class NavigatorScanner {

    private static final Logger LOGGER = Logger.getLogger(NavigatorScanner.class.getName());
    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N
    private static final String FONT_INHERITED_COLOR = "<font color=\"#7D694A\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>"; //NOI18N
    private static ImageIcon interfaceIcon = null;
    private static ImageIcon traitIcon = null;
    private static ImageIcon enumIcon = null;
    private static ImageIcon enumCaseIcon = null;
    private static boolean isLogged = false;
    private final FileScope fileScope;
    private final Set<TypeElement> deprecatedTypes;

    public static NavigatorScanner create(Model model, boolean resolveDeprecatedElements) {
        return new NavigatorScanner(model, resolveDeprecatedElements);
    }

    private static final Comparator<TraitScope> TRAIT_SCOPE_COMPARATOR = (TraitScope o1, TraitScope o2) -> o1.getName().compareToIgnoreCase(o2.getName());

    private NavigatorScanner(Model model, boolean resolveDeprecatedElements) {
        fileScope = model.getFileScope();
        if (resolveDeprecatedElements) {
            if (!isLogged) {
                LOGGER.info("Resolving of deprecated elements in Navigator scanner - IDE will be possibly slow!");
                isLogged = true;
            }
            deprecatedTypes = ElementFilter.forDeprecated(true).filter(model.getIndexScope().getIndex().getTypes(NameKind.empty()));
        } else {
            deprecatedTypes = Collections.<TypeElement>emptySet();
        }
    }

    public List<? extends StructureItem> scan() {
        final List<StructureItem> items = new ArrayList<>();
        processNamespaces(items, fileScope.getDeclaredNamespaces());
        return items;
    }

    private void processNamespaces(List<StructureItem> items, Collection<? extends NamespaceScope> declaredNamespaces) {
        for (NamespaceScope nameScope : declaredNamespaces) {
            List<StructureItem> namespaceChildren = nameScope.isDefaultNamespace() ? items : new ArrayList<>();
            if (!nameScope.isDefaultNamespace()) {
                items.add(new PHPNamespaceStructureItem(nameScope, namespaceChildren));
            }
            Collection<? extends UseScope> declaredUses = nameScope.getAllDeclaredSingleUses();
            for (UseScope useElement : declaredUses) {
                namespaceChildren.add(new PHPUseStructureItem(useElement));
            }

            Collection<? extends FunctionScope> declaredFunctions = nameScope.getDeclaredFunctions();
            for (FunctionScope fnc : declaredFunctions) {
                if (fnc.isAnonymous()) {
                    continue;
                }
                List<StructureItem> variables = new ArrayList<>();
                namespaceChildren.add(new PHPFunctionStructureItem(fnc, variables));
            }
            Collection<? extends ConstantElement> declaredConstants = nameScope.getDeclaredConstants();
            for (ConstantElement constant : declaredConstants) {
                namespaceChildren.add(new PHPConstantStructureItem(constant, "const"));
            }
            processTypes(items, namespaceChildren, nameScope.getDeclaredTypes());
        }
    }

    private void processTypes(List<StructureItem> items, List<StructureItem> namespaceChildren, Collection<? extends TypeScope> declaredTypes) {
        for (TypeScope type : declaredTypes) {
            List<StructureItem> children = new ArrayList<>();
            if (type instanceof ClassScope) {
                namespaceChildren.add(new PHPClassStructureItem((ClassScope) type, children));
            } else if (type instanceof InterfaceScope) {
                namespaceChildren.add(new PHPInterfaceStructureItem((InterfaceScope) type, children));
            } else if (type instanceof TraitScope) {
                namespaceChildren.add(new PHPTraitStructureItem((TraitScope) type, children));
            } else if (type instanceof EnumScope) {
                namespaceChildren.add(new PHPEnumStructureItem((EnumScope) type, children));
            }

            // methods
            Set<String> declMethodNames = new HashSet<>();
            Collection<? extends MethodScope> declaredMethods = type.getDeclaredMethods();
            for (MethodScope method : declaredMethods) {
                // The method name doesn't have to be always defined during parsing.
                // For example when user writes in  a php doc @method and parsing is
                // started when there is no name yet.
                if (method.getName() != null && !method.getName().isEmpty()) {
                    List<StructureItem> variables = new ArrayList<>();
                    if (method.isConstructor()) {
                        children.add(new PHPConstructorStructureItem(method, variables));
                    } else {
                        children.add(new PHPMethodStructureItem(method, variables));
                    }
                    declMethodNames.add(method.getName());
                }
            }
            // inherited methods
            for (MethodScope inheritedMethod : type.getInheritedMethods()) {
                if (!inheritedMethod.getName().isEmpty() && !declMethodNames.contains(inheritedMethod.getName())) {
                    List<StructureItem> variables = new ArrayList<>();
                    if (inheritedMethod.isConstructor()) {
                        children.add(new PHPConstructorStructureItem(inheritedMethod, variables, true));
                    } else {
                        children.add(new PHPMethodStructureItem(inheritedMethod, variables, true));
                    }
                }
            }

            // constants
            Set<String> declClsConstantNames = new HashSet<>();
            Collection<? extends ClassConstantElement> declaredClsConstants = type.getDeclaredConstants();
            for (ClassConstantElement classConstant : declaredClsConstants) {
                children.add(new PHPClassConstantStructureItem(classConstant, "con")); //NOI18N
                declClsConstantNames.add(classConstant.getName());
            }
            // inherited constants
            for (ClassConstantElement inheritedConstant : type.getInheritedConstants()) {
                if (!declClsConstantNames.contains(inheritedConstant.getName())) {
                    children.add(new PHPClassConstantStructureItem(inheritedConstant, "con", true)); //NOI18N
                }
            }

            if (type instanceof ClassScope) {
                ClassScope cls = (ClassScope) type;
                // fields
                Set<String> declaredFieldNames = new HashSet<>();
                Collection<? extends FieldElement> declaredFields = cls.getDeclaredFields();
                for (FieldElement field : declaredFields) {
                    children.add(new PHPFieldStructureItem(field));
                    declaredFieldNames.add(field.getName());
                }
                // inherited fields
                for (FieldElement inheritedField : cls.getInheritedFields()) {
                    if (!declaredFieldNames.contains(inheritedField.getName())) {
                        children.add(new PHPFieldStructureItem(inheritedField, true));
                    }
                }
            }
            if (type instanceof TraitScope) {
                TraitScope trait = (TraitScope) type;
                Collection<? extends FieldElement> declaredFields = trait.getDeclaredFields();
                for (FieldElement field : declaredFields) {
                    children.add(new PHPFieldStructureItem(field));
                }
            }
            if (type instanceof EnumScope) {
                EnumScope enumScope = (EnumScope) type;
                Collection<? extends CaseElement> declaredEnumCases = enumScope.getDeclaredEnumCases();
                for (CaseElement enumCase : declaredEnumCases) {
                    children.add(new PHPEnumCaseStructureItem(enumCase, "ecase")); // NOI18N
                    declClsConstantNames.add(enumCase.getName());
                }
            }
        }
    }

    private boolean isDeprecatedType(String type, ModelElement modelElement) {
        boolean result = false;
        String typeName = CodeUtils.removeNullableTypePrefix(type);
        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(typeName), modelElement.getOffset(), modelElement.getInScope());
        for (TypeElement typeElement : deprecatedTypes) {
            if (typeElement.getFullyQualifiedName().equals(fullyQualifiedName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private abstract class PHPStructureItem implements StructureItem {

        private final ModelElement modelElement;
        private final List<? extends StructureItem> children;
        private final String sortPrefix;

        public PHPStructureItem(ModelElement elementHandle, List<? extends StructureItem> children, String sortPrefix) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }

        @Override
        public boolean equals(Object obj) {
            boolean thesame = false;
            if (obj instanceof PHPStructureItem) {
                PHPStructureItem item = (PHPStructureItem) obj;
                if (item.getName() != null && this.getName() != null) {
                    thesame = item.modelElement.getName().equals(modelElement.getName()) && item.modelElement.getOffset() == modelElement.getOffset();
                }
            }
            return thesame;
        }

        @Override
        public int hashCode() {
            //int hashCode = super.hashCode();
            int hashCode = 11;
            if (getName() != null) {
                hashCode = 31 * getName().hashCode() + hashCode;
            }
            hashCode = (int) (31 * getPosition() + hashCode);
            return hashCode;
        }

        @Override
        public String getName() {
            return modelElement.getName();
        }

        @Override
        public String getSortText() {
            return sortPrefix + modelElement.getName();
        }

        @Override
        public ElementHandle getElementHandle() {
            return modelElement.getPHPElement();
        }

        public ModelElement getModelElement() {
            return modelElement;
        }

        @Override
        public ElementKind getKind() {
            return modelElement.getPHPElement().getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return modelElement.getPHPElement().getModifiers();
        }

        @Override
        public boolean isLeaf() {
            return (children.isEmpty());
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        @Override
        public long getPosition() {
            return modelElement.getOffset();
        }

        @Override
        public long getEndPosition() {
            if (modelElement instanceof Scope) {
                final OffsetRange blockRange = ((Scope) modelElement).getBlockRange();
                if (blockRange != null) {
                    return blockRange.getEnd();
                }
            }
            return modelElement.getNameRange().getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

        protected void appendInterfaces(Collection<? extends InterfaceScope> interfaes, HtmlFormatter formatter) {
            boolean first = true;
            for (InterfaceScope interfaceScope : interfaes) {
                if (interfaceScope != null) {
                    if (!first) {
                        formatter.appendText(", ");  //NOI18N
                    } else {
                        first = false;
                    }
                    appendName(interfaceScope, formatter);
                }
            }
        }

        protected void appendUsedTraits(Collection<? extends TraitScope> usedTraits, HtmlFormatter formatter) {
            boolean first = true;
            List<TraitScope> traits = new ArrayList<>(usedTraits);
            traits.sort(TRAIT_SCOPE_COMPARATOR);
            for (TraitScope traitScope : traits) {
                if (!first) {
                    formatter.appendText(", ");  //NOI18N
                } else {
                    first = false;
                }
                appendName(traitScope, formatter);
            }
        }

        protected void appendConstantDescription(ConstantElement constant, HtmlFormatter formatter) {
            appendConstantDescription(constant, formatter, false);
        }

        protected void appendConstantDescription(ConstantElement constant, HtmlFormatter formatter, boolean isInherited) {
            if (constant.isDeprecated()) {
                formatter.deprecated(true);
            }
            if (isInherited) {
                formatter.appendHtml(FONT_INHERITED_COLOR);
            }
            formatter.appendText(getName());
            if (isInherited) {
                formatter.appendHtml(CLOSE_FONT);
            }
            if (constant.isDeprecated()) {
                formatter.deprecated(false);
            }
            if (constant instanceof ClassConstantElement) {
                ClassConstantElement classConstant = (ClassConstantElement) constant;
                if (StringUtils.hasText(classConstant.getDeclaredType())) {
                    processDeclaredType(classConstant, formatter, classConstant.getDeclaredType(), false);
                }
            }
            String value = constant.getValue();
            if (value != null) {
                formatter.appendText(" "); //NOI18N
                formatter.appendHtml(FONT_GRAY_COLOR);
                formatter.appendText(value);
                formatter.appendHtml(CLOSE_FONT);
            }
        }

        protected void appendFunctionDescription(FunctionScope function, HtmlFormatter formatter) {
            appendFunctionDescription(function, formatter, false);
        }

        protected void appendFunctionDescription(FunctionScope function, HtmlFormatter formatter, boolean isInherited) {
            formatter.reset();
            if (function == null) {
                return;
            }
            if (isInherited) {
                formatter.appendHtml(FONT_INHERITED_COLOR);
            }
            if (function.isDeprecated()) {
                formatter.deprecated(true);
            }
            formatter.appendText(function.getName());
            if (function.isDeprecated()) {
                formatter.deprecated(false);
            }
            formatter.appendText("(");   //NOI18N
            List<? extends ParameterElement> parameters = function.getParameters();
            if (parameters != null && !parameters.isEmpty()) {
                processParameters(function, formatter, parameters);
            }
            formatter.appendText(")");   //NOI18N
            if (isInherited) {
                formatter.appendHtml(CLOSE_FONT);
            }
            Collection<? extends String> returnTypes = function.getReturnTypeNames();
            String declaredReturnType = function.getDeclaredReturnType();
            if (StringUtils.hasText(declaredReturnType)) {
                processReturnTypes(function, formatter, declaredReturnType);
            } else if (!returnTypes.isEmpty()) {
                processReturnTypes(function, formatter, returnTypes);
            }
        }

        private void processParameters(FunctionScope function, HtmlFormatter formatter, List<? extends ParameterElement> parameters) {
            boolean first = true;
            for (ParameterElement formalParameter : parameters) {
                String name = formalParameter.getName();
                Set<TypeResolver> types = formalParameter.getTypes();
                if (name != null) {
                    if (!first) {
                        formatter.appendText(", "); //NOI18N
                    }
                    if (formalParameter.hasDeclaredType()) {
                        processDeclaredType(function, formatter, formalParameter.getDeclaredType(), false);
                    } else if (formalParameter.getPhpdocType() != null) {
                        processDeclaredType(function, formatter, formalParameter.getPhpdocType(), false);
                    } else {
                        assert types.isEmpty() : function.getName() + " has " + types.size() + " parameter(s)"; // NOI18N
                    }
                    formatter.appendText(name);
                    first = false;
                }
            }
        }

        private void processReturnTypes(FunctionScope function, HtmlFormatter formatter, Collection<? extends String> returnTypes) {
            formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N

            // ignore duplicate types e.g. ?MyClass, MyClass
            HashSet<String> ignoredTypes = new HashSet<>();
            returnTypes.stream().filter(returnType -> CodeUtils.isNullableType(returnType)).forEach(returnType -> {
                ignoredTypes.add(returnType.substring(1));
            });

            int i = 0;
            for (String type : returnTypes) {
                if (!ignoredTypes.contains(type)) {
                    i++;
                    if (i > 1) {
                        formatter.appendText(Type.getTypeSeparator(function.isReturnIntersectionType()));
                    }
                    processTypeName(type, function, formatter);
                }
            }
            formatter.appendHtml(CLOSE_FONT);
        }

        private void processReturnTypes(FunctionScope function, HtmlFormatter formatter, @NullAllowed String declaredReturnType) {
            processDeclaredType(function, formatter, declaredReturnType, true);
        }

        protected void processDeclaredType(ModelElement modelElement, HtmlFormatter formatter, @NullAllowed String declaredType, boolean isReturn) {
            if (declaredType == null) {
                return;
            }
            if (isReturn
                    || modelElement instanceof FieldElement
                    || modelElement instanceof ClassConstantElement) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); // NOI18N
            } else {
                formatter.appendHtml(FONT_GRAY_COLOR);
            }
            StringBuilder sb = new StringBuilder(declaredType.length());
            for (int i = 0; i < declaredType.length(); i++) {
                char c = declaredType.charAt(i);
                switch (c) {
                    case '(': // no break
                    case '?':
                        formatter.appendText(String.valueOf(c));
                        break;
                    case ')': // no break
                    case '|': // no break
                    case '&':
                        processTypeName(sb, modelElement, formatter);
                        formatter.appendText(String.valueOf(c));
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            if (sb.length() > 0) {
                processTypeName(sb, modelElement, formatter);
            }
            if (!isReturn && modelElement instanceof FunctionScope) { // parameter
                formatter.appendText(" "); // NOI18N
            }
            formatter.appendHtml(CLOSE_FONT);
        }
    }

    private void processTypeName(StringBuilder sb, ModelElement modelElement, HtmlFormatter formatter) {
        String type = sb.toString();
        if (sb.length() > 0) {
            sb.delete(0, sb.length());
            processTypeName(type, modelElement, formatter);
        }
    }

    private void processTypeName(String type, ModelElement element, HtmlFormatter formatter) {
        if (CodeUtils.isNullableType(type)) {
            formatter.appendText(CodeUtils.NULLABLE_TYPE_PREFIX);
        }
        String typeName = CodeUtils.removeNullableTypePrefix(type);
        boolean deprecatedType = isDeprecatedType(typeName, element);
        if (deprecatedType) {
            formatter.deprecated(true);
        }
        formatter.appendText(typeName);
        if (deprecatedType) {
            formatter.deprecated(false);
        }
    }

    protected void appendName(ModelElement modelElement, HtmlFormatter formatter) {
        String name = modelElement.getName();
        if (CodeUtils.isSyntheticTypeName(name)) {
            name = "{}"; // NOI18N
        }
        if (modelElement.isDeprecated()) {
            formatter.deprecated(true);
            formatter.appendText(name);
            formatter.deprecated(false);
        } else {
            formatter.appendText(name);
        }
    }


    private abstract class PHPStructureInheritedItem extends PHPStructureItem implements StructureItem.InheritedItem {

        private final boolean isInherited;

        public PHPStructureInheritedItem(ModelElement elementHandle, List<? extends StructureItem> children, String sortPrefix, boolean isInherited) {
            super(elementHandle, children, sortPrefix);
            this.isInherited = isInherited;
        }

        @Override
        public boolean isInherited() {
            return isInherited;
        }

        @Override
        public ElementHandle getDeclaringElement() {
            return getModelElement().getInScope();
        }
    }

    private class PHPFieldStructureItem extends PHPSimpleStructureItem implements StructureItem.InheritedItem {

        private final boolean isInherited;

        public PHPFieldStructureItem(FieldElement elementHandle) {
            this(elementHandle, false);
        }

        public PHPFieldStructureItem(FieldElement elementHandle, boolean isInherited) {
            super(elementHandle, "field"); //NOI18N
            this.isInherited = isInherited;
        }

        public FieldElement getField() {
            return (FieldElement) getElementHandle();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            FieldElement field = getField();
            if (field.isDeprecated()) {
                formatter.deprecated(true);
            }
            if (isInherited()) {
                formatter.appendHtml(FONT_INHERITED_COLOR);
            }
            formatter.appendText(field.getName());
            if (isInherited()) {
                formatter.appendHtml(CLOSE_FONT);
            }
            if (field.isDeprecated()) {
                formatter.deprecated(false);
            }
            if (StringUtils.hasText(field.getDefaultType())) {
                processDeclaredType(field, formatter, field.getDefaultType(), false);
            }
            return formatter.getText();
        }

        @Override
        public boolean isInherited() {
            return isInherited;
        }

        @Override
        public ElementHandle getDeclaringElement() {
            return getField().getInScope();
        }
    }

    private class PHPSimpleStructureItem extends PHPStructureItem {

        private String simpleText;

        public PHPSimpleStructureItem(ModelElement elementHandle, String prefix) {
            super(elementHandle, null, prefix);
            this.simpleText = elementHandle.getName();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(simpleText);
            return formatter.getText();
        }

    }

    private class PHPNamespaceStructureItem extends PHPStructureItem {
        public PHPNamespaceStructureItem(NamespaceScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "namespace"); //NOI18N
        }

        public NamespaceScope getNamespaceScope() {
            return (NamespaceScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            if (getNamespaceScope().isDeprecated()) {
                formatter.deprecated(true);
            }
            formatter.appendText(getName());
            if (getNamespaceScope().isDeprecated()) {
                formatter.deprecated(false);
            }
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.MODULE;
        }
    }

    private class PHPUseStructureItem extends PHPStructureItem {

        public PHPUseStructureItem(UseScope elementHandle) {
            super(elementHandle, null, "aaaa_use"); //NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            UseScope useElement = (UseScope) getElementHandle();
            String name = getName();
            boolean deprecatedType = isDeprecatedType(name, useElement);
            if (deprecatedType) {
                formatter.deprecated(true);
            }
            formatter.appendText(name);
            final AliasedName aliasedName = useElement.getAliasedName();
            if (aliasedName != null) {
                formatter.appendText(" as "); //NOI18N
                formatter.appendText(aliasedName.getAliasName());
            }
            if (deprecatedType) {
                formatter.deprecated(false);
            }
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

    }

    private class PHPClassStructureItem extends PHPStructureItem {
        private final String superClassName;
        private final Collection<? extends InterfaceScope> interfaces;
        private final Collection<? extends TraitScope> usedTraits;

        public PHPClassStructureItem(ClassScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
            superClassName = ModelUtils.getFirst(getClassScope().getSuperClassNames());
            interfaces = getClassScope().getSuperInterfaceScopes();
            usedTraits = getClassScope().getTraits();
        }

        private ClassScope getClassScope() {
            return (ClassScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendName(getClassScope(), formatter);
            if (superClassName != null) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                formatter.appendText(superClassName);
                formatter.appendHtml(CLOSE_FONT);
            }
            if (interfaces != null && interfaces.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); //NOI18N
                appendInterfaces(interfaces, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            if (usedTraits != null && usedTraits.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "#"); //NOI18N
                appendUsedTraits(usedTraits, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPConstantStructureItem extends PHPStructureItem {

        public PHPConstantStructureItem(ConstantElement elementHandle, String prefix) {
            super(elementHandle, null, prefix);
        }

        public ConstantElement getConstant() {
            return (ConstantElement) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendConstantDescription(getConstant(), formatter);
            return formatter.getText();
        }

    }

    private class PHPClassConstantStructureItem extends PHPConstantStructureItem implements StructureItem.InheritedItem {

        private boolean isInherited;

        public PHPClassConstantStructureItem(ConstantElement elementHandle, String prefix) {
            this(elementHandle, prefix, false);
        }

        public PHPClassConstantStructureItem(ConstantElement elementHandle, String prefix, boolean isInherited) {
            super(elementHandle, prefix);
            this.isInherited = isInherited;
        }

        @Override
        public boolean isInherited() {
            return isInherited;
        }

        @Override
        public ElementHandle getDeclaringElement() {
            return getConstant().getInScope();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendConstantDescription(getConstant(), formatter, isInherited());
            return formatter.getText();
        }

    }

    private class PHPFunctionStructureItem extends PHPStructureItem {

        public PHPFunctionStructureItem(FunctionScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "fn"); //NOI18N
        }

        public FunctionScope getFunctionScope() {
            return (FunctionScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendFunctionDescription(getFunctionScope(), formatter);
            return formatter.getText();
        }

    }

    private class PHPMethodStructureItem extends PHPStructureInheritedItem {

        public PHPMethodStructureItem(MethodScope elementHandle, List<? extends StructureItem> children) {
            this(elementHandle, children, false);
        }

        public PHPMethodStructureItem(MethodScope elementHandle, List<? extends StructureItem> children, boolean isInherited) {
            super(elementHandle, children, "fn", isInherited); //NOI18N
        }

        public MethodScope getMethodScope() {
            return (MethodScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendFunctionDescription(getMethodScope(), formatter, isInherited());
            return formatter.getText();
        }

    }

    private class PHPInterfaceStructureItem extends PHPStructureItem {

        @StaticResource
        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N
        private final Collection<? extends InterfaceScope> interfaces;

        public PHPInterfaceStructureItem(InterfaceScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
            interfaces = getInterfaceScope().getSuperInterfaceScopes();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (interfaceIcon == null) {
                interfaceIcon = IconsUtils.loadInterfaceIcon();
            }
            return interfaceIcon;
        }

        private InterfaceScope getInterfaceScope() {
            return (InterfaceScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendName(getInterfaceScope(), formatter);
            if (interfaces != null && interfaces.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "::"); //NOI18N
                appendInterfaces(interfaces, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPTraitStructureItem extends PHPStructureItem {

        private final Collection<? extends TraitScope> usedTraits;

        public PHPTraitStructureItem(ModelElement elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
            usedTraits = getTraitScope().getTraits();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (traitIcon == null) {
                traitIcon = IconsUtils.loadTraitIcon();
            }
            return traitIcon;
        }

        private TraitScope getTraitScope() {
            return (TraitScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendName(getTraitScope(), formatter);
            if (usedTraits != null && usedTraits.size() > 0) {
                formatter.appendHtml(FONT_GRAY_COLOR + "#"); //NOI18N
                appendUsedTraits(usedTraits, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

    }

    private class PHPEnumStructureItem extends PHPStructureItem {

        private final Collection<? extends InterfaceScope> interfaces;
        private final Collection<? extends TraitScope> usedTraits;
        private final QualifiedName backingType;

        public PHPEnumStructureItem(ModelElement elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "cl"); //NOI18N
            interfaces = getEnumScope().getSuperInterfaceScopes();
            usedTraits = getEnumScope().getTraits();
            backingType = getEnumScope().getBackingType();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (enumIcon == null) {
                enumIcon = IconsUtils.loadEnumIcon();
            }
            return enumIcon;
        }

        private EnumScope getEnumScope() {
            return (EnumScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendName(getEnumScope(), formatter);
            if (backingType != null) {
                formatter.appendHtml(FONT_GRAY_COLOR + "("); // NOI18N
                formatter.appendText(backingType.toString());
                formatter.appendHtml(")" + CLOSE_FONT); // NOI18N
            }
            if (interfaces != null && !interfaces.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR + ":"); // NOI18N
                appendInterfaces(interfaces, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            if (usedTraits != null && !usedTraits.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR + "#"); // NOI18N
                appendUsedTraits(usedTraits, formatter);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }
    }

    private class PHPEnumCaseStructureItem extends PHPStructureItem {

        public PHPEnumCaseStructureItem(CaseElement elementHandle, String prefix) {
            super(elementHandle, null, prefix);
        }

        public CaseElement getEnumCase() {
            return (CaseElement) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            if (getEnumCase().isDeprecated()) {
                formatter.deprecated(true);
            }
            formatter.appendText(getName());
            if (getEnumCase().isDeprecated()) {
                formatter.deprecated(false);
            }
            final CaseElement enumCase = getEnumCase();
            String value = enumCase.getValue();
            if (value != null) {
                formatter.appendText(" "); //NOI18N
                formatter.appendHtml(FONT_GRAY_COLOR); //NOI18N
                formatter.appendText(value);
                formatter.appendHtml(CLOSE_FONT);
            }
            return formatter.getText();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (enumCaseIcon == null) {
                enumCaseIcon = IconsUtils.loadEnumCaseIcon();
            }
            return enumCaseIcon;
        }
    }

    private class PHPConstructorStructureItem extends PHPStructureInheritedItem {

        public PHPConstructorStructureItem(MethodScope elementHandle, List<? extends StructureItem> children) {
            this(elementHandle, children, false);
        }

        public PHPConstructorStructureItem(MethodScope elementHandle, List<? extends StructureItem> children, boolean isInherited) {
            super(elementHandle, children, "con", isInherited); //NOI18N
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        public MethodScope getMethodScope() {
            return (MethodScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendFunctionDescription(getMethodScope(), formatter, isInherited());
            return formatter.getText();
        }

    }

}
