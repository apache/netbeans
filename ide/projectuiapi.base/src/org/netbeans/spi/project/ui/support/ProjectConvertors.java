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
package org.netbeans.spi.project.ui.support;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Support for {@link ProjectConvertor}s.
 * @author Tomas Zezula
 * @since 1.80
 */
public final class ProjectConvertors {
    private ProjectConvertors() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    /**
     * Checks if the given {@link Project} is a artificial one created by a {@link ProjectConvertor}.
     * @param project the {@link Project} to be tested
     * @return true if the {@link Project} was created by a {@link ProjectConvertor}
     */
    public static boolean isConvertorProject(@NonNull final Project project) {
        return ProjectConvertorFactory.isConvertorProject(project);
    }

    /**
     * Unregisters the artifical convertor {@link Project} from {@link ProjectManager}.
     * Unregisters the artifical convertor {@link Project} from {@link ProjectManager} to
     * allow another {@link Project} to take folder ownership. The method should be called
     * only by {@link Project} generators when creating a new {@link Project}.
     * Requires {@link ProjectManager#mutex()} write access.
     * @param project the project to be unregistered.
     * @since 1.81
     */
    public static void unregisterConvertorProject(@NonNull final Project project) {
        ProjectConvertorFactory.unregisterConvertorProject(project);
    }

    /**
     * Finds the owning non convertor project.
     * Finds nearest enclosing non convertor project.
     * @param file the {@link FileObject} to find owner for
     * @return the owning {@link Project} or null if there is no such a project.
     * @since 1.82
     */
    @CheckForNull
    @SuppressWarnings("NestedAssignment")
    public static Project getNonConvertorOwner(@NonNull final FileObject file) {
        for (FileObject parent = file; parent != null; parent = parent.getParent()) {
            final Project prj = FileOwnerQuery.getOwner(parent);
            if (prj == null || !isConvertorProject(prj)) {
                return prj;
            }
        }
        return null;
    }

    /**
     * Creates {@link FileEncodingQueryImplementation} delegating to the nearest non convertor project.
     * @return the {@link FileEncodingQueryImplementation}
     * @since 1.82
     */
    @NonNull
    public static FileEncodingQueryImplementation createFileEncodingQuery() {
        return new ConvertorFileEncodingQuery();
    }

    /**
     * Creates a {@link Lookup} with given instances.
     * The returned {@link Lookup} implements {@link Closeable}, calling {@link Closeable#close}
     * on it removes all the instances.
     * <p class="nonnormative">
     * Typical usage would be to pass the {@link Lookup} to {@link ProjectConvertor.Result#Result(org.openide.util.Lookup, java.util.concurrent.Callable, java.lang.String, javax.swing.Icon) }
     * and call {@link Closeable#close} on it in the convertor's project factory before the real
     * project is created.
     * </p>
     * @param instances the {@link Lookup} content
     * @return the {@link Lookup} implementing {@link Closeable}
     * @since 1.82
     */
    @NonNull
    public static Lookup createProjectConvertorLookup(@NonNull final Object... instances) {
        return new CloseableLookup(instances);
    }

    /**
     * Creates a {@link Lookup} delegating to the owner project.
     * @param projectDirectory the convertor project directory
     * @return a {@link Lookup} delegating to the owner project
     * @since 1.85
     */
    @NonNull
    public static Lookup createDelegateToOwnerLookup(@NonNull final FileObject projectDirectory) {
        return new OwnerLookup(projectDirectory);
    }

    private static final class ConvertorFileEncodingQuery extends FileEncodingQueryImplementation {

        ConvertorFileEncodingQuery() {}

        @Override
        @CheckForNull
        public Charset getEncoding(@NonNull final FileObject file) {
            final Project p = getNonConvertorOwner(file);
            return p != null ?
                p.getLookup().lookup(FileEncodingQueryImplementation.class).getEncoding(file) :
                null;
        }
    }

    private static final class CloseableLookup extends ProxyLookup implements Closeable {

        CloseableLookup(Object... instances) {
            setLookups(Lookups.fixed(instances));
        }

        @Override
        public void close() throws IOException {
            setLookups(Lookup.EMPTY);
        }
    }

    private static final class OwnerLookup extends Lookup implements Closeable {

        private static final Class[] BLACK_LIST = {
            ProjectOpenedHook.class,
            ProjectInformation.class
        };

        private final FileObject projectDirectory;
        private volatile boolean closed;

        OwnerLookup(@NonNull final FileObject projectDirectory) {
            Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
            this.projectDirectory = projectDirectory;
        }

        @Override
        public <T> T lookup(Class<T> clazz) {
            if (supports(clazz)) {
                final Lookup delegate = findDelegate();
                if (delegate != null) {
                    return delegate.lookup(clazz);
                }
            }
            return null;
        }

        @Override
        public <T> Result<T> lookup(Template<T> template) {
            if (supports(template.getType())) {
                final Lookup delegate = findDelegate();
                if (delegate != null) {
                    return delegate.lookup(template);
                }
            }
            return (Result<T>) EMPTY_RESULT;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        private Lookup findDelegate() {
            if (closed) {
                return null;
            }
            final Project currentOwner = getNonConvertorOwner(projectDirectory);
            return currentOwner == null ?
                    null :
                    currentOwner.getLookup();
        }

        private static boolean supports (@NullAllowed final Class<?> clz) {
            if (clz == null) {
                return false;
            }
            for (Class<?> blackListed : BLACK_LIST) {
                if(blackListed.isAssignableFrom(clz)) {
                    return false;
                }
            }
            return true;
        }

        private static final Result<?> EMPTY_RESULT = new Result<Object>() {
            @Override
            public void addLookupListener(LookupListener l) {
            }

            @Override
            public void removeLookupListener(LookupListener l) {
            }

            @Override
            public Collection<?> allInstances() {
                return Collections.emptySet();
            }
        };
    }

}
