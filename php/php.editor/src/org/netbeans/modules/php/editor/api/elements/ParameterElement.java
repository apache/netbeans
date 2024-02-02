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

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.PhpVersion;

/**
 * @author Radek Matous
 */
public interface ParameterElement {
    String getName();
    String asString(OutputType outputType);
    String asString(OutputType outputType, TypeNameResolver typeNameResolver);
    String asString(OutputType outputType, TypeNameResolver typeNameResolver, PhpVersion phpVersion);
    boolean isReference();
    boolean isVariadic();
    boolean isUnionType();
    boolean isIntersectionType();
    int getModifier();
    Set<TypeResolver> getTypes();
    @CheckForNull
    String getDeclaredType();
    @CheckForNull
    String getPhpdocType();
    @CheckForNull
    String getDefaultValue();
    /**
     * @return false if the type information is taken from PHPDoc
     */
    boolean hasDeclaredType();
    boolean isMandatory();
    int  getOffset();
    OffsetRange  getOffsetRange();

    enum OutputType {
        /**
         * Represents: <code>array &$foo = VERY_SUPER_LONG_DEFAULT_VALUE</code>.
         */
        COMPLETE_DECLARATION,

        /**
         * Represents: <code>array &$foo = ...</code>.
         */
        SHORTEN_DECLARATION,

        /**
         * Represents: <code>$foo</code>.
         */
        SIMPLE_NAME,

        /**
         * Represents: <code>public array &$foo = VERY_SUPER_LONG_DEFAULT_VALUE</code>.
         */
        COMPLETE_DECLARATION_WITH_MODIFIER,

        /**
         * Represents: <code>private array &$foo = ...</code>.
         */
        SHORTEN_DECLARATION_WITH_MODIFIER,
   }
}
