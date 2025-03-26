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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.modules.openide.text.Installer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.*;
import org.openide.windows.*;

/** Cloneable top component to hold the editor kit.
 */
public class CloneableEditor extends CloneableTopComponent implements CloneableEditorSupport.Pane {

    private static final String HELP_ID = "editing.editorwindow"; // !!! NOI18N
    
    static final long serialVersionUID = -185739563792410059L;
    
    /** editor pane  */
    protected JEditorPane pane;

    /** Asociated editor support  */
    private CloneableEditorSupport support;

    /**
     * Flag to detect if component is opened or closed to control return value
     * of getEditorPane. If component creation starts this flag is set to true and
     * getEditorPane() waits till initialization finishes.
     */
    private boolean componentCreated = false;
    
    /** Position of cursor. Used to keep the value between deserialization
     * and initialization time. */
    private int cursorPosition = -1;
    
    private final boolean[] CLOSE_LAST_LOCK = new boolean[1];

    /** Custom editor component, which is used if specified by document
     * which implements <code>NbDocument.CustomEditor</code> interface.
     * @see NbDocument.CustomEditor#createEditor */
    private Component customComponent;

    private CloneableEditorInitializer initializer;
    
    static final Logger LOG = Logger.getLogger("org.openide.text.CloneableEditor"); // NOI18N
    
    /** For externalization of subclasses only  */
    public CloneableEditor() {
        this(null);
    }

    /** Creates new editor component associated with
    * support object.
    * @param support support that holds the document and operations above it
    */
    public CloneableEditor(CloneableEditorSupport support) {
        this(support, false);
    }

    /** Creates new editor component associated with
    * support object (possibly also with its 
    * {@link CloneableEditorSupport#CloneableEditorSupport(org.openide.text.CloneableEditorSupport.Env, org.openide.util.Lookup) lookup}.
    * 
    * @param support support that holds the document and operations above it
    * @param associateLookup true, if {@link #getLookup()} should return the lookup
    *   associated with {@link CloneableEditorSupport}.
    */
    public CloneableEditor(CloneableEditorSupport support, boolean associateLookup) {
        super();
        this.support = support;

        updateName();
        _setCloseOperation();
        setMinimumSize(new Dimension(10, 10));
        if (associateLookup) {
            associateLookup(support.getLookup());
        }
    }
    @SuppressWarnings("deprecation")
    private void _setCloseOperation() {
        setCloseOperation(CLOSE_EACH);
    }

    /** Gives access to {@link CloneableEditorSupport} object under
     * this <code>CloneableEditor</code> component.
     * @return the {@link CloneableEditorSupport} object
     *         that holds the document or <code>null</code>, what means
     *         this component is not in valid state yet and can be discarded */
    protected CloneableEditorSupport cloneableEditorSupport() {
        return support;
    }

    /** Overriden to explicitely set persistence type of CloneableEditor
     * to PERSISTENCE_ONLY_OPENED */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    /** Get context help for this editor pane.
     * If the registered editor kit provides a help ID in bean info
     * according to the protocol described for {@link HelpCtx#findHelp},
     * then that it used, else general help on the editor is provided.
     * @return context help
     */
    @Override
    public HelpCtx getHelpCtx() {
        Object kit = support.cesKit();
        HelpCtx fromKit = kit == null ? null : HelpCtx.findHelp(kit);

        if (fromKit != null) {
            return fromKit;
        } else {
            return new HelpCtx(HELP_ID);
        }
    }

    /**
     * Indicates whether this component can be closed.
     * Adds scheduling of "emptying" editor pane and removing all sub components.
     * {@inheritDoc}
     */
    @Override
    public boolean canClose() {
        boolean result = super.canClose();
        return result;
    }

    /** Overrides superclass method. In case it is called first time,
     * initializes this <code>CloneableEditor</code>. */
    @Override
    protected void componentShowing() {
        super.componentShowing();
        initialize();
    }

    /**
     * Performs needed initialization.
     * The method should only be invoked from EDT.
     */
    private void initialize() {
        // Only called form EDT
        if (pane != null || discard()) {
            return;
        }

        QuietEditorPane tmp = new QuietEditorPane();
        tmp.putClientProperty("usedByCloneableEditor", true);

        this.pane = tmp;
        
        synchronized (getInitializerLock()) {
            this.componentCreated = true;
            initializer = new CloneableEditorInitializer(this, support, pane);
        }
        initializer.start(); // Initializer variable will be cleared by the initializer task itself later.
    }
    
