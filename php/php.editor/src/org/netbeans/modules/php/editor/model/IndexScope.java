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

package org.netbeans.modules.php.editor.model;

import java.util.List;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;

/**
 * @author Radek Matous
 */
public interface IndexScope extends Scope {

    //for now implemented on top of PHPIndex
    ElementQuery.Index getIndex();
    //globally visible
    List<? extends InterfaceScope> findInterfaces(final QualifiedName ifaceName);
    List<? extends TraitScope> findTraits(final QualifiedName traitName);
    List<? extends TypeScope> findTypes(final QualifiedName typeName);
    List<? extends ClassScope> findClasses(final QualifiedName className);
    List<? extends EnumScope> findEnums(final QualifiedName enumName);
    List<? extends FunctionScope> findFunctions(final QualifiedName fncName);
    List<? extends ConstantElement> findConstants(final QualifiedName constName);
    List<? extends VariableName> findVariables(final String varName);
    //class members
    List<? extends MethodScope> findMethods(TypeScope type);
    List<? extends MethodScope> findMethods(TypeScope type, final String methName, final int... modifiers);
    List<? extends MethodScope>  findInheritedMethods(TypeScope typeScope, String methName);
    List<? extends ClassConstantElement> findClassConstants(TypeScope type);
    List<? extends ClassConstantElement> findClassConstants(TypeScope type, String clsConstName);
    List<? extends ClassConstantElement> findInheritedClassConstants(ClassScope clsScope, String constName);
    List<? extends FieldElement> findFields(ClassScope cls, String field, int... modifiers);
    List<? extends FieldElement> findFields(TraitScope cls, String field, int... modifiers);
    List<? extends FieldElement> findFields(ClassScope cls, int... modifiers);
    List<? extends FieldElement> findFields(TraitScope cls, int... modifiers);
    List<? extends FieldElement> findInheritedFields(ClassScope clsScope, String fieldName);
    List<? extends CaseElement> findEnumCases(TypeScope type);
    List<? extends CaseElement> findEnumCases(TypeScope type, String enumCaseName);

    public interface PHP84IndexScope extends IndexScope {

        /**
         * Find fields(properties) for an interface field name.
         *
         * @param interfaceScope an interface scope
         * @param field a field name
         * @param modifiers modifiers
         * @return field elements
         * @since 2.46.0
         */
        List<? extends FieldElement> findFields(InterfaceScope interfaceScope, String field, int... modifiers);

        /**
         * Find fields(properties) for an interface.
         *
         * @param interfaceScope an interface scope
         * @param modifiers modifiers
         * @return field elements
         * @since 2.46.0
         */
        List<? extends FieldElement> findFields(InterfaceScope interfaceScope, int... modifiers);
    }
}
