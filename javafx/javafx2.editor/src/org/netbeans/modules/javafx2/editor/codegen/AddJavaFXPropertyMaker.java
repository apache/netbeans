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
package org.netbeans.modules.javafx2.editor.codegen;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.javafx2.editor.codegen.AddFxPropertyConfig.ACCESS;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class AddJavaFXPropertyMaker {

    private WorkingCopy javac;
    private Scope scope;
    private TreeMaker make;
    private AddFxPropertyConfig config;
    private String getterMethod;
    private boolean hasGet;

    public AddJavaFXPropertyMaker(WorkingCopy javac, Scope scope, TreeMaker make, AddFxPropertyConfig config) {
        this.javac = javac;
        this.scope = scope;
        this.make = make;
        this.config = config;
    }

    private MethodTree createGetter(ModifiersTree mods, TypeMirror valueType) {
        StringBuilder getterName = GeneratorUtils.getCapitalizedName(config.getName());
        getterName.insert(0, valueType.getKind() == TypeKind.BOOLEAN ? "is" : "get");
        ReturnTree returnTree = make.Return(make.MethodInvocation(Collections.emptyList(), make.MemberSelect(make.Identifier(config.getName()), hasGet ? "get" : "getValue"), Collections.emptyList()));
        BlockTree getterBody = make.Block(Collections.singletonList(returnTree), false);
        Tree valueTree;
        if (valueType.getKind() == TypeKind.DECLARED) {
            valueTree = make.QualIdent(((DeclaredType) valueType).asElement());
        } else if (valueType.getKind().isPrimitive()) {
            valueTree = make.PrimitiveType(valueType.getKind());
        } else {
            valueTree = make.Identifier(valueType.toString());
        }
        MethodTree getter = make.Method(mods, getterName, valueTree, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), getterBody, null);
        return getter;
    }

    private MethodTree createSetter(ModifiersTree mods, TypeMirror valueType) {
        StringBuilder getterName = GeneratorUtils.getCapitalizedName(config.getName());
        getterName.insert(0, "set");
        Tree valueTree;
        if (valueType.getKind() == TypeKind.DECLARED) {
            valueTree = make.QualIdent(((DeclaredType) valueType).asElement());
        } else if (valueType.getKind().isPrimitive()) {
            valueTree = make.PrimitiveType(valueType.getKind());
        } else {
            valueTree = make.Identifier(valueType.toString());
        }
        StatementTree statement = make.ExpressionStatement(make.MethodInvocation(Collections.emptyList(), make.MemberSelect(make.Identifier(config.getName()), hasGet ? "set" : "setValue"), Collections.singletonList(make.Identifier("value"))));
        BlockTree getterBody = make.Block(Collections.singletonList(statement), false);
        VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "value", valueTree, null);
        MethodTree getter = make.Method(mods, getterName, make.PrimitiveType(TypeKind.VOID), Collections.emptyList(), Collections.singletonList(var), Collections.emptyList(), getterBody, null);
        return getter;
    }

    private MethodTree createProperty(ModifiersTree mods, DeclaredType selectedType, ExecutableElement wrapperMethod) {
        String getterName = config.getName() + "Property";
        ExpressionTree expression;
        if (wrapperMethod == null) {
            expression = make.Identifier(config.getName());
        } else {
            expression = make.MethodInvocation(Collections.emptyList(), make.MemberSelect(make.Identifier(config.getName()), wrapperMethod.getSimpleName()), Collections.emptyList());
        }
        ReturnTree returnTree = make.Return(expression);
        BlockTree getterBody = make.Block(Collections.singletonList(returnTree), false);
        MethodTree getter = make.Method(mods, getterName, selectedType == null ? make.Identifier(config.getPropertyType()) : make.QualIdent(selectedType.asElement()), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), getterBody, null);
        return getter;
    }

    public List<Tree> createMembers() {
        Elements elements = javac.getElements();
        TypeElement readOnlyProperty = elements.getTypeElement("javafx.beans.property.ReadOnlyProperty");
        TypeElement property = elements.getTypeElement("javafx.beans.property.Property");
        if (readOnlyProperty == null || property == null) {
            return null;
        }

        String type = config.getPropertyType();
        TreeUtilities treeUtilities = javac.getTreeUtilities();
        DeclaredType selectedType = (DeclaredType) treeUtilities.parseType(type, scope.getEnclosingClass());
        if (selectedType == null || selectedType.getKind() == TypeKind.ERROR) {
            selectedType = (DeclaredType) treeUtilities.parseType("javafx.beans.property." + type, scope.getEnclosingClass());
        }
        TypeMirror valueType = findValueType(selectedType);
        
        // hack: javac.parseType cannot handle diamond operator. If diamond is present
        // strip it, and then decorate the TypeTree
        String implTypeDef = config.getImplementationType();
        int diamond = implTypeDef.indexOf("<>"); // NOI18N
        if (diamond > -1) {
            implTypeDef = implTypeDef.substring(0, diamond);
        }
        DeclaredType implementationType = (DeclaredType) treeUtilities.parseType(implTypeDef, scope.getEnclosingClass());
        if (implementationType == null || implementationType.getKind() == TypeKind.ERROR) {
            implementationType = (DeclaredType) treeUtilities.parseType("javafx.beans.property." + implTypeDef, scope.getEnclosingClass());
        }
        ExpressionTree implTypeTree;
        
        if (diamond != -1) {
            implTypeTree = (ExpressionTree)make.ParameterizedType(make.Type(implementationType), Collections.<Tree>emptyList());
        } else {
            implTypeTree = getTypeTree(implementationType);
        }
        boolean writable = config.getGenerate() == AddFxPropertyConfig.GENERATE.WRITABLE;

        ModifiersTree mods = createMods();
        VariableTree field;
        ExecutableElement wrapperMethod = null;
        if (writable || implementationType == null) {
            field = createField(selectedType, implTypeTree);
        } else {
            List<? extends ExecutableElement> methods = ElementFilter.methodsIn(javac.getElements().getAllMembers((TypeElement) implementationType.asElement()));
            for (ExecutableElement method : methods) {
                if (selectedType != null && javac.getTypes().isSubtype(method.getReturnType(), selectedType)) {
                    wrapperMethod = method;
                    break;
                } else if (method.getReturnType().getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) method.getReturnType();
                    if (declaredType.asElement().getSimpleName().contentEquals(config.getPropertyType())) {
                        wrapperMethod = method;
                        break;
                    }
                }
            }
            if (wrapperMethod != null) {
                field = createField(implementationType, implTypeTree);
            } else {
                field = createField(selectedType, implTypeTree);
            }
        }
        MethodTree getter = createGetter(mods, valueType);
        MethodTree setter = null;
        if (writable) {
            setter = createSetter(mods, valueType);
        }
        MethodTree method = createProperty(mods, selectedType, wrapperMethod);
        return writable ? Arrays.asList(field, getter, setter, method) : Arrays.asList(field, getter, method);
    }

    private VariableTree createField(DeclaredType selectedType, ExpressionTree implementationType) {
        String initializer = config.getInitializer();
        NewClassTree newClass = make.NewClass(null,
                Collections.emptyList(),
                implementationType,
                Collections.singletonList(make.Identifier(initializer)), null);
        VariableTree property = make.Variable(
                make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)),
                config.getName(),
                getTypeTree(selectedType),
                newClass);
        return property;
    }

    private ModifiersTree createMods() {
        ACCESS access = config.getAccess();
        ModifiersTree mods;
        switch (access) {
            case PACKAGE:
                mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                break;
            case PRIVATE:
                mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                break;
            case PROTECTED:
                mods = make.Modifiers(EnumSet.of(Modifier.PROTECTED));
                break;
            case PUBLIC:
            default:
                mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                break;
        }
        return mods;
    }

    private TypeMirror findValueType(TypeMirror selectedType) {
        Types types = javac.getTypes();
        TypeMirror valueType = null;
        if (selectedType != null && selectedType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) selectedType;
            List<ExecutableElement> methods = ElementFilter.methodsIn(javac.getElements().getAllMembers((TypeElement) declaredType.asElement()));
            for (ExecutableElement executableElement : methods) {
                if (executableElement.getSimpleName().contentEquals("get") && executableElement.getParameters().isEmpty()) {
                    hasGet = true;
                    ExecutableType member = (ExecutableType) types.asMemberOf(declaredType, executableElement);
                    valueType = member.getReturnType();
                    break;
                }
                if (executableElement.getSimpleName().contentEquals("getValue") && executableElement.getParameters().isEmpty()) {
                    hasGet = false;
                    ExecutableType member = (ExecutableType) types.asMemberOf(declaredType, executableElement);
                    valueType = member.getReturnType();
                }
            }
        }
        if (valueType == null) {
            valueType = javac.getElements().getTypeElement(Object.class.getName()).asType();
        }
        return valueType;
    }

    private ExpressionTree getTypeTree(DeclaredType type) {
        ExpressionTree ident = make.QualIdent(type.asElement());
        List<? extends TypeMirror> arguments = type.getTypeArguments();
        List<Tree> newArguments = new ArrayList<Tree>(arguments.size());
        for (TypeMirror typeMirror : arguments) {
            newArguments.add(make.Type(typeMirror));
        }
        if (!newArguments.isEmpty()) {
            ident = (ExpressionTree) make.ParameterizedType(ident, newArguments);
        }
        return ident;
    }
}
