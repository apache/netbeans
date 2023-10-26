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

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.php.api.PhpVersion;

/**
 *
 * @author Radek Matous
 */
public interface BaseFunctionElement extends PhpElement {

    enum PrintAs {
        NameAndParamsDeclaration,
        NameAndParamsInvocation,
        DeclarationWithoutBody,
        DeclarationWithEmptyBody,
        DeclarationWithParentCallInBody,
        ReturnSemiTypes,
        ReturnTypes
    }

    List<ParameterElement> getParameters();
    Collection<TypeResolver> getReturnTypes();
    /**
     * Get the declared return type in the declaration.
     *
     * @return declared return type
     */
    String getDeclaredReturnType();
    /**
     * Check whether return type is a union type.
     *
     * @return {@code true} if not phpdoc but actual return type is a union
     * type, {@code false} otherwise
     */
    boolean isReturnUnionType();
    /**
     * Check whether return type is an intersection type.
     *
     * @return {@code true} if not phpdoc but actual return type is an
     * intersection type, {@code false} otherwise
     */
    boolean isReturnIntersectionType();
    String asString(PrintAs as);
    String asString(PrintAs as, TypeNameResolver typeNameResolver);
    String asString(PrintAs as, TypeNameResolver typeNameResolver, PhpVersion phpVersion);

}
