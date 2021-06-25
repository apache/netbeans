/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.groovy.editor.language.GroovyFormatter;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * In order to be able to run tests using java.source on Mac, you need to apply patch
 * java-tests-mac.diff in root of groovy.editor module (but DO NOT COMMIT IT!)
 * See issue 97290 for details.
 *
 * @author Martin Adamek
 */
public class GroovyTestBase extends CslTestBase {

    protected FileObject testFO;

    public GroovyTestBase(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    @Override
    protected void setUp() throws Exception {
        // No translation; call before the classpath scanning starts
        GroovyIndex.setClusterUrl("file:/bogus");
        
        super.setUp();
        TestLanguageProvider.register(GroovyTokenId.language());
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("Test.groovy");
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new GroovyLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return GroovyTokenId.GROOVY_MIME_TYPE;
    }

    @Override
    public org.netbeans.modules.csl.api.Formatter getFormatter(IndentPrefs preferences) {
        /* Findbugs-removed: 
        if (preferences == null) {
        preferences = new IndentPrefs(4,4);
        }*/

//        Preferences prefs = NbPreferences.forModule(JsFormatterTest.class);
//        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
//        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
//        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        GroovyFormatter formatter = new GroovyFormatter();//codeStyle, 80);
        
        return formatter;
    }

    protected FileObject getTestFileObject() {
        return testFO;
    }
    
    // Called via reflection from GsfUtilities and AstUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    public BaseDocument createDocument(String s) {
        BaseDocument doc = super.getDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, GroovyTokenId.language());
        doc.putProperty("mimeType", GroovyTokenId.GROOVY_MIME_TYPE);

        return doc;
    }
    
    public BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = new HashMap<String, ClassPath>();
        map.put(ClassPath.BOOT, createBootClassPath());
        map.put(ClassPath.COMPILE, createCompilePath());
        map.put(ClassPath.SOURCE, createSourcePath());

        return map;
    }

    private static ClassPath createBootClassPath() {
        return BootClassPathUtil.getBootClassPath();
    }

    private static ClassPath createCompilePath() {
        URL url = groovy.lang.GroovyObject.class.getProtectionDomain().getCodeSource().getLocation();
        return ClassPathSupport.createClassPath(FileUtil.getArchiveRoot(url));
    }

    private ClassPath createSourcePath() {
        List<FileObject> classPathSources = new ArrayList<FileObject>();
        classPathSources.addAll(additionalSources());

        return ClassPathSupport.createClassPath(classPathSources.toArray(new FileObject[0]));
    }

    private Set<FileObject> additionalSources() {
        Set<FileObject> sourceClassPath = new HashSet<FileObject>();

        for (String sourcePath : additionalSourceClassPath()) {
            sourceClassPath.add(getFO(sourcePath));
        }
        return sourceClassPath;
    }

    /**
     * This method should be override by tests which needs to modify default source ClassPath settings.
     *
     * @return set of <code>String</code> whose need to be added into the ClassPath
     */
    protected Set<String> additionalSourceClassPath() {
        return Collections.emptySet();
    }

    private FileObject getFO(String path) {
        return FileUtil.toFileObject(FileUtil.normalizeFile(getDataFile(path)));
    }

    protected Set<String> newHashSet(String... strings) {
        Set<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(strings));
        
        return set;
    }
}
