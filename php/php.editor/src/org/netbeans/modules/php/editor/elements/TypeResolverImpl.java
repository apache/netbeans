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
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.PhpElementImpl.Separator;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.openide.util.Pair;

/**
 * This is simple immutable impl.
 * @author Radek Matous
 */
public final class TypeResolverImpl implements TypeResolver {
    private static final Logger LOG = Logger.getLogger(TypeResolverImpl.class.getName());

    private final String typeName;
    private final boolean isNullableType;

    public static Set<TypeResolver> parseTypes(final String typeSignature) {
        // use LinkedHashSet to keep the type order
        // avoid being changed type order(e.g. int|float|Foo|Bar) when an override method is generated
        Set<TypeResolver> retval = new LinkedHashSet<>();
        if (typeSignature != null && typeSignature.length() > 0) {
            for (String type : Type.splitTypes(typeSignature)) {
                String typeName = type;
                boolean isNullableType = CodeUtils.isNullableType(typeName);
                if (isNullableType) {
                    typeName = typeName.substring(1);
                }
                String encodedTypeName;
                if (isResolvedImpl(typeName)) {
                    encodedTypeName = ParameterElementImpl.encode(typeName);
                } else {
                    final EnumSet<Separator> separators = Separator.toEnumSet();
                    separators.remove(Separator.COLON);
                    encodedTypeName = ParameterElementImpl.encode(typeName, separators);
                }
                if (typeName.equals(encodedTypeName)) {
                    retval.add(new TypeResolverImpl(typeName, isNullableType));
                } else {
                    log(String.format("wrong typename: \"%s\" parsed from \"%s\"", typeSignature, typeName), Level.FINE); //NOI18N
                }
            }
        }
        return retval;
    }

    public static Set<TypeResolver> forNames(final Collection<Pair<QualifiedName, Boolean>> names) {
        Set<TypeResolver> retval = new LinkedHashSet<>();
        for (Pair<QualifiedName, Boolean> name : names) {
            QualifiedName qualifiedName = name.first();
            final String typeName = qualifiedName.toString();
            if (typeName.equals(ParameterElementImpl.encode(typeName))) {
                retval.add(new TypeResolverImpl(typeName, name.second()));
            } else {
                log(String.format("wrong typename: \"%s\"", typeName), Level.FINE); //NOI18N
            }
        }
        return retval;
    }

    TypeResolverImpl(final String semiTypeName, boolean isNullableType) {
        this.typeName = semiTypeName;
        this.isNullableType = isNullableType;
    }

    public String getSignature() {
        return isNullableType ? CodeUtils.NULLABLE_TYPE_PREFIX + getRawTypeName() : getRawTypeName();
    }

    @Override
    public boolean isResolved() {
        return isResolvedImpl(typeName);
    }

    private static boolean isResolvedImpl(final String typeName) {
        return typeName != null && !VariousUtils.isSemiType(typeName);
    }

    @Override
    public boolean canBeResolved() {
        return isResolved();
    }

    @Override
    public synchronized QualifiedName getTypeName(boolean resolve) {
        return isResolved() ? QualifiedName.create(typeName) : null;
    }

    @Override
    public String getRawTypeName() {
        return typeName;
    }

    @Override
    public boolean isNullableType() {
        return isNullableType;
    }

    private static void log(final String message, final Level level) {
        if (LOG.isLoggable(level)) {
            LOG.log(level, message);
        }
    }
}
