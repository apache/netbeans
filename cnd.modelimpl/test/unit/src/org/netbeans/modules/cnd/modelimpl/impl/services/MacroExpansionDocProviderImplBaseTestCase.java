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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Base class for MacroExpansionDocProviderImpl tests
 *
 */
public class MacroExpansionDocProviderImplBaseTestCase extends TraceModelTestBase {

    public MacroExpansionDocProviderImplBaseTestCase(String testName) {
        super(testName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff
    @Override
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp is preparing project"); // NOI18N
        initParsedProject();
        log("postSetUp finished project preparing"); // NOI18N
        log("Test " + getName() + "started"); // NOI18N
    }

    public static Document createExpandedContextDocument(Document mainDoc, CsmFile csmFile) {
        FileObject fobj = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if (fobj == null) {
            return null;
        }
        Document doc = openFileDocument(fobj);
        if (doc == null) {
            return null;
        }

        doc.putProperty(Document.TitleProperty, mainDoc.getProperty(Document.TitleProperty));
        doc.putProperty(CsmFile.class, csmFile);
        doc.putProperty(FileObject.class, fobj);
        doc.putProperty("beforeSaveRunnable", null); // NOI18N
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT, true);

        mainDoc.putProperty(Document.class, doc);
        doc.putProperty(Document.class, mainDoc);

        setupMimeType(doc);

        return doc;
    }

    public static FileObject createMemoryFile(String name) {
        FileObject fo = null;
        try {
            FileObject root = FileUtil.createMemoryFileSystem().getRoot();
            fo = FileUtil.createData(root, name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fo;
    }

    public static void setupMimeType(Document doc) {
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (mimeType != null) {
            if ("text/plain".equals(mimeType)) { // NOI18N
                doc.putProperty(BaseDocument.MIME_TYPE_PROP, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        }
    }

    public static Document openFileDocument(FileObject fo) {
        Document doc = null;
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            doc = CsmUtilities.openDocument(ec);
            if (doc != null) {
                doc.putProperty(Document.StreamDescriptionProperty, dob);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }

    public int getLine(BaseDocument doc, int offset) {
        try {
            return Utilities.getLineOffset(doc, offset) + 1;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }

    public int getColumn(BaseDocument doc, int offset) {
        return offset - Utilities.getRowStartFromLineOffset(doc, getLine(doc, offset) - 1) + 1;
    }
}
