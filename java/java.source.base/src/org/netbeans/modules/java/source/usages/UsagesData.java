/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
