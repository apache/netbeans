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

package org.netbeans.modules.properties;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.NbDocument;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.NbBundle.Messages;
import org.netbeans.core.api.multiview.MultiViews;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharset;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharsetEncoder;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableOpenSupport;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import static java.util.logging.Level.FINER;
import javax.swing.*;
import org.openide.filesystems.StatusDecorator;

/** 
 * Support for viewing .properties files (EditCookie) by opening them in a text editor.
 *
 * @author Petr Jiricka, Peter Zavadsky 
 * @see org.openide.text.CloneableEditorSupport
 */
public class PropertiesEditorSupport extends CloneableEditorSupport 
implements EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, Serializable, SaveAsCapable {
    
    /** error manager for CloneableEditorSupport logging and error reporting */
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.properties.PropertiesEditorSupport"); // NOI18N
    
    /** */
    private FileStatusListener fsStatusListener;
    
    /** Visible view of underlying file entry */
    transient PropertiesFileEntry myEntry;
    
    /** Generated serial version UID. */
    static final long serialVersionUID =1787354011149868490L;
    
    private Charset charset;
    
    /** Constructor. */
    public PropertiesEditorSupport(PropertiesFileEntry entry) {
        super(new Environment(entry), new ProxyLookup(Lookups.singleton(entry.getDataObject()), entry.getDataObject().getLookup()));
        this.myEntry = entry;
    }
    
    @Override
    protected Pane createPane() {
        return (Pane) MultiViews.createCloneableMultiView(PropertiesDataLoader.PROPERTIES_MIME_TYPE, getDataObject());
    }
    
    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final CloneableEditorSupport.Env desEnv() {
        return (CloneableEditorSupport.Env) env;
    }
    
    /** 
     * Overrides superclass method.
     * Should test whether all data is saved, and if not, prompt the user
     * to save. Called by my topcomponent when it wants to close its last topcomponent, but the table editor may still be open
     * @return <code>true</code> if everything can be closed
     */
    @Override
    protected boolean canClose () {
        // if the table is open, can close without worries, don't remove the save cookie
        if (hasOpenedTableComponent()){
            return true;
        }else{
            DataObject propDO = myEntry.getDataObject();
            if ((propDO == null) || !propDO.isModified()) {
                return true;
            }
            return super.canClose();
        }
    }
    
    /** Getter of the data object that this support is associated with.
    * @return data object passed in constructor
    */
    public final DataObject getDataObject () {
        return myEntry.getDataObject();
    }

    private boolean isEnvReadOnly() {
        CloneableEditorSupport.Env myEnv = desEnv();
        return myEnv instanceof Environment && !((Environment) myEnv).getFileImpl().canWrite();
    }
    
    /**
     *
     */
    final class FsStatusListener implements FileStatusListener, Runnable {
        
	/**
	 */
	public void annotationChanged(FileStatusEvent ev) {
            if (ev.isNameChange() && ev.hasChanged(myEntry.getFile())) {
                Mutex.EVENT.writeAccess(this);
            }
	}
        
	/**
	 */
	public void run() {
	    updateEditorDisplayNames();
	}
    }
    
    /**
     */
    private void attachStatusListener() {
        if (fsStatusListener != null) {
            return;                 //already attached
        }
        
        FileSystem fs;
        try {
            fs = myEntry.getFile().getFileSystem();
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        
        fsStatusListener = new FsStatusListener();
        fs.addFileStatusListener(
                FileUtil.weakFileStatusListener(fsStatusListener, fs));
    }
    
    /**
     */
    private void updateEditorDisplayNames() {
        assert EventQueue.isDispatchThread();
        
        final String title = messageName();
        final String htmlTitle = messageHtmlName();
        final String toolTip = messageToolTip();
        Enumeration en = allEditors.getComponents();
        while (en.hasMoreElements()) {
            TopComponent tc = (TopComponent) en.nextElement();
            tc.setDisplayName(title);
            tc.setHtmlDisplayName(htmlTitle);
            tc.setToolTipText(toolTip);
        }
    }
    
    /**
     */
    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
	((PropertiesEditor) editor).initialize(myEntry);
    }

    /**
     * Overrides superclass method. 
     * Let's the super method create the document and also annotates it
     * with Title and StreamDescription properities.
     * @param kit kit to user to create the document
     * @return the document annotated by the properties
     */
    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument document = super.createStyledDocument(kit);
        
        // Set additional proerties to document.
        // Set document name property. Used in CloneableEditorSupport.
        document.putProperty(Document.TitleProperty, myEntry.getFile().toString());
        
        // Set dataobject to stream desc property.
        document.putProperty(Document.StreamDescriptionProperty, myEntry.getDataObject());
        
        // hook the document to listen for any changes to update changes by
        // reparsing the document
        document.addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed();}
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed();}            
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed();}
            private void changed() {
                myEntry.getHandler().autoParse();                
            }
        });
        
        return document;
    }

    /**
     * Reads the file from the stream, filter the guarded section
     * comments, and mark the sections in the editor. Overrides superclass method. 
     * @param document the document to read into
     * @param inputStream the open stream to read from
     * @param editorKit the associated editor kit
     * @throws <code>IOException</code> if there was a problem reading the file
     * @throws <code>BadLocationException</code> should not normally be thrown
     * @see #saveFromKitToStream
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument document, InputStream inputStream, EditorKit editorKit) throws IOException, BadLocationException {
        final Charset c = getCharset();
        final Reader reader = new BufferedReader(new InputStreamReader(inputStream, c));

        try {
            editorKit.read(reader, document, 0);
        } finally {
            reader.close();
        }
    }

    /** 
     * Adds new lines according actual value of <code>newLineType</code> variable.
     * Overrides superclass method.
     * @param document the document to write from
     * @param editorKit the associated editor kit
     * @param outputStream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    @Override
    protected void saveFromKitToStream(StyledDocument document, EditorKit editorKit, OutputStream outputStream) throws IOException, BadLocationException {
        final Writer writer;
        final Charset c = getCharset();
        if (c.name().equals(PropertiesEncoding.PROP_CHARSET_NAME)) {
            final PropCharsetEncoder encoder = new PropCharsetEncoder();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoder));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, c));
        }

        try {
            editorKit.write(writer, document, 0, document.getLength());
        } finally {
            writer.flush();
            writer.close();
        }
    }
    
    private Charset getCharset() {
        if (charset == null) {
            charset = FileEncodingQuery.getEncoding(myEntry.getDataObject().getPrimaryFile());
        }
        return charset;
    }

    void resetCharset() {
        charset = null;
    }

    /** 
     * Adds a save cookie if the document has been marked modified. Overrides superclass method. 
     * @return <code>true</code> if the environment accepted being marked as modified
     *    or <code>false</code> if it refused it and the document should still be unmodified
     */
    @Override
    protected boolean notifyModified () {
        // Reparse file.
        myEntry.getHandler().autoParse();

        if (!super.notifyModified()) {
            return false; // Will cause the log message:
                          // INFO [org.openide.text.ClonableEditorSupport]: ...
        }

        // See #89029, #175275, #186876 for info about the implementation

        ((Environment)env).addSaveCookie();

        return true; 
    }
    
    @Override
    protected Task reloadDocument(){
        Task tsk = super.reloadDocument();
        tsk.addTaskListener(new TaskListener(){
            public void taskFinished(Task task){
                myEntry.getHandler().autoParse();
            }
        });
        return tsk;
    }

    /** Overrides superclass method. Adds checking for opened Table component. */
    @Override
    protected void notifyUnmodified () {
        super.notifyUnmodified();
        
        ((Environment)env).removeSaveCookie();
    }
    
    /**
     */
    @Override
    public void open() {
	super.open();
	attachStatusListener();
    }
    
    /** Overrides superclass method. Adds checking for opened Table panel. */
    @Override
    protected void notifyClosed() {
        // Close document only in case there is not open table editor.
        if(!hasOpenedTableComponent()) {
            boolean wasModified = isModified();
            super.notifyClosed();
            if (wasModified) {
                // #21850. Don't reparse invalid or virtual file.
                if(myEntry.getFile().isValid() && !myEntry.getFile().isVirtual()) {
                    myEntry.getHandler().reparseNowBlocking();
                }
            }
        }
    }

    /** 
     * Overrides superclass abstract method. 
     * Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpening() {
        return NbBundle.getMessage(
            PropertiesEditorSupport.class,
            "LBL_ObjectOpen", // NOI18N
            getFileLabel()
        );
    }
    
    /**
     * Overrides superclass abstract method. 
     * Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpened() {
        return NbBundle.getMessage(
            PropertiesEditorSupport.class,
            "LBL_ObjectOpened", // NOI18N
            getFileLabel()
       );
    }
    
    private String getFileLabel() {
        PropertiesDataObject propDO = (PropertiesDataObject) myEntry.getDataObject();
        return propDO.getPrimaryFile().getNameExt();
    }
    
    /** 
     * Overrides superclass abstract method. 
     * Constructs message that should be used to name the editor component.
     * @return name of the editor
     */
    protected String messageName () {
        if (!myEntry.getDataObject().isValid()) {
            return "";                                                  //NOI18N       
        }
        return DataEditorSupport.annotateName(getFileLabel(), false, isModified(), !myEntry.getFile().canWrite());
    }

    /** */
    @Override
    protected String messageHtmlName () {
        if (!myEntry.getDataObject().isValid()) {
            return null;
        }

        String rawName = getFileLabel();
        
        String annotatedName = null;
        final FileObject entry = myEntry.getFile();
        try {
            StatusDecorator status = entry.getFileSystem().getDecorator();
            if (status != null) {
                Set<FileObject> files = Collections.singleton(entry);
                annotatedName = status.annotateNameHtml(rawName, files);
                if (rawName.equals(annotatedName)) {
                    annotatedName = null;
                }
                if ((annotatedName != null)
                        && (!annotatedName.startsWith("<html>"))) { //NOI18N
                    annotatedName = "<html>" + annotatedName;       //NOI18N
                }
                if (annotatedName == null) {
                    annotatedName = status.annotateName(rawName, files);
                }
            }
        } catch (FileStateInvalidException ex) {
            //do nothing and fall through
        }
        
        String name = (annotatedName != null) ? annotatedName : /*XXX escape HTML content*/rawName;
        return DataEditorSupport.annotateName(name, true, isModified(), !myEntry.getFile().canWrite());
    }
    
    /** 
     * Overrides superclass abstract method.
     * Is modified and is being closed.
     * @return text to show to the user
     */
    protected String messageSave () {
        return NbBundle.getMessage (
            PropertiesEditorSupport.class,
            "MSG_SaveFile", // NOI18N
            getFileLabel()
        );
    }
    
    /** 
     * Overrides superclass abstract method.
     * Text to use as tooltip for component.
     * @return text to show to the user
     */
    protected String messageToolTip () {
        // copied from DataEditorSupport, more or less
        FileObject fo = myEntry.getFile();
        return DataEditorSupport.toolTip(fo, isModified(), !myEntry.getFile().canWrite());
    }
    
    /** Overrides superclass method. Gets <code>UndoRedo</code> manager which maps 
     * <code>UndoalbleEdit</code>'s to <code>StampFlag</code>'s. */
    @Override
    protected UndoRedo.Manager createUndoRedoManager () {
        return new UndoRedoStampFlagManager();
    }
    
    /** 
     * Helper method. Hack on superclass <code>getUndoRedo()</code> method, to widen its protected modifier. 
     * Needs to be accessed from outside this class (in <code>PropertiesOpen</code>). 
     * @see PropertiesOpen 
     */
    UndoRedo.Manager getUndoRedoManager() {
        return super.getUndoRedo();
    }
    
    /** 
     * Helper method. Used only by <code>PropertiesOpen</code> support when closing last Table component.
     * Note: It's quite ugly by-pass of <code>notifyClosed()</code> method. Should be revised. 
     */
    void forceNotifyClosed() {
        super.notifyClosed();
    }
    
    /** Helper method. Saves this entry. */
    private void saveThisEntry() throws IOException {
        FileSystem.AtomicAction aa = new SaveImpl(this);
        FileUtil.runAtomicAction(aa);
//        super.saveDocument();
        // #32777 - it can happen that save operation was interrupted
        // and file is still modified. Mark it unmodified only when it is really
        // not modified.
        if (!env.isModified()) {
            myEntry.setModified(false);
        }
    }

    final void superSaveDoc() throws IOException {
        super.saveDocument();
    }
    
    /**
     * Save the document under a new file name and/or extension.
     * @param folder New folder to save the DataObject to.
     * @param fileName New file name to save the DataObject to.
     * @throws java.io.IOException If the operation failed
     * @since 6.3
     */
    public void saveAs( FileObject folder, String fileName ) throws IOException {
        //ask the user for a new file name to save to
        String newExtension = FileUtil.getExtension( fileName );

        DataObject newDob = null;
        DataObject currentDob = myEntry.getDataObject();
        if( !currentDob.isModified() || null == getDocument() ) {
            //the document is not modified on disk, we copy/rename the file
            DataFolder df = DataFolder.findFolder( folder );

            FileObject newFile = folder.getFileObject(fileName);
            if( null != newFile ) {
                //remove the target file if it already exists
                newFile.delete();
            }

            newFile = myEntry.copyRename(df.getPrimaryFile(), getFileNameNoExtension(fileName), newExtension);
            if (null != newFile) {
                newDob = DataObject.find(newFile);
            }
        } else {
            //the document is modified in editor, we need to save the editor kit instead
            FileObject newFile = FileUtil.createData( folder, fileName );
            saveDocumentAs( newFile.getOutputStream() );
            currentDob.setModified( false );
            newDob = DataObject.find( newFile );
        }

        if( null != newDob ) {
            OpenCookie c = newDob.getCookie( OpenCookie.class );
            if( null != c ) {
                //close the original document
                close( false );
                //open the new one
                c.open();
            }
        }
    }
    
    private String getFileNameNoExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    
    /** 
     * Save the document to a new file.
     * @param output 
     * @exception IOException on I/O error
     * @since 6.3
     */
    private void saveDocumentAs( final OutputStream output ) throws IOException {

        final StyledDocument myDoc = getDocument();
        
        // save the document as a reader
        class SaveAsWriter implements Runnable {
            private IOException ex;

            public void run() {
                try {
                    OutputStream os = null;

                    try {
                        os = new BufferedOutputStream( output );
                        EditorKit kit = createEditorKit();
                        saveFromKitToStream( myDoc, kit, os );

                        os.close(); // performs firing
                        os = null;

                    } catch (BadLocationException ex2) {
                        LOG.log(Level.INFO, null, ex2);
                    } finally {
                        if (os != null) { // try to close if not yet done
                            os.close();
                        }
                    }
                } catch (IOException e) {
                    this.ex = e;
                }
            }

            public void after() throws IOException {
                if (ex != null) {
                    throw ex;
                }
            }
        }

        SaveAsWriter saveAsWriter = new SaveAsWriter();
        myDoc.render(saveAsWriter);
        saveAsWriter.after();
    }

    /** Helper method. 
     * @return whether there is an table view opened */
    public synchronized boolean hasOpenedTableComponent() {
        PropertiesDataObject dataObject = (PropertiesDataObject) myEntry.getDataObject();
        if (dataObject.getBundleStructureOrNull() == null || dataObject.getBundleStructure().getEntryCount()==0) {
            return false;
        }
        return dataObject.getOpenSupport().hasOpenedTableComponent();
    }
    
    /**
     * Helper method.
     * @return whether there is an open editor component. */
    public synchronized boolean hasOpenedEditorComponent() {
        Enumeration en = allEditors.getComponents ();
        return en.hasMoreElements ();
    }

    /** Class which exist only due comaptibility with version 3.0. */    
    private static final class Env extends Environment {
        /** Generated Serialized Version UID. */
        static final long serialVersionUID = -9218186467757330339L;

        /** Used for deserialization. */
        private PropertiesFileEntry entry;

        /** */
        public Env(PropertiesFileEntry entry) {
            super(entry);
        }

        /** Adds passing entry field to superclass. */
        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
                in.defaultReadObject();
                
                if (this.entry != null) {
                    super.entry = this.entry;
                }
        }
        }


    /** Nested class. Implementation of <code>ClonableEditorSupport.Env</code> interface. */
    private static class Environment implements CloneableEditorSupport.Env,
    PropertyChangeListener, SaveCookie {
        
        /** generated Serialized Version UID */
        static final long serialVersionUID = 354528097109874355L;
            
        /** Entry on which is support build. */
        private PropertiesFileEntry entry;

        /** Lock acquired after the first modification and used in <code>save</code> method. */
        private transient FileLock fileLock;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
        private transient FileObject fileObject;
            
        /** Spport for firing of property changes. */
        private transient PropertyChangeSupport propSupp;
        
        /** Support for firing of vetoable changes. */
        private transient VetoableChangeSupport vetoSupp;

        private transient EnvironmentListener envListener;
            
        /** Constructor.
         * @param obj this support should be associated with
         */
        public Environment (PropertiesFileEntry entry) {
            LOG.finer("PropertiesEditorSupport(<PropertiesFileEntry>)");//NOI18N
            LOG.finer(" - new Environment(<PropertiesFileEntry>)");     //NOI18N
            this.entry = entry;
            envListener = new EnvironmentListener(this);
            entry.getFile().addFileChangeListener(envListener);
            entry.addPropertyChangeListener(this);
        }
        /** Getter for the file to work on.
        * @return the file
        */
        private FileObject getFileImpl () {
            // updates the file if there was a change
            changeFile();
            return fileObject;
        }

        protected final DataObject getDataObject() {
            return entry.getDataObject();
        }
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Adds property listener. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            LOG.finer("Environment.addPropertyChangeListener(...)");    //NOI18N
            prop().addPropertyChangeListener (l);
        }

        
        /** Accepts property changes from entry and fires them to own listeners. */
        public void propertyChange(PropertyChangeEvent evt) {
            if (LOG.isLoggable(FINER)) {
                LOG.finer("Environment.propertyChange("                 //NOI18N
                          + evt.getPropertyName()
                          + ", " + evt.getOldValue()                    //NOI18N
                          + ", " + evt.getNewValue()                    //NOI18N
                          + ')');
            }
            // We will handle the object invalidation here.
            if(DataObject.PROP_VALID.equals(evt.getPropertyName ())) { 
                // do not check it if old value is not true
                if (Boolean.FALSE.equals(evt.getOldValue())) {
                    return;
                }

                // loosing validity
                PropertiesEditorSupport support = (PropertiesEditorSupport)findCloneableOpenSupport();
                if(support != null) {
                    
                    // mark the object as not being modified, so nobody
                    // will ask for save
                    unmarkModified();

                    support.close(false);
                }
            } else {
                firePropertyChange (
                    evt.getPropertyName(),
                    evt.getOldValue(),
                    evt.getNewValue()
                );
            }
        }
        
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Removes property listener. */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            LOG.finer("Environment.removePropertyChangeListener(...)"); //NOI18N
            prop().removePropertyChangeListener (l);
        }
            
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Adds veto listener. */
        public void addVetoableChangeListener(VetoableChangeListener l) {
            LOG.finer("Environment.addVetoableChangeListener(...)");    //NOI18N
            veto().addVetoableChangeListener (l);
        }
            
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Removes veto listener. */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            LOG.finer("Environment.removeVetoableChangeListener(...)"); //NOI18N
            veto().removeVetoableChangeListener (l);
        }

        /** Overrides superclass method.
         * Note: in fact it returns <code>CloneableEditorSupport</code> instance.
         * @return the support or null if the environemnt is not in valid
         * state and the CloneableOpenSupport cannot be found for associated
         * entry object
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (PropertiesEditorSupport)entry.getCookieSet().getCookie(EditCookie.class);
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         * @return true or false depending on its state
         */
        public boolean isValid() {
            return entry.getDataObject().isValid();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Test whether the object is modified or not.
         * @return true if the object is modified
         */
        public boolean isModified() {
            return entry.isModified();
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * First of all tries to lock the primary file and
         * if it succeeds it marks the data object modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
         */
        public void markModified() throws java.io.IOException {
            LOG.finer("Environment.markModified()");                    //NOI18N
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = entry.takeLock();
            }
            
            entry.setModified(true);
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Reverse method that can be called to make the environment
         * unmodified.
         */
        public void unmarkModified() {
            LOG.finer("Environment.unmarkModified()");                  //NOI18N
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
            }
            
            entry.setModified(false);
        }
        /**
         * Called from the <code>EnvironmentListener</code>.
         */
        final void updateDocumentProperty () {
            //Update document TitleProperty
            EditorCookie ec = getDataObject().getCookie(EditorCookie.class);
            if (ec != null) {
                StyledDocument doc = ec.getDocument();
                if (doc != null) {
                    doc.putProperty(Document.TitleProperty,
                    FileUtil.getFileDisplayName(getDataObject().getPrimaryFile()));
                }
            }
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Mime type of the document.
         * @return the mime type to use for the document
         */
        public String getMimeType() {
            return getFileImpl().getMIMEType();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * The time when the data has been modified. */
        public Date getTime() {
            // #32777 - refresh file object and return always the actual time
            getFileImpl().refresh();
            return getFileImpl().lastModified();
        }

        /** Method that allows subclasses to notify this environment that
        * the file associated with this support has changed and that
        * the environment should listen on modifications of different
        * file object.
        */
        protected final void changeFile () {
            FileObject newFile = entry.getFile ();
            if (newFile.equals (fileObject)) {
                // the file has not been updated
                return;
            }

            boolean lockAgain;
            if (fileLock != null) {
// <> NB #61818 In case the lock was not active (isValid() == false), the new lock was taken,
// which seems to be incorrect. There is taken a lock on new file, while it there wasn't on the old one.
//                fileLock.releaseLock ();
//                lockAgain = true;
// =====
                if(fileLock.isValid()) {
                    LOG.fine("changeFile releaseLock: " + fileLock + " for " + fileObject); // NOI18N
                    fileLock.releaseLock ();
                    lockAgain = true;
                } else {
                    fileLock = null;
                    lockAgain = false;
                }
// </>
            } else {
                lockAgain = false;
            }

            boolean wasNull = fileObject == null;

            fileObject = newFile;
            LOG.fine("changeFile: " + newFile + " for " + fileObject); // NOI18N
            if (envListener != null)
                fileObject.removeFileChangeListener(envListener);
            envListener = new EnvironmentListener(this);
            fileObject.addFileChangeListener (envListener);

            if (lockAgain) { // refresh lock
                try {
                    fileLock = entry.takeLock ();
                    LOG.fine("changeFile takeLock: " + fileLock + " for " + fileObject); // NOI18N
                } catch (IOException e) {
                    Logger.getLogger(PropertiesEditorSupport.class.getName()).log(Level.WARNING, null, e);
                }
            }
            if (!wasNull) {
                firePropertyChange("expectedTime", null, getTime()); // NOI18N
            }
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Obtains the input stream.
         * @exception IOException if an I/O error occures
         */
        public InputStream inputStream() throws IOException {
            LOG.finer("Environment.inputStream()");                     //NOI18N
            return getFileImpl().getInputStream();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Obtains the output stream.
         * @exception IOException if an I/O error occures
         */
        public OutputStream outputStream() throws IOException {
            LOG.finer("Environment.outputStream()");                    //NOI18N
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = entry.takeLock ();
            }
            LOG.fine("outputStream after takeLock: " + fileLock + " for " + fileObject); // NOI18N
            try {
                return getFileImpl ().getOutputStream (fileLock);
            } catch (IOException fse) {
	        // [pnejedly] just retry once.
		// Ugly workaround for #40552
                if (fileLock == null || !fileLock.isValid()) {
                    fileLock = entry.takeLock ();
                }
                LOG.fine("ugly workaround for #40552: " + fileLock + " for " + fileObject); // NOI18N
                return getFileImpl ().getOutputStream (fileLock);
            }
//            return entry.getFile().getOutputStream(fileLock);
        }

        /**
         * Implements <code>SaveCookie</code> interface. 
         * Invoke the save operation.
         * @throws IOException if the object could not be saved
         */
        public void save() throws IOException {
            LOG.finer("Environment.save()");                            //NOI18N
            // Do saving job. Note it gets editor support, not open support.
            ((PropertiesEditorSupport)findCloneableOpenSupport()).saveThisEntry();
        }

        /** Fires property change.
         * @param name the name of property that changed
         * @param oldValue old value
         * @param newValue new value
         */
        private void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop().firePropertyChange (name, oldValue, newValue);
        }
            
        /** Fires vetoable change.
         * @param name the name of property that changed
         * @param oldValue old value
         * @param newValue new value
         */
        private void fireVetoableChange (String name, Object oldValue, Object newValue) throws PropertyVetoException {
                veto ().fireVetoableChange (name, oldValue, newValue);
        }
            
        /** Lazy getter for property change support. */
        private PropertyChangeSupport prop() {
            synchronized (this) {
                if (propSupp == null) {
                    propSupp = new PropertyChangeSupport (this);
                }
            }
            
            return propSupp;
        }
            
        /** Lazy getter for vetoable support. */
        private VetoableChangeSupport veto() {
            synchronized (this) {
                if (vetoSupp == null) {
                    vetoSupp = new VetoableChangeSupport (this);
                }
            }
            return vetoSupp;
        }
            
        /** Helper method. Adds save cookie to the entry. */
        private void addSaveCookie() {
            LOG.finer("Environment.addSaveCookie(...)");                //NOI18N
            if (entry.getCookie(SaveCookie.class) == null) {
                entry.getCookieSet().add(this);
            }
            //Need to add cookie to DataObject since saveAll use it, and
            //OpenCookie may not be initialized
            PropertiesDataObject dataObject = (PropertiesDataObject) getDataObject();
            dataObject.updateModificationStatus();
            if (dataObject.getCookie(SaveCookie.class) == null){
                dataObject.getCookieSet0().add(this);
            }
        }
            
        /** Helper method. Removes save cookie from the entry. */
        private void removeSaveCookie() {
            LOG.finer("Environment.removeSaveCookie(...)");             //NOI18N
            // remove Save cookie from the entry
            SaveCookie sc = entry.getCookie(SaveCookie.class);
            
            if (sc != null && sc.equals(this)) {
                entry.getCookieSet().remove(this);
            }
            final SaveCookie cookie = this;
            PropertiesRequestProcessor.getInstance().post(new Runnable() {
                public void run() {
                    PropertiesDataObject dataObject = (PropertiesDataObject) getDataObject();
                    dataObject.updateModificationStatus();
                    dataObject.getCookieSet0().remove(cookie);
                }
            });
        }
            
        /** Called from the <code>EnvironmnetListener</code>
         * @param expected is the change expected
         * @param time of the change
         */
        private void fileChanged(boolean expected, long time) {
            LOG.finer("Environment.fileChanged(...)");                  //NOI18N
            if (expected) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (time));
            }
        }

        /** Called from the <code>EnvironmentListener</code>.
         */
        final void fileRenamed () {
            //#151787: Sync timestamp when svn client changes timestamp externally during rename.
            firePropertyChange("expectedTime", null, getTime()); // NOI18N
        }
        
        /** Called from the <code>EnvironmentListener</code>.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        private void fileRemoved() {
            LOG.finer("Environment.fileRemoved() ... ");                //NOI18N
            try {
                fireVetoableChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
            } catch(PropertyVetoException pve) {
                // Ignore it and close anyway. File doesn't exist anymore.
            }
            
            firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    } // End of nested class Environment.


    
    /** Weak listener on file object that notifies the <code>Environment</code> object
     * that a file has been modified. */
    private static final class EnvironmentListener extends FileChangeAdapter {
        
        /** Reference of <code>Environment</code> */
        private Reference<Environment> reference;
        
        /** @param environment <code>Environment<code> to use
         */
        public EnvironmentListener(Environment environment) {
            LOG.finer("new EnvironmentListener(<Environment>)");        //NOI18N
            reference = new WeakReference<Environment>(environment);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            Environment myEnv = this.reference.get();
            if (myEnv != null) {
                myEnv.updateDocumentProperty();
                myEnv.fileRemoved();
            }
//            super.fileDeleted(fe);
        }
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent evt) {
            if (LOG.isLoggable(FINER)) {
                LOG.finer("EnviromentListener.fileChanged(...)");       //NOI18N
                LOG.finer(" - original file: "                          //NOI18N
                          + FileUtil.getFileDisplayName(evt.getFile()));
                LOG.finer(" - current file: "                           //NOI18N
                          + FileUtil.getFileDisplayName((FileObject) evt.getSource()));
            }
            //see #160338
            if (evt.firedFrom(SaveImpl.DEFAULT)) {
                return;
            }
            Environment environment = reference.get();
            if (environment != null) {
                if(!environment.getFileImpl().equals(evt.getFile()) ) {
                    // If the FileObject was changed.
                    // Remove old listener from old FileObject.
                    evt.getFile().removeFileChangeListener(this);
                    // Add new listener to new FileObject.
                    environment.getFileImpl().addFileChangeListener(new EnvironmentListener(environment));
                    return;
                }

                // #16403. See DataEditorSupport.EnvListener.
                if(evt.getFile().isVirtual()) {
                    environment.entry.getFile().removeFileChangeListener(this);
                    // File doesn't exist on disk -> simulate env is invalid,
                    // even the fileObject could be valid, see VCS FS.
                    environment.fileRemoved();
                    environment.entry.getFile().addFileChangeListener(this);
                } else {
                    environment.fileChanged(evt.isExpected(), evt.getTime());
                }
            }
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            Environment myEnv = this.reference.get();
            if (myEnv != null) {
                myEnv.updateDocumentProperty();
                myEnv.fileRenamed();
            }
        }
    } // End of nested class EnvironmentListener.
    
    /** Inner class for opening editor view at a given key. */
    public class PropertiesEditAt implements EditCookie {

        /** Key at which should be pane opened. (Cursor will be at the position of that key). */
        private String key;
        
        
        /** Constructor. */
        PropertiesEditAt(String key) {
            this.key   = key;
        }
        
        
        /** Setter for <code>key</code>. */
        public void setKey(String key) {
            this.key = key;
        }
        
        /** Implementation of <code>EditCookie</code> interface. */
        public void edit() {
            CloneableTopComponent ctc = PropertiesEditorSupport.super.openCloneableTopComponent();
            ctc.requestActive();
            
            PropertiesEditor editor = ctc.getLookup().lookup(PropertiesEditor.class);
            
            Element.ItemElem item = myEntry.getHandler().getStructure().getItem(key);
            if (item != null) {
                int offset = item.getKeyElem().getBounds().getBegin().getOffset();
                if ((editor.getPane() != null) && (editor.getPane().getCaret() != null)) {
                    editor.getPane().getCaret().setDot(offset);
                }
            }
        }
    } // End of inner class PropertiesEditAt.

    
    /** Cloneable top component to hold the editor kit. */
    @Messages("CTL_SourceTabCaption=&Source")
    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/properties/propertiesObject.png",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="properties.source",
        mimeType=PropertiesDataLoader.PROPERTIES_MIME_TYPE,
        position=1
    )        
    public static class PropertiesEditor extends CloneableEditor implements MultiViewElement {
        
        /** Holds the file being edited. */
        protected transient PropertiesFileEntry entry;
        
        /** Listener for entry's save cookie changes. */
        private transient PropertyChangeListener saveCookieLNode;
        
        /** Generated serial version UID. */
        static final long serialVersionUID =-2702087884943509637L;
        private MultiViewElementCallback callback;
        private transient JToolBar bar;
        
        private transient PropertiesEditorLookup peLookup;
        private transient Lookup originalLookup;
        
        /** Constructor for deserialization */
        public PropertiesEditor() {
            super();
        }
        
        /** Creates new editor */
        public PropertiesEditor(Lookup lookup) {
            super(lookup.lookup(PropertiesEditorSupport.class));
            PropertiesEditorSupport support = lookup.lookup(PropertiesEditorSupport.class);
            setActivatedNodes(new Node[] {support.getDataObject().getNodeDelegate()});
        }

        /** Initializes object, used in construction and deserialization. */
        private void initialize(PropertiesFileEntry entry) {
            this.entry = entry;
            
            Node n = entry.getNodeDelegate ();
            setActivatedNodes (new Node[] { n });
            
            updateName();
            
            // entry to the set of listeners
            saveCookieLNode = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Node.PROP_COOKIE.equals(evt.getPropertyName()) ||
                        DataObject.PROP_NAME.equals(evt.getPropertyName())) 
                    {
                        updateName();
                    }
                }
            };
            this.entry.addPropertyChangeListener(WeakListeners.propertyChange(saveCookieLNode, this.entry));
        }

        /**
         * Overrides superclass method. 
         * When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        @Override
        protected boolean closeLast () {
            return super.closeLast(false);
        }

        /** Overrides superclass method. Gets <code>Icon</code>. */
        @Override
        public Image getIcon () {
            PropertiesDataObject propDO = (PropertiesDataObject) getDataObject();
            return ImageUtilities.loadImage(
                    propDO.isMultiLocale()
                    ? "org/netbeans/modules/properties/propertiesLocale.gif"    // NOI18N
                    : "org/netbeans/modules/properties/propertiesObject.png");  // NOI18N
        }
        
        /** Overrides superclass method. Gets help context. */
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Util.HELP_ID_EDITLOCALE);
        }
        
        /** Getter for pane. */
        private JEditorPane getPane() {
            return pane;
        }

        @Override
        public Lookup getLookup() {
            Lookup currentLookup = super.getLookup();
            if (currentLookup != originalLookup || null == peLookup) {
                originalLookup = currentLookup;
                if(peLookup == null) {
                    peLookup = new PropertiesEditorLookup(Lookups.singleton(PropertiesEditor.this));
                }
                peLookup.updateLookups(originalLookup);
            }
            return peLookup;
        }
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }

        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }

        @Override
        public void componentClosed() {
            super.componentClosed();
        }
        
        @Override
        public JComponent getToolbarRepresentation() {
            JToolBar toolBar = bar;
            if (toolBar == null) {
                JEditorPane lPane = getEditorPane();
                if (lPane != null) {
                    Document doc = lPane.getDocument();
                    if (doc instanceof NbDocument.CustomToolbar) {
                        toolBar = ((NbDocument.CustomToolbar)doc).createToolbar(lPane);
                    }
                }
                if (toolBar == null) {
                    toolBar = new JToolBar();
                }
                bar = toolBar;
            }
            return toolBar;
        }

        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            PropertiesEditorSupport editor = (PropertiesEditorSupport) cloneableEditorSupport();
            editor.attachStatusListener();
        }

        @Messages({
            "MSG_SaveModified=File {0} is modified. Save?"
        })
        @Override
        public CloseOperationState canCloseElement() {
            final CloneableEditorSupport sup = getLookup().lookup(CloneableEditorSupport.class);
            Enumeration en = getReference().getComponents();
            if (en.hasMoreElements()) {
                en.nextElement();
                if (en.hasMoreElements()) {
                    // at least two is OK
                    return CloseOperationState.STATE_OK;
                }
            }
            
            PropertiesDataObject dataObject = getDataObject();
            if (dataObject.isModified()) {
                AbstractAction save = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            sup.saveDocument();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(FileUtil.getFileDisplayName(dataObject.getPrimaryFile())));
                return MultiViewFactory.createUnsafeCloseState("editor", save, null);
            } 
            return CloseOperationState.STATE_OK;
        }

        @Override
        public void componentActivated() {
            super.componentActivated();
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
        }

        @Override
        public void componentOpened() {
            super.componentOpened();
        }

        @Override
        public void componentShowing() {
            if (callback != null) {
                updateName();
            }
            super.componentShowing();
        }

        @Override
        public void requestVisible() {
            if (callback != null) {
                callback.requestVisible();
            } else {
                super.requestVisible();
            }
        }

        @Override
        public void requestActive() {
            if (callback != null) {
                callback.requestActive();
            } else {
                super.requestActive();
            }
        }

        @Override
        public void updateName() {
            super.updateName();
            Mutex.EVENT.writeAccess(
                new Runnable() {
                @Override
                    public void run() {
                        if (callback != null) {
                            TopComponent tc = callback.getTopComponent();
                            tc.setHtmlDisplayName(getHtmlDisplayName());
                            tc.setDisplayName(getDisplayName());
                            tc.setName(getName());
                            tc.setToolTipText(getToolTipText());
                        }
                    }
                }
            );
        }

        @Override
        public void open() {
            if (callback != null) {
                callback.requestVisible();
            } else {
                super.open();
            }
        }
        
        private PropertiesDataObject getDataObject() {
            return (PropertiesDataObject) ((PropertiesEditorSupport) cloneableEditorSupport()).getDataObject();
        }
        
    } // End of nested class PropertiesEditor.
    

    /** Inner class. UndoRedo manager which saves a StampFlag
     * for each UndoAbleEdit.
     */
    class UndoRedoStampFlagManager extends UndoRedo.Manager {
        
        /** Hash map of weak reference keys (UndoableEdit's) to their StampFlag's. */
        WeakHashMap<UndoableEdit,StampFlag> stampFlags
                = new WeakHashMap<UndoableEdit,StampFlag>(5);
        
        /** Overrides superclass method. Adds StampFlag to UndoableEdit. */
        @Override
        public synchronized boolean addEdit(UndoableEdit anEdit) {
            stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(),
//                ((PropertiesDataObject)PropertiesEditorSupport.this.myEntry.getDataObject()).getOpenSupport().atomicUndoRedoFlag ));
                PropertiesEditorSupport.this.myEntry.atomicUndoRedoFlag ));
            return super.addEdit(anEdit);
        }
        
        /** Overrides superclass method. Adds StampFlag to UndoableEdit. */
        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), 
