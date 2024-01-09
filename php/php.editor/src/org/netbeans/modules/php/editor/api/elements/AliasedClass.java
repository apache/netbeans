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
import java.util.Collections;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;

/**
 *
 * @author Radek Matous
 */
public final class AliasedClass extends AliasedType implements ClassElement {
    public AliasedClass(final AliasedName aliasedName, final ClassElement clz) {
        super(aliasedName, clz);
    }

    @Override
    public QualifiedName getSuperClassName() {
        return getClassElement().getSuperClassName();
    }

    @Override
    public boolean isFinal() {
        return getClassElement().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getClassElement().isAbstract();
    }

    @Override
    public boolean isReadonly() {
        return getClassElement().isReadonly();
    }

    @Override
    public boolean isAnonymous() {
        return getClassElement().isAnonymous();
    }

    private ClassElement getClassElement() {
        return (ClassElement) element;
    }

    @Override
    public Collection<QualifiedName> getPossibleFQSuperClassNames() {
        return Collections.emptyList();
    }

    @Override
    public Collection<QualifiedName> getFQMixinClassNames() {
        return getClassElement().getFQMixinClassNames();
    }

    @Override
    public Collection<QualifiedName> getUsedTraits() {
        return getClassElement().getUsedTraits();
    }

    @Override
    public boolean isAttribute() {
        return getClassElement().isAttribute();
    }
}
