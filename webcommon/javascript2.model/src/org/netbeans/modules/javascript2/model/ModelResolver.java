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
package org.netbeans.modules.javascript2.model;

import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.util.Lookup;


/**
 *
 * @author Tomas Zezula
 */
public interface ModelResolver {
    void init();
    JsObject getGlobalObject();
    JsObject resolveThis(JsObject where);
    void processCalls(
            ModelElementFactory elementFactory,
            Map<String, Map<Integer, List<TypeUsage>>> returnTypesFromFrameworks);
    @NonNull
    List<Identifier> getASTNodeName(Object astNode);

    public static interface Provider {
        ModelResolver create(
                @NonNull ParserResult result,
                @NonNull final OccurrenceBuilder occurrenceBuilder);
    }

    @CheckForNull
    public static ModelResolver create (
            @NonNull final ParserResult result,
            @NonNull final OccurrenceBuilder occurrenceBuilder) {
        for (Provider provider : Lookup.getDefault().lookupAll(Provider.class)) {
            final ModelResolver resolver = provider.create(result, occurrenceBuilder);
            if (resolver != null) {
                return resolver;
            }
        }
        return null;
    }
}
