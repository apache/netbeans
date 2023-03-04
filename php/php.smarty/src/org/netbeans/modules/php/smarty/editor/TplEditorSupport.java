
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
package org.netbeans.modules.php.smarty.editor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.php.smarty.ui.SaveConfirmationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.windows.CloneableOpenSupport;
import static org.netbeans.modules.php.smarty.editor.Bundle.*;

/**
 * Editor support for TPL data objects. Most code of this class was get from
 * HtmlEditorSupport - especially encoding support.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class TplEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie {

    private static final String DOCUMENT_SAVE_ENCODING = "Document_Save_Encoding";
    private static final String UTF_8_ENCODING = "UTF-8";

    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset.
     * It also invokes beforeSave() method on the TplDataObject to give it
     * a chance to eg. reflect changes in 'charset' attribute
     * */
    private final SaveCookie saveCookie = new SaveCookie() {

        /** Implements <code>SaveCookie</code> interface. */
        @Override
        public void save() throws IOException {
            try {
                saveDocument();
            } catch (UserCancelException uce) {
                //just ignore
            }
        }
    };

    /** Constructor. */
    TplEditorSupport(TplDataObject obj) {
        super(obj, null, new Environment(obj));
        setMIMEType(obj.getPrimaryFile().getMIMEType());
    }

    @Override
    protected Pane createPane() {
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(TplDataLoader.MIME_TYPE, getDataObject());
    }

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    public void saveDocument() throws IOException {
        updateEncoding();
        super.saveDocument();
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//        TplEditorSupport.this.getDataObject().setModified(false);
    }

    @NbBundle.Messages({
        "warning=Warning",
        "# {0} document name",
        "# {1} encoding",
        "# {2} original encoding of the file when loaded",
        "MSG_unsupportedEncodingSave=<html>The encoding {1} specified in meta tag of the document {0} is invalid<br> or the document contains characters which cannot be saved using this encoding.<br> Do you want to save the file using <b>{2}</b> encoding?</html>"
    })
    void updateEncoding() throws UserCancelException {
        //try to find encoding specification in the editor content
        String documentContent = getDocumentText();
        String encoding = TplDataObject.findEncoding(documentContent);
        String feqEncoding = FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile()).name();
        String finalEncoding = null;
        if (encoding != null) {
            //found encoding specified in the file content by meta tag
            if (!isSupportedEncoding(encoding) || !canEncode(documentContent, encoding)) {
                //test if the file can be saved by the original encoding or if it needs to be saved using utf-8
                finalEncoding = canEncode(documentContent, feqEncoding) ? feqEncoding : UTF_8_ENCODING;
                Integer showEncodingWarnings = getTplDO().getShowEncodingWarnings();
                if (showEncodingWarnings == null) {
                    String message = MSG_unsupportedEncodingSave(getDataObject().getPrimaryFile().getNameExt(), encoding, finalEncoding); 
                    SaveConfirmationPanel panel = new SaveConfirmationPanel(message);
                    DialogDescriptor dd = new DialogDescriptor(panel, Bundle.warning(), true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.YES_OPTION, null);
                    DialogDisplayer.getDefault().notify(dd);
                    showEncodingWarnings = (Integer) dd.getValue();
                    if (panel.isDoNotShowAgainCheckBox() && showEncodingWarnings == NotifyDescriptor.YES_OPTION) {
                        getTplDO().setShowEncodingWarnings(showEncodingWarnings);
                    }
                }
                if (!showEncodingWarnings.equals(NotifyDescriptor.YES_OPTION)) {
                    throw new UserCancelException();
                }
            } else {
                finalEncoding = encoding;
            }
        } else {
            //no encoding specified in the file, use FEQ value
            if (!canEncode(documentContent, feqEncoding)) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(TplEditorSupport.class, "MSG_badCharConversionSave", new Object[]{getDataObject().getPrimaryFile().getNameExt(), feqEncoding}), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if (nd.getValue() != NotifyDescriptor.YES_OPTION) {
                    throw new UserCancelException();
                } else {
                    finalEncoding = UTF_8_ENCODING;
                }
            } else {
                finalEncoding = feqEncoding;
            }
        }

        //FEQ cannot be run in saveFromKitToStream since document is locked for writing,
        //so setting the FEQ result to document property
        Document document = getDocument();
        if (document != null) {
            document.putProperty(DOCUMENT_SAVE_ENCODING, finalEncoding);
        }
    }


    private String getDocumentText() {
        String text = "";
        try {
            StyledDocument doc = getDocument();
            if (doc != null) {
                text = doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
            }
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
        }
        return text;
    }

    private TplDataObject getTplDO() {
        return (TplDataObject) getDataObject();
    }

    private boolean canEncode(String docText, String encoding) {
        CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
        return encoder.canEncode(docText);
    }

    private boolean isSupportedEncoding(String encoding) {
        boolean supported;
        try {
            supported = java.nio.charset.Charset.isSupported(encoding);
        } catch (java.nio.charset.IllegalCharsetNameException e) {
            supported = false;
        }
        return supported;
    }

    @Override
    @NbBundle.Messages({
        "# {0} document name",
        "# {1} encoding",
        "# {2} alternative encoding",
        "MSG_unsupportedEncodingLoad=<html>The encoding {1} specified in meta tag of the document {0} is invalid.<br>Do you want to load the file using <b>{2}</b> encoding?</html>"})
    public void open() {
        String encoding = ((TplDataObject) getDataObject()).getFileEncoding();
        String feqEncoding = FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile()).name();
        if (encoding != null && !isSupportedEncoding(encoding)) {
            Integer showEncodingWarnings = getTplDO().getShowEncodingWarnings();
            if (showEncodingWarnings == null) {
                String message = MSG_unsupportedEncodingLoad(getDataObject().getPrimaryFile().getNameExt(), encoding, feqEncoding);
                SaveConfirmationPanel panel = new SaveConfirmationPanel(message);
                DialogDescriptor dd = new DialogDescriptor(panel, Bundle.warning(), true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.YES_OPTION, null);
                DialogDisplayer.getDefault().notify(dd);
                showEncodingWarnings = (Integer) dd.getValue();
                if (panel.isDoNotShowAgainCheckBox() && showEncodingWarnings == NotifyDescriptor.YES_OPTION) {
                    getTplDO().setShowEncodingWarnings(showEncodingWarnings);
                }
            }
            if (!showEncodingWarnings.equals(NotifyDescriptor.YES_OPTION)) {
                return; // do not open the file
            }
        }

        super.open();
    }

   /**
     * @inheritDoc
     */
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        String foundEncoding = (String) doc.getProperty(DOCUMENT_SAVE_ENCODING);
        String usedEncoding = foundEncoding != null ? foundEncoding : UTF_8_ENCODING;
        final Charset c = Charset.forName(usedEncoding);
        final Writer w = new OutputStreamWriter(stream, c);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }

    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);

        // see TplKit.createDefaultDocument;
        Runnable postInitRunnable = (Runnable) doc.getProperty("postInitRunnable"); //NOI18N
        if (postInitRunnable != null) {
            postInitRunnable.run();
        }

        return doc;
    }

    /**
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified()) {
            return false;
        }
        addSaveCookie();
        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        removeSaveCookie();
    }

    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        TplDataObject obj = (TplDataObject) getDataObject();

        // Adds save cookie to the data object.
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        TplDataObject obj = (TplDataObject) getDataObject();

        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }

    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543158452715818L;

        /** Constructor. */
        public Environment(TplDataObject obj) {
            super(obj);
        }

        /** Implements abstract superclass method. */
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        @Override
        protected FileLock takeLock() throws IOException {
            return ((TplDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        /**
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getCookie(TplEditorSupport.class);
        }
    } // End of nested Environment class.

    /** A method to create a new component. Overridden in subclasses.
     * @return the {@link TplEditor} for this support
     */
    @Override
    protected CloneableEditor createCloneableEditor() {
        return new TplEditor(this);
    }

    public static class TplEditor extends CloneableEditor {

        public TplEditor() {
        }

        /** Creates new editor */
        public TplEditor(TplEditorSupport s) {
            super(s);
            initialize();
        }

        private void initialize() {
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
    }
}
