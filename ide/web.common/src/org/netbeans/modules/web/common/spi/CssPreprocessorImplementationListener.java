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
package org.netbeans.modules.web.common.spi;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.util.Parameters;

/**
 * Defines an object which listens for changes in CSS preprocessor.
 * @since 1.44
 */
public interface CssPreprocessorImplementationListener extends EventListener {

    /**
     * This method is called when options of the given CSS preprocessor changes.
     * @param cssPreprocessor CSS preprocessor which options has changed
     */
    void optionsChanged(@NonNull CssPreprocessorImplementation cssPreprocessor);

    /**
     * This method is called when project properties of the given CSS preprocessor changes.
     * @param project project which properties has changed
     * @param cssPreprocessor CSS preprocessor which properties has changed
     */
    void customizerChanged(@NonNull Project project, @NonNull CssPreprocessorImplementation cssPreprocessor);

    /**
     * This method is called when processing error occurs.
     * @param project project where processing error occurred
     * @param cssPreprocessor CSS preprocessor where processing error occurred
     * @param error error message
     * @since 1.45
     */
    void processingErrorOccured(@NonNull Project project, @NonNull CssPreprocessorImplementation cssPreprocessor, @NonNull String error);

    /**
     * Support class for working with {@link CssPreprocessorImplementationListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<CssPreprocessorImplementationListener> listeners = new CopyOnWriteArrayList<>();

        /**
         * Add a {@code CssPreprocessorImplementationListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param  listener the {@code CssPreprocessorImplementationListener} to be added, can be {@code null}
         */
        public void addCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@code CssPreprocessorImplementationListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param  listener the {@code CssPreprocessorImplementationListener} to be removed, can be {@code null}
         */
        public void removeCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fire an event in options of the given CSS preprocessor.
         */
        public void fireOptionsChanged(@NonNull CssPreprocessorImplementation cssPreprocessor) {
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            for (CssPreprocessorImplementationListener listener : listeners) {
                listener.optionsChanged(cssPreprocessor);
            }
        }

        /**
         * Fire an event in project properties of the given CSS preprocessor.
         */
        public void fireCustomizerChanged(@NonNull Project project, @NonNull CssPreprocessorImplementation cssPreprocessor) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            for (CssPreprocessorImplementationListener listener : listeners) {
                listener.customizerChanged(project, cssPreprocessor);
            }
        }

        /**
         * Fire an event in processing the given project and CSS preprocessor.
         * @since 1.45
         */
        public void fireProcessingErrorOccured(@NonNull Project project, @NonNull CssPreprocessorImplementation cssPreprocessor, @NonNull String error) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            Parameters.notNull("error", error); // NOI18N
            for (CssPreprocessorImplementationListener listener : listeners) {
                listener.processingErrorOccured(project, cssPreprocessor, error);
            }
        }

        /**
         * Check if there are any listeners registered to this {@code Support}.
         * @return {@code true} if there are one or more listeners, {@code false} otherwise.
         */
        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
