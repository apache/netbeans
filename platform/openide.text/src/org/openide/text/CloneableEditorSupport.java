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

import java.awt.Component;
import java.awt.Container;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

//import org.openide.util.actions.SystemAction;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.UserQuestionException;
import org.openide.windows.*;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.*;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.openide.text.AskEditorQuestions;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.UserCancelException;
import org.openide.util.WeakSet;


/** Support for associating an editor and a Swing {@link Document}.
* Can be assigned as a cookie to any editable data object.
* This class is abstract, so any subclass has to provide implementation
* for abstract method (usually for generating of messages) and also
* provide environment {@link Env} to give this support access to
* input/output streams, mime type and other features of edited object.
*
* <P>
* This class implements methods of the interfaces
* {@link org.openide.cookies.EditorCookie}, {@link org.openide.cookies.OpenCookie},
* {@link org.openide.cookies.EditCookie},
* {@link org.openide.cookies.ViewCookie}, {@link org.openide.cookies.LineCookie},
* {@link org.openide.cookies.CloseCookie}, and {@link org.openide.cookies.PrintCookie}
* but does not implement
* those interfaces. It is up to the subclass to decide which interfaces
* really implement and which not.
* <P>
* This class supports collecting multiple edits into a group which is treated
* as a single edit by undo/redo. Send {@link #BEGIN_COMMIT_GROUP} and
* {@link #END_COMMIT_GROUP} to UndoableEditListener. These must always be paired.
* Send {@link #MARK_COMMIT_GROUP} to commit accumulated edits and to continue
* accumulating.
*
* @author Jaroslav Tulach
*/
public abstract class CloneableEditorSupport extends CloneableOpenSupport {
    
    /** Common name for editor mode. */
    public static final String EDITOR_MODE = "editor"; // NOI18N
    /**
     * Start a group of edits which will be committed as a single edit
     * for purpose of undo/redo.
     * Nesting semantics are that any BEGIN_COMMIT_GROUP and
     * END_COMMIT_GROUP delimits a commit-group, unless the group is
     * empty in which case the begin/end is ignored.
     * While coalescing edits, any undo/redo/save implicitly delimits
     * a commit-group.
     * @since 6.34
     */
    public static final UndoableEdit BEGIN_COMMIT_GROUP = UndoRedoManager.BEGIN_COMMIT_GROUP;
    /** End a group of edits. 
     * @since 6.34
     */
    public static final UndoableEdit END_COMMIT_GROUP = UndoRedoManager.END_COMMIT_GROUP;
    /**
     * Any coalesced edits become a commit-group and a new commit-group
     * is started.
     * @since 6.40
     */
    public static final UndoableEdit MARK_COMMIT_GROUP = UndoRedoManager.MARK_COMMIT_GROUP;

    private static final String PROP_PANE = "CloneableEditorSupport.Pane"; //NOI18N

    /** Used to avoid calling updateTitles from notifyUnmodified when called
     * from doCloseDocument */
    private static final ThreadLocal<Boolean> LOCAL_CLOSE_DOCUMENT = new ThreadLocal<Boolean>();

    DocumentOpenClose openClose;

    /** error manager for CloneableEditorSupport logging and error reporting */
    static final Logger ERR = Logger.getLogger("org.openide.text.CloneableEditorSupport"); // NOI18N

    /** editor kit to work with */
    private EditorKit kit;

    /** Non default MIME type used to editing */
    private String mimeType;

    /** Actions to show in toolbar */

    //    private SystemAction[] actions;

    /** Listener to the document changes and all other changes */
    private Listener listener;

    /** the undo/redo manager to use for this document */
    private UndoRedo.Manager undoRedo;

    /** lines set for this object */
    private Line.Set[] lineSet = new Line.Set[] { null };

    /** Helper variable to prevent multiple cocurrent printing of this
     * instance. */
    private boolean printing;

    /** Lock used for access to <code>printing</code> variable. */
    private final Object LOCK_PRINTING = new Object();

    /** position manager */
    private PositionRef.Manager positionManager;

    /** Listeners for the changing of the state - document in memory X closed. */
    private Set<ChangeListener> listeners;

    /** last selected editor pane. */
    private transient Reference<Pane> lastSelected = null;

    /** The time of the last save to determine the real external modifications */
    private long lastSaveTime;
    /** true when this document is being saved to prevent concurrent saves */
    private transient boolean isSaving;

    /** Whether the reload dialog is currently opened. Prevents poping of multiple
     * reload dialogs if there is more external saves.
     */
    private boolean reloadDialogOpened;

    /** Support for property change listeners*/
    private PropertyChangeSupport propertyChangeSupport;

    /** context of this editor support */
    private Lookup lookup;

    /** Flag whether the document is already modified or not.*/

    // #34728 performance optimization 
    private boolean alreadyModified;

    /**
     * Whether previous or upcoming undo is being undone
     * once the notifyModified() is prohibited.
     * <br>
     * Also set when document is being reloaded.
     */
    private boolean preventModification;
    
    private boolean listeningOnEnv;
    
    private boolean inUserQuestionExceptionHandler;

    /**
     * Reference to LineVector that is used by all Line.Sets created
     * for this CloneableEditorSupport.
     */
    private LineVector lineSetLineVector;

    private boolean annotationsLoaded;
    
    private DocFilter docFilter;
    
    private final Object checkModificationLock = new Object();

    /** Classes that have been warned about overriding asynchronousOpen() */
    private static final Set<Class<?>> warnedClasses = new WeakSet<Class<?>>();
    
    /** Creates new CloneableEditorSupport attached to given environment.
    *
    * @param env environment that is source of all actions around the
    *    data object
    */
    public CloneableEditorSupport(Env env) {
        this(env, Lookup.EMPTY);
    }

    /** Creates new CloneableEditorSupport attached to given environment.
    *
    * @param env environment that is source of all actions around the
    *    data object
    * @param l the context that will be passed to each Line produced
    *    by this support's Line.Set. The line will return it from Line.getLookup
    *    call
    */
    public CloneableEditorSupport(Env env, Lookup l) {
        super(env);
        Parameters.notNull("l", l);
        this.lookup = l;
        openClose = new DocumentOpenClose(this);
    }

    //
    // abstract messages section
    //

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected abstract String messageSave();

    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected abstract String messageName();
    
    /** Constructs message that should be used to name the editor component
     * in html fashion, with possible coloring, text styles etc.
     *
     * May return null if no html name is needed or available.
     *
     * @return html name of the editor component or null
     * @since 6.8
     */
    protected String messageHtmlName() {
        return null;
    }

    /** Constructs the ID used for persistence of opened editors.
     * Should be overridden to return sane ID of the underlying document,
     * like the name of the disk file.
     *
     * @return ID of the document
     * @since 4.24
     */
    protected String documentID() {
        return messageName();
    }

    /** Text to use as tooltip for component.
     *
     * @return text to show to the user
     */
    protected abstract String messageToolTip();

    /** Computes display name for a line produced
     * by this {@link CloneableEditorSupport#getLineSet }. The default
     * implementation reuses messageName and line number of the line.
     *
     * @param line the line object to compute display name for
     * @return display name for the line like "MyFile.java:243"
     *
     * @since 4.3
     */
    protected String messageLine(Line line) {
        return NbBundle.getMessage(Line.class, "FMT_CESLineDisplayName", messageName(), line.getLineNumber() + 1);
    }

    //
    // Section of getter of default objects
    // 

    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final Env cesEnv() {
        return (Env) env;
    }

    /** Getter for the kit that loaded the document.
    */
    final EditorKit cesKit() {
        return createEditorKit(); // Use 'kit' variable or create the kit
    }

