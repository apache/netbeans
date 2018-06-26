/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.api;

import java.net.URL;
import java.util.Set;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
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

        Set<InterfaceElement> getInterfaces(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<TraitElement> getTraits(final NameKind query);

        Set<TypeElement> getTypes(NameKind query, Set<AliasedName> aliases, AliasedElement.Trait trait);

        Set<MethodElement> getConstructors(NameKind typeQuery, Set<AliasedName> aliases, AliasedElement.Trait trait);

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

        /** probably delete, just because of being able to somehow fast rewrite. */
        Set<FileObject> getLocationsForIdentifiers(String identifierName);
    }
}