//                ((PropertiesDataObject)PropertiesEditorSupport.this.myEntry.getDataObject()).getOpenSupport().atomicUndoRedoFlag ));
                PropertiesEditorSupport.this.myEntry.atomicUndoRedoFlag ));
            return super.replaceEdit(anEdit);
        }
        
        /** Overrides superclass method. Updates time stamp for the edit. */
        @Override
        public synchronized void undo() throws CannotUndoException {
            UndoableEdit anEdit = editToBeUndone();
            if(anEdit != null) {
                Object atomicFlag = stampFlags.get(anEdit).getAtomicFlag(); // atomic flag remains
                super.undo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Overrides superclass method. Updates time stamp for that edit. */
        @Override
        public synchronized void redo() throws CannotRedoException {
            UndoableEdit anEdit = editToBeRedone();
            if(anEdit != null) {
                Object atomicFlag = stampFlags.get(anEdit).getAtomicFlag(); // atomic flag remains
                super.redo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Method which gets time stamp of next Undoable edit to be undone. 
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be undone). */
        public long getTimeStampOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if (nextUndo == null) {
                return 0L;
            } else {
                return stampFlags.get(nextUndo).getTimeStamp();
            }
        }
        
        /** Method which gets time stamp of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public long getTimeStampOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if (nextRedo == null) {
                return 0L;
            } else {
                return stampFlags.get(nextRedo).getTimeStamp();
            }
        }
        
        /** Method which gets atomic flag of next Undoable edit to be undone. 
         * @ return atomic flag in milliseconds or 0 (if don't exit edit to be undone). */
        public Object getAtomicFlagOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if (nextUndo == null) {
                return null;
            } else {
                return (stampFlags.get(nextUndo)).getAtomicFlag();
            }
        }
        
        /** Method which gets atomic flag of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public Object getAtomicFlagOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if (nextRedo == null) {
                return null;
            } else {
                return (stampFlags.get(nextRedo)).getAtomicFlag();
            }
        }
        
    } // End of inner class UndoRedoTimeStampManager.

    /** Simple nested class for storing time stamp and atomic flag used 
     * in <code>UndoRedoStampFlagManager</code>.
     */
    static class StampFlag {
        
        /** Time stamp when was an UndoableEdit (to which is this class mapped via 
         * UndoRedoStampFlagManager,) was created, replaced, undone, or redone. */
        private long timeStamp;
        
        /** Atomic flag. If this object is not null it means that an UndoableEdit ( to which
         * is this class mapped via UndoRedoStampFlagManager,) was created as part of one 
         * action which could consist from more UndoableEdits in differrent editor supports.
         * These Undoable edits are marked with this (i.e. same) object. */
        private Object atomicFlag;
        
        /** Consructor. */
        public StampFlag(long timeStamp, Object atomicFlag) {
            this.timeStamp = timeStamp;
            this.atomicFlag = atomicFlag;            
        }
        
        /** Getter for time stamp. */
        public long getTimeStamp() {
            return timeStamp;
        }
        
        /** Setter for time stamp. */
        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }
        
        /** Getter for atomic flag.
         @ return Returns null if is not linked with more Undoable edits.*/
        public Object getAtomicFlag() {
            return atomicFlag;
        }
    } // End of nested class TimeStamp.

    private static class SaveImpl implements AtomicAction {
        private static final SaveImpl DEFAULT = new SaveImpl(null);
        private final PropertiesEditorSupport des;

        public SaveImpl(PropertiesEditorSupport des) {
            this.des = des;
        }

        public void run() throws IOException {
            if (des.desEnv().isModified() && des.isEnvReadOnly()) {
                IOException e = new IOException("File is read-only: " + ((Environment) des.env).getFileImpl()); // NOI18N
//                UIException.annotateUser(e, null, org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class, "MSG_FileReadOnlySaving", new java.lang.Object[]{((org.netbeans.modules.properties.PropertiesEditorSupport.Environment) des.env).getFileImpl().getNameExt()}), null, null);
                throw e;
            }
            des.superSaveDoc();
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass() == obj.getClass();
        }
    }

    private static final class PropertiesEditorLookup extends ProxyLookup {
        private Lookup initialLookup;
        public PropertiesEditorLookup(Lookup lookup) {
            super(lookup);
            this.initialLookup = lookup;
        }

        public void updateLookups(Lookup additionalLookup) {
            setLookups(new Lookup[] {initialLookup, additionalLookup});
        }
    }

}
