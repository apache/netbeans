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

package org.netbeans.spi.debugger.ui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Dispatcher of context-related events and provider of active elements in the IDE.
 * 
 * <p>This class tracks the changes of the selected file and active editor and re-fires
 * the changes to registered listeners. The listeners can register based on
 * a MIME type of files which they are interested in. This prevents from unnecessary
 * activity of debugging actions when the context is switched among unrelated files.
 * 
 * <p>The EditorContextDispatcher provides convenient access to currently selected
 * elements and recently selected elements in the GUI.
 * 
 * <p>
 * <strong>Typical usage:</strong>
 * </p>
 * Attach a listener based on file MIME type. The usage of WeakListeners is
 * preferred, unless the listener can be removed explicitely.
 * <pre>
 *  EditorContextDispatcher.getDefault().addPropertyChangeListener("&lt;MIME type&gt;",
 *              WeakListeners.propertyChange(dispatchListener, EditorContextDispatcher.getDefault()));
 * </pre>
 * Then use <code>getCurrent*()</code> methods to find the currently selected
 * elements in the IDE.
 * If recently selected elements are desired, use <code>getMostRecent*()</code>
 * methods. They provide current elements if available, or elements that were
 * current the last time.
 * 
 * 
 * @author Martin Entlicher
 * @since 2.13
 */
public final class EditorContextDispatcher {

    private static final Logger logger = Logger.getLogger(EditorContextDispatcher.class.getName());
    
    /**
     * Name of property fired when the current file changes.
     */
    public static final String PROP_FILE = "file";  // NOI18N
    /**
     * Name of property fired when the current editor changes.
     */
    public static final String PROP_EDITOR = "editor";  // NOI18N
    
    private static EditorContextDispatcher context;
    
    /**
     * Get the default instance of EditorContextDispatcher.
     * @return The EditorContextDispatcher
     */
    public static synchronized EditorContextDispatcher getDefault() {
        if (context == null) {
            context = new EditorContextDispatcher();
        }
        return context;
    }

    
    private final RequestProcessor refreshProcessor;
    private final Lookup.Result<FileObject> resFileObject;
    private final PropertyChangeListener  erListener;
    private final ThreadLocal<CoalescedChange> lookupCoalescedChange = new ThreadLocal<CoalescedChange>();
    private final RequestProcessor ccrp = new RequestProcessor("Coalesced Change Request Processor", 1, false, false); // NOI18N
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<String, PropertyChangeSupport> pcsByMIMEType = new HashMap<String, PropertyChangeSupport>();
    
    private String lastFiredMIMEType = null;
    private Map<String, Reference<Object>> lastMIMETypeEvents = new HashMap<String, Reference<Object>>();

    private static final Reference<FileObject> NO_FILE = new WeakReference<FileObject>(null);
    private static final Reference<JTextComponent> NO_TEXT_COMPONENT = new WeakReference<JTextComponent>(null);
    private static final Reference<FileChangeListener> NO_FILE_CHANGE = new WeakReference<FileChangeListener>(null);

    private String currentURL;
    private Reference<FileObject> currentFile = NO_FILE;
    private FileChangeListener currentFileChangeListener = null;
    private Reference<FileChangeListener> currentFileChangeListenerWeak = NO_FILE_CHANGE;
    private Reference<JTextComponent> currentTextComponent = NO_TEXT_COMPONENT;
    
    // Most recent in editor:
    private Reference<FileObject> mostRecentFileRef = NO_FILE;
    private FileChangeListener mostRecentFileChangeListener = null;
    private Reference<FileChangeListener> mostRecentFileChangeListenerWeak = NO_FILE_CHANGE;
    
