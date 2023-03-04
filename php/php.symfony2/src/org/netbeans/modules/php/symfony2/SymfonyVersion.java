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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.symfony2.commands.SymfonyScript;
import org.netbeans.modules.php.symfony2.preferences.SymfonyPreferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


public abstract class SymfonyVersion {

    final PhpModule phpModule;


    SymfonyVersion(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @CheckForNull
    public static SymfonyVersion forPhpModule(PhpModule phpModule) {
        Sf3 sf3 = new Sf3(phpModule);
        FileObject console = sf3.getConsole();
        if (console != null
                && console.isData()) {
            return sf3;
        }
        Sf2 sf2 = new Sf2(phpModule);
        console = sf2.getConsole();
        if (console != null
                && console.isData()) {
            return sf2;
        }
        return null;
    }

    @CheckForNull
    public abstract FileObject getConsole();

    @NonNull
    public abstract String getFrameworkName(boolean shortName);

    @CheckForNull
    public abstract FileObject getTests();

    @CheckForNull
    public abstract File getCacheDir();

    /**
     * @return console file or {@code null} if not valid
     */
    @CheckForNull
    FileObject getConsole(String relativeParentDir) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        FileObject appDir = sourceDirectory.getFileObject(relativeParentDir);
        if (appDir == null) {
            // perhaps deleted app dir? fallback to default and let it fail later...
            return null;
        }
        File appDirFile = FileUtil.toFile(appDir); // #238679
        if (appDirFile != null) {
            File file = new File(appDirFile, SymfonyScript.SCRIPT_NAME);
            return FileUtil.toFileObject(FileUtil.normalizeFile(file));
        }
        return null;
    }

    //~ Inner classes

    private static final class Sf2 extends SymfonyVersion {

        private static final String TEST_DIR_PATH = "src/AppBundle/Tests"; // NOI18N
        private static final String CACHE_DIR_NAME = "cache"; // NOI18N
        private static final String DEFAULT_CACHE_DIR_PATH = "app/" + CACHE_DIR_NAME; // NOI18N


        Sf2(PhpModule phpModule) {
            super(phpModule);
        }

        @NbBundle.Messages({
            "Sf2.name.short=Symfony 2",
            "Sf2.name.long=Symfony 2 PHP Web Framework",
        })
        @Override
        public String getFrameworkName(boolean shortName) {
            if (shortName) {
                    return Bundle.Sf2_name_short();
            }
            return Bundle.Sf2_name_long();
        }

        @CheckForNull
        @Override
        public FileObject getConsole() {
            return getConsole(SymfonyPreferences.getAppDir(phpModule));
        }

        @Override
        public FileObject getTests() {
            return phpModule.getProjectDirectory().getFileObject(TEST_DIR_PATH);
        }

        @Override
        public File getCacheDir() {
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory == null) {
                // broken project
                return null;
            }
            final File defaultCacheDir = new File(FileUtil.toFile(sourceDirectory), DEFAULT_CACHE_DIR_PATH.replace('/', File.separatorChar)); // NOI18N
            FileObject appDir = sourceDirectory.getFileObject(SymfonyPreferences.getAppDir(phpModule));
            if (appDir == null) {
                // not found, simply return the default location
                return defaultCacheDir;
            }
            FileObject cacheFo = appDir.getFileObject(CACHE_DIR_NAME);
            if (cacheFo != null
                    && cacheFo.isFolder()) {
                return FileUtil.toFile(cacheFo);
            }
            return defaultCacheDir;
        }

    }

    private static final class Sf3 extends SymfonyVersion {

        private static final String BIN_DIR_PATH = "bin"; // NOI18N
        private static final String TEST_DIR_PATH = "tests"; // NOI18N
        private static final String CACHE_DIR_PATH = "var/cache"; // NOI18N


        Sf3(PhpModule phpModule) {
            super(phpModule);
        }

        @NbBundle.Messages({
            "Sf3.name.short=Symfony 3",
            "Sf3.name.long=Symfony 3 PHP Web Framework",
        })
        @Override
        public String getFrameworkName(boolean shortName) {
            if (shortName) {
                return Bundle.Sf3_name_short();
            }
            return Bundle.Sf3_name_long();
        }

        @CheckForNull
        @Override
        public FileObject getConsole() {
            return getConsole(BIN_DIR_PATH);
        }

        @Override
        public FileObject getTests() {
            return phpModule.getProjectDirectory().getFileObject(TEST_DIR_PATH);
        }

        @Override
        public File getCacheDir() {
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory == null) {
                // broken project
                return null;
            }
            return new File(FileUtil.toFile(sourceDirectory), CACHE_DIR_PATH.replace('/', File.separatorChar)); // NOI18N
        }

    }

}
