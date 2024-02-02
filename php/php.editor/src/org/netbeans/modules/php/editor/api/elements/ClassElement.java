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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;

/**
 * @author Radek Matous
 */
public interface ClassElement extends TraitedElement {
    PhpElementKind KIND = PhpElementKind.CLASS;
    @CheckForNull
    QualifiedName getSuperClassName();
    Collection<QualifiedName> getPossibleFQSuperClassNames();
    Collection<QualifiedName> getFQMixinClassNames();
    boolean isFinal();
    boolean isAbstract();
    boolean isReadonly();
    boolean isAnonymous();
    /**
     * Check whether a class is marked with #[\Attribute].
     *
     * @return {@code true} if a class is marked with #[\Attribute],
     * {@code false} otherwise
     * @since 2.36.0
     */
    boolean isAttribute();
}
