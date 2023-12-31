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
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.ElementTransformation;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.codegen.CGSGenerator.GenWay;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Constructor Getter Setter Info.
 *
 * @author Petr Pisl
 */
public final class CGSInfo {

    private String className;
    // cotain the class consructor?
    private boolean hasConstructor;
    private final List<Property> properties;
    private final List<Property> instanceProperties;
    private final List<Property> possibleGetters;
    private final List<Property> possibleSetters;
    private final List<Property> possibleGettersSetters;
    private final List<MethodProperty> possibleMethods;
    private final JTextComponent textComp;
    private final PhpVersion phpVersion;
    /**
     * how to generate  getters and setters method name
     */
    private CGSGenerator.GenWay howToGenerate;
    private boolean generateDoc;
    private boolean fluentSetter;
    private boolean isPublicModifier;
    @NullAllowed
    private Index index;

    private CGSInfo(JTextComponent textComp, PhpVersion phpVersion) {
        properties = new ArrayList<>();
        instanceProperties = new ArrayList<>();
        possibleGetters = new ArrayList<>();
        possibleSetters = new ArrayList<>();
        possibleGettersSetters = new ArrayList<>();
        possibleMethods = new ArrayList<>();
        className = null;
        this.textComp = textComp;
        hasConstructor = false;
        this.generateDoc = true;
        fluentSetter = false;
        isPublicModifier = true;
        this.howToGenerate = CGSGenerator.GenWay.AS_JAVA;
        this.phpVersion = phpVersion != null ? phpVersion : PhpVersion.getDefault();
    }

    public static CGSInfo getCGSInfo(JTextComponent textComp) {
        PhpVersion phpVersion = null;
        FileObject file = NavUtils.getFile(textComp.getDocument());
        if (file != null) {
            phpVersion = CodeUtils.getPhpVersion(file);
        }
        return getCGSInfo(textComp, phpVersion);
    }

