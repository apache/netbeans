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

package org.netbeans.modules.php.symfony;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public class SymfonyPhpModuleIgnoredFilesExtender extends PhpModuleIgnoredFilesExtender {
    private static final String DIR_CACHE = "cache"; // NOI18N
    private static final String DIR_LOG = "log"; // NOI18N

    private final PhpModule phpModule;
    private final File cache;
    private final File log;


    public SymfonyPhpModuleIgnoredFilesExtender(PhpModule phpModule) {
        assert phpModule != null;

        this.phpModule = phpModule;

        File sources = FileUtil.toFile(phpModule.getSourceDirectory());
        FileObject cacheFO = SymfonyPhpFrameworkProvider.locate(phpModule, DIR_CACHE, true);
        if (cacheFO != null && cacheFO.isFolder()) {
            cache = FileUtil.toFile(cacheFO);
        } else {
            // cache not found, simply pretend that it's under sources
            cache = new File(sources, DIR_CACHE);
        }
        log = new File(sources, DIR_LOG);
    }

    @Override
    public Set<File> getIgnoredFiles() {
        Set<File> ignored = new HashSet<>();
        if (SymfonyPhpModuleCustomizerExtender.isCacheDirectoryIgnored(phpModule)) {
            ignored.add(cache);
        }
        ignored.add(log);
        return ignored;
    }
}
