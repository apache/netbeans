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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.*;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring.Equality;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ralph Ruijs
 */
public class IntroduceLocalExtensionTransformer extends RefactoringVisitor {

    private static final Logger LOG = Logger.getLogger(IntroduceLocalExtensionTransformer.class.getName());
    private static final String DELEGATE = "delegate"; //NOI18N
    private final IntroduceLocalExtensionRefactoring refactoring;
    private Problem problem;
    private String fqn;
    private boolean initialized;
    private final Map<ElementHandle<Element>, String[]> getterSetterMap;

    public IntroduceLocalExtensionTransformer(IntroduceLocalExtensionRefactoring refactoring) {
        this.refactoring = refactoring;
        this.initialized = false;
        this.getterSetterMap = new HashMap<>();
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        if (!initialized) {
            FileObject origin = SourceUtils.getFile(refactoring.getRefactoringSource().lookup(TreePathHandle.class).getElementHandle(), workingCopy.getClasspathInfo());
            if (origin != null) {
                SourceUtils.forceSource(workingCopy, origin);
            }
        }
        super.setWorkingCopy(workingCopy);
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        if (!initialized) {
            String packageName = refactoring.getPackageName();
            FileObject sourceRoot = refactoring.getSourceRoot();
            String name = refactoring.getNewName();
            boolean wrap = refactoring.getWrap();
            fqn = packageName + '.' + name;

            GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
            CodeStyle cs = RefactoringUtils.getCodeStyle(workingCopy);

            TypeElement source = (TypeElement) refactoring.getRefactoringSource().lookup(TreePathHandle.class).getElementHandle().resolve(workingCopy);

            boolean noInterface = source.getKind() != ElementKind.INTERFACE;
            boolean samePackage = packageName.contentEquals(workingCopy.getElements().getPackageOf(source).getQualifiedName());
            
            List<TypeParameterTree> newTypeParams = new ArrayList<>(source.getTypeParameters().size());
            transformTypeParameters(source, source.getTypeParameters(), make, genUtils, newTypeParams);

            List<Tree> implementsList = wrap ? addInterfaces(source) : Collections.EMPTY_LIST;

            // add members
            List<Tree> members = new ArrayList<>();
            Modifier needed;
            if(wrap) {
                if(samePackage) {
                    needed = null;
                } else {
                    needed = Modifier.PUBLIC;
                }
            } else {
                if(samePackage) {
                    needed = null;
                } else {
                    needed = Modifier.PROTECTED;
                }
            }
            addConstructors(source, members, needed);

            if (wrap && noInterface) {
                Tree type = make.Type(source.asType());
                VariableTree field = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), DELEGATE, type, null); //NOI18N
                members.add(0, field);
                addFields(source, cs, members);
            }

            if (wrap && noInterface) {
                createWrap(source, fqn, members);
                addMembers(source, genUtils, members);
                createEquals(source, genUtils, members);
            }

            // create new class
            Tree newClassTree;
            final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(source.getModifiers());
            modifiers.remove(Modifier.ABSTRACT);
            modifiers.remove(Modifier.STATIC);
            if(noInterface) {
                newClassTree = make.Class(
                   make.Modifiers(modifiers), //classModifiersTree,
                   name,
                   newTypeParams,
                   wrap ? null : make.Type(source.asType()), //superClass,
                   implementsList,
                   members);
            } else {    
                newClassTree = make.Interface(
                   make.Modifiers(modifiers), //classModifiersTree,
                   name,
                   newTypeParams,
                   Collections.singletonList(make.Type(source.asType())), //superClass,
                   members);
            }

            // TODO: Useful javadoc for the class
//            Doc javadoc = wc.getElementUtilities().javaDocFor(source);
//            Comment comment = Comment.create(Comment.Style.JAVADOC, javadoc.getRawCommentText());
//            make.addComment(newClassTree, comment, true);

            String relativePath = fqn.replace('.', '/') + ".java"; // NOI18N

            CompilationUnitTree newCompilation = JavaPluginUtils.createCompilationUnit(sourceRoot, relativePath, newClassTree, workingCopy, make);

