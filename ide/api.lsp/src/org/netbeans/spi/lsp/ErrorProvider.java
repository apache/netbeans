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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lsp.Diagnostic;
import org.openide.filesystems.FileObject;

/**
 * A provided for errors/warnings for a given file.
 * Should be registered in the {@link MimeLookup}.
 *
 * @since 1.3
 */
public interface ErrorProvider {

    /**
     * Returns a list of {@code ErrorDescription}s for the given {@code Context}.
     *
     * @param context specification of the file, for which the output should be computed
     * @return the errors/warnings of the given type in the given file
     */
    public List<? extends Diagnostic> computeErrors(Context context);

    /**
     * The context for the error provider.
     */
    public static final class Context {
        private final FileObject file;
        private final int offset;
        private final Kind errorKind;
        private final AtomicBoolean cancel = new AtomicBoolean();
        private final List<Runnable> cancelCallbacks = new ArrayList<>();
        private final FileObject hintsConfigFile;

        /**
         * Construct a new {@code Context}.
         *
         * @param file file for which the errors/warnings should be computed
         * @param errorKind the type of errors/warnings that should be computed
         */
        public Context(FileObject file, Kind errorKind) {
            this(file, -1, errorKind);
        }

        /**
         * Construct a new {@code Context}.
         *
         * @param file file for which the errors/warnings should be computed
         * @param offset offset for which the errors/warnings should be computed
         * @param errorKind the type of errors/warnings that should be computed
         *
         * @since 1.4
         */
        public Context(FileObject file, int offset, Kind errorKind) {
            this(file, offset, errorKind, null);
        }

        /**
         * Construct a new {@code Context}.
         *
         * @param file file for which the errors/warnings should be computed
         * @param offset offset for which the errors/warnings should be computed
         * @param errorKind the type of errors/warnings that should be computed
         * @param hintsConfigFile file which contains preferences for the the errors/warnings to be computed
         *
         * @since 1.25
         * 
         */
        public Context(FileObject file, int offset, Kind errorKind, FileObject hintsConfigFile) {
            this.file = file;
            this.offset = offset;
            this.errorKind = errorKind;
            this.hintsConfigFile = hintsConfigFile;
        }

        /**
         *
         * The file which contains preferences for the the errors/warnings to be computed.
         *
         * @return the file which contains preferences for the the errors/warnings to be computed
         *
         * @since 1.25
         * 
         */
        public FileObject getHintsConfigFile() {
            return hintsConfigFile;
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
         * The offset for which the errors/warnings should be computed.
         *
         * @return the offset for which the errors/warnings should be computed
         *
         * @since 1.4
         */
        public int getOffset() {
            return offset;
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
            List<Runnable> callbacks;
            synchronized (this) {
                callbacks = new ArrayList<>(cancelCallbacks);
            }
            for (Runnable r : callbacks) {
                r.run();
            }
        }
        
        /**
         * Register a {@linkplain Runnable} which will be called when this
         * computation is cancelled.
         *
         * @param r the cancel callback
         */
        public synchronized void registerCancelCallback(Runnable r) {
            cancelCallbacks.add(r);
        }
    }

    /**
     * The kind of errors/warnings that should be computed. {@code ERRORS} is
     * intended to compute more significant diagnostics quicker, {@code HINTS}
     * is inteded to compute less significant diagnostics at the cost of taking
     * longer. The <a href="@org-netbeans-spi-editor-hints@/org/netbeans/spi/editor/hints/Severity.html">Severity</a> 
     * of <a href="@org-netbeans-spi-editor-hints@/org/netbeans/spi/editor/hints/ErrorDescription.html">ErrorDescription</a> reported for neither
     * of these is limited.
     */
    public enum Kind {
        ERRORS,
        HINTS;
    }
}
