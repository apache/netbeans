/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.api;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.util.Parameters;

/**
 * Defines an object which listens for changes in CSS preprocessors.
 * @since 1.44
 */
public interface CssPreprocessorsListener extends EventListener {

    /**
     * This method is called when list of CSS preprocessors changes.
     */
    void preprocessorsChanged();

    /**
     * This method is called when options of the given CSS preprocessor changes.
     * @param cssPreprocessor CSS preprocessor which options has changed
     */
    void optionsChanged(@NonNull CssPreprocessor cssPreprocessor);

    /**
     * This method is called when project properties of the given CSS preprocessor changes.
     * @param project project which properties has changed
     * @param cssPreprocessor CSS preprocessor which properties has changed
     */
    void customizerChanged(@NonNull Project project, @NonNull CssPreprocessor cssPreprocessor);

    /**
     * This method is called when processing error occurs.
     * @param project project where processing error occured
     * @param cssPreprocessor CSS preprocessor where processing error occured
     * @param error error message
     * @since 1.45
     */
    void processingErrorOccured(@NonNull Project project, @NonNull CssPreprocessor cssPreprocessor, @NonNull String error);

    /**
     * Support class for working with {@link CssPreprocessorsListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<CssPreprocessorsListener> listeners = new CopyOnWriteArrayList<>();

        /**
         * Add a {@code CssPreprocessorsListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param  listener the {@code CssPreprocessorsListener} to be added, can be {@code null}
         */
        public void addCssPreprocessorListener(@NullAllowed CssPreprocessorsListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@code CssPreprocessorsListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param  listener the {@code CssPreprocessorsListener} to be removed, can be {@code null}
         */
        public void removeCssPreprocessorListener(@NullAllowed CssPreprocessorsListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fire an event in {@link CssPreprocessors#getPreprocessors() list of CSS preprocessors}.
         */
        public void firePreprocessorsChanged() {
            for (CssPreprocessorsListener listener : listeners) {
                listener.preprocessorsChanged();
            }
        }

        /**
         * Fire an event in options of the given CSS preprocessor.
         */
        public void fireOptionsChanged(@NonNull CssPreprocessor cssPreprocessor) {
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            for (CssPreprocessorsListener listener : listeners) {
                listener.optionsChanged(cssPreprocessor);
            }
        }

        /**
         * Fire an event in project properties of the given CSS preprocessor.
         */
        public void fireCustomizerChanged(@NonNull Project project, @NonNull CssPreprocessor cssPreprocessor) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            for (CssPreprocessorsListener listener : listeners) {
                listener.customizerChanged(project, cssPreprocessor);
            }
        }

        /**
         * Fire an event in processing the given project and CSS preprocessor.
         * @since 1.45
         */
        public void fireProcessingErrorOccured(@NonNull Project project, @NonNull CssPreprocessor cssPreprocessor, @NonNull String error) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            Parameters.notNull("error", error); // NOI18N
            for (CssPreprocessorsListener listener : listeners) {
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
