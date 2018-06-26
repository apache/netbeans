/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.PhpElementImpl.Separator;
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
        Set<TypeResolver> retval = new HashSet<>();
        if (typeSignature != null && typeSignature.length() > 0) {
            final String regexp = String.format("\\%s", Separator.PIPE.toString()); //NOI18N
            for (String type : typeSignature.split(regexp)) {
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
        Set<TypeResolver> retval = new HashSet<>();
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
