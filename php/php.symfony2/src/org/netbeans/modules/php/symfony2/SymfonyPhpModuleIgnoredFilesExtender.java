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
package org.netbeans.modules.php.symfony2;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.symfony2.preferences.SymfonyPreferences;

/**
 * Ignored files extender.
 */
public class SymfonyPhpModuleIgnoredFilesExtender extends PhpModuleIgnoredFilesExtender {

    private final PhpModule phpModule;
    private final File cache;


    public SymfonyPhpModuleIgnoredFilesExtender(PhpModule phpModule) {
        assert phpModule != null;

        this.phpModule = phpModule;
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        cache = symfonyVersion != null ? symfonyVersion.getCacheDir() : null;
    }

    @Override
    public Set<File> getIgnoredFiles() {
        if (cache == null) {
            // not a real symfony project?
            return Collections.<File>emptySet();
        }
        boolean cacheIgnored = SymfonyPreferences.isCacheDirIgnored(phpModule);
        return cacheIgnored ? Collections.singleton(cache) : Collections.<File>emptySet();
    }

}
