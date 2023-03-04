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

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**Return annotation processing configuration for given Java file, package or source folder.
 *
 * @author Jan Lahoda
 * @since org.netbeans.api.java/1 1.25
 */
public class AnnotationProcessingQuery {

    /**
     * Returns annotation processing configuration for Java file, Java package or source folder.
     * @param file Java source file, Java package or source folder in question
     * @return annotation processing configuration. Never null.
     */
    public static @NonNull Result getAnnotationProcessingOptions(@NonNull FileObject file) {
        Parameters.notNull("file", file);
        
        for (AnnotationProcessingQueryImplementation i : Lookup.getDefault().lookupAll(AnnotationProcessingQueryImplementation.class)) {
            Result r = i.getAnnotationProcessingOptions(file);

            if (r != null) {
                return r;
            }
        }

        return EMPTY;
    }

    /**Annotation processing configuration. The processor path is returned from
     * {@link ClassPath#getClassPath(org.openide.filesystems.FileObject, java.lang.String)}
     * for {@link JavaClassPathConstants#PROCESSOR_PATH}.
     *
     */
    public static interface Result {

        /**When the annotation processors should be run.
         *
         * @return returns a set of triggers on which the annotation processors should be run
         * @since org.netbeans.api.java/1 1.27
         */
        public @NonNull Set<? extends Trigger> annotationProcessingEnabled();

        /**Which annotation processors should be run.
         *
         * @return if null, run all annotation processors found on the {@link JavaClassPathConstants#PROCESSOR_PATH},
         *         otherwise only the selected processors will be run. The values should be binary names of the
         *         annotation processors.
         */
        public @CheckForNull Iterable<? extends String> annotationProcessorsToRun();

        /**Returns directory to which the annotation processing (during build process) generates sources, if any.
         *
         * @return if not-null, a directory to which the annotation processing generates sources.
         */
        public @CheckForNull URL sourceOutputDirectory();

        /**Returns options passed to annotation processors ({@code "-Akey=value"}). Options are
         * returned in the form of a map from option key to option value. For an option
         * with no value the corresponding value in the map is {@code null}.
         *
         * @return the processor-specific options
         * @see javax.annotation.processing.ProcessingEnvironment#getOptions()
         * @since org.netbeans.api.java/1 1.26
         */
        public @NonNull Map<? extends String, ? extends String> processorOptions();

        /**Add a {@link ChangeListener}.
         *
         * @param l the listener
         */
        public void addChangeListener(@NonNull ChangeListener l);

        /**Remove a {@link ChangeListener}.
         *
         * @param l the listener
         */
        public void removeChangeListener(@NonNull ChangeListener l);
    }

    /** Annotation processing triggers
     */
    public static enum Trigger {

        /** Annotation processors should run on scanning
         */
        ON_SCAN,
        /** Annotation processors should run on every modification of a file in editor
         */
        IN_EDITOR
    }

    private static final Result EMPTY = new Result() {

        public Set<? extends Trigger> annotationProcessingEnabled() {
            return EnumSet.noneOf(Trigger.class);
        }

        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        public URL sourceOutputDirectory() {
            return null;
        }

        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.emptyMap();
        }

        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}

    };

    private AnnotationProcessingQuery() {}
}
