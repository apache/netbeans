/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
