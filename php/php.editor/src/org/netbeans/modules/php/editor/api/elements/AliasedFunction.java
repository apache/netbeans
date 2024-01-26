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
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;

/**
 *
 * @author Radek Matous
 */
public class AliasedFunction extends AliasedElement implements FunctionElement {

    public AliasedFunction(final AliasedName aliasedName, final FunctionElement functionElement) {
        super(aliasedName, functionElement);
    }

    protected final FunctionElement getRealFunction() {
        return (FunctionElement) element;
    }
    @Override
    public List<ParameterElement> getParameters() {
        return getRealFunction().getParameters();
    }

    @Override
    public Collection<TypeResolver> getReturnTypes() {
        return getRealFunction().getReturnTypes();
    }

    @Override
    public String getDeclaredReturnType() {
        return getRealFunction().getDeclaredReturnType();
    }

    @Override
    public boolean isReturnUnionType() {
        return getRealFunction().isReturnUnionType();
    }

    @Override
    public boolean isReturnIntersectionType() {
        return getRealFunction().isReturnIntersectionType();
    }

    @Override
    public boolean isAnonymous() {
        return getRealFunction().isAnonymous();
    }

    @Override
    public String asString(PrintAs as) {
        return asString(as, TypeNameResolverImpl.forNull());
    }

    @Override
    public String asString(PrintAs as, TypeNameResolver typeNameResolver) {
        return getRealFunction().asString(as, typeNameResolver);
    }

    @Override
    public String asString(PrintAs as, TypeNameResolver typeNameResolver, PhpVersion phpVersion) {
        return getRealFunction().asString(as, typeNameResolver, phpVersion);
    }

}
