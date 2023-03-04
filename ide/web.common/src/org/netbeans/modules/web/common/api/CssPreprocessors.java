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
package org.netbeans.modules.web.common.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.cssprep.CssPreprocessorsAccessor;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered CSS preprocessors. The path
 * for registration is "{@value #PREPROCESSORS_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @since 1.37
 */
public final class CssPreprocessors {

    /**
     * Path on SFS for CSS preprocessors registrations.
     */
    public static final String PREPROCESSORS_PATH = "CSS/PreProcessors"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(CssPreprocessors.class.getName(), 2);
    private static final Lookup.Result<CssPreprocessorImplementation> PREPROCESSORS = Lookups.forPath(PREPROCESSORS_PATH).lookupResult(CssPreprocessorImplementation.class);
    private static final CssPreprocessors INSTANCE = new CssPreprocessors();

    private final List<CssPreprocessor> preprocessors = new CopyOnWriteArrayList<>();
    final CssPreprocessorsListener.Support listenersSupport = new CssPreprocessorsListener.Support();
    private final PreprocessorImplementationsListener preprocessorImplementationsListener = new PreprocessorImplementationsListener();


    static {
        PREPROCESSORS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProcessors();
            }
        });
        CssPreprocessorsAccessor.setDefault(new CssPreprocessorsAccessor() {
            @Override
            public List<CssPreprocessor> getPreprocessors() {
                return INSTANCE.getPreprocessors();
            }
        });
    }

    private CssPreprocessors() {
        initProcessors();
    }

    /**
     * Get CssPreprocessors instance.
     * @return CssPreprocessors instance
     * @since 1.40
     */
    public static CssPreprocessors getDefault() {
        return INSTANCE;
    }

    /**
     * Get CSS preprocessor for the given identifier.
     * @param cssPreprocessorIdentifier identifier
     * @return CSS preprocessor for the given identifier or {@code null} if no CSS preprocessor is found
     * @since 1.87
     */
    @CheckForNull
    public CssPreprocessor getCssPreprocessor(String cssPreprocessorIdentifier) {
        Parameters.notNull("cssPreprocessorIdentifier", cssPreprocessorIdentifier); // NOI18N
        for (CssPreprocessor cssPreprocessor : preprocessors) {
            if (cssPreprocessorIdentifier.equals(cssPreprocessor.getIdentifier())) {
                return cssPreprocessor;
            }
        }
        return null;
    }

    List<CssPreprocessor> getPreprocessors() {
        return new ArrayList<>(preprocessors);
    }

    /**
     * Attach a listener that is to be notified of changes
     * in CSS preprocessors.
     * @param listener a listener, can be {@code null}
     * @since 1.44
     */
    public void addCssPreprocessorsListener(@NullAllowed CssPreprocessorsListener listener) {
        listenersSupport.addCssPreprocessorListener(listener);
    }

    /**
     * Removes a change listener.
     * @param listener a listener, can be {@code null}
     * @since 1.44
     */
    public void removeCssPreprocessorsListener(@NullAllowed CssPreprocessorsListener listener) {
        listenersSupport.removeCssPreprocessorListener(listener);
    }

    /**
     * Process given file (can be a folder as well) by {@link #getPreprocessors() all CSS preprocessors}.
     * <b>The project must have {@link org.netbeans.modules.web.common.spi.ProjectWebRootProvider}
     * in its lookup.</b>
     * <p>
     * For detailed information see {@link CssPreprocessorImplementation#process(Project, FileObject, String, String)}.
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid or even invalid file (or folder) to be processed
     * @see #process(Project, FileObject, String, String)
     * @since 1.42
     */
    public void process(@NullAllowed final Project project, @NonNull final FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); // NOI18N
        processInternal(getPreprocessors(), project, fileObject, null, null);
    }

    /**
     * Same as {@link #process(Project, FileObject)} but for rename operation so original name and extension
     * is provided as well.
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid file (or folder) to be processed
     * @param originalName original file name
     * @param originalExtension original file extension
     * @see #process(Project, FileObject)
     * @since 1.52
     */
    public void process(@NullAllowed final Project project, @NonNull final FileObject fileObject,
            @NonNull String originalName, @NonNull String originalExtension) {
        Parameters.notNull("fileObject", fileObject); // NOI18N
        Parameters.notNull("originalName", originalName); // NOI18N
        Parameters.notNull("originalExtension", originalExtension); // NOI18N
        processInternal(getPreprocessors(), project, fileObject, originalName, originalExtension);
    }

    /**
     * Process given file (can be a folder as well) by the given CSS preprocessor.
     * <p>
     * For detailed information see {@link CssPreprocessorImplementation#process(Project, FileObject, String, String)}.
     * @param cssPreprocessor CSS preprocesor
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid or even invalid file (or folder) to be processed
     * @see #process(CssPreprocessor, Project, FileObject, String, String)
     * @see #getCssPreprocessor(String)
     * @since 1.42
     */
    public void process(@NonNull CssPreprocessor cssPreprocessor, @NullAllowed final Project project, @NonNull final FileObject fileObject) {
        Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
        Parameters.notNull("fileObject", fileObject); // NOI18N
        processInternal(Collections.singletonList(cssPreprocessor), project, fileObject, null, null);
    }

    /**
     * Same as {@link #process(CssPreprocessor, Project, FileObject)} but for rename operation so original name and extension
     * is provided as well.
     * @param cssPreprocessor CSS preprocesor
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid file (or folder) to be processed
     * @param originalName original file name
     * @param originalExtension original file extension
     * @see #process(CssPreprocessor, Project, FileObject)
     * @see #getCssPreprocessor(String)
     * @since 1.52
     */
    public void process(@NonNull CssPreprocessor cssPreprocessor, @NullAllowed final Project project, @NonNull final FileObject fileObject,
            @NonNull String originalName, @NonNull String originalExtension) {
        Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
        Parameters.notNull("fileObject", fileObject); // NOI18N
        Parameters.notNull("originalName", originalName); // NOI18N
        Parameters.notNull("originalExtension", originalExtension); // NOI18N
        processInternal(Collections.singletonList(cssPreprocessor), project, fileObject, originalName, originalExtension);
    }

    void processInternal(final List<CssPreprocessor> preprocessors, final Project project, final FileObject fileObject,
            final String originalName, final String originalExtension) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                for (CssPreprocessor cssPreprocessor : preprocessors) {
                    cssPreprocessor.getDelegate().process(project, fileObject, originalName, originalExtension);
                }
            }
        });
    }

    private void initProcessors() {
        assert preprocessors.isEmpty() : "Empty preprocessors expected but: " + preprocessors;
        preprocessors.addAll(map(PREPROCESSORS.allInstances()));
        for (CssPreprocessor cssPreprocessor : preprocessors) {
            cssPreprocessor.getDelegate().addCssPreprocessorListener(preprocessorImplementationsListener);
        }
    }

    void reinitProcessors() {
        synchronized (preprocessors) {
            clearProcessors();
            initProcessors();
        }
        listenersSupport.firePreprocessorsChanged();
    }

    private void clearProcessors() {
        for (CssPreprocessor cssPreprocessor : preprocessors) {
            cssPreprocessor.getDelegate().removeCssPreprocessorListener(preprocessorImplementationsListener);
        }
        preprocessors.clear();
    }

    @CheckForNull
    CssPreprocessor findCssPreprocessor(CssPreprocessorImplementation cssPreprocessorImplementation) {
        assert cssPreprocessorImplementation != null;
        for (CssPreprocessor cssPreprocessor : preprocessors) {
            if (cssPreprocessor.getDelegate() == cssPreprocessorImplementation) {
                return cssPreprocessor;
            }
        }
        assert false : "Cannot find CSS preprocessor for implementation: " + cssPreprocessorImplementation.getIdentifier();
        return null;
    }

    //~ Mappers

    private List<CssPreprocessor> map(Collection<? extends CssPreprocessorImplementation> preprocessors) {
        List<CssPreprocessor> result = new ArrayList<>();
        for (CssPreprocessorImplementation cssPreprocessor : preprocessors) {
            result.add(CssPreprocessorAccessor.getDefault().create(cssPreprocessor));
        }
        return result;
    }

    //~ Inner classes

    private final class PreprocessorImplementationsListener implements CssPreprocessorImplementationListener {

        @Override
        public void optionsChanged(CssPreprocessorImplementation cssPreprocessor) {
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            CssPreprocessor preprocessor = findCssPreprocessor(cssPreprocessor);
            if (preprocessor != null) {
                listenersSupport.fireOptionsChanged(preprocessor);
            }
        }

        @Override
        public void customizerChanged(Project project, CssPreprocessorImplementation cssPreprocessor) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            CssPreprocessor preprocessor = findCssPreprocessor(cssPreprocessor);
            if (preprocessor != null) {
                listenersSupport.fireCustomizerChanged(project, preprocessor);
            }
        }

        @Override
        public void processingErrorOccured(Project project, CssPreprocessorImplementation cssPreprocessor, String error) {
            Parameters.notNull("project", project); // NOI18N
            Parameters.notNull("cssPreprocessor", cssPreprocessor); // NOI18N
            Parameters.notNull("error", error); // NOI18N
            CssPreprocessor preprocessor = findCssPreprocessor(cssPreprocessor);
            if (preprocessor != null) {
                listenersSupport.fireProcessingErrorOccured(project, preprocessor, error);
            }
        }

    }

}
