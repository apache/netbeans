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
package org.netbeans.modules.xml.text;

import org.netbeans.modules.xml.util.Util;
import java.io.*;
import java.util.Enumeration;
import java.lang.ref.WeakReference;
import java.beans.PropertyChangeEvent;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;

import java.util.Collection;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.XMLDataObject;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.text.syntax.XMLKit;

/**
 * Text editor support that handles I/O encoding and sync with tree.
 * There are two timers a long time and a short time. The long time
 * updates tree even in middle of writing text. The short time is restarted
 * at every text change..
 * <p>
 * Listens for: text document change (edit), timers and document status change (loading).
 */
public class TextEditorSupport extends DataEditorSupport implements EditorCookie.Observable,
        OpenCookie, EditCookie, CloseCookie, PrintCookie {
// ToDo:
// + extend CloneableEditorSupport instead of DataEditorSupport which is associated with DataObject

    /**
     * Swings document property added by this support.
     */
    public static final String PROP_DOCUMENT_URL = "doc-url";

    /**
     * Timer which countdowns the auto-reparsing time.
     */
    private Timer timer;

    /**
     * Used as lock object in close and openCloneableTopComponent.
     */
    private static java.awt.Container awtLock;

    private Representation rep;  //it is my representation

    private String mimeType;

    /**
     * public jsu for backward compatibility purposes.
     */
    protected TextEditorSupport(XMLDataObjectLook xmlDO, Env env, String mime_type) {
        super((DataObject)xmlDO, null, env);
        this.mimeType = mime_type;
        initTimer();
        initListeners();
    }

    @Override
    public void setMIMEType(String s) {
        super.setMIMEType(s);
        this.mimeType = s;
    }



    /**
     * public jsu for backward compatibility purposes.
     */
    public TextEditorSupport(XMLDataObjectLook xmlDO, String mime_type) {
        this(xmlDO, new Env(xmlDO), mime_type);
    }

    private final void syncMimeType() {
        super.setMIMEType(mimeType);
    }

    /**
     * Initialize timers and handle their ticks.
     */
    private void initTimer() {
        timer = new Timer(0, new java.awt.event.ActionListener() {
            // we are called from the AWT thread so put itno other one
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("$$ TextEditorSupport::initTimer::actionPerformed: event = " + e);

                RequestProcessor.postRequest( new Runnable() {
                    public void run() {
                        syncDocument(false);
                    }
                });
            }
        });

        timer.setInitialDelay(getAutoParsingDelay());
        timer.setRepeats(false);
    }

    @Override
    protected Pane createPane() {
        syncMimeType();
        // defect #202766: whatever+xml gets multiview, although there is nobody
        // who would register the editor. M
        if (getDataObject().getClass() == XMLDataObject.class &&
            !mimeType.equals(XMLDataObject.MIME_PLAIN_XML) &&
            !hasMultiTextEditor()) {
            return createCloneableEditor();
        }
        return (CloneableEditorSupport.Pane)MultiViews.createCloneableMultiView(mimeType,
                getDataObject());
    }

    /**
     * Detects whether XML source editor is registered: must start with 'xml.text'. If
     * a module registers such multiview, it's responsible for displaying source in this
     * pane.
     *
     * @return true, if multiview source editor is available and multiview pane should be created
     */
    boolean hasMultiTextEditor() {
        Collection<? extends MultiViewDescription> all = MimeLookup.getLookup(mimeType).lookupAll(MultiViewDescription.class);
        for (MultiViewDescription d : all) {
            if (d.preferredID().startsWith("xml.text")) {
                return true;
            }
        }
        return false;
    }

    /*
     * Add listeners at Document and document memory status (loading).
     */
    private void initListeners() {
        // create document listener
        final DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::insertUpdate: event = " + e);
                restartTime();
            }

            public void changedUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::changedUpdate: event = " + e);
                // not interested in attribute changes
            }

            public void removeUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::removeUpdate: event = " + e);
                restartTime();
            }

            private void restartTime() {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::restartTime: isInSync = " +
                getXMLDataObjectLook().getSyncInterface().isInSync());

                if (getXMLDataObjectLook().getSyncInterface().isInSync()) {
                    return;
                }
                restartTimer(false);
            }
        };

        // listen for document loading then register to it the docListener as weak

        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {

                if (isDocumentLoaded()) {

                    Document doc = getDocument();
                    // when the document is not yet loaded, do nothing
                    if (doc == null)
                        return;
                    doc.addDocumentListener(WeakListeners.document(docListener, doc));

                    Representation newRep;
                    
                    synchronized(TextEditorSupport.this) {
                        newRep = rep;
                    }
                    
                    if (newRep == null) {
                        XMLDataObjectLook dobj = (XMLDataObjectLook) getDataObject();
                        Synchronizator sync = dobj.getSyncInterface();

                        //!!! What does this hardcoding mean???
                        //[DEPENDENCY] it introduces really ugly core to it's client dependencies!!!
                        if (dobj instanceof org.netbeans.modules.xml.XMLDataObject) {
                            newRep = new XMLTextRepresentation(TextEditorSupport.this, sync);
                        } else if (dobj instanceof DTDDataObject) {
                            newRep = new DTDTextRepresentation(TextEditorSupport.this, sync);
                        } else if (dobj instanceof EntityDataObject) {
                            newRep = new EntityTextRepresentation(TextEditorSupport.this, sync);
                        }

                        if (newRep != null) {
                            synchronized (TextEditorSupport.this) {
                                if (rep == null) {
                                    rep = newRep;
                                } else {
                                    newRep = null;
                                }
                            }
                            if (newRep != null) {
                                sync.addRepresentation(newRep);
                            }
                        }
                    }
                }
            }
        });

    }


    /**
     * It simply calls super.notifyClosed() for all instances except
     * TextEditorSupport.class == this.getClass().
     */
    protected void notifyClosed() {
        super.notifyClosed();

        // #15756 following code handles synchronization on text editor closing only!
        if (this.getClass() != TextEditorSupport.class) return;

        XMLDataObjectLook dobj = (XMLDataObjectLook) getDataObject();
        Synchronizator sync = dobj.getSyncInterface();
        Representation oldRep;
        synchronized (this) {
            oldRep = rep;
            rep = null;
        }
        if ( oldRep != null ) { // because of remove modified document
            sync.removeRepresentation(oldRep);
        }

//          if ( isModified() ) { // possible way to remove needless closeDocument followed by open
//              Task reload = reloadDocument();
//              reload.waitFinished();
//          }
    }

    /**
     */
    Env getEnv() {
        return (Env) env;
    }


    /**
     */
    protected XMLDataObjectLook getXMLDataObjectLook() {
        return getEnv().getXMLDataObjectLook();
    }

    /*
     * Update presence of SaveCookie on first keystroke.
     */
    protected boolean notifyModified() {
        if (getEnv().isModified()) {
            return true;
        }
        if (!super.notifyModified()) {
            return false;
        }

        CookieManagerCookie manager = getEnv().getXMLDataObjectLook().getCookieManager();
        manager.addCookie(getEnv());
        XMLDataObjectLook obj = (XMLDataObjectLook) getDataObject();
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieManager().addCookie(new SaveCookie() {
                public void save() throws java.io.IOException {
                    try {
                        saveDocument();
                    } catch(UserCancelException e) {
                        //just ignore
                    }
                }
            });
        }

        return true;
    }

    /*
     * Update presence of SaveCookie after save.
     */
    protected void notifyUnmodified() {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Notifing unmodified"); // NOI18N

        super.notifyUnmodified();
        CookieManagerCookie manager = getEnv().getXMLDataObjectLook().getCookieManager();
        manager.removeCookie(getEnv());
    }

   /** Store the document in proper encoding.
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream out) throws IOException, BadLocationException {
        // not calling super.
        String enc = EncodingUtil.detectEncoding(doc);
        
        // saved form saveDocument()
        Charset cs = fileEncoding.get();
        // + fallback, if no info is available
        if (cs == null) {
            if (enc != null) {
                cs = Charset.forName(enc);
            } else {
                // fallback to the original encoding, no encoding in document istelf.
                cs = FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile());
            }
        }
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Saving using encoding");//, new RuntimeException (enc)); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!! TextEditorSupport::saveFromKitToStream: enc = " + enc);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!!                  ::saveFromKitToStream: after first test -> OK");

        FilterOutputStream fos = new FilterOutputStream(out) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        Writer w = new OutputStreamWriter (fos, encoder);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!!                  ::saveFromKitToStream: writer = " + w);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }
    
    /**
     * It's not possible to open input stream from saveFromStreamToKit as FEQ.getEncoding() does. This TL
     * variable passes the desired encoding to the inner method through openIDE/text call machinery.
     */
    private ThreadLocal<Charset> fileEncoding = new ThreadLocal<Charset>();



