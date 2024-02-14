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

package org.netbeans.test.web.core.syntax;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.gsf.JspLanguage;
import org.netbeans.modules.web.jspparser.JspParserImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Common ancestor for all test classes.
 */
public class TestBase2 extends CslTestBase {


    public TestBase2(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JspLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/x-jsp";
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected BaseKit getEditorKit(String mimeType) {
        return new JspKit(JspKit.JSP_MIME_TYPE);
    }

    public final void initParserJARs() throws MalformedURLException {
        String path = System.getProperty("jsp.parser.jars");
        String[] paths = PropertyUtils.tokenizePath(path);
        List<URL> list = new ArrayList<>();
        for (int i = 0; i< paths.length; i++) {
            String token = paths[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            list.add(f.toURI().toURL());
        }
        JspParserImpl.setParserJARs(list.toArray(new URL[0]));
    }

    public final ClassPath createServletAPIClassPath() throws MalformedURLException, IOException {
        return createClassPath("web.project.jars");
    }
    
    public final ClassPath createClassPath(String property) 
        throws MalformedURLException, IOException 
    {
        String path = System.getProperty("web.project.jars");
        if ( path == null ){
            path = "";
        }
        String[] st = PropertyUtils.tokenizePath(path);
        List<FileObject> fos = new ArrayList<FileObject>();
        for (int i=0; i<st.length; i++) {
            String token = st[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            FileObject fo = FileUtil.toFileObject(f);
            fos.add(FileUtil.getArchiveRoot(fo));
        }
        return ClassPathSupport.createClassPath(fos.toArray(new FileObject[0]));
    }
    
    protected void assertFileContentsMatches(String relFilePath, String newFileName,
            String content ) throws Exception 
    {
        File file = getDataFile(relFilePath);
        if (!file.exists()) {
            NbTestCase.fail("File " + file + " not found.");
        }

        File goldenFile = getDataFile( newFileName );
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(content);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);
        assertEquals(expected.trim(), content.trim());
    }
}
