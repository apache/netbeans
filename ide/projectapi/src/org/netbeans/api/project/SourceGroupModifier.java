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

package org.netbeans.api.project;

import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.SourceGroupRelativeModifierImplementation;

/**
 * <code>SourceGroupModifier</code> provides ways of create specific folders ({@link org.netbeans.api.project.SourceGroup} root folders)
 * in case they don't exist, eg. cannot be retrieved from {@link org.netbeans.api.project.Sources}
 * The project type supporting automated creation of {@link org.netbeans.api.project.SourceGroup} root folders needs to
 * provide {@link org.netbeans.spi.project.SourceGroupModifierImplementation} in the project's lookup.
 *
 * @since org.netbeans.modules.projectapi 1.24
 * @author mkleint
 */
public final class SourceGroupModifier {

    private SourceGroupModifier() {
    }

    /**
     * Creates a {@link org.netbeans.api.project.SourceGroup} in the given {@link org.netbeans.api.project.Project} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     *
     * @param project
     * @param type constant for type of sources
     * @param hint
     * @return the created SourceGroup or null
     */
    public static final SourceGroup createSourceGroup(Project project, String type, String hint) {
        assert project != null;
        SourceGroupModifierImplementation impl = project.getLookup().lookup(SourceGroupModifierImplementation.class);
        if (impl == null) {
            return null;
        }
        return impl.createSourceGroup(type, hint);
    }
    
    /**
     * Creates a source group associated to an existing one. In a project with multiple locations for sources or tests some of those locations
     * can be more appropriate (or completely unrelated) to already existing specific sources. This variant of {@link #createSourceGroup(org.netbeans.api.project.Project, java.lang.String, java.lang.String)}
     * allows to select appropriate locations, if the newly created {@code SourceGroup} should work in association with some existing one.
     * <p/>
     * The source group will be created on location most similar to the provided {@code original} group. If {@code projectParts} are specified, the most matching
     * location will be selected.
     * <p/>
     * This feature is prototypically used in J2SE modular projects, where multiple locations exists for tests and sources, yet they are related by their owning module. Other
     * project types may also partition project sources into logical groups, similar to modules.
     * <p/>
     * Some (java) examples:
     * <ul>
     * <li>to create a source folder in project module, use <code>relativeTo(modulesGroup, "moduleName").createSourceGroup(..)</code>
     * <li>to create a specific module root in project module, use <code>relativeTo(modulesGroup, "moduleName", "path-to-modules").createSourceGroup(...)</code>
     * <li>to create a test folder for a specific source location, use <code>relativeTo(sourceLocation).createSourceGroup(...)</code>
     * <li>or, if there are more test locations to choose, you can use <code>relativeTo(sourceLocation, "test2").createSourceGroup(...)</code>.
     * </ul>
     * @param project the project
     * @param original the original SourceGroup, which the new one should be related to.
     * @param type type of sources
     * @param hint additional type hint
     * @param projectParts optional; abstract location within the project.
     * @return the creaed SourceGroup or {@code null}
     * @since 1.68
     */
    public static final SourceGroup createAssociatedSourceGroup(Project project, SourceGroup original, String type, String hint, String... projectParts) {
        SourceGroupRelativeModifierImplementation relMod = project.getLookup().lookup(SourceGroupRelativeModifierImplementation.class);
        if (relMod == null) {
            return createSourceGroup(project, type, hint);
        } else {
            SourceGroupModifierImplementation impl =  relMod.relativeTo(original, projectParts);
            if (impl == null) {
                return createSourceGroup(project, type, hint);
            } else {
                return impl.createSourceGroup(type, hint);
            }
        }
    }

    /**
     * Creates a {@link org.netbeans.api.project.SourceGroupModifier.Future} object
     * that is capable of lazily creating {@link org.netbeans.api.project.SourceGroup} in the given {@link org.netbeans.api.project.Project} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     * @param project
     * @param type constant for type of sources
     * @param hint
     * @return Future instance that is capable of creating a SourceGroup or null
     */
    public static final SourceGroupModifier.Future createSourceGroupFuture(Project project, String type, String hint) {
        assert project != null;
        SourceGroupModifierImplementation impl = project.getLookup().lookup(SourceGroupModifierImplementation.class);
        if (impl == null) {
            return null;
        }
        if (impl.canCreateSourceGroup(type, hint)) {
            return new Future(impl, type, hint);
        }
        return null;
    }

    /**
     * A wrapper class that is capable of lazily creating a {@link SourceGroup} instance.
     */
    public static final class Future {

        SourceGroupModifierImplementation impl;
        private String hint;
        private String type;

        private Future(SourceGroupModifierImplementation im, String type, String hint) {
            this.impl = im;
            this.hint = hint;
            this.type = type;
        }

        /**
         * Create the instance of {@link SourceGroup} wrapped by
         * this object.
         * @return a source group
         */
        public final SourceGroup createSourceGroup() {
            return impl.createSourceGroup(type, hint);
        }


        /**
         * The type of sources associated with the current instance.
         * @return type constant for type of sources
         */
        public String getType() {
            return type;
        }

        /**
         * The source hint associated with the current instance.
         * @return hint constant for type of sources
         */
        public String getHint() {
            return hint;
        }
    }
}
