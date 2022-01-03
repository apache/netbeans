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
package org.netbeans.modules.cnd.navigation.macroview;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utility methods for Macro Expansion view.
 *
 */
public final class MacroExpansionViewUtils {

    // Properties for macro expansion document
    /** Start offset of expansion */
    public final static String MACRO_EXPANSION_START_OFFSET = "macro-expansion-start-offset"; // NOI18N
    /** End offset of expansion */
    public final static String MACRO_EXPANSION_END_OFFSET = "macro-expansion-end-offset"; // NOI18N
    // marker for non standard editor components where semantic services are expected to work
    public static final String CND_EDITOR_COMPONENT = "CND_EDITOR_COMPONENT"; // NOI18N

    /**
     * Updates content of macro expansion panel on offset change.
     * 
     * @param newOffset - offset
     * @return true if something was changed during update
     */
    public static void updateView(Document mainDoc, int newOffset, final CsmFile csmFile, final AtomicBoolean canceled, final Runnable syncPositions) {
        if (!MacroExpansionTopComponent.isSyncCaretAndContext()) {
            return;
        }

        final MacroExpansionTopComponent view = MacroExpansionTopComponent.getInstance();
        if (view == null) {
            return;
        }        
        final Document expandedContextDoc = view.getExpandedContextDoc();
        if (expandedContextDoc == null) {
            return;
        }
        if (csmFile == null) {
            return;
        }
        // Get ofsets and check if update needed
        int startOffset = 0;
        int endOffset = mainDoc.getLength();
        boolean localContext = MacroExpansionTopComponent.isLocalContext();
        if (localContext) {
            CsmScope scope = ContextUtils.findInnerFileScope(csmFile, newOffset);
            if (CsmKindUtilities.isOffsetable(scope)) {
                startOffset = ((CsmOffsetable) scope).getStartOffset();
                endOffset = ((CsmOffsetable) scope).getEndOffset();
            }
        }
        if (canceled.get()) {
            return;
        }
        final Document doc = (Document) expandedContextDoc.getProperty(Document.class);
        if (mainDoc.equals(doc)) {
            if (!isOffsetChanged(expandedContextDoc, startOffset, endOffset)) {
                SwingUtilities.invokeLater(syncPositions);
            }
        }
        
        // Init expanded context field
        final Document newExpandedContextDoc = createExpandedContextDocument(mainDoc, csmFile);
        if (newExpandedContextDoc == null) {
            return;
        }
        final int expansionsNumber = CsmMacroExpansion.expand(mainDoc, startOffset, endOffset, newExpandedContextDoc, canceled);
        if (canceled.get()) {
            return;
        }
        setOffset(newExpandedContextDoc, startOffset, endOffset);
        saveDocumentAndMarkAsReadOnly(newExpandedContextDoc);

        // Open view
        Runnable openView = new Runnable() {

            @Override
            public void run() {
                if (!view.isOpened()) {
                    view.open();
                }
                view.setDocuments(newExpandedContextDoc);
                view.setStatusBarText(NbBundle.getMessage(MacroExpansionTopComponent.class, "CTL_MacroExpansionStatusBarLine", expansionsNumber)); // NOI18N
                syncPositions.run();
            }
        };
        SwingUtilities.invokeLater(openView);
    }

    /**
     * Check match between old and new offsets of expansion.
     *
     * @param doc - document of macro expansion view
     * @param startOffset - new start offset
     * @param endOffset - new end offset
     * @return true in offsets have changed
     */
    private static boolean isOffsetChanged(Document doc, int startOffset, int endOffset) {
        int oldStartOffset = 0;
        int oldEndOffset = 0;
        Object obj = doc.getProperty(MACRO_EXPANSION_START_OFFSET);
        if (obj instanceof Integer) {
            oldStartOffset = (Integer) obj;
        }
        obj = doc.getProperty(MACRO_EXPANSION_END_OFFSET);
        if (obj instanceof Integer) {
            oldEndOffset = (Integer) obj;
        }
        return !((oldStartOffset == startOffset) && (oldEndOffset == endOffset));
    }

    /**
     * Sets expansion offsets.
     *
     * @param doc - document
     * @param startOffset - new start offset
     * @param endOffset - new end offset
     */
    public static void setOffset(Document doc, int startOffset, int endOffset) {
        doc.putProperty(MACRO_EXPANSION_START_OFFSET, Integer.valueOf(startOffset));
        doc.putProperty(MACRO_EXPANSION_END_OFFSET, Integer.valueOf(endOffset));
    }

