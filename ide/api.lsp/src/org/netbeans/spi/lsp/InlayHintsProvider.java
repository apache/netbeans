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
import org.openide.util.Parameters;

/**
 * A provider for code lens for a given document.
 *
 * @since 1.34
 */
@MimeLocation(subfolderName = "CodeLensProvider")
public interface InlayHintsProvider {

    /**
     * Return a set of types of hints supported by this provider.
     *
     * The {@code codeLens} method will only be called if at least one
     * of the hint type is enabled in the settings.
     *
     * @return a set of the hint types supported by this provider.
     */
    public Set<String> supportedHintTypes();

    /**
     * Provide {@code CodeLens} for the given document.
     *
     * @param doc the document
     * @return the (future) code lens.
     */
    public CompletableFuture<List<? extends InlayHint>> inlayHints(@NonNull Context context);

    /**
     * Context in which the inlay hints should be evaluated.
     */
    public static final class Context {
        private final @NonNull FileObject file;
        private final @NullAllowed Range range;
        private final @NonNull Set<String> requestedHintTypes;

        /**
         * Create the Context.
         *
         * @param file a file in which the inlay hints should be evaluated
         * @param range the range for which the hints should be computed
         * @param requestedHintTypes the types of hints that should be computed
         */
        public Context(@NonNull FileObject file, @NullAllowed Range range, @NonNull Set<String> requestedHintTypes) {
            Parameters.notNull("file", file);
            Parameters.notNull("range", range);
            Parameters.notNull("requestedHintTypes", requestedHintTypes);

            this.file = file;
            this.range = range;
            this.requestedHintTypes = Set.copyOf(requestedHintTypes);
        }

        /**
         * The file for which the inlay hints should be computed.
         *
         * @return the file for which the inlay hints should be computed
         */
        public @NonNull FileObject getFile() {
            return file;
        }

        /**
         * The range for which the inlay hints should be computed.
         *
         * @return the range for which the inlay hints should be computed
         */
        public @CheckForNull Range getRange() {
            return range;
        }

        /**
         * The types of hints that should be computed.
         *
         * @return the types of hints that should be computed
         */
        public @NonNull Set<String> getRequestedHintTypes() {
            return requestedHintTypes;
        }

    }
}