            workingCopy.rewrite(null, newCompilation);
            initialized = true;
        }
        return super.visitCompilationUnit(node, p);
    }

    private void addFields(TypeElement source, CodeStyle cs, List<Tree> members) throws IllegalStateException {
        for (VariableElement field : ElementFilter.fieldsIn(workingCopy.getElements().getAllMembers(source))) {
            if (!field.getModifiers().contains(Modifier.NATIVE)
                    && field.getModifiers().contains(Modifier.PUBLIC)
                    && field.getEnclosingElement().getKind() != ElementKind.INTERFACE
                    && !field.getEnclosingElement().equals(workingCopy.getElements().getTypeElement("java.lang.Object"))) { //NOI18N
                if (field.getModifiers().contains(Modifier.FINAL) && field.getModifiers().contains(Modifier.STATIC)) { // Encapsulate?
                    VariableTree variable = make.Variable(field, make.QualIdent(field));
                    members.add(0, variable);
                } else {
                    ExecutableElement[] elements = new ExecutableElement[2];
                    MethodTree[] createdGetterAndSetter = createGetterAndSetter(field, field.getModifiers(), cs, elements);
                    ElementHandle<Element> fieldHandle = ElementHandle.create((Element) field);
                    MethodTree getter = createdGetterAndSetter[0];
                    MethodTree setter = createdGetterAndSetter[1];
                    getterSetterMap.put(fieldHandle, new String[]{
                        getter == null ? elements[0].getSimpleName().toString() : getter.getName().toString(),
                        setter == null ? elements[1].getSimpleName().toString() : setter.getName().toString()});
                    if(getter != null) {
                       members.add(getter);
                    }
                    if(setter != null) {
                        members.add(setter);
                    }
                }
            }
        }
    }

    private MethodTree[] createGetterAndSetter(VariableElement field, Set<Modifier> useModifiers, CodeStyle cs, ExecutableElement[] elements) {
        boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
        String getterName = CodeStyleUtils.computeGetterName(field.getSimpleName(), field.asType().getKind() == TypeKind.BOOLEAN, staticMod, cs);
        String setterName = CodeStyleUtils.computeSetterName(field.getSimpleName(), staticMod, cs);
        String fieldName = field.getSimpleName().toString();
        String longName = (staticMod ? "" : "this.") + DELEGATE + "." + fieldName;//NOI18N
        String parName = staticMod ? "a" + getCapitalizedName(field) : Utilities.isJavaIdentifier(stripPrefix(fieldName)) ? stripPrefix(fieldName) : fieldName; //NOI18N
        String getterBody = "{return " + DELEGATE + "." + fieldName + ";}"; //NOI18N
        StringBuilder setterBody = new StringBuilder();
        setterBody.append("{");//NOI18N
        setterBody.append(longName).append(" = ").append(parName).append(";"); //NOI18N
        setterBody.append("}");//NOI18N

        Set<Modifier> mods = EnumSet.noneOf(Modifier.class);
        mods.addAll(useModifiers);

        if (staticMod) {
            mods.add(Modifier.STATIC);
        }

        VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
        MethodTree[] result = new MethodTree[2];

        ExecutableElement getterElm = null;
        getterElm = EncapsulateFieldRefactoringPlugin.findMethod(
                workingCopy,
                (TypeElement) field.getEnclosingElement(),
                getterName,
                Collections.<VariableElement>emptyList(), false);
        if (getterElm == null) {
            MethodTree getter = make.Method(
                    make.Modifiers(mods),
                    getterName,
                    fieldTree.getType(),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    getterBody,
                    null);
            result[0] = getter;
            String jdText = null;
            String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
            Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2,
                    prefix + "@return the " + field.getSimpleName()); // NOI18N
            make.addComment(getter, comment, true);
        } else {
            elements[0] = getterElm;
        }

        ExecutableElement setterElm = null;
        setterElm = EncapsulateFieldRefactoringPlugin.findMethod(
                workingCopy,
                (TypeElement) field.getEnclosingElement(),
                setterName,
                Collections.<VariableElement>singletonList(field), false);
        if (setterElm == null) {
            VariableTree paramTree = make.Variable(
                    make.Modifiers(Collections.<Modifier>emptySet()), parName, fieldTree.getType(), null);
            MethodTree setter = make.Method(
                    make.Modifiers(mods),
                    setterName,
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.singletonList(paramTree),
                    Collections.<ExpressionTree>emptyList(),
                    setterBody.toString(),
                    null);
            result[1] = setter;

            String jdText = null;
            String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
            Comment comment = Comment.create(
                    Comment.Style.JAVADOC, -2, -2, -2,
                    prefix + String.format("@param %s the %s to set", parName, fieldName)); // NOI18N
            make.addComment(setter, comment, true);
        } else {
            elements[1] = setterElm;
        }
        return result;
    }
    
    private void addMembers(TypeElement source, GeneratorUtilities genUtils, List<Tree> members) throws IllegalStateException {
        for (ExecutableElement method : ElementFilter.methodsIn(workingCopy.getElements().getAllMembers(source))) {
            if (!method.getModifiers().contains(Modifier.NATIVE)
                    && method.getModifiers().contains(Modifier.PUBLIC)
                    && !method.getEnclosingElement().equals(workingCopy.getElements().getTypeElement("java.lang.Object"))) { //NOI18N
                if (!((method.getReturnType().getKind() == TypeKind.BOOLEAN && method.getSimpleName().contentEquals("equals")) || //NOI18N
                        (method.getReturnType().getKind() == TypeKind.INT && method.getSimpleName().contentEquals("hashCode")))) { //NOI18N
                    addMember(source, method, genUtils, members);
                }
            }
        }
    }
    
    private String decapitalize(CharSequence name) {
        return Character.toLowerCase(name.charAt(0)) + name.subSequence(1, name.length()).toString() + 's';
    }
    
    private void createWrap(TypeElement source, String fqn, List<Tree> members) throws IllegalStateException {
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        final String name = decapitalize(source.getSimpleName());
        
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.QualIdent(source), null)); //NOI18N
        String bodyText = preparePattern(workingCopy, source.asType(), WRAP_PATTERNS, name, source.getSimpleName().toString(), fqn); //NOI18N
        SourcePositions[] positions = new SourcePositions[1];
        StatementTree body = workingCopy.getTreeUtilities().parseStatement(bodyText, positions);
        ModifiersTree modifiers = prepareModifiers(workingCopy, mods, make, false);

        MethodTree wrap = make.Method(modifiers, "wrap", make.ArrayType(make.QualIdent(fqn)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), (BlockTree) body, null, true); //NOI18N
        members.add(wrap);
    }
    
    /*
    LocalFieldExtension[] result = new LocalFieldExtension[fields.length];
    for (int i = 0; i < fields.length; i++) {
        result[i] = new LocalFieldExtension(fields[i]);
    }
    return result;
    */
    
    /*
    public static LocalFieldExtension[] wrap(Field[] fields) {
        return Arrays.stream(fields).map((Field t) -> new LocalFieldExtension(t)).toArray(LocalFieldExtension[]::new);
    }
    */

    private void createEquals(TypeElement source, GeneratorUtilities genUtils, List<Tree> members) throws IllegalStateException {
        List<Tree> newTypeParams = new LinkedList<>();
        for (TypeParameterElement typeParam : source.getTypeParameters()) {
            newTypeParams.add(make.Type(typeParam.asType()));
        }

        TypeMirror sourceType = source.asType();
        Tree newTypeTree = make.Type(refactoring.getNewName());
        Tree newSimpleTypeTree = make.Type(refactoring.getNewName());
        if (sourceType.getKind() == TypeKind.DECLARED) {
            if (!newTypeParams.isEmpty()) {
                newTypeTree = (ExpressionTree) make.ParameterizedType(newTypeTree, newTypeParams);
            }
        }

        Equality equality = refactoring.getEquality();
        switch (equality) {
            case SEPARATE: {
                /*
                 * boolean equals(Object o) {
                 *     return this.delegate.equals(o);
                 * }
                 *
                 * boolean equalsSOURCE(THIS o) {
                 *     return this.delegate.equals(o.delegate);
                 * }
                 * 
                 * int hashCode() {
                 *     return this.delegate.hashCode();
                 * }
                 */
                BlockTree body = make.Block(Collections.singletonList(make.Return(make.MethodInvocation(Collections.EMPTY_LIST, make.MemberSelect(make.Identifier("this.delegate"), "equals"), Collections.singletonList(make.MemberSelect(make.Identifier("o"), DELEGATE))))), false); //NOI18N
                MethodTree method = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                        "equals" + refactoring.getNewName(), //NOI18N
                        make.PrimitiveType(TypeKind.BOOLEAN),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "o", newTypeTree, null)), //NOI18N
                        Collections.EMPTY_LIST,
                        body,
                        null,
                        false);
                members.add(method);
            }
            case DELEGATE: {
                /*
                 * boolean equals(Object o) {
                 *     Object target = o;
                 *     if(o instanceof THIS) {
                 *         target = ((THIS)o).delegate;
                 *     }
                 *     return this.delegate.equals(target);
                 * }
                 * 
                 * int hashCode() {
                 *     return this.delegate.hashCode();
                 * }
                 */
                List<StatementTree> statements = new LinkedList<>();
                statements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "target", make.Type("Object"), make.Identifier("o")));
                statements.add(
                        make.If(make.InstanceOf(make.Identifier("o"), newSimpleTypeTree), make.Block(Collections.singletonList(make.ExpressionStatement(
                        make.Assignment(make.Identifier("target"),
                        make.MemberSelect(make.Parenthesized(make.TypeCast(newSimpleTypeTree, make.Identifier("o"))), DELEGATE)))), false), null));
                statements.add(make.Return(make.MethodInvocation(Collections.EMPTY_LIST, make.Identifier("this.delegate.equals"), Collections.singletonList(make.Identifier("target")))));
                BlockTree body = make.Block(statements, false);
                MethodTree method = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                        "equals",
                        make.PrimitiveType(TypeKind.BOOLEAN),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "o", make.Type("Object"), null)),
                        Collections.EMPTY_LIST,
                        body,
                        null,
                        false);
                members.add(method);

                body = make.Block(Collections.singletonList(make.Return(make.MethodInvocation(Collections.EMPTY_LIST, make.MemberSelect(make.Identifier("this.delegate"), "hashCode"), Collections.EMPTY_LIST))), false); //NOI18N
                method = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                        "hashCode", //NOI18N
                        make.PrimitiveType(TypeKind.INT),
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        body,
                        null,
                        false);
                members.add(method);
                break;
            }
            case GENERATE: {
                MethodTree equalsMethod = createEqualsMethod(workingCopy, (DeclaredType) sourceType, newTypeTree);
                MethodTree hashMethod = createHashCodeMethod(workingCopy, (DeclaredType) sourceType);
                members.add(equalsMethod);
                members.add(hashMethod);
                break;
            }
        }
    }

    private void addMember(TypeElement source, ExecutableElement member, GeneratorUtilities genUtils, List<Tree> members) throws IllegalStateException {
        List<ExpressionTree> paramList = new ArrayList<>();
        ExecutableType method = (ExecutableType) workingCopy.getTypes().asMemberOf((DeclaredType) source.asType(), member);
        final List<? extends VariableElement> parameters = member.getParameters();
        final List<? extends TypeMirror> parameterTypes = method.getParameterTypes();
        for (int i = 0;i < parameterTypes.size(); i++) {
            TypeMirror variableElement = parameterTypes.get(i);
            final ExpressionTree identifier;
            if (workingCopy.getTypes().isSameType(variableElement, source.asType())) {
                identifier = make.MemberSelect(make.Identifier(parameters.get(i).getSimpleName()), "delegate"); //NOI18N
            } else {
                identifier = make.Identifier(parameters.get(i).getSimpleName());
            }
            paramList.add(identifier);
        }
        List<ExpressionTree> typeArguments = new ArrayList<>();
        final List<? extends TypeParameterElement> typeParameters = member.getTypeParameters();
        for (TypeParameterElement typeParameterElement : typeParameters) {
            if(typeParameterElement.getEnclosingElement().equals(member)) {
                IdentifierTree identifier = make.Identifier(typeParameterElement);
                typeArguments.add(identifier);
            }
        }
        MemberSelectTree memberSelect;
        final Set<Modifier> mods = EnumSet.copyOf(member.getModifiers());
        mods.remove(Modifier.ABSTRACT);
        mods.remove(Modifier.DEFAULT);
        if (mods.contains(Modifier.STATIC)) {
            memberSelect = make.MemberSelect(make.QualIdent(source), member);
        } else {
            memberSelect = make.MemberSelect(make.Identifier("delegate"), member); //NOI18N
        }
        ExpressionTree methodInvocation = make.MethodInvocation(typeArguments,
                memberSelect,
                paramList);
        TypeMirror methodReturnTypeMirror = method.getReturnType();
        Tree methodReturnType = make.Type(method.getReturnType());
        boolean hasReturn = true;
        Types types = workingCopy.getTypes();
        if (types.isSameType(methodReturnTypeMirror, types.getNoType(TypeKind.VOID))) {
            hasReturn = false;
        }
        StatementTree statement;
        if (hasReturn) {
            if (workingCopy.getTypes().isSameType(methodReturnTypeMirror, source.asType())) {
                ExpressionTree ident;

                DeclaredType declaredType = (DeclaredType) methodReturnTypeMirror;
                List<? extends TypeMirror> returntypeArguments = declaredType.getTypeArguments();

                List<ExpressionTree> returntypes = new LinkedList<>();
                for (TypeMirror typeMirror : returntypeArguments) {
                    returntypes.add((ExpressionTree) make.Type(typeMirror));
                }
                ident = make.QualIdent(fqn);
                if (!returntypes.isEmpty()) {
                    ident = (ExpressionTree) make.ParameterizedType(ident, returntypes);
                }
                methodReturnType = ident;

                methodInvocation = make.NewClass(null, Collections.EMPTY_LIST, ident, Collections.singletonList(methodInvocation), null);
            }
            statement = make.Return(methodInvocation);
        } else {
            statement = make.ExpressionStatement(methodInvocation);
        }
        
        List<AnnotationTree> annotations = member.getEnclosingElement().getKind() == ElementKind.INTERFACE?
                    Collections.singletonList(make.Annotation(make.Type("Override"),
                                          Collections.EMPTY_LIST)):
                    Collections.<AnnotationTree>emptyList();
        ModifiersTree modifiers = make.Modifiers(mods, annotations);

        List<TypeParameterTree> newTypeParams = new ArrayList<>(member.getTypeParameters().size());
        transformTypeParameters(member, member.getTypeParameters(), make, genUtils, newTypeParams);

        List<VariableTree> newParameters = new ArrayList<>(parameterTypes.size());
        for (int i = 0; i < parameterTypes.size(); i++) {
            TypeMirror variableType = parameterTypes.get(i);
            VariableElement variableElement = parameters.get(i);
            if (workingCopy.getTypes().isSameType(variableType, source.asType())) {
                Tree ident = make.QualIdent(fqn);
                if (variableType.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) variableType;
                    List<? extends TypeMirror> arguments = declaredType.getTypeArguments();
                    List<Tree> newArguments = new ArrayList<>(arguments.size()); 
                    for (TypeMirror typeMirror : arguments) {
                        newArguments.add(make.Type(typeMirror));
                    }
                    if (!newArguments.isEmpty()) {
                        ident = (ExpressionTree) make.ParameterizedType(ident, newArguments);
                    }
                }
                newParameters.add(make.Variable(make.Modifiers(variableElement.getModifiers()), variableElement.getSimpleName(), ident, null));
            } else {
                newParameters.add(make.Variable(make.Modifiers(variableElement.getModifiers()), variableElement.getSimpleName(), make.Type(variableType), null));
            }
        }

        final List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
        List<ExpressionTree> newThrownTypes = new ArrayList<>(thrownTypes.size());
        for (TypeMirror typeMirror : thrownTypes) {
            newThrownTypes.add((ExpressionTree) make.Type(typeMirror));
        }

        MethodTree newMethod = make.Method(modifiers, member.getSimpleName(), methodReturnType, newTypeParams, newParameters, newThrownTypes, make.Block(Collections.singletonList(statement), false), null, member.isVarArgs());
        newMethod = genUtils.importFQNs(newMethod);
        DocCommentTree javadoc = workingCopy.getDocTrees().getDocCommentTree(member);
        if (javadoc != null && !javadoc.getFullBody().isEmpty()) {
            Comment comment = Comment.create(Comment.Style.JAVADOC, javadoc.toString());
            make.addComment(newMethod, comment, true);
        }
        members.add(newMethod);
    }

    private List<Tree> addInterfaces(TypeElement source) {
        List<Tree> implementsList = new ArrayList<>();
        Set<String> implemented = new HashSet<>();
        TypeElement typeElement = source;
        while (typeElement != null) {
            for (TypeMirror typeMirror : typeElement.getInterfaces()) {
                Tree type = make.Type(typeMirror);
                if (typeMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) typeMirror;
                    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                    List<Tree> newTypeParams = new LinkedList<>();
                    for (TypeMirror typeParam : typeArguments) {
                        Element element = workingCopy.getTypes().asElement(typeParam);
                        if (source.equals(element)) {
                            newTypeParams.add(make.QualIdent(fqn));
                        } else {
                            newTypeParams.add(make.Type(typeParam));
                        }
                    }
                    if (!newTypeParams.isEmpty()) {
                        type = make.ParameterizedType(make.QualIdent(declaredType.asElement()), newTypeParams);
                    }
                }
                if (implemented.add(typeMirror.toString())) {
                    implementsList.add(type);
                }
            }
            TypeMirror superclass = typeElement.getSuperclass();
            if (superclass.getKind() != TypeKind.NONE) {
                typeElement = (TypeElement) workingCopy.getTypes().asElement(superclass);
            } else {
                typeElement = null;
            }
        }
        return implementsList;
    }

    private void transformTypeParameters(Element source, List<? extends TypeParameterElement> typeParams, TreeMaker make, GeneratorUtilities genUtils, List<TypeParameterTree> newTypeParams) {
        for (TypeParameterElement typeParam : typeParams) {
            if(typeParam.getEnclosingElement().equals(source)) {
                List<? extends TypeMirror> bounds = typeParam.getBounds();
                List<ExpressionTree> newBounds = new ArrayList<>(bounds.size());
                for (TypeMirror typeMirror : bounds) {
                    TypeMirror typeObject = workingCopy.getElements().getTypeElement("java.lang.Object").asType();
                    if (!workingCopy.getTypes().isSameType(typeMirror, typeObject)) {
                        ExpressionTree type = (ExpressionTree) make.Type(typeMirror);
                        newBounds.add(type);
                    }
                }
                TypeParameterTree typeParameterTree = make.TypeParameter(typeParam.getSimpleName(), newBounds);
                if (!typeParameterTree.getBounds().isEmpty()) {
                    typeParameterTree = (TypeParameterTree) genUtils.importFQNs(typeParameterTree);
                }
                newTypeParams.add(typeParameterTree);
            }
        }
    }

    private void addConstructors(final TypeElement origClass, List<Tree> members, Modifier needed) {
        final GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);

        Tree type = make.Type(origClass.asType());
        VariableTree parameter = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "delegate", type, null);

        EnumSet<Modifier> modifiers = origClass.getModifiers().isEmpty()? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(origClass.getModifiers());
        modifiers.remove(Modifier.STATIC);
        modifiers.remove(Modifier.FINAL);
        modifiers.remove(Modifier.ABSTRACT);
        
        if (refactoring.getWrap() && origClass.getKind() != ElementKind.INTERFACE) {
            // create constructor
            AssignmentTree assignment = make.Assignment(make.MemberSelect(make.Identifier("this"), "delegate"), make.Identifier("delegate")); //NOI18N
            ExpressionStatementTree statement = make.ExpressionStatement(assignment);
            BlockTree block = make.Block(Collections.singletonList(statement), false);
            MethodTree newConstr = make.Method(make.Modifiers(modifiers),
                    refactoring.getNewName(),
                    null,
                    Collections.EMPTY_LIST,
                    Collections.singletonList(parameter),
                    Collections.EMPTY_LIST,
                    block,
                    null);

            newConstr = genUtils.importFQNs(newConstr);
            members.add(newConstr);
        }
        final boolean isAbstract = origClass.getModifiers().contains(Modifier.ABSTRACT);
        if (!isAbstract) {
        for (ExecutableElement constr : ElementFilter.constructorsIn(origClass.getEnclosedElements())) {
            Set<Modifier> cMods = constr.getModifiers();
            if(!cMods.contains(Modifier.PUBLIC)) {
                if(cMods.contains(Modifier.PRIVATE)) continue;
                if(needed != null) {
                    if(!cMods.contains(needed)) {
                        continue;
                    }
                }
            }
            final List<? extends TypeParameterElement> typeParameters = constr.getTypeParameters();
            List<TypeParameterTree> newTypeParams = new ArrayList<>(typeParameters.size());
            transformTypeParameters(constr, typeParameters, make, genUtils, newTypeParams);

            List<ExpressionTree> newTypeArguments = new ArrayList<>();
            for (TypeParameterElement typeParameterElement : typeParameters) {
                IdentifierTree identifier = make.Identifier(typeParameterElement);
                newTypeArguments.add(identifier);
            }

            List<ExpressionTree> newArguments = new ArrayList<>();
            for (VariableElement variableElement : constr.getParameters()) {
                IdentifierTree identifier = make.Identifier(variableElement);
                newArguments.add(identifier);
            }

            List<? extends VariableElement> parameters = constr.getParameters();
            List<VariableTree> newParams = new ArrayList<>(typeParameters.size());
            for (VariableElement variableElement : parameters) {
                VariableTree var = make.Variable(variableElement, null);
                newParams.add(var);
            }
            List<? extends TypeMirror> thrownTypes = constr.getThrownTypes();
            List<ExpressionTree> newThrownTypes = new ArrayList<>(thrownTypes.size());
            for (TypeMirror typeMirror : thrownTypes) {
                Tree thrownType = make.Type(typeMirror);
                newThrownTypes.add((ExpressionTree) thrownType);
            }

            ExpressionTree expression;
            if (refactoring.getWrap()) {
                ExpressionTree newClassTree = make.NewClass(null, newTypeArguments, (ExpressionTree)type, newArguments, null);
                expression = make.Assignment(make.MemberSelect(make.Identifier("this"), "delegate"), newClassTree); //NOI18N
            } else {
                expression = make.MethodInvocation(newTypeArguments, make.Identifier("super"), newArguments); //NOI18N
            }

            ExpressionStatementTree statement = make.ExpressionStatement(expression);
            BlockTree block = make.Block(Collections.singletonList(statement), false);

            // create constructor
            MethodTree newConstr = make.Method(make.Modifiers(modifiers),
                    refactoring.getNewName(),
                    null,
                    newTypeParams,
                    newParams,
                    newThrownTypes, block, null);

            newConstr = genUtils.importFQNs(newConstr);
            DocCommentTree javadoc = workingCopy.getDocTrees().getDocCommentTree(constr);
            if (javadoc != null && !javadoc.getFullBody().isEmpty()) {
                Comment comment = Comment.create(Comment.Style.JAVADOC, javadoc.toString());
                make.addComment(newConstr, comment, true);
            }
            members.add(newConstr);
        }
        }
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        Element element = workingCopy.getTrees().getElement(getCurrentPath());
        if (p.equals(element)) {
            return node;
        }
        return super.visitClass(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitIdentifier(node, p);
        }
        TreePath typePath = new TreePath(getCurrentPath(), node);
        rewriteType(typePath, p, node);
        return super.visitIdentifier(node, p);
    }

