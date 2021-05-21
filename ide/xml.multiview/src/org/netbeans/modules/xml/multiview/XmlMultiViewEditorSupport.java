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

package org.netbeans.modules.xml.multiview;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.Task;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.DataEditorSupport;
import org.openide.text.CloneableEditor;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.event.DocumentListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import org.openide.util.Utilities;

/**
 * An implementation of <code>DataEditorSupport</code> that is
 * <code>XmlMultiViewDataObject</code> specific.<p/>
 * 
 * By default the MIME type of this document is set to <code>"text/xml"</code>, 
 * if you need another MIME type, for example for providing a code completion
 * provider registered with that MIME type, you need to set it yourself 
 * using the {@link setMIMEType(String)} method.<p/>
 *
 * Created on October 5, 2004, 10:46 AM
 * @author mkuchtiak
 */
public class XmlMultiViewEditorSupport extends DataEditorSupport implements Serializable, EditCookie, OpenCookie,
        EditorCookie.Observable, PrintCookie {
    
    private XmlMultiViewDataObject dObj;
    private DocumentListener docListener;
    private int xmlMultiViewIndex = 0;
    private TopComponent mvtc;
    private int lastOpenView = 0;
    private TopComponentsListener topComponentsListener;
    private MultiViewDescription[] multiViewDescriptions;
    private XmlMultiViewEditorSupport.DocumentSynchronizer documentSynchronizer;
    private int loading = 0;
    private FileLock saveLock;
    private static final String PROPERTY_MODIFICATION_LISTENER = "modificationListener"; // NOI18N;
    /**
     * Indicates whether xml view should be shown or not.
     */
    private boolean suppressXmlView = false;
    
    public XmlMultiViewEditorSupport() {
        super(null, null);
    }
    
    /** Creates a new instance of XmlMultiviewEditorSupport */
    public XmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
        super(dObj, new XmlEnv(dObj));
        this.dObj = dObj;
        documentSynchronizer = new DocumentSynchronizer(dObj);
        
        // Set a MIME type as needed, e.g.:
        setMIMEType("text/xml");   // NOI18N
        
        docListener = new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                doUpdate();
            }
            
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                doUpdate();
            }
            
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                doUpdate();
            }
            
            private void doUpdate() {
                if (saveLock == null) {
                    documentSynchronizer.requestUpdateData();
                }
            }
        };
        
        // the document listener is added when the document is loaded
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                    Document document = getDocument();
                    if (document != null) {
                        document.addDocumentListener(docListener);
                    }
                }
            }
        });
        
        
    }
    
    /** providing an UndoRedo object for XMLMultiViewElement
     */
    org.openide.awt.UndoRedo getUndoRedo0() {
        return super.getUndoRedo();
    }
    
    public XmlEnv getXmlEnv() {
        return (XmlEnv) env;
    }
    
    /** method enabled to create Cloneable Editor
     */
    protected CloneableEditor createCloneableEditor() {
        return new XmlCloneableEditor(this);
    }
    
    public InputStream getInputStream() throws IOException {
        return super.getInputStream();
    }
    
    protected Task reloadDocument() {
        loading++;
        documentSynchronizer.reloadingStarted();
        FileLock reloadLock;
        try {
            reloadLock = dObj.waitForLock();
            dObj.getDataCache().loadData(dObj.getPrimaryFile(), reloadLock);
        } catch (IOException e) {
            reloadLock = null;
            ErrorManager.getDefault().notify(e);
        }
        final Task reloadDocumentTask = XmlMultiViewEditorSupport.super.reloadDocument();
        final FileLock lock = reloadLock;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    if (!reloadDocumentTask.isFinished()) {
                        reloadDocumentTask.waitFinished(5000);
                    }
                } catch (InterruptedException e) {
                    ErrorManager.getDefault().annotate(e, NbBundle.getMessage(XmlMultiViewEditorSupport.class,
                            "CANNOT_UPDATE_LOCKED_DATA_OBJECT"));
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                    documentSynchronizer.reloadingFinished();
                    loading--;
                }
            }
        });
        return reloadDocumentTask;
    }
    
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
    throws IOException, BadLocationException {
        kit.read(new InputStreamReader(stream, dObj.getEncodingHelper().getEncoding()), doc, 0);
    }
    
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
    throws IOException, BadLocationException {
        kit.write(new OutputStreamWriter(stream, dObj.getEncodingHelper().getEncoding()), doc, 0, doc.getLength());
    }
    
    public StyledDocument openDocument() throws IOException {
        dObj.getDataCache().getStringData();
        return super.openDocument();
    }

    @Override
    protected String messageSave() {
        return super.messageSave();
    }

    public void saveDocument() throws IOException {
        if (loading > 0) {
            return;
        }
        FileLock dataLock = ((XmlMultiViewDataObject) getDataObject()).waitForLock();
        try {
            ((XmlMultiViewDataObject) getDataObject()).getDataCache().saveData(dataLock);
        } finally {
            dataLock.releaseLock();
        }
    }
    
    void saveDocument(FileLock dataLock) throws IOException {
        if (saveLock != dataLock) {
            saveLock = dataLock;
            documentSynchronizer.reloadModel();
            try {
                doSaveDocument();
                dObj.getDataCache().resetFileTime();
            } finally {
                saveLock = null;
            }
        }
    }
    
    private void doSaveDocument() throws IOException {
        // code below is basically a copy-paste from XmlJ2eeEditorSupport
        
        final StyledDocument doc = getDocument();
        // dependency on xml/core
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) enc = "UTF8"; //!!! // NOI18N
        
        try {
            //test encoding on dummy stream
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);
            if (!checkCharsetConversion(enc)) {
                return;
            }
            super.saveDocument();
            //moved from Env.save()
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//            getDataObject().setModified(false);
        } catch (UnsupportedEncodingException ex) {
            // ask user what next?
            String message = NbBundle.getMessage(XmlMultiViewEditorSupport.class,"TEXT_SAVE_AS_UTF", enc);
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message);
            Object res = DialogDisplayer.getDefault().notify(descriptor);
            
            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                
                // update prolog to new valid encoding
                
                try {
                    final int MAX_PROLOG = 1000;
                    int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
                    final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
                    int prologLen = 0;  // actual prolog length
                    
                    //parse prolog and get prolog end
                    if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {
                        
                        // look for delimitting ?>
                        for (int i = 3; i<maxPrologLen; i++) {
                            if (prolog[i] == '?' && prolog[i+1] == '>') {
                                prologLen = i + 1;
                                break;
                            }
                        }
                    }
                    
                    final int passPrologLen = prologLen;
                    
                    Runnable edit = new Runnable() {
                        public void run() {
                            try {
                                
                                doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                                doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N
                                
                            } catch (BadLocationException e) {
                                if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                    e.printStackTrace();
                            }
                        }
                    };
                    
                    NbDocument.runAtomic(doc, edit);
                    
                    super.saveDocument();
                    //moved from Env.save()
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//                    getDataObject().setModified(false);
                    // need to force reloading
                    ((XmlMultiViewDataObject) getDataObject()).getDataCache().reloadData();
                    
                    
                } catch (BadLocationException lex) {
                    ErrorManager.getDefault().notify(lex);
                }
                
            } else { // NotifyDescriptor != YES_OPTION
                return;
            }
        }
    }
    
    private boolean checkCharsetConversion(final String encoding) {
        boolean value = true;
        try {
            CharsetEncoder coder = Charset.forName(encoding).newEncoder();
            if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))){
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(XmlMultiViewEditorSupport.class, "MSG_BadCharConversion",
                        new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                        encoding}),
                        NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if(nd.getValue() != NotifyDescriptor.YES_OPTION) {
                    value = false;
                }
            }
        } catch (BadLocationException e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return value;
    }
    
    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        if (dObj.getEditorMimeType() != null) {
            CloneableTopComponent mvtc = MultiViews.createCloneableMultiView(dObj.getEditorMimeType(), dObj);
            this.mvtc = mvtc;
            return mvtc;
        }

        MultiViewDescription[] descs = getMultiViewDescriptions();
        
        CloneableTopComponent mvtc =
                MultiViewFactory.createCloneableMultiView(descs, descs[0], new MyCloseHandler(dObj));
        
        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(org.openide.text.CloneableEditorSupport.EDITOR_MODE);
        
        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        this.mvtc = mvtc;
        return mvtc;
    }
    
    public MultiViewDescription[] getMultiViewDescriptions() {
        if (multiViewDescriptions == null) {
            if (suppressXmlView) {
                multiViewDescriptions = dObj.getMultiViewDesc();
                xmlMultiViewIndex = 0;
            } else {
                MultiViewDescription[] customDesc = dObj.getMultiViewDesc();
                MultiViewDescription xmlDesc = new XmlViewDesc(dObj);
                
                multiViewDescriptions = new MultiViewDescription[customDesc.length + 1];
                System.arraycopy(customDesc, 0, multiViewDescriptions, 1, customDesc.length);
                multiViewDescriptions[0] = xmlDesc;
                xmlMultiViewIndex = dObj.getXMLMultiViewIndex();
            }
        }
        return multiViewDescriptions;
    }
    
    public void setSuppressXmlView(boolean suppressXmlView) {
        this.suppressXmlView = suppressXmlView;
        multiViewDescriptions = null;
    }
    
    /** Focuses existing component to view, or if none exists creates new.
     * The default implementation simply calls {@link #open}.
     * @see org.openide.cookies.EditCookie#edit
     */
    public void edit() {
        openView(-1);
    }
    
    /**
     * Opens the view identified by given <code>index</code>
     * and calls <code>#openDocument()</code>.
     * @param index the index of the view to be opened.
     */
    void openView(final int index) {
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                CloneableTopComponent mvtc = openCloneableTopComponent();
                MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
                handler.requestVisible(handler.getPerspectives()[index < 0 ? xmlMultiViewIndex : index]);
                mvtc.requestActive();
            }
        });
        try {
            openDocument();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /** Overrides superclass method
     */
    public void open() {
        openView(lastOpenView);
    }

    @Override
    protected boolean asynchronousOpen() {
         return true;
    }
    
    void goToXmlPerspective() {
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
                // #234365 - the window may get closed in the meantime.
                if (handler == null) {
                    return;
                }
                handler.requestVisible(handler.getPerspectives()[xmlMultiViewIndex]);
            }
        });
    }
    /** Resolving problems when editor was modified and closed
     * (issue 57483)
     */
    protected void notifyClosed() {
        mvtc = null;
        if (topComponentsListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(topComponentsListener);
            topComponentsListener = null;
        }
        Document document = getDocument();
        if (document!=null) document.removeDocumentListener(docListener);
        super.notifyClosed();
    }
    
    org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        if (mvtc != null) {
            return MultiViews.findMultiViewHandler(mvtc).getSelectedPerspective();
        }
        return null;
    }
    
    /**
     * Updates the display name of the associated top component.
     */
    public void updateDisplayName() {
        if (mvtc != null) {
            Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    String displayName = messageName();
                    if (!displayName.equals(mvtc.getDisplayName())) {
                        mvtc.setDisplayName(displayName);
                    }
                    String htmlDisplayName = messageHtmlName();
                    if (!Utilities.compareObjects(htmlDisplayName, mvtc.getHtmlDisplayName())) {
                        mvtc.setHtmlDisplayName(htmlDisplayName);
                    }
                    mvtc.setToolTipText(DataEditorSupport.toolTip(
                            dObj.getPrimaryFile(), getDataObject().isModified(), !getDataObject().getPrimaryFile().canWrite()));
                }
            });
        }
    }


    void onCloseSave() throws IOException {
        dObj.getEditorSupport().saveDocument();
    }

    void onCloseDiscard() {
        dObj.getEditorSupport().reloadDocument().waitFinished();
        dObj.getEditorSupport().notifyClosed();
    }
    
    
    /** A description of the binding between the editor support and the object.
     * Note this may be serialized as part of the window system and so
     * should be static, and use the transient modifier where needed.
     */
    public static class XmlEnv extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = 2882981960507292985L;   //todo calculate a new one
        private final XmlMultiViewDataObject xmlMultiViewDataObject;
        
        /** Create a new environment based on the buffer object.
         * @param obj the buffer object to edit
         */
        public XmlEnv(XmlMultiViewDataObject obj) {
            super(obj);
            xmlMultiViewDataObject = obj;
            changeFile();
        }
        
        /** Get the file to edit.
         * @return the primary file normally
         */
        protected FileObject getFile() {
            return xmlMultiViewDataObject.getPrimaryFile();
        }
        
        /** Lock the file to edit.
         * Should be taken from the file entry if possible, helpful during
         * e.g. deletion of the file.
         * @return a lock on the primary file normally
         * @throws IOException if the lock could not be taken
         */
        protected FileLock takeLock() throws IOException {
            return xmlMultiViewDataObject.getPrimaryEntry().takeLock();
        }
        
        
        /** Find the editor support this environment represents.
         * Note that we have to look it up, as keeping a direct
         * reference would not permit this environment to be serialized.
         * @return the editor support
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return xmlMultiViewDataObject.getEditorSupport();
        }
        
        public InputStream inputStream() throws IOException {
            return xmlMultiViewDataObject.getDataCache().createInputStream();
        }
        
        protected OutputStream getFileOutputStream() throws IOException {
            return super.outputStream();
        }
        
        public OutputStream outputStream() throws IOException {
            if (xmlMultiViewDataObject.getEditorSupport().saveLock != null) {
                return super.outputStream();
            } else {
                return xmlMultiViewDataObject.getDataCache().createOutputStream();
            }
        }
        
        public boolean isModified() {
            return super.isModified();
        }
    }

    private static class XmlViewDesc implements MultiViewDescription, java.io.Serializable {
        
        private static final long serialVersionUID = 8085725367398466167L;
        XmlMultiViewDataObject dObj;
        
        XmlViewDesc() {
        }
        
        XmlViewDesc(XmlMultiViewDataObject dObj) {
            this.dObj = dObj;
        }
        
        public MultiViewElement createElement() {
            return new XmlMultiViewElement(dObj);
        }
        
        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(XmlMultiViewEditorSupport.class, "LBL_XML_TAB");
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return dObj.getHelpCtx();
        }
        
        public java.awt.Image getIcon() {
            return dObj.getXmlViewIcon();
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
        
        public String preferredID() {
            return "multiview_xml"; //NOI18N
        }
    }
    
    public TopComponent getMVTC() {
        return mvtc;
    }
    
    void setMVTC(TopComponent mvtc) {
        this.mvtc = mvtc;
        if (topComponentsListener == null) {
            topComponentsListener = new TopComponentsListener();
            TopComponent.getRegistry().addPropertyChangeListener(topComponentsListener);
        }
    }
    
    void setLastOpenView(int index) {
        lastOpenView = index;
    }
    
    private class DocumentSynchronizer extends XmlMultiViewDataSynchronizer {
        
        private final RequestProcessor.Task reloadUpdatedTask = requestProcessor.create(new Runnable() {
            public void run() {
                Document document = getDocument();
                DocumentListener listener = document == null ? null :
                    (DocumentListener) document.getProperty(PROPERTY_MODIFICATION_LISTENER);
                if (listener != null) {
                    document.removeDocumentListener(listener);
                }
                try {
                    reloadModel();
                } finally {
                    if (listener != null) {
                        document.addDocumentListener(listener);
                    }
                }
            }
        });
        
        
        public DocumentSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 100);
            getXmlEnv().addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if (Env.PROP_TIME.equals(propertyName) && getDocument() == null) {
                        dObj.getDataCache().loadData();
                    }
                }
            });
        }
        
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }
        
        protected void dataUpdated(long timeStamp) {
            if (loading == 0) {
                reloadUpdatedTask.schedule(0);
            }
        }
        
        protected Object getModel() {
            return getDocument();
        }
        
        protected void updateDataFromModel(Object model, final FileLock lock, final boolean modify) {
            final Document doc = (Document) model;
            if (doc == null) {
                try {
                    dObj.getDataCache().setData(lock, "", modify); // NOI18N
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else {
                // safely take the text from the document
                doc.render(new Runnable() {
                    public void run() {
                        try {
                            dObj.getDataCache().setData(lock, doc.getText(0, doc.getLength()), modify);
                        } catch (BadLocationException e) {
                            // impossible
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                });
            }
        }
        
        protected void reloadModelFromData() {
            if (loading == 0) {
                Utils.replaceDocument(getDocument(), dObj.getDataCache().getStringData());
            }
        }
    }
    
    public static class MyCloseHandler implements CloseOperationHandler, java.io.Serializable {

        static final long serialVersionUID = -6512103928294991474L;

        private final XmlMultiViewDataObject dObj;

        public MyCloseHandler(XmlMultiViewDataObject dObj) {
            this.dObj = dObj;
        }
        
        @Override
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            for (int i = 0; i < elements.length; i++) {
                CloseOperationState element = elements[i];
                if (ToolBarDesignEditor.PROPERTY_FLUSH_DATA.equals(element.getCloseWarningID())) {
                    return false;
                }
            }
            if (dObj.isModified()) {
                XmlMultiViewEditorSupport support = dObj.getEditorSupport();
                String msg = support.messageSave();
                
                java.util.ResourceBundle bundle =
                        org.openide.util.NbBundle.getBundle(org.openide.text.CloneableEditorSupport.class);
                
                javax.swing.JButton saveOption = new javax.swing.JButton(bundle.getString("CTL_Save")); // NOI18N
                saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
                saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N
                javax.swing.JButton discardOption = new javax.swing.JButton(bundle.getString("CTL_Discard")); // NOI18N
                discardOption.getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
                discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
                discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt(0)); // NOI18N
                
                NotifyDescriptor nd = new NotifyDescriptor(
                        msg,
                        bundle.getString("LBL_SaveFile_Title"),
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        new Object[]{saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION},
                        saveOption
                        );
                
                Object ret = org.openide.DialogDisplayer.getDefault().notify(nd);
                
                if (NotifyDescriptor.CANCEL_OPTION.equals(ret) || NotifyDescriptor.CLOSED_OPTION.equals(ret)) {
                    return false;
                }
                
                if (saveOption.equals(ret)) {
                    try {
                        if (dObj.acceptEncoding() && dObj.verifyDocumentBeforeClose() ) {
                            dObj.getEditorSupport().onCloseSave();
                        } else {
                            return false;
                        }
                    } catch (java.io.IOException e) {
                        org.openide.ErrorManager.getDefault().notify(e);
                        return false;
                    }
                } else if (discardOption.equals(ret)) {
                    dObj.getEditorSupport().onCloseDiscard();
                }
            }
            return true;
        }
    }
    
    // Accessibility for ToolBarMultiViewElement:
    protected String messageName() {
        return super.messageName();
    }
    protected String messageHtmlName() {
        return super.messageHtmlName();
    }
    protected String messageToolTip() {
        return super.messageToolTip();
    }
    
    private class TopComponentsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                // Check closed top components
                Set closed = ((Set) evt.getOldValue());
                if (closed != null) {
                    closed.removeAll((Set) evt.getNewValue());
                    for (Iterator iterator = closed.iterator(); iterator.hasNext();) {
                        Object o = iterator.next();
                        if (o instanceof CloneableTopComponent) {
                            final CloneableTopComponent topComponent = (CloneableTopComponent) o;
                            Enumeration en = topComponent.getReference().getComponents();
                            if (mvtc == topComponent) {
                                if (en.hasMoreElements()) {
                                    // Remember next cloned top component
                                    mvtc = (CloneableTopComponent) en.nextElement();
                                } else {
                                    // All cloned top components are closed
                                    notifyClosed();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static final class XmlCloneableEditor extends CloneableEditor {
        public XmlCloneableEditor(XmlMultiViewEditorSupport s) {
            super(s);
            initializeBySupport();
        }

        protected void componentActivated() {
            super.componentActivated();
        }

        protected void componentClosed() {
            super.componentClosed();
        }

        protected void componentShowing() {getTabPosition();
            super.componentShowing();
        }

        protected void componentOpened() {
            super.componentOpened();
        }
    }
}
