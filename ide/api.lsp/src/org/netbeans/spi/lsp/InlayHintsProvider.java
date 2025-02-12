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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lsp.InlayHint;
import org.netbeans.api.lsp.Range;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;

/**
 * A provider for code lens for a given document.
 *
 * @since 1.33
 */
@MimeLocation(subfolderName = "CodeLensProvider")
public interface InlayHintsProvider {

    public Set<String> supportedHintTypes();

    /**
     * Provide {@code CodeLens} for the given document.
     *
     * @param doc the document
     * @return the (future) code lens.
     */
    public CompletableFuture<List<? extends InlayHint>> codeLens(@NonNull Context context);

    public static final class Context {
        private final @NonNull FileObject file;
        private final @NonNull Set<String> requestedHintTypes;
        private final @NullAllowed Range range;

        public Context(@NonNull FileObject file, @NonNull Set<String> requestedHintTypes, @NullAllowed Range range) {
            this.file = file;
            this.requestedHintTypes = requestedHintTypes;
            this.range = range;
        }

        public @NonNull FileObject getFile() {
            return file;
        }

        public Set<String> getRequestedHintTypes() {
            return requestedHintTypes;
        }

        public @CheckForNull Range getRange() {
            return range;
        }

    }
}
