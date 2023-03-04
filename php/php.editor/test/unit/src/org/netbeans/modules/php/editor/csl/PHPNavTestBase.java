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

package org.netbeans.modules.php.editor.csl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public abstract class PHPNavTestBase extends PHPTestBase {

    public PHPNavTestBase(String testName) {
        super(testName);
    }

    private static final String FOLDER = "GsfPlugins";

    protected String prepareTestFile(String filePath) throws IOException {
        String retval = TestUtilities.copyFileToString(new File(getDataDir(), filePath));
        return retval;
    }

    protected String prepareTestFile(String filePath, String... texts) throws IOException {
        String retval = prepareTestFile(filePath);
        assert texts != null && texts.length%2 == 0;
        for (int i = 0; i+1 < texts.length; i++) {
            String originalText = texts[i];
            String replacement = texts[++i];
            retval = retval.replace(originalText, replacement);
        }
        return retval;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        FileObject f = FileUtil.getConfigFile(FOLDER + "/text/html");

        if (f != null) {
            f.delete();
        }

        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                Throwable t = record.getThrown();

                if (t == null) {
                    return true;
                }

                for (StackTraceElement e : t.getStackTrace()) {
                    if (   "org.netbeans.modules.php.editor.index.GsfUtilities".equals(e.getClassName())
                        && "getBaseDocument".equals(e.getMethodName())
                        && t instanceof ClassNotFoundException) {
                        return false;
                    }
                }
                return false;
            }
        });
    }

    protected static String computeFileName(int index) {
        return "test" + (index == (-1) ? "" : (char) ('a' + index)) + ".php";
    }

    protected void performTest(String[] code, final UserTask task, boolean waitFinished) throws Exception {
        FileUtil.refreshAll();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject folder = workDir.createFolder("src");
        int index = -1;
        for (String c : code) {
            FileObject f = FileUtil.createData(folder, computeFileName(index));
            TestUtilities.copyStringToFile(f, c);
            index++;
        }
        final FileObject test = folder.getFileObject("test.php");
        Source testSource = getTestSource(test);
        if (waitFinished) {
            Future<Void> parseWhenScanFinished = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            parseWhenScanFinished.get();
        } else {
            ParserManager.parse(Collections.singleton(testSource), task);
        }
    }
    
    protected void performTest(String[] code, final UserTask task) throws Exception {
        performTest(code, task, true);
    }

    private static Document openDocument(FileObject fileObject) throws Exception {
        DataObject dobj = DataObject.find(fileObject);

        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        return ec.openDocument();
    }

    @Override
    protected final Map<String, ClassPath> createClassPathsForTest() {
        FileObject[] srcFolders = createSourceClassPathsForTest();
        return srcFolders != null ? Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(srcFolders)
        ) : null;
    }


    protected FileObject[] createSourceClassPathsForTest() {
        return null;
    }

    protected final FileObject[] createSourceClassPathsForTest(FileObject base, String relativePath) {
        try {
            return new FileObject[]{toFileObject(base, relativePath, true)};
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    protected final FileObject workDirToFileObject() throws IOException {
        FileObject workDir = null;
        assert getWorkDir().exists();
        workDir = FileUtil.toFileObject(getWorkDir());
        return workDir;
    }

    protected final FileObject toFileObject(FileObject base, String relativePath, boolean isFolder) throws IOException {
        FileObject retval = null;
        if (isFolder) {
            retval = FileUtil.createFolder(base, relativePath);
        } else {
            retval = FileUtil.createData(base, relativePath);
        }
        return retval;
    }

}
