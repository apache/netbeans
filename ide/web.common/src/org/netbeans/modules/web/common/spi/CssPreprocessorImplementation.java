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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates a CSS preprocessor.
 * <p>
 * Instances of this class are {@link org.openide.util.lookup.ServiceProvider registered}
 * in the <code>{@value org.netbeans.modules.web.common.api.CssPreprocessors#PREPROCESSORS_PATH}</code> folder
 * in the module layer.
 * @see CssPreprocessorImplementationListener.Support
 * @since 1.39
 */
public interface CssPreprocessorImplementation {

    /**
     * Return the <b>non-localized (usually English)</b> identifier of this CSS preprocessor.
     * @return the <b>non-localized (usually English)</b> identifier; never {@code null}
     */
    @NonNull
    String getIdentifier();

    /**
     * Return the display name of this CSS preprocessor. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    String getDisplayName();

    /**
     * Process given file (can be a folder as well).
     * <p>
     * For folder, it usually means that all children should be processed.
     * <p>
     * <b>Warning:</b> The given file can be {@link FileObject#isValid() invalid}, it means deleted.
     * <p>
     * It usually means that if the given file can be processed by this CSS preprocessor, some action is done
     * (usually compiling).
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid or even invalid file (or folder) to be processed
     * @param originalName original file name (typically for rename), can be {@code null}
     * @param originalExtension original file extension (typically for rename), can be {@code null}
     * @since 1.52
     */
    void process(@NullAllowed Project project, @NonNull FileObject fileObject, @NullAllowed String originalName, @NullAllowed String originalExtension);

    /**
     * Attach a listener that is to be notified of changes
     * in this CSS preprocessor.
     * @param listener a listener, can be {@code null}
     * @since 1.44
     */
    void addCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener);

    /**
     * Removes a listener.
     * @param listener a listener, can be {@code null}
     * @since 1.44
     */
    void removeCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener);

    //~ Inner classes

}