//~~~~~~~~~~~~~~~~~~~~~~~~~ I/O ENCODING HANDLING ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
     * Save document using encoding declared in XML prolog if possible otherwise
     * at UTF-8 (in such case it updates the prolog).
     */
    public void saveDocument() throws IOException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("saveDocument()..."); // NOI18N
        final StyledDocument doc = getDocument();
        String enc = EncodingUtil.detectEncoding(doc);
        Charset cs = null;
        
        try {
            if (enc == null) {
                cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            } else {
                cs = Charset.forName(enc);
            }
            if (!cs.newEncoder().canEncode(doc.getText(0, doc.getLength()))) {
                handleUnsupportedEncoding(doc, enc);
            }
        } catch (BadLocationException ble) {
            // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
            return;
        } catch (IllegalCharsetNameException ex) {
            handleUnsupportedEncoding(doc, enc);
            return;
        } catch (UnsupportedCharsetException ex) {
            // handle invalid character set
            handleUnsupportedEncoding(doc, enc);
            return;
        }
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!! TextEditorSupport::saveDocument: enc = " + enc);
        try {
            // Note: this pass-around of encoding duplicates the DataEditorSupport's implemnetation. In addition to what DES.saveFromKitToStream does, this impl
            // detects encoding from the to-be-saved document AND handles invalid encoding gracefully (by asking the user).
            fileEncoding.set(cs);
            super.saveDocument();
            //moved from Env.save()
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//            getDataObject().setModified(false);
            getXMLDataObjectLook().getSyncInterface().representationChanged(Document.class);
        } catch (UnmappableCharacterException ex) {
            handleUnsupportedEncoding(doc, enc);
        } catch (UnsupportedEncodingException ex) {
            handleUnsupportedEncoding(doc, enc);
        } finally {
            fileEncoding.remove();
        }
    }

    private void handleUnsupportedEncoding(StyledDocument doc, String enc) throws IOException {
        //ask user what next?
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                java.text.MessageFormat.format(Util.THIS.getString(
                TextEditorSupport.class, enc == null ? "TEXT_SAVE_AS_UTF_DEFAULT" : "TEXT_SAVE_AS_UTF"), enc));
        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res.equals(NotifyDescriptor.YES_OPTION)) {
            updateDocumentWithNewEncoding(doc);
        } else { // NotifyDescriptor != YES_OPTION
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Let unsaved."); // NOI18N
            throw new UserCancelException("Content could not be saved using the current encoding");
        }
    }

    /**
     * update prolog to new valid encoding
     */
    private void updateDocumentWithNewEncoding(final StyledDocument doc) throws IOException {
        try {
            final int MAX_PROLOG = 1000;
            int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
            final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
            int prologLen = -1;  // actual prolog length
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
                        if (passPrologLen > 0) {
                            doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                            doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N
                        } else {
                            doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: no XML declaration present -->\n", null); // NOI18N
                        }
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
//            getDataObject().setModified(false);
            getXMLDataObjectLook().getSyncInterface().representationChanged(Document.class);
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Saved."); // NOI18N
        } catch (BadLocationException lex) {
            ErrorManager.getDefault().notify(lex);
        }
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SYNC ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * TEXT changed -> update TREE.
     */
    protected void syncDocument(boolean fromFocus) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("@@ TextEditorSupport::syncDocument: fromFocus = " + fromFocus);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("@@                  ::syncDocument: timer.isRunning = " + timer.isRunning());

        if (fromFocus && !timer.isRunning())
            return;
        if (timer.isRunning())
            timer.stop();

        XMLDataObjectLook sync = getXMLDataObjectLook();
        if (sync != null) { // && isModified()) {
            sync.getSyncInterface().representationChanged(Document.class);
        }

    }

    int getAutoParsingDelay () {
        return 3000;
    }

    /** Restart the timer which starts the parser after the specified delay.
     * @param onlyIfRunning Restarts the timer only if it is already running
     */
    void restartTimer(boolean onlyIfRunning) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("## TextEditorSupport::restartTimer: onlyIfRunning = " + onlyIfRunning);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("##                  ::restartTimer: timer.isRunning = " + timer.isRunning());

        if (onlyIfRunning && !timer.isRunning())
            return;

        int delay = getAutoParsingDelay();
        if (delay > 0) {
            timer.setInitialDelay(delay);
            timer.restart();
        }
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    /*
     * Simply open for an cloneable editor. It at first tries to locate
     * existing component in <code>allEditors</code> then if it fails create new one
     * and registers it with <code>allEditors>/code>.
     */
    protected final CloneableEditor openCloneableEditor() {

        CloneableEditor ret = null;

        synchronized (getLock()) {

            String msg = messageOpening();
            if (msg != null) {
                StatusDisplayer.getDefault().setStatusText(msg);
            }

            Enumeration<CloneableTopComponent> en = allEditors.getComponents();
            while ( en.hasMoreElements() ) {
                CloneableTopComponent editor = en.nextElement();
                if ( editor instanceof CloneableEditor ) {
                    editor.open();
                    ret = (CloneableEditor) editor;
                }
            }

            // no opened editor, create a new one

            if (ret == null) {
                CloneableEditor editor = (CloneableEditor)createCloneableTopComponent(); // this is important -- see final createCloneableTopComponent
                editor.setReference(allEditors);
                editor.open();
                ret = editor;
            }

            msg = messageOpened();
            if (msg == null) {
                msg = ""; // NOI18N
            }
            StatusDisplayer.getDefault().setStatusText(msg);

            return ret;
        }
    }

    /**
     * Creates lock object used in close and openCloneableTopComponent.
     * @return never null
     */
    protected Object getLock() {
        if (awtLock == null) {
            awtLock = new java.awt.Container();
        }
        return awtLock.getTreeLock();
    }

    /*
     * @return component visualizing this support.
     */
    protected CloneableEditor createCloneableEditor() {
        syncMimeType();
        return new TextEditorComponent(this);
    }

    // This must call super createCloneableTopComponent because it prepare document, create cloneable editor and initialize it. See super.
    protected final CloneableTopComponent createCloneableTopComponent() {
        syncMimeType();
        return super.createCloneableTopComponent(); // creates CloneableEditor (calling createCloneableEditor)
    }

    /**
     */
    public static final TextEditorSupportFactory findEditorSupportFactory(XMLDataObjectLook xmlDO, String mime) {
        return new TextEditorSupportFactory(xmlDO, mime);
    }


    // used from unit tests
    String getMIMEType() {
        return mimeType;
    }

    //
    // class Env
    //

    /**
     *
     */
    protected static class Env extends DataEditorSupport.Env implements SaveCookie {

        /** Serial Version UID */
        private static final long serialVersionUID=-5285524519399090028L;

        /** */
        public Env(XMLDataObjectLook obj) {
            super((DataObject)obj);
        }

        /**
         */
        protected XMLDataObjectLook getXMLDataObjectLook() {
            return (XMLDataObjectLook) getDataObject();
        }

        /**
         */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /**
         */
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }


        /**
         */
        public synchronized void save() throws IOException {
            findTextEditorSupport().saveDocument();
        }

        /**
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return findTextEditorSupport();
        }

        /**
         */
        public TextEditorSupport findTextEditorSupport() {
            EditCookie cookie = getDataObject().getCookie(EditCookie.class);
            if(cookie instanceof TextEditorSupport)
                return (TextEditorSupport)cookie;

            return null;
        }

        // copy pasted, do not get it
        public void propertyChange(PropertyChangeEvent ev) {
            if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())) {
                changeFile();
            }
            super.propertyChange(ev);
        }


    } // end: class Env


    //
    // class TextEditorSupportFactory
    //

    /**
     *
     */
    public static class TextEditorSupportFactory implements CookieSet.Factory {
        /** */
        private WeakReference<TextEditorSupport> editorRef;
        /** */
        private final XMLDataObjectLook dataObject; // used while creating the editor
        /** */
        private String mime;                  // used while creating the editor

        //
        // init
        //

        /** Create new TextEditorSupportFactory. */
        public TextEditorSupportFactory(XMLDataObjectLook dobj, String mime) {
            if (mime == null && !(dobj instanceof DataObject)) {
                throw new IllegalArgumentException("DataObject is needed to lazy-resolve MIME type");
            }
            this.dataObject = dobj;
            this.mime       = mime;
        }


        /**
         */
        protected Class[] supportedCookies() {
            return new Class[] {
                    EditorCookie.Observable.class,
                    OpenCookie.class,
                    EditCookie.class,
                    CloseCookie.class,
                    PrintCookie.class,
                    CloneableEditorSupport.class
            };
        }

        /**
         */
        public final void registerCookies(CookieSet cookieSet) {
            cookieSet.add(supportedCookies(), this);
        }

        /** Creates a Node.Cookie of given class. The method
         * may be called more than once.
         */
        public final Node.Cookie createCookie(Class klass) {
            Class[] supportedCookies = supportedCookies();
            for (int i = 0; i < supportedCookies.length; i++) {
                if ( supportedCookies[i].isAssignableFrom(klass) ) {
                    return createEditor();
                }
            }
            return null;
        }

        /**
         */
        public final TextEditorSupport createEditor() { // atomic test and set
            TextEditorSupport editorSupport = null;

            synchronized (this) {
                if ( editorRef != null ) {
                    editorSupport = editorRef.get();
                }
                if ( editorSupport != null ) {
                    return editorSupport;
                }
                editorSupport = prepareEditor();
                editorRef = new WeakReference<>(editorSupport);
            }
            editorSupport.syncMimeType();
            return editorSupport;
        }

        /**
         */
        protected TextEditorSupport prepareEditor() {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Initializing TextEditorSupport ..."); // NOI18N

            return new TextEditorSupport(getDataObject(), getMIMEType());
        }

        /**
         */
        protected final XMLDataObjectLook getDataObject() {
            return dataObject;
        }

        /**
         */
        protected final String getMIMEType() {
            if (mime == null) {
                // lazy-initialize the MIME type:
                mime = findMIMEtype();
            }
            return mime;
        }

        private String findMIMEtype() {
            FileObject fo = ((DataObject)dataObject).getPrimaryFile();
            String mimetype = null;

             if (fo.isValid()) {
                 mimetype = fo.getMIMEType();
             }
            //when undelying fileobject has a mimetype defined,
            //don't enforce text/xml on the editor document.
            //be conservative and apply the new behaviour only when the mimetype is xml like..
            if (mimetype == null || mimetype.indexOf("xml") == -1) { // NOI18N
                mimetype = XMLKit.MIME_TYPE;
            }
            // divert the standard MIME type to plain XML; others must register their
            // multiviews for non-standard mime types.
            if (XMLKit.MIME_TYPE.equals(mimetype)) {
                mimetype = org.netbeans.modules.xml.XMLDataObject.MIME_PLAIN_XML;
            }
            return mimetype;
        }

    } // end of class TextEditorSupportFactory

}
