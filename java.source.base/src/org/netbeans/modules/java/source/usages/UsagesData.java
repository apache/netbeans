/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.usages;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class UsagesData<T> {

    private final Convertor<? super T, String> convertor;
    private final Map<T, Set<ClassIndexImpl.UsageType>> usages = new HashMap<T, Set<ClassIndexImpl.UsageType>>();
    private final Set<CharSequence> featuresIdents = new HashSet<CharSequence>();
    private final Set<CharSequence> idents = new HashSet<CharSequence>();


    UsagesData(@NonNull final Convertor<? super T, String> convertor) {
        Parameters.notNull("convertor", convertor); //NOI18N
        this.convertor = convertor;
    }

    void addFeatureIdent(@NonNull final CharSequence ident) {
        featuresIdents.add(ident);
    }

    void addIdent(@NonNull final CharSequence ident) {
        idents.add(ident);
    }

    void addUsage(
            @NonNull final T className,
            @NonNull final ClassIndexImpl.UsageType type) {
        Set<ClassIndexImpl.UsageType> usageType = usages.get (className);
        if (usageType == null) {
            usageType = EnumSet.of(type);
            usages.put (className, usageType);
        } else {
            usageType.add (type);
        }
    }

    void addUsages(
            @NonNull final T className,
            @NonNull final ClassIndexImpl.UsageType... types) {
        Set<ClassIndexImpl.UsageType> usageType = usages.get (className);
        if (usageType == null) {
            usageType = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
            usages.put (className, usageType);
        }
        for (ClassIndexImpl.UsageType type : types) {
            usageType.add (type);
        }
    }

    boolean hasUsage(@NonNull final T name) {
        return usages.containsKey(name);
    }

    String featureIdentsToString() {
        return toString(featuresIdents);
    }

    String identsToString() {
        return toString(idents);
    }

    List<String> usagesToStrings() {
        final List<String> result = new ArrayList<String>();
        for (Map.Entry<T,Set<ClassIndexImpl.UsageType>> entry : usages.entrySet()) {
            result.add (
                DocumentUtil.encodeUsage(
                    convertor.convert(entry.getKey()),
                    entry.getValue()));
        }
        return result;
    }

    private String toString(@NonNull final Set<? extends CharSequence> data) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence id : data) {
            sb.append(id);
            sb.append(' '); //NOI18N
        }
        return sb.toString();
    }
}