    private Object getInitializerLock() {
        return CloneableEditorInitializer.edtRequests;
    }
    
    boolean isInitializationRunning() {
        synchronized (getInitializerLock()) {
            boolean running = (initializer != null);
            return running;
        }
    }
    
    boolean isProvideUnfinishedPane() {
        synchronized (getInitializerLock()) {
            return (initializer != null) && initializer.isProvideUnfinishedPane();
        }
    }
    
    void markInitializationFinished(boolean success) {
        synchronized (getInitializerLock()) {
            initializer = null;
            if (!success) {
                pane = null;
            }
            // Notify possible waiting clients that initialization is finished
            CloneableEditorInitializer.notifyEDTRequestsMonitor();
        }
    }
    
    /** Asks the associated {@link CloneableEditorSupport} to initialize
     * this editor via its {@link CloneableEditorSupport#initializeCloneableEditor(org.openide.text.CloneableEditor)}
     * method. By default called from the support on various occasions including
     * shortly after creation and 
     * after the {@link CloneableEditor} has been deserialized.
     * 
     * @since 6.37 
     */
    protected final void initializeBySupport() {
        cloneableEditorSupport().initializeCloneableEditor(this);
    }
    
    void setCustomComponent(Component customComponent) {
        this.customComponent = customComponent;
    }

    int getCursorPosition() {
        return cursorPosition;
    }