    /**
     * Gets the undo redo manager.
     * @return the manager
     */
    protected final synchronized UndoRedo.Manager getUndoRedo() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getUndoRedo();
        }
        
        if (undoRedo == null) {
            UndoRedo.Manager mgr = createUndoRedoManager();
//            if (!(mgr instanceof UndoRedoManager)) {
//                ERR.info("createUndoRedoManager(): ignoring created instance of class " + // NOI18N
//                        mgr.getClass() + " since CloneableEditorSupport requires instance of " + // NOI18N"
//                        UndoRedoManager.class.getName() + "\n"); // NOI18N
//                mgr = new UndoRedoManager(this);
//            }
            undoRedo = mgr;
        }

        return undoRedo;
    }

    /** Provides access to position manager for the document.
    * It maintains a set of positions even the document is in memory
    * or is on the disk.
    *
    * @return position manager
    */
    final synchronized PositionRef.Manager getPositionManager() {
        if (positionManager == null) {
            positionManager = new PositionRef.Manager(this);
        }

        return positionManager;
    }

    void ensureAnnotationsLoaded() {
        if (!annotationsLoaded) {
            /*ERR.log(Level.FINE,"CES.ensureAnnotationsLoaded Enter Asynchronous"
            + " Time:" + System.currentTimeMillis()
            + " Thread:" + Thread.currentThread().getName());*/
            annotationsLoaded = true;

            Line.Set lines = getLineSet();
            for (AnnotationProvider act : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
                act.annotate(lines, lookup);
            }
        }
    }

    /**
     * Controls behavior of method open.
     * If it returns false method open will load document synchronously
     * and process UserQuestionException.
     * If it returns true document will be loaded in CloneableEditor creation ie. asynchronously
     * and UserQuestionException will be processed there. Asynchronous loading is added to avoid
     * blocking AWT thread when method open is called in AWT thread - issue #171713
     *
     * Subclasses should override this method. Warning is logged if subclass does not override this method.
     *
     * @return default implementation returns false to keep original behavior
     * of <code>CloneableEditorSupport.open()</code>
     *
     * @since 6.26
     */
    protected boolean asynchronousOpen() {
        Class<?> clazz = getClass();
        
        if (warnedClasses.add(clazz)) {
            ERR.warning(clazz.getName() + " should override asynchronousOpen()."  //NOI18N
            + " See http://bits.netbeans.org/dev/javadoc/org-openide-text/apichanges.html#CloneableEditorSupport.asynchronousOpen" //NOI18N
            );
        }
        return false;
    }
    
    /** Overrides superclass method, first processes document preparation.
     * @see #prepareDocument */
    @Override
    public void open() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.open();
            return;
        }

        
        if (asynchronousOpen()) {
            super.open();
        } else {
            try {
                //Assign reference to local variable to avoid gc before return
                StyledDocument doc = openDocument();
                super.open();
            } catch (final UserQuestionException e) {
                new UserQuestionExceptionHandler(this, e) {
                    @Override
                    protected void opened(StyledDocument openDoc) {
                        CloneableEditorSupport.super.open();
                    }
                }.runInEDT();
            } catch (IOException e) {
                ERR.log(Level.INFO, null, e);
            }
        }
    }
    
    //
    // EditorCookie.Observable implementation
    // 

    /** Add a PropertyChangeListener to the listener list.
     * See {@link org.openide.cookies.EditorCookie.Observable}.
     * @param l  the PropertyChangeListener to be added
     * @since 3.40
     */
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    /** Remove a PropertyChangeListener from the listener list.
     * See {@link org.openide.cookies.EditorCookie.Observable}.
     * @param l the PropertyChangeListener to be removed
     * @since 3.40
     */
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    /** Report a bound property update to any registered listeners.
     * @param propertyName the programmatic name of the property that was changed.
     * @param oldValue rhe old value of the property.
     * @param newValue the new value of the property.
     * @since 3.40
     */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    private synchronized PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }

        return propertyChangeSupport;
    }

    //
    // EditorCookie implementation
    // 
    // editor cookie .......................................................................

    /** Load the document into memory. This is done
    * in different thread. A task for the thread is returned
    * so anyone may test whether the loading has been finished
    * or is still in process.
    *
    * @return task for control over loading
    */
    public Task prepareDocument() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.prepareDocument();
        }

        return openClose.openTask();
    }

    final void addDocListener(Document d) {
        if (Boolean.TRUE.equals(d.getProperty("supportsModificationListener"))) { // NOI18N
            d.putProperty("modificationListener", getListener()); // NOI18N
        }

        if (d instanceof AbstractDocument) {
            AbstractDocument aDoc = (AbstractDocument) d;
            DocumentFilter origFilter = aDoc.getDocumentFilter();
            docFilter = new DocFilter(origFilter);
            aDoc.setDocumentFilter(docFilter);
        } else { // Put property for non-AD
            DocumentFilter origFilter = (DocumentFilter) d.getProperty(DocumentFilter.class);
            docFilter = new DocFilter(origFilter);
            d.putProperty(DocumentFilter.class, docFilter);
        }
        d.addDocumentListener(getListener());
    }

    final void removeDocListener(Document d) {
        if (Boolean.TRUE.equals(d.getProperty("supportsModificationListener"))) { // NOI18N
            d.putProperty("modificationListener", null); // NOI18N
        }

        if (docFilter != null) {
            if (d instanceof AbstractDocument) {
                AbstractDocument aDoc = (AbstractDocument) d;
                aDoc.setDocumentFilter(docFilter.origFilter);
            } else { // Put property for non-AD
                d.putProperty(DocumentFilter.class, docFilter.origFilter);
            }
            docFilter = null;
        }
        d.removeDocumentListener(getListener());
    }

    /** Get the document associated with this cookie.
    * It is an instance of Swing's {@link StyledDocument} but it should
    * also understand the NetBeans {@link NbDocument#GUARDED} to
    * prevent certain lines from being edited by the user.
    * <P>
    * If the document is not loaded the method blocks until
    * it is.
    * 
    * <p>Method will throw {@link org.openide.util.UserQuestionException} exception
    * if file size is too big. This exception could be caught and 
    * its method {@link org.openide.util.UserQuestionException#confirmed} 
    * can be used for confirmation. You need to call {@link #openDocument}}
    * one more time after confirmation.
    *
    * @return the styled document for this cookie that
    *   understands the guarded attribute
    * @exception IOException if the document could not be loaded
    */
    public StyledDocument openDocument() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.openDocument();
        }
        return openClose.open();
    }

    /** Get the document. This method may be called before the document initialization
     * (<code>prepareTask</code>)
     * has been completed, in such a case the document must not be modified.
     * @return document or <code>null</code> if it is not yet loaded
     */
    public StyledDocument getDocument() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getDocument();
        }
        return openClose.getDocument();
    }

    /** Test whether the document is modified.
    * @return <code>true</code> if the document is in memory and is modified;
    *   otherwise <code>false</code>
    */
    public boolean isModified() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isModified();
        }
        return cesEnv().isModified();
    }

    /** Save the document in this thread.
    * Create 'orig' document for the case that the save would fail.
    * @exception IOException on I/O error
    */
    public void saveDocument() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        final boolean log = ERR.isLoggable(Level.FINE);
        if (log) {
            ERR.fine(documentID() + ": saveDocument() started."); // NOI18N
        }
        if (redirect != null) {
            if (log) {
                ERR.fine("  redirect to " + redirect.documentID()); // NOI18N
            }
            redirect.saveDocument();
            return;
        }
        // #17714: Don't try to save unmodified doc.
        if (!cesEnv().isModified()) {
            if (log) {
                ERR.fine(documentID() + "  No save performed because cesEnv().isModified() == false"); // NOI18N
            }
            return;
        }
        final StyledDocument myDoc = getDocument();
        if (myDoc == null) {
            if (log) {
                ERR.fine(documentID() + "  No save performed because getDocument() == null"); // NOI18N
            }
            return;
        }
        synchronized (this) {
            while (isSaving) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ERR.log(Level.INFO, null, ex);
                }
            }
            isSaving = true;
        }
        try {
            saveDocumentImpl(myDoc, log);
        } finally {
            synchronized (this) {
                isSaving = false;
                notifyAll();
            }
        }
    }
    
    private void saveDocumentImpl(final StyledDocument myDoc, final boolean log) throws IOException {
        long prevLST = lastSaveTime;
        if (prevLST != -1) {
            final long externalMod = cesEnv().getTime().getTime();
            if (externalMod > prevLST) {
                if (log) {
                    ERR.fine(documentID() + ":  externalMod=" + externalMod + // NOI18N
                            " > prevLST=" + prevLST + " => throw new UserQuestionException()"); // NOI18N
                }
                throw new UserQuestionException(mimeType) {
                    @Override
                    public String getLocalizedMessage() {
                        return NbBundle.getMessage(
                            CloneableEditorSupport.class,
                            "FMT_External_change_write",
                            myDoc.getProperty(Document.TitleProperty)
                        );
                    }

                    @Override
                    public void confirmed() throws IOException {
                        setLastSaveTime(externalMod);
                        saveDocument();
                    }
                };
            }
        }

        class MemoryOutputStream extends ByteArrayOutputStream {

            public MemoryOutputStream(int size) {
                super(size);
            }

            @Override
            public void writeTo(OutputStream os) throws IOException {
                os.write(buf, 0, count);
            }

        }

        // Perform the save and possibly run on-save actions first.
        // Due to consistency of UndoRedoManager both save actions and actual save
        // (reading doc's contents) should be done under single runAtomic().
        final MemoryOutputStream[] memoryOutputStream = new MemoryOutputStream[1];
        final IOException[] ioException = new IOException[1];
        final boolean[] onSaveTasksStarted = new boolean[1];
        Runnable saveToMemory = new Runnable() {
            @Override
            public void run() {
                try {
                    UndoRedo.Manager urm = getUndoRedo();
                    if (urm instanceof UndoRedoManager) {
                        UndoRedoManager urManager = (UndoRedoManager) urm;
                        if (onSaveTasksStarted[0]) {
                            urManager.endOnSaveTasks();
                        }
                        urManager.markSavepoint();
                    }

                    // Alloc 10% for non-single byte chars
                    int byteArrayAllocSize = myDoc.getLength() * 11 / 10;
                    memoryOutputStream[0] = new MemoryOutputStream(byteArrayAllocSize);
                    EditorKit editorKit = createEditorKit();
                    saveFromKitToStream(myDoc, editorKit, memoryOutputStream[0]);

                    // update cached info about lines
                    updateLineSet(true);

                    if (log) {
                        ERR.fine(documentID() + ": Saved " + memoryOutputStream[0].size() + // NOI18N
                                " bytes to memory output stream."); // NOI18N
                    }
                } catch (BadLocationException blex) {
                    Exceptions.printStackTrace(blex);
                } catch (IOException ex) {
                    ioException[0] = ex;
                }
            }
        };

        Runnable beforeSaveRunnable = (Runnable) myDoc.getProperty("beforeSaveRunnable");
        if (beforeSaveRunnable != null) {
            // Create runnable that marks next edit fired from document as save actions.
            // This assumes that before save tasks will run in a single atomic edit.
            // At the end of the edit an actual save task will be done.
            Runnable beforeSaveStart = new Runnable() {
                @Override
                public void run() {
                    UndoRedo.Manager urm = getUndoRedo();
                    if (urm instanceof UndoRedoManager) {
                        ((UndoRedoManager) undoRedo).startOnSaveTasks();
                        onSaveTasksStarted[0] = true;
                    }
                }
            };
            // Property to be run by beforeSaveRunnable before actual save actions
            myDoc.putProperty("beforeSaveStart", beforeSaveStart);
            // Property to be run by beforeSaveRunnable after actual save actions
            myDoc.putProperty("beforeSaveEnd", saveToMemory);
            // Perform beforeSaveStart then before-save-tasks then beforeSaveEnd under atomic lock
            beforeSaveRunnable.run();
            
            // Fallback in case the document would not process "beforeSaveStart" and "beforeSaveEnd" runnables
            if (memoryOutputStream[0] == null) {
                myDoc.render(saveToMemory);
            }

        } else { // No on-save tasks
            myDoc.render(saveToMemory); // Run under doc's readlock
        }
        if (ioException[0] != null) {
            if (log) {
                ERR.log(Level.FINE, documentID() + ": Save broken due to IOException", ioException[0]); // NOI18N
            }
            throw ioException[0];
        }

        OutputStream os = null;
        long oldSaveTime = lastSaveTime;
        try {
            setLastSaveTime(-1);
            os = cesEnv().outputStream();
            memoryOutputStream[0].writeTo(os);
            os.close(); // performs firing
            os = null;
            myDoc.render(new Runnable() {
                @Override
                public void run() {
                    UndoRedo.Manager urm = getUndoRedo();
                    // Compare whether the savepoint edit is still the edit-to-be-undone
                    boolean unmodify = false;
                    if (urm instanceof UndoRedoManager) {
                        // If not on savepoint then do not mark as unmodified
                        if (((UndoRedoManager)urm).isAtSavepoint()) {
                            unmodify = true;
                        }
                    } else {
                        unmodify = true;
                    }
                    if (unmodify) {
                        callNotifyUnmodified();
                    }
                }
            });

            // remember time of last save
            if (log) {
                ERR.fine(documentID() + ": Save to file OK, oldSaveTime: " + oldSaveTime + // NOI18N
                        ", " + new Date(oldSaveTime)); // NOI18N
            }
            // #149069 - Cannot use System.currentTimeMillis()
            // because there can be a delay between closing stream
            // and setting file modification time by OS.
            setLastSaveTime(cesEnv().getTime().getTime());
        } finally {
            if (lastSaveTime == -1) { // restore for unsuccessful save
                if (log) {
                    ERR.fine(documentID() + ": Save failed (lastSaveTime == -1) restoring old save time."); // NOI18N
                }
                setLastSaveTime(oldSaveTime);
                callNotifyModified(); // Another save attempt needed
            }

            if (os != null) { // try to close if not yet done
                os.close();
            }
        }
    }

    /**
     * Gets editor panes opened by this support.
     * Can be called from AWT event thread only.
     *
     * @return a non-empty array of panes, or null
     * @see EditorCookie#getOpenedPanes
     */
    public JEditorPane[] getOpenedPanes() {
        // expected in AWT only
        assert Mutex.EVENT.isReadAccess()
                : "CloneableEditorSupport.getOpenedPanes() must be called from AWT thread only"; // NOI18N
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getOpenedPanes();
        }
        
        LinkedList<JEditorPane> ll = new LinkedList<JEditorPane>();
        Enumeration<CloneableTopComponent> en = allEditors.getComponents();
        
        Pane last = getLastSelected();
        while (en.hasMoreElements()) {
            CloneableTopComponent ctc = en.nextElement();
            Pane ed = (Pane) ctc.getClientProperty(PROP_PANE);
            
            if ((ed == null) && ctc instanceof Pane) {
                ed = (Pane) ctc;
            }
            
            if (ed != null) {
                // #23491: pane could be still null, not yet shown component.
                // [PENDING] Right solution? TopComponent opened, but pane not.
                JEditorPane p = ed.getEditorPane();
                
                if (p == null) {
                    continue;
                }
                
                if ((last == ed) ||
                    ((last instanceof Component) && (ed instanceof Container)
                     && ((Container) ed).isAncestorOf((Component) last))) {
                    ll.addFirst(p);
                } else {
                    ll.add(p);
                }
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }

        return ll.isEmpty() ? null : ll.toArray(new JEditorPane[0]);
    }

    /**
     * Gets recently selected editor pane opened by this support
     * Can be called from AWT event thread only. It is nonblocking. It returns either pane
     * if pane intialization is finished or null if initialization is still in progress.
     *
     * @return pane or null
     *
     */
    JEditorPane getRecentPane () {
        // expected in AWT only
        assert Mutex.EVENT.isReadAccess()
                : "CloneableEditorSupport.getRecentPane must be called from AWT thread only"; // NOI18N
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getRecentPane();
        }
        
        Enumeration<CloneableTopComponent> en = allEditors.getComponents();
        
        Pane last = getLastSelected();
        while (en.hasMoreElements()) {
            CloneableTopComponent ctc = en.nextElement();
            Pane ed = (Pane) ctc.getClientProperty(PROP_PANE);
            
            if ((ed == null) && ctc instanceof Pane) {
                ed = (Pane) ctc;
            }
            
            if (ed != null) {
                JEditorPane p = null;
                if ((last == ed) ||
                    ((last instanceof Component) && (ed instanceof Container)
                     && ((Container) ed).isAncestorOf((Component) last))) {
                    if (ed instanceof CloneableEditor) {
                        if (((CloneableEditor) ed).isEditorPaneReady()) {
                            p = ed.getEditorPane();
                        }
                    } if (last instanceof CloneableEditor) {
                        if (((CloneableEditor) last).isEditorPaneReady()) {
                            p = ed.getEditorPane();
                        }
                    } else {
                        p = ed.getEditorPane();
                    }
                }
                if (p != null) {
                    return p;
                }
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }

        return null;
    }

    @Override
    protected void afterRedirect(CloneableOpenSupport redirectedTo) {
        super.afterRedirect(redirectedTo);
        // synchronize field from redirected instance, i.e. for correct getOpenedPanes answers
        if (redirectedTo instanceof CloneableEditorSupport) {
            CloneableEditorSupport other = ((CloneableEditorSupport)redirectedTo);
            this.lastSelected = other.lastSelected;
            this.openClose = other.openClose;
            this.lineSet = other.lineSet;
        }
        // notify EditorCookie.Observable listeners if any
        if (propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        }
    }
    
    /** Returns the lastly selected Pane or null
     */
    final Pane getLastSelected() {
        return (lastSelected == null) ? null : lastSelected.get();
    }

    final void setLastSelected(Pane lastSelected) {
        this.lastSelected = new WeakReference<>(lastSelected);
    }

    //
    // LineSet interface impl
    //

    /** Get the line set for all paragraphs in the document.
    * @return positions of all paragraphs on last save
    */
    public Line.Set getLineSet() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getLineSet();
        }
        return updateLineSet(false);
    }

    /**
     * Lazily creates or finds line vector for internal use.
     */
    final LineVector findLineVector() {
        // any lock not hold for too much time will do as we do not 
        // call outside in the sync block
        synchronized (LOCK_PRINTING) {
            if (lineSetLineVector != null) {
                return lineSetLineVector;
            }

            lineSetLineVector = new LineVector();

            return lineSetLineVector;
        }
    }

    //
    // Print interface
    //

    /** A printing implementation suitable for {@link org.openide.cookies.PrintCookie}. */
    public void print() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.print();
            return;
        }
        // XXX should this run synch? can be slow for an enormous doc
        synchronized (LOCK_PRINTING) {
            if (printing) {
                return;
            }

            printing = true;
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            Object o = NbDocument.findPageable(openDocument());

            if (o instanceof Pageable) {
                job.setPageable((Pageable) o);
            } else {
                PageFormat pf = PrintPreferences.getPageFormat(job);
                job.setPrintable((Printable) o, pf);
            }

            if (job.printDialog()) {
                job.print();
            }
        } catch (FileNotFoundException e) {
            notifyProblem(e, "CTL_Bad_File"); // NOI18N
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (PrinterAbortException e) { // user exception
            notifyProblem(e, "CTL_Printer_Abort"); // NOI18N
        }catch (PrinterException e) {
            notifyProblem(e, "EXC_Printer_Problem"); // NOI18N
        } finally {
            synchronized (LOCK_PRINTING) {
                printing = false;
            }
        }
    }

    private static void notifyProblem(Exception e, String key) {
        String msg = NbBundle.getMessage(CloneableEditorSupport.class, key, e.getLocalizedMessage());
        Exceptions.attachLocalizedMessage(e, msg);
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(e));
    }

    //
    // Methods overriden from CloneableOpenSupport
    // 

    /** Prepares document, creates and initializes
     * new <code>CloneableEditor</code> component.
     * Typically do not override this method.
     * For creating your own <code>CloneableEditor</code> type component
     * override {@link #createCloneableEditor} method.
     *
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableTopComponent createCloneableTopComponent() {
        // initializes the document if not initialized
        prepareDocument();

        Pane pane = createPane();
        pane.getComponent().putClientProperty(PROP_PANE, pane);

        return pane.getComponent();
    }

    /** Creates and initializes
     * new <code>CloneableEditor</code> component.
     * Typically do not override this method (unless you are dealing with 
     * <a href="@org-netbeans-core-multiview@/overview-summary.html">multiviews</a>).
     * For creating your own <code>CloneableEditor</code> type component
     * override {@link #createCloneableEditor} method.
     *
     * @return the {@link Pane} for this support
     */
    protected Pane createPane() {
        CloneableEditor ed = createCloneableEditor();
        initializeCloneableEditor(ed);

        return ed;
    }
    
    /**
     * Wraps the editor component in a custom component, allowing for creating
     * more complicated user interfaces which contain the editor UI in 
     * an arbitrary place. 
     *
     * <p>The default implementation merely returns the passed 
     * <code>editorComponent</code> parameter.</p> 
     *
     * @param editorComponent the component containing the editor 
     *        (usually not the JEditorPane, but some its ancestor).
     * 
     * @return a component containing <code>editorComponent</code> or
     *         <code>editorComponent</code> itself.
     *
     * @since 6.3
     */
    protected Component wrapEditorComponent(Component editorComponent) {
        return editorComponent;
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    @Override
    protected boolean canClose() {
        if (cesEnv().isModified()) {

			class SafeAWTAccess implements Runnable {
				boolean running;
				boolean finished;
				int ret;
				
				public void run() {
					synchronized (this) {
						running = true;
						notifyAll();
					}
					
					try {
						ret = canCloseImpl();
					} finally {					
						synchronized (this) {
							finished = true;
							notifyAll();
						}
					}
				}
				
				
				
				public synchronized void waitForResult() throws InterruptedException {
					if (!running) {
						wait(10000);
					}
					if (!running) {
						throw new InterruptedException("Waiting 10s for AWT and nothing! Exiting to prevent deadlock"); // NOI18N
					}
					
					while (!finished) {
						wait();
					}
				}
			}
			
			
			SafeAWTAccess safe = new SafeAWTAccess();
            if (Mutex.EVENT.isReadAccess()) {
                safe.run(); 
            } else {
                // safe.run only blocks for a certain time, unlike Mutex.EVENT.readAccess().
                Mutex.EVENT.postReadRequest(safe::run);
                try {
                    safe.waitForResult();
                } catch (InterruptedException ex) {
                    ERR.log(Level.INFO, null, ex);
                    return false;
                }
            }
			
            if (safe.ret == 0) {
                return false;
            }

            if (safe.ret == 1) {
                try {
                    saveDocument();
                } catch (UserCancelException uce) {
                    return false;
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);

                    return false;
                }
            }
        }

        return true;
    }
	
	/** @return 0 => cannot close, -1 can close and do not save, 1 can close and save */
	private int canCloseImpl() {
		String msg = messageSave();

		ResourceBundle bundle = NbBundle.getBundle(CloneableEditorSupport.class);

		JButton saveOption = new JButton(bundle.getString("CTL_Save")); // NOI18N
		saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
		saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N

		JButton discardOption = new JButton(bundle.getString("CTL_Discard")); // NOI18N
		discardOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
		discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
		discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt(0)); // NOI18N

		NotifyDescriptor nd = new NotifyDescriptor(
				msg, bundle.getString("LBL_SaveFile_Title"), NotifyDescriptor.YES_NO_CANCEL_OPTION,
				NotifyDescriptor.QUESTION_MESSAGE,
				new Object[] { saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION }, saveOption
			);

		Object ret = DialogDisplayer.getDefault().notify(nd);

		if (NotifyDescriptor.CANCEL_OPTION.equals(ret) || NotifyDescriptor.CLOSED_OPTION.equals(ret)) {
			return 0;
		}

		if (saveOption.equals(ret)) {
			return 1;
		} else {
			return -1;
		}
	}

    //
    // public methods provided by this class
    //

    /** Test whether the document is in memory, or whether loading is still in progress.
    * @return <code>true</code> if document is loaded
    */
    public boolean isDocumentLoaded() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isDocumentLoaded();
        }
        return openClose.isDocumentLoadedOrLoading();
    }
    
    /** Test whether the document is ready.
    * @return <code>true</code> if document is ready
    */
    boolean isDocumentReady() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isDocumentReady();
        }
        return openClose.isDocumentOpened();
    }
    
    /**
    * Set the MIME type for the document.
    * @param s the new MIME type
    */
    public void setMIMEType(String s) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this, true);
        if (redirect != null) {
            redirect.setMIMEType(s);
            return;
        }
        mimeType = s;
    }

    /** Adds a listener for status changes. An event is fired
    * when the document is moved or removed from memory.
    * @param l new listener
    * @deprecated Deprecated since 3.40. Use {@link #addPropertyChangeListener} instead.
    * See also {@link org.openide.cookies.EditorCookie.Observable}.
    */
    @Deprecated
    public synchronized void addChangeListener(ChangeListener l) {
        if (listeners == null) {
            listeners = new HashSet<ChangeListener>(8);
        }

        listeners.add(l);
    }

    /** Removes a listener for status changes.
     * @param l listener to remove
    * @deprecated Deprecated since 3.40. Use {@link #removePropertyChangeListener} instead.
    * See also {@link org.openide.cookies.EditorCookie.Observable}.
    */
    @Deprecated
    public synchronized void removeChangeListener(ChangeListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    // Position management methods

    /** Create a position reference for the given offset.
    * The position moves as the document is modified and
    * reacts to closing and opening of the document.
    *
    * @param offset the offset to create position at
    * @param bias the Position.Bias for new creating position.
    * @return position reference for that offset
    */
    public final PositionRef createPositionRef(int offset, Position.Bias bias) {
        return new PositionRef(getPositionManager(), offset, bias);
    }

    //
    // Methods that can be overriden by subclasses
    //

    /** Allows subclasses to create their own version
     * of <code>CloneableEditor</code> component.
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new CloneableEditor(this);
    }

    /** Initialize the editor. This method is called after the editor component
     * is deserialized and also when the component is created. It allows
     * the subclasses to annotate the component with icon, selected nodes, etc.
     *
     * @param editor the editor that has been created and should be annotated
     */
    protected void initializeCloneableEditor(CloneableEditor editor) {
    }

    /** Create an undo/redo manager.
    * This manager is then attached to the document, and listens to
    * all changes made in it.
    * <P>
    * The default implementation uses improved <code>UndoRedo.Manager</code>,
    * with support for various extensions (including {@link #BEGIN_COMMIT_GROUP}
    * and {@link #END_COMMIT_GROUP}). It is not wise to override this
    * method without delegating to <code>super.createUndoRedoManager</code>.
    *
    * @return the undo/redo manager
    */
    protected UndoRedo.Manager createUndoRedoManager() {
        return new UndoRedoManager(this);
    }

    /** Returns an InputStream which reads the current data from this editor, taking into
     * account the encoding of the file. The returned InputStream will be useful for
     * example when passing the file to an external compiler or other tool, which
     * expects an input stream and which deals with encoding internally.<br>
     *
     * See also {@link #saveFromKitToStream}.
     *
     * @return input stream for the file. If the file is open in the editor (and possibly modified),
     * then the returned <code>InputStream</code> will contain the same data as if the file
     * was written out to the {@link CloneableEditorSupport.Env} (usually disk). So it will contain
     * guarded block markers etc. If the document is not loaded,
     * then the <code>InputStream</code> will be taken from the {@link CloneableEditorSupport.Env}.
     *
     * @throws IOException if saving the document to a virtual stream or other IO operation fails
     * @since 4.7
     */
    public InputStream getInputStream() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getInputStream();
        }
        // Implementation note
        // Piped stream will not work, as we are in the same thread
        // Doing this in a different thread would need to lock the document for
        // reading through doc.render() while this stream is open, which may be unacceptable
        // So we copy the document in memory
        StyledDocument tmpDoc = getDocument();

        if (tmpDoc == null) {
            return cesEnv().inputStream();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            saveFromKitToStream(tmpDoc, createEditorKit(), baos);
        } catch (BadLocationException e) {
            //assert false : e;
            // should not happen
            ERR.log(Level.INFO, null, e);
            throw (IllegalStateException) new IllegalStateException(e.getMessage()).initCause(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Actually write file data to an output stream from an editor kit's document.
     * Called during a file save by {@link #saveDocument}.
     * <p>The default implementation just calls {@link EditorKit#write(OutputStream, Document, int, int) EditorKit.write(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to write from
     * @param kit the associated editor kit
     * @param stream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
    throws IOException, BadLocationException {
        kit.write(stream, doc, 0, doc.getLength());
    }

    /**
     * Actually read file data into an editor kit's document from an input stream.
     * Called during a file load by {@link #prepareDocument}.
     * <p>The default implementation just calls {@link EditorKit#read(InputStream, Document, int) EditorKit.read(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to read into
     * @param stream the open stream to read from
     * @param kit the associated editor kit
     * @throws IOException if there was a problem reading the file
     * @throws BadLocationException should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
    throws IOException, BadLocationException {
        kit.read(stream, doc, 0);
    }

    /**
     * Reload the document in response to external modification.
     * @return task that reloads the document or an empty task if there's currently
     *  no reload scheduled.
     */
    protected Task reloadDocument() {
        return openClose.reloadTask();
    }

    /**
     * Gets an <code>EditorKit</code> from Netbeans registry. The method looks
     * in the <code>MimeLookup</code> for <code>EditorKit</code>s registered for
     * the mime-path passed in and returns the first one it finds. If there is
     * no <code>EditorKit</code> registered for the mime-path it will fall back
     * to the 'text/plain' <code>EditorKit</code> and eventually to its own
     * default kit.
     * 
     * <div class="nonnormative">
     * <p>A mime-path is a concatenation of one or more mime-types allowing to
     * address fragments of text with a different mime-type than the mime-type
     * of a document that contains those fragments. As an example you can use
     * a JSP page containing a java scriplet. The JSP page is a document of
     * 'text/x-jsp' mime-type, while the mime-type of the java scriplet is 'text/x-java'.
     * When accessing settings or services such as an 'EditorKit' for java scriplets
     * embedded in a JSP page the scriplet's mime-path 'text/x-jsp/text/x-java'
     * should be used.
     * </p>
     * <p>If you are trying to get an 'EditorKit' for the whole document you can
     * simply pass in the document's mime-type (e.g. 'text/x-java'). For the main
     * document its mime-type and mime-path are the same.
     * </div>
     *
     * @param mimePath    The mime-path to find an <code>EditorKit</code> for.
     *
     * @return The <code>EditorKit</code> implementation registered for the given mime-path.
     * @see org.netbeans.api.editor.mimelookup.MimeLookup
     * @since org.openide.text 6.12
     */
    public static EditorKit getEditorKit(String mimePath) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        EditorKit kit = lookup.lookup(EditorKit.class);
        
        if (kit == null) {
            // Try 'text/plain'
            lookup = MimeLookup.getLookup(MimePath.parse("text/plain"));
            kit = lookup.lookup(EditorKit.class);
        }
        
        // Don't use the prototype instance straightaway
        return kit != null ? (EditorKit) kit.clone() : new PlainEditorKit();
    }
    
    /** Creates editor kit for this source.
    * @return editor kit
    */
    protected EditorKit createEditorKit() {
        if (kit != null) {
            return kit;
        }

        if (mimeType != null) {
            kit = getEditorKit(mimeType);
        } else {
            String defaultMIMEType = cesEnv().getMimeType();
            kit = getEditorKit(defaultMIMEType);
        }

        return kit;
    }

    /** Method that can be overriden by children to create empty
    * styled document or attach additional document properties to it.
    *
    * @param kit the kit to use
    * @return styled document to use
    */
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument sd = createNetBeansDocument(kit.createDefaultDocument());
        sd.putProperty("mimeType", (mimeType != null) ? mimeType : cesEnv().getMimeType()); // NOI18N

        return sd;
    }

    /** Notification method called when the document become unmodified.
    * Called after save or after reload of document.
    * <P>
    * This implementation simply marks the associated
    * environement unmodified and updates titles of all components.
    */
    protected void notifyUnmodified() {
        env.unmarkModified();
        if (!Boolean.TRUE.equals(LOCAL_CLOSE_DOCUMENT.get())) {
            updateTitles();
        }
    }
    
    /** Conditionally calls notifyModified
     * @return true if the modification was allowed, false if it should be prohibited
     */
    final boolean callNotifyModified() {
        synchronized (checkModificationLock) {
            if (isAlreadyModified()) {
                return true;
            }
            if (preventModification) {
                return false;
            }
            setAlreadyModified(true); // Prevent repetitive calling to notifyModified()
        }

        // Call notifyModified() outside of any lock to prevent deadlocks such as #228991
        // and see also #234791.
        // Note that notifyModified() may call DataEditorSupport.Env.markModified()
        // which starts another thread (ProgressUtils) so any monitors acquired so far
        // must not be re-acquired by the code called from notifyModify() otherwise
        // a deadlock would occur.
        // From document-editing point of view the modification notification
        // should only be called upon document modification which is guarded
        // by document's write-lock.
        // Since unmodification (from save, undo, or document close) is also guarded either
        // by document's read-lock or write-lock the call should be safe.
        if (!notifyModified()) {
            synchronized (checkModificationLock) {
                setAlreadyModified(false);
            }
            return false;
        }

        // Ensure that the positions get converted if a modification occurs.
        getPositionManager().documentOpened(openClose.docRef);
        return true;
    }

    final void callNotifyUnmodified() {
        synchronized (checkModificationLock) {
            if (!isAlreadyModified()) {
                return;
            }
            setAlreadyModified(false);
        }
        
        // Call notifyUnmodified() outside of any lock - see callNotifyModified() description.
        notifyUnmodified();

        if (getAnyEditor() == null) {
            getPositionManager().documentClosed();
        }
    }

    /** Called when the document is being modified.
    * The responsibility of this method is to inform the environment
    * that its document is modified. Current implementation
    * Just calls env.setModified (true) to notify it about
    * modification.
    *
    * @return true if the environment accepted being marked as modified
    *    or false if it refused it and the document should still be unmodified
    */
    protected boolean notifyModified() {
        boolean locked = true;

        try {
            env.markModified();
            // #239622 - since notifyModified() could be called directly (in case of extending CES)
            // check that alreadyModified flag is also set. Otherwise callNotifyUnmodified()
            // would not proceed to notifyUnmodified() call (it would end up on checking alreadyModified flag).
            synchronized (checkModificationLock) {
                if (!isAlreadyModified()) {
                    setAlreadyModified(true);
                }
            }

        } catch (final UserQuestionException ex) {
            synchronized (this) {
                if (!this.inUserQuestionExceptionHandler) {
                    this.inUserQuestionExceptionHandler = true;
                    DocumentOpenClose.RP.post(new Runnable() {
                       public void run() {
                           NotifyDescriptor nd = new NotifyDescriptor.Confirmation(ex.getLocalizedMessage(),
                                                                                   NotifyDescriptor.YES_NO_OPTION);
                           Object res = DialogDisplayer.getDefault().notify(nd);

                           if (NotifyDescriptor.OK_OPTION.equals(res)) {
                               try {
                                   ex.confirmed();
                               }
                               catch (IOException ex1) {
                                   Exceptions.printStackTrace(ex1);
                               }
                           }
                           synchronized (CloneableEditorSupport.this) {
                               CloneableEditorSupport.this.inUserQuestionExceptionHandler = false;
                           }
                       }
                   });
                }
            }
            
            locked = false;
            ERR.log(Level.FINE, "Could not lock document", ex);
        } catch (IOException e) { // locking failed
            //#169695: Added exception log to investigate
            ERR.log(Level.FINE, "Could not lock document", e);
            //#169695: END
            String message = null;

            if ((Object)e.getMessage() != e.getLocalizedMessage()) {
                message = e.getLocalizedMessage();
            } else {
                message = Exceptions.findLocalizedMessage(e);
            }

            if (message != null) {
                StatusDisplayer.getDefault().setStatusText(message);
            }

            locked = false;
        }

        if (!locked) {
            Toolkit.getDefaultToolkit().beep();
            ERR.log(Level.FINE, "notifyModified returns false");
            return false;
        }

        // source modified, remove it from tab-reusing slot
        lastReusable.clear();
        updateTitles();

        if (ERR.isLoggable(Level.FINE)) {
            ERR.log(Level.FINE, "notifyModified returns true; env.isModified()=" + env.isModified());
        }
        return true;
    }

    /** Method that is called when all components of the support are
    * closed. The default implementation closes the document.
    *
    */
    protected void notifyClosed() {
        annotationsLoaded = false;
        closeDocument();
    }

    /** Allows access to the document without any checking.
    */
    final StyledDocument getDocumentHack() {
        return getDoc();
    }

    /** Getter for context associated with this
    * data object.
    */
    final org.openide.util.Lookup getLookup() {
        return lookup;
    }

    // LineSet methods .....................................................................

    /** Updates the line set.
    * @param clear clear any cached set?
    * @return the set
    */
    Line.Set updateLineSet(boolean clear) {
        synchronized (getLock()) {
            if ((lineSet[0] != null) && !clear) {
                return lineSet[0];
            }

            if ((getDoc() == null) ||
                (openClose.getDocumentStatusLA() == DocumentStatus.RELOADING))
            {
                lineSet[0] = new EditorSupportLineSet.Closed(CloneableEditorSupport.this);
            } else {
                lineSet[0] = new EditorSupportLineSet(CloneableEditorSupport.this,getDoc());
            }

            return lineSet[0];
        }
    }

    /** Closes all opened editors (if the user agrees) and
    * flushes content of the document to the file.
    *
    * @param ask ask whether to save the document or not?
    * @return <code>false</code> if the operation is cancelled
    */
    @Override
    protected boolean close(boolean ask) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.close(ask);
        }
        
        if (!super.close(ask)) {
            // if not all editors has been closed
            return false;
        }

        notifyClosed();
        
        return true;
    }

    /** Clears all data from memory.
    */
    private void closeDocument() {
        openClose.close();
    }

    /** Handles the actual reload of document.
    * @param doReload false if we should first ask the user
    */
    private void checkReload(JEditorPane[] openedPanes, boolean doReload) {
        StyledDocument d;

        synchronized (getLock()) {
            d = getDoc(); // Hold reference to document being reloaded
        }

        if (!doReload && !reloadDialogOpened) {
            String msg = NbBundle.getMessage(
                    CloneableEditorSupport.class, "FMT_External_change", // NOI18N
                    d.getProperty(javax.swing.text.Document.TitleProperty)
                );

            reloadDialogOpened = true;

            try {
                if (AskEditorQuestions.askReloadDocument(msg)) {
                    doReload = true;
                }
            } finally {
                reloadDialogOpened = false;
            }
        }

        if (doReload) {
            openClose.reload(openedPanes);

            // Call just for compatibility but this has no effect since the code will not wait
            // for the returned task anyway
            reloadDocument();
        }
    }
    
    /** Creates netbeans document for a given document.
    * @param d document to use as underlaying one
    * @return styled document that could support Guarded.ATTRIBUTE
    */
    private static StyledDocument createNetBeansDocument(Document d) {
        if (d instanceof StyledDocument) {
            return (StyledDocument) d;
        } else {
            // create filter
            return new FilterDocument(d);
        }
    }

    final void fireDocumentChange(StyledDocument document, boolean closing) {
        fireStateChangeEvent(document, closing);
        firePropertyChange(EditorCookie.Observable.PROP_DOCUMENT,
                closing ? document : null,
                closing ? null : document);
    }

    /** Fires a status change event to all listeners. */
    private final void fireStateChangeEvent(StyledDocument document, boolean closing) {
        if (listeners != null) {
            EnhancedChangeEvent event = new EnhancedChangeEvent(this, document, closing);
            ChangeListener[] ls;

            synchronized (this) {
                ls = listeners.toArray(new ChangeListener[0]);
            }

            for (ChangeListener l : ls) {
                l.stateChanged(event);
            }
        }
    }

    /** Updates titles of all editors.
    */
    protected void updateTitles() {
        Enumeration<CloneableTopComponent> en = allEditors.getComponents();

        while (en.hasMoreElements()) {
            CloneableTopComponent o = en.nextElement();
            Pane e = (Pane) o.getClientProperty(PROP_PANE);

            if ((e == null) && o instanceof Pane) {
                e = (Pane) o;
            }

            if (e != null) {
                e.updateName();
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }
    }

    private static Reference<CloneableTopComponent> lastReusable = new WeakReference<CloneableTopComponent>(null);

    private static void replaceTc(TopComponent orig, TopComponent open) {
        int pos = orig.getTabPosition ();
        orig.close();
        open.openAtTabPosition (pos);
    }

    // #18981. There could happen a thing also another class type
    // of CloneableTopCoponent then CloneableEditor could be in allEditors.

    /** Opens a <code>CloneableEditor</code> component. */
    private Pane openPane(boolean reuse) {
        Pane ce = null;
        boolean displayMsgOpened = false;

        synchronized (getLock()) {
            ce = getAnyEditor();

            if (ce == null) {
                // no opened editor
                String msg = messageOpening();

                if (msg != null) {
                    StatusDisplayer.getDefault().setStatusText(msg);
                }

                // initializes the document if not initialized
                prepareDocument();
                ce = createPane();
                ce.getComponent().putClientProperty(PROP_PANE, ce);
                ce.getComponent().setReference(allEditors);

                // signal opened msg should be displayed after subsequent open finishes
                displayMsgOpened = true;
            }
        }

        // #36601 - open moved outside getLock() synchronization
        CloneableTopComponent ctc = ce.getComponent();
        if (reuse && displayMsgOpened) {
            CloneableTopComponent last = lastReusable.get();
            if (last != null) {
                replaceTc(last, ctc);
            } else {
                ctc.open();
            }
            lastReusable = new WeakReference<CloneableTopComponent>(ctc);
        } else {
            ctc.open();
        }
        
        if (displayMsgOpened) {
            String msg = messageOpened();

            if (msg == null) {
                msg = ""; // NOI18N
            }

            StatusDisplayer.getDefault().setStatusText(msg);
        }

        return ce;
    }

    /** If one or more editors are opened finds one.
    * @return an editor or null if none is opened
    */
    Pane getAnyEditor() {
        CloneableTopComponent ctc;
        ctc = allEditors.getArbitraryComponent();

        if (ctc == null) {
            return null;
        }

        Pane e = (Pane) ctc.getClientProperty(PROP_PANE);

        if (e != null) {
            return e;
        } else {
            if (ctc instanceof Pane) {
                return (Pane) ctc;
            }

            Enumeration<CloneableTopComponent> en = allEditors.getComponents();

            while (en.hasMoreElements()) {
                ctc = en.nextElement();
                e = (Pane) ctc.getClientProperty(PROP_PANE);

                if (e != null) {
                    return e;
                } else {
                    if (ctc instanceof Pane) {
                        return (Pane) ctc;
                    }

                    throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
                }
            }

            return null;
        }
    }

    @Deprecated
    final Pane openReuse(final PositionRef pos, final int column, int mode) {
        if (mode == Line.SHOW_REUSE_NEW) lastReusable.clear();
        return openAtImpl(pos, column, true);
    }

    final Pane openReuse(final PositionRef pos, final int column, Line.ShowOpenType mode) {
        if (mode == Line.ShowOpenType.REUSE_NEW) lastReusable.clear();
        return openAtImpl(pos, column, true);
    }
    
    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @param column where to place the caret
    * @return always non-<code>null</code> editor
    * @since 5.2
    */
    protected final Pane openAt(final PositionRef pos, final int column) {
        return openAtImpl(pos, column, false);
    }
    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @param column where to place the caret
    * @param reuse if true, the infrastructure tries to reuse other, already opened editor
     * for the purpose of opening this file/line. 
    * @return always non-<code>null</code> editor
    */
    private final Pane openAtImpl(final PositionRef pos, final int column, boolean reuse) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.openAtImpl(pos, column, reuse);
        }
        final Pane e = openPane(reuse);
        final Task t = prepareDocument();
        e.ensureVisible();
        class Selector implements TaskListener, Runnable {
            private boolean documentLocked = false;

            public void taskFinished(org.openide.util.Task t2) {
                Mutex.EVENT.postReadRequest(this);
                t2.removeTaskListener(this);
            }

            public void run() {
                // #25435. Pane can be null.
                JEditorPane ePane = e.getEditorPane();

                if (ePane == null) {
                    return;
                }

                StyledDocument doc = getDocument();

                if (doc == null) {
                    return; // already closed or error loading
                }

                if (!documentLocked) {
                    documentLocked = true;
                    doc.render(this);
                } else {
                    Caret caret = ePane.getCaret();

                    if (caret == null) {
                        return;
                    }

                    int offset;

                    // Pane's document may differ - see #204980
                    Document paneDoc = ePane.getDocument();
                    if (paneDoc instanceof StyledDocument && paneDoc != doc) {
                        if (ERR.isLoggable(Level.FINE)) {
                            ERR.fine("paneDoc=" + paneDoc + "\n !=\ndoc=" + doc); // NOI18N
                        }
                        doc = (StyledDocument) paneDoc;
                    }

                    javax.swing.text.Element el = NbDocument.findLineRootElement(doc);
                    el = el.getElement(el.getElementIndex(pos.getOffset()));
                    offset = el.getStartOffset() + Math.max(0, column);

                    if (offset > el.getEndOffset()) {
                        offset = Math.max(el.getStartOffset(), el.getEndOffset() - 1);
                    }

                    caret.setDot(offset);

                    try { // scroll to show reasonable part of the document
                        Rectangle r = ePane.modelToView(offset);
                        if (r != null) {
                            r.height *= 5;
                            ePane.scrollRectToVisible(r);
                        }
                    } catch (BadLocationException ex) {
                        ERR.log(Level.WARNING, "Can't scroll to text: pos.getOffset=" + pos.getOffset() //NOI18N
                            + ", column=" + column + ", offset=" + offset //NOI18N
                            + ", doc.getLength=" + doc.getLength(), ex); //NOI18N
                    }
                }
            }
        }
        t.addTaskListener(new Selector());

        return e;
    }

    /** Access to lock on operations on the support
    */
    final Object getLock() {
        return allEditors;
    }

    /** Accessor to the <code>Listener</code> instance, lazy created on demand.
     * The instance serves as a listener on document, environment
     * and also provides document initialization task for this support.
     * @see Listener */
    private Listener getListener() {
        // Should not need to lock; it is always first
        // called within a synchronized(getLock()) block anyway.
        if (listener == null) {
            listener = new Listener();
        }

        return listener;
    }

    /**
     * Start or stop listening on Env.
     * 
     * @param listen whether start listening (true) or stop listening (false).
     */
    void setListeningOnEnv(boolean listen) {
        if (listen != listeningOnEnv) {
            listeningOnEnv = listen;
            if (listen) {
                cesEnv().addPropertyChangeListener(getListener());
            } else {
                cesEnv().removePropertyChangeListener(getListener());
            }
        }
    }

    // [pnejedly]: helper for 40766 test
    void howToReproduceDeadlock40766(boolean beforeLock) {
    }

    /** Make sure we log every access to last save time.
     * @param lst the time in millis of last save
     */
    final void setLastSaveTime(long lst) {
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine(documentID() + ": Setting new lastSaveTime to " + lst + ", " + new Date(lst));
        }
        this.lastSaveTime = lst;
    }
    
    final void updateLastSaveTime() {
        setLastSaveTime(cesEnv().getTime().getTime());
    }

    final boolean isAlreadyModified() {
        return alreadyModified;
    }

    final void setAlreadyModified(boolean alreadyModified) {
        if (alreadyModified != this.alreadyModified) {
            if (ERR.isLoggable(Level.FINE)) {
                boolean origModified;
                synchronized (checkModificationLock) {
                    origModified = isAlreadyModified();
                }
                ERR.fine(documentID() + ": setAlreadyModified from " + origModified + " to " + alreadyModified); // NOI18N
                ERR.log(Level.FINEST, null, new Exception("Setting to modified: " + alreadyModified));
            }

            this.alreadyModified = alreadyModified;
            openClose.setDocumentStronglyReferenced(alreadyModified);
        }
    }
    
    final void setPreventModification(boolean preventModification) {
        this.preventModification = preventModification;
    }

    /* test */ StyledDocument getDoc() {
        return openClose.getRefDocument();
    }

    @Override
    public String toString() {
        return "CES: " + openClose;
    }

    /** Interface for providing data for the support and also
    * locking the source of data.
    */
    public static interface Env extends CloneableOpenSupport.Env {
        /** property that is fired when time of the data is changed */
        public static final String PROP_TIME = "time"; // NOI18N

        /** Obtains the input stream.
         * @return an input stream permitting the document to be loaded
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream() throws IOException;

        /** Obtains the output stream.
         * @return an output stream permitting the document to be saved
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream() throws IOException;

        /**
         * Gets the last modification time for the document.
         * @return the date and time when the document is considered to have been
         *         last changed
         */
        public Date getTime();

        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType();
    }

    /** Describes one existing editor.
     */
    public interface Pane {
        /**
         * get the editor pane component represented by this wrapper.
         */
        public JEditorPane getEditorPane();

        /**
         * Get the TopComponent that contains the EditorPane
         */
        public CloneableTopComponent getComponent();

        public void updateName();

        /**
         * callback for the Pane implementation to adjust itself to the openAt() request.
         */
        public void ensureVisible();
    }

    /** Default editor kit.
    */
    private static final class PlainEditorKit extends DefaultEditorKit implements ViewFactory {
        static final long serialVersionUID = -5788777967029507963L;

        PlainEditorKit() {
        }

        /** @return cloned instance
        */
        @Override
        public Object clone() {
            return new PlainEditorKit();
        }

        /** @return this (I am the ViewFactory)
        */
        @Override
        public ViewFactory getViewFactory() {
            return this;
        }

        /** Plain view for the element
        */
        public View create(Element elem) {
            return new WrappedPlainView(elem);
        }

        /** Set to a sane font (not proportional!). */
        @Override
        public void install(JEditorPane pane) {
            super.install(pane);
            pane.setFont(new Font("Monospaced", Font.PLAIN, pane.getFont().getSize() + 1)); //NOI18N
        }
    }

    /** The listener that this support uses to communicate with
     * document, environment and also temporarilly on undoredo.
     */
    private final class Listener extends Object implements PropertyChangeListener, DocumentListener,
        java.beans.VetoableChangeListener {

        /** revert modification if asked */
        private boolean revertModifiedFlag;

        Listener() {
        }

        public void insertUpdate(DocumentEvent evt) {
            callNotifyModified();
            revertModifiedFlag = false;
        }

        public void removeUpdate(DocumentEvent evt) {
            callNotifyModified();
            revertModifiedFlag = false;
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws java.beans.PropertyVetoException {
            if ("modified".equals(evt.getPropertyName())) { // NOI18N

                if (Boolean.TRUE.equals(evt.getNewValue())) {
                    boolean alreadyModified;
                    synchronized (checkModificationLock) {
                        alreadyModified = isAlreadyModified();
                    }

                    if (!callNotifyModified()) {
                        throw new java.beans.PropertyVetoException("Not allowed", evt); // NOI18N
                    }

                    revertModifiedFlag = !alreadyModified;
                } else {
                    if (revertModifiedFlag) {
                        callNotifyUnmodified();
                    }
                }
            }
        }

        /** Listener to changes in the Env.
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if ("expectedTime".equals(ev.getPropertyName())) { // NOI18N
                lastSaveTime = ((Date)ev.getNewValue()).getTime();
            }
            if (Env.PROP_TIME.equals(ev.getPropertyName())) {
                // empty new value means to force reload all the time
                final Date time = (Date) ev.getNewValue();
                
                ERR.fine(documentID() + ": PROP_TIME new value: " + time + ", " + (time != null ? time.getTime() : -1)); // NOI18N
                ERR.fine("       lastSaveTime: " + new Date(lastSaveTime) + ", " + lastSaveTime); // NOI18N
                
                boolean reload = (lastSaveTime != -1) && ((time == null) || (time.getTime() > lastSaveTime) ||
                        time.getTime() + 10000 < lastSaveTime); // Threshold 10secs to be further discussed
                ERR.fine("             reload: " + reload); // NOI18N

                if (reload) {
                    // - post in AWT event thread because of possible dialog popup
                    // - acquire the write access before checking, so there is no
                    //   clash in-between and we're safe for potential reload.
                    Mutex.EVENT.postReadRequest(
                        new Runnable() {
                            private boolean inRunAtomic;
                            
                            private JEditorPane[] openedPanes;

                            public void run() {
                                if (!inRunAtomic) {
                                    inRunAtomic = true;

                                    StyledDocument sd = getDoc();
                                    if (sd == null) {
                                        return;
                                    }
                                    
                                    // Grab opened panes outside of write-lock
                                    // since getOpenedPanes() may wait for pane initialization
                                    // to be finished but various stages of a possible pane init require read lock
                                    // which would not be acquired without EDT first releasing its write-lock
                                    // so it would wait forewer in getOpenedPanes().
                                    openedPanes = getOpenedPanes();

                                    // #57104 - avoid notifyModified() which takes file lock
                                    preventModification = true;
                                    try {
                                        NbDocument.runAtomic(sd, this);
                                    } finally {
                                        preventModification = false; // #57104
                                    }

                                    return;
                                }

                                boolean noAsk = time == null || !isModified();
                                ERR.fine(documentID() + ": checkReload noAsk: " + noAsk);
                                checkReload(openedPanes, noAsk);
                            }
                        }
                    );

                    ERR.fine(documentID() + ": reload task posted"); // NOI18N
                }
            }

            if (Env.PROP_MODIFIED.equals(ev.getPropertyName())) {
                CloneableEditorSupport.this.firePropertyChange(
                    EditorCookie.Observable.PROP_MODIFIED, ev.getOldValue(), ev.getNewValue()
                );
            }

            // #129178 - update title if read-only state is externally changed
            if ("DataEditorSupport.read-only.changing".equals(ev.getPropertyName())) {  //NOI18N
                Object o = ev.getNewValue();
                if (o == Boolean.TRUE) {
                    Document d = getDoc();
                    // see #222935, indicate the file has become editable to editor.lib
                    d.putProperty("editable", Boolean.TRUE);
                }
                updateTitles();
            }
        }

    }

    /** Special runtime exception that holds the original I/O failure.
     */
//    static final class DelegateIOExc extends IllegalStateException {
//        public DelegateIOExc(IOException ex) {
//            super(ex.getMessage());
//            initCause(ex);
//        }
//    }

    private final class DocFilter extends DocumentFilter {
        
        final DocumentFilter origFilter;
        
        DocFilter(DocumentFilter origFilter) {
            this.origFilter = origFilter;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            boolean origModified = checkModificationAllowed(offset);
            boolean success = false;
            try {
                if (origFilter != null) {
                    origFilter.insertString(fb, offset, string, attr);
                } else {
                    super.insertString(fb, offset, string, attr);
                }
                success = true;
            } finally {
                if (!success) {
                    if (!origModified) {
                        callNotifyUnmodified();
                    }
                }
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            boolean origModified = checkModificationAllowed(offset);
            boolean success = false;
            try {
                if (origFilter != null) {
                    origFilter.remove(fb, offset, length);
                } else {
                    super.remove(fb, offset, length);
                }
                success = true;
            } finally {
                if (!success) {
                    if (!origModified) {
                        callNotifyUnmodified();
                    }
                }
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            boolean origModified = checkModificationAllowed(offset);
            boolean success = false;
            try {
                if (origFilter != null) {
                    origFilter.replace(fb, offset, length, text, attrs);
                } else {
                    super.replace(fb, offset, length, text, attrs);
                }
                success = true;
            } finally {
                if (!success) {
                    if (!origModified) {
                        callNotifyUnmodified();
                    }
                }
            }
        }
        
        private boolean checkModificationAllowed(int offset) throws BadLocationException {
            boolean alreadyModified;
            synchronized (checkModificationLock) {
                alreadyModified = isAlreadyModified();
            }
            if (!callNotifyModified()) {
                modificationNotAllowed(offset);
            }
            return alreadyModified;
        }
        
        private void modificationNotAllowed(int offset) throws BadLocationException {
            throw new BadLocationException("Modification not allowed", offset); // NOI18N
        }

    }

}
