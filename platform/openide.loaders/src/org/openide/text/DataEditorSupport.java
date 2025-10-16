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

package org.openide.text;


import java.awt.EventQueue;
import org.netbeans.modules.openide.loaders.SimpleES;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.CharConversionException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.openide.loaders.AskEditorQuestions;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.netbeans.modules.openide.loaders.UIException;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.xml.XMLUtil;

/**
 * Support for associating an editor and a Swing {@link Document} to a data object.
 * @author Jaroslav Tulach
 */
public class DataEditorSupport extends CloneableEditorSupport {
    /** error manager for CloneableEditorSupport logging and error reporting */
    static final Logger ERR = Logger.getLogger("org.openide.text.DataEditorSupport"); // NOI18N

    /** Which data object we are associated with */
    private final DataObject obj;
    /** listener to associated node's events */
    private NodeListener nodeL;

    /** Editor support for a given data object. The file is taken from the
    * data object and is updated if the object moves or renames itself.
    * @param obj object to work with
    * @param env environment to pass to 
    */
    public DataEditorSupport (DataObject obj, CloneableEditorSupport.Env env) {
        this(obj, new DOEnvLookup (obj), env);
    }
    
    /** Editor support for given data object. The content of editor is taken
     * from the primary file of the data object. The lookup can be anything,
     * but it is recommended to use {@link DataObject#getLookup()}. 
     * 
     * @param obj object to create editor for
     * @param lkp lookup to use. if <code>null</code>, then {@link DataObject#getLookup()} is used.
     * @param env environment responsible for loading/storing the strams
     * @since 7.28
     */
    public DataEditorSupport(DataObject obj, @NullAllowed Lookup lkp, CloneableEditorSupport.Env env) {
        super (env, lkp == null ? obj.getLookup() : lkp);
        this.obj = obj;
    }
    
    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final CloneableEditorSupport.Env desEnv() {
        return (CloneableEditorSupport.Env) env;
    }
    
    /** Factory method to create simple CloneableEditorSupport for a given
     * entry of a given DataObject. The common use inside DataObject looks like
     * this:
     * <pre>
     *  getCookieSet().add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet()));
     * </pre>
     *
     * @param obj the data object
     * @param entry the entry to read and write from
     * @param set cookie set to add remove additional cookies (currently only {@link org.openide.cookies.SaveCookie})
     * @return a subclass of DataEditorSupport that implements at least
     *   {@link org.openide.cookies.OpenCookie}, 
     *   {@link org.openide.cookies.EditCookie}, 
     *   {@link org.openide.cookies.EditorCookie.Observable}, 
     *   {@link org.openide.cookies.PrintCookie}, 
     *   {@link org.openide.cookies.CloseCookie}
     * @since 5.2
     */
    public static CloneableEditorSupport create (DataObject obj, MultiDataObject.Entry entry, org.openide.nodes.CookieSet set) {
        return new SimpleES (obj, entry, set);
    }

    /** Factory method to create a bit more complicated CloneableEditorSupport for a given
     * entry of a given DataObject. The common use inside DataObject looks like
     * this:
     * <pre>{@code
     *  getCookieSet().add((Node.Cookie) DataEditorSupport.create(
     *    this, getPrimaryEntry(), getCookieSet(),
     *    new Callable<Pane>() { 
     *      public Pane call() {
     *        return new {@link CloneableEditor YourSubclassOfCloneableEditor}(support);
     *      }
     *    }
     *  ));
     * }</pre>
     * The method can be used to instantiate <b>multi view</b> editor by returning
     * <a href="@org-netbeans-core-multiview@/org/netbeans/core/api/multiview/MultiViews.html">
     * MultiViews.createCloneableMultiView("text/yourmime", this)</a>.
     *
     * @param obj the data object
     * @param entry the entry to read and write from
     * @param set cookie set to add remove additional cookies (currently only {@link org.openide.cookies.SaveCookie})
     * @param paneFactory callback to create editor(s) for this support (if null {@link CloneableEditor} will be created)
     * @return a subclass of DataEditorSupport that implements at least
     *   {@link org.openide.cookies.OpenCookie}, 
     *   {@link org.openide.cookies.EditCookie}, 
     *   {@link org.openide.cookies.EditorCookie.Observable}, 
     *   {@link org.openide.cookies.PrintCookie}, 
     *   {@link org.openide.cookies.CloseCookie}
     * @since 7.21
     */
    public static CloneableEditorSupport create(
        DataObject obj, MultiDataObject.Entry entry, 
        org.openide.nodes.CookieSet set,
        @NullAllowed Callable<CloneableEditorSupport.Pane> paneFactory
    ) {
        return new SimpleES (obj, entry, set, paneFactory);
    }
    
