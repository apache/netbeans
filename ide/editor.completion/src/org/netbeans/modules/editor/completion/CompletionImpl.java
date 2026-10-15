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

package org.netbeans.modules.editor.completion;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import javax.swing.undo.UndoableEdit;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.spi.editor.completion.*;
import org.openide.ErrorManager;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Implementation of the completion processing.
 * The visual related processing is done in AWT thread together
 * with completion providers invocation and result set sorting.
 * <br>
 * The only thing that can be done outside of the AWT
 * is hiding of the completion/documentation/tooltip.
 *
 * <p>
 * The completion providers typically reschedule computation intensive
 * collecting of their result set into an extra thread to keep the GUI responsive.
 *
 * @author Dusan Balek, Miloslav Metelka
 */

public class CompletionImpl extends MouseAdapter implements DocumentListener,
CaretListener, KeyListener, FocusListener, ListSelectionListener, PropertyChangeListener, ChangeListener {
    
    // -J-Dorg.netbeans.modules.editor.completion.CompletionImpl.level=FINE
    private static final Logger LOG = Logger.getLogger(CompletionImpl.class.getName());
    private static final boolean alphaSort = Boolean.getBoolean("org.netbeans.modules.editor.completion.alphabeticalSort"); // [TODO] create an option
    private static final boolean NO_TAB_COMPLETION = Boolean.getBoolean("org.netbeans.modules.editor.completion.noTabCompletion");  //NOI18N

    private static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.editor.completion"); // NOI18N

    private static CompletionImpl singleton = null;

    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CompletionImpl.class, "completion-no-suggestions");
    private static final String PLEASE_WAIT = NbBundle.getMessage(CompletionImpl.class, "completion-please-wait");

    private static final String COMPLETION_SHOW = "completion-show"; //NOI18N
    private static final String COMPLETION_ALL_SHOW = "completion-all-show"; //NOI18N
    private static final String DOC_SHOW = "doc-show"; //NOI18N
    private static final String TOOLTIP_SHOW = "tooltip-show"; //NOI18N
    
    private static final int PLEASE_WAIT_TIMEOUT = 750;
    private static final int PRESCAN = 25;
    
    static final CompletionDocumentation PLEASE_WAIT_DOC = new CompletionDocumentation() {

        @Override
        public String getText() {
            return PLEASE_WAIT;
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    };
    
    public static CompletionImpl get() {
        if (singleton == null)
            singleton = new CompletionImpl();
        return singleton;
    }

    static LazyListModel.Filter filter = new LazyListModel.Filter() {
        public boolean accept(Object obj) {
            if (obj instanceof LazyCompletionItem)
                return ((LazyCompletionItem)obj).accept();
            return true;
        }
        public void scheduleUpdate(Runnable run) {
            SwingUtilities.invokeLater( run );
        }
    };
    
    /** Text component being currently edited. Changed in AWT only. */
    private WeakReference<JTextComponent> activeComponent = null;
    
    /** Document currently installed in the active component. Changed in AWT only. */
    private WeakReference<Document> activeDocument = null;
    
    /** Map containing keystrokes that should be overriden by completion processing. Changed in AWT only. */
    private InputMap inputMap;
    
    /** Action map containing actions bound to keys through input map. Changed in AWT only. */
    private ActionMap actionMap;

    /** Layout of the completion pane/documentation/tooltip. Changed in AWT only. */
    private final CompletionLayout layout = new CompletionLayout();
    
    /* Completion providers registered for the active component (its mime-type). Changed in AWT only. */
    private CompletionProvider[] activeProviders = null;
    
    /** Mapping of mime-type to array of providers. Changed in AWT only. */
    private HashMap<String, CompletionProvider[]> providersCache = new HashMap<String, CompletionProvider[]>();

    /**
     * Result of the completion query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the completion query.
     */
    private Result completionResult;
    
    /**
     * Result of the documentation query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the documentation query.
     */
    private Result docResult;
    
    /**
     * Result of the tooltip query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the tooltip query.
     */
    private Result toolTipResult;
    
    /** Timer for opening completion automatically. Changed in AWT only. */
    private Timer completionAutoPopupTimer;
    /** Timer for opening documentation window automatically. Changed in AWT only. */
    private Timer docAutoPopupTimer;
    /** Timer for opening Please Wait popup. Changed in AWT only. */
    private Timer pleaseWaitTimer;
    /** Whether it's initial or refreshed query. Changed in AWT only. */
    private boolean refreshedQuery = false;
    /** Whether it's explicit or automatic query. Changed in AWT only. */
    private boolean explicitQuery = false;
    
    private WeakReference<CompletionItem> lastSelectedItem = null;
    
    /** Ending offset of the recent autopopup modification. */
    private int autoModEndOffset = -1;
    
    private boolean pleaseWaitDisplayed = false;
    private String completionShortcut = null;
    
    private Lookup.Result<KeyBindingSettings> kbs;
    private RequestProcessor.Task asyncWarmUpTask = null;
    private String asyncWarmUpMimeType = null;
    private static CompletionImplProfile profile;
    
    private final LookupListener shortcutsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            Utilities.runInEventDispatchThread(new Runnable(){
                public void run(){
                    installKeybindings();
                }
            });
        }
    };

    private Point lastViewPosition; // Visible view in JViewport
    
    private CompletionImpl() {
        EditorRegistry.addPropertyChangeListener(this);
        completionAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Result localCompletionResult;
                synchronized (CompletionImpl.this) {
                    localCompletionResult = completionResult;
                }
                if (localCompletionResult != null && !localCompletionResult.isQueryInvoked()) {
                    pleaseWaitTimer.restart();
                    CompletionImpl.this.refreshedQuery = false;
                    getActiveComponent().putClientProperty("completion-active", Boolean.TRUE);  //NOI18N
                    queryResultSets(localCompletionResult.getResultSets());
                    localCompletionResult.queryInvoked();
                }
            }
        });
        completionAutoPopupTimer.setRepeats(false);
        
        docAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedItem == null || lastSelectedItem.get() != layout.getSelectedCompletionItem())
                    showDocumentation();
            }
        });
        docAutoPopupTimer.setRepeats(false);
        pleaseWaitTimer = new Timer(PLEASE_WAIT_TIMEOUT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String waitText = PLEASE_WAIT;
                boolean politeWaitText = false;
                Result localCompletionResult;
                synchronized (CompletionImpl.this) {
                    localCompletionResult = completionResult;
                }
                List<CompletionResultSetImpl> resultSets;
                if (localCompletionResult != null && (resultSets = localCompletionResult.getResultSets()) != null) {
                    for (Iterator it = resultSets.iterator(); it.hasNext();) {
                        CompletionResultSetImpl resultSet = (CompletionResultSetImpl)it.next();
                        if (resultSet != null && resultSet.getWaitText() != null) {
                            waitText = resultSet.getWaitText();
                            politeWaitText = true;
                            break;
                        }
                    }
                }
                layout.showCompletion(Collections.singletonList(waitText),
                        null, -1, CompletionImpl.this, null, null, 0);
                pleaseWaitDisplayed = true;
                if (!politeWaitText) {
                    long when = System.currentTimeMillis() - PLEASE_WAIT_TIMEOUT;
                    initializeProfiling(when);
                }
            }
        });
        pleaseWaitTimer.setRepeats(false);
        
        kbs = MimeLookup.getLookup(MimePath.EMPTY).lookupResult(KeyBindingSettings.class);
        kbs.addLookupListener(WeakListeners.create(LookupListener.class, shortcutsTracker, kbs));
    }
    
    private JTextComponent getActiveComponent() {
        return activeComponent != null ? activeComponent.get() : null;
    }

    private Document getActiveDocument() {
        return activeDocument != null ? activeDocument.get() : null;
    }
    
    int getSortType() {
        return alphaSort ? CompletionResultSet.TEXT_SORT_TYPE : CompletionResultSet.PRIORITY_SORT_TYPE;
    }
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        // Ignore insertions done outside of the AWT (various content generation)
        if (!SwingUtilities.isEventDispatchThread()) {
            return;
        }
        // Check whether the insertion came from typing
        if (!DocumentUtilities.isTypingModification(e)) {
            return;
        }

        if (ensureActiveProviders()) {
            try {
                int modEndOffset = e.getOffset() + e.getLength();
                if (modEndOffset != getActiveComponent().getCaretPosition()) {
                    return;
                }
                String typedText = e.getDocument().getText(e.getOffset(), e.getLength());
                for (int i = 0; i < activeProviders.length; i++) {
                    int type = activeProviders[i].getAutoQueryTypes(getActiveComponent(), typedText);
                    boolean completionResultNull;
                    synchronized (this) {
                        completionResultNull = (completionResult == null);
                    }
                    if ((type & CompletionProvider.COMPLETION_QUERY_TYPE) != 0 &&
                            CompletionSettings.getInstance(getActiveComponent()).completionAutoPopup()) {
                        autoModEndOffset = modEndOffset;
                        if (completionResultNull)
                            showCompletion(false, false, true, CompletionProvider.COMPLETION_QUERY_TYPE);
                    }

                    boolean tooltipResultNull;
                    synchronized (this) {
                        tooltipResultNull = (toolTipResult == null);
                    }
                    if (tooltipResultNull && (type & CompletionProvider.TOOLTIP_QUERY_TYPE) != 0) {
                        showToolTip();
                    }
                }
            } catch (BadLocationException ex) {}
            if (completionAutoPopupTimer.isRunning())
                restartCompletionAutoPopupTimer();
        }
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        // Ignore insertions done outside of the AWT (various content generation)
        if (!SwingUtilities.isEventDispatchThread()) {
            return;
        }
    }
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
    }
    
    public void caretUpdate(javax.swing.event.CaretEvent e) {
        assert (SwingUtilities.isEventDispatchThread());

        if (ensureActiveProviders()) {
            // Check whether there is an active result being computed but not yet displayed
            // Caret update should be notified AFTER document modifications
            // thank to document listener priorities
            Result localCompletionResult;
            synchronized (this) {
                localCompletionResult = completionResult;
            }
            if (autoModEndOffset >= 0 && e.getDot() != autoModEndOffset
                    && (completionAutoPopupTimer.isRunning() || localCompletionResult != null)
                    && (!layout.isCompletionVisible() || pleaseWaitDisplayed)) {
                hideCompletion(false);
            }

            completionRefresh();
            toolTipRefresh();
        }
    }

    public void keyPressed(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void keyReleased(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void keyTyped(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        hideAll();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        hideAll();
    }
    
    public void stateChanged(ChangeEvent e) {
        // From JViewport
        boolean hide = true;
        JTextComponent component = getActiveComponent();
        Container parent = component != null ? component.getParent() : null;
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            Point viewPosition = viewport.getViewPosition();
            if (lastViewPosition != null && lastViewPosition.y == viewPosition.y) {
                hide = false;
            }
            lastViewPosition = viewPosition;
        }
        if (hide) {
            hideAll();
        }
    }

    public void hideAll() {
        hideToolTip();
        hideCompletion(true);
        hideDocumentation(true);
    }

    /**
     * Called from AWT when selection in the completion list pane changes.
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        assert (SwingUtilities.isEventDispatchThread());

        documentationCancel();
        if (layout.isDocumentationVisible() || CompletionSettings.getInstance(getActiveComponent()).documentationAutoPopup()) {
            restartDocumentationAutoPopupTimer();
        }
    }

    /**
     * Expected to be called from the AWT only.
     */
    public void propertyChange(PropertyChangeEvent e) {
        assert (SwingUtilities.isEventDispatchThread()); // expected in AWT only

        boolean cancel = false;
        JTextComponent component = EditorRegistry.lastFocusedComponent();
        if (component != getActiveComponent()) {
            initActiveProviders(component);
            JTextComponent activeJtc = getActiveComponent();
            if (activeJtc != null) {
                activeJtc.removeCaretListener(this);
                activeJtc.removeKeyListener(this);
                activeJtc.removeFocusListener(this);
                activeJtc.removeMouseListener(this);
                Container parent = activeJtc.getParent();
                if (parent instanceof JLayeredPane) {
                    parent = parent.getParent();
                }
                if (parent instanceof JViewport) {
                    JViewport viewport = (JViewport) parent;
                    viewport.removeChangeListener(this);
                }
            }
            if (component != null) {
                component.addCaretListener(this);
                component.addKeyListener(this);
                component.addFocusListener(this);
                component.addMouseListener(this);
                Container parent = component.getParent();
                if (parent instanceof JLayeredPane) {
                    parent = parent.getParent();
                }
                if (parent instanceof JViewport) {
                    JViewport viewport = (JViewport) parent;
                    viewport.addChangeListener(this);
                }
            }
            activeComponent = (component != null)
                    ? new WeakReference<JTextComponent>(component)
                    : null;
            layout.setEditorComponent(getActiveComponent());
            stopProfiling();
            installKeybindings();
            cancel = true;
        }
        
        // Also check document change of an active component
        Document document = (component != null) ? component.getDocument() : null;
        if (document != getActiveDocument()) {
            initActiveProviders(component);
            if (getActiveDocument() != null)
                DocumentUtilities.removeDocumentListener(getActiveDocument(), this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            if (document != null)
                DocumentUtilities.addDocumentListener(document, this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            activeDocument = (document != null) ? new WeakReference<Document>(document) : null;
            cancel = true;
        }
        if (cancel)
            completionCancel();
    }
    
    private void initActiveProviders(JTextComponent component) {
        activeProviders = (component != null)
                ? getCompletionProvidersForComponent(component, getMimePathBasic(component), true)
                : null;
        if (LOG.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer("Completion PROVIDERS:\n"); // NOI18N
            if (activeProviders != null) {
                for (int i = 0; i < activeProviders.length; i++) {
                    sb.append("providers["); // NOI18N
                    sb.append(i);
                    sb.append("]: "); // NOI18N
                    sb.append(activeProviders[i].getClass());
                    sb.append('\n');
                }
            }
            LOG.fine(sb.toString());
        }
    }
    
    private boolean ensureActiveProviders() {
        JTextComponent component = getActiveComponent();
        if (component == null) {
            activeProviders = null;
            currentMimePath = null;
        } else {
            String mime = getMimePath(component);
            if (activeProviders != null) {
                if (mime == null) {
                    return false;
                }
                if (mime.equals(currentMimePath)) {
                    return true;
                }
            }
            activeProviders = getCompletionProvidersForComponent(component, mime, false);
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer("Completion PROVIDERS:\n"); // NOI18N
            if (activeProviders != null) {
                for (int i = 0; i < activeProviders.length; i++) {
                    sb.append("providers["); // NOI18N
                    sb.append(i);
                    sb.append("]: "); // NOI18N
                    sb.append(activeProviders[i].getClass());
                    sb.append('\n');
                }
            }
            LOG.fine(sb.toString());
        }
        return activeProviders != null;
    }
    
    private void restartCompletionAutoPopupTimer() {
        assert (SwingUtilities.isEventDispatchThread()); // expect in AWT only

        int completionDelay = CompletionSettings.getInstance(getActiveComponent()).completionAutoPopupDelay();
        completionAutoPopupTimer.setInitialDelay(completionDelay);
        completionAutoPopupTimer.restart();
    }
    
    private void restartDocumentationAutoPopupTimer() {
        assert (SwingUtilities.isEventDispatchThread()); // expect in AWT only

        int docDelay = CompletionSettings.getInstance(getActiveComponent()).documentationAutoPopupDelay();
        docAutoPopupTimer.setInitialDelay(docDelay);
        docAutoPopupTimer.restart();    
    }
    
    private String getMimePathBasic(JTextComponent component) {
        final Document doc = component.getDocument();
        // original mimeType code
        Object mimeTypeObj =  doc == null ? null : DocumentUtilities.getMimeType(doc);  //NOI18N
        String mimeType;

        if (mimeTypeObj instanceof String) {
            mimeType = (String) mimeTypeObj;
        } else {
            BaseKit kit = Utilities.getKit(component);
            
            if (kit == null) {
                return null;
            }
            
            mimeType = kit.getContentType();
        }
        return mimeType;
    }
    
    /**
     * Gives MimePath of the caret position as String
     * @param component
     * @return 
     */
    private String getMimePath(JTextComponent component) {
        final int offset = component.getCaretPosition();
        final MimePath[] mimePathR = new MimePath[1];
        final Document doc = component.getDocument();
        if (doc == null) {
            return null;
        }
        doc.render(new Runnable() {
            @Override
            public void run() {
                List<TokenSequence<?>> seqs = TokenHierarchy.get(doc).embeddedTokenSequences(offset, true);
                TokenSequence<?> seq;
                if (seqs.size() == 1) {
                    // get the mime path from the document/kit
                    return;
                }
                if (seqs.isEmpty()) {
                    seq  = TokenHierarchy.get(doc).tokenSequence();
                } else {
                    seq = seqs.get(seqs.size() - 1);
                }

                if (seq != null) {
                    mimePathR[0] = MimePath.parse(seq.languagePath().mimePath());
                }
            }
        });
        if (mimePathR[0] != null) {
            return mimePathR[0].getPath();
        } else {
            return getMimePathBasic(component);
        }
    }
    
    private String currentMimePath;

    /**
     * Gets a complete MIME path for the given offset in the document
     * @param doc the document
     * @param offset point of interest
     * @return MimePath
     */
    static MimePath getMimePath(final Document doc, final int offset) {
        final MimePath[] mimePathR = new MimePath[1];
        doc.render(new Runnable() {
            @Override
            public void run() {
                List<TokenSequence<?>> seqs = TokenHierarchy.get(doc).embeddedTokenSequences(offset, true);
                TokenSequence<?> seq = seqs.isEmpty() ? null : seqs.get(seqs.size() - 1);
                seq = seq == null ? TokenHierarchy.get(doc).tokenSequence() : seq;
                mimePathR[0] = seq == null ? MimePath.parse(DocumentUtilities.getMimeType(doc)) : MimePath.parse(seq.languagePath().mimePath());
            }
        });
        return mimePathR[0];
    }
    
    /**
     * Has side effects !
     * @param component
     * @param asyncWarmUp
     * @return 
     */
    private CompletionProvider[] getCompletionProvidersForComponent(JTextComponent component, String mimePathString, boolean asyncWarmUp) {
        assert (SwingUtilities.isEventDispatchThread());

        if (component == null)
            return null;
        
        if (mimePathString == null) {
            return null;
        }
        if (providersCache.containsKey(mimePathString)) {
            currentMimePath = mimePathString;
            return providersCache.get(mimePathString);
        }

        if (asyncWarmUpTask != null) {
            if (asyncWarmUp && mimePathString != null && mimePathString.equals(asyncWarmUpMimeType))
                return null;
            if (!asyncWarmUpTask.cancel()) {
                asyncWarmUpTask.waitFinished();
            }
            asyncWarmUpTask = null;
            asyncWarmUpMimeType = null;
        }
        MimePath path = MimePath.parse(mimePathString);
        if (asyncWarmUp) {
            final Lookup lookup = MimeLookup.getLookup(path);
            asyncWarmUpMimeType = mimePathString;
            asyncWarmUpTask = RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    lookup.lookupAll(CompletionProvider.class);
                }
            });
            return null;
        }
        // Note 1:
        // it's not possible to compare instances of CompletionProvider; each MimeLookup has its own instances, so if e.g. MyLookup
        // is registered for text/html then MimeLookup for text/x-jsp/text/html and MimeLookup for text/html produce a *different*
        // instances of the provider.
        // the registered classes could be used as key instead BUT there would be no way how to disambiguate the registration, IF
        // it was really needed for whatever reason. So I decided to match the providers based on Lookup.Item.getID(), which evaluates
        // (usually) to the registration filename, which the developer can alter, if for some reason the provider is needed.
        
        List<CompletionProvider>    allProviders = new ArrayList<>();
        Lookup lookup;
        Set<String> seenProviders = new HashSet<>();
        
        for (int pref = path.size(); pref >= 1; pref--) {
            lookup = MimeLookup.getLookup(path.getPrefix(pref));
            Collection<? extends Lookup.Item> allItems = lookup.lookupResult(CompletionProvider.class).allItems();
            for (Lookup.Item i : allItems) {
                String id = i.getId();
                // hack, relies on that each provider has a different filename; which is typically
                // true as providers are registered using classnames.
                int lastSlash = id.lastIndexOf('/'); // NOI18N
                if (lastSlash > 0) {
                    String fname = id.substring(lastSlash + 1);
                    if (!seenProviders.add(fname)) {
                        // the provider has been already seen in this list; do not add it.
                        continue;
                    }
                }
                allProviders.add((CompletionProvider)i.getInstance());
            }
        }
        currentMimePath = mimePathString;
        CompletionProvider[] ret;
        providersCache.put(mimePathString, ret = allProviders.toArray(new CompletionProvider[0]));
        return ret;
    }
    
    private void dispatchKeyEvent(KeyEvent e) {
        if (e == null)
            return;
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
        JTextComponent comp = getActiveComponent();
        boolean compEditable = (comp != null && comp.isEditable());
        Document doc = comp.getDocument();
        boolean guardedPos = doc instanceof GuardedDocument && ((GuardedDocument)doc).isPosGuarded(comp.getSelectionEnd());
        Object obj = inputMap.get(ks);
        if (obj != null) {
            Action action = actionMap.get(obj);
            if (action != null) {
                if (compEditable)
                    action.actionPerformed(null);
                e.consume();
                return;
            }
        }
        if (layout.isCompletionVisible()) {
            CompletionItem item = layout.getSelectedCompletionItem();
            if (item != null) {
                sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
                MulticaretHandler mch = MulticaretHandler.create(comp);
                try {
                    if (compEditable && !guardedPos) {
                        LogRecord r = new LogRecord(Level.FINE, "COMPL_KEY_SELECT"); // NOI18N
                        r.setParameters(new Object[] {e.getKeyChar(), layout.getSelectedIndex(), item.getClass().getSimpleName()});
                        item.processKeyEvent(e);
                        if (e.isConsumed()) {
                            uilog(r);
                            return;
                        }
                    }
                    // Call default action if ENTER was pressed
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED
                            && (e.getModifiers() & InputEvent.ALT_MASK) == 0) {
                        e.consume();
                        if (guardedPos) {
                            Toolkit.getDefaultToolkit().beep();
                        } else if (compEditable) {
                            // Consuming completion
                            if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) { // CTRL+ENTER
                                consumeIdentifier();
                            }
                            LogRecord r = new LogRecord(Level.FINE, "COMPL_KEY_SELECT_DEFAULT"); // NOI18N
                            r.setParameters(new Object[]{'\n', layout.getSelectedIndex(), item.getClass().getSimpleName()});
                            item.defaultAction(getActiveComponent());
                            uilog(r);
                        }
                        return;
                    }
                } finally {
                    mch.release();
                    sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
                }
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
                    || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                    || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END) {
                hideCompletion(false);                
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB && e.getID() == KeyEvent.KEY_PRESSED) {
                e.consume();
                if (compEditable && !guardedPos) {
                    insertCommonPrefix();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
                return;
            }
        }
        layout.processKeyEvent(e);
    }

    static void sendUndoableEdit(Document d, UndoableEdit ue) {
        if(d instanceof AbstractDocument) {
            UndoableEditListener[] uels = ((AbstractDocument)d).getUndoableEditListeners();
            UndoableEditEvent ev = new UndoableEditEvent(d, ue);
            for(UndoableEditListener uel : uels) {
                uel.undoableEditHappened(ev);
            }
        }
    }
    
    private void completionQuery(boolean refreshedQuery, boolean delayQuery, int queryType) {
        Result newCompletionResult = this.new Result(activeProviders.length);
        synchronized (this) {
            assert (completionResult == null);
            completionResult = newCompletionResult;
        }
        List<CompletionResultSetImpl> completionResultSets = newCompletionResult.getResultSets();

        // Initialize the completion tasks
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask compTask = activeProviders[i].createTask(
                    queryType, getActiveComponent());
            if (compTask != null) {
                CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                        this, newCompletionResult, compTask, queryType);
                completionResultSets.add(resultSet);
            }
        }
        
        if (completionResultSets.size() > 0) {
            // Query the tasks
            if (delayQuery) {
                restartCompletionAutoPopupTimer();
            } else {
                pleaseWaitTimer.restart();
                this.refreshedQuery = refreshedQuery;
                getActiveComponent().putClientProperty("completion-active", Boolean.TRUE);  //NOI18N
                queryResultSets(completionResultSets);
                newCompletionResult.queryInvoked();
            }
        } else {
            completionCancel();
            if (explicitQuery)
                layout.showCompletion(Collections.singletonList(NO_SUGGESTIONS), null, -1, CompletionImpl.this, null, null, 0);
            pleaseWaitDisplayed = false;
            stopProfiling();
        }
    }

    /**
     * Called from caretUpdate() to refresh the completion result after caret move.
     * <br>
     * Must be called in AWT thread.
     */
    private void completionRefresh() {
        Result localCompletionResult;
        synchronized (this) {
            localCompletionResult = completionResult;
        }
        if (localCompletionResult != null) {
            refreshedQuery = true;
            Result refreshResult = localCompletionResult.createRefreshResult();
            synchronized (this) {
                completionResult = refreshResult;
            }
            refreshResult.invokeRefresh(true);
        }
    }
    
    private void completionCancel() {
        Result oldCompletionResult;
        synchronized (this) {
            oldCompletionResult = completionResult;
            completionResult = null;
        }
        if (oldCompletionResult != null) {
            oldCompletionResult.cancel();
        }
    }

    /** 
     * Consumes identifier part of text behind caret upto first non-identifier
     * char.
     */
    private void consumeIdentifier() {
        JTextComponent comp = getActiveComponent();
        BaseDocument doc = (BaseDocument) comp.getDocument();
        int initCarPos = comp.getCaretPosition();
        int carPos = initCarPos;
        boolean nonChar = false;
        char c;
        try {
            while(nonChar == false) {
                c = doc.getChars(carPos, 1)[0];
                if(!Character.isJavaIdentifierPart(c)) {
                    nonChar = true;
                }
                carPos++;
            }
            doc.remove(initCarPos, carPos - initCarPos -1);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Called from dispatchKeyEvent() to insert prefix common to all items in the 
     * completion result after TAB.<br>
     * Must be called in AWT thread after all tasks of the current completionResult are finished.
     */
    private void insertCommonPrefix() {
        JTextComponent c = getActiveComponent();
        Result localCompletionResult;
        synchronized (this) {
            localCompletionResult = completionResult;
            if (localCompletionResult == null)
                return;
            if (!isAllResultsFinished(localCompletionResult.resultSets)) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }
        if (localCompletionResult != null) {
            CompletionItem item = layout.getSelectedCompletionItem();
            CharSequence commonText = item != null ? item.getInsertPrefix() : null;
            int anchorOffset = -1;
outer:      for (Iterator it = localCompletionResult.getResultSets().iterator(); it.hasNext();) {
                CompletionResultSetImpl resultSet = (CompletionResultSetImpl)it.next();
                List<? extends CompletionItem> resultItems = resultSet.getItems();
                if (resultItems.size() > 0) {
                    if (anchorOffset >= -1) {
                        if (anchorOffset > -1 && anchorOffset != resultSet.getAnchorOffset())
                            anchorOffset = -2;
                        else
                            anchorOffset = resultSet.getAnchorOffset();
                    }
                    boolean caseSensitive = CompletionSettings.getInstance(c).completionCaseSensitive();
                    for (Iterator itt = resultItems.iterator(); itt.hasNext();) {
                        CharSequence text = ((CompletionItem)itt.next()).getInsertPrefix();
                        if (text == null) {
                            commonText = null;
                            break outer;
                        }
                        if (commonText == null) {
                            commonText = text;
                        } else {
                            // Get the largest common part
                            if (text.length() < commonText.length()) {
                                commonText = commonText.subSequence(0, text.length());
                            }
                            for (int commonInd = 0; commonInd < commonText.length(); commonInd++) {
                                char textChar = text.charAt(commonInd);
                                char commonTextChar = commonText.charAt(commonInd);
                                if (!caseSensitive) {
                                    textChar = Character.toLowerCase(textChar);
                                    commonTextChar = Character.toLowerCase(commonTextChar);
                                }
                                if (textChar != commonTextChar) {
                                    if (commonInd == 0) {
                                        commonText = null;
                                        break outer; // no common text
                                    }
                                    commonText = commonText.subSequence(0, commonInd);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (commonText != null && anchorOffset >= 0) {
                int len = c.getSelectionStart() - anchorOffset;
                if (len >= 0 && len < commonText.length()) {

                    Document doc = getActiveDocument();
                    BaseDocument baseDoc = null;
                    if(doc instanceof BaseDocument)
                        baseDoc = (BaseDocument)doc;
                        
                    // Insert the missing end part of the prefix
                    if(baseDoc != null)
                        baseDoc.atomicLock();
                    try {
                        doc.remove(anchorOffset, len);
                        doc.insertString(anchorOffset, commonText.toString(), null);
                    } catch (BadLocationException e) {
                    } finally {
                        if(baseDoc != null)
                            baseDoc.atomicUnlock();
                    }
                    return;
                }
            }
            if (item != null && !NO_TAB_COMPLETION)
                item.defaultAction(c);
        }
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showCompletion() {
        autoModEndOffset = -1;
        showCompletion(true, false, false, CompletionProvider.COMPLETION_QUERY_TYPE);
    }

    private void showCompletion(boolean explicitQuery, boolean refreshedQuery, boolean delayQuery, int queryType) {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_COMPLETION, explicitQuery, delayQuery, queryType));
            return;
        }
        
        LogRecord r = new LogRecord(Level.FINE, "COMPL_INVOCATION"); // NOI18N
        r.setParameters(new Object[] {explicitQuery});
        uilog(r);
        
        this.explicitQuery = explicitQuery;
        if (ensureActiveProviders()) {
            completionAutoPopupTimer.stop();
            synchronized(this) {
                if (explicitQuery && completionResult != null) {
                    for (CompletionResultSetImpl rSet : completionResult.resultSets) {
                        if (rSet.getQueryType() == CompletionProvider.COMPLETION_ALL_QUERY_TYPE)
                            return;
                        else
                            break;
                    }
                    queryType = CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
                }
            }
            completionCancel(); // cancel possibly pending query
            completionQuery(refreshedQuery, delayQuery, queryType);
        }
    }

    /** 
     * Request displaying of the completion pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowCompletionPane(final Result result) {
        pleaseWaitTimer.stop();
        stopProfiling();
        
        // Compute total count of the result sets
        int size = 0;
        int qType = 0;
        boolean hasAdditionalItems = false;
        final StringBuilder hasAdditionalItemsText = new StringBuilder();
        List<CompletionResultSetImpl> completionResultSets = result.getResultSets();
        for (int i = completionResultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl resultSet = completionResultSets.get(i);
            size += resultSet.getItems().size();
            qType = resultSet.getQueryType();
            if (resultSet.hasAdditionalItems()) {
                hasAdditionalItems = true;
                String s = resultSet.getHasAdditionalItemsText();
                if (s != null)
                    hasAdditionalItemsText.append(s);
            }
        }
        
        // Collect and sort the gathered completion items
        List<CompletionItem> resultItems = new ArrayList<CompletionItem>(size);
        String title = null;
        int anchorOffset = -1;
        if (size > 0) {
            for (int i = 0; i < completionResultSets.size(); i++) {
                CompletionResultSetImpl resultSet = completionResultSets.get(i);
                List<? extends CompletionItem> items = resultSet.getItems();
                if (items.size() > 0) {
                    resultItems.addAll(items);
                    if (title == null)
                        title = resultSet.getTitle();
                    if (anchorOffset == -1)
                        anchorOffset = resultSet.getAnchorOffset();
                }
            }
        }
        
        final ArrayList<CompletionItem> sortedResultItems = new ArrayList<CompletionItem>(size = resultItems.size());
        if (size > 0) {
            try {
                resultItems.sort(CompletionItemComparator.get(getSortType()));
            } catch (IllegalArgumentException iae) {
                LOG.warning("Unable to sort: " + resultItems); //NOI18N
            }
            int cnt = 0;
            for(int i = 0; i < size; i++) {
                CompletionItem item = resultItems.get(i);                
                if (cnt < PRESCAN ) {
                    if (!filter.accept(item))
                        continue;
                    else
                        sortedResultItems.add( item );
                }
                else {
                    sortedResultItems.add(item);
                }
                cnt++;
            }
        }

        final boolean noSuggestions = sortedResultItems.size() == 0;
        if (noSuggestions) {
            if (hasAdditionalItems && qType == CompletionProvider.COMPLETION_QUERY_TYPE && !this.refreshedQuery) {
                showCompletion(this.explicitQuery, this.refreshedQuery, false, CompletionProvider.COMPLETION_ALL_QUERY_TYPE);
                return;
            }
            if (!explicitQuery) {                
                hideCompletion(false);
                return;
            }
        }
       
        // Request displaying of the completion pane in AWT thread
        final String displayTitle = title;
        final int displayAnchorOffset = anchorOffset;
        final boolean displayAdditionalItems = hasAdditionalItems;
        Runnable requestShowRunnable = new Runnable() {
            public void run() {
                synchronized(CompletionImpl.this) {
                    if (result != completionResult)
                        return;
                }
                JTextComponent c = getActiveComponent();
                Document doc = c.getDocument();
                CompletionSettings cs = CompletionSettings.getInstance(c);
                int caretOffset = c.getSelectionStart();
                // completionResults = null;
                if (sortedResultItems.size() == 1 && !refreshedQuery && explicitQuery
                        && cs.completionInstantSubstitution()
                        && c.isEditable() && !(doc instanceof GuardedDocument && ((GuardedDocument)doc).isPosGuarded(caretOffset))) {
                    try {
                        int[] block = Utilities.getIdentifierBlock(c, caretOffset);
                        if (block == null || block[1] == caretOffset) { // NOI18N
                            CompletionItem item = sortedResultItems.get(0);
                            sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
                            MulticaretHandler mch = MulticaretHandler.create(c);
                            try {
                                if (item.instantSubstitution(c))
                                    return;
                            } finally {
                                mch.release();
                                sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
                            }
                        }
                    } catch (BadLocationException ex) {
                    }
                }
                
                int selectedIndex = getCompletionPreSelectionIndex(sortedResultItems);
                c.putClientProperty("completion-visible", Boolean.TRUE);
                layout.showCompletion(noSuggestions ? Collections.singletonList(NO_SUGGESTIONS) : sortedResultItems, displayTitle, displayAnchorOffset, CompletionImpl.this, displayAdditionalItems ? hasAdditionalItemsText.toString() : null, displayAdditionalItems ? completionShortcut : null, selectedIndex);
                pleaseWaitDisplayed = false;
                stopProfiling();

                // Show documentation as well if set by default
                if (cs.documentationAutoPopup()) {
                    if (noSuggestions) {
                        docAutoPopupTimer.stop(); // Ensure the popup timer gets stopped
                        documentationCancel();
                        layout.hideDocumentation();
                    } else {
                        restartDocumentationAutoPopupTimer();
                    }
                }
            }
        };
        runInAWT(requestShowRunnable);
    }
    
    private int getCompletionPreSelectionIndex(List<CompletionItem> items) {
        String prefix = null;
        if(getActiveDocument() instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)getActiveDocument();
            int caretOffset = getActiveComponent().getSelectionStart();
            try {
                int[] block = Utilities.getIdentifierBlock(doc, caretOffset);
                if (block != null) {
                    block[1] = caretOffset;
                    prefix = doc.getText(block);
                }
            } catch (BadLocationException ble) {
            }
        }
        int closestIdx = 0;
        if (prefix != null && prefix.length() > 0) {
            int distance = Integer.MAX_VALUE;
            int idx = 0;
            String prefLC = prefix.toLowerCase();
            for (CompletionItem item : items) {
                CharSequence text = item.getInsertPrefix();
                int prio = item.getSortPriority() * 1000;
                boolean isSmart = prio < 0;
                String name = text != null ? text.toString() : null;
                if (name != null) {
                    for (String part : name.split("\\.")) {
                        if (part.startsWith(prefix) && (!(item instanceof LazyCompletionItem) || ((LazyCompletionItem)item).accept())) {
                            return idx;
                        }
                        int d = prio + getDistance(part.toLowerCase(), prefLC);
                        if (part.toLowerCase().startsWith(prefLC)) {
                            d -= 500;
                        }
                        if (d < distance) {
                            distance = d;
                            closestIdx = idx;
                        }
                        if (!isSmart) {
                            break;
                        }
                    }
                }
                idx++;
            }
        }
        return closestIdx;
    }

    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideCompletion() {
        return hideCompletion(true);
    }
    
    public boolean hideCompletion(boolean completionOnly) {
        completionCancel();
        // Invoke hideCompletionPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_COMPLETION_PANE, completionOnly));
            return false;
        } else { // in AWT
            return hideCompletionPane(completionOnly);
        }
    }
    
    /**
     * Hide the completion pane. This must be called in AWT thread.
     */
    private boolean hideCompletionPane(boolean completionOnly) {
        completionAutoPopupTimer.stop(); // Ensure the popup timer gets stopped
        pleaseWaitTimer.stop();
        stopProfiling();
        boolean hidePerformed = layout.hideCompletion(completionOnly);
        if (!layout.isCompletionVisible()) {
            pleaseWaitDisplayed = false;
            JTextComponent jtc = getActiveComponent();
            if (!completionOnly && hidePerformed && CompletionSettings.getInstance(jtc).documentationAutoPopup()) {
                hideDocumentation(true);
            }
            if (jtc != null) {
                jtc.putClientProperty("completion-visible", Boolean.FALSE);
                jtc.putClientProperty("completion-active", Boolean.FALSE);
            }
        }
        return hidePerformed;
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showCompletionSubItems() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_COMPLETION_SUB_ITEMS));
            return;
        }
        layout.showCompletionSubItems();
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showDocumentation() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_DOCUMENTATION));
            return;
        }

        if (ensureActiveProviders()) {
            documentationCancel();
            layout.clearDocumentationHistory();
            documentationQuery();
        }
    }

    /**
     * Request displaying of the documentation pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowDocumentationPane(Result result) {
        final CompletionResultSetImpl resultSet = findFirstValidResult(result.getResultSets());
        runInAWT(new Runnable() {
            public void run() {
                synchronized (CompletionImpl.this) {
                    if (resultSet != null) {
                        layout.showDocumentation(
                                resultSet.getDocumentation(), resultSet.getAnchorOffset());
                    } else {
                        documentationCancel();
                        layout.hideDocumentation();
                    }
                }
            }
        });
    }

    /**
     * May be called in AWT only.
     */
    private void documentationQuery() {
        Result newDocumentationResult = this.new Result(1); // Estimate for selected item only
        synchronized (this) {
            assert (docResult == null);
            docResult = newDocumentationResult;
        }
        List<CompletionResultSetImpl> documentationResultSets = docResult.getResultSets();

        CompletionTask docTask;
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null) {
            lastSelectedItem = new WeakReference<CompletionItem>(selectedItem);
            docTask = selectedItem.createDocumentationTask();
            if (docTask != null) { // attempt the documentation for selected item
                CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                        this, newDocumentationResult, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                documentationResultSets.add(resultSet);
            }
        } else { // No item selected => Query all providers
            lastSelectedItem = null;
            for (int i = 0; i < activeProviders.length; i++) {
                docTask = activeProviders[i].createTask(
                        CompletionProvider.DOCUMENTATION_QUERY_TYPE, getActiveComponent());
                if (docTask != null) {
                    CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                            this, newDocumentationResult, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                    documentationResultSets.add(resultSet);
                }
            }
        }

        if (documentationResultSets.size() > 0) {
            if (layout.isDocumentationVisible())
                layout.showDocumentation(PLEASE_WAIT_DOC, -1);
            queryResultSets(documentationResultSets);
            newDocumentationResult.queryInvoked();
        } else {
            documentationCancel();
            layout.hideDocumentation();
        }
    }

    private void documentationCancel() {
        Result oldDocumentationResult;
        synchronized (this) {
            oldDocumentationResult = docResult;
            docResult = null;
        }
        if (oldDocumentationResult != null) {
            oldDocumentationResult.cancel();
        }
    }
    
    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideDocumentation() {
        return hideDocumentation(true);
    }
    
    boolean hideDocumentation(boolean documentationOnly) {
        documentationCancel();
        // Invoke hideDocumentationPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_DOCUMENTATION_PANE, documentationOnly));
            return false;
        } else { // in AWT
            return hideDocumentationPane(documentationOnly);
        }
    }
    
    /**
     * May be called in AWT only.
     */
    boolean hideDocumentationPane(boolean documentationOnly) {
        // Ensure the documentation popup timer is stopped
        docAutoPopupTimer.stop();
        boolean hidePerformed = layout.hideDocumentation();
 // Also hide completion if documentation pops automatically
        if (!documentationOnly && hidePerformed && CompletionSettings.getInstance(getActiveComponent()).documentationAutoPopup()) {
            hideCompletion(true);
        }
        return hidePerformed;
    }

    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showToolTip() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_TOOL_TIP));
            return;
        }

        if (ensureActiveProviders()) {
            toolTipCancel();
            toolTipQuery();
        }
    }

    /**
     * Request displaying of the tooltip pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowToolTipPane(Result result) {
        final CompletionResultSetImpl resultSet = findFirstValidResult(result.getResultSets());
        runInAWT(new Runnable() {
            public void run() {
                if (resultSet != null) {
                    layout.showToolTip(
                            resultSet.getToolTip(), resultSet.getAnchorOffset());
                } else {
                    hideToolTip();
                }
            }
        });
    }

    /**
     * May be called in AWT only.
     */
    private void toolTipQuery() {
        Result newToolTipResult = this.new Result(1);
        synchronized (this) {
            assert (toolTipResult == null);
            toolTipResult = newToolTipResult;
        }
        List<CompletionResultSetImpl> toolTipResultSets = newToolTipResult.getResultSets();

        CompletionTask toolTipTask;
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null && (toolTipTask = selectedItem.createToolTipTask()) != null) {
            CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                    this, newToolTipResult, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
            toolTipResultSets.add(resultSet);
        } else {
            for (int i = 0; i < activeProviders.length; i++) {
                toolTipTask = activeProviders[i].createTask(
                        CompletionProvider.TOOLTIP_QUERY_TYPE, getActiveComponent());
                if (toolTipTask != null) {
                    CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                            this, newToolTipResult, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
                    toolTipResultSets.add(resultSet);
                }
            }
        }
        
        queryResultSets(toolTipResultSets);
        newToolTipResult.queryInvoked();
    }

    private void toolTipRefresh() {
        Result localToolTipResult;
        synchronized (this) {
            localToolTipResult = toolTipResult;
        }
        if (localToolTipResult != null) {
            Result refreshResult = localToolTipResult.createRefreshResult();
            synchronized (this) {
                toolTipResult = refreshResult;
            }
            refreshResult.invokeRefresh(false);
        }
    }

    /**
     * May be called from any thread.
     */
    private void toolTipCancel() {
        Result oldToolTipResult;
        synchronized (this) {
            oldToolTipResult = toolTipResult;
            toolTipResult = null;
        }
        if (oldToolTipResult != null) {
            oldToolTipResult.cancel();
        }
    }

    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideToolTip() {
        toolTipCancel();
        // Invoke hideToolTipPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_TOOL_TIP_PANE));
            return false;
        } else { // in AWT
            return hideToolTipPane();
        }
    }
    
    /**
     * May be called in AWT only.
     */
    boolean hideToolTipPane() {
        return layout.hideToolTip();
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        if (editorActionName != null && getActiveComponent() != null) {
            TextUI ui = getActiveComponent().getUI();
            Keymap km = getActiveComponent().getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(getActiveComponent());
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            return keys;
                        } else {
                            // try kit's keymap
                            Keymap km2 = ((BaseKit)kit).getKeymap();
                            KeyStroke[] keys2 = km2.getKeyStrokesForAction(a);
                            if (keys2 != null && keys2.length > 0) {
                                return keys2;
                            }
                        }
                    }
                }
            }
        }
        return new KeyStroke[0];
    }

    private void installKeybindings() {
        actionMap = new ActionMap();
        inputMap = new InputMap();
        completionShortcut = null;
        
        // Register completion show
        kbs.allInstances(); // in order to make Lookup.Result active and fire events
        KeyStroke[] keys = findEditorKeys(ExtKit.completionShowAction);
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], COMPLETION_SHOW);
            if (completionShortcut == null) {
                completionShortcut = getKeyStrokeAsText(keys[i]);
            }
        }
        actionMap.put(COMPLETION_SHOW, new CompletionShowAction(CompletionProvider.COMPLETION_QUERY_TYPE));

        // Register all completion show
        keys = findEditorKeys(ExtKit.allCompletionShowAction);
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], COMPLETION_ALL_SHOW);
        }
        actionMap.put(COMPLETION_ALL_SHOW, new CompletionShowAction(CompletionProvider.COMPLETION_ALL_QUERY_TYPE));

        // Register documentation show
        keys = findEditorKeys(ExtKit.documentationShowAction);
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], DOC_SHOW);
        }
        actionMap.put(DOC_SHOW, new DocShowAction());
        
        // Register tooltip show
        keys = findEditorKeys(ExtKit.completionTooltipShowAction);
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], TOOLTIP_SHOW);
        }
        actionMap.put(TOOLTIP_SHOW, new ToolTipShowAction());
    }
    
    private static String getKeyStrokeAsText (KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers ();
        StringBuffer sb = new StringBuffer ();
        sb.append('\'');
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0)
            sb.append ("Ctrl+"); //NOI18N
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0)
            sb.append ("Alt+"); //NOI18N
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0)
            sb.append ("Shift+"); //NOI18N
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0)
            sb.append ("Meta+"); //NOI18N
        if (keyStroke.getKeyCode () != KeyEvent.VK_SHIFT &&
            keyStroke.getKeyCode () != KeyEvent.VK_CONTROL &&
            keyStroke.getKeyCode () != KeyEvent.VK_META &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT_GRAPH
        )
            sb.append (org.openide.util.Utilities.keyToString (
                KeyStroke.getKeyStroke (keyStroke.getKeyCode (), 0)
            ));
        sb.append('\'');
        return sb.toString ();
    }

    /**
     * Notify that a particular completion result set has just been finished.
     * <br>
     * This method may be called from any thread.
     */
    void finishNotify(CompletionResultSetImpl finishedResult) {
        Result localResult;
        boolean finished = false;
        switch (finishedResult.getQueryType()) {
            case CompletionProvider.COMPLETION_QUERY_TYPE:
            case CompletionProvider.COMPLETION_ALL_QUERY_TYPE:
                synchronized (this) {
                    localResult = completionResult;
                    if (finishedResult.getResultId() == localResult) {
                        finished = isAllResultsFinished(localResult.getResultSets());
                    }
                }
                if (finished)
                    requestShowCompletionPane(localResult);
                break;

            case CompletionProvider.DOCUMENTATION_QUERY_TYPE:
                synchronized (this) {
                    localResult = docResult;
                    if (finishedResult.getResultId() == localResult) {
                        finished = isAllResultsFinished(localResult.getResultSets());
                    }
                }
                if (finished)
                    requestShowDocumentationPane(localResult);
                break;

            case CompletionProvider.TOOLTIP_QUERY_TYPE:
                synchronized (this) {
                    localResult = toolTipResult;
                    if (finishedResult.getResultId() == localResult) {
                        finished = isAllResultsFinished(localResult.getResultSets());
                    }
                }
                if (finished)
                    requestShowToolTipPane(localResult);
                break;
                
            default:
                throw new IllegalStateException(); // Invalid query type
        }
    }
    
    private static boolean isAllResultsFinished(List<CompletionResultSetImpl> resultSets) {
        for (int i = resultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl result = resultSets.get(i);
            if (!result.isFinished()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("CompletionTask: " + result.getTask() // NOI18N
                            + " not finished yet\n"); // NOI18N
                }
                return false;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("----- All tasks finished -----\n");
        }
        return true;
    }

    /**
     * Find first result that has non-null documentation or tooltip
     * depending on its query type.
     * <br>
     * The method assumes that all the resultSets are already finished.
     */
    private static CompletionResultSetImpl findFirstValidResult(List<CompletionResultSetImpl> resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl result = resultSets.get(i);
            switch (result.getQueryType()) {
                case CompletionProvider.DOCUMENTATION_QUERY_TYPE:
                    if (result.getDocumentation() != null) {
                        return result;
                    }
                    break;

                case CompletionProvider.TOOLTIP_QUERY_TYPE:
                    if (result.getToolTip() != null) {
                        return result;
                    }
                    break;
                    
                default:
                    throw new IllegalStateException();
            }
        }
        return null;
    }
    
    private static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    // ..........................................................................
    
    CompletionLayout testGetCompletionLayout() {
        return layout;
    }
    
    void testSetActiveComponent(JTextComponent component) {
        activeComponent = new WeakReference<JTextComponent>(component);
    }

    // ..........................................................................

    /**
     * Workaround for http://netbeans.org/bugzilla/show_bug.cgi?id=223290 .
     * 
     * Client needs to explicitly repaint its CompletionItem-s when their full 
     * state is computation is finished in a background thread.
     */
    public void repaintCompletionView() {
        layout.repaintCompletionView();
    }
    
    private final class CompletionShowAction extends AbstractAction {
        private int queryType;
        
        private CompletionShowAction(int queryType) {
            this.queryType = queryType;
        }

        public void actionPerformed(ActionEvent e) {
            autoModEndOffset = -1;
            showCompletion(true, false, false, queryType);
        }
    }

    private final class DocShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showDocumentation();
        }
    }

    private final class ToolTipShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showToolTip();
        }
    }

    private final class ParamRunnable implements Runnable {
        
        private static final int SHOW_COMPLETION = 0;
        private static final int SHOW_COMPLETION_SUB_ITEMS = 1;
        private static final int SHOW_DOCUMENTATION = 2;
        private static final int SHOW_TOOL_TIP = 3;
        private static final int HIDE_COMPLETION_PANE = 4;
        private static final int HIDE_DOCUMENTATION_PANE = 5;
        private static final int HIDE_TOOL_TIP_PANE = 6;
        
        private final int opCode;
        private final boolean explicit;
        private final boolean delayQuery;
        private final int type;
        
        ParamRunnable(int opCode) {
            this(opCode, false);
        }
        
        ParamRunnable(int opCode, boolean explicit) {
            this(opCode, explicit, false, CompletionProvider.COMPLETION_QUERY_TYPE);
        }

        ParamRunnable(int opCode, boolean explicit, boolean delayQuery, int type) {
            this.opCode = opCode;
            this.explicit = explicit;
            this.delayQuery = delayQuery;
            this.type = type;
        }

        public void run() {
            switch (opCode) {
                case SHOW_COMPLETION:
                    showCompletion(explicitQuery, false, delayQuery, type);
                    break;

                case SHOW_COMPLETION_SUB_ITEMS:
                    showCompletion(explicitQuery, false, delayQuery, type);
                    break;

                case SHOW_DOCUMENTATION:
                    showDocumentation();
                    break;
                    
                case SHOW_TOOL_TIP:
                    showToolTip();
                    break;
                    
                case HIDE_COMPLETION_PANE:
                    hideCompletionPane(explicit);
                    break;

                case HIDE_DOCUMENTATION_PANE:
                    hideDocumentationPane(explicit);
                    break;
                    
                case HIDE_TOOL_TIP_PANE:
                    hideToolTipPane();
                    break;
                    
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    private static void queryResultSets(List<CompletionResultSetImpl> resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl resultSet = resultSets.get(i); 
            resultSet.getTask().query(resultSet.getResultSet());
        }
    }
    
    private static void createRefreshResultSets(List<CompletionResultSetImpl> resultSets, Result refreshResult) {
        List<CompletionResultSetImpl> refreshResultSets = refreshResult.getResultSets();
        int size = resultSets.size();
        // Create new resultSets
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = resultSets.get(i);
            result.markInactive();
            result = new CompletionResultSetImpl(result.getCompletionImpl(),
                    refreshResult, result.getTask(), result.getQueryType());
            refreshResultSets.add(result);
        }
    }
    
    private static void refreshResultSets(List<CompletionResultSetImpl> resultSets, boolean beforeQuery) {
        try {
            int size = resultSets.size();
            for (int i = 0; i < size; i++) {
                CompletionResultSetImpl result = resultSets.get(i);
                result.getTask().refresh(beforeQuery ? null : result.getResultSet());
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static void cancelResultSets(List<CompletionResultSetImpl> resultSets) {
        int size = resultSets.size();
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = resultSets.get(i);
            result.markInactive();
            result.getTask().cancel();
        }
    }

      /** Computes distance between strings
      * @param s First string
      * @param t Second string
      * @return Distance between the strings. (Number of changes which have to 
      *         be done to get from <CODE>s</CODE> to <CODE>t</CODE>.
      */
    private static int getDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        n = s.length ();
        m = t.length ();
        if (n == 0) {
          return m;
        }
        if (m == 0) {
          return n;
        }
        d = new int[n+1][m+1];

        // Step 2
        for (i = 0; i <= n; i++) {
          d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
          d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
          s_i = s.charAt (i - 1);

          // Step 4
          for (j = 1; j <= m; j++) {
            t_j = t.charAt (j - 1);

            // Step 5
            if (s_i == t_j) {
              cost = 0;
            }
            else {
              cost = 1;
            }

            // Step 6
            d[i][j] = min (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
          }
        }

        // Step 7
        return d[n][m];        
    }

    private static int min (int a, int b, int c) {
        int mi;
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;
   }

    /**
     * Result holding list of completion result sets.
     * <br>
     * Initially the result is in unprepared state which allows the holding
     * thread to add the result sets and start the tasks.
     * <br>
     * If another thread calls cancel() it has no effect except setting a flag
     * that is returned from the prepared() method.
     * <br>
     * If the result is finished then cancelling physically cancels the result sets.
     */
    final class Result {
        
        private final List<CompletionResultSetImpl> resultSets;
        
        private boolean invoked;                
        private boolean cancelled;
        private boolean beforeQuery = true;
        
        Result(int resultSetsSize) {
            resultSets = new ArrayList<CompletionResultSetImpl>(resultSetsSize);
        }

        /**
         * Get the contained resultSets.
         *
         * @return non-null resultSets.
         */
        List<CompletionResultSetImpl> getResultSets() {
            return resultSets;
        }

        /**
         * Cancel the resultSets.
         * <br>
         * If the result is not prepared a flag that the result
         * was cancelled is turned on (and later returned from prepared()).
         * <br>
         * Otherwise physical cancellation of the result sets is done.
         */
        void cancel() {
            boolean fin;
            synchronized (this) {
                assert (!cancelled);
                fin = invoked;
                if (!invoked) {
                    cancelled = true;
                }
            }
            
            if (fin) { // already invoked
                cancelResultSets(resultSets);
            }
        }
        
        synchronized boolean isQueryInvoked() {
            return invoked;
        }
        
        /**
         * Mark the queries were invoked on the tasks in the result sets.
         * @return true if the result was cancelled in the meantime.
         */
        boolean queryInvoked() {
            boolean canc;
            synchronized (this) {
                assert (!invoked);
                invoked = true;
                canc = cancelled;
                beforeQuery = false;
            }
            if (canc) {
                cancelResultSets(resultSets);
            }
            return canc;
        }
        
        /**
         * and return the new result set
         * containing the refreshed results.
         */
        Result createRefreshResult() {
            synchronized (this) {
                if (cancelled) {
                    return null;
                }
                if (beforeQuery) {
                    return this;
                }
                assert (invoked); // had to be invoked
                invoked = false;
            }
            Result refreshResult = CompletionImpl.this.new Result(getResultSets().size());
            refreshResult.beforeQuery = beforeQuery;
            createRefreshResultSets(resultSets, refreshResult);
            return refreshResult;
        }
        
        /**
         * Invoke refreshing of the result sets.
         * This method should be invoked on the result set returned from
         * {@link #createRefreshResult()}.
         */
        void invokeRefresh(boolean docCancel) {
            refreshResultSets(getResultSets(), beforeQuery);
            if (!beforeQuery) {
                queryInvoked();
                synchronized (CompletionImpl.this) {
                    if (completionResult != null) {
                        if (!isAllResultsFinished(completionResult.getResultSets())) {
                            if (docCancel)
                                documentationCancel();
                            pleaseWaitTimer.restart();
                        }
                    }
                }
            }
        }

    }
    
    public CompletionResultSetImpl createTestResultSet(CompletionTask task, int queryType) {
        return new CompletionResultSetImpl(this, "TestResult", task, queryType);
    }
    
    static void uilog(LogRecord rec) {
        rec.setResourceBundle(NbBundle.getBundle(CompletionImpl.class));
        rec.setResourceBundleName(CompletionImpl.class.getPackage().getName() + ".Bundle"); // NOI18N
        rec.setLoggerName(UI_LOG.getName());
        UI_LOG.log(rec);
    }

    private static void initializeProfiling(long since) {
        boolean devel = false;
        assert devel = true;
        if (!devel) {
            return;
        }
        synchronized (CompletionImpl.class) {
            stopProfiling();
            profile = new CompletionImplProfile(since);
        }
    }

    private static synchronized void stopProfiling() {
        if (profile != null) {
            profile.stop();
            profile = null;
        }
    }
}
