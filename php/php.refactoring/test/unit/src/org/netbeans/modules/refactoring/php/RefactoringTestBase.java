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
package org.netbeans.modules.refactoring.php;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.csl.PHPLanguage;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class RefactoringTestBase extends CslTestBase {

    private static void suppressUselessLogging() {
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            handler.setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    boolean result = true;
                    if (record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.LogContext")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage")) { //NOI18N
                        result = false;
                    }
                    return result;
                }
            });
        }
    }

    public RefactoringTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        suppressUselessLogging();
        super.setUp();
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        TestLanguageProvider.register(HTMLTokenId.language());
    }

    protected String getTestPath() {
        return getTestFolderPath() + "/index.php";
    }

    protected String getTestFolderPath() {
        return "testfiles/" + getTestFolderPathSuffix();
    }

    protected abstract String getTestFolderPathSuffix();

    protected String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new PHPLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return FileUtils.PHP_MIME_TYPE;
    }

}
