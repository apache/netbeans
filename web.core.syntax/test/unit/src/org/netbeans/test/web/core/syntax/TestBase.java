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
