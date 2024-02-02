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


import java.beans.PropertyChangeEvent;
import java.io.*;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import org.openide.awt.UndoRedo;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Exceptions;
import org.openide.util.Task;
import org.openide.util.actions.SystemAction;
import org.openide.windows.*;

/** Support for associating an editor and a Swing {@link Document} to a data object.
 * Can be assigned as a cookie to any editable data object.
 * Then this data object will be capable of being opened in an editor, and there will be ways of retrieving and acting upon the Swing document which is editing it.
*
* @author Jaroslav Tulach
* @deprecated Use {@link org.openide.text.DataEditorSupport} instead
*/
@Deprecated
public class EditorSupport extends OpenSupport
implements EditorCookie.Observable, OpenCookie, CloseCookie, PrintCookie {
    /** Common name for editor mode.
     * @deprecated Use {@link org.openide.text.CloneableEditorSupport#EDITOR_MODE} instead.
     */
    @Deprecated
    public static final String EDITOR_MODE = CloneableEditorSupport.EDITOR_MODE;

    /** @deprecated no longer used */
    @Deprecated
    protected String modifiedAppendix = " *"; // NOI18N
    
    /** The flag saying if we should listen to the document modifications */
    private boolean listenToModifs = true;
    
    /** delegating support */
    private Del del;

    /** Support an existing loader entry. The file is taken from the
    * entry and is updated if the entry is moved or renamed.
    * @param entry entry to create instance from
    */
    public EditorSupport(MultiDataObject.Entry entry) {
        super(entry, new DelEnv(entry.getDataObject()));

        del = new Del (
            entry.getDataObject (),
            (DelEnv)env,
            allEditors
        );
    }
    
    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        return del.superMessageOpening ();
    }
    
    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        return del.superMessageOpened ();
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return del.superMessageSave ();
    }
    
    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        return del.superMessageName ();
    }
    
    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    protected String messageToolTip () {
        return del.superMessageToolTip ();
    }
    
    
    /** Updates titles of all editors.
    */
    protected void updateTitles () {
        del.superUpdateTitles ();
    }
    

    /* A method to create a new component. Overridden in subclasses.
    * @return the {@link Editor} for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
        // initializes the document if not initialized
        prepareDocument ();

        DataObject obj = findDataObject ();
        Editor editor = new Editor (obj);
        return editor;
    }

    /** Create an undo/redo manager.
    * This manager is then attached to the document, and listens to
    * all changes made in it.
    * <P>
    * The default implementation simply uses <code>UndoRedo.Manager</code>.
    *
    * @return the undo/redo manager
    */
    protected UndoRedo.Manager createUndoRedoManager () {
        return del.superUndoRedoManager ();
    }

    /** Passes the actual opening to internal delegate support.
     * Overrides superclass method. */
    public void open() {
        del.open();
    }

    // editor cookie .......................................................................

    /** Closes all opened editors (if the user agrees) and
    * flushes content of the document to the file.
    *
    * @return <code>false</code> if the operation is cancelled
    */
    public boolean close () {
        return del.close ();
    }
    
    /** Closes the editor, asks if necessary.
     * @param ask true if we should ask the user
     * @return true if succesfully closed
     */
    protected boolean close (boolean ask) {
        return del.superClose (ask);
    }

    /** Load the document into memory. This is done
    * in different thread. A task for the thread is returned
    * so anyone may test whether the loading has been finished
    * or is still in process.
    *
    * @return task for control over loading
    */
    public synchronized Task prepareDocument () {
        return del.prepareDocument ();
    }

    /** Get the document associated with this cookie.
    * It is an instance of Swing's {@link StyledDocument} but it should
    * also understand the NetBeans {@link org.openide.text.NbDocument#GUARDED} to
    * prevent certain lines from being edited by the user.
    * <P>
    * If the document is not loaded the method blocks until
    * it is.
    *
    * @return the styled document for this cookie that
    *   understands the guarded attribute
    * @exception IOException if the document could not be loaded
    */
    public StyledDocument openDocument () throws IOException {
        return del.openDocument ();
    }

    /** Get the document. This method may be called before the document initialization
     * (<code>prepareTask</code>)
     * has been completed, in such a case the document must not be modified.
     * @return document or <code>null</code> if it is not yet loaded
     */
    public StyledDocument getDocument () {
        return del.getDocument ();
    }

    /** Test whether the document is in memory, or whether loading is still in progress.
    * @return <code>true</code> if document is loaded
    */
    public boolean isDocumentLoaded() {
        return del.isDocumentLoaded ();
    }

    /** Save the document in this thread.
    * Create 'orig' document for the case that the save would fail.
    * @exception IOException on I/O error
    */
    public void saveDocument () throws IOException {
        del.superSaveDocument ();
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
    protected void saveFromKitToStream (StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        del.superSaveFromKitToStream (doc, kit, stream);
    }

    /** Test whether the document is modified.
    * @return <code>true</code> if the document is in memory and is modified;
    *   otherwise <code>false</code>
    */
    public boolean isModified () {
        return del.isModified ();
    }

    /** Finds data object the entry belongs to.
    * @return data object or null
    */
    protected MultiDataObject findDataObject () {
        return entry.getDataObject ();
    }

    /** Create a position reference for the given offset.
    * The position moves as the document is modified and
    * reacts to closing and opening of the document.
    *
    * @param offset the offset to create position at
    * @param bias the Position.Bias for new creating position.
    * @return position reference for that offset
    */
    public final PositionRef createPositionRef (int offset, Position.Bias bias) {
        return del.createPositionRef (offset, bias);
    }

    /** Get the line set for all paragraphs in the document.
    * @return positions of all paragraphs on last save
    */
    public Line.Set getLineSet () {
        return del.getLineSet ();
    }

    // other public methods ................................................................

    /**
    * Set the MIME type for the document.
    * @param s the new MIME type
    */
    public void setMIMEType (String s) {
        del.setMIMEType (s);
    }

    /** @deprecated has no effect
    */
    @Deprecated
    public void setActions (SystemAction[] actions) {
    }

    /** Creates editor kit for this source.
    * @return editor kit
    */
    protected EditorKit createEditorKit () {
        return del.superCreateEditorKit ();
    }

    /** Utility method which enables or disables listening to modifications
    * on asociated document.
    * <P>
    * Could be useful if we have to modify document, but do not want the
    * Save and Save All actions to be enabled/disabled automatically.
    * Initially modifications are listened to.
    * @param listenToModifs whether to listen to modifications
    */
    public void setModificationListening (final boolean listenToModifs) {
        this.listenToModifs = listenToModifs;
    }

    /** Adds a listener for status changes. An event is fired
    * when the document is moved or removed from memory.
    * @param l new listener
    */
    public void addChangeListener (ChangeListener l) {
        del.addChangeListener (l);
    }

    /** Removes a listener for status changes.
     * @param l listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        del.removeChangeListener (l);
    }

    public final void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        del.addPropertyChangeListener (l);
    }
    
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        del.removePropertyChangeListener (l);
    }

    /** The implementation of @see org.openide.cookies.PrintCookie#print() method. */
    public void print() {
        del.print ();
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
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        del.superLoadFromStreamToKit (doc, stream, kit);
    }
    
    /** Reload the document in response to external modification.
    * @see #reloadDocumentTask
    */
    protected void reloadDocument() {
        reloadDocumentTask ().waitFinished ();
    }

    /** Starts reloading of document. Could not be named reloadDocument,
     * because of backward compatibility.
     *
     * @return task one can listen on when reloading the document
     */
    protected Task reloadDocumentTask () {
        return del.superReloadDocument ();
    }

    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @return always non-<code>null</code> editor
    */
    protected Editor openAt(PositionRef pos) {
        CloneableEditorSupport.Pane p = del.openAt (pos, -1);
        if (p instanceof Editor) {
            return (Editor)p;
        }
        java.awt.Component c = p.getEditorPane();
        for (;;) {
            if (c instanceof Editor) {
                return (Editor)c;
            }
            c = c.getParent();
        }
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose () {
        return del.superCanClose ();
    }

    /* List of all JEditorPane's opened by this editor support.
    * The first item in the array should represent the component
    * that is currently selected or has been selected lastly.
    *
    * @return array of panes or null if no pane is opened.
    *   In no case empty array is returned.
    */
    public JEditorPane[] getOpenedPanes () {
        return del.getOpenedPanes ();
    }

    /** Notification method called when the document become unmodified.
    * Called after save or after reload of document.
    */
    protected void notifyUnmodified () {
        EditorSupport.this.modifySaveCookie (false);
        del.superNotifyUnmodified ();
    }

    /** Overrides the super method to add a save cookie if the 
    * document has been marked modified.
    *
    * @return true if the environment accepted being marked as modified
    *    or false if it refused it and the document should still be unmodified
    */
    protected boolean notifyModified () {
        if (del.superNotifyModified ()) {
            EditorSupport.this.modifySaveCookie (true);
            return true;
        } else {
            return false;
        }
    }


    /** Called when the document is closed and released from memory.
    */
    protected void notifyClosed() {
        del.superNotifyClosed ();
    }

    
    /** Utility method to extract EditorSupport from Del instance.
     * @param ces cloneable editor support
     * @return EditorSupport
     * @exception ClassCastException if the variables do not match
     */
    static EditorSupport extract (CloneableEditorSupport ces) {
        EditorSupport.Del del = (Del)ces;
        return del.es ();
    }
    
    
    /** Modifies the save cookie, if necessary
    * @param add true if we should add the cookie
    */
    final void modifySaveCookie (boolean add) {
        if (listenToModifs) {
            if (add) {
                ((EntryEnv)env).addSaveCookie ();
            } else {
                ((EntryEnv)env).removeSaveCookie ();
            }
        }
    }

    
    /** Cloneable top component to hold the editor kit.
    */
    public static class Editor extends CloneableEditor {
        /** data object to work with */
        protected DataObject obj;

        static final long serialVersionUID =-185739563792410059L;
        
        /** For externalization of subclasses only */
        public Editor () {
            super();
        }

        /** Constructor
        * @param obj data object we belong to. The appropriate editor support is 
        * acquired as the DataObject's EditorSupport.class cookie.
        */
        public Editor (DataObject obj) {
            this(obj, obj.getCookie(EditorSupport.class));
        }

        /** Constructor
        * @param obj data object we belong to. 
        * @param support editor support to use.
        */
        public Editor (DataObject obj, EditorSupport support) {
            super (support.del);
            this.obj = obj;
        }
        
        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            
            Object ces = cloneableEditorSupport ();
            if (ces instanceof Del) {
                obj = ((Del)ces).getDataObjectHack2 ();
            }
            
        }

    } // end of Editor inner class

    /** Special implementation of CloneableEditorSupport that is used 
    * by all methods of EditorSupport to delegate on it.
    */
    private final class Del extends DataEditorSupport implements EditorCookie.Observable {
        
        /** Listener on node changes. */
        private NodeListener nodeL;
        
        
        /** Constrcutor. Takes environemnt and ref object to replace
        * the default one
        */
        public Del (
            DataObject obj,
            CloneableEditorSupport.Env env,
            CloneableTopComponent.Ref ref
        ) {
            super (obj, env);
            this.allEditors = ref;
        }
        
        /** Getter */
        public final EditorSupport es () {
            return EditorSupport.this;
        }
        
        protected void notifyUnmodified () {
            EditorSupport.this.notifyUnmodified ();
        }

        protected boolean notifyModified () {
            return EditorSupport.this.notifyModified ();
        }

        protected void notifyClosed() {
            EditorSupport.this.notifyClosed ();
        }

        final void superNotifyUnmodified () {
            super.notifyUnmodified ();
        }

        final boolean superNotifyModified () {
            return super.notifyModified ();
        }
        final void superNotifyClosed() {
            // #28256(#27645) Unregisters lisntening on node
            // when all components closed.
            nodeL = null;
            
            super.notifyClosed ();
        }

        protected CloneableEditor createCloneableEditor() {
            if (true) throw new IllegalStateException ("Do not call!");
            CloneableTopComponent ctc = createCloneableTopComponent();
            if(ctc instanceof Editor) {
                return (CloneableEditor)ctc;
            } else {
                return new Editor(getDataObject());
            }
        }
        
        
        protected Pane createPane () {
            CloneableTopComponent ctc = createCloneableTopComponent();
            if(ctc instanceof Editor) {
                return (CloneableEditor)ctc;
            } else {
                Pane pan = (Pane)ctc.getClientProperty("CloneableEditorSupport.Pane");
                if (pan != null) {
                    return pan;
                }
                if (ctc instanceof Pane) {
                    return (Pane)ctc;
                }
                return new Editor(getDataObject());
            }
            
        }
        
        //
        // Messages
        // 
        protected String messageToolTip() {
            return EditorSupport.this.messageToolTip ();
        }
        
        protected String messageName() {
            return EditorSupport.this.messageName ();
        }
        
        protected String messageOpening() {
            return EditorSupport.this.messageOpening ();
        }
        
        protected String messageOpened() {
            return EditorSupport.this.messageOpened ();
        }
        
        protected String messageSave() {
            return EditorSupport.this.messageSave ();
        }
        
        protected void updateTitles () {
            EditorSupport.this.updateTitles ();
        }


        final String superMessageToolTip() {
            return super.messageToolTip ();
        }

        final String superMessageName() {
            return super.messageName ();
        }

        final String superMessageOpening() {
            return super.messageOpening ();
        }

        final String superMessageOpened() {
            return super.messageOpened ();
        }

        final String superMessageSave() {
            return super.messageSave ();
        }
        
        final void superUpdateTitles () {
            super.updateTitles ();
        }
        
        
        //
        // close 
        //
        
        protected boolean close(boolean ask) {
            return EditorSupport.this.close (ask);
        }
        
        protected boolean superClose(boolean ask) {
            return super.close (ask);
        }
        
        /** Overrides superclass method. Delegates the creation
         * of component to enclosing class and adds initializing of
         * editor component. */
        protected CloneableTopComponent createCloneableTopComponent() {
            CloneableTopComponent ctc =  EditorSupport.this.createCloneableTopComponent ();
            if(ctc instanceof CloneableEditor) {
                initializeCloneableEditor((CloneableEditor)ctc);
            }
            return ctc;
        }
        
        /** Overrides superclass method. Initializes editor component. */
        protected void initializeCloneableEditor (CloneableEditor editor) {
            DataObject obj = getDataObject();
            if(obj.isValid()) {
                org.openide.nodes.Node ourNode = obj.getNodeDelegate();
                editor.setActivatedNodes(new org.openide.nodes.Node[] {ourNode});
                editor.setIcon(ourNode.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
                NodeListener nl = new DataNodeListener(editor);
                ourNode.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener (nl, ourNode));
                nodeL = nl;
            }
        }
        
        
        //
        // Stream manipulation
        // 
        final void superLoadFromStreamToKit(
            StyledDocument doc,InputStream stream,EditorKit kit
        ) throws IOException, BadLocationException {
            super.loadFromStreamToKit (doc, stream, kit);
        }
        
        protected void loadFromStreamToKit(
            StyledDocument doc,InputStream stream,EditorKit kit
        ) throws IOException, BadLocationException {
            EditorSupport.this.loadFromStreamToKit (doc, stream, kit);
        }
        
        protected void superSaveFromKitToStream(
            StyledDocument doc,EditorKit kit,OutputStream stream
        ) throws IOException, BadLocationException {
            super.saveFromKitToStream (doc, kit, stream);
        }
        protected void saveFromKitToStream(
            StyledDocument doc,EditorKit kit,OutputStream stream
        ) throws IOException, BadLocationException {
            EditorSupport.this.saveFromKitToStream (doc, kit, stream);
        }

        protected Task reloadDocument () {
            return EditorSupport.this.reloadDocumentTask ();
        }

        final Task superReloadDocument () {
            return super.reloadDocument ();
        }
        
        public void saveDocument() throws IOException {
            EditorSupport.this.saveDocument();
        }
        
        final void superSaveDocument() throws IOException {
            super.saveDocument();
        }
        final UndoRedo.Manager superUndoRedoManager() {
            return super.createUndoRedoManager ();
        }
        
        // 
        // Undo manager 
        //
        protected UndoRedo.Manager createUndoRedoManager() {
            return EditorSupport.this.createUndoRedoManager ();
        }
        
        // 
        // Editor kit
        //
        EditorKit superCreateEditorKit() {
            return super.createEditorKit ();
        }
        protected EditorKit createEditorKit() {
            return EditorSupport.this.createEditorKit ();
        }
        
/*        
        protected StyledDocument createStyledDocument(EditorKit kit) {
            return EditorSupport.this.createStyledDocument (kit);
        }
*/
        //
        // canClose
        //
        final boolean superCanClose() {
            return super.canClose ();
        }
        protected boolean canClose() {
            return EditorSupport.this.canClose ();
        }


        /** Class which supports listening on node delegate. */
        private final class DataNodeListener extends NodeAdapter {
            /** Asociated editor */
            private final CloneableEditor editor;

            DataNodeListener (CloneableEditor editor) {
                this.editor = editor;
            }

            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                    updateTitles();
                }
                if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                    final DataObject obj = getDataObject();
                    if (obj.isValid()) {
                        org.openide.util.Mutex.EVENT.writeAccess(new Runnable()  {
                            public void run() {
                        editor.setIcon(obj.getNodeDelegate().getIcon (
                                java.beans.BeanInfo.ICON_COLOR_16x16));
                            }
                        });
                    }
                }
            }
        } // End of DataNodeListener class.
        
    }
    
    
    /** Implementation of the default Environment for EditorSupport
    */
    private static class EntryEnv extends DataEditorSupport.Env
    implements SaveCookie {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 354528097109874355L;

        /** Constructor.
        * @param obj this support should be associated with
        */
        public EntryEnv (MultiDataObject obj) {
            super (obj);
        }
        
        /** Getter for file associated with this environment.
        * @return the file input/output operation should be performed on
        */
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }
        
        /** Locks the file.
        * @return the lock on the file getFile ()
        * @exception IOException if the file cannot be locked
        */
        protected FileLock takeLock () throws IOException {
            return ((MultiDataObject)getDataObject ()).getPrimaryEntry ().takeLock ();
        }
        
        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())) {
                changeFile ();
            }
            if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                EditorSupport es = getDataObject().getCookie(EditorSupport.class);
                if (es != null) {
                    es.updateTitles ();
                }
            }
            
            super.propertyChange (ev);
        }
        
        
        /** Invoke the save operation.
        * @throws IOException if the object could not be saved
        */
        public void save() throws java.io.IOException {
            // Do not use findCloneableOpenSupport; it will not work if the
            // DataObject has both an EditorSupport and an OpenSupport attached
            // at once.
            EditorSupport es = getDataObject().getCookie(EditorSupport.class);
            if (es == null)
                throw new IOException ("no EditorSupport found on this data object"); // NOI18N
            else
                es.saveDocument ();
        }
