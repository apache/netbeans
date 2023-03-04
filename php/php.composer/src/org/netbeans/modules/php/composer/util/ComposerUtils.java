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
package org.netbeans.modules.php.composer.util;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.openide.filesystems.FileObject;

public final class ComposerUtils {

    private static final String USAGE_LOGGER_NAME = "org.netbeans.ui.metrics.php.composer"; // NOI18N
    private static final UsageLogger COMPOSER_REQUIRE_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(ComposerUtils.class, "USG_COMPOSER_EDIT") // NOI18N
            .create();
    private static final UsageLogger COMPOSER_LIBRARY_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(ComposerUtils.class, "USG_COMPOSER_LIBRARY") // NOI18N
            .firstMessageOnly(false)
            .create();


    private ComposerUtils() {
        throw new IllegalStateException("no instances allowed");
    }

    public static void logUsageComposerRequire() {
        COMPOSER_REQUIRE_USAGE_LOGGER.log();
    }

    public static void logUsageComposerLibrary(String type, String name, String version) {
        COMPOSER_LIBRARY_USAGE_LOGGER.log(type, name, version);
    }

    /**
     * Gets Composer working directory. Prefers project directory but can return
     * source directory if <tt>composer.json</tt> already exists there.
     * @param phpModule PHP module to be used for detection
     * @return Composer working directory
     */
    @NonNull
    public static FileObject getComposerWorkDir(@NonNull PhpModule phpModule) {
        assert phpModule != null;
        // first project dir
        FileObject projectDirectory = phpModule.getProjectDirectory();
        if (projectDirectory.getFileObject(Composer.COMPOSER_FILENAME) != null) {
            return projectDirectory;
        }
        // now source dir
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory != null
                && sourceDirectory.getFileObject(Composer.COMPOSER_FILENAME) != null) {
            return sourceDirectory;
        }
        return projectDirectory;
    }

}
