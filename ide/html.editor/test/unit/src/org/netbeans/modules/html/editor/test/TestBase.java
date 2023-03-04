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
package org.netbeans.modules.html.editor.test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author Marek Fukala
 */
public class TestBase extends CslTestBase {

    private static final String EMPTY_STRING = "";

    public TestBase(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        super.setUp();
    }
    
    public FileObject createFile(String relative, String contents) throws IOException {
        File workdir = getWorkDir();
        FileObject fo = FileUtil.toFileObject(workdir);
        assertNotNull(fo);
        
        FileObject datafile = FileUtil.createData(fo, relative);
        assertNotNull(datafile);
        
        OutputStream os = datafile.getOutputStream();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(contents);
        }
        
        return datafile;
    }
    

    protected BaseDocument createDocument() {
        return getDocument(EMPTY_STRING); 
    }

    protected Document[] createDocuments(String... fileName) {
        try {
            List<Document> docs = new ArrayList<>();
            FileSystem memFS = FileUtil.createMemoryFileSystem();
            for (String fName : fileName) {

                //we may also create folders
                StringTokenizer items = new StringTokenizer(fName, "/");
                FileObject fo = memFS.getRoot();
                while(items.hasMoreTokens()) {
                    String item = items.nextToken();
                    if(items.hasMoreTokens()) {
                        //folder
                        fo = fo.createFolder(item);
                    } else {
                        //last, create file
                        fo = fo.createData(item);
                    }
                    assertNotNull(fo);
                }
                
                DataObject dobj = DataObject.find(fo);
                assertNotNull(dobj);

                EditorCookie cookie = dobj.getCookie(EditorCookie.class);
                assertNotNull(cookie);

                Document document = (Document) cookie.openDocument();
                assertEquals(0, document.getLength());

                docs.add(document);

            }
            return docs.toArray(new Document[]{});
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting up tests", ex);
        }
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }
}
