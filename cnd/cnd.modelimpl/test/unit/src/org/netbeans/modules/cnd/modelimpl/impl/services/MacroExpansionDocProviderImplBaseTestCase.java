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
