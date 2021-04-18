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
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;

/**
 * A provided for errors/warnings for a given file.
 * Should be registered in the {@link MimeLookup}.
 *
 * @since 1.1
 */
public interface ErrorProvider {

    /**
     * Returns a list of {@code ErrorDescription}s for the given {@code Context}.
     *
     * @param context specification of the file, for which the output should be computed
     * @return the errors/warnings of the given type in the given file
     */
    public List<? extends ErrorDescription> computeErrors(Context context);

    /**
     * The context for the error provider.
     */
    public static final class Context {
        private final FileObject file;
        private final Kind errorKind;
        private final AtomicBoolean cancel = new AtomicBoolean();

        /**
         * Construct a new {@code Context}.
         *
         * @param file file for which the errors/warnings should be computed
         * @param errorKind the type of errors/warnings that should be computed
         */
        public Context(FileObject file, Kind errorKind) {
            this.file = file;
            this.errorKind = errorKind;
        }

        /**
         * The file for which the errors/warnings should be computed.
         *
         * @return the file for which the errors/warnings should be computed
         */
        public FileObject file() {
            return file;
        }

        /**
         * The type of errors/warnings should be computed.
         *
         * @return the type of errors/warnings should be computed
         */
        public Kind errorKind() {
            return errorKind;
        }

        /**
         * Checks if the computation should be cancelled.
         *
         * @return true iff the computation should be cancelled
         */
        public boolean isCancelled() {
            return cancel.get();
        }

        /**
         * Cancel the computation.
         */
        public void cancel() {
            cancel.set(true);
        }
    }

    /**
     * The kind of errors/warnings that should be computed. {@code ERRORS} is
     * intended to compute more significant diagnostics quicker, {@code HINTS}
     * is inteded to compute less significant diagnostics at the cost of taking
     * longer. The {@link Severity} of {@link ErrorDescription} reported for neither
     * of these is limited.
     */
    public enum Kind {
        ERRORS,
        HINTS;
    }
}
