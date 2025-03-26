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

package org.netbeans.modules.editor.java;

import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public final class JavaCompletionItemFactory implements JavaCompletionTask.TypeCastableItemFactory<JavaCompletionItem>,
        JavaCompletionTask.LambdaItemFactory<JavaCompletionItem>,
        JavaCompletionTask.ModuleItemFactory<JavaCompletionItem>,
        JavaCompletionTask.RecordPatternItemFactory<JavaCompletionItem> {

    private final WhiteListQuery.WhiteList whiteList;

    public JavaCompletionItemFactory(FileObject fo) {
        whiteList = fo != null ? WhiteListQuery.getWhiteList(fo) : null;
    }

    @Override
    public JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
        return JavaCompletionItem.createKeywordItem(kwd, postfix, substitutionOffset, smartType);
    }

    @Override
    public JavaCompletionItem createModuleItem(String moduleName, int substitutionOffset) {
        return JavaCompletionItem.createModuleItem(moduleName, substitutionOffset);
    }

    @Override
    public JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
        return JavaCompletionItem.createPackageItem(pkgFQN, substitutionOffset, inPackageStatement);
    }

    @Override
    public JavaCompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
        return JavaCompletionItem.createTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType, whiteList);
    }

    @Override
    public JavaCompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
        return LazyJavaCompletionItem.createTypeItem(handle, kinds, substitutionOffset, referencesCount, source, insideNew, addTypeVars, afterExtends, whiteList);
    }

    @Override
    public JavaCompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
        return JavaCompletionItem.createArrayItem(info, type, substitutionOffset, referencesCount, elements, whiteList);
    }

    @Override
    public JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
        return JavaCompletionItem.createTypeParameterItem(elem, substitutionOffset);
    }

    @Override
    public JavaCompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
        return JavaCompletionItem.createVariableItem(info, elem, type, null, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset, whiteList);
    }

    @Override
    public JavaCompletionItem createTypeCastableVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
        return JavaCompletionItem.createVariableItem(info, elem, type, castType, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset, whiteList);
    }

    @Override
    public JavaCompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
        return JavaCompletionItem.createVariableItem(info, varName, substitutionOffset, newVarName, smartType);
    }

    @Override
    public JavaCompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
        return createExecutableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, false, smartType, assignToVarOffset, memberRef);
    }

    @Override
    public JavaCompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean afterConstructorTypeParams, boolean smartType, int assignToVarOffset, boolean memberRef) {
        return JavaCompletionItem.createExecutableItem(info, elem, type, null, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, afterConstructorTypeParams, smartType, assignToVarOffset, memberRef, whiteList);
    }

    @Override
    public JavaCompletionItem createTypeCastableExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
        return JavaCompletionItem.createExecutableItem(info, elem, type, castType, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, false, smartType, assignToVarOffset, memberRef, whiteList);
    }

    @Override
    public JavaCompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
        return JavaCompletionItem.createThisOrSuperConstructorItem(info, elem, type, substitutionOffset, isDeprecated, name, whiteList);
    }

    @Override
    public JavaCompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
        return JavaCompletionItem.createOverrideMethodItem(info, elem, type, substitutionOffset, implement, whiteList);
    }

    @Override
    public JavaCompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
        return JavaCompletionItem.createGetterSetterMethodItem(info, elem, type, substitutionOffset, name, setter);
    }

    @Override
    public JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
        return JavaCompletionItem.createDefaultConstructorItem(elem, substitutionOffset, smartType);
    }

    @Override
    public JavaCompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
        return JavaCompletionItem.createParametersItem(info, elem, type, substitutionOffset, isDeprecated, activeParamIndex, name);
    }

    @Override
    public JavaCompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
        return JavaCompletionItem.createAnnotationItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, whiteList);
    }

    @Override
    public JavaCompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
        return JavaCompletionItem.createAttributeItem(info, elem, type, substitutionOffset, isDeprecated);
    }

    @Override
    public JavaCompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
        return JavaCompletionItem.createAttributeValueItem(info, value, documentation, element, substitutionOffset, referencesCount, whiteList);
    }

    @Override
    public JavaCompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, boolean smartType) {
        return JavaCompletionItem.createStaticMemberItem(info, type, memberElem, memberType, multipleVersions, substitutionOffset, isDeprecated, addSemicolon, smartType, whiteList);
    }

    @Override
    public JavaCompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, boolean smartType) {
        return LazyJavaCompletionItem.createStaticMemberItem(handle, name, substitutionOffset, addSemicolon, referencesCount, source, smartType, whiteList);
    }

    @Override
    public JavaCompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
        return JavaCompletionItem.createChainedMembersItem(info, chainedElems, chainedTypes, substitutionOffset, isDeprecated, addSemicolon, whiteList);
    }

    @Override
    public JavaCompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
        return JavaCompletionItem.createInitializeAllConstructorItem(info, isDefault, fields, superConstructor, parent, substitutionOffset);
    }

    @Override
    public JavaCompletionItem createLambdaItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, boolean expression, boolean addSemicolon) {
        return JavaCompletionItem.createLambdaItem(info, elem, type, substitutionOffset, expression, addSemicolon);
    }

    @Override
    public JavaCompletionItem createRecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars) {
        return JavaCompletionItem.createRecordPatternItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars);
    }
}