//    @Override
//    public Tree visitVariable(VariableTree node, Element p) {
//        if (!refactoring.getReplace()) {
//            return super.visitVariable(node, p);
//        }
//        TreePath currentPath = getCurrentPath();
//        Tree typeTree = node.getType();
//        TreePath typePath = new TreePath(currentPath, typeTree);
//        rewriteType(typePath, p, typeTree);
//        return super.visitVariable(node, p);
//    }
//    @Override
//    public Tree visitNewClass(NewClassTree node, Element p) {
//        if (!refactoring.getReplace()) {
//            return super.visitNewClass(node, p);
//        }
//        TreePath currentPath = getCurrentPath();
//        ExpressionTree typeTree = node.getIdentifier();
//        TreePath typePath = new TreePath(currentPath, typeTree);
//        rewriteType(typePath, p, typeTree);
//        return super.visitNewClass(node, p);
//    }
//    @Override
//    public Tree visitTypeCast(TypeCastTree node, Element p) {
//        if (!refactoring.getReplace()) {
//            return super.visitTypeCast(node, p);
//        }
//        TreePath currentPath = getCurrentPath();
//        Tree typeTree = node.getType();
//        TreePath typePath = new TreePath(currentPath, typeTree);
//        rewriteType(typePath, p, typeTree);
//        return super.visitTypeCast(node, p);
//    }
//    @Override
//    public Tree visitMethod(MethodTree node, Element p) {
//        if (!refactoring.getReplace()) {
//            return super.visitMethod(node, p);
//        }
//        TreePath currentPath = getCurrentPath();
//        ExecutableElement method = (ExecutableElement) workingCopy.getTrees().getElement(currentPath);
//        TypeMirror returnType = method.getReturnType();
//        Element element = workingCopy.getTypes().asElement(returnType);
//        if (p.equals(element)) {
//            Tree typeTree = node.getReturnType();
//            ExpressionTree ident = make.QualIdent(fqn);
//            if (typeTree.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
//                List<? extends Tree> typeArguments = ((ParameterizedTypeTree) typeTree).getTypeArguments();
//                if (!typeArguments.isEmpty()) {
//                    ident = (ExpressionTree) make.ParameterizedType(ident, typeArguments);
//                }
//            }
//            rewrite(typeTree, ident);
//        }
//        return super.visitMethod(node, p);
//    }
    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitMethodInvocation(node, p);
        }
        TreePath currentPath = getCurrentPath();
        TypeMirror returnType = workingCopy.getTrees().getTypeMirror(currentPath);
        boolean fromArray = false;
        if (returnType != null && returnType.getKind() == TypeKind.ARRAY) {
            ArrayType array = (ArrayType) returnType;
            returnType = array.getComponentType();
            fromArray = true;
        }
        Element typeElement = workingCopy.getTypes().asElement(returnType);

        // TODO: Check if the method is in the scope, then skip it.
        if (p.equals(typeElement)) {
            if(willBeExtended(currentPath, node, returnType)) {
                return super.visitMethodInvocation(node, p);
            }
            ExpressionTree ident;

            DeclaredType declaredType = (DeclaredType) returnType;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

            List<ExpressionTree> types = new LinkedList<>();
            for (TypeMirror typeMirror : typeArguments) {
                types.add((ExpressionTree) make.Type(typeMirror));
            }
            ident = make.QualIdent(fqn);
            if (!types.isEmpty()) {
                ident = (ExpressionTree) make.ParameterizedType(ident, types);
            }

            Tree newClass;
            if(fromArray) {
                newClass = make.MethodInvocation(Collections.EMPTY_LIST, make.MemberSelect(ident, "wrap"), Collections.singletonList(node));
            } else {
                newClass = make.NewClass(null, Collections.EMPTY_LIST, ident, Collections.singletonList(node), null);
            }
            rewrite(node, newClass);
        }

        return super.visitMethodInvocation(node, p);
    }

    private boolean willBeExtended(TreePath currentPath, MethodInvocationTree node, TypeMirror returnType) {
        boolean skip = true;
        ExpressionTree methodSelect = node.getMethodSelect();
        if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree selectTree = (MemberSelectTree) methodSelect;
            Element el = workingCopy.getTrees().getElement(new TreePath(currentPath, selectTree));
            ElementHandle handle = el == null ? null : ElementHandle.create(el);
            if(handle != null && JavaRefactoringUtils.isFromLibrary(handle, workingCopy.getClasspathInfo())) {
                skip = false;
            }
        }
        return skip;
    }

    private void rewriteType(TreePath typePath, Element p, Tree typeTree) {
        Element element = workingCopy.getTrees().getElement(typePath);
        if (p.equals(element)) {
            ExpressionTree ident = make.QualIdent(fqn);
            if (typeTree.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                List<? extends Tree> typeArguments = ((ParameterizedTypeTree) typeTree).getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    ident = (ExpressionTree) make.ParameterizedType(ident, typeArguments);
                }
            }
            rewrite(typeTree, ident);
        }
    }

    @Override
    public Tree visitAssignment(AssignmentTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitAssignment(node, p);
        }
        ExpressionTree variable = node.getVariable();
        boolean isArray = false;
        while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
            isArray = true;
            // int[] a; a[a[0]][a[1]] = 0; // scan also array indices
            scan(((ArrayAccessTree) variable).getIndex(), p);
            variable = ((ArrayAccessTree) variable).getExpression();
        }

        Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
        if(el != null) {
        Element enclosingElement = el.getEnclosingElement();
        if (enclosingElement != null && enclosingElement.equals(p)
                && (isArray || checkAssignmentInsideExpression())) {
            ElementHandle<Element> handle = ElementHandle.create(el);
            String[] getterSetter = getterSetterMap.get(handle); // TODO: Check for null and solve !
            if (getterSetter != null) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, getterSetter[0]);
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, getterSetter[1]);

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    TypeMirror expType = trees.getTypeMirror(expPath);
                    if (workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = expTree;
                    } else {
                        newExpTree = make.TypeCast(make.Type(varType), expTree);
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));

                    rewrite(node, invksetter);
                }
            }
            }
        }
        return scan(node.getExpression(), p);
    }

    @Override
    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitCompoundAssignment(node, p);
        }
        ExpressionTree variable = node.getVariable();
        boolean isArray = false;
        while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
            isArray = true;
            variable = ((ArrayAccessTree) variable).getExpression();
        }

        Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
        if(el != null) {
        Element enclosingElement = el.getEnclosingElement();
        if (enclosingElement != null && enclosingElement.equals(p)
                // check (field += 3) == 3
                && (isArray || checkAssignmentInsideExpression())) {
            ElementHandle<Element> handle = ElementHandle.create(el);
            String[] getterSetter = getterSetterMap.get(handle); // TODO: Check for null and solve !
            if (getterSetter != null) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, getterSetter[0]);
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, getterSetter[1]);

                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
                    String s = node.getKind().name();
                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
                    Tree.Kind operator = Tree.Kind.valueOf(s);

                    ExpressionTree invkgetter = createGetterInvokation(variable, getterSetter[0]);

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    // getter need not exist yet, use variable to resolve type of binary expression
                    ExpressionTree expTreeFake = make.Binary(operator, variable, expTree);
                    TypeMirror expType = workingCopy.getTreeUtilities().attributeTree(expTreeFake, trees.getScope(expPath));

                    newExpTree = make.Binary(operator, invkgetter, expTree);
                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
        }
        }
        return scan(node.getExpression(), p);
    }

    @Override
    public Tree visitUnary(UnaryTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitUnary(node, p);
        }
        ExpressionTree t = node.getExpression();
        Tree.Kind kind = node.getKind();
        boolean isArrayOrImmutable = kind != Tree.Kind.POSTFIX_DECREMENT
                && kind != Tree.Kind.POSTFIX_INCREMENT
                && kind != Tree.Kind.PREFIX_DECREMENT
                && kind != Tree.Kind.PREFIX_INCREMENT;
        while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
            isArrayOrImmutable = true;
            t = ((ArrayAccessTree) t).getExpression();
        }
        Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
        if (el != null) {
            Element enclosingElement = el.getEnclosingElement();
            if (enclosingElement != null && enclosingElement.equals(p)
                    && (isArrayOrImmutable || checkAssignmentInsideExpression())) {
                ElementHandle<Element> handle = ElementHandle.create(el);
                String[] getterSetter = getterSetterMap.get(handle); // TODO: Check for null and solve !
                // check (++field + 3)
                if (getterSetter != null) {
                    ExpressionTree invkgetter = createGetterInvokation(t, getterSetter[0]);
                    if (isArrayOrImmutable) {
                        rewrite(t, invkgetter);
                    } else {
                        ExpressionTree setter = createMemberSelection(node.getExpression(), getterSetter[1]);

                        Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
                                ? Tree.Kind.PLUS
                                : Tree.Kind.MINUS;

                        // resolve types
                        Trees trees = workingCopy.getTrees();
                        ExpressionTree expTree = node.getExpression();
                        TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                        TypeMirror varType = trees.getTypeMirror(varPath);
                        TypeMirror expType = workingCopy.getTypes().getPrimitiveType(TypeKind.INT);
                        ExpressionTree newExpTree = make.Binary(operator, invkgetter, make.Literal(1));
                        if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                            newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                        }

                        MethodInvocationTree invksetter = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                setter,
                                Collections.singletonList(newExpTree));
                        rewrite(node, invksetter);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if (!refactoring.getReplace()) {
            return super.visitMemberSelect(node, p);
        }
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (el != null) {
            Element enclosingElement = el.getEnclosingElement();
            if (enclosingElement != null && enclosingElement.equals(p)) {
                ElementHandle<Element> handle = ElementHandle.create(el);
                String[] getterSetter = getterSetterMap.get(handle); // TODO: Check for null and solve !
                if (getterSetter != null) {
                    ExpressionTree nodeNew = createGetterInvokation(node, getterSetter[0]);
                    rewrite(node, nodeNew);
                }
            }
        }
        return super.visitMemberSelect(node, p);
    }

    private boolean checkAssignmentInsideExpression() {
        Tree exp1 = getCurrentPath().getLeaf();
        Tree parent = getCurrentPath().getParentPath().getLeaf();
        if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
            problem = JavaPluginUtils.chainProblems(problem, new Problem(false,
                    NbBundle.getMessage(
                    EncapsulateFieldRefactoringPlugin.class,
                    "ERR_EncapsulateInsideAssignment", // NOI18N
                    exp1.toString(),
                    parent.toString(),
                    FileUtil.getFileDisplayName(workingCopy.getFileObject()))));
            return false;
        }
        return true;
    }

    /**
     * replace current expresion with the proper one.<p> c.field -> c.getField()
     * field -> getField() or copy in case of refactoring.getGetterName() ==
     * null
     */
    private ExpressionTree createGetterInvokation(ExpressionTree current, String getterName) {
        ExpressionTree getter = createMemberSelection(current, getterName);

        MethodInvocationTree invkgetter = make.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                getter,
                Collections.<ExpressionTree>emptyList());
        return invkgetter;
    }

    private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
        ExpressionTree selector;
        if (node.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree select = (MemberSelectTree) node;
            selector = make.MemberSelect(select.getExpression(), name);
        } else {
            selector = make.Identifier(name);
        }
        return selector;
    }
    public static final String CLASS_FIELD_PREFIX = "_"; // NOI18N

    /**
     * Removes the class field prefix from the identifer of a field. For
     * example, if the class field prefix is "_", the identifier "_name" is
     * stripped to become "name".
     *
     * @param identifierString The identifer to strip.
     * @return The stripped identifier.
     */
    private static String stripPrefix(String identifierString) {
        String stripped;
        if (identifierString.startsWith(CLASS_FIELD_PREFIX) && identifierString.length() > 1) {
            stripped = identifierString.substring(CLASS_FIELD_PREFIX.length());
        } else {
            stripped = identifierString;
        }
        return stripped;
    }

    private static StringBuilder getCapitalizedName(VariableElement field) {
        StringBuilder name = new StringBuilder(stripPrefix(field.getSimpleName().toString()));

        //Beans naming convention, #165241
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            return name;
        }

        name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return name;
    }

    private String trimNewLines(String javadoc) {
        if (javadoc == null) {
            return null;
        }

        int len = javadoc.length();
        int st = 0;
        int off = 0;      /*
         * avoid getfield opcode
         */
        char[] val = javadoc.toCharArray();    /*
         * avoid getfield opcode
         */

        while ((st < len) && Character.isWhitespace(val[off + st])/*
                 * && (val[off + st] <= '\n')
                 */) {
            st++;
        }
        while ((st < len) && Character.isWhitespace(val[off + len - 1])/*
                 * val[off + len - 1] <= '\n')
                 */) {
            len--;
        }
        return ((st > 0) || (len < val.length)) ? javadoc.substring(st, len) : javadoc;
    }

    Problem getProblem() {
        return problem;
    }

    //<editor-fold defaultstate="collapsed" desc="Copied from Java Editor CodeGen">
    private enum KindOfType {

        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        CHAR,
        FLOAT,
        DOUBLE,
        ENUM,
        ARRAY_PRIMITIVE,
        ARRAY,
        STRING,
        OTHER;
    }
    private static final Map<Acceptor, String> WRAP_PATTERNS;
    private static final Map<Acceptor, String> EQUALS_PATTERNS;
    private static final Map<Acceptor, String> HASH_CODE_PATTERNS;

    static {
        WRAP_PATTERNS = new LinkedHashMap<>();
        
        WRAP_PATTERNS.put(new SourceVersionAcceptor(SourceVersion.RELEASE_8), "{ return Arrays.stream({VAR0}).map(({VAR1} t) -> new {VAR2}(t)).toArray({VAR2}[]::new); }");
        WRAP_PATTERNS.put(new SourceVersionAcceptor(SourceVersion.RELEASE_2),
                  "{ {VAR2}[] result = new {VAR2}[{VAR0}.length];\n"
                + "for (int i = 0; i < {VAR0}.length; i++) {\n"
                + "    result[i] = new {VAR2}({VAR0}[i]);\n"
                + "}\n"
                + "return result; }");
        
        EQUALS_PATTERNS = new LinkedHashMap<>();

        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.BOOLEAN, KindOfType.BYTE, KindOfType.SHORT, KindOfType.INT, KindOfType.LONG, KindOfType.CHAR), "this.{VAR0} != other.{VAR0}");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.FLOAT), "java.lang.Float.floatToIntBits(this.{VAR0}) != java.lang.Float.floatToIntBits(other.{VAR0})");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.DOUBLE), "java.lang.Double.doubleToLongBits(this.{VAR0}) != java.lang.Double.doubleToLongBits(other.{VAR0})");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.ENUM), "this.{VAR0} != other.{VAR0}");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.ARRAY_PRIMITIVE), "! java.util.Arrays.equals(this.{VAR0}, other.{VAR0}");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.ARRAY), "! java.util.Arrays.deepEquals(this.{VAR0}, other.{VAR0}");
        EQUALS_PATTERNS.put(new MethodExistsAcceptor("java.util.Objects", "equals", SourceVersion.RELEASE_7), "! java.util.Objects.equals(this.{VAR0}, other.{VAR0})");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.STRING), "(this.{VAR0} == null) ? (other.{VAR0} != null) : !this.{VAR0}.equals(other.{VAR0})");
        EQUALS_PATTERNS.put(new SimpleAcceptor(KindOfType.OTHER), "this.{VAR0} != other.{VAR0} && (this.{VAR0} == null || !this.{VAR0}.equals(other.{VAR0}))");

        HASH_CODE_PATTERNS = new LinkedHashMap<>();

        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.BYTE, KindOfType.SHORT, KindOfType.INT, KindOfType.CHAR), "this.{VAR0}");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.LONG), "(int) (this.{VAR0} ^ (this.{VAR0} >>> 32))");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.FLOAT), "java.lang.Float.floatToIntBits(this.{VAR0})");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.DOUBLE), "(int) (Double.doubleToLongBits(this.{VAR0}) ^ (Double.doubleToLongBits(this.{VAR0}) >>> 32))");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.BOOLEAN), "(this.{VAR0} ? 1 : 0)");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.ENUM), "(this.{VAR0} != null ? this.{VAR0}.hashCode() : 0)");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.ARRAY_PRIMITIVE), "java.util.Arrays.hashCode(this.{VAR0}");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.ARRAY), "java.util.Arrays.deepHashCode(this.{VAR0}");
        HASH_CODE_PATTERNS.put(new MethodExistsAcceptor("java.util.Objects", "hashCode", SourceVersion.RELEASE_7), "java.util.Objects.hashCode(this.{VAR0})");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.STRING), "(this.{VAR0} != null ? this.{VAR0}.hashCode() : 0)");
        HASH_CODE_PATTERNS.put(new SimpleAcceptor(KindOfType.OTHER), "(this.{VAR0} != null ? this.{VAR0}.hashCode() : 0)");
    }

    private MethodTree createEqualsMethod(WorkingCopy wc, DeclaredType type, Tree typeTree) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        TypeElement objElement = wc.getElements().getTypeElement("java.lang.Object"); //NOI18N
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "obj", objElement != null ? make.Type(objElement.asType()) : make.Identifier("Object"), null)); //NOI18N

        List<StatementTree> statements = new ArrayList<>();
        //if (obj == null) return false;
        statements.add(make.If(make.Binary(Tree.Kind.EQUAL_TO, make.Identifier("obj"), make.Identifier("null")), make.Return(make.Identifier("false")), null)); //NOI18N
        //if (getClass() != obj.getClass()) return false;
        statements.add(make.If(make.Binary(Tree.Kind.NOT_EQUAL_TO, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("getClass"), Collections.<ExpressionTree>emptyList()), //NOI18N
                make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("obj"), "getClass"), Collections.<ExpressionTree>emptyList())), make.Return(make.Identifier("false")), null)); //NOI18N
        //<this type> other = (<this type>) o;
