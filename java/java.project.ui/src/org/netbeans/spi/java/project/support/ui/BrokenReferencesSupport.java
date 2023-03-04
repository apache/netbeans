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

package org.netbeans.spi.java.project.support.ui;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.project.ui.ProjectProblemsProviders;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.Parameters;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.api.project.ui.ProjectProblems;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Support for managing broken project references. Project freshly checkout from
 * VCS can has broken references of several types: reference to other project, 
 * reference to a foreign file, reference to an external source root, reference
 * to a Java Library or reference to a Java Platform. This class has helper
 * methods for detection of these problems and for fixing them.
 * <div class="nonnormative">
 * Typical usage of this class it to check whether the project has some broken
 * references and if it has then providing an action on project's node which
 * allows to correct these configuration problems by showing broken references
 * customizer.
 * </div>
 * @author David Konecny
 * @author Tomas Zezula
 */
public class BrokenReferencesSupport {
        
    private BrokenReferencesSupport() {}

    /**
     * Checks whether the project has some broken references or not.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @return true if some problem was found and it is necessary to give
     *    user a chance to fix them
     *
     * @deprecated Add {@link ProjectProblemsProvider} into project lookup,
     * use {@link BrokenReferencesSupport#createReferenceProblemsProvider} as default
     * implementation, and use {@link ProjectProblems#isBroken}
     */
    @Deprecated
    public static boolean isBroken(AntProjectHelper projectHelper, 
            ReferenceHelper referenceHelper, String[] properties, String[] platformProperties) {
        Parameters.notNull("projectHelper", projectHelper);             //NOI18N
        Parameters.notNull("referenceHelper", referenceHelper);         //NOI18N
        Parameters.notNull("properties", properties);                   //NOI18N
        Parameters.notNull("platformProperties", platformProperties);   //NOI18N
        return ProjectProblems.isBroken(ProjectDecorator.create(
                projectHelper,
                referenceHelper,
                projectHelper.getStandardPropertyEvaluator(),
                properties,
                platformProperties,
                true));
    }
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @see LibraryDefiner
     *
     * @deprecated Add {@link ProjectProblemsProvider} into project lookup,
     * use {@link BrokenReferencesSupport#createReferenceProblemsProvider} as default
     * implementation, and use {@link ProjectProblems#showCustomizer}
     */
    @Deprecated
    public static void showCustomizer(AntProjectHelper projectHelper, 
            ReferenceHelper referenceHelper, String[] properties, String[] platformProperties) {
        ProjectProblems.showCustomizer(ProjectDecorator.create(
                projectHelper,
                referenceHelper,
                projectHelper.getStandardPropertyEvaluator(),
                properties,
                platformProperties,
                false));
    }

    /**
     * Show alert message box informing user that a project has broken
     * references. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     *
     * @deprecated Add {@link ProjectProblemsProvider} into project lookup,
     * use {@link BrokenReferencesSupport#createReferenceProblemsProvider} as default
     * implementation, and use {@link ProjectProblems#showAlert}
     */
    @Deprecated
    public static void showAlert() {
        ProjectProblems.showAlert(ProjectDecorator.create());
    }

    /**
     * Show alert message box informing user that a project has broken
     * references. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box and provides resolve
     * broken references option
     * @param projectHelper the {@link AntProjectHelper} used to resolve broken references
     * @param referenceHelper the {@link ReferenceHelper} used to resolve broken references
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @since 1.37
     *
     * @deprecated Add {@link ProjectProblemsProvider} into project lookup,
     * use {@link BrokenReferencesSupport#createReferenceProblemsProvider} as default
     * implementation, and use {@link ProjectProblems#showAlert}
     */
    @Deprecated
    public static void showAlert(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String[] properties,
            @NonNull final String[] platformProperties) {
        Parameters.notNull("projectHelper", projectHelper);             //NOI18N
        Parameters.notNull("referenceHelper", referenceHelper);         //NOI18N
        Parameters.notNull("evaluator", evaluator);                     //NOI18N
        Parameters.notNull("properties", properties);                   //NOI18N
        Parameters.notNull("platformProperties", platformProperties);   //NOI18N
        ProjectProblems.showAlert(ProjectDecorator.create(
                projectHelper,
                referenceHelper,
                evaluator,
                properties,
                platformProperties,
                false));
    }

    /**
     * Creates a {@link ProjectProblemsProvider} creating broken references
     * problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @return the {@link ProjectProblemsProvider} to be laced into project lookup.
     * @see ProjectProblemsProvider
     * @since 1.48
     */
    @NonNull
    public static ProjectProblemsProvider createReferenceProblemsProvider(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String[] properties,
            @NonNull final String[] platformProperties) {
        return createReferenceProblemsProvider(
                projectHelper,
                referenceHelper,
                evaluator,
                null,
                properties,
                platformProperties
            );
    }

    /**
     * Creates a {@link ProjectProblemsProvider} creating broken references
     * problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param platformUpdatedCallBack called by problem resolution after the platform property has changed
     *    to a new platform. The project type can do project specific changes like updating project.xml file.
     *    The hook is called under {@link ProjectManager#mutex} write access before the project is saved.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @return the {@link ProjectProblemsProvider} to be laced into project lookup.
     * @see ProjectProblemsProvider
     * @since 1.68
     */
    @NonNull
    public static ProjectProblemsProvider createReferenceProblemsProvider(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final PlatformUpdatedCallBack platformUpdatedCallBack,
            @NonNull final String[] properties,
            @NonNull final String[] platformProperties) {
        Parameters.notNull("projectHelper", projectHelper);             //NOI18N
        Parameters.notNull("referenceHelper", referenceHelper);         //NOI18N
        Parameters.notNull("evaluator", evaluator);                     //NOI18N
        Parameters.notNull("properties", properties);                   //NOI18N
        Parameters.notNull("platformProperties", platformProperties);   //NOI18N
        return ProjectProblemsProviders.createReferenceProblemProvider(
                projectHelper,
                referenceHelper,
                evaluator,
                platformUpdatedCallBack,
                properties,
                platformProperties);
    }

    /**
     * Creates a {@link ProjectProblemsProvider} creating wrong Java platform
     * version problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param postPlatformSetHook called by problem resolution after the platform property has changed
     * to a new platform. The project type can do project specific changes like updating project.xml file.
     * The hook is called under {@link ProjectManager#mutex} write access before the project is saved.
     * @param platformType the type of platform, for example j2se
     * @param platformProperty a property holding the active platform id.
     * @param versionProperties array of property names which values hold the source,
     * target level.
     * @return {@link ProjectProblemsProvider} to be laced into project lookup.
     * 
     * @see ProjectProblemsProvider
     * @since 1.48
     */
    @NonNull
    public static ProjectProblemsProvider createPlatformVersionProblemProvider(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final PlatformUpdatedCallBack postPlatformSetHook,
            @NonNull final String platformType,
            @NonNull final String platformProperty,
            @NonNull final String... versionProperties) {
        return createPlatformVersionProblemProvider(
                projectHelper,
                evaluator,
                postPlatformSetHook,
                platformType,
                SourceLevelQuery.MINIMAL_SOURCE_LEVEL,
                platformProperty,
                versionProperties);
    }

    /**
     * Creates a {@link ProjectProblemsProvider} creating wrong Java platform
     * version problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param postPlatformSetHook called by problem resolution after the platform property has changed
     * to a new platform. The project type can do project specific changes like updating project.xml file.
     * The hook is called under {@link ProjectManager#mutex} write access before the project is saved.
     * @param platformType the type of platform, for example j2se
     * @param minimalVersion the minimal source level required by the project
     * @param platformProperty a property holding the active platform id.
     * @param versionProperties array of property names which values hold the source,
     * target level.
     * @return {@link ProjectProblemsProvider} to be laced into project lookup.
     *
     * @see ProjectProblemsProvider
     * @since 1.74
     */
    @NonNull
    public static ProjectProblemsProvider createPlatformVersionProblemProvider(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final PlatformUpdatedCallBack postPlatformSetHook,
            @NonNull final String platformType,
            @NonNull final SpecificationVersion minimalVersion,
            @NonNull final String platformProperty,
            @NonNull final String... versionProperties) {
        Parameters.notNull("projectHelper", projectHelper);             //NOI18N
        Parameters.notNull("evaluator", evaluator);                     //NOI18N
        Parameters.notNull("platformType", platformType);               //NOI18N
        Parameters.notNull("minimalVersion", minimalVersion);           //NOI18N
        Parameters.notNull("platformProperty", platformProperty);       //NOI18N
        Parameters.notNull("versionProperties", versionProperties);     //NOI18N
        return ProjectProblemsProviders.createPlatformVersionProblemProvider(
                projectHelper,
                evaluator,
                postPlatformSetHook,
                platformType,
                minimalVersion,
                platformProperty,
                versionProperties);
    }

    /**
     * Creates a {@link ProjectProblemsProvider} creating wrong JDK 8 Profile
     * problems.
     * @param projectHelper AntProjectHelper associated with the project
     * @param referenceHelper ReferenceHelper associated with the project
     * @param evaluator the {@link PropertyEvaluator} used to resolve broken references
     * @param profileProperty  the property holding the actual project profile
     * @param classPathProperties an array of property names which values hold the
     * classpaths to be checked.
     * @return {@link ProjectProblemsProvider} to be placed into project lookup.
     *
     * @see ProjectProblemsProvider
     * @since 1.53
     */
    @NonNull
    public static ProjectProblemsProvider createProfileProblemProvider(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String profileProperty,
            @NonNull final String... classPathProperties) {
        Parameters.notNull("projectHelper", projectHelper); //NOI18N
        Parameters.notNull("referenceHelper", referenceHelper); //NOI18N
        Parameters.notNull("evaluator", evaluator);     //NOI18N
        Parameters.notNull("profileProperty", profileProperty); //NOI18N
        Parameters.notNull("classPathProperties", classPathProperties); //NOI18N
        final String[] safeClassPathProperties = Arrays.copyOf(
                classPathProperties,
                classPathProperties.length);
        for (String safeClassPathProperty : safeClassPathProperties) {
            Parameters.notNull("classPathProperties[]", safeClassPathProperty); //NOI18N
        }
        return ProjectProblemsProviders.createProfileProblemProvider(
                projectHelper,
                referenceHelper,
                evaluator,
                profileProperty,
                safeClassPathProperties);
    }
        
    /**
     * Service which may be {@linkplain ServiceProvider registered} to download remote libraries or otherwise define them.
     * @since org.netbeans.modules.java.project/1 1.35
     */
    public interface LibraryDefiner {

        /**
         * Checks to see if a missing library definition can be created.
         * @param name a desired {@link Library#getName}
         * @return a callback which may be run (asynchronously) to create and return a library with the given name, or null if not recognized
         */
        @CheckForNull Callable<Library> missingLibrary(String name);

    }

    /**
     * Callback called after the project platform has been updated.
     * The implementor can do project specific changes required by platform change.
     * @since 1.48
     */
    public interface PlatformUpdatedCallBack {
        /**
         * Called by resolution of project problem when platform was changed.
         * @param platform the new platform
         */
        void platformPropertyUpdated(@NonNull final JavaPlatform platform);
    }


    private static final class ProjectDecorator implements Project {

        private final Project delegate;
        private final Lookup lookup;

        private ProjectDecorator(
                @NonNull final Project delegate,
                @NonNull final ProjectProblemsProvider  provider) {
            assert delegate != null;
            this.delegate = delegate;
            this.lookup = new ProxyLookup(delegate.getLookup(),Lookups.singleton(provider));
        }

        private ProjectDecorator() {
            this.delegate = null;
            this.lookup = Lookup.EMPTY;
        }

        @Override
        public FileObject getProjectDirectory() {
            return delegate != null?
                delegate.getProjectDirectory():
                null;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }        

        @NonNull
        static ProjectDecorator create(
            @NonNull final AntProjectHelper projectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String[] properties,
            @NonNull final String[] platformProperties,
            final boolean abortAfterFirstProblem) {
            final Project prj = FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
            return prj != null?
                new ProjectDecorator(
                    prj,
                    ProjectProblemsProviders.createReferenceProblemProvider(
                        projectHelper,
                        referenceHelper,
                        evaluator,
                        null,
                        properties,
                        platformProperties)):
                new ProjectDecorator();
        }

        @NonNull
        static ProjectDecorator create() {
            return new ProjectDecorator();
        }
    }
            
}
