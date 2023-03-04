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
package org.netbeans.modules.web.clientproject.spi;

import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;

/**
 * Site template interface. The workflow is:
 * <ol>
 * <li>{@link #prepare() prepare} the template itself (only if {@link #isPrepared() needed})</li>
 * <li>{@link #configure(ProjectProperties) configure} the given project properties</li>
 * <li>{@link #apply(FileObject, ProjectProperties, ProgressHandle)  apply} this template to the given project</li>
 * </ol>
 */
public interface SiteTemplateImplementation {

    /**
     * Non localizable ID of the template; only used for tracking usage statistic.
     */
    @NonNull
    String getId();

    @NonNull
    String getName();

    @NonNull
    String getDescription();

    /**
     * Check whether site template is already {@link #prepare() prepared} (e.g. downloaded from a remote website).
     * @return {@code true} if site template is already {@link #prepare(ProgressHandle) prepared}, {@code false} otherwise
     * @since 1.3
     */
    boolean isPrepared();

    /**
     * Prepare site template, e.g. download it to a cache directory if it is not already downloaded.
     * This method is always called before {@link #configure(ProjectProperties) configuring}
     * and {@link #apply(FileObject, ProjectProperties, ProgressHandle)  applying} this site template
     * but only if the site template is not already {@link #isPrepared() prepared}.
     * <p>
     * For network errors, {@link org.netbeans.modules.web.clientproject.api.network.NetworkException} can be used.
     * This exception is handled via
     * {@link org.netbeans.modules.web.clientproject.api.network.NetworkSupport#showNetworkErrorDialog(String)}.
     * <p>
     * This method is never called in the UI thread.
     * @throws IOException if any error occurs
     * @see org.netbeans.modules.web.clientproject.api.network.NetworkException
     * @since 1.3
     */
    void prepare() throws IOException;

    /**
     * Configure project properties. These properties are later passed to the
     * {@link #apply(FileObject, ProjectProperties, ProgressHandle) apply} method.
     * <p>
     * This method is never called in the UI thread.
     * @param projectProperties current project properties
     */
    void configure(@NonNull ProjectProperties projectProperties);

    /**
     * Apply site template (e.g. copy {@link #prepare() prepared} files) to the given directory (typically project directory
     * or {@link ProjectProperties#getSiteRootFolder() Site Root folder}).
     * <p>
     * This method is never called in the UI thread.
     * @param projectDir project directory
     * @param projectProperties {@link #configure(ProjectProperties) configured} project properties
     * @param handle progress handle, can be {@code null}
     * @throws IOException if any error occurs
     */
    void apply(@NonNull FileObject projectDir, @NonNull ProjectProperties projectProperties, @NullAllowed ProgressHandle handle) throws IOException;

    /**
     * Clean up the possibly {@link #prepare() prepared} site template. Typically, locally cached file is
     * simply deleted.
     * @throws IOException if any error occurs
     * @since 1.54
     */
    void cleanup() throws IOException;

    //~ Inner classes

    /**
     * Project properties. It contains usually relative paths of:
     * <ul>
     * <li>Source folder</li>
     * <li>Site Root folder</li>
     * <li>Unit Tests folder</li>
     * </ul>
     * <p>
     * This class is not thread-safe.
     */
    public final class ProjectProperties {

        private String sourceFolder;
        private String siteRootFolder;
        private String testFolder;
        private String testSeleniumFolder;
        private String jsTestingProvider;
        private String seleniumTestingProvider;


        /**
         * Get Source folder, usually relative path.
         * @return Source folder, usually relative path; can return {@code null} if not used
         * @since 1.66
         */
        @CheckForNull
        public String getSourceFolder() {
            return sourceFolder;
        }

        /**
         * Set Source folder, can be empty string.
         * @param sourceFolder Source folder, can be {@code null} if not used
         * @return itself
         * @since 1.66
         */
        public ProjectProperties setSourceFolder(@NullAllowed String sourceFolder) {
            this.sourceFolder = sourceFolder;
            return this;
        }

        /**
         * Get Site Root folder, usually relative path.
         * @return Site Root folder, usually relative path; can be {@code null} if not used
         */
        @CheckForNull
        public String getSiteRootFolder() {
            return siteRootFolder;
        }

        /**
         * Set Site Root folder, can be empty string.
         * @param siteRootFolder Site Root folder, can be {@code null} if not used
         * @return itself
         */
        public ProjectProperties setSiteRootFolder(@NullAllowed String siteRootFolder) {
            this.siteRootFolder = siteRootFolder;
            return this;
        }

        /**
         * Get Test folder, usually relative path.
         * @return Test folder, usually relative path; can be {@code null} if no Test folder is present
         */
        @CheckForNull
        public String getTestFolder() {
            return testFolder;
        }

        /**
         * Set Test folder, can be {@code null} if there are no tests available.
         * @param testFolder Test folder, can be {@code null} if there are no tests available
         * @return itself
         */
        public ProjectProperties setTestFolder(@NullAllowed String testFolder) {
            this.testFolder = testFolder;
            return this;
        }

        /**
         * Get Test folder for Selenium tests, usually relative path.
         * @return Test folder for Selenium tests, usually relative path; can be {@code null} if no Test folder is present
         * @since 1.94
         */
        @CheckForNull
        public String getTestSeleniumFolder() {
            return testSeleniumFolder;
        }

        /**
         * Set Test folder for Selenium tests, can be {@code null} if there are no tests available.
         * @param testSeleniumFolder Test folder for Selenium tests, can be {@code null} if there are no tests available
         * @return itself
         * @since 1.94
         */
        public ProjectProperties setTestSeleniumFolder(@NullAllowed String testSeleniumFolder) {
            this.testSeleniumFolder = testSeleniumFolder;
            return this;
        }

        /**
         * Get JS testing provider (its identifier).
         * @return JS testing provider (its identifier); can be {@code null} if there is no default JS testing provider
         * @since 1.54
         */
        @CheckForNull
        public String getJsTestingProvider() {
            return jsTestingProvider;
        }

        /**
         * Set JS testing provider (its identifier), can be {@code null} if there is no default JS testing provider.
         * @param jsTestingProvider JS testing provider (its identifier), can be {@code null} if there is no default JS testing provider
         * @return itself
         * @since 1.54
         */
        public ProjectProperties setJsTestingProvider(String jsTestingProvider) {
            this.jsTestingProvider = jsTestingProvider;
            return this;
        }

        /**
         * Get Selenium testing provider (its identifier).
         *
         * @return Selenium testing provider (its identifier); can be
         * {@code null} if there is no default Selenium testing provider
         * @since 1.98
         */
        @CheckForNull
        public String getSeleniumTestingProvider() {
            return seleniumTestingProvider;
    }

        /**
         * Set Selenium testing provider (its identifier), can be {@code null}
         * if there is no default Selenium testing provider.
         *
         * @param seleniumTestingProvider Selenium testing provider (its
         * identifier), can be {@code null} if there is no default Selenium
         * testing provider
         * @return itself
         * @since 1.98
         */
        public ProjectProperties setSeleniumTestingProvider(String seleniumTestingProvider) {
            this.seleniumTestingProvider = seleniumTestingProvider;
            return this;
}

    }

}
