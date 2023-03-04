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
package org.netbeans.modules.csl.editor.completion;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class CompletionCancelSupportImpl implements CancelSupportImplementation {

    private final AsyncCompletionQuery query;

    private CompletionCancelSupportImpl(@NonNull final AsyncCompletionQuery query) {
        Parameters.notNull("query", query); //NOI18N
        this.query = query;
    }

    @Override
    public boolean isCancelled() {
        return query.isTaskCancelled();
    }

    @NonNull
    static CompletionCancelSupportImpl create(@NonNull AsyncCompletionQuery query) {
        return new CompletionCancelSupportImpl(query);
    }
}