    /** Getter of the data object that this support is associated with.
    * @return data object passed in constructor
    */
    public final DataObject getDataObject () {
        return obj;
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    @Override
    protected String messageOpening () {
        return NbBundle.getMessage (DataObject.class , "CTL_ObjectOpen", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }
    

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    @Override
    protected String messageOpened () {
        return null;
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    @Override
    protected String messageSave () {
        return NbBundle.getMessage (
            DataObject.class,
            "MSG_SaveFile", // NOI18N
            obj.getPrimaryFile().getNameExt()
        );
    }

    static boolean TABNAMES_HTML = Boolean.parseBoolean(System.getProperty("nb.tabnames.html", "true")); // #47290

    /**
     * Marks up a tab name according to modified and read-only status.
     * Done for subclasses automatically in {@link #messageName} and {@link #messageHtmlName}
     * but useful for other editor-like windows.
     * <p class="nonnormative">Behavior currently varies according to the system property {@code nb.tabnames.html}.</p>
     * @param label incoming label (null not permitted, so take care with {@link Node#getHtmlDisplayName})
     * @param html if true, {@code label} may include HTML markup (with or without initial {@code <html>}), and result may as well
     * @param modified mark up the label as for a document which is modified in memory
     * @param readOnly mark up the label as for a document based on a read-only file
     * @return a possibly marked-up label
     * @since org.openide.loaders 7.7
     */
    public static String annotateName(String label, boolean html, boolean modified, boolean readOnly) {
        Parameters.notNull("original", label);
        if (html && TABNAMES_HTML) {
            if (label.startsWith("<html>")) {
                label = label.substring(6);
            }
            if (modified) {
                label = "<b>" + label + "</b>";
            }
            if (readOnly) {
                label = "<i>" + label + "</i>";
            }
            return "<html>" + label;
        } else {
            if (html && !label.startsWith("<html>")) {
                label = "<html>" + label;
            }
            int version = modified ? (readOnly ? 2 : 1) : (readOnly ? 0 : 3);
            try {
                return NbBundle.getMessage(DataObject.class, "LAB_EditorName", version, label);
            } catch (IllegalArgumentException iae) {
                String pattern = NbBundle.getMessage(DataObject.class, "LAB_EditorName");
                ERR.log(Level.WARNING, "#166035: formatting failed. pattern=" + pattern + ", version=" + version + ", name=" + label, iae);  //NOI18N
                return label;
            }
        }
    }
    
    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    @Override
    protected String messageName () {
        if (! obj.isValid()) {
            return ""; // NOI18N
        }

        return annotateName(obj.getNodeDelegate().getDisplayName(), false, isModified(), !obj.getPrimaryFile().canWrite());
    }
    
    @Override
    protected String messageHtmlName() {
        if (! obj.isValid()) {
            return null;
        }

        String name = obj.getNodeDelegate().getHtmlDisplayName();
        if (name == null) {
            try {
                name = XMLUtil.toElementContent(obj.getNodeDelegate().getDisplayName());
            } catch (CharConversionException ex) {
                return null;
            }
        }

        return annotateName(name, true, isModified(), !obj.getPrimaryFile().canWrite());
    }
        
    @Override
    protected String documentID() {
        if (! obj.isValid()) {
            return ""; // NOI18N
        }
        return obj.getPrimaryFile().getNameExt();
    }

    /**
     * Constructs a tool tip possibly marked up with document modified and read-only status.
     * Done for subclasses automatically in {@link #messageToolTip} but useful for other editor-like windows.
     * <p class="nonnormative">Behavior currently varies according to the system property {@code nb.tabnames.html}.</p>
     * @param file a file representing the tab
     * @param modified mark up the tool tip as for a document which is modified in memory
     * @param readOnly mark up the tool tip as for a document based on a read-only file
     * @return a tool tip
     * @since org.openide.loaders 7.7
     */
    public static String toolTip(FileObject file, boolean modified, boolean readOnly) {
        String tip = FileUtil.getFileDisplayName(file);
        if (TABNAMES_HTML) {
            if (modified) {
                tip += NbBundle.getMessage(DataObject.class, "TIP_editor_modified");
            }
            if (readOnly) {
                tip += NbBundle.getMessage(DataObject.class, "TIP_editor_ro");
            }
        }
        return tip;
    }

    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    @Override
    protected String messageToolTip () {
        // update tooltip
        return toolTip(obj.getPrimaryFile(), isModified(), !obj.getPrimaryFile().canWrite());
    }
    
    /** Computes display name for a line based on the 
     * name of the associated DataObject and the line number.
     *
     * @param line the line object to compute display name for
     * @return display name for the line like "MyFile.java:243"
     *
     * @since 4.3
     */
    @Override
    protected String messageLine (Line line) {
        return NbBundle.getMessage(DataObject.class, "FMT_LineDisplayName2",
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile()), line.getLineNumber() + 1);
    }
    
    
    /** Annotates the editor with icon from the data object and also sets 
     * appropriate selected node. But only in the case the data object is valid.
     * This implementation also listen to display name and icon changes of the
     * node and keeps editor top component up-to-date. If you override this
     * method and not call super, please note that you will have to keep things
     * synchronized yourself. 
     *
     * @param editor the editor that has been created and should be annotated
     */
    @Override
    protected void initializeCloneableEditor (CloneableEditor editor) {
        // Prevention to bug similar to #17134. Don't call getNodeDelegate
        // on invalid data object. Top component should be discarded later.
        if(obj.isValid()) {
            Node ourNode = obj.getNodeDelegate();
            editor.setActivatedNodes (new Node[] { ourNode });
            editor.setIcon(ourNode.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
            NodeListener nl = new DataNodeListener(editor);
            ourNode.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener (nl, ourNode));
            nodeL = nl;
        }
    }

    /** Called when closed all components. Overrides superclass method,
     * also unregisters listening on node delegate. */
    @Override
    protected void notifyClosed() {
        // #27645 All components were closed, unregister weak listener on node.
        nodeL = null;
        
        super.notifyClosed();
    }
    
    /** Let's the super method create the document and also annotates it
    * with Title and StreamDescription properties.
    *
    * @param kit kit to user to create the document
    * @return the document annotated by the properties
    */
    @Override
    protected StyledDocument createStyledDocument (EditorKit kit) {
        StyledDocument doc = super.createStyledDocument (kit);
            
        // set document name property
        doc.putProperty(Document.TitleProperty,
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
        // set dataobject to stream desc property
        doc.putProperty(Document.StreamDescriptionProperty,
            obj
        );
        
        //Report the document into the Timers&Counters window:
        Logger.getLogger("TIMER").log(Level.FINE, "Document", new Object[] {obj.getPrimaryFile(), doc});
        
        return doc;
    }

    /** Checks whether is possible to close support components.
     * Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    @Override
    protected boolean canClose() {
        if(desEnv().isModified() && isEnvReadOnly()) {
            final String fileName = ((Env)env).getFileImpl().getNameExt();
            return AskEditorQuestions.askFileReadOnlyOnClose(fileName);
        }
        
        return super.canClose();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        Charset c = charsets.get(this.getDataObject());
        if (c == null) {
            c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        }
        final FileObject fo = this.getDataObject().getPrimaryFile();
        doc.putProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR, fo.getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR));
        final Reader r;
        if (warnedEncodingFiles.contains(fo)) {
            r = new InputStreamReader (stream, c);
        } else {
            CharsetDecoder decoder = c.newDecoder();
            decoder.reset();
            r = new InputStreamReader (stream, decoder);
        }
        try {
            kit.read(r, doc, 0);
        } catch (CharacterCodingException e) {
            ERR.log(Level.FINE, "Encoding problem using " + c, e); // NOI18N
            doc.remove(0, doc.getLength());
            createAndThrowIncorrectCharsetUQE(fo, c);
        } catch (IllegalStateException e) {
            ERR.log(Level.FINE, "Encoding problem using " + c, e); // NOI18N
            doc.remove(0, doc.getLength());
            createAndThrowIncorrectCharsetUQE(fo, c);
        }
    }

    private static boolean createAndThrowIncorrectCharsetUQE(final FileObject fo, Charset charset) throws UserQuestionException {
        ERR.log(Level.INFO, "Encoding problem using {0} for {1}", new Object[]{charset, fo}); // NOI18N
        throw new UserQuestionException(NbBundle.getMessage(DataObject.class, "MSG_EncodingProblem", charset, fo.getPath())) {
            @Override
            public void confirmed() throws IOException {
                warnedEncodingFiles.add(fo);
            }
        };
    }

    private static Set<FileObject> warnedEncodingFiles = Collections.newSetFromMap(new WeakHashMap<>());

    /** can hold the right charset to be used during save, needed for communication
     * between saveFromKitToStream and saveDocument
     */
    private static Map<DataObject,Charset> charsets = Collections.synchronizedMap(new HashMap<DataObject,Charset>());
    /** Holds counter of charsets cache. Cached value mustn't be removed until couter is zero. */
    private static final Map<DataObject,Integer> cacheCounter = Collections.synchronizedMap(new HashMap<DataObject,Integer>());

    private static int incrementCacheCounter(DataObject tmpObj) {
        synchronized (cacheCounter) {
            Integer count = cacheCounter.get(tmpObj);
            if (count == null) {
                count = 0;
            }
            count++;
            cacheCounter.put(tmpObj, count);
            return count;
        }
    }

    private static synchronized int decrementCacheCounter(DataObject tmpObj) {
        synchronized (cacheCounter) {
            Integer count = cacheCounter.get(tmpObj);
            assert count != null;
            count--;
            if (count == 0) {
                cacheCounter.remove(tmpObj);
            } else {
                cacheCounter.put(tmpObj, count);
            }
            return count;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (doc == null) {
            throw new NullPointerException("Document is null"); // NOI18N
        }
        if (kit == null) {
            throw new NullPointerException("Kit is null"); // NOI18N
        }
        
        Charset c = charsets.get(this.getDataObject());
        if (c == null) {
            c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        }
        FilterOutputStream fos = new FilterOutputStream(stream) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
        Writer w = new OutputStreamWriter (fos, c);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }

    @Override
    public StyledDocument openDocument() throws IOException {
        DataObject tmpObj = getDataObject();
        Charset c = charsets.get(tmpObj);
        if (c == null) {
            c = FileEncodingQuery.getEncoding(tmpObj.getPrimaryFile());
        }
        try {
            charsets.put(tmpObj, c);
            incrementCacheCounter(tmpObj);
            return super.openDocument();
        } finally {
            if (decrementCacheCounter(tmpObj) == 0) {
                charsets.remove(tmpObj);
            }
            ERR.finest("openDocument - charset removed");
        }
    }

    /** Saves document. Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    @Override
    public void saveDocument() throws IOException {
        FileSystem.AtomicAction aa = new SaveImpl(this);
        FileUtil.runAtomicAction(aa);
    }

    final void superSaveDoc() throws IOException {
        super.saveDocument();
    }

    /** Indicates whether the <code>Env</code> is read only. */
    private boolean isEnvReadOnly() {
        CloneableEditorSupport.Env myEnv = desEnv();
        return myEnv instanceof Env && !((Env) myEnv).getFileImpl().canWrite();
    }
    
    /** Needed for EditorSupport */
    final DataObject getDataObjectHack2 () {
        return obj;
    }
    
    /** Accessor for updateTitles.
     */
    final void callUpdateTitles () {
        updateTitles ();
    }
    
    /** Support method that extracts a DataObject from a Line. If the 
     * line is created by a DataEditorSupport then associated DataObject
     * can be accessed by this method.
     *
     * @param l line object 
     * @return data object or null
     *
     * @since 4.3
     */
    public static DataObject findDataObject (Line l) {
        if (l == null) {
            throw new NullPointerException();
        }
        return l.getLookup().lookup(DataObject.class);
    }
    
    /**
     * Save the document under a new file name and/or extension.
     * @param folder New folder to save the DataObject to.
     * @param fileName New file name to save the DataObject to.
     * @throws java.io.IOException If the operation failed
     * @since 6.3
     */
    public void saveAs( FileObject folder, String fileName ) throws IOException {
        if( env instanceof Env ) {
            
            //ask the user for a new file name to save to
            String newExtension = FileUtil.getExtension( fileName );
            
            DataObject newDob = null;
            DataObject currentDob = getDataObject();
            if( !currentDob.isModified() || null == getDocument() ) {
                //the document is not modified on disk, we copy/rename the file
                DataFolder df = DataFolder.findFolder( folder );
                
                FileObject newFile = folder.getFileObject(fileName);
                if( null != newFile ) {
                    //remove the target file if it already exists
                    newFile.delete();
                }
                
                newDob = DataObjectAccessor.DEFAULT.copyRename( currentDob, df, getFileNameNoExtension(fileName), newExtension );
            } else {
                //the document is modified in editor, we need to save the editor kit instead
                FileObject newFile = FileUtil.createData( folder, fileName );
                saveDocumentAs( newFile.getOutputStream() );
                currentDob.setModified( false );
                newDob = DataObject.find( newFile );
            }
            
            if( null != newDob ) {
                //TODO open the document at the position of the original document when #94607 is implemented
                OpenCookie c = newDob.getCookie( OpenCookie.class );
                if( null != c ) {
                    //close the original document
                    close( false );
                    //open the new one
                    c.open();
                }
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

            @Override
            public void run() {
                try {
                    OutputStream os = null;

                    try {
                        os = new BufferedOutputStream( output );
                        saveFromKitToStream( myDoc, os );

                        os.close(); // performs firing
                        os = null;

                    } catch( BadLocationException blex ) {
                        ERR.log( Level.INFO, null, blex );
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
    
    /**
     * Save the document to given stream
     * @param myDoc
     * @param os
     * @throws IOException
     * @throws BadLocationException
     * @since 6.3
     */
    private void saveFromKitToStream( StyledDocument myDoc, OutputStream os ) throws IOException, BadLocationException {
        // Note: there's no new kit getting created, the method actually caches
        // previously created kit and has just a funny name
        final EditorKit kit = createEditorKit();
        
        saveFromKitToStream( myDoc, kit, os );
    }

    /** Environment that connects the data object and the CloneableEditorSupport.
    */
    public abstract static class Env extends OpenSupport.Env implements CloneableEditorSupport.Env {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = -2945098431098324441L;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
        private transient FileObject fileObject;

        /** Lock acquired after the first modification and used in save.
        * Transient => is not serialized.
        * Not private for tests.
        */
        transient FileLock fileLock;
        
        /** did we warned about the size of the file?
         */
        private static transient Set<FileObject> warnedFiles = new HashSet<FileObject>();

        private static transient boolean sentBigFileInfo;

        /** Atomic action used to ignore fileChange event from FileObject.refresh */
        private transient FileSystem.AtomicAction action = null;

        /** Holds read-only state of associated file object. null means the state is unknown. */
        private transient Boolean canWrite;

        /** Constructor.
        * @param obj this support should be associated with
        */
        public Env (DataObject obj) {
            super (obj);
        }
        
        /** Getter for the file to work on.
        * @return the file
        */
        private FileObject getFileImpl () {
            // updates the file if there was a change
            changeFile();
            return fileObject;
        }
        
        /** Getter for file associated with this environment.
        * @return the file input/output operation should be performed on
        */
        protected abstract FileObject getFile ();

        /** Locks the file.
        * @return the lock on the file getFile ()
        * @exception IOException if the file cannot be locked
        */
        protected abstract FileLock takeLock () throws IOException;
                
        /** Method that allows subclasses to notify this environment that
        * the file associated with this support has changed and that 
        * the environment should listen on modifications of different 
        * file object.
        */
        protected final void changeFile () {
            FileObject newFile = getFile ();
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
                    ERR.fine("changeFile releaseLock: " + fileLock + " for " + fileObject); // NOI18N
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
            ERR.fine("changeFile: " + newFile + " for " + fileObject); // NOI18N
            new EnvListener(fileObject, this);

            if (lockAgain) { // refresh lock
                try {
                    fileLock = takeLock ();
                    ERR.fine("changeFile takeLock: " + fileLock + " for " + fileObject); // NOI18N
                } catch (IOException e) {
                    Logger.getLogger(DataEditorSupport.class.getName()).log(Level.WARNING, null, e);
                }
            }
            if (!wasNull) {
                firePropertyChange("expectedTime", null, getTime()); // NOI18N
            }
        }
        
        /**
         * default threshold for big file to warn user (default is 5MB)
         */
        private final transient long BIG_FILE_THRESHOLD_MB = Integer.getInteger("org.openide.text.big.file.size", 5) * 1024 * 1024;
        
        /** Obtains the input stream.
        * @exception IOException if an I/O error occurs
        */
        public InputStream inputStream() throws IOException {
            final FileObject fo = getFileImpl ();
            boolean warned = warnedFiles.contains(fo);
            long size = -1;
            if (!warned && (size = fo.getSize ()) > BIG_FILE_THRESHOLD_MB) {
                throw new ME (size);
            } else if (!sentBigFileInfo && ((size >= 0) ? size : fo.getSize()) > BIG_FILE_THRESHOLD_MB) {
                // warned can contain any file after deserialization
                notifyBigFileLoaded();
            }
            initCanWrite(false);
            InputStream is = getFileImpl ().getInputStream ();
            return is;
        }
        
        /** Obtains the output stream.
        * @exception IOException if an I/O error occurs
        */
        public OutputStream outputStream() throws IOException {
            ERR.fine("outputStream: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("outputStream after takeLock: " + fileLock + " for " + fileObject); // NOI18N
            try {
                return getFileImpl ().getOutputStream (fileLock);
            } catch (IOException fse) {
	        // [pnejedly] just retry once.
		// Ugly workaround for #40552
                if (fileLock == null || !fileLock.isValid()) {
                    fileLock = takeLock ();
                }
                ERR.fine("ugly workaround for #40552: " + fileLock + " for " + fileObject); // NOI18N
                return getFileImpl ().getOutputStream (fileLock);
            }	    
        }
        
        /** The time when the data has been modified
        */
        public Date getTime() {
            // #32777 - refresh file object and return always the actual time
            action = new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    getFileImpl().refresh(false);
                }
            };
            try {
                getFileImpl().getFileSystem().runAtomicAction(action);
            } catch (IOException ex) {
                //Nothing to do here
            }
            
            return getFileImpl ().lastModified ();
        }
        
        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType() {
            return getFileImpl ().getMIMEType ();
        }
        
        /** First of all tries to lock the primary file and
        * if it succeeds it marks the data object modified.
         * <p><b>Note: There is a contract (better saying a curse)
         * that this method has to call {@link #takeLock} method
         * in order to keep working some special filesystem's feature.
         * See <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=28212">issue #28212</a></b>.
        *
        * @exception IOException if the environment cannot be marked modified
        *   (for example when the file is readonly), when such exception
        *   is the support should discard all previous changes
         * @see  org.openide.filesystems.FileObject#isReadOnly
        */
        @Override
        public void markModified() throws java.io.IOException {
            if (EventQueue.isDispatchThread()) {
                class Mark implements Runnable {
                    final AtomicBoolean cancel = new AtomicBoolean();
                    IOException error;
                    
                    Mark(FileObject fo) {
                        error = new IOException("Operation cancelled");
                        Exceptions.attachLocalizedMessage(error, 
                            NbBundle.getMessage(DataObject.class, "MSG_MarkModifiedCancel", fo.getPath())
                        );
                    }
                    
                    @Override
                    public void run() {
                        try {
                            markModifiedImpl(cancel);
                            error = null;
                        } catch (IOException ex) {
                            error = ex;
                        }
                    }
                }
                Mark m = new Mark(fileObject);
                BaseProgressUtils.runOffEventDispatchThread(m, 
                    NbBundle.getMessage(DataObject.class, "MSG_MarkModified", fileObject.getPath()),
                    m.cancel, false, 1000, 3000
                );
                IOException err = m.error;
                if (err != null) {
                    throw err;
                }
            } else {
                markModifiedImpl(null);
            }
        }
        
        private void markModifiedImpl(AtomicBoolean cancel) throws IOException {
            // XXX This shouldn't be here. But it is due to the 'contract',
            // see javadoc to this method.
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("markModified: " + fileLock + " for " + fileObject); // NOI18N
            
            if (!getFileImpl().canWrite()) {
                if(fileLock != null && fileLock.isValid()) {
                    fileLock.releaseLock();
                }
                throw new IOException("File " // NOI18N
                    + getFileImpl().getNameExt() + " is read-only!"); // NOI18N
            }
            if (cancel == null || !cancel.get()) {
                this.getDataObject ().setModified (true);
            }
        }
        
        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        @Override
        public void unmarkModified() {
            ERR.fine("unmarkModified: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
                ERR.fine("releaseLock: " + fileLock + " for " + fileObject); // NOI18N
            }
            
            this.getDataObject ().setModified (false);
        }
        
        /** Called from the EnvListener
        * @param expected is the change expected
        * @param time of the change
        */
        final void fileChanged (FileEvent fe) {
            ERR.fine("fileChanged: " + fe.isExpected() + " for " + fileObject); // NOI18N
            //#155680: We will ignore events generated from FileObject.refresh() in getTime().
            if ((action != null) && fe.firedFrom(action)) {
                return;
            }
            if (fe.isExpected()) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (fe.getTime()));
            }
        }
        
        /** @return true if known canWrite state changed */
        private boolean initCanWrite(boolean refresh) {
            if (canWrite == null || !refresh) {
                canWrite = getFileImpl().canWrite();
                return false;
            }
            boolean oldCanWrite = canWrite;
            canWrite = getFileImpl().canWrite();
            return oldCanWrite != canWrite;
        }

        /** Called from EnvListener if read-only state is externally changed (#129178).
         * @param readOnly true if changed to read-only state, false if changed to read-write
         */
        private void readOnlyRefresh() {
            if (initCanWrite(true)) {
                if (!canWrite && isModified()) {
                    final String fileName = getFileImpl().getNameExt();
                    AskEditorQuestions.notifyChangedToReadOnly(fileName);
                }
                // event is consumed in CloneableEditorSupport
                firePropertyChange("DataEditorSupport.read-only.changing", !canWrite, canWrite);  //NOI18N
            }
        }

        /** Called from the <code>EnvListener</code>.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        final void fileRemoved(boolean canBeVetoed) {
            /* JST: Do not do anything here, as there will be new call from
               the DataObject.markInvalid0
             
            if (canBeVetoed) {
                try {
                    // Causes the 'Save' dialog to show if necessary.
                    fireVetoableChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
                } catch(PropertyVetoException pve) {
                    // ok vetoed, keep the window open, but continue to veto for ever
                    // any subsequent veto messages from the data object
                }
            }
            
            // Closes the components.
            firePropertyChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);            
             */
        }

        /**
         * Called from the <code>EnvListener</code>.
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
        
        /** Called from the <code>EnvListener</code>.
         */
        final void fileRenamed () {
            //#151787: Sync timestamp when svn client changes timestamp externally during rename.
            firePropertyChange("expectedTime", null, getTime()); // NOI18N
        }
        
        private void readObject (ObjectInputStream ois) throws ClassNotFoundException, IOException {
            ois.defaultReadObject ();
            ois.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    warnedFiles.add(getFileImpl());
                }
            }, 0);
        }
        
        private class SaveAsCapableImpl implements SaveAsCapable {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                CloneableOpenSupport cos = Env.super.findCloneableOpenSupport();
                if (cos instanceof DataEditorSupport) {
                    ((DataEditorSupport)cos).saveAs( folder, fileName );
                }
            }
        }
        
        private static void notifyBigFileLoaded() {
            if (!sentBigFileInfo) {
                ERR.info("UIHANDLER_TOO_BIG_FILE_LOADED");
                sentBigFileInfo = true;
            }
        }

        class ME extends org.openide.util.UserQuestionException {
            static final long serialVersionUID = 1L;

            private long size;

            public ME (long size) {
                super ("The file is too big. " + size + " bytes.");
                this.size = size;
            }

            @Override
            public String getLocalizedMessage () {
                Object[] arr = {
                    getFileImpl().getPath (),
                    getFileImpl().getNameExt (), 
                    Long.valueOf(size), // bytes
                    Long.valueOf(size / 1024 + 1), // kilobytes
                    Long.valueOf(size / (1024 * 1024)), // megabytes
                    Long.valueOf(size / (1024 * 1024 * 1024)) // gigabytes
                };
                return NbBundle.getMessage(DataObject.class, "MSG_ObjectIsTooBig", arr);
            }
            
            public void confirmed () {
                warnedFiles.add(getFileImpl());
                notifyBigFileLoaded();
            }
        } // end of ME
    } // end of Env
    
    /** Listener on file object that notifies the Env object
    * that a file has been modified.
    */
    private static final class EnvListener extends FileChangeAdapter {
        /** Reference (Env) */
        private Reference<Env> env;
        
        /** @param env environment to use
        */
        public EnvListener (FileObject listen, Env env) {
            this.env = new java.lang.ref.WeakReference<Env> (env);
            addFileChangeListener(listen);
        }
        
        private void addFileChangeListener(FileObject fo) {
            try {
                fo.getFileSystem().addFileChangeListener(this);
            } catch (FileStateInvalidException ex) {
                ERR.log(Level.INFO, "cannot add listener to " + fo, ex);
            }
        }
        private void removeFileChangeListener(FileObject fo) {
            try {
                fo.getFileSystem().removeFileChangeListener(this);
            } catch (FileStateInvalidException ex) {
                ERR.log(Level.INFO, "cannot remove listener from " + fo, ex);
            }
        }


        /** Handles <code>FileObject</code> deletion event. */
        @Override
        public void fileDeleted(FileEvent fe) {
            Env myEnv = this.env.get();
            FileObject fo = fe.getFile();
            if (myEnv != null) {
                myEnv.updateDocumentProperty();
            }
            if(myEnv == null || myEnv.getFileImpl() == null) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                removeFileChangeListener(fo);
                return;
            }
            if (myEnv.getFileImpl() == fo) {
                removeFileChangeListener(fo);
            
                myEnv.fileRemoved(true);
                addFileChangeListener(fo);
            }
        }
        
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        @Override
        public void fileChanged(FileEvent fe) {
            if (fe.firedFrom(SaveImpl.DEFAULT)) {
                return;
            }

            Env myEnv = this.env.get ();
            if (myEnv == null || myEnv.getFileImpl () == null) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                removeFileChangeListener (fe.getFile ());
                return;
            }
            
            if (myEnv.getFileImpl() != fe.getFile()) {
                return;
            }

            // #16403. Added handling for virtual property of the file.
            if(fe.getFile().isVirtual()) {
                // Remove file event coming as consequence of this change.
                fe.getFile().removeFileChangeListener(this);
                // File doesn't exist on disk -> simulate env is invalid,
                // even the fileObject could be valid, see VCS FS.
                myEnv.fileRemoved(true);
                fe.getFile().addFileChangeListener(this);
            } else {
                myEnv.fileChanged (fe);
            }
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            Env myEnv = this.env.get();
            if (myEnv != null) {
                myEnv.updateDocumentProperty();
                myEnv.fileRenamed();
            }
        }
        
        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {
            Env myEnv = this.env.get ();
            if (myEnv == null || myEnv.getFileImpl () != fae.getFile()) {
                return;
            }
            
            // wait only for event from org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObj.refreshImpl()
            if ("DataEditorSupport.read-only.refresh".equals(fae.getName())) {  //NOI18N
                myEnv.readOnlyRefresh();
            }
        }
    }
    
