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
package org.netbeans.modules.web.core.jsploader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.filesystems.FileUtil;
import org.openide.text.DataEditorSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiDataObject;
import org.openide.text.CloneableEditor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.WeakListeners;

class BaseJspEditorSupport extends DataEditorSupport implements EditCookie, EditorCookie.Observable, OpenCookie,
        LineCookie, CloseCookie, PrintCookie {

    private static final Logger LOGGER = Logger.getLogger(BaseJspEditorSupport.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(BaseJspEditorSupport.class.getSimpleName(), 4);
    private static final int AUTO_PARSING_DELAY = 2000;//ms
    private static final String DOCUMENT_SAVE_ENCODING = "Document_Save_Encoding"; //NOI18N
    private Task PARSER_RESTART_TASK;

    /** 
     * When unsupported encoding is set for a jsp file, then defaulEncoding is used for loading
     * and saving
     */
    private static String defaulEncoding = "UTF-8"; // NOI18N
    
    private final DocumentListener DOCUMENT_LISTENER;

    public BaseJspEditorSupport(JspDataObject obj) {
        super(obj, null, new BaseJspEnv(obj));
        DataObject data = getDataObject();
        if (data instanceof JspDataObject) {
            setMIMEType(JspLoader.getMimeType((JspDataObject) data));
        }

        // create document listener
        DOCUMENT_LISTENER = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                change(e);
            }
            public void changedUpdate(DocumentEvent e) {
            }
            public void removeUpdate(DocumentEvent e) {
                change(e);
            }
            private void change(DocumentEvent e) {
                restartParserTask();
                TagLibParseSupport sup = (TagLibParseSupport) getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.setDocumentDirty(true);
                }
            }
        };

        PARSER_RESTART_TASK = RP.create(new Runnable() {
            public void run() {
                final TagLibParseSupport sup = (TagLibParseSupport) getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null && WebModule.getWebModule(getDataObject().getPrimaryFile()) != null) {
                    sup.autoParse(); //parse the file
                }
            }
        });

        RP.post(new Runnable() {

            @Override
            public void run() {
                WebModule webModule = getWebModule(getDataObject().getPrimaryFile());
                if (webModule != null) {
                    FileObject wmRoot = webModule.getDocumentBase();
                    // register class path listener
                    ClassPath cp = ClassPath.getClassPath(wmRoot, ClassPath.EXECUTE);
                    if (cp != null) {
                        cp.addPropertyChangeListener(WeakListeners.propertyChange(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                // the classpath was changed, need to reparsed
                                restartParserTask();
                            }
                        }, cp));
                    }
                }
            }

        });

        

        //EditorCookie.Observable listener
        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_DOCUMENT)) {
                    if (evt.getNewValue() != null) {
                        //document loaded
                        restartParserTask();
                        ((Document)evt.getNewValue()).addDocumentListener(DOCUMENT_LISTENER);
                    } else {
                        //document closed
                        //Issue #172631>>>
                        Object oldDoc = evt.getOldValue();
                        if(oldDoc == null) {
                            Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    "property change fired with both new and old value null!",
                                    new IllegalStateException());
                            return ;
                        }
                        //<<<
                        ((Document)oldDoc).removeDocumentListener(DOCUMENT_LISTENER);
                        //cancel waiting parsing task if there is any
                        //this is largely a workaround for issue #50926
                        TagLibParseSupport sup = (TagLibParseSupport) getDataObject().getCookie(TagLibParseSupport.class);
                        if (sup != null) {
                            sup.cancelParsingTask();
                        }
                    }

                }
            }
        });
    }

    @Override
    protected Pane createPane() {
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(getDataObject().getPrimaryFile().getMIMEType(), getDataObject());
    }
    
    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);

        //#174763 workaround - there isn't any elegant place where to place
        //a code which needs to be run after document's COMPLETE initialization.
        //DataEditorSupport.createStyledDocument() creates the document via the
        //EditorKit.createDefaultDocument(), but some of the important properties
        //like Document.StreamDescriptionProperty or mimetype are set as the
        //document properties later.
        //A hacky solution is that a Runnable can be set to the postInitRunnable property
        //in the EditorKit.createDefaultDocument() and the runnable is run
        //once the document is completely initialized.
        Runnable postInitRunnable = (Runnable)doc.getProperty("postInitRunnable"); //NOI18N
        if(postInitRunnable != null) {
            postInitRunnable.run();
        }

        return doc;
    }

    private WebModule getWebModule(FileObject fo) {
        WebModule wm = WebModule.getWebModule(fo);
        if (wm != null) {
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null && (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo))) {
                return wm;
            }
        }
        return null;
    }

    /** Restart the timer which starts the parser after the specified delay.
     */
    void restartParserTask() {
        PARSER_RESTART_TASK.schedule(AUTO_PARSING_DELAY);
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
    protected boolean asynchronousOpen() {
        return true;
    }
    
    private JspDataObject getJspDataObject() {
        return (JspDataObject)getDataObject();
    }
   
    private final AtomicBoolean encodingVerified = new AtomicBoolean();
    
    @Override
    public void open() {
        super.open();

        //possibly warn the user about incorrect encoding after the file opens
        if(encodingVerified.compareAndSet(false, true)) {
            verifyEncoding();
        }

    }

    @Override
    protected void notifyClosed() {
        //all views of this file have been closed, reset the verification 
        //flag so next time it will popup again if user opens the file
        encodingVerified.set(false);
        
        super.notifyClosed();
    }
    
    
    /**
     * Test if the loaded encoding is supported and warn the user if not.
     */
    private void verifyEncoding() {
        String encoding = getJspDataObject().getFileEncoding();
        if (!isSupportedEncoding(encoding)) {

            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(BaseJspEditorSupport.class, "MSG_BadEncodingDuringLoad", //NOI18N
                    new Object[]{getDataObject().getPrimaryFile().getNameExt(),
                        encoding,
                        defaulEncoding}),
                    NotifyDescriptor.WARNING_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }


    }

    @Override
    protected boolean notifyModified() {
        boolean notify = super.notifyModified();
        if (!notify) {
            return false;
        }
        JspDataObject obj = (JspDataObject) getDataObject();
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.addSaveCookie(new SaveCookie() {

                public void save() throws java.io.IOException {
                    try {
                        saveDocument();
                    } catch (UserCancelException e) {
                        //just ignore
                    }
                }
            });
        }
        return true;
    }

    /** Called when the document becomes unmodified.
     * Here, removing the save cookie from the object and marking it unmodified.
     */
    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        JspDataObject obj = (JspDataObject) getDataObject();
        obj.removeSaveCookie();
    }

    @Override
    public void saveAs(FileObject folder, String fileName) throws IOException {
        checkFileEncoding();
        super.saveAs(folder, fileName);
    }

    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    @Override
    public void saveDocument() throws IOException {
        saveDocument(true, true);
    }

    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @param forceSave if true save always, otherwise only when is modified
     * @exception IOException on I/O error
     */
    private void saveDocument(boolean parse, boolean forceSave) throws IOException {
        if (forceSave || isModified()) {
            checkFileEncoding();
            super.saveDocument();
            if (parse) {
                TagLibParseSupport sup = (TagLibParseSupport) getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.prepare();
                }
            }
        }
    }

    private void checkFileEncoding() throws UserCancelException {
        ((JspDataObject) getDataObject()).updateFileEncoding(true);

        String encoding = ((JspDataObject) getDataObject()).getFileEncoding();
        if (!isSupportedEncoding(encoding)) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(BaseJspEditorSupport.class, "MSG_BadEncodingDuringSave", //NOI18N
                    new Object[]{getDataObject().getPrimaryFile().getNameExt(),
                        encoding,
                        defaulEncoding}),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE);
            nd.setValue(NotifyDescriptor.NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if (nd.getValue() != NotifyDescriptor.YES_OPTION) {
                throw new UserCancelException();
            }
        } else {
            try {
                java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(encoding).newEncoder();
                if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(BaseJspEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                            new Object[]{getDataObject().getPrimaryFile().getNameExt(),
                                encoding}),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                    nd.setValue(NotifyDescriptor.NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    if (nd.getValue() != NotifyDescriptor.YES_OPTION) {
                        throw new UserCancelException();
                    }
                }
            } catch (javax.swing.text.BadLocationException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }

        //FEQ cannot be run in saveFromKitToStream since document is locked for writing,
        //so setting the FEQ result to document property
        Document doc = getDocument();
        //the document is already loaded so getDocument() should normally return
        //no null value, but if a CES redirector returns null from the redirected
        //CES.getDocument() then we are not able to set the found encoding
        if (doc != null) {
            doc.putProperty(DOCUMENT_SAVE_ENCODING, encoding);
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        Parameters.notNull("doc", doc);
        Parameters.notNull("kit", kit);

        String foundEncoding = (String) doc.getProperty(DOCUMENT_SAVE_ENCODING);
        String encoding = foundEncoding != null ? foundEncoding : defaulEncoding;
        Charset charset = StandardCharsets.UTF_8;
        try {
            charset = Charset.forName(encoding);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            LOGGER.log(Level.INFO, "Illegal charset found: {0}, defaulted to UTF-8 as warned by dialog", encoding);
        }
        writeByteOrderMark(charset, stream);
        super.saveFromKitToStream(doc, kit, stream);
    }
    private static final Set<String> UTF_16_CHARSETS = new HashSet<String>();
    private static final Set<String> UTF_32_CHARSETS = new HashSet<String>();

    static {
        Collections.addAll(UTF_16_CHARSETS, "UTF-16", "UTF-16LE", "UTF-16BE");
        Collections.addAll(UTF_32_CHARSETS, "UTF-32", "UTF-32LE", "UTF-32BE");
    }

    /**
     * This method handle byte order mark for charset that do not write it.
     * 
     * @param charset charset (UTF 16 or 32 based)
     * @param os output stream where to write BOM
     * @throws java.io.IOException
     */
    private void writeByteOrderMark(Charset charset, OutputStream os) throws IOException {
        if (!UTF_16_CHARSETS.contains(charset.name()) && !UTF_32_CHARSETS.contains(charset.name())) {
            return;
        }

        /*
         * We need to use writer because encode methods in Charset don't work.
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, charset);
        try {
            writer.write('\uFFFD'); // NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return;
        } finally {
            writer.close();
        }

        byte[] buffer = out.toByteArray();

        if ((UTF_16_CHARSETS.contains(charset.name()) && buffer.length > 2) || (UTF_32_CHARSETS.contains(charset.name()) && buffer.length > 4)) {
            // charset writes BOM
            return;
        }

        if (UTF_16_CHARSETS.contains(charset.name())) {
            if (buffer.length < 2) {
                // don't know what to do
                return;
            }

            if (buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFD) {
                // big endian
                os.write(0xFE);
                os.write(0xFF);
            } else if (buffer[0] == (byte) 0xFD && buffer[1] == (byte) 0xFF) {
                // little endian
                os.write(0xFF);
                os.write(0xFE);
            }
        } else if (UTF_32_CHARSETS.contains(charset.name())) {
            if (buffer.length < 4) {
                // don't know what to do
                return;
            }

            if (buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFD && buffer[2] == (byte) 0x00 && buffer[3] == (byte) 0x00) {
                // big endian
                os.write(0x00);
                os.write(0x00);
                os.write(0xFE);
                os.write(0xFF);
            } else if (buffer[0] == (byte) 0x00 && buffer[1] == (byte) 0x00 && buffer[2] == (byte) 0xFD && buffer[3] == (byte) 0xFF) {
                // little endian
                os.write(0xFF);
                os.write(0xFE);
                os.write(0x00);
                os.write(0x00);
            }
        }
    }

    public static class BaseJspEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = -800036748848958489L;

        public BaseJspEnv(JspDataObject obj) {
            super(obj);
        }

        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (BaseJspEditorSupport) getDataObject().getCookie(BaseJspEditorSupport.class);
        }
    }

    public static class BaseJspEditor extends CloneableEditor {

        public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
        public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
        private TagLibParseSupport taglibParseSupport;
        private InstanceContent instanceContent;

        public BaseJspEditor() {
            super();
        }

        public boolean isXmlSyntax(DataObject dataObject) {

            FileObject fileObject = (dataObject != null) ? dataObject.getPrimaryFile() : null;
            if (fileObject == null) {
                return false;
            }

            return taglibParseSupport.getCachedOpenInfo(false, false).isXmlSyntax();
        }
    }
}
