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

package org.netbeans.modules.csl.spi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventObject;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.UserQuestionException;

/**
 * Misc utilities to avoid code duplication among the various language plugins
 *
 * @author Tor Norbye
 */
public final class GsfUtilities {
    private static final Logger LOG = Logger.getLogger(GsfUtilities.class.getName());

    private GsfUtilities() { // Utility class only, no instances
    }
    
    /**
     * Determines indentation level at the defined point in the document. The document
     * must provide {@link LineDocument} service otherwise 0 will be returned. On invalid 
     * offset 0 will be returned as well.
     * 
     * @param doc the document.
     * @param offset position in the document.
     * @return indentation level, in characters; 0 in case of location error.
     * @since 2.65
     */
    public static int getLineIndent(Document doc, int offset) {
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return 0;
        }
        try {
            return IndentUtils.lineIndent(doc, LineDocumentUtils.getLineStart(ld, offset));
        } catch (BadLocationException | IndexOutOfBoundsException ex) {
            LOG.log(Level.WARNING, null, ex);
            return 0;
        }
    }

    /**
     * Determines indentation level at the defined point in the document.
     * This obsolete variant uses {@link BaseDocument} which promotes depedency on specialized
     * Editor UI APIs. 
     * 
     * @param doc the document.
     * @param offset position in the document.
     * @return indentation level, in characters.
     * @deprecated Use {@link #getLineIndent(javax.swing.text.Document, int) } instead.
     */
    @Deprecated
    public static int getLineIndent(BaseDocument doc, int offset) {
        return getLineIndent((Document)doc, offset);
    }

    /**
     * Adjust the indentation of the line containing the given offset to the provided
     * indentation, and return the length difference of old and new indentation.
     *
     * Copied from Indent module's "modifyIndent"
     * @deprecated Use {@link #setLineIndentation(javax.swing.text.Document, int, int).
     */
    @Deprecated
    public static int setLineIndentation(BaseDocument doc, int lineOffset, int newIndent) throws BadLocationException {
        return setLineIndentation((Document)doc, lineOffset, newIndent);
    }
    
    /**
     * Adjust the indentation of the line containing the given offset to the provided
     * indentation. Returns the length difference of old and new indentation. The document
     * must support {@link LineDocument} services, otherwise {@code -1} is returned.
     * <p>
     * Copied from Indent module's "modifyIndent"
     * 
     * @param doc the document
     * @param lineOffset character index into the line
     * @param newIndent new indentation level
     * @throws BadLocationException in case of position error
     * @return old indentation, or {@code -1} if the document is not supported.
     * @since 2.65
     */
    public static int setLineIndentation(Document doc, int lineOffset, int newIndent) throws BadLocationException {
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return -1;
        }
        int lineStartOffset;
        
        try {
            lineStartOffset = LineDocumentUtils.getLineStart(ld, lineOffset);
        } catch (IndexOutOfBoundsException ex) {
            throw new BadLocationException(ex.getMessage(), lineOffset);
        }

        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = DocumentUtilities.getText(doc);
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1) {
                    tabSize = IndentUtils.tabSize(doc);
                }
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(doc, newIndent);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        boolean different = false;
        int i = 0;
        for (; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                different = true;
                break;
            }
        }
        if (!different) {
            offset = lineStartOffset + i;
            newIndentString = newIndentString.substring(i);
        }

        // Replace the old indent
        if (offset < oldIndentEndOffset) {
            doc.remove(offset, oldIndentEndOffset - offset);
        }
        if (newIndentString.length() > 0) {
            doc.insertString(offset, newIndentString, null);
        }
        return newIndentString.length() - (oldIndentEndOffset - offset);
    }


    public static JTextComponent getOpenPane() {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();

        return pane;
    }

    public static JTextComponent getPaneFor(FileObject fo) {
        JTextComponent pane = getOpenPane();
        if (pane != null && findFileObject(pane) == fo) {
            return pane;
        }

        for (JTextComponent c : EditorRegistry.componentList()) {
            if (findFileObject(c) == fo) {
                return c;
            }
        }

        return null;
    }

    /**
     * Finds or loads a document for the FileObject. If 'openIfNecessary' is false, returns {@code null}
     * if the document is not opened yet.
     * 
     * @param fileObject file for the document
     * @param openIfNecessary if true, the Document will be loaded into memory, if not open at the moment.
     * @return document instance or {@code null} if not opened and {@code openIfNecessary} was false.
     * @deprecated Use {@link #getADocument(org.openide.filesystems.FileObject, boolean)}.
     */
    @Deprecated
    public static BaseDocument getDocument(FileObject fileObject, boolean openIfNecessary) {
        return getDocument(fileObject, openIfNecessary, false);
    }

    /**
     * Finds or loads a document for the FileObject. If 'openIfNecessary' is false, returns {@code null}
     * if the document is not opened yet.
     * 
     * @param fileObject file for the document
     * @param openIfNecessary if true, the Document will be loaded into memory, if not open at the moment.
     * @return document instance or {@code null} if not opened and {@code openIfNecessary} was false.
     * @since 2.65
     */
    public static Document getADocument(FileObject fileObject, boolean openIfNecessary) {
        return getDocument(fileObject, openIfNecessary, false);
    }

    /**
     * see org.openide.text.DataEditorSupport#BIG_FILE_THRESHOLD_MB
     */
    private static final long BIG_FILE_THRESHOLD_MB = Integer.getInteger("org.openide.text.big.file.size", 5) * 1024 * 1024;

    /**
     * Load the document for the given fileObject.
     * @param fileObject the file whose document we want to obtain
     * @param openIfNecessary If true, block if necessary to open the document. If false, will only return the
     *    document if it is already open.
     * @param skipLarge If true, check the file size, and if the file is really large (defined by
     *    openide.loaders), then skip it (otherwise we could end up with a large file warning).
     * @return
     * @deprecated Use {@link #getADocument(org.openide.filesystems.FileObject, boolean, boolean)}.
     */
    @Deprecated
    public static BaseDocument getDocument(FileObject fileObject, boolean openIfNecessary, boolean skipLarge) {
        if (skipLarge) {
            // Make sure we're not dealing with a huge file!
            // Causes issues like 132306
            // openide.loaders/src/org/openide/text/DataEditorSupport.java
            // has an Env#inputStream method which posts a warning to the user
            // if the file is greater than 1Mb...
            //SG_ObjectIsTooBig=The file {1} seems to be too large ({2,choice,0#{2}b|1024#{3} Kb|1100000#{4} Mb|1100000000#{5} Gb}) to safely open. \n\
            //  Opening the file could cause OutOfMemoryError, which would make the IDE unusable. Do you really want to open it?

            // Apparently there is a way to handle this
            // (see issue http://www.netbeans.org/issues/show_bug.cgi?id=148702 )
            // but for many cases, the user probably doesn't want really large files as indicated
            // by the skipLarge parameter).
            if (fileObject.getSize () > BIG_FILE_THRESHOLD_MB) {
                return null;
            }
        }

        try {
            EditorCookie ec = fileObject.isValid() ? DataLoadersBridge.getDefault().getCookie(fileObject, EditorCookie.class) : null;
            if (ec != null) {
                if (openIfNecessary) {
                    try {
                        return (BaseDocument) ec.openDocument();
                    } catch (UserQuestionException uqe) {
                        uqe.confirmed();
                        return (BaseDocument) ec.openDocument();
                    }
                } else {
                    return (BaseDocument) ec.getDocument();
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

        return null;
    }
    
    /**
     * Load the document for the given fileObject.
     * @param fileObject the file whose document we want to obtain
     * @param openIfNecessary If true, block if necessary to open the document. If false, will only return the
     *    document if it is already open.
     * @param skipLarge If true, check the file size, and if the file is really large (defined by
     *    openide.loaders), then skip it (otherwise we could end up with a large file warning).
     * @return
     * @since 2.65
     */
    public static Document getADocument(FileObject fileObject, boolean openIfNecessary, boolean skipLarge) {
        return getDocument(fileObject, openIfNecessary, skipLarge);
    }
   
    @Deprecated // Use getDocument instead
    public static BaseDocument getBaseDocument(FileObject fileObject, boolean forceOpen) {
        return getDocument(fileObject, forceOpen);
    }

    public static FileObject findFileObject(Document doc) {
        DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

        if (dobj == null) {
            return null;
        }

        return dobj.getPrimaryFile();
    }

    public static FileObject findFileObject(JTextComponent target) {
        Document doc = target.getDocument();
        return findFileObject(doc);
    }

    // Copied from UiUtils. Shouldn't this be in a common library somewhere?
    public static boolean open(final FileObject fo, final int offset, final String search) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public @Override void run() {
                        doOpen(fo, offset, search);
                    }
                });

            return true; // not exactly accurate, but....
        }

        return doOpen(fo, offset, search);
    }

    // Private methods ---------------------------------------------------------
    private static boolean doOpen(FileObject fo, int offset, String search) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);

            // If the caller hasn't specified an offset, and the document is
            // already open, don't jump to a particular line!
            if (ec != null && offset == -1 && ec.getDocument() != null && search == null) {
                ec.open();
                return true;
            }

            // Simple text search if no known offset (e.g. broken/unparseable source)
            if ((search != null) && (offset == -1)) {
                StyledDocument doc = NbDocument.getDocument(od);

                try {
                    String text = doc.getText(0, doc.getLength());
                    int caretDelta = search.indexOf('^');
                    if (caretDelta != -1) {
                        search = search.substring(0, caretDelta) + search.substring(caretDelta+1);
                    } else {
                        caretDelta = 0;
                    }
                    offset = text.indexOf(search);
                    if (offset != -1) {
                        offset += caretDelta;
                    }
                } catch (BadLocationException ble) {
                    LOG.log(Level.WARNING, null, ble);
                }
            }
            
            return NbDocument.openDocument(od, offset, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return false;
    }

    public static void extractZip(final FileObject extract, final FileObject dest) throws IOException {
        File extractFile = FileUtil.toFile(extract);
        extractZip(dest, new BufferedInputStream(new FileInputStream(extractFile)));
    }

    // Based on openide/fs' FileUtil.extractJar
    private static void extractZip(final FileObject fo, final InputStream is)
    throws IOException {
        FileSystem fs = fo.getFileSystem();

        fs.runAtomicAction(
            new FileSystem.AtomicAction() {
                public @Override void run() throws IOException {
                    extractZipImpl(fo, is);
                }
            }
        );
    }

    /** Does the actual extraction of the Jar file.
     */
    // Based on openide/fs' FileUtil.extractJarImpl
    private static void extractZipImpl(FileObject fo, InputStream is)
    throws IOException {
        ZipEntry je;

        ZipInputStream jis = new ZipInputStream(is);

        while ((je = jis.getNextEntry()) != null) {
            String name = je.getName();

            if (name.toLowerCase().startsWith("meta-inf/")) {
                continue; // NOI18N
            }

            if (je.isDirectory()) {
                FileUtil.createFolder(fo, name);

                continue;
            }

            // copy the file
            FileObject fd = FileUtil.createData(fo, name);
            FileLock lock = fd.lock();

            try {
                OutputStream os = fd.getOutputStream(lock);

                try {
                    FileUtil.copy(jis, os);
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }

    /** Return true iff we're editing code templates */
    public static boolean isCodeTemplateEditing(Document doc) {
        // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
        String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
        String CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

        return doc.getProperty(EDITING_TEMPLATE_DOC_PROPERTY) == Boolean.TRUE ||
                doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
    }

    public static boolean isRowWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean isRowEmpty(CharSequence text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length()-1 || text.charAt(offset+1) == '\n')))) {
                    return false;
                }
            }

            if (!(offset == 0 || text.charAt(offset-1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowLastNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length()-1 || text.charAt(i+1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowFirstNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset-1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowStart(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i+1;
                }
            }

            return 0;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowEnd(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i;
                }
            }

            return text.length();
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean endsWith(StringBuilder sb, String s) {
        int len = s.length();

        if (sb.length() < len) {
            return false;
        }

        for (int i = sb.length()-len, j = 0; j < len; i++, j++) {
            if (sb.charAt(i) != s.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    public static String truncate(String s, int length) {
        assert length > 3; // Not for short strings
        if (s.length() <= length) {
            return s;
        } else {
            return s.substring(0, length-3) + "...";
        }
    }

    /**
     * Gets the last known offset of the editor caret.
     *
     * @param snapshot The snapshot to get the offset for.
     * @param event The event that can contain offset information. Can be <code>null</code>.
     *
     * @return The last know caret offset or -1.
     */
    public static int getLastKnownCaretOffset(Snapshot snapshot, EventObject event) {
        // Try scheduler event first
        if (event instanceof CursorMovedSchedulerEvent) {
            return ((CursorMovedSchedulerEvent) event).getCaretOffset();
        }
        
        // defect #221922: getDocument() false forces DObj construction, which I'd like to avoid
        // during parsing. Since != -1 is only returned iff opened Editor component is found, 
        // we can as well search in the opposite direction, starting from opened editors.
        FileObject snapshotFile = snapshot.getSource().getFileObject();
        Document snapshotDoc = null;
        
        if (snapshotFile != null) {
            for(JTextComponent jtc : EditorRegistry.componentList()) {
                if (snapshotFile == NbEditorUtilities.getFileObject(jtc.getDocument())) {
                    
                    // double check: check the document is the same:
                    snapshotDoc = snapshot.getSource().getDocument(false);
                    if (snapshotDoc == null || snapshotDoc == jtc.getDocument()) {
                        Caret c = jtc.getCaret();
                        if (c != null) {
                            return c.getDot();
                        }
                    }
                    break;
                }
            }
        }
        // if the file was NOT null, the document is not opened in any editor (result of the previous search)
        // so in that case we leave snapshotDoc null and avoid creation of DObj.
        if (snapshotDoc == null && snapshotFile == null) {
            snapshotDoc = snapshot.getSource().getDocument(false);
        }
        if (snapshotDoc != null) {
            for(JTextComponent jtc : EditorRegistry.componentList()) {
                if (snapshotDoc == jtc.getDocument()) {
                    Caret c = jtc.getCaret();
                    if (c != null) {
                        return c.getDot();
                    }
                }
            }
        }        

        // Finally, try the enforced caret offset (eg. enforced by tests)
        Integer enforcedCaretOffset = enforcedCaretOffsets.get(snapshot.getSource());
        if (enforcedCaretOffset != null) {
            return enforcedCaretOffset;
        }

        if (event instanceof SourceModificationEvent) {
            return ((SourceModificationEvent)event).getAffectedEndOffset();
        }
        
        return -1;
    }

    /**
     * Gets the CloneableEditorSupport for given FileObject
     *
     * @param fo A FileObject to get the CES for.
     * @return Instance of CloneableEditorSupport
     */
    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fo) {
	try {
	    DataObject dob = DataObject.find(fo);
	    Object obj = dob.getCookie(OpenCookie.class);
	    if (obj instanceof CloneableEditorSupport) {
		return (CloneableEditorSupport)obj;
	    }
	    obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
	    if (obj instanceof CloneableEditorSupport) {
		return (CloneableEditorSupport)obj;
	    }
	} catch (DataObjectNotFoundException ex) {
	    Exceptions.printStackTrace(ex);
	}
        return null;
    }

    // this is called from tests
    /* package */ static void setLastKnowCaretOffset(Source source, int offset) {
        enforcedCaretOffsets.put(source, offset);
    }

    private static final Map<Source, Integer> enforcedCaretOffsets = new WeakHashMap<Source, Integer>();

}
