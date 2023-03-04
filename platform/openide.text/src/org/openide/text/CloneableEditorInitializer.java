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
package org.openide.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Initializer of the CloneableEditor component.
 *
 * @author Miloslav Metelka
 */
final class CloneableEditorInitializer implements Runnable {

    // Use CloneableEditor.LOG due to CloneableEditorCreationFinishedTest.FocusHandler.assertFocused()
    private static final Logger EDITOR_LOG = CloneableEditor.LOG;
    
    // -J-Dorg.openide.text.CloneableEditorInitializer.level=FINE
    private static final Logger LOG = Logger.getLogger(CloneableEditorInitializer.class.getName());
    
    private static final RequestProcessor RP = new RequestProcessor("org.openide.text Editor Initialization");
    
    static final Logger TIMER = Logger.getLogger("TIMER"); // NOI18N
    
    /**
     * Flag indicating if modal dialog for handling UQE is displayed. If it is
     * yes we cannot handle call of getEditorPane from modal EQ because it
     * results in deadlock.
     */
    static boolean modalDialog;

    static final List<Runnable> edtRequests = new ArrayList<Runnable>(2);
    
    static final Runnable processPendingEDTRequestsRunnable = new Runnable() {
        @Override
        public void run() {
            processPendingEDTRequests();
        }
    };

