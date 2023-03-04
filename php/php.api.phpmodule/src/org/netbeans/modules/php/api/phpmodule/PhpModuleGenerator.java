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
package org.netbeans.modules.php.api.phpmodule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Basic generator for PHP module. Implementations
 * can be found in the default lookup.
 * @since 2.28
 * @see CreateProperties
 * @see CreatePropertiesValidator
 */
public interface PhpModuleGenerator {

    /**
     * Create new PHP module (PHP project) for the given properties. These properties
     * are checked and unchecked exception is thrown if they are incorrect.
     * <p>
     * For creating a new PHP module, these properties must be set:
     * <ul>
     * <li>name</li>
     * <li>sources directory</li>
     * <li>PHP version</li>
     * <li>charset</li>
     * </ul>
     * @param properties properties of the new PHP module
     * @return PHP module
     * @throws IOException if any error occurs
     */
    @NonNull
    PhpModule createModule(@NonNull CreateProperties properties) throws IOException;

    //~ Inner classes

    /**
     * Create properties for new PHP module.
     * <p>
     * This class is thread safe.
     * @see CreatePropertiesValidator
     */
    public static final class CreateProperties {

        private volatile String name;
        private volatile File sourcesDirectory;
        private volatile File projectDirectory;
        private volatile PhpVersion phpVersion;
        private volatile Charset charset;
        private volatile boolean autoconfigured;


        /**
         * Create new empty properties.
         */
        public CreateProperties() {
        }

        /**
         * Get PHP module name.
         * @return PHP module name, can be {@code null} for invalid properties
         */
        @CheckForNull
        public String getName() {
            return name;
        }

        /**
         * Set PHP module name.
         * @param name name of PHP module
         * @return self
         */
        public CreateProperties setName(@NonNull String name) {
            Parameters.notNull("name", name); // NOI18N
            this.name = name;
            return this;
        }

        /**
         * Get PHP module sources directory.
         * @return PHP sources directory, can be {@code null} for invalid properties
         */
        @CheckForNull
        public File getSourcesDirectory() {
            return sourcesDirectory;
        }

        /**
         * Set PHP module sources directory.
         * @param sourcesDirectory sources directory of PHP module
         * @return self
         */
        public CreateProperties setSourcesDirectory(@NonNull File sourcesDirectory) {
            Parameters.notNull("sourcesDirectory", sourcesDirectory); // NOI18N
            this.sourcesDirectory = FileUtil.normalizeFile(sourcesDirectory);
            return this;
        }

        /**
         * Get PHP project directory, can be {@code null} if {@link #getSourcesDirectory() sources directory}
         * should be used as project directory as well.
         * @return PHP project directory, can be {@code null} for using sources directory
         */
        @CheckForNull
        public File getProjectDirectory() {
            return projectDirectory;
        }

        /**
         * Set PHP module project directory.
         * @param projectDirectory project directory of PHP module, can be {@code null}
         * @return self
         */
        public CreateProperties setProjectDirectory(@NullAllowed File projectDirectory) {
            if (projectDirectory != null) {
                projectDirectory = FileUtil.normalizeFile(projectDirectory);
            }
            this.projectDirectory = projectDirectory;
            return this;
        }

        /**
         * Get PHP version of PHP module.
         * @return PHP version of PHP module, can be {@code null} for invalid properties
         */
        @CheckForNull
        public PhpVersion getPhpVersion() {
            return phpVersion;
        }

        /**
         * Set PHP version of PHP module.
         * @param phpVersion PHP version of PHP module
         * @return self
         */
        public CreateProperties setPhpVersion(PhpVersion phpVersion) {
            Parameters.notNull("phpVersion", phpVersion); // NOI18N
            this.phpVersion = phpVersion;
            return this;
        }

        /**
         * Get PHP module charset.
         * @return PHP module charset, can be {@code null} for invalid properties
         */
        @CheckForNull
        public Charset getCharset() {
            return charset;
        }

        /**
         * Set PHP module charset.
         * @param charset charset of PHP module
         * @return self
         */
        public CreateProperties setCharset(Charset charset) {
            Parameters.notNull("charset", charset); // NOI18N
            this.charset = charset;
            return this;
        }

        /**
         * Is project autoconfigured? If yes, project will show notification about it once opened
         * for the first time in the IDE.
         * @return {@code true} if project is autoconfigured, {@code false} otherwise
         * @since 2.49
         */
        public boolean isAutoconfigured() {
            return autoconfigured;
        }

        /**
         * Set whether project is autoconfigured or not. If yes, project will show notification
         * about it once opened for the first time in the IDE.
         * @param autoconfigured {@code true} if project is autoconfigured, {@code false} otherwise
         * @return self
         * @since 2.49
         */
        public CreateProperties setAutoconfigured(boolean autoconfigured) {
            this.autoconfigured = autoconfigured;
            return this;
        }

    }

    /**
     * Validator for create properties.
     * @see CreateProperties
     */
    public static final class CreatePropertiesValidator {

        private final ValidationResult result = new ValidationResult();


        /**
         * Create new validator.
         */
        public CreatePropertiesValidator() {
        }

        /**
         * Get validation result.
         * @return validation result
         * @see #validate(org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator.CreateProperties)
         */
        @NonNull
        public ValidationResult getResult() {
            return result;
        }

        /**
         * Validate given create properties.
         * @param properties properties to be validated
         * @return self
         */
        @NbBundle.Messages({
            "CreatePropertiesValidator.error.name=Invalid name.",
            "CreatePropertiesValidator.error.sourcesDirectory=No sources directory.",
            "CreatePropertiesValidator.error.phpVersion=No PHP version.",
            "CreatePropertiesValidator.error.charset=No charset.",
        })
        public CreatePropertiesValidator validate(@NonNull CreateProperties properties) {
            Parameters.notNull("properties", properties); // NOI18N
            if (!StringUtils.hasText(properties.getName())) {
                result.addError(new ValidationResult.Message("name", Bundle.CreatePropertiesValidator_error_name())); // NOI18N
            }
            if (properties.getSourcesDirectory() == null) {
                result.addError(new ValidationResult.Message("sourcesDirectory", Bundle.CreatePropertiesValidator_error_sourcesDirectory())); // NOI18N
            }
            if (properties.getPhpVersion() == null) {
                result.addError(new ValidationResult.Message("phpVersion", Bundle.CreatePropertiesValidator_error_phpVersion())); // NOI18N
            }
            if (properties.getCharset() == null) {
                result.addError(new ValidationResult.Message("charset", Bundle.CreatePropertiesValidator_error_charset())); // NOI18N
            }
            return this;
        }

    }

}