    /** Listener on node representing associated data object, listens to the
     * property changes of the node and updates state properly
     */
    private final class DataNodeListener extends NodeAdapter {
        /** Associated editor */
        Reference<CloneableEditor> editorRef;
        
        DataNodeListener (CloneableEditor editor) {
            this.editorRef = new WeakReference<CloneableEditor>(editor);
        }
        
        @Override
        public void propertyChange(final PropertyChangeEvent ev) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
            if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                callUpdateTitles();
            }
            if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                CloneableEditor editor;
                if (obj.isValid() && (editor = editorRef.get()) != null) {
                    editor.setIcon(obj.getNodeDelegate().getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
                }
            }
                }
            });
        }
        
    } // end of DataNodeListener

    /** Lookup that holds DataObject, its primary file and updates if that
     * changes.
     */
    private static class DOEnvLookup extends AbstractLookup 
    implements PropertyChangeListener {
        static final long serialVersionUID = 333L;
        
        private DataObject dobj;
        private InstanceContent ic;
        
        public DOEnvLookup (DataObject dobj) {
            this (dobj, new InstanceContent ());
        }
        
        private DOEnvLookup (DataObject dobj, InstanceContent ic) {
            super (ic);
            this.ic = ic;
            this.dobj = dobj;
        	dobj.addPropertyChangeListener(WeakListeners.propertyChange(this, dobj));
     
            updateLookup ();
        }
        
        private void updateLookup() {
            ic.set(Arrays.asList(new Object[] { dobj, dobj.getPrimaryFile() }), null);
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            String propName = ev.getPropertyName();
            if (propName == null || propName.equals(DataObject.PROP_PRIMARY_FILE)) {
                updateLookup();
            }
        }
    }

    private static class SaveImpl implements AtomicAction {
        private static final SaveImpl DEFAULT = new SaveImpl(null);
        private final DataEditorSupport des;

        public SaveImpl(DataEditorSupport des) {
            this.des = des;
        }

        public void run() throws IOException {
            if (des.desEnv().isModified() && des.isEnvReadOnly()) {
                final FileObject fo = ((Env) des.env).getFileImpl();
                throw AskEditorQuestions.throwableIsReadOnly(fo);
            }
            DataObject tmpObj = des.getDataObject();
            Charset c = charsets.get(tmpObj);
            if (c == null) {
                c = FileEncodingQuery.getEncoding(tmpObj.getPrimaryFile());
            }
            try {
                charsets.put(tmpObj, c);
                incrementCacheCounter(tmpObj);
                ERR.finest("SaveImpl - charset put");
                try {
                    des.superSaveDoc();
                } catch (UserQuestionException ex) {
                    if (AskEditorQuestions.askUserQuestionExceptionOnSave(ex.getLocalizedMessage())) {
                        ex.confirmed();
                    }
                }
            } catch (IOException e) {
                UIException.annotateUser(e, null, 
                    org.openide.util.NbBundle.getMessage(
                        org.openide.loaders.DataObject.class, 
                        "MSG_SaveAsFailed", 
                        new java.lang.Object[]{
                            ((org.openide.text.DataEditorSupport.Env) des.env).getFileImpl().getNameExt(), 
                            e.getLocalizedMessage()}),
                    null, new Date());
                throw e;
                
            } finally {
                if (decrementCacheCounter(tmpObj) == 0) {
                    charsets.remove(tmpObj);
                }
            }
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
    
}