    static void waitForFinishedInitialization(CloneableEditor editor) { // Should only be called from EDT
        assert (SwingUtilities.isEventDispatchThread()) : "Method should only be called from EDT"; // NOI18N
        while (true) {
            // First check if initialization is even running (there might be init requests
            // from other CEs in edtRequests and there would be no need to run them)
            synchronized (edtRequests) {
                if (!editor.isInitializationRunning()) {
                    return;
                }
            }
            // Do not wait in case when query comes from EDT and initializer phase is just running
            // because such wait would never end.
            if (editor.isProvideUnfinishedPane()) {
                return;
            }
            processPendingEDTRequests();
            synchronized (edtRequests) {
                if (!editor.isInitializationRunning()) {
                    return;
                } else {
                    try {
                        // Wait since the initializer will notify once it's finished
                        // or when a next phase EDT request gets added to edtRequests
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("CEI:Will wait() editor=" + System.identityHashCode(editor) + '\n'); // NOI18N
                        }
                        // Wait for a limited time in case the notify() would not arrive (to find a fix for issue #235319)
                        edtRequests.wait(5000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
    
    static void processPendingEDTRequests() { // Should only be called from EDT
        while (true) {
            Runnable request;
            synchronized (edtRequests) {
                if (!edtRequests.isEmpty()) {
                    request = edtRequests.remove(0);
                } else {
                    break;
                }
            }
            if (request != null) {
                request.run();
            }
        }
    }
    
    static void addEDTRequest(Runnable request) {
        synchronized (edtRequests) {
            edtRequests.add(request);
            // Notify possible EDT waiters to fetch and process the request
            notifyEDTRequestsMonitor();
        }
    }
    
    static void notifyEDTRequestsMonitor() {
        synchronized (edtRequests) {
            edtRequests.notifyAll();
        }
    }

    final CloneableEditor editor;
    
    final CloneableEditorSupport ces;
    
    final JEditorPane pane;
    
    StyledDocument doc;
    
    private Phase phase;

    private RequestProcessor.Task task;

    private EditorKit kit;

    private JLabel loadingLabel;
    
    private UserQuestionException uqe;
    
    boolean provideUnfinishedPane;

    enum Phase {
        
        DOCUMENT_OPEN(false),
        HANDLE_USER_QUESTION_EXCEPTION(true),
        ACTION_MAP(true),
        INIT_KIT(false),
        KIT_AND_DOCUMENT_TO_PANE(true),
        CUSTOM_EDITOR_AND_DECORATIONS(true),
        FIRE_PANE_READY(true),
        ANNOTATIONS(false),
        ;

        private final boolean runInEDT;
        
        Phase(boolean runInEDT) {
            this.runInEDT = runInEDT;
        }

        public boolean isRunInEDT() {
            return runInEDT;
        }
        
    }

    
    CloneableEditorInitializer(CloneableEditor editor, CloneableEditorSupport ces, JEditorPane pane) {
        this.editor = editor;
        this.ces = ces;
        this.pane = pane;
    }
    
    void start() {
        boolean success = false;
        try {
            kit = ces.createEditorKit();
            addLoadingLabel();
            task = RP.create(this);
            task.setPriority(Thread.MIN_PRIORITY + 2);
            nextPhase(); // Process first phase
            success = true;
        } finally {
            if (!success) {
                cancelInitialization();
            }
        }
    }
    
    boolean nextPhase() {
        if (phase == null) {
            phase = Phase.DOCUMENT_OPEN;
        } else {
            int nextOrdinal = phase.ordinal() + 1;
            if (nextOrdinal < Phase.values().length) {
                phase = Phase.values()[nextOrdinal];
            } else {
                return false;
            }
        }

        boolean success = false;
        try {
            // Schedule the task
            if (phase.isRunInEDT()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("CEI:addEDTRequest(): " + this + '\n'); // NOI18N
                }
                addEDTRequest(this);
                // Ensure that the requests gets processed
                WindowManager.getDefault().invokeWhenUIReady(processPendingEDTRequestsRunnable);
    //            Mutex.EVENT.readAccess(processPendingEDTRequestsRunnable);
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("CEI:task.schedule(): " + this + '\n'); // NOI18N
                }
                task.schedule(0);
            }
            success = true;
        } finally {
            if (!success) {
                cancelInitialization();
            }
        }
        return true;
    }
    
    boolean isProvideUnfinishedPane() {
        return provideUnfinishedPane;
    }
    
    void cancelInitialization() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CEI:cancelInitialization(): " + this + '\n'); // NOI18N
        }
        editor.markInitializationFinished(false);

        // Close the top component
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent toClose = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, editor);
                if (null == toClose) {
                    toClose = editor;
                }
                toClose.close();
            }
        });
    }
    
    void finishInitialization() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CEI:finishInitialization(): " + this + '\n'); // NOI18N
        }
        editor.markInitializationFinished(true);
    }

    private void addLoadingLabel() {
        editor.setLayout(new BorderLayout());
        loadingLabel = new JLabel(NbBundle.getMessage(CloneableEditor.class, "LBL_EditorLoading")); // NOI18N
        loadingLabel.setOpaque(true);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        loadingLabel.setVisible(false);
        editor.add(loadingLabel, BorderLayout.CENTER);
    }

    @SuppressWarnings("fallthrough")
    public void run() {
        long now = System.currentTimeMillis();
        boolean success = false; // determine if phase ended with success
        try {
            switch (phase) {
                case DOCUMENT_OPEN:
                    success = initDocument();
                    break;
                case HANDLE_USER_QUESTION_EXCEPTION:
                    success = handleUserQuestionExceptionInEDT();
                    break;
                case ACTION_MAP:
                    success = initActionMapInEDT();
                    break;
                case INIT_KIT:
                    success = initKit();
                    break;
                case KIT_AND_DOCUMENT_TO_PANE:
                    success = setKitAndDocumentToPaneInEDT();
                    break;
                case CUSTOM_EDITOR_AND_DECORATIONS:
                    success = initCustomEditorAndDecorationsInEDT();
                    break;
                case FIRE_PANE_READY:
                    success = firePaneReadyInEDT();
                    break;
                case ANNOTATIONS:
                    // Initialization of annotations should not affect the opening process success
                    initAnnotations();
                    success = true;
                    break;
                    
                default:
                    throw new IllegalStateException("Wrong state: " + phase + " for " + ces);
            }

        } catch (RuntimeException ex) {
            Exceptions.printStackTrace(ex);
            // Re-throw the exception. The EDT clients may recieve the exception
            // if the current phase runs in EDT. If this would be a problem rethrowing
            // may be abandoned and replaced with 'return' only.
            throw ex;
        } finally {
            if (!success) {
                cancelInitialization(); // Cancel init - noitify possible EDT waiter(s)
                return;
            }
        }
    
        success = false;
        try {
            long howLong = System.currentTimeMillis() - now;
            if (TIMER.isLoggable(Level.FINE)) {
                String thread = SwingUtilities.isEventDispatchThread() ? "EDT" : "RP"; // NOI18N
                Document d = doc;
                Object who = d == null ? null : d.getProperty(Document.StreamDescriptionProperty);
                if (who == null) {
                    who = ces.messageName();
                }
                TIMER.log(Level.FINE,
                        "Open Editor, phase " + phase + ", " + thread + " [ms]",
                        new Object[]{who, howLong});
            }
            success = true;
        } finally {
            if (!success) {
                cancelInitialization();
            }
        }

        success = false;
        try {
            nextPhase();
            success = true;
        } finally {
            if (!success) {
                cancelInitialization();
            }
        }
        // Note: finishInitialization() called as part of CUSTOM_EDITOR_AND_DECORATIONS phase
    }

    private boolean initDocument() {
        if (EDITOR_LOG.isLoggable(Level.FINE)) {
            EDITOR_LOG.log(Level.FINE, "CloneableEditorInitializer.initDocument() Enter"
                    + " Time:" + System.currentTimeMillis()
                    + " Thread:" + Thread.currentThread().getName()
                    + " ce:[" + Integer.toHexString(System.identityHashCode(editor)) + "]"
                    + " support:[" + Integer.toHexString(System.identityHashCode(ces)) + "]"
                    + " Name:" + editor.getName());
        }

        try {
            ces.prepareDocument(); // Ensure prepareDocument() is called when existing component is deserialized after IDE restart
            setDocument(ces.openDocument());
            ces.getPositionManager().documentOpened(new WeakReference<StyledDocument>(doc));
            assert (doc != null) : "ces.openDocument() returned null"; // NOI18N
            return true;
        } catch (UserQuestionException ex) {
            uqe = ex; // Will be handled in next phase
            return true;

        } catch (IOException ex) {
            if (ex.getCause() != null) {
                Exceptions.printStackTrace(ex.getCause());
            }
            return false;
        }
    }
    
    boolean handleUserQuestionExceptionInEDT() {
        assert SwingUtilities.isEventDispatchThread() : "Not EDT"; // NOI18N
        if (uqe != null) {
            if (EDITOR_LOG.isLoggable(Level.FINE)) {
                EDITOR_LOG.fine("CEI:handleUserQuestionExceptionInEDT: uqe=" + uqe + "\n");
            }
            UserQuestionExceptionHandler handler = new UserQuestionExceptionHandler(ces, uqe) {
                @Override
                protected void opened(StyledDocument openDoc) {
                    setDocument(openDoc);
                }

                @Override
                protected void handleStart() {
                    modalDialog = true;
                }

                @Override
                protected void handleEnd() {
                    modalDialog = false;
                }
            };
            if (handler.handleUserQuestionException()) {
                uqe = null; // UQE answered processed and init can continue
            } else {
                cancelInitialization();
            }
        }

        // Here the document should be ready or initialization should be cancelled
        if (doc == null && editor.isInitializationRunning()) {
            throw new IllegalStateException("Null document for non-cancelled initialization. uqe=" + uqe);
        }
        
        return (uqe == null);
    }
    
    private void setDocument(StyledDocument doc) {
        this.doc = doc;
    }

    private boolean initActionMapInEDT() {
        // Init action map: cut,copy,delete,paste actions.
        javax.swing.ActionMap am = editor.getActionMap();

        //#43157 - editor actions need to be accessible from outside using the TopComponent.getLookup(ActionMap.class) call.
        // used in main menu enabling/disabling logic.
        javax.swing.ActionMap paneMap = pane.getActionMap();
        // o.o.windows.DelegateActionMap.setParent() leads to CloneableEditor.getEditorPane()
        provideUnfinishedPane = true;
        try {
            am.setParent(paneMap);
        } finally {
            provideUnfinishedPane = false;
        }

        //#41223 set the defaults befor the custom editor + kit get initialized, giving them opportunity to
        // override defaults..
        paneMap.put(DefaultEditorKit.cutAction, getAction(DefaultEditorKit.cutAction));
        paneMap.put(DefaultEditorKit.copyAction, getAction(DefaultEditorKit.copyAction));
        paneMap.put("delete", getAction(DefaultEditorKit.deleteNextCharAction)); // NOI18N
        paneMap.put(DefaultEditorKit.pasteAction, getAction(DefaultEditorKit.pasteAction));
        return true;
    }
    
    private boolean initKit() {
        if (kit instanceof Callable) {
            try {
                ((Callable) kit).call();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        return true;
    }

    private Action getAction(String key) {
        if (key == null) {
            return null;
        }

        // Try to find the action from kit.
        if (kit == null) { // kit is cleared in closeDocument()
            return null;
        }

        Action[] actions = kit.getActions();
        for (int i = 0; i < actions.length; i++) {
            if (key.equals(actions[i].getValue(Action.NAME))) {
                return actions[i];
            }
        }
        return null;
    }
    
    private void initCustomEditor() {
        if (doc instanceof NbDocument.CustomEditor) {
            NbDocument.CustomEditor ce = (NbDocument.CustomEditor) doc;
            Component customComponent;
            provideUnfinishedPane = true;
            try {
                customComponent = ce.createEditor(pane);
            } finally {
                provideUnfinishedPane = false;
            }
            if (customComponent == null) {
                throw new IllegalStateException(
                        "Document:" + doc // NOI18N
                        + " implementing NbDocument.CustomEditor may not" // NOI18N
                        + " return null component" // NOI18N
                        );
            }
            editor.setCustomComponent(customComponent);
            editor.add(ces.wrapEditorComponent(customComponent), BorderLayout.CENTER);

        } else {
            // remove default JScrollPane border, borders are provided by window system
            JScrollPane noBorderPane = new JScrollPane(pane);
            pane.setBorder(null);
            editor.add(ces.wrapEditorComponent(noBorderPane), BorderLayout.CENTER);

        }
    }

    private void initDecoration() {
        if (doc instanceof NbDocument.CustomToolbar) {
            NbDocument.CustomToolbar ce = (NbDocument.CustomToolbar) doc;
            JToolBar customToolbar;
            provideUnfinishedPane = true;
            try {
                customToolbar = ce.createToolbar(pane);
            } finally {
                provideUnfinishedPane = false;
            }
            if (customToolbar == null) {
                throw new IllegalStateException(
                        "Document:" + doc // NOI18N
                        + " implementing NbDocument.CustomToolbar may not" // NOI18N
                        + " return null toolbar"); // NOI18N
            }
            Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
            customToolbar.setBorder(b);
            editor.add(customToolbar, BorderLayout.NORTH);
        }
    }

    private boolean setKitAndDocumentToPaneInEDT() {
        provideUnfinishedPane = true;
        try {
            pane.setEditorKit(kit);
            // #132669, do not fire prior setting the kit, which by itself sets a bogus document, etc.
            // if this is a problem please revert the change and initialize QuietEditorPane.working = FIRE
            // and reopen #132669
            ((QuietEditorPane)pane).setWorking(QuietEditorPane.FIRE);

            pane.setDocument(doc); // doc should be non-null here
        } finally {
            provideUnfinishedPane = false;
        }
        return true;
    }
    
    private boolean initCustomEditorAndDecorationsInEDT() {
        initCustomEditor();
        initDecoration();
        editor.remove(loadingLabel);
        ((QuietEditorPane)pane).setWorking(QuietEditorPane.ALL);
        // set the caret to right possition if this component was deserialized
        int cursorPosition = editor.getCursorPosition();
        if (cursorPosition != -1) {
            Caret caret = pane.getCaret();
            if (caret != null) {
                caret.setDot(cursorPosition);
            }
        }
        ActionMap actionMap = editor.getActionMap();
        ActionMap p = actionMap.getParent();
        actionMap.setParent(null);
        actionMap.setParent(p);

        //#134910: If editor TopComponent is already activated request focus
        //to it again to get focus to correct subcomponent eg. QuietEditorPane which
        //is added above.
        if (shouldRequestFocus(pane)) {
            EDITOR_LOG.log(Level.FINE, "requestFocusInWindow {0}", pane);
            editor.requestFocusInWindow();
        }
        //#162961, #167289: Force repaint of editor. Sometimes editor stays empty.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editor.revalidate();
            }
        });

        // Mark the initialization finished here so that CloneableEditor.isEditorPaneReady() returns true
        // Do it before custom editor and decorations since they might query getEditorPane()
        // which would wait indeinitely for initialization completion.
        finishInitialization();
        return true;
    }
    
    private boolean firePaneReadyInEDT() {
        // Fire. Is EDT expected for firing PROP_OPENED_PANES?? Generally probably not but
        // getOpenedPanes() should come from EDT so it's handy.
        ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        return true;
    }
    
    private void initAnnotations() {
        ces.ensureAnnotationsLoaded();
    }

    private boolean shouldRequestFocus(Component c) {
        TopComponent active = TopComponent.getRegistry().getActivated();
        while (c != null) {
            if (c == active) {
                return true;
            }
            c = c.getParent();
        }
        return false;
    }

    @Override
    public String toString() {
        return "phase=" + phase + ", editor=" + System.identityHashCode(editor);
    }

}