    /**
     * Creates document for expanded context pane.
     *
     * @param mainDoc - original document
     * @param csmFile - file
     * @return document
     */
    public static Document createExpandedContextDocument(Document mainDoc, CsmFile csmFile) {
        FileObject fobj = createMemoryFile(MacroExpansionViewUtils.getDocumentName(mainDoc));
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
        
        // close old if any
        closeMemoryBasedDocument((Document) mainDoc.getProperty(Document.class));

        mainDoc.putProperty(Document.class, doc);
        doc.putProperty(Document.class, mainDoc);
        setupMimeType(doc, mainDoc);

        return doc;
    }

    /**
     * Returns offset in original file.
     *
     * @param doc - document
     * @param documentOffset - offset in document
     * @return - offset in file
     */
    public static int getFileOffset(Document doc, int documentOffset) {
        return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
    }

    /**
     * Returns offset in document.
     *
     * @param doc - document
     * @param fileOffset - offset in original file
     * @return - offset in document
     */
    public static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    /**
     * Finds editor pane of document.
     *
     * @param doc - document
     * @return editor pane
     */
    public static JEditorPane getEditor(Document doc) {
        Object jEditorPane = doc.getProperty(JEditorPane.class);
        if (jEditorPane != null) {
            return (JEditorPane) jEditorPane;
        }
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            return ec == null ? null : CsmUtilities.findRecentEditorPaneInEQ(ec);
        }
        return null;
    }

    /**
     * Sets mime type.
     *
     * @param doc - document
     */
    public static void setupMimeType(Document doc, Document mainDoc) {
        String mimeType = DocumentUtilities.getMimeType(mainDoc);
        if (mimeType != null) {
            if (MIMENames.isHeaderOrCppOrC(mimeType)) {
                doc.putProperty(BaseDocument.MIME_TYPE_PROP, mimeType);
            } else {
                doc.putProperty(BaseDocument.MIME_TYPE_PROP, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        } else {
            doc.putProperty(BaseDocument.MIME_TYPE_PROP, MIMENames.CPLUSPLUS_MIME_TYPE);
        }
    }

    /**
     * Creates file in memory.
     *
     * @param name - file name
     * @return file
     */
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

    /**
     * Opens document for file object.
     *
     * @param fo - file object
     * @return document
     */
    public static Document openFileDocument(FileObject fo) {
        Document doc = null;
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            doc = CsmUtilities.openDocument(ec);
            if (doc != null) {
                doc.putProperty(Document.StreamDescriptionProperty, dob);
            }
        } catch (DataObjectNotFoundException e) {
            //do nothing, memory file already deleted
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }

    public static void closeMemoryBasedDocument(Document doc) {
        if (doc != null && doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
            DataObject dob = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (dob != null) {
                CloseCookie closeCookie = dob.getLookup().lookup(CloseCookie.class);
                if (closeCookie != null) {
                    closeCookie.close();
                }
                FileObject primaryFile = dob.getPrimaryFile();
                if (primaryFile != null && primaryFile.isValid() && !primaryFile.isLocked()) {
                    assert primaryFile.equals(doc.getProperty(FileObject.class));
                    try {
                        primaryFile.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
    /**
     * Saves document.
     *
     * @param doc - document
     */
    public static void saveDocumentAndMarkAsReadOnly(Document doc) {
        FileObject fo = CsmUtilities.getFileObject(doc);
        if (fo != null && fo.isValid()) {
            saveFileAndMarkAsReadOnly(fo);
        }
    }

    /**
     * Saves file.
     *
     * @param fo - file object
     */
    private static void saveFileAndMarkAsReadOnly(FileObject fo) {
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            ec.saveDocument();
            ReadOnlySupport ro = dob.getLookup().lookup(ReadOnlySupport.class);
            if (ro != null) {
                ro.setReadOnly(true);
            }
        } catch (DataObjectNotFoundException e) {
            //do nothing, memory file already deleted
        } catch (FileStateInvalidException e) {
            //do nothing, memory file already deleted
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static String getDocumentName(Document bDoc) {
        DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
        if (dobj != null && dobj.isValid()) {
            FileObject fo = dobj.getPrimaryFile();
            return fo.getNameExt();
        }
        Object title = bDoc.getProperty(Document.TitleProperty);
        if (title instanceof String) {
            return (String)title;
        }
        return "MacroView"; // NOI18N
    }
    /**
     * Locks file.
     *
     * @param fo - file object
     */
    public static void lockFile(FileObject fo) {
        try {
            fo.lock();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