    private EditorContextDispatcher() {
        refreshProcessor = new RequestProcessor("Refresh Editor Context", 1);   // NOI18N
        
        resFileObject = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
        EditorLookupListener ell = new EditorLookupListener(FileObject.class);
        resFileObject.addLookupListener(ell);
        ell.lookupChanged(false); // To initialize data
        
        erListener = new EditorRegistryListener();
        EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(erListener, EditorRegistry.class));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // To initialize data:
                ((EditorRegistryListener) erListener).update(false);
            }
        });
        
    }
    
    /**
     * Get the current active file.
     * @return The current file or <code>null</code> when there is no active file.
     */
    public synchronized FileObject getCurrentFile() {
        return currentFile.get();
    }
    
    /**
     * Get the String representation of URL of the current active file.
     * @return The String representation of URL of the current active file or
     *         an empty String when there is no active file.
     */
    public synchronized String getCurrentURLAsString() {
        if (currentURL == null) {
            FileObject fo = getCurrentFile();
            if (fo != null) {
                currentURL = fo.toURL().toString ();
            }
            if (currentURL == null) {
                currentURL = ""; // NOI18N
            }
        }
        return currentURL;
    }
    
    /**
     * Get the {@link org.openide.cookies.EditorCookie} of currently edited file.
     * @return The current {@link org.openide.cookies.EditorCookie} or
     *         <code>null</code> when there is no currently edited file.
     */
    private EditorCookie getCurrentEditorCookie() {
        JEditorPane editor = getCurrentEditor();
        if (editor != null) {
            Document document = editor.getDocument();
            DataObject dataObject = NbEditorUtilities.getDataObject(document);
            if (dataObject != null) {
                EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
                return ec;
            }
        }
        return null;
    }
    
    /**
     * Get the {@link javax.swing.JEditorPane} of currently edited file.
     * @return The current {@link javax.swing.JEditorPane} or
     *         <code>null</code> when there is no currently edited file.
     */
    public synchronized JEditorPane getCurrentEditor() {
        JTextComponent ctc = currentTextComponent.get();
        if (ctc instanceof JEditorPane) {
            return ((JEditorPane) ctc);
        } else {
            return null;
        }
    }
    
    /**
     * Get the line number of the caret in the current editor.
     * @return the line number or <code>-1</code> when there is no current editor.
     */
    public int getCurrentLineNumber() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return -1;
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return -1;
        StyledDocument d = e.getDocument ();
        if (d == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        int ln = NbDocument.findLineNumber (
            d,
            caret.getDot ()
        );
        return ln + 1;
    }
    
    /**
     * Get the line of the caret in the current editor.
     * @return the line or <code>null</code> when there is no current editor.
     */
    public Line getCurrentLine() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return null;
        StyledDocument d = e.getDocument ();
        if (d == null) return null;
        Caret caret = ep.getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber(d, caret.getDot());
        Line.Set lineSet = e.getLineSet();
        try {
            assert lineSet != null : e;
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    /**
     * Get a list of MIME types of languages found on the current line.
     * @return A set of MIME types.
     * @since 2.50
     */
    public Set<String> getMIMETypesOnCurrentLine() {
        Line line = getCurrentLine();
        if (line == null) {
            return Collections.EMPTY_SET;
        }
        return getMIMETypesOnLine(line);
    }
    
    /**
     * Get a list of MIME types of languages found on a line.
     * @param line The line to search for the MIME types.
     * @return A set of MIME types.
     * @since 2.50
     */
    public Set<String> getMIMETypesOnLine(Line line) {
        EditorCookie editorCookie = line.getLookup().lookup(EditorCookie.class);
        if (editorCookie == null) {
            DataObject dobj = line.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                editorCookie = dobj.getLookup().lookup(EditorCookie.class);
            }
            if (editorCookie == null) {
                return Collections.emptySet();
            }
        }
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            return Collections.emptySet();
        }
        Set<String> mimeTypes = null;
        ((AbstractDocument) document).readLock();
        try {
            TokenHierarchy<Document> th = TokenHierarchy.get((Document) document);
            int ln = line.getLineNumber();
            int offset = NbDocument.findLineOffset(document, ln);
            int maxOffset = document.getLength() - 1;
            int maxLine = NbDocument.findLineNumber(document, maxOffset);
            int offset2;
            if (ln + 1 > maxLine) {
                offset2 = maxOffset;
            } else {
                offset2 = NbDocument.findLineOffset(document, ln+1) - 1;
            }
            // The line has offsets <offset, offset2>
            Set<LanguagePath> languagePaths = th.languagePaths();
            for (LanguagePath lp : languagePaths) {
                List<TokenSequence<?>> tsl = th.tokenSequenceList(lp, offset, offset2);
                for (TokenSequence ts : tsl) {
                    if (ts.moveNext()) {
                        //int to = ts.offset();
                        //if (!(offset <= to && to < offset2)) {
                        //    continue;
                        //}
                        String mimeType = ts.language().mimeType();
                        if (mimeType != null) {
                            if (mimeTypes == null) {
                                mimeTypes = Collections.singleton(mimeType);
                            } else {
                                if (mimeTypes.size() == 1) {
                                    mimeTypes = new HashSet<String>(mimeTypes);
                                }
                                mimeTypes.add(mimeType);
                            }
                        }
                    }
                }
            }
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
        return (mimeTypes != null) ? mimeTypes : Collections.<String>emptySet();
        
    }
    
    /**
     * Get the most recent active file. This returns the active file if there's
     * one, or a file, that was most recently active.
     * @return The most recent file or <code>null</code> when there was no recent
     * active file.
     */
    public synchronized FileObject getMostRecentFile() {
        return mostRecentFileRef.get();
    }

    /** Used by unit test only */
    void setMostRecentFile(FileObject file) {
        Object oldFile;
        String MIMEType = null;
        synchronized (this) {
            oldFile = mostRecentFileRef.get();
            mostRecentFileRef = new WeakReference(file);
            if (file != null) {
                MIMEType = file.getMIMEType();
            }
        }
        refreshProcessor.post(new EventFirer(PROP_EDITOR, oldFile, file, MIMEType));
    }

    /**
     * Get the String representation of URL of the most recent active file.
     * @return The String representation of URL of the most recent file or
     *         an empty String when there was no recent active file.
     */
    public synchronized String getMostRecentURLAsString() {
        FileObject fo = getMostRecentFile();
        if (fo != null) {
            return fo.toURL().toString ();
        }
        return ""; // NOI18N
    }
    
    private EditorCookie getMostRecentEditorCookie() {
        JEditorPane editor = getMostRecentEditor();
        if (editor != null) {
            Document document = editor.getDocument();
            DataObject dataObject = NbEditorUtilities.getDataObject(document);
            if (dataObject != null) {
                EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
                return ec;
            }
        }
        return null;

    }
    
    public JEditorPane getMostRecentEditor() {
        JTextComponent ctc = EditorRegistry.lastFocusedComponent();
        if (ctc instanceof JEditorPane) {
            return ((JEditorPane) ctc);
        } else {
            return null;
        }
    }
    
    /**
     * Get the line number of the caret in the most recent editor.
     * This returns the current line number in the current editor if there's one,
     * or a line number of the caret in the editor, that was most recently active.
     * @return the line number or <code>-1</code> when there was no recent active editor.
     */
    public int getMostRecentLineNumber() {
        EditorCookie e = getMostRecentEditorCookie ();
        if (e == null) return -1;
        JEditorPane ep = getMostRecentEditor ();
        if (ep == null) return -1;
        StyledDocument d = e.getDocument ();
        if (d == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        int ln = NbDocument.findLineNumber (
            d,
            caret.getDot ()
        );
        return ln + 1;
    }
    
    /**
     * Get the line of the caret in the most recent editor.
     * This returns the current line in the current editor if there's one,
     * or a line of the caret in the editor, that was most recently active.
     * @return the line or <code>null</code> when there was no recent active editor.
     */
    public Line getMostRecentLine() {
        EditorCookie e = getMostRecentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getMostRecentEditor ();
        if (ep == null) return null;
        StyledDocument d = e.getDocument ();
        if (d == null) return null;
        Caret caret = ep.getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber(d, caret.getDot());
        Line.Set lineSet = e.getLineSet();
        try {
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    /**
     * Add a PropertyChangeListener to this context dispatcher.
     * It's strongly suggested to use {@link #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
     * instead, if possible, for performance reasons.
     * @param l The PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a PropertyChangeListener from this context dispatcher.
     * @param l The PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        // Also remove the listener from all MIME types
        synchronized (pcsByMIMEType) {
            Set<String> MIMETypes = new HashSet<>(pcsByMIMEType.keySet());
            for (String MIMEType : MIMETypes) {
                PropertyChangeSupport _pcs = pcsByMIMEType.get(MIMEType);
                _pcs.removePropertyChangeListener(l);
                if (_pcs.getPropertyChangeListeners().length == 0) {
                    pcsByMIMEType.remove(MIMEType);
                }
            }
        }
    }
    
    /**
     * Add a PropertyChangeListener to this context dispatcher to be notified
     * about changes of files with a specified MIME type.
     * @param MIMEType The MIME type to report changes for
     * @param l The PropertyChangeListener
     */
    public void addPropertyChangeListener(String MIMEType, PropertyChangeListener l) {
        PropertyChangeSupport _pcs;
        synchronized (pcsByMIMEType) {
            _pcs = pcsByMIMEType.get(MIMEType);
            if (_pcs == null) {
                _pcs = new PropertyChangeSupport(this);
                pcsByMIMEType.put(MIMEType, _pcs);
            }
        }
        _pcs.addPropertyChangeListener(l);
    }
    
    /*public void removePropertyChangeListener(String MIMEType, PropertyChangeListener l) {
        PropertyChangeSupport pcs;
        synchronized (pcsByMIMEType) {
            pcs = pcsByMIMEType.get(MIMEType);
            if (pcs == null) {
                return ;
            }
        }
        pcs.removePropertyChangeListener(l);
    }*/
    
    private void firePropertyChange(PropertyChangeEvent evt, String preferredMIMEType) {
        //System.err.println("EditorContextDispatcher.firePropertyChange("+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
        pcs.firePropertyChange(evt);
        if (PROP_FILE.equals(evt.getPropertyName())) {
            // Retrieve the files MIME types and fire to appropriate MIME type listeners:
            FileObject oldFile = (FileObject) evt.getOldValue();
            FileObject newFile = (FileObject) evt.getNewValue();
            String oldMIMEType = (oldFile != null) ? oldFile.getMIMEType() : null;
            String newMIMEType = (newFile != null) ? newFile.getMIMEType() : null;
            PropertyChangeSupport pcsMIMEOld = null, pcsMIMENew = null;
            PropertyChangeEvent evtOld = null, evtNew = null;
            synchronized (pcsByMIMEType) {
                if (oldMIMEType != null && oldMIMEType.equals(newMIMEType)) {
                    pcsMIMEOld = pcsByMIMEType.get(oldMIMEType);
                    evtOld = evt;
                    //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+oldMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
                } else {
                    if (oldMIMEType != null) {
                        pcsMIMEOld = pcsByMIMEType.get(oldMIMEType);
                        if (pcsMIMEOld != null) {
                            evtOld = new PropertyChangeEvent(evt.getSource(),
                                                             evt.getPropertyName(),
                                                             evt.getOldValue(),
                                                             null);
                        }
                        //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+oldMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", null)");
                    }
                    if (newMIMEType != null) {
                        pcsMIMENew = pcsByMIMEType.get(newMIMEType);
                        if (pcsMIMENew != null) {
                            evtNew = new PropertyChangeEvent(evt.getSource(),
                                                             evt.getPropertyName(),
                                                             null,
                                                             evt.getNewValue());
                        }
                        //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+newMIMEType+", "+evt.getPropertyName()+", null, "+evt.getNewValue()+")");
                    }
                }
            }
            if (pcsMIMEOld != null) {
                pcsMIMEOld.firePropertyChange(evtOld);
            }
            if (pcsMIMENew != null) {
                pcsMIMENew.firePropertyChange(evtNew);
            }
            // Now check, whether the MIME type has changed and whether we should
            // fire non-file change events with 'null' new values to listeners
            // registered for a particular MIME type:
            if (oldMIMEType != null && !oldMIMEType.equals(newMIMEType) && pcsMIMEOld != null) {
                String lastMIMEType;
                Map<String, Reference<Object>> lastEvents;
                synchronized (this) {
                    lastMIMEType = lastFiredMIMEType;
                    lastEvents = new HashMap<>(lastMIMETypeEvents);
                    if (lastMIMEType != null && lastMIMEType.equals(oldMIMEType)) {
                        lastFiredMIMEType = null;
                        lastMIMETypeEvents.clear();
                    } else {
                        lastEvents = null;
                    }
                }
                if (lastEvents != null) {
                    for (String property : lastEvents.keySet()) {
                        pcsMIMEOld.firePropertyChange(property, lastEvents.get(property).get(), null);
                    }
                }
            }
        } else {
            PropertyChangeSupport pcsMIME = null;
            if (preferredMIMEType != null) {
                synchronized (pcsByMIMEType) {
                    pcsMIME = pcsByMIMEType.get(preferredMIMEType);
                }
                if (pcsMIME != null) {
                    pcsMIME.firePropertyChange(evt);
                }
            }
            //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+preferredMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
            synchronized (this) {
                if (pcsMIME != null) {
                    lastFiredMIMEType = preferredMIMEType;
                    // evt.getNewValue() may be a JEditorPane instance which disables its GC
                    lastMIMETypeEvents.put(evt.getPropertyName(), new WeakReference<Object>(evt.getNewValue()));
                } else {
                    lastFiredMIMEType = null;
                    lastMIMETypeEvents.clear();
                }
            }
        }
    }

    private class EditorLookupListener extends Object implements LookupListener {
        
        private RequestProcessor.Task cctask;
        private Class type;
        
        public EditorLookupListener(Class type) {
            this.type = type;
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            // It can happen, that we're called many times in one AWT cycle...
            coalescedLookupChanged();
        }
        
        private void coalescedLookupChanged() {
            CoalescedChange cc = lookupCoalescedChange.get();
            if (cc == null) {
                cc = new CoalescedChange();
                lookupCoalescedChange.set(cc);
            }
            Collection<? extends FileObject> fos = null;
            boolean doCoalescing = cc.isCoalescing();
            if (!doCoalescing) {
                if (SwingUtilities.isEventDispatchThread()) {
                    if (type == FileObject.class) {
                        fos = resFileObject.allInstances();
                        if (fos.size() > 1) {
                            // Do not call an expensive DataObject.find(fo) in AWT thread.
                            doCoalescing = true;
                        }
                    }
                }
            }
            if (doCoalescing) {
                RequestProcessor.Task task;
                synchronized (this) {
                    if (cctask == null) {
                        cctask = ccrp.create(new Runnable() {
                            @Override
                            public void run() {
                                lookupChanged(true);
                            }
                        });
                    }
                    task = cctask;
                }
                task.schedule(2);
            } else {
                lookupChanged(true, fos);
            }
            cc.done();
        }

        private void lookupChanged(final boolean doFire) {
            lookupChanged(doFire, null);
        }
        
        private void lookupChanged(final boolean doFire, Collection<? extends FileObject> fos) {
            //System.err.println("EditorContextDispatcher.resultChanged(), type = "+type+" in "+Thread.currentThread());
            if (type == FileObject.class) {
                if (fos == null) {
                    fos = resFileObject.allInstances();
                }
                FileObject oldFile;
                FileObject newFile;
                if (fos.isEmpty()) {
                    newFile = null;
                } else if (fos.size() == 1) {
                    newFile = fos.iterator().next();
                } else {
                    newFile = findPrimary(fos);
                }
                if (newFile != null && !newFile.isData()) {
                    newFile = null;
                }
                synchronized (EditorContextDispatcher.this) {
                    oldFile = currentFile.get();
                    //System.err.println("\nCURRENT FILES = "+fos+"\n");
                    currentFile = newFile == null ? NO_FILE : new WeakReference<FileObject>(newFile);
                    currentURL = null;
                    if (newFile != null) {
                        mostRecentFileRef = currentFile;
                    }
                    reAttachFileChangeListener(oldFile, newFile, true);
                    /*if (newFile != null) {  - NO, we need the last file in editor.
                        mostRecentFileRef = new WeakReference(newFile);
                    }*/
                }
                if (doFire && oldFile != newFile) {
                    refreshProcessor.post(new EventFirer(PROP_FILE, oldFile, newFile));
                }
            }
        }
        
        private FileObject findPrimary(Collection<? extends FileObject> fos) {
            for (FileObject fo : fos) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    if (fo.equals(dobj.getPrimaryFile())) {
                        return fo;
                    }
                } catch (DataObjectNotFoundException ex) {}
            }
            // No primary file, return just the first one:
            return fos.iterator().next();
        }
        
    }
    
    private class EditorRegistryListener implements PropertyChangeListener, FocusListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("EditorRegistryListener.propertyChange("+propertyName+": "+evt.getOldValue()+" => "+evt.getNewValue()+")");
            }
            if (propertyName.equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
                Object newFocused = evt.getNewValue();
                if (newFocused instanceof JRootPane) {
                    JRootPane root = (JRootPane) newFocused;
                    if (root.isAncestorOf((Component) evt.getOldValue())) {
                        logger.fine("Focused root.");
                        root.addFocusListener(this);
                        return;
                    }
                }
            }
            if (propertyName.equals(EditorRegistry.FOCUS_GAINED_PROPERTY) ||
                propertyName.equals(EditorRegistry.FOCUS_LOST_PROPERTY) ||
                propertyName.equals(EditorRegistry.FOCUSED_DOCUMENT_PROPERTY)) {
                
                update(true);
            }
        }
        
        private void update(boolean doFire) {
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("New focused component = "+focusedComponent);
            }
            JTextComponent oldEditor;
            JTextComponent newEditor;
            String MIMEType = null;
            synchronized (EditorContextDispatcher.this) {
                oldEditor = currentTextComponent.get();
                if (focusedComponent != null) {
                    currentTextComponent = new WeakReference(focusedComponent);
                } else {
                    currentTextComponent = NO_TEXT_COMPONENT;
                }
                newEditor = currentTextComponent.get();
                FileObject f = currentFile.get();
                if (f != null) {
                    if (newEditor != null) {
                        reAttachFileChangeListener(mostRecentFileRef.get(), f, false);
                        mostRecentFileRef = new WeakReference<FileObject>(f);
                    }
                    if (doFire && oldEditor != newEditor) {
                        lazyRetrieveMIMETypeAndFire(oldEditor, newEditor, f);
                        doFire = false;
                    }
                } else {
                    MIMEType = null;
                }
            }
            if (doFire && oldEditor != newEditor) {
                refreshProcessor.post(new EventFirer(PROP_EDITOR, oldEditor, newEditor, MIMEType));
            }
        }
        
        private void lazyRetrieveMIMETypeAndFire(final JTextComponent oldEditor, final JTextComponent newEditor, final FileObject f) {
            refreshProcessor.post(new Runnable() {
                @Override
                public void run() {
                    String MIMEType = f.getMIMEType();
                    new EventFirer(PROP_EDITOR, oldEditor, newEditor, MIMEType).run();
                }
            });
            
        }

        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Focus Lost from "+e.getComponent());
                logger.fine("  opposite component is: "+e.getOppositeComponent());
            }
            e.getComponent().removeFocusListener(this);
            synchronized (EditorContextDispatcher.this) {
                if (e.getOppositeComponent() == currentTextComponent.get()) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Opposite is current. No update.");
                    }
                    return ;
                }
            }
            update(true);
        }

    }
    
    private void reAttachFileChangeListener(FileObject oldFile, FileObject newFile, boolean current) {
        // Must be called in synchronized block
        assert Thread.holdsLock(EditorContextDispatcher.this);
        if (current) {
            if (oldFile != null) {
                FileChangeListener chw = currentFileChangeListenerWeak.get();
                if (chw != null) {
                    AddRemoveFileListenerInEQThread.removeFileChangeListener(oldFile, chw);
                    currentFileChangeListenerWeak = NO_FILE_CHANGE;
                    currentFileChangeListener = null;
                }
            }
            if (newFile != null) {
                currentFileChangeListener = new FileRenameListener();
                FileChangeListener chw =
                        WeakListeners.create(FileChangeListener.class,
                                                currentFileChangeListener,
                                                newFile);
                AddRemoveFileListenerInEQThread.addFileChangeListener(newFile, chw);
                currentFileChangeListenerWeak = new WeakReference<FileChangeListener>(chw);
            }
        } else {
            if (oldFile != null) {
                FileChangeListener chw = mostRecentFileChangeListenerWeak.get();
                if (chw != null) {
                    AddRemoveFileListenerInEQThread.removeFileChangeListener(oldFile, chw);
                    mostRecentFileChangeListenerWeak = NO_FILE_CHANGE;
                    mostRecentFileChangeListener = null;
                }
            }
            if (newFile != null) {
                mostRecentFileChangeListener = new FileRenameListener();
                FileChangeListener chw =
                        WeakListeners.create(FileChangeListener.class,
                                                mostRecentFileChangeListener,
                                                newFile);
                AddRemoveFileListenerInEQThread.addFileChangeListener(newFile, chw);
                mostRecentFileChangeListenerWeak = new WeakReference<FileChangeListener>(chw);
            }
        }
    }

    private static final class CoalescedChange {
        
        private static final int NUM = 5; // Start coalescing after 5 fast calls
        private static final long TD = 20; // 20ms delta
        private long lastTime = 0l;
        private int num = 0;
        
        public CoalescedChange() {
        }
        
        public void done() {
            lastTime = System.currentTimeMillis();
        }

        private boolean isCoalescing() {
            if (System.currentTimeMillis() <= (lastTime + TD)) {
                num++;
            } else {
                num = 0;
            }
            return num > NUM;
        }

    }

    /**
     * Use this class to add or remove file change listener in EQ thread.
     * The addition or removal of a listener may require some disk operations.
     * Therefore it can block EventQueue for an unpredictable amount of time.
     * This is why doing it in AWT can block UI. Use this class not to block the UI.
     */
    private static final class AddRemoveFileListenerInEQThread implements Runnable {
        
        private enum AddRemove { ADD, REMOVE }
        private static final Queue<Work> work = new LinkedList<Work>();
        private static final RequestProcessor rp = new RequestProcessor(AddRemoveFileListenerInEQThread.class.getName(), 1, false, false);
        private static Task t;
        private static final int TIMEOUT = 250; // Can spend 250ms in EQ at most
        
        public static void addFileChangeListener(FileObject fo, FileChangeListener l) {
            //System.err.println("addFileChangeListener("+fo+") in AWT");
            //long start = System.nanoTime();
            doWork(new Work(AddRemove.ADD, fo, l));
            //long end = System.nanoTime();
            //System.err.println("addFileChangeListener("+fo+") in AWT done in "+((end-start)/1000000.0)+"ms.");
        }
        
        public static void removeFileChangeListener(FileObject fo, FileChangeListener l) {
            //System.err.println("removeFileChangeListener("+fo+") in AWT");
            //long start = System.nanoTime();
            doWork(new Work(AddRemove.REMOVE, fo, l));
            //long end = System.nanoTime();
            //System.err.println("removeFileChangeListener("+fo+") in AWT done in "+((end-start)/1000000.0)+"ms.");
        }
        
        private static void doWork(Work w) {
            synchronized (AddRemoveFileListenerInEQThread.class) {
                work.add(w);
                if (t == null) {
                    t = rp.create(new AddRemoveFileListenerInEQThread());
                }
                t.schedule(0);
            }
            try {
                w.waitFinished(TIMEOUT);
            } catch (InterruptedException ex) {
                // Re-interrupt
                Thread.currentThread().interrupt();
            }
        }
        
        private static synchronized Work getWork() {
            return work.poll();
        }

        @Override
        public void run() {
            Work w;
            while ((w = getWork()) != null) {
                switch (w.ar) {
                    case ADD:    w.fo.addFileChangeListener(w.l);
                                 //System.err.println("  listener added to "+w.fo);
                                 break;
                    case REMOVE: w.fo.removeFileChangeListener(w.l);
                                 //System.err.println("  listener removed from "+w.fo);
                                 break;
                }
                w.finished();
            }
        }
        
        private static final class Work {
            
            AddRemove ar; FileObject fo; FileChangeListener l;
            boolean finished;
            
            Work(AddRemove ar, FileObject fo, FileChangeListener l) {
                this.ar = ar; this.fo = fo; this.l = l;
            }
            
            private synchronized void finished() {
                finished = true;
                notifyAll();
            }
            
            private synchronized void waitFinished(int timeout) throws InterruptedException {
                if (!finished) {
                    wait(timeout);
                }
            }
        }
    }
    
    private final class EventFirer implements Runnable {
        
        private final PropertyChangeEvent evt;
        private final String MIMEType;
        
        public EventFirer(String propertyName, Object oldValue, Object newValue) {
            this(propertyName, oldValue, newValue, null);
        }
        
        public EventFirer(String propertyName, Object oldValue, Object newValue, String MIMEType) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("EventFirer("+propertyName+", "+oldValue+", "+newValue+")");
            }
            this.evt = new PropertyChangeEvent(EditorContextDispatcher.this, propertyName, oldValue, newValue);
            this.MIMEType = MIMEType;
        }

        @Override
        public void run() {
            firePropertyChange(evt, MIMEType);
        }
        
    }

    private final class FileRenameListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileObject fo = (FileObject) fe.getSource();
            boolean doFire;
            synchronized (EditorContextDispatcher.this) {
                FileObject currentFO = currentFile.get();
                FileObject lastFO = mostRecentFileRef.get();
                doFire = fo.equals(currentFO) || fo.equals(lastFO);
                if (doFire) {
                    currentURL = null;
                }
            }
            if (doFire) {
                // Fire null as the old value so that the event is not ignored.
                refreshProcessor.post(new EventFirer(PROP_FILE, null, fo, fo.getMIMEType()));
            }
        }

    }
    
}
