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

import java.util.Collection;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;

/**
 * @author Radek Matous
 */
public interface TypeScope extends Scope, FullyQualifiedElement, TypeElement {

    Collection<? extends MethodScope> getDeclaredMethods();
    Collection<? extends MethodScope> getInheritedMethods();
    Collection<? extends MethodScope> getMethods();
    Collection<? extends ClassConstantElement> getDeclaredConstants();
    Collection<? extends ClassConstantElement> getInheritedConstants();
    Collection<? extends InterfaceScope> getSuperInterfaceScopes();
    Collection<? extends String> getSuperInterfaceNames();
    boolean isSuperTypeOf(TypeScope subType);
    boolean isSubTypeOf(TypeScope subType);
    // TODO: must be removed!!! only OverridingMethodsImpl uses it...for some recursion check?
    String getIndexSignature();

    public interface FieldDeclarable extends TypeScope {
        /**
         * Get declared fields.
         *
         * @return declared fields
         * @since 2.46.0
         */
        Collection<? extends FieldElement> getDeclaredFields();

        /**
         * Get inherited fields.
         *
         * @return declared fields
         * @since 2.46.0
         */
        Collection<? extends FieldElement> getInheritedFields();
    }

}