//        if(typeTree)
        statements.add(make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "other", typeTree, make.TypeCast(typeTree, make.Identifier("obj")))); //NOI18N

        ExpressionTree condition = prepareExpression(wc, EQUALS_PATTERNS, type, "delegate"); //NOI18N
        statements.add(make.If(condition, make.Return(make.Identifier("false")), null)); //NOI18N

        statements.add(make.Return(make.Identifier("true")));
        BlockTree body = make.Block(statements, false);
        ModifiersTree modifiers = prepareModifiers(wc, mods, make, true);

        return make.Method(modifiers, "equals", make.PrimitiveType(TypeKind.BOOLEAN), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    private MethodTree createHashCodeMethod(WorkingCopy wc, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);

        Integer number = refactoring.getContext().lookup(Integer.class); // Used in test code
        int startNumber;
        int multiplyNumber;
        if (number != null) {
            startNumber = number.intValue();
            multiplyNumber = number.intValue();
        } else {
            startNumber = generatePrimeNumber(2, 10);
            multiplyNumber = generatePrimeNumber(10, 100);
        }
        List<StatementTree> statements = new ArrayList<>();
        //int hash = <startNumber>;
        statements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "hash", make.PrimitiveType(TypeKind.INT), make.Literal(startNumber))); //NOI18N

        TypeMirror tm = type;
        ExpressionTree variableRead = prepareExpression(wc, HASH_CODE_PATTERNS, tm, "delegate"); //NOI18N
        statements.add(make.ExpressionStatement(make.Assignment(make.Identifier("hash"), make.Binary(Tree.Kind.PLUS, make.Binary(Tree.Kind.MULTIPLY, make.Literal(multiplyNumber), make.Identifier("hash")), variableRead)))); //NOI18N

        statements.add(make.Return(make.Identifier("hash"))); //NOI18N
        BlockTree body = make.Block(statements, false);
        ModifiersTree modifiers = prepareModifiers(wc, mods, make, true);

        return make.Method(modifiers, "hashCode", make.PrimitiveType(TypeKind.INT), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    private static boolean isPrimeNumber(int n) {
        int squareRoot = (int) Math.sqrt(n) + 1;
        if (n % 2 == 0) {
            return false;
        }
        for (int cntr = 3; cntr < squareRoot; cntr++) {
            if (n % cntr == 0) {
                return false;
            }
        }
        return true;
    }
    static int randomNumber = -1;

    private static int generatePrimeNumber(int lowerLimit, int higherLimit) {
        if (randomNumber > 0) {
            return randomNumber;
        }

        Random r = new Random(System.currentTimeMillis());
        int proposed = r.nextInt(higherLimit - lowerLimit) + lowerLimit;
        while (!isPrimeNumber(proposed)) {
            proposed++;
        }
        if (proposed > higherLimit) {
            proposed--;
            while (!isPrimeNumber(proposed)) {
                proposed--;
            }
        }
        return proposed;
    }

    private static ModifiersTree prepareModifiers(WorkingCopy wc, Set<Modifier> mods, TreeMaker make, boolean addOverride) {

        List<AnnotationTree> annotations = new LinkedList<>();

        if (addOverride && supportsOverride(wc)) {
            TypeElement override = wc.getElements().getTypeElement("java.lang.Override");

            if (override != null) {
                annotations.add(wc.getTreeMaker().Annotation(wc.getTreeMaker().QualIdent(override), Collections.<ExpressionTree>emptyList()));
            }
        }

        ModifiersTree modifiers = make.Modifiers(mods, annotations);

        return modifiers;
    }

    /**
     * @param info tested file's info
     *
     * @return true if SourceVersion of source represented by provided info
     * supports Override
     */
    private static boolean supportsOverride(CompilationInfo info) {
        return SourceVersion.RELEASE_5.compareTo(info.getSourceVersion()) <= 0
                && info.getElements().getTypeElement("java.lang.Override") != null;
    }

    private static KindOfType detectKind(CompilationInfo info, TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return KindOfType.valueOf(tm.getKind().name());
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            return ((ArrayType) tm).getComponentType().getKind().isPrimitive() ? KindOfType.ARRAY_PRIMITIVE : KindOfType.ARRAY;
        }

        if (tm.getKind() == TypeKind.DECLARED) {
            Types t = info.getTypes();
            TypeElement en = info.getElements().getTypeElement("java.lang.Enum");

            if (en != null) {
                if (t.isSubtype(tm, t.erasure(en.asType()))) {
                    return KindOfType.ENUM;
                }
            }

            if (((DeclaredType) tm).asElement().getKind().isClass() && ((TypeElement) ((DeclaredType) tm).asElement()).getQualifiedName().contentEquals("java.lang.String")) {
                return KindOfType.STRING;
            }
        }

        return KindOfType.OTHER;
    }

    private static String choosePattern(CompilationInfo info, TypeMirror tm, Map<Acceptor, String> patterns) {
        for (Map.Entry<Acceptor, String> e : patterns.entrySet()) {
            if (e.getKey().accept(info, tm)) {
                return e.getValue();
            }
        }

        throw new IllegalStateException();
    }

    private static ExpressionTree prepareExpression(WorkingCopy wc, Map<Acceptor, String> patterns, TypeMirror tm, String... ve/*, Scope scope*/) {
        String conditionText = preparePattern(wc, tm, patterns, ve);
        ExpressionTree exp = wc.getTreeUtilities().parseExpression(conditionText, new SourcePositions[1]);

        exp = GeneratorUtilities.get(wc).importFQNs(exp);
//        wc.getTreeUtilities().attributeTree(exp, scope);

        return exp;
    }

    private static String preparePattern(WorkingCopy wc, TypeMirror tm, Map<Acceptor, String> patterns, String... ve) {
        String pattern = choosePattern(wc, tm, patterns);
        assert pattern != null;
        Map<String, String> variables = new HashMap<>(ve.length);
        for (int i = 0; i < ve.length; i++) {
            String string = ve[i];
            variables.put("VAR"+ i, string);
        }
        String conditionText = MapFormat.format(pattern, variables);
        return conditionText;
    }

    private static interface Acceptor {

        public boolean accept(CompilationInfo info, TypeMirror tm);
    }

    private static final class SimpleAcceptor implements Acceptor {

        private final Set<KindOfType> kinds;

        public SimpleAcceptor(KindOfType kind) {
            kinds = EnumSet.of(kind);
        }

        public SimpleAcceptor(KindOfType kind, KindOfType... moreKinds) {
            this.kinds = EnumSet.of(kind);
            this.kinds.addAll(Arrays.asList(moreKinds));
        }

        public boolean accept(CompilationInfo info, TypeMirror tm) {
            return kinds.contains(detectKind(info, tm));
        }
    }

    private static class SourceVersionAcceptor implements Acceptor {

        private final SourceVersion minimalVersion;

        public SourceVersionAcceptor(SourceVersion minimalVersion) {
            this.minimalVersion = minimalVersion;
        }

        @Override
        public boolean accept(CompilationInfo info, TypeMirror tm) {
            return !(minimalVersion != null && minimalVersion.compareTo(info.getSourceVersion()) > 0);
        }
    }
    
    private static final class MethodExistsAcceptor extends SourceVersionAcceptor {

        private final String fqn;
        private final String methodName;

        public MethodExistsAcceptor(String fqn, String methodName) {
            this(fqn, methodName, null);
        }

        public MethodExistsAcceptor(String fqn, String methodName, SourceVersion minimalVersion) {
            super(minimalVersion);
            this.fqn = fqn;
            this.methodName = methodName;
        }

        @Override
        public boolean accept(CompilationInfo info, TypeMirror tm) {
            if (!super.accept(info, tm)) {
                return false;
            }

            TypeElement clazz = info.getElements().getTypeElement(fqn);

            if (clazz == null) {
                return false;
            }

            for (ExecutableElement m : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if (m.getSimpleName().contentEquals(methodName)) {
                    return true;
                }
            }

            return false;
        }
    }
    //</editor-fold>
}
