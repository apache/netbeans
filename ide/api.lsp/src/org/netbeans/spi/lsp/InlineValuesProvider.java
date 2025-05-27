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
package org.netbeans.spi.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lsp.InlineValue;
import org.openide.filesystems.FileObject;

/**
 * Compute {@link InlineValue}s for the given file and offset.
 *
 * @since 1.35
 */
public interface InlineValuesProvider {

    /**
     * Compute {@linkplain InlineValue}s for the given file and location.
     *
     * @param file file for which the inline values should be computed
     * @param currentExecutionPosition position for which the inline values should be computed
     * @return the computed inline values
     */
    public CompletableFuture<List<? extends InlineValue>> inlineValues(@NonNull FileObject file, int currentExecutionPosition);
}