    void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    @Override
    protected CloneableTopComponent createClonedObject() {
        return support.createCloneableTopComponent();
    }

    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    @Override
    protected void componentOpened() {
        super.componentOpened();
        
        CloneableEditorSupport ces = cloneableEditorSupport();
        
        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
            Document d = ces.getDocument();
            if (d != null) {
                String mimeType = (String) d.getProperty("mimeType"); //NOI18N
                Installer.add(mimeType);
            }
        }
    }
    
    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    @Override
    protected void componentClosed() {
        // #23486: pane could not be initialized yet.
        if (pane != null) {
            // #114608 - commenting out setting of the empty document
//                        Document doc = support.createStyledDocument(pane.getEditorKit());
//                        pane.setDocument(doc);

            // #138611 - this calls kit.deinstall, which is what our kits expect,
            // calling it with null does not impact performance, because the pane
            // will not create new document and typically nobody listens on "editorKit" prop change
            pane.setEditorKit(null);
            pane.putClientProperty("usedByCloneableEditor", false);
        }

        synchronized (getInitializerLock()) {
            customComponent = null;
            pane = null;
            componentCreated = false;
        }
        
        super.componentClosed();

        CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        }
        if (ces.getAnyEditor() == null) {
            ces.close(false);
        }
    }

    /** When closing last view, also close the document. 
     * Calls {@link #closeLast(boolean) closeLast(true)}.
     * @return <code>true</code> if close succeeded
     */
    @Override
    protected boolean closeLast() {
        return closeLast(true);
    }
    
    /** Utility method to close the document. 
     * 
     * @param ask verify and ask the user whether a document can be closed or not?
     * @return true if the document was successfully closed
     * @since 6.37
     */
    protected final boolean closeLast(boolean ask) {
        if (ask) {
            if (!support.canClose()) {
                // if we cannot close the last window
                return false;
            }
        }

        // close everything and do not ask
        synchronized (CLOSE_LAST_LOCK) {
            if (CLOSE_LAST_LOCK[0]) {
                CLOSE_LAST_LOCK[0] = false;
            } else {
                support.notifyClosed();
            }
        }

        if (support.getLastSelected() == this) {
            support.setLastSelected(null);
        }

        return true;
    }

    /** The undo/redo manager of the support.
     * @return the undo/redo manager shared by all editors for this support
     */
    @Override
    public UndoRedo getUndoRedo() {
        return support.getUndoRedo();
    }

    @Override
    public Action[] getActions() {
        List<Action> actions = new ArrayList<Action>(Arrays.asList(super.getActions()));
        // XXX nicer to use MimeLookup for type-specific actions, but not easy; see org.netbeans.modules.editor.impl.EditorActionsProvider
        actions.add(null);
        actions.addAll(Utilities.actionsForPath("Editors/TabActions"));
        return actions.toArray(new Action[0]);
    }

    /** Transfer the focus to the editor pane.
     */
    @Deprecated
    @Override
    public void requestFocus() {
        super.requestFocus();

        if (pane != null) {
            if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
                customComponent.requestFocus();
            } else {
                pane.requestFocus();
            }
        }
    }

    /** Transfer the focus to the editor pane.
     */
    @Deprecated
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();

        if (pane != null) {
            if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
                return customComponent.requestFocusInWindow();
            } else {
                return pane.requestFocusInWindow();
            }
        }

        return false;
    }

    @Deprecated
    @Override
    public boolean requestDefaultFocus() {
        if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
            return customComponent.requestFocusInWindow();
        } else if (pane != null) {
            return pane.requestFocusInWindow();
        }

        return false;
    }

    // XXX is this method really needed?
    /** @return Preferred size of editor top component  */
    @Override
    public Dimension getPreferredSize() {
        @SuppressWarnings("deprecation")
        Rectangle bounds = WindowManager.getDefault().getCurrentWorkspace().getBounds();

        return new Dimension(bounds.width / 2, bounds.height / 2);
    }

    @Override
    public void open() {
        boolean wasNull = getClientProperty( "TopComponentAllowDockAnywhere" ) == null; //NOI18N
        super.open();
        if( wasNull ) {
            //since we don't define a mode to dock this editor to, the window
            //system thinks we're an uknown component allowed to dock anywhere
            //but editor windows can dock into editor modes only, so let's clear
            //the 'special' flag
            putClientProperty( "TopComponentAllowDockAnywhere", null); //NOI18N
        }
    }

    /**
     * Overrides superclass method. Remembers last selected component of
     * support belonging to this component.
     *
     * Descendants overriding this method must call this implementation to set last
     * selected pane otherwise <code>CloneableEditorSupport.getRecentPane</code> and
     * <code>CloneableEditorSupport.getOpenedPanes</code> will be broken.
     *
     * @see #componentDeactivated
     */
    @Override
    protected void componentActivated() {
        support.setLastSelected(this);
    }
    
    /** Updates the name and tooltip of this <code>CloneableEditor</code>
     * {@link org.openide.windows.TopComponent TopCompoenent}
     * according to the support retrieved from {@link #cloneableEditorSupport}
     * method. The name and tooltip are in case of support presence
     * updated thru its {@link CloneableEditorSupport#messageName} and
     * {@link CloneableEditorSupport#messageToolTip} methods.
     * @see #cloneableEditorSupport() */
    public void updateName() {
        final CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            Mutex.EVENT.writeAccess(
                new Runnable() {
                    public void run() {
                        String name = ces.messageHtmlName();
                        setHtmlDisplayName(name);
                        name = ces.messageName();
                        setDisplayName(name);
                        setName(name); // XXX compatibility

                        setToolTipText(ces.messageToolTip());
                    }
                }
            );
        }
    }

    // override for simple and consistent IDs
    @Override
    protected String preferredID() {
        final CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            return ces.documentID();
        }

        return "";
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        // Save environent if support is non-null.
        // XXX #13685: When support is null, the tc will be discarded 
        // after deserialization.
        out.writeObject((support != null) ? support.cesEnv() : null);

        // #16461 Caret could be null?!,
        // hot fix - making it robust for that case.
        int pos = 0;

        // 19559 Even pane could be null! Better solution would be put
        // writeReplace method in place also, but it is a API change. For
        // the time be just robust here.
        JEditorPane p = pane;

        if (p != null) {
            Caret caret = p.getCaret();

            if (caret != null) {
                pos = caret.getDot();
            } else {
                if (p instanceof QuietEditorPane) {
                    int lastPos = ((QuietEditorPane) p).getLastPosition();

                    if (lastPos == -1) {
                        Logger.getLogger(CloneableEditor.class.getName()).log(Level.WARNING, null,
                                          new java.lang.IllegalStateException("Pane=" +
                                                                              p +
                                                                              "was not initialized yet!"));
                    } else {
                        pos = lastPos;
                    }
                } else {
                    Document doc = ((support != null) ? support.getDocument() : null);

                    // Relevant only if document is non-null?!
                    if (doc != null) {
                        Logger.getLogger(CloneableEditor.class.getName()).log(Level.WARNING, null,
                                          new java.lang.IllegalStateException("Caret is null in editor pane=" +
                                                                              p +
                                                                              "\nsupport=" +
                                                                              support +
                                                                              "\ndoc=" +
                                                                              doc));
                    }
                }
            }
        }

        out.writeObject(new Integer(pos));
        out.writeBoolean(getLookup() == support.getLookup());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int offset;

        Object firstObject = in.readObject();

        // New deserialization that uses Env environment,
        // and which could be null(!) see writeExternal.
        if (firstObject instanceof CloneableOpenSupport.Env) {
            CloneableOpenSupport.Env env = (CloneableOpenSupport.Env) firstObject;
            CloneableOpenSupport os = env.findCloneableOpenSupport();
            support = (CloneableEditorSupport) os;
        }

        // load cursor position
        offset = ((Integer) in.readObject()).intValue();

        if (!discard()) {
            cursorPosition = offset;
        }

        updateName();
        componentCreated = true;
        if (in.available() > 0) {
            boolean associate = in.readBoolean();
            if (associate && support != null) {
                associateLookup(support.getLookup());
            }
        }
    }

    /**
     * Replaces serializing object. Overrides superclass method. Adds checking
     * for object validity. In case this object is invalid
     * throws {@link java.io.NotSerializableException NotSerializableException}.
     * @throws ObjectStreamException When problem during serialization occures.
     * @throws NotSerializableException When this <code>CloneableEditor</code>
     *               is invalid and doesn't want to be serialized. */
    @Override
    protected Object writeReplace() throws ObjectStreamException {
        if (discard()) {
            throw new NotSerializableException("Serializing component is invalid: " + this); // NOI18N
        }

        return super.writeReplace();
    }

    /**
     * Resolves deserialized object. Overrides superclass method. Adds checking
     * for object validity. In case this object is invalid
     * throws {@link java.io.InvalidObjectException InvalidObjectException}.
     * @throws ObjectStreamException When problem during serialization occures.
     * @throws java.io.InvalidObjectException When deserialized <code>CloneableEditor</code>
     *              is invalid and shouldn't be used. */
    protected Object readResolve() throws ObjectStreamException {
        if (discard()) {
            throw new java.io.InvalidObjectException("Deserialized component is invalid: " + this); // NOI18N
        } else {
            initializeBySupport();

            return this;
        }
    }

    /** This component should be discarded if the associated environment
    * is not valid.
    */
    private boolean discard() {
        return (support == null) || !support.cesEnv().isValid();
    }
    
    //
    // Implements the CloneableEditorSupport.Pane interface
    //
    public CloneableTopComponent getComponent() {
        return this;
    }

    /**
     * #168415: Returns true if creation of editor pane is finished. It is used
     * to avoid blocking AWT thread by call of getEditorPane.
     */
    boolean isEditorPaneReady () {
        assert SwingUtilities.isEventDispatchThread();
        return isEditorPaneReadyImpl();
    }
    
    /** Used from test only. Can be called out of AWT */
    boolean isEditorPaneReadyTest () {
        return isEditorPaneReadyImpl();
    }

    private boolean isEditorPaneReadyImpl() {
        return (pane != null) && !isInitializationRunning();
    }

    /** Returns editor pane. Returns null if document loading was canceled by user
     * through answering UserQuestionException or when CloneableEditor is closed.
     *
     * @return editor pane or null
     */
    public JEditorPane getEditorPane() {
        assert SwingUtilities.isEventDispatchThread();
        //User selected not to load document
        if (!componentCreated) {
            return null;
        }
        //#175528: This case should not happen as modal dialog handling UQE should
        //not be displayed during IDE start ie. during component deserialization.
        if (CloneableEditorInitializer.modalDialog) {
            LOG.log(Level.WARNING,"AWT is blocked by modal dialog. Return null from CloneableEditor.getEditorPane."
            + " Please report this to IZ.");
            LOG.log(Level.WARNING,"support:" + support.getClass().getName());
            Exception ex = new Exception();
            StringWriter sw = new StringWriter(500);
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            LOG.log(Level.WARNING,sw.toString());
            return null;
        }

        initialize();

        CloneableEditorInitializer.waitForFinishedInitialization(this);
        return pane;
    }
    
    /**
     * callback for the Pane implementation to adjust itself to the openAt() request.
     */
    @Override
    public void ensureVisible() {
        open();
        requestVisible();
    }
}
