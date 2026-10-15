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
package org.netbeans.modules.php.editor.api;

import java.net.URL;
import java.util.Set;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public interface ElementQuery {

    public static enum QueryScope {

        INDEX_SCOPE,
        VIRTUAL_SCOPE,
        FILE_SCOPE;

        public boolean isIndexScope() {
            return this.equals(INDEX_SCOPE);
        }

        public boolean isVirtualScope() {
            return this.equals(VIRTUAL_SCOPE);
        }

        public boolean isFileScope() {
            return this.equals(FILE_SCOPE);
        }
    }

    //methods returning declared elements
    Set<ClassElement> getClasses();

    Set<ClassElement> getClasses(NameKind query);


    Set<InterfaceElement> getInterfaces();

    Set<InterfaceElement> getInterfaces(NameKind query);


    Set<TypeElement> getTypes(NameKind query);


    Set<FunctionElement> getFunctions();

    Set<FunctionElement> getFunctions(NameKind query);

    Set<ConstantElement> getConstants();

    Set<ConstantElement> getConstants(NameKind query);

    Set<MethodElement> getConstructors(NameKind typeQuery);

    Set<TypeMemberElement> getTypeMembers(NameKind.Exact typeQuery, NameKind memberQuery);

    Set<MethodElement> getMethods(NameKind.Exact typeQuery, NameKind methodQuery);

    Set<FieldElement> getFields(NameKind.Exact classQuery, NameKind fieldQuery);

    Set<TypeConstantElement> getTypeConstants(NameKind.Exact typeQuery, NameKind constantQuery);

    Set<MethodElement> getMethods(NameKind methodQuery);

    Set<FieldElement> getFields(NameKind fieldQuery);

    Set<TypeConstantElement> getTypeConstants(NameKind constantQuery);

    Set<EnumCaseElement> getEnumCases(NameKind.Exact typeQuery, NameKind enumCaseQuery);

    Set<EnumCaseElement> getEnumCases(NameKind enumCaseQuery);

    Set<VariableElement> getTopLevelVariables(NameKind query);

    Set<NamespaceElement> getNamespaces(NameKind query);

    QueryScope getQueryScope();

    public interface File extends ElementQuery {
        FileObject getFileObject();

        URL getURL();

        PHPParseResult getResult();

        Set<MethodElement> getDeclaredMethods(TypeElement typeElement);

        Set<FieldElement> getDeclaredFields(TypeElement classQuery);

        Set<TypeConstantElement> getDeclaredTypeConstants(TypeElement typeElement);

        Set<VariableElement> getTopLevelVariables();

        Set<VariableElement> getMethodVariables(MethodElement method);

        Set<VariableElement> getFunctionVariables(FunctionElement function);
    }

    public interface Index extends ElementQuery {
        Set<PhpElement> getTopLevelElements(NameKind query);

        Set<FunctionElement> getFunctions(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<ConstantElement> getConstants(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<ClassElement> getClasses(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        /**
         * Get classes marked as Attribute(#[\Attribute]).
         *
         * @param query the query
         * @param aliases aliased names
         * @param trait the trait
         * @return classes marked as #[\Attribute]
         * @since 2.36.0
         */
        Set<ClassElement> getAttributeClasses(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<InterfaceElement> getInterfaces(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<EnumElement> getEnums(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        /**
         * Get traits.
         *
         * @param query the query
         * @param aliases aliased names
         * @param trait the trait
         * @return traits
         * @since 2.38.0
         */
        Set<TraitElement> getTraits(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<TraitElement> getTraits(final NameKind query);

        Set<EnumElement> getEnums(final NameKind query);

        Set<TypeElement> getTypes(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<MethodElement> getConstructors(NameKind typeQuery, Set<AliasedName> aliases, AliasedElement.Trait trait);

        /**
         * Get constructors of attribute classes.
         * <pre>
         * #[\Attribute]
         * class MyAttibute {
         *     public function __construct(int $int, string $string) {}
         * }
         * </pre>
         *
         * @param typeQuery the query
         * @param aliases aliased names
         * @param trait the trait
         * @return constructors of attribute classes
         * @since 2.36.0
         */
        Set<MethodElement> getAttributeClassConstructors(NameKind typeQuery, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<NamespaceElement> getNamespaces(NameKind query, Set<AliasedName> aliasedNames, AliasedElement.Trait trait);

        Set<PhpElement> getTopLevelElements(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);


        Set<MethodElement> getDeclaredConstructors(ClassElement typeElement);

        Set<MethodElement> getConstructors(ClassElement typeElement);

        Set<MethodElement> getAccessibleMagicMethods(TypeElement type);

        Set<TypeConstantElement> getAccessibleMagicConstants(TypeElement type);

        Set<TypeMemberElement> getDeclaredTypeMembers(TypeElement typeElement);

        Set<MethodElement> getDeclaredMethods(TypeElement typeElement);

        Set<FieldElement> getDeclaredFields(TypeElement classQuery);

        Set<TypeConstantElement> getDeclaredTypeConstants(TypeElement typeElement);

        Set<EnumCaseElement> getDeclaredEnumCases(TypeElement typeElement);

        Set<TypeElement> getDirectInheritedTypes(final TypeElement typeElement);

        Set<ClassElement> getDirectInheritedClasses(final TypeElement typeElement);

        Set<InterfaceElement> getDirectInheritedInterfaces(final TypeElement typeElement);

        Set<TypeElement> getDirectInheritedByTypes(final TypeElement typeElement);

        Set<TypeElement> getInheritedByTypes(final TypeElement typeElement);
        /**
         * @return all extended classes (see method getInheritedClasses) + implemented interfaces (see method getInheritedInterfaces)
         * recursively
         */
        Set<TypeElement> getInheritedTypes(TypeElement typeElement);
        TreeElement<TypeElement> getInheritedTypesAsTree(TypeElement typeElement);
        TreeElement<TypeElement> getInheritedTypesAsTree(TypeElement typeElement, final Set<TypeElement> preferredTypes);
        TreeElement<TypeElement> getInheritedByTypesAsTree(TypeElement typeElement);
        TreeElement<TypeElement> getInheritedByTypesAsTree(TypeElement typeElement, final Set<TypeElement> preferredTypes);
        /**
         * @return all extended classes recursively
         */
        Set<ClassElement> getInheritedClasses(TypeElement typeElement);

        /**
         * @return all implemented interfaces recursively
         */
        Set<InterfaceElement> getInheritedInterfaces(TypeElement typeElement);

        /**
         * @return all not private fields from inherited types (see getInheritedTypes method)
         */
        Set<FieldElement> getInheritedFields(TypeElement classElement);

        /**
         * @return all not private, static fields from inherited types (see getInheritedTypes method)
         */
        Set<FieldElement> getStaticInheritedFields(TypeElement classElement);

        /**
         * @return all not private methods from inherited types (see getInheritedTypes method)
         */
        Set<MethodElement> getInheritedMethods(TypeElement typeElement);

        /**
         * @return all not private, static methods from inherited types (see getInheritedTypes method)
         */
        Set<MethodElement> getStaticInheritedMethods(TypeElement typeElement);

        /**
         * @return all type constants from inherited types (see getInheritedTypes method)
         */
        Set<TypeConstantElement> getInheritedTypeConstants(TypeElement typeElement);

        Set<TypeMemberElement> getAccessibleTypeMembers(TypeElement typeElement, TypeElement calledFromEnclosingType);

        Set<TypeMemberElement> getAccessibleMixinTypeMembers(TypeElement typeElement, TypeElement calledFromEnclosingType);

        /**
         * @param typeElement
         * @param insideEnclosingType false means that private, protected elements are filtered. True
         * means that declared elements are not filtered at all and inherited elements are filtered if private
         * @return declared + inherited, overriden elements are filtered
         */
        Set<MethodElement> getAccessibleMethods(TypeElement typeElement, TypeElement calledFromEnclosingType);

        /**
         * see method getAccessible... - just not static elements are filtered
         */
        Set<MethodElement> getAccessibleStaticMethods(TypeElement typeElement, TypeElement calledFromEnclosingType);

        /**
         * @param typeElement
         * @param insideEnclosingType false means that private, protected elements are filtered. True
         * means that declared elements are not filtered at all and inherited elements are filtered if private
         * @return declared + inherited, overriden elements are filtered
         */
        Set<FieldElement> getAccessibleFields(TypeElement classElement, TypeElement calledFromEnclosingType);

        /**
         * see method getAccessible... - just not static elements are filtered
         */
        Set<FieldElement> getAccessibleStaticFields(TypeElement classElement, TypeElement calledFromEnclosingType);

        Set<TypeMemberElement> getInheritedTypeMembers(final TypeElement typeElement);

        Set<TypeMemberElement> getAllTypeMembers(TypeElement typeElement);

        /**
         * @return declared + inherited elements (private,protected, public) - only overriden elements are filtered
         */
        Set<MethodElement> getAllMethods(TypeElement typeElement);

        /**
         * @return declared + inherited elements (private,protected, public) - only overriden elements are filtered
         */
        Set<FieldElement> getAlllFields(TypeElement typeElement);

        /**
         * @return declared + inherited elements  - only overriden elements are filtered
         */
        Set<TypeConstantElement> getAllTypeConstants(TypeElement classElement);

        Set<EnumCaseElement> getAllEnumCases(TypeElement enumElement);

        /**
         * @return declared + inherited elements (private,protected, public) - only overriden elements are filtered
         */
        Set<MethodElement> getAllMethods(NameKind.Exact typeQuery, NameKind methodQuery);

        /**
         * @return declared + inherited elements (private,protected, public) - only overriden elements are filtered
         */
        Set<FieldElement> getAlllFields(NameKind.Exact classQuery, NameKind fieldQuery);

        /**
         * @return declared + inherited elements (private,protected, public) - only overriden elements are filtered
         */
        Set<TypeConstantElement> getAllTypeConstants(NameKind.Exact typeQuery, NameKind constantQuery);

        Set<EnumCaseElement> getAllEnumCases(NameKind.Exact typeQuery, NameKind enumCaseQuery);

        /** probably delete, just because of being able to somehow fast rewrite. */
        Set<FileObject> getLocationsForIdentifiers(String identifierName);
    }
}
