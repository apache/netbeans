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
import java.util.Set;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;

/**
 *
 * @author Raddek Matous
 */
public class AliasedType extends AliasedElement implements TypeElement {

    protected AliasedType(final AliasedName aliasedName, final TypeElement type) {
        super(aliasedName, type);
    }

    protected final TypeElement getRealType() {
        return (TypeElement) element;
    }

    @Override
    public final String asString(PrintAs as) {
        return getRealType().asString(as);
    }

    @Override
    public final Set<QualifiedName> getSuperInterfaces() {
        return getRealType().getSuperInterfaces();
    }

    @Override
    public final boolean isClass() {
        return getRealType().isClass();
    }

    @Override
    public final boolean isInterface() {
        return getRealType().isInterface();
    }

    @Override
    public final boolean isTrait() {
        return getRealType().isTrait();
    }

    @Override
    public boolean isTraited() {
        return getRealType().isTraited();
    }

    @Override
    public final boolean isEnum() {
        return getRealType().isEnum();
    }

    @Override
    public Collection<QualifiedName> getFQSuperInterfaceNames() {
        return Collections.<QualifiedName>emptyList();
    }
}