/*
        void clearSaveCookie() {
            DataObject dataObj = findDataObject();
            // remove save cookie (if save was succesfull)
            dataObj.setModified(false);
            releaseFileLock();
        }
*/
        /** Adds save cookie to the DO.
        */
        final void addSaveCookie() {
            DataObject dataObj = getDataObject ();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == null) {
                    getCookieSet((MultiDataObject)dataObj).add(this);
                }
            }
        }
        
        /** Removes save cookie from the DO.
        */
        final void removeSaveCookie() {
            DataObject dataObj = getDataObject ();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == this) {
                    getCookieSet((MultiDataObject)dataObj).remove(this);
                }
            }
        }

        // UGLY
        private static java.lang.reflect.Method getCookieSetMethod = null;
        private static final org.openide.nodes.CookieSet getCookieSet (MultiDataObject obj) {
            try {
                if (getCookieSetMethod == null) {
                    getCookieSetMethod = MultiDataObject.class.getDeclaredMethod ("getCookieSet", new Class[] { }); // NOI18N
                    getCookieSetMethod.setAccessible (true);
                }
                return (org.openide.nodes.CookieSet) getCookieSetMethod.invoke (obj, new Object[] { });
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return new org.openide.nodes.CookieSet ();
            }
        }
        
        /** Method that allows environment to find its 
         * cloneable open support.
        * @return the support or null if the environemnt is not in valid 
        * state and the CloneableOpenSupport cannot be found for associated
        * data object
        */
        public CloneableOpenSupport findCloneableOpenSupport() {
            CloneableOpenSupport s = super.findCloneableOpenSupport ();
            if (s != null) {
                return s;
            }
                
            EditorSupport es = getDataObject().getCookie(EditorSupport.class);
            if (es != null) {
                return es.del;
            } else {
                return null;
            }
        }
        
    } // end of EntryEnv
    
    /** Environment for delegating object.
    */
    private static final class DelEnv extends EntryEnv {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 174320972368471234L;
        
        public DelEnv (MultiDataObject obj) {
            super (obj);
        }
        
        /** Finds delegating environment for this editor object.
        */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
	    // Svata: is this really needed ? EditorSupport does not implement CloneableOpenSupport anyway.
            CloneableOpenSupport o = super.findCloneableOpenSupport ();
            if (o instanceof EditorSupport) {
                EditorSupport es = (EditorSupport)o;
                return es.del;
            }
            return o;
        }
    }
}
