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

package org.netbeans.test.web.core.syntax;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.gsf.JspLanguage;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 */
public class TestBase extends CslTestBase {

    static {
        MockServices.setServices(new Class[] {RepositoryImpl.class});
    }

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
    
    public TestBase(String name) {
        super(name);
    }


//    protected Document createDocument(String text) {
//        try {
//            FileSystem memFS = FileUtil.createMemoryFileSystem();
//            FileObject fo = memFS.getRoot().createData("test", "jsp");
//            assertNotNull(fo);
//            DataObject dobj = DataObject.find(fo);
//            assertNotNull(dobj);
//            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
//            assertNotNull(cookie);
//            Document document = cookie.openDocument();
//            assertEquals(0, document.getLength());
//            document.insertString(0, text, null);
//            return document;
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return null;
//    }

    protected BaseDocument createDocument(String text) {
        try {
            BaseDocument bdoc = createDocument();
            bdoc.insertString(0, text, null);
            return bdoc;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    protected BaseDocument createDocument() {
        NbEditorDocument doc = new NbEditorDocument(JspKit.JSP_MIME_TYPE);
        doc.putProperty(PROP_MIME_TYPE, JspKit.JSP_MIME_TYPE);
        doc.putProperty(Language.class, JspTokenId.language());
        return doc;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JspLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return JspKit.JSP_MIME_TYPE;
    }
    
}
