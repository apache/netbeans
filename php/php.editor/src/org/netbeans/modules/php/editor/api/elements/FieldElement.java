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
package org.netbeans.modules.php.editor.api.elements;

import java.util.List;
import org.netbeans.modules.php.editor.api.PhpElementKind;

/**
 * @author Radek Matous
 */
public interface FieldElement extends TypedInstanceElement, TypeMemberElement {

    PhpElementKind KIND = PhpElementKind.FIELD;

    String getName(boolean dollared);

    boolean isAnnotation();

    boolean isUnionType();

    boolean isIntersectionType();

    String getDeclaredType();

    /**
     * Check whether this element is a hooked property(field).
     *
     * @param field
     * @return {@code true} it's hooked property, {@code false} otherwise
     * @since 2.46.0
     */
    public static boolean isHooked(FieldElement field) {
        return (field instanceof HookedFieldElement)
                && ((HookedFieldElement) field).isHooked();
    }

    public interface HookedFieldElement extends FieldElement {

        /**
         * Check whether this element is a hooked property.
         *
         * @return {@code true} it's hooked property, {@code false} otherwise
         * @since 2.46.0
         */
        boolean isHooked();

        /**
         * Get property hooks.
         *
         * @return property hooks
         * @since 2.46.0
         */
        List<? extends PropertyHookElement> getPropertyHooks();
    }
}
