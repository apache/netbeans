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
package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeElement;

/**
 * @author Radek Matous
 */
public abstract class TypeElementImpl extends FullyQualifiedElementImpl implements TypeElement {

    private final PhpModifiers modifiers;
    private final Set<QualifiedName> superInterfaces;
    private final Collection<QualifiedName> fqSuperInterfaces;

    /**
     * @param constants might be null which means not initialized (so will be
     * loaded later)
     * @param constants might be null which means not initialized (so will be
     * loaded later)
     */
    protected TypeElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final Set<QualifiedName> ifaceNames,
            final Collection<QualifiedName> fqSuperInterfaces,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(), fileUrl, offset, elementQuery, isDeprecated);
        this.superInterfaces = ifaceNames;
        this.modifiers = PhpModifiers.fromBitMask(flags);
        this.fqSuperInterfaces = fqSuperInterfaces;
    }

    @Override
    public final PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    @Override
    public final Set<QualifiedName> getSuperInterfaces() {
        return Collections.unmodifiableSet(superInterfaces);
    }

    @Override
    public Collection<QualifiedName> getFQSuperInterfaceNames() {
        return Collections.unmodifiableCollection(fqSuperInterfaces);
    }

    @Override
    public final boolean isClass() {
        return getPhpElementKind().equals(PhpElementKind.CLASS);
    }

    @Override
    public final boolean isInterface() {
        return getPhpElementKind().equals(PhpElementKind.IFACE);
    }

    @Override
    public final boolean isTrait() {
        return getPhpElementKind().equals(PhpElementKind.TRAIT);
    }

    @Override
    public boolean isTraited() {
        return getPhpElementKind().equals(PhpElementKind.TRAIT)
                || getPhpElementKind().equals(PhpElementKind.CLASS)
                || getPhpElementKind().equals(PhpElementKind.ENUM);
    }

    @Override
    public boolean isEnum() {
        return getPhpElementKind() == PhpElementKind.ENUM;
    }

}