    // for unit tests
    static CGSInfo getCGSInfo(JTextComponent textComp, PhpVersion phpVersion) {
        CGSInfo info = new CGSInfo(textComp, phpVersion);
        info.findPropertyInScope();
        return info;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Property> getInstanceProperties() {
        return instanceProperties;
    }

    public List<MethodProperty> getPossibleMethods() {
        return possibleMethods;
    }

    public List<Property> getPossibleGetters() {
        return possibleGetters;
    }

    public List<Property> getPossibleGettersSetters() {
        return possibleGettersSetters;
    }

    public List<Property> getPossibleSetters() {
        return possibleSetters;
    }

    public String getClassName() {
        return className;
    }

    public boolean hasConstructor() {
        return hasConstructor;
    }

    public GenWay getHowToGenerate() {
        return howToGenerate;
    }

    public void setHowToGenerate(GenWay howGenerate) {
        this.howToGenerate = howGenerate;
    }

    public boolean isGenerateDoc() {
        return generateDoc;
    }

    public void setGenerateDoc(boolean generateDoc) {
        this.generateDoc = generateDoc;
    }

    public boolean isFluentSetter() {
        return fluentSetter;
    }

    public void setFluentSetter(final boolean fluentSetter) {
        this.fluentSetter = fluentSetter;
    }

    public boolean isPublicModifier() {
        return isPublicModifier;
    }

    public void setPublicModifier(boolean isPublicModifier) {
        this.isPublicModifier = isPublicModifier;
    }

    public JTextComponent getComponent() {
        return textComp;
    }

    public PhpVersion getPhpVersion() {
        return phpVersion;
    }

    @CheckForNull
    public Index getIndex() {
        return index;
    }

    public TypeNameResolver createTypeNameResolver(MethodElement method) {
        TypeNameResolver result;
        if (method.getParameters().isEmpty()) {
            result = TypeNameResolverImpl.forNull();
        } else {
            Model model = ModelUtils.getModel(Source.create(getComponent().getDocument()), 300);
            if (model == null) {
                result = TypeNameResolverImpl.forNull();
            } else {
                result = CodegenUtils.createSmarterTypeNameResolver(method, model, getComponent().getCaretPosition());
            }
        }
        return result;
    }

    /**
     * Extract attributes and methods from caret enclosing class and initialize list of properties.
     */
    private void findPropertyInScope() {
        FileObject file = NavUtils.getFile(textComp.getDocument());
        if (file == null) {
            return;
        }
        try {
            ParserManager.parse(Collections.singleton(Source.create(textComp.getDocument())), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    initProperties(resultIterator);
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void initProperties(ResultIterator resultIterator) throws ParseException {
        PHPParseResult info = (PHPParseResult) resultIterator.getParserResult();
        if (info != null) {
            int caretOffset = textComp.getCaretPosition();
            ASTNode typeDecl = findEnclosingType(info, caretOffset);
            if (typeDecl != null) {
                className = getTypeName(typeDecl);
                if (className != null) {
                    FileObject fileObject = info.getSnapshot().getSource().getFileObject();
                    index = ElementQueryFactory.getIndexQuery(info);
                    final ElementFilter forFilesFilter = ElementFilter.forFiles(fileObject);
                    QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(
                            QualifiedName.create(className),
                            caretOffset,
                            info.getModel().getVariableScope(caretOffset));
                    Set<TypeElement> types = forFilesFilter.filter(index.getTypes(NameKind.exact(fullyQualifiedName)));
                    for (TypeElement typeElement : types) {
                        ElementFilter forNotDeclared = ElementFilter.forExcludedElements(index.getDeclaredMethods(typeElement));
                        final Set<MethodElement> accessibleMethods = new HashSet<>();
                        accessibleMethods.addAll(forNotDeclared.filter(index.getAccessibleMethods(typeElement, typeElement)));
                        if (typeElement instanceof ClassElement) {
                            accessibleMethods.addAll(
                                    ElementFilter.forExcludedElements(accessibleMethods).filter(forNotDeclared.filter(index.getConstructors((ClassElement) typeElement))));
                        }
                        accessibleMethods.addAll(
                                ElementFilter.forExcludedElements(accessibleMethods).filter(forNotDeclared.filter(index.getAccessibleMagicMethods(typeElement))));
                        final Set<TypeElement> preferedTypes = forFilesFilter.prefer(ElementTransformation.toMemberTypes().transform(accessibleMethods));
                        final TreeElement<TypeElement> enclosingType = index.getInheritedTypesAsTree(typeElement, preferedTypes);
                        final List<MethodProperty> methodProperties = new ArrayList<>();
                        final Set<MethodElement> methods = ElementFilter.forMembersOfTypes(preferedTypes).filter(accessibleMethods);
                        for (final MethodElement methodElement : methods) {
                            if (!methodElement.isFinal()) {
                                methodProperties.add(new MethodProperty(methodElement, enclosingType, phpVersion));
                            }
                        }
                        Collections.<MethodProperty>sort(methodProperties, MethodProperty.getComparator());
                        getPossibleMethods().addAll(methodProperties);
                    }
                }

                List<String> existingGetters = new ArrayList<>();
                List<String> existingSetters = new ArrayList<>();

                PropertiesVisitor visitor = new PropertiesVisitor(existingGetters, existingSetters, Utils.getRoot(info));
                visitor.scan(typeDecl);
                if (typeDecl instanceof EnumDeclaration) {
                    // Enum can't have a constructor
                    // to avoid adding the list of code generators, change this
                    hasConstructor = true;
                }
                String propertyName;
                boolean existGetter, existSetter;
                for (Property property : getProperties()) {
                    propertyName = property.getName().toLowerCase();
                    existGetter = existingGetters.contains(propertyName);
                    existSetter = existingSetters.contains(propertyName);
                    if (!existGetter && !existSetter) {
                        getPossibleGettersSetters().add(property);
                        getPossibleGetters().add(property);
                        getPossibleSetters().add(property);
                    } else if (!existGetter) {
                        getPossibleGetters().add(property);
                    } else if (!existSetter) {
                        getPossibleSetters().add(property);
                    }
                }
            }
        }
    }

    /**
     * Find out the type enclosing the caret.
     *
     * @param info parser result
     * @param offset caret offset
     * @return type declaration or class instance creation(anonymous class),
     * otherwise {@code null}
     */
    @CheckForNull
    private ASTNode findEnclosingType(ParserResult info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        int count = nodes.size();
        if (count > 2) {  // the cursor has to be in class block see issue #142417
            ASTNode declaration = nodes.get(count - 2);
            ASTNode block = nodes.get(count - 1);
            if (block instanceof Block && isValidEnclosingType(declaration)) {
                return declaration;
            }
        }
        return null;
    }

    private boolean isValidEnclosingType(ASTNode typeDeclaration) {
        return typeDeclaration instanceof ClassDeclaration
                || typeDeclaration instanceof TraitDeclaration
                || typeDeclaration instanceof EnumDeclaration
                || (typeDeclaration instanceof ClassInstanceCreation && ((ClassInstanceCreation) typeDeclaration).isAnonymous());
    }

    @CheckForNull
    private String getTypeName(@NullAllowed ASTNode typeDeclaration) {
        if (typeDeclaration == null) {
            return null;
        }
        String typeName = null;
        if (typeDeclaration instanceof TypeDeclaration) {
            typeName = ((TypeDeclaration) typeDeclaration).getName().getName();
        } else if (typeDeclaration instanceof ClassInstanceCreation) {
            typeName = CodeUtils.extractClassName((ClassInstanceCreation) typeDeclaration);
        } else {
            assert false : "Expected: TypeDeclaration or ClassInstanceCreation, but got" + typeDeclaration.getClass(); // NOI18N
        }
        return typeName;
    }

    private class PropertiesVisitor extends DefaultVisitor {

        private final List<String> existingGetters;
        private final List<String> existingSetters;
        private final Program program;

        public PropertiesVisitor(List<String> existingGetters, List<String> existingSetters, Program program) {
            this.existingGetters = existingGetters;
            this.existingSetters = existingSetters;
            this.program = program;
        }

        @Override
        public void visit(FieldsDeclaration node) {
            List<SingleFieldDeclaration> fields = node.getFields();
            for (SingleFieldDeclaration singleFieldDeclaration : fields) {
                Variable variable = singleFieldDeclaration.getName();
                if (variable != null && variable.getName() instanceof Identifier) {
                    String name = ((Identifier) variable.getName()).getName();
                    String type = getPropertyType(node, singleFieldDeclaration);
                    Property property = new Property(name, node.getModifier(), type);
                    if (!BodyDeclaration.Modifier.isStatic(node.getModifier())) {
                        getInstanceProperties().add(property);
                    }
                    getProperties().add(property);
                }
            }
        }

        private String getPropertyType(FieldsDeclaration fieldsDeclaration, SingleFieldDeclaration singleFieldDeclaration) {
            String type = ""; // NOI18N
            if (fieldsDeclaration.getFieldType() == null || !phpVersion.hasPropertyTypes()) {
                type = getPropertyType(singleFieldDeclaration);
            } else {
                // PHP 7.4 or newer
                if (fieldsDeclaration.getFieldType() instanceof UnionType) {
                    type = VariousUtils.getUnionType((UnionType) fieldsDeclaration.getFieldType());
                } else if (fieldsDeclaration.getFieldType() instanceof IntersectionType) {
                    // NETBEANS-5599 PHP 8.1 Pure intersection types
                    type = VariousUtils.getIntersectionType((IntersectionType) fieldsDeclaration.getFieldType());
                } else {
                    QualifiedName qualifiedName = QualifiedName.create(fieldsDeclaration.getFieldType());
                    if (qualifiedName != null) {
                        type = qualifiedName.toString();
                        if (fieldsDeclaration.getFieldType() instanceof NullableType) {
                            type = CodeUtils.NULLABLE_TYPE_PREFIX + type;
                        }
                    }
                }
                assert !type.isEmpty() : "couldn't get the qualified name from the field type(" + fieldsDeclaration.getFieldType() + ")"; // NOI18N
                // if type is empty, check QualifiedName.create method (and fix it if posiible)
                // or get type name using another way
            }
            return type;
        }

        private String getPropertyType(final ASTNode node) {
            String result = ""; //NOI18N
            Comment comment = Utils.getCommentForNode(program, node);
            if (comment instanceof PHPDocBlock) {
                result = getFirstTypeFromBlock((PHPDocBlock) comment);
            }
            return result;
        }

        private String getFirstTypeFromBlock(final PHPDocBlock phpDoc) {
            String result = ""; //NOI18N
            for (PHPDocTag pHPDocTag : phpDoc.getTags()) {
                if (pHPDocTag instanceof PHPDocTypeTag && pHPDocTag.getKind().equals(PHPDocTag.Type.VAR)) {
                    result = getFirstTypeFromTag((PHPDocTypeTag) pHPDocTag);
                    if (!result.isEmpty()) {
                        break;
                    }
                }
            }
            return result;
        }

        private String getFirstTypeFromTag(final PHPDocTypeTag typeTag) {
            boolean canBeNull = canBeNull(typeTag);
            String result = ""; //NOI18N
            for (PHPDocTypeNode typeNode : typeTag.getTypes()) {
                String type = typeNode.getValue();
                if (phpVersion.hasScalarAndReturnTypes()
                        && !VariousUtils.isSpecialClassName(type)
                        && !Type.isInvalidPropertyType(type)) {
                    result = typeNode.isArray() ? Type.ARRAY : type;
                    if (canBeNull && phpVersion.hasNullableTypes()) {
                        result = CodeUtils.NULLABLE_TYPE_PREFIX + result;
                    }
                    break;
                } else if (!Type.isPrimitive(type) && !VariousUtils.isSpecialClassName(type)) {
                    result = typeNode.isArray() ? Type.ARRAY : type;
                    break;
                }
            }
            return result;
        }

        private boolean canBeNull(final PHPDocTypeTag typeTag) {
            boolean canBeNull = false;
            if (typeTag.getTypes().size() > 1) {
                for (PHPDocTypeNode typeNode : typeTag.getTypes()) {
                    String type = typeNode.getValue().toLowerCase(Locale.ROOT);
                    if (type.equals(Type.NULL)) {
                        canBeNull = true;
                        break;
                    }
                }
            }
            return canBeNull;
        }

        @Override
        public void visit(MethodDeclaration node) {
            String name = node.getFunction().getFunctionName().getName();
            String possibleProperty;
            if (CodeUtils.isConstructor(node)) {
                // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
                for (FormalParameter parameter : node.getFunction().getFormalParameters()) {
                    FieldsDeclaration fieldsDeclaration = FieldsDeclaration.create(parameter);
                    if (fieldsDeclaration != null) {
                        scan(fieldsDeclaration);
                    }
                }
            }
            if (name != null) {
                if (name.startsWith(CGSGenerator.START_OF_GETTER)) {
                    possibleProperty = name.substring(CGSGenerator.START_OF_GETTER.length());
                    existingGetters.addAll(getAllPossibleProperties(possibleProperty));
                } else if (name.startsWith(CGSGenerator.START_OF_SETTER)) {
                    possibleProperty = name.substring(CGSGenerator.START_OF_GETTER.length());
                    existingSetters.addAll(getAllPossibleProperties(possibleProperty));
                } else if (className != null && (className.equals(name) || "__construct".equals(name))) { //NOI18N
                    hasConstructor = true;
                }
            }
        }

        /**
         * Returns all possible properties which are based on the passed property derived from method name.
         *
         * @param possibleProperty Name of the property which was derived from method name (setField() -> field).
         * @return field => (field, _field) OR _field => (_field, field)
         */
        private List<String> getAllPossibleProperties(String possibleProperty) {
            List<String> allPossibleProperties = new LinkedList<>();
            String lowerCasePossibleProperty = possibleProperty.toLowerCase(Locale.ROOT);
            allPossibleProperties.add(lowerCasePossibleProperty);
            if (lowerCasePossibleProperty.startsWith("_")) { // NOI18N
                allPossibleProperties.add(lowerCasePossibleProperty.substring(1));
            } else {
                allPossibleProperties.add("_" + lowerCasePossibleProperty); // NOI18N
            }
            return allPossibleProperties;
        }
    }
}
