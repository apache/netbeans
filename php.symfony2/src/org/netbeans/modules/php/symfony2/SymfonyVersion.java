/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
