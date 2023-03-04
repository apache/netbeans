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
package org.netbeans.modules.parsing.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class EmbeddingProviderFactory extends TaskFactory {

    public static final String ATTR_TARGET_MIME_TYPE = "targetMimeType";   //NOI18N
    public static final String ATTR_PROVIDER = "provider";                 //NOI18N
    
    private final Map<String,Object> params;
    private final String targetMimeType;

    private EmbeddingProviderFactory(@NonNull final Map<String,Object> params) {
        Parameters.notNull("definition", params);   //NOI18N
        this.params = params;
        this.targetMimeType = (String) params.get(ATTR_TARGET_MIME_TYPE);
        if (this.targetMimeType == null) {
            throw new IllegalArgumentException(
                String.format(
                    "The definition file has no %s attribute.", //NOI18N
                    ATTR_TARGET_MIME_TYPE));
        }
    }

    public String getTargetMimeType() {
        return this.targetMimeType;
    }

    @NonNull
    @Override
    public Collection<? extends SchedulerTask> create (@NonNull final Snapshot snapshot) {
        final Object delegate = params.get(ATTR_PROVIDER);
        return (delegate instanceof EmbeddingProvider) ?
                Collections.singleton((EmbeddingProvider)delegate) :
                Collections.<EmbeddingProvider>emptySet();
    }

    public static TaskFactory create(@NonNull final Map<String,Object> params) {
        return new EmbeddingProviderFactory(params);
    }
}
