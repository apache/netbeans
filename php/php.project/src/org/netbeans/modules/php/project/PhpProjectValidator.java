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
package org.netbeans.modules.php.project;

import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;

/**
 * Validator for PHP projects.
 */
public final class PhpProjectValidator {

    private PhpProjectValidator() {
    }

    /**
     * Is the given project fatally broken?
     *
     * Currently, it means that its source directory is not set or is invalid (deleted).
     * @param project project to be checked
     * @return {@code true} if the project is fatally broken
     */
    public static boolean isFatallyBroken(PhpProject project) {
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        return sources == null || !sources.isValid();
    }

    /**
     * Is the given project broken?
     *
     * Currently, it means that the given project has any {@link ProjectProblemsProvider#getProblems() problem}.
     * @param project project to be checked
     * @return {@code true} if the project is broken
     */
    public static boolean isBroken(PhpProject project) {
        return !project.getLookup().lookup(ProjectProblemsProvider.class).getProblems().isEmpty();
    }

}
