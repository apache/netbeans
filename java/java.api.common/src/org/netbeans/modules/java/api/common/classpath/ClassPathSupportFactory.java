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

package org.netbeans.modules.java.api.common.classpath;

import java.net.URL;
import java.util.function.Function;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Support class for creating different types of classpath related implementations.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathSupportFactory {

    private ClassPathSupportFactory() {
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property.
     * @param evaluator project's property evaluator
     * @return classpath implementation
     */
    public static ClassPathImplementation createBootClassPathImplementation(PropertyEvaluator evaluator) {
        return createBootClassPathImplementation(evaluator, null, null);
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property and given endorsed classpath which will have precedence of platform classpath.
     * @param evaluator project's property evaluator
     * @param endorsedClassPath endorsed classpath to prepend to boot classpath
     * @return classpath implementation
     * @since org.netbeans.modules.java.api.common/0 1.11
     */
    public static ClassPathImplementation createBootClassPathImplementation(PropertyEvaluator evaluator, ClassPath endorsedClassPath) {
        return createBootClassPathImplementation(evaluator, null, endorsedClassPath, null);
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property and given endorsed classpath which will have precedence of platform classpath.
     * @param evaluator project's property evaluator
     * @param endorsedClassPath endorsed classpath to prepend to boot classpath
     * @param platformType the type of {@link JavaPlatform}
     * @return classpath implementation
     * @since 1.59
     */
    public static ClassPathImplementation createBootClassPathImplementation(
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        return createBootClassPathImplementation(evaluator, null, endorsedClassPath, platformType);
    }
    
    public static ClassPathImplementation createBootClassPathImplementation(
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final Project project,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        return new BootClassPathImplementation(project, evaluator, endorsedClassPath, platformType);
    }

    /**
     * Creates implementation of SOURCE classpath for given source roots and project
     * assuming build classes folder is stored in property <code>build.dir</code>.
     *
     * @param sourceRoots project source roots
     * @param projectHelper AntProjectHelper
     * @param evaluator PropertyEvaluator
     * @return classpath implementation
     */
    public static ClassPathImplementation createSourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper, PropertyEvaluator evaluator) {
        return new SourcePathImplementation(sourceRoots, projectHelper, evaluator);
    }

    /**
     * 
     * @param base
     * @param sourceRoots
     * @param systemModules
     * @param userModules
     * @param legacyClassPath
     * @param filter
     * @return 
     */
    public static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath base,
            @NonNull final ClassPath sourceRoots,
            @NonNull final ClassPath systemModules,
            @NonNull final ClassPath userModules,
            @NullAllowed final ClassPath legacyClassPath,
            @NullAllowed final Function<URL,Boolean> filter) {
        return ModuleClassPaths.createModuleInfoBasedPath(base, sourceRoots, systemModules, userModules, legacyClassPath, filter);
    }
}
