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

package org.netbeans.modules.diff.builtin.visualizer.editable;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.nodes.CookieSet;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.editor.EditorUI;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.NbDocument;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.WeakListeners;

/**
 * Panel that shows differences between two files. The code here was originally distributed among DiffPanel and
 * DiffComponent classes.
 * 
 * @author Maros Sandor
 */
public class EditableDiffView extends DiffControllerImpl implements DiffView, DocumentListener, PropertyChangeListener, PreferenceChangeListener, ChangeListener {

    private static final int INITIAL_DIVIDER_SIZE = 32;
    private static final String CONTENT_TYPE_PLAIN = "text/plain";
    
    private Stroke boldStroke = new BasicStroke(3);
    
    // === Default Diff Colors ===========================================================
    private Color colorMissing;
    private Color colorAdded;
    private Color colorChanged;
    private Color colorLines   = Color.DARK_GRAY;
    private Color COLOR_READONLY_BG = new Color(255,200,200);

    private final Difference [] NO_DIFFERENCES = new Difference[0];
    
    /**
     * Left (first) half of the Diff view, contains the editor pane, actions bar and line numbers bar.
     */
    private DiffContentPanel jEditorPane1;

    /**
     * Right (second) half of the Diff view, contains the editor pane, actions bar and line numbers bar.
     */
    private DiffContentPanel jEditorPane2;

    private JEditorPane textualEditorPane;

    private boolean secondSourceAvailable;
    private boolean firstSourceAvailable;
    private boolean firstSourceUnsupportedTextUI;
    private boolean secondSourceUnsupportedTextUI;
    private final boolean binaryDiff;
    
    private JViewport jViewport2;

    final JLabel fileLabel1 = new JLabel();
    final JLabel fileLabel2 = new JLabel();
    final JPanel filePanel1 = new JPanel();
    final JPanel filePanel2 = new JPanel();
    final JPanel textualPanel = new JPanel();
    final JTabbedPane jTabbedPane;
    final JComponent view;
    final JSplitPane jSplitPane1 = new JSplitPane();

    private int diffSerial;
    private Difference[] diffs = NO_DIFFERENCES;
   
    private boolean ignoredUpdateEvents;
    
    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    
    private RequestProcessor.Task   refreshDiffTask;
    private DiffViewManager manager;
    
    private boolean actionsEnabled;
    private DiffSplitPaneUI spui;
    
    private Document baseDocument;
    private Document modifiedDocument;
    private Boolean skipFile;
    
    /**
     * The right pane is editable IFF editableCookie is not null.
     */ 
    private EditorCookie.Observable editableCookie;
    private Document editableDocument;
    private UndoRedo.Manager editorUndoRedo;
    private EditableDiffMarkProvider diffMarkprovider;

    private Integer askedLineLocation;
    private static final String PROP_SMART_SCROLLING_DISABLED = "diff.smartScrollDisabled"; //NOI18N
    static final RequestProcessor rp = new RequestProcessor("EditableDiffViewRP", 10);
    private static final Logger LOG = Logger.getLogger(EditableDiffView.class.getName());

    private static final String CONTENT_TYPE_DIFF = "text/x-diff"; //NOI18N
    private final JPanel searchContainer;
    private static final String PROP_SEARCH_CONTAINER = "diff.search.container"; //NOI18N
    private final Object DIFFING_LOCK = new Object();
    private final String name1;
    private final String name2;
    private boolean sourcesInitialized;
    private boolean viewAdded;
    private boolean addedToHierarchy;

    public EditableDiffView (final StreamSource ss1, final StreamSource ss2) {
        this(ss1, ss2, false);
    }

    public EditableDiffView(final StreamSource ss1, final StreamSource ss2, boolean enhancedView) {
        refreshDiffTask = rp.create(new RefreshDiffTask());
        initColors();
        String title1 = ss1.getTitle();
        if (title1 == null) title1 = NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoTitle"); // NOI18N
        String title2 = ss2.getTitle();
        if (title2 == null) title2 = NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoTitle"); // NOI18N
        String mimeType1 = ss1.getMIMEType();
        String mimeType2 = ss2.getMIMEType();
        name1 = ss1.getName();
        name2 = ss2.getName();
        if (mimeType1 == null) mimeType1 = mimeType2;
        if (mimeType2 == null) mimeType2 = mimeType1;
        binaryDiff = mimeType1 == null || mimeType2 == null || mimeType1.equals("application/octet-stream") || mimeType2.equals("application/octet-stream");        
        
        actionsEnabled = ss2.isEditable();
        diffMarkprovider = new EditableDiffMarkProvider();        

        view = new JPanel(new BorderLayout(0, 0)) {

            @Override
            public void addNotify () {
                super.addNotify();
                viewAdded();
            }

            @Override
            public void removeNotify () {
                viewRemoved();
                super.removeNotify();
            }
            
        };
        searchContainer = new JPanel();
        searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.Y_AXIS));
        view.add(searchContainer, BorderLayout.PAGE_END);
        if (enhancedView) {
            jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
            jTabbedPane.putClientProperty("diff-view-mode-switcher", true);
            view.add(jTabbedPane, BorderLayout.CENTER);
        } else {
            jTabbedPane = null;
            view.add(jSplitPane1, BorderLayout.CENTER);
        }
        initComponents ();

        if (!binaryDiff) {
            jEditorPane2.getEditorPane().putClientProperty(DiffMarkProviderCreator.MARK_PROVIDER_KEY, diffMarkprovider);
        }
        jSplitPane1.setName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "DiffComponent.title", ss1.getName(), ss2.getName())); // NOI18N
        spui = new DiffSplitPaneUI(jSplitPane1);
        jSplitPane1.setUI(spui);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setDividerSize(INITIAL_DIVIDER_SIZE);
        jSplitPane1.putClientProperty("PersistenceType", "Never"); // NOI18N
        jSplitPane1.putClientProperty("diff-view-mode-splitter", true);
        jSplitPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yName"));  // NOI18N
        jSplitPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yDesc"));  // NOI18N
        view.setName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "DiffComponent.title", ss1.getName(), ss2.getName())); // NOI18N
        view.putClientProperty("PersistenceType", "Never"); // NOI18N
        view.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yName"));  // NOI18N
        view.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yDesc"));  // NOI18N
        initializeTabPane(ss1, ss2);
        
        setSourceTitle(fileLabel1, title1);
        setSourceTitle(fileLabel2, title2);

        final String f1 = mimeType1;
        final String f2 = mimeType2;
        boolean canceled = Thread.interrupted();
        try {
            Runnable awtTask = new Runnable() {
                public void run() {
                    Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
                    if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
                    jSplitPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    view.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    
                    if (binaryDiff) {
                        adjustPreferredSizes();
                        return;
                    }
                    
                    jEditorPane1.getScrollPane().setBorder(null);
                    jEditorPane2.getScrollPane().setBorder(null);
                    jEditorPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    jEditorPane2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    
                    EditorKit editorKit;
                    try {
                        editorKit = CloneableEditorSupport.getEditorKit(f1);
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.INFO, ss1.toString(), ex);
                        editorKit = CloneableEditorSupport.getEditorKit(CONTENT_TYPE_PLAIN);
                    }
                    jEditorPane1.getEditorPane().setEditorKit(editorKit);
                    repairTextUI(jEditorPane1.getEditorPane());
                    jEditorPane1.getEditorPane().putClientProperty("usedByCloneableEditor", Boolean.TRUE); //NOI18N
                    try {
                        editorKit = CloneableEditorSupport.getEditorKit(f2);
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.INFO, ss2.toString(), ex);
                        editorKit = CloneableEditorSupport.getEditorKit(CONTENT_TYPE_PLAIN);
                    }
                    jEditorPane2.getEditorPane().setEditorKit(editorKit);
                    repairTextUI(jEditorPane2.getEditorPane());
                    jEditorPane2.getEditorPane().putClientProperty("usedByCloneableEditor", Boolean.TRUE); //NOI18N

                    if (jTabbedPane != null) {
                        textualEditorPane.setEditorKit(CloneableEditorSupport.getEditorKit(CONTENT_TYPE_DIFF));
                        repairTextUI(textualEditorPane);
                        setTextualContent();
                    }

                    manager = new DiffViewManager(EditableDiffView.this);
                    
                    rp.post(new Runnable() {

                        @Override
                        public void run () {
                            final Document doc1 = getSourceDocument(ss1);
                            final Document doc2 = getSourceDocument(ss2);
                            EventQueue.invokeLater(new Runnable() {
                                
                                @Override
                                public void run () {
                                    try {
                                        setSource1(ss1, doc1);
                                    } catch (IOException ioex) {
                                        Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, "Diff source 1 unavailable", ioex); //NOI18N
                                    }
                                    try {
                                        setSource2(ss2, doc2);
                                    } catch (IOException ioex) {
                                        Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, "Diff source 2 unavailable", ioex); //NOI18N
                                    }

                                    if (!secondSourceAvailable) {
                                        filePanel2.remove(jEditorPane2);
                                        NoContentPanel ncp = new NoContentPanel(NbBundle.getMessage(EditableDiffView.class,
                                                secondSourceUnsupportedTextUI ? "CTL_DiffPanel_UnsupportedTextUI" : "CTL_DiffPanel_NoContent")); // NOI18N
                                        ncp.setPreferredSize(new Dimension(jEditorPane1.getPreferredSize().width, ncp.getPreferredSize().height));
                                        filePanel2.add(ncp);
                                        actionsEnabled = false;
                                    }
                                    if (!firstSourceAvailable) {
                                        filePanel1.remove(jEditorPane1);
                                        NoContentPanel ncp = new NoContentPanel(NbBundle.getMessage(EditableDiffView.class,
                                                firstSourceUnsupportedTextUI ? "CTL_DiffPanel_UnsupportedTextUI" : "CTL_DiffPanel_NoContent")); // NOI18N
                                        ncp.setPreferredSize(new Dimension(jEditorPane2.getPreferredSize().width, ncp.getPreferredSize().height));
                                        filePanel1.add(ncp);
                                        actionsEnabled = false;
                                    }
                                    adjustPreferredSizes();

                                    JTextComponent leftEditor = jEditorPane1.getEditorPane();
                                    JTextComponent rightEditor = jEditorPane2.getEditorPane();
                                    if (rightEditor.isEditable()) {
                                        setBackgroundColorForNonEditable(leftEditor, rightEditor);
                                    }
                                    if ((rightEditor.getBackground().getRGB() & 0xFFFFFF) == 0) {
                                        colorLines   = Color.WHITE;
                                    }
                                    
                                    manager.init();
                                    
                                    sourcesInitialized = true;
                                    if (viewAdded) {
                                        addListeners();
                                    }
                                    refreshDiff(100);
                                }
                                
                            });
                        }
                        
                    });
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awtTask.run();
            } else {
                 SwingUtilities.invokeAndWait(awtTask);
            }
        } catch (InterruptedException e) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.FINE, ".colorLines:" + colorLines + ", .jviewPort2:" + jViewport2
                    + ", editableDocument:" + editableDocument + ", editableCookie:" + editableCookie + ", editorUndoRedo:" + editorUndoRedo, e);
            canceled = true;
        } catch (InvocationTargetException e) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.SEVERE, ".colorLines:" + colorLines + ", .jviewPort2:" + jViewport2
                    + ", editableDocument:" + editableDocument + ", editableCookie:" + editableCookie + ", editorUndoRedo:" + editorUndoRedo, e);
        }

        if (binaryDiff || canceled) {
            return;
        }
    }

    private void initializeTabPane (StreamSource ss1, StreamSource ss2) {
        if (jTabbedPane != null) {
            jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "EditableDiffView.viewGraphical.title"), jSplitPane1); //NOI18N
            jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "EditableDiffView.viewTextual.title"), textualPanel); //NOI18N
            jTabbedPane.addChangeListener(this);
        }
    }

    private void setBackgroundColorForNonEditable(JTextComponent leftEditor,
                                                  JTextComponent rightEditor) {
        String mimeType = DocumentUtilities.getMimeType(leftEditor);
        if (mimeType == null) {
            mimeType = CONTENT_TYPE_PLAIN;                                    //NOI18N
        }

        Color bgColor = null;

        Lookup lookup = MimeLookup.getLookup(mimeType);
        if (lookup != null) {
            FontColorSettings fontColorSettings = lookup.lookup(FontColorSettings.class);
            if (fontColorSettings != null) {
                AttributeSet attrSet = fontColorSettings.getFontColors(
                                          FontColorNames.GUARDED_COLORING);
                if (attrSet != null) {
                    Object bgColorObj = attrSet.getAttribute(StyleConstants.Background);
                    if (bgColorObj instanceof Color) {
                        bgColor = (Color) bgColorObj;
                    }
                }
            }
        }

        if (bgColor == null) {
            /* Fallback to the old routine: */
            int editableBgColor = rightEditor.getBackground().getRGB() & 0xFFFFFF;
            if ((editableBgColor == 0xFFFFFF)
                    && System.getProperty("netbeans.experimental.diff.ReadonlyBg") == null) { //NOI18N
                bgColor = COLOR_READONLY_BG;
            }
        }

        if (bgColor != null) {
            leftEditor.setBackground(bgColor);
        }
    }

    private void adjustPreferredSizes() {
        // Make sure split pane opens with divider in the center
        Dimension pf1 = fileLabel1.getPreferredSize();
        Dimension pf2 = fileLabel2.getPreferredSize();
        if (pf1.width > pf2.width) {
            fileLabel2.setPreferredSize(new Dimension(pf1.width, pf2.height));
        } else {
            fileLabel1.setPreferredSize(new Dimension(pf2.width, pf1.height));
        }
    }

    @Override
    public void setLocation(final DiffController.DiffPane pane, final DiffController.LocationType type, final int location) {
        if (type == DiffController.LocationType.DifferenceIndex) {
            manager.runWithSmartScrollingDisabled(new Runnable() {
                @Override
                public void run() {
                    setDifferenceImpl(location);
                }
            });
        } else {
            EditableDiffView.this.askedLineLocation = location;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Runnable withDisabledSmartScroll = null;
                    if (pane == DiffController.DiffPane.Base) {
                        if (Boolean.TRUE.equals(getJComponent().getClientProperty(PROP_SMART_SCROLLING_DISABLED))) {
                            withDisabledSmartScroll = new Runnable() {
                                @Override
                                public void run() {
                                    setBaseLineNumberImpl(location, false);
                                }
                            };
                        } else {
                            // refactoring jumps only in the left pane, setting the position must be done with smart scrolling enabled
                            setBaseLineNumberImpl(location, true);
                        }
                    } else {
                        withDisabledSmartScroll = new Runnable() {
                            @Override
                            public void run() {
                                setModifiedLineNumberImpl(location);
                            }
                        };
                    }
                    if (withDisabledSmartScroll != null) {
                        manager.runWithSmartScrollingDisabled(withDisabledSmartScroll);
                    }
                }
            });
        }
    }

    private void setModifiedLineNumberImpl(int line) {
        initGlobalSizes(); // The window might be resized in the mean time.
        try {
            EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(jEditorPane2.getEditorPane());
            if (editorUI == null) return;
            int off2 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane2.getEditorPane().getDocument(), line);
            jEditorPane2.getEditorPane().setCaretPosition(off2);

            int offset = jEditorPane2.getScrollPane().getViewport().getViewRect().height / 2 + 1;
            View rootView = org.netbeans.editor.Utilities.getDocumentView(jEditorPane2.getEditorPane());
            Rectangle rec = jEditorPane2.getEditorPane().modelToView(rootView.getView(line).getEndOffset() - 1);
            int lineOffset;
            if (rec == null) {
                int lineHeight = editorUI.getLineHeight();
                lineOffset = lineHeight * line - offset;
            } else {
                lineOffset = rec.y - offset;
            }

            JScrollBar rightScrollBar = jEditorPane2.getScrollPane().getVerticalScrollBar();

            rightScrollBar.setValue((int) (lineOffset));
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
        } catch (BadLocationException ex) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private void setBaseLineNumberImpl(int line, boolean updateRightPanel) {
        initGlobalSizes(); // The window might be resized in the mean time.
        try {
            EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(jEditorPane1.getEditorPane());
            if (editorUI == null) return;
            
            int offset = jEditorPane1.getScrollPane().getViewport().getViewRect().height / 2 + 1;
            View rootView = org.netbeans.editor.Utilities.getDocumentView(jEditorPane1.getEditorPane());
            View lineView = rootView != null ? rootView.getView(line) : null;
            Rectangle rec = lineView != null ? jEditorPane1.getEditorPane().modelToView(lineView.getEndOffset() - 1) : null;
            int lineOffset;
            if (rec == null) {
                int lineHeight = editorUI.getLineHeight();
                lineOffset = lineHeight * line - offset;
            } else {
                lineOffset = rec.y - offset;
            }
    
            int off1 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane1.getEditorPane().getDocument(), line);
            jEditorPane1.getEditorPane().setCaretPosition(off1);
    
            JScrollBar leftScrollBar = jEditorPane1.getScrollPane().getVerticalScrollBar();
            leftScrollBar.setValue(lineOffset);
            if (updateRightPanel) {
                JScrollBar rightScrollBar = jEditorPane2.getScrollPane().getVerticalScrollBar();
                rightScrollBar.setValue(lineOffset);
                updateCurrentDifference(null);
            }
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
        } catch (BadLocationException ex) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private void setDifferenceImpl(int location) {
        if (location < -1 || location >= diffs.length) throw new IllegalArgumentException("Illegal difference number: " + location); // NOI18N
        if (location == -1) {
        } else {
            setDifferenceIndex(location);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ignoredUpdateEvents = true;
                    showCurrentDifference();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ignoredUpdateEvents = false;
                        }
                    });
                }
            });
        }
    }

    @Override
    public JComponent getJComponent() {
        return view;
    }

    /**
     * @return true if Move, Replace, Insert and Move All actions should be visible and enabled, false otherwise
     */
    public boolean isActionsEnabled() {
        return actionsEnabled;
    }
   
    private void initColors() {
        colorMissing = DiffModuleConfig.getDefault().getDeletedColor();
        colorAdded = DiffModuleConfig.getDefault().getAddedColor(); 
        colorChanged = DiffModuleConfig.getDefault().getChangedColor();
    }

    private void addDocumentListeners() {
        if (baseDocument != null) baseDocument.addDocumentListener(this);
        if (modifiedDocument != null) modifiedDocument.addDocumentListener(this);
    }

    private void removeDocumentListeners() {
        if (baseDocument != null) baseDocument.removeDocumentListener(this);
        if (modifiedDocument != null) modifiedDocument.removeDocumentListener(this);
    }
    
    private void viewAdded () {
        DiffModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        addedToHierarchy = true;
        if (sourcesInitialized) {
            addListeners();
            refreshDiff(50);
        } else {
            viewAdded = true;
        }
    }

    private void refreshEditableDocument() {
        Document doc = null;
        try {
            doc = editableCookie.openDocument();
        } catch (IOException e) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, "Getting new Document from EditorCookie", e); // NOI18N
            return;
        }
        editableDocument.removeDocumentListener(this);
        if (doc != editableDocument) {
            editableDocument = doc;
            jEditorPane2.getEditorPane().setDocument(editableDocument);
            refreshDiff(20);
        }
        editableDocument.addDocumentListener(this);
    }

    private void viewRemoved () {
        viewAdded = false;
        if (addedToHierarchy) {
            addedToHierarchy = false;
            DiffModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
            if (sourcesInitialized) {
                removeDocumentListeners();
                if (editableCookie != null) {
                    editableCookie.removePropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        initColors();
        diffChanged();  // trigger re-calculation of hightlights in case diff stays the same
        refreshDiff(20);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        refreshDiff(50);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        refreshDiff(50);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        refreshDiff(50);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (jTabbedPane == e.getSource()) {
            if (jTabbedPane.getSelectedComponent() == jSplitPane1) {
                updateCurrentDifference(null);
            } else {
                setDifferenceIndex(-1);
            }
        }
    }
    
    Color getColor(Difference ad) {
        if (ad.getType() == Difference.ADD) return colorAdded;
        if (ad.getType() == Difference.CHANGE) return colorChanged;
        return colorMissing;
    }
    
    JComponent getMyDivider() {
        return spui.splitPaneDivider.getDivider();
    }

    DiffContentPanel getEditorPane1() {
        return jEditorPane1;
    }

    DiffContentPanel getEditorPane2() {
        return jEditorPane2;
    }

    DiffViewManager getManager() {
        return manager;
    }

    Difference[] getDifferences() {
        return diffs;
    }

    private void replace(final StyledDocument doc, final int start, final int length, final String text) {
        NbDocument.runAtomic(doc, new Runnable() {
            @Override    
            public void run() {
                try {
                    doc.remove(start, length);
                    doc.insertString(start, text, null);
                } catch (BadLocationException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });
    }
    
    /**
     * Rolls back a difference in the second document.
     * 
     * @param diff a difference to roll back, null to remove all differences
     */ 
    void rollback(Difference diff) {
        StyledDocument document = (StyledDocument) getEditorPane2().getEditorPane().getDocument();
        if (diff == null) {
            Document src = getEditorPane1().getEditorPane().getDocument();
            try {
                replace(document, 0, document.getLength(), src.getText(0, src.getLength()));
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            return;
        }
        try {
            if (diff.getType() == Difference.ADD) {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondEnd());
                if (end == -1) {
                    end = document.getLength();
                }
                document.remove(start, end - start);
            } else if (diff.getType() == Difference.DELETE) {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart());
                /**
                 * If adding as the last line, there is no line after
                 * And start is -1;
                 */
                String addedText = diff.getFirstText();
                if (start == -1) {
                    start = document.getLength();
                    addedText = switchLineEndings(addedText);
                }
                document.insertString(start, addedText, null);
            } else {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondEnd());
                if (end == -1) {
                    end = document.getLength();
                }
                replace(document, start, end - start, diff.getFirstText());
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Moves empty line from the end to the beginning
     * @param addedText
     * @return
     */
    private static String switchLineEndings(String addedText) {
        StringBuilder sb = new StringBuilder(addedText);
        sb.insert(0, '\n'); // add a line to the beginning
        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1); // and remove the last empty line
        }
        return sb.toString();
    }

    Stroke getBoldStroke() {
        return boldStroke;
    }

    private void addListeners () {
        expandFolds();
        initGlobalSizes();
        addChangeListeners();
        addDocumentListeners();

        if (editableCookie == null) return;
        refreshEditableDocument();
        editableCookie.addPropertyChangeListener(this);
    }

    class DiffSplitPaneUI extends BasicSplitPaneUI {

        final DiffSplitPaneDivider splitPaneDivider;

        public DiffSplitPaneUI(JSplitPane splitPane) {
            this.splitPane = splitPane;
            splitPaneDivider = new DiffSplitPaneDivider(this, EditableDiffView.this);
        }

        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return splitPaneDivider;
        }
    }
    
    public boolean requestFocusInWindow() {
        return jEditorPane1.requestFocusInWindow();
    }

    @Override
    public JComponent getComponent() {
        return view;
    }

    @Override
    public int getDifferenceCount() {
        int retval = diffs.length;
        if (jTabbedPane != null && jTabbedPane.getSelectedComponent() == textualPanel) {
            retval = 0;
        }
        return retval;
    }

    @Override
    public boolean canSetCurrentDifference() {
        return jTabbedPane == null || jTabbedPane.getSelectedComponent() != textualPanel;
    }

    @Override
    public void setCurrentDifference(int diffNo) throws UnsupportedOperationException {
        setLocation(null, DiffController.LocationType.DifferenceIndex, diffNo);
    }

    @Override
    public int getCurrentDifference() {
        int retval = getDifferenceIndex();
        if (!canSetCurrentDifference()) {
            retval = -1;
        }
        return retval;
    }

    private int computeCurrentDifference (Boolean down) {
        // jViewport == null iff initialization failed
        if (manager == null || jViewport2 == null) return 0;
        Rectangle viewRect = jViewport2.getViewRect();
        int bottom = viewRect.y + viewRect.height * 2 / 3;
        DiffViewManager.DecoratedDifference [] ddiffs = manager.getDecorations();
        if (Boolean.FALSE.equals(down)) {
            // moving up
            if (viewRect.y != 0) { // the first diff should be marked if at the top
                int up = viewRect.y + viewRect.height * 1 / 3;
                for (int i = ddiffs.length - 1; i >= 0; i--) {
                    int startLine = ddiffs[i].getTopRight();
                    int endLine = ddiffs[i].getBottomRight();
                    if (startLine < up && endLine < up) {
                        return Math.min(ddiffs.length - 1, i + 1);
                    }
                }
            }
            return ddiffs.length == 0 ? -1 : 0;
        } else {
            // moving down
            for (int i = 0; i < ddiffs.length; i++) {
                int startLine = ddiffs[i].getTopRight();
                int endLine = ddiffs[i].getBottomRight();
                if (startLine > bottom && (endLine > bottom || endLine == -1)) {
                    return Math.max(0, i-1);
                }
            }
            return ddiffs.length - 1;
        }
    }

    /**
     * Notifies the Diff View that it should update the current difference index. If the update is called in the scope
     * of setCurrentDifference() method, this method does nothing. If not, it computes current difference base on
     * current view. This is to ensure the following workflow:
     * 1) If user only pushes Next/Previous buttons in Diff, he wants to review changes one by one
     * 2) If user touches the scrollbar, 'current difference' changes accordingly 
     */
    void updateCurrentDifference (Boolean down) {
        assert SwingUtilities.isEventDispatchThread();
        if (ignoredUpdateEvents) {
            return;
        }
        int cd = computeCurrentDifference(down);
        setDifferenceIndex(cd);
    }
    
    @Override
    public JToolBar getToolBar() {
        return null;
    }

    private void showCurrentDifference() {
        int index = getDifferenceIndex();
        if (index < 0 || index >= diffs.length || index >= manager.getDecorations().length) {
            return;
        }
        final Difference diff = diffs[index];
        initGlobalSizes(); // The window might be resized in the mean time.
        try {
            final StyledDocument doc1 = (StyledDocument) jEditorPane1.getEditorPane().getDocument();
            final StyledDocument doc2 = (StyledDocument) jEditorPane2.getEditorPane().getDocument();
            
            final int offCurrent = jEditorPane2.getEditorPane().getCaretPosition();
            doc1.render(new Runnable() {

                @Override
                public void run () {
                    int offFirstStart = org.openide.text.NbDocument.findLineOffset(doc1, diff.getFirstStart() > 0 ? diff.getFirstStart() - 1 : 0);
                    jEditorPane1.getEditorPane().setCaretPosition(offFirstStart);
                }
            });
            doc2.render(new Runnable() {

                @Override
                public void run () {
                    int offSecondStart = org.openide.text.NbDocument.findLineOffset(doc2, diff.getSecondStart() > 0 ? diff.getSecondStart() - 1 : 0);
                    int offSecondEnd;
                    if(diff.getSecondEnd() > diff.getSecondStart()) {
                        offSecondEnd = org.openide.text.NbDocument.findLineOffset(doc2, diff.getSecondEnd() > 0 ? diff.getSecondEnd() - 1 : 0);
                    } else {
                        int lastLine = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getLength()) + 1;
                        if(diff.getSecondStart() < lastLine) {
                            offSecondEnd = org.openide.text.NbDocument.findLineOffset(doc2, diff.getSecondStart());
                        } else {
                            offSecondEnd = doc2.getLength();
                        }
                    }

                    if(offCurrent < offSecondStart || offCurrent > offSecondEnd) {
                        // it could be somebody is editing right now,
                        // so set the caret on the diferences first lines first column only in case
                        // it isn't already somrwhere in the line 
                        jEditorPane2.getEditorPane().setCaretPosition(offSecondStart);
                    }
                }
            });
            
            DiffViewManager.DecoratedDifference ddiff = manager.getDecorations()[index];
            int offset;
            offset = jEditorPane2.getScrollPane().getViewport().getViewRect().height / 2 + 1;
            jEditorPane2.getScrollPane().getVerticalScrollBar().setValue(ddiff.getTopRight() - offset);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
        }

        // scroll the left pane accordingly
        manager.scroll(index == diffs.length - 1 || index == 0);
    }
    
    /** This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        fileLabel1.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        fileLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        filePanel1.setLayout(new BorderLayout());
        filePanel1.add(fileLabel1, BorderLayout.PAGE_START);

        fileLabel2.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        fileLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        filePanel2.setLayout(new BorderLayout());
        filePanel2.add(fileLabel2, BorderLayout.PAGE_START);

        textualPanel.setLayout(new BorderLayout());

        if (binaryDiff) {
            NoContentPanel ncp1 = new NoContentPanel(NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_BinaryFile"));
            fileLabel1.setLabelFor(ncp1);
            filePanel1.add(ncp1);
            NoContentPanel ncp2 = new NoContentPanel(NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_BinaryFile"));
            fileLabel2.setLabelFor(ncp2);
            filePanel2.add(ncp2);
        } else {
            jEditorPane1 = new DiffContentPanel(this, true);
            jEditorPane2 = new DiffContentPanel(this, false);
            jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yName"));  // NOI18N
            jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
            jEditorPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane2A11yName"));  // NOI18N
            jEditorPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane2A11yDescr"));  // NOI18N
            fileLabel1.setLabelFor(jEditorPane1);
            filePanel1.add(jEditorPane1);
            fileLabel2.setLabelFor(jEditorPane2);
            filePanel2.add(jEditorPane2);
            textualEditorPane = new JEditorPane() {
                private int fontHeight = -1;
                private int charWidth;
                
                @Override
                public void setFont(Font font) {
                    super.setFont(font);
                    FontMetrics metrics = getFontMetrics(font);
                    charWidth = metrics.charWidth('m');
                    fontHeight = metrics.getHeight();
                }

                @Override
                public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                    if (fontHeight == -1) {
                        return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
                    }
                    switch (orientation) {
                    case SwingConstants.VERTICAL:
                        return fontHeight;
                    case SwingConstants.HORIZONTAL:
                        return charWidth;
                    default:
                        throw new IllegalArgumentException("Invalid orientation: " + orientation); // discrimination
                    }
                }
            };
            textualEditorPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yName"));  // NOI18N
            textualEditorPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
            textualPanel.add(new JScrollPane(textualEditorPane));
            textualEditorPane.putClientProperty(PROP_SEARCH_CONTAINER, searchContainer);
            jEditorPane1.getEditorPane().putClientProperty(PROP_SEARCH_CONTAINER, searchContainer);
            jEditorPane2.getEditorPane().putClientProperty(PROP_SEARCH_CONTAINER, searchContainer);
        }        
        
        jSplitPane1.setLeftComponent(filePanel1);
        jSplitPane1.setRightComponent(filePanel2);        
        
        // aqua background workaround
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N 
            jSplitPane1.setBackground(color);
            filePanel1.setBackground(color); 
            filePanel2.setBackground(color); 
            view.setBackground(color); 
            view.setOpaque(true);
        }   
    
    }

    WeakHashMap<JEditorPane, FoldHierarchyListener> hieararchyListeners = new WeakHashMap<JEditorPane, FoldHierarchyListener>(2);
    // Code for dispatching events from components to event handlers.
    private void expandFolds(JEditorPane pane) {
        final FoldHierarchy fh = FoldHierarchy.get(pane);
        FoldUtilities.expandAll(fh);
        FoldHierarchyListener list = new FoldHierarchyListener() {
            @Override
            public void foldHierarchyChanged(FoldHierarchyEvent evt) {
                FoldUtilities.expandAll(fh);
            }
        };
        hieararchyListeners.put(pane, list);
        fh.addFoldHierarchyListener(WeakListeners.create(FoldHierarchyListener.class, list, fh));
    }

    private void expandFolds() {
        expandFolds(jEditorPane1.getEditorPane());
        expandFolds(jEditorPane2.getEditorPane());
    }

    private void initGlobalSizes() {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getEditorPane().getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getEditorPane().getDocument();
        int numLines1 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getEndPosition().getOffset());
        int numLines2 = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getEndPosition().getOffset());

        int numLines = Math.max(numLines1, numLines2);
        if (numLines < 1) numLines = 1;
        int totHeight = jEditorPane1.getSize().height;
        int value = jEditorPane2.getSize().height;
        if (value > totHeight) totHeight = value;
    }

    private void joinScrollBars() {
        final JScrollBar scrollBarH1 = jEditorPane1.getScrollPane().getHorizontalScrollBar();
        final JScrollBar scrollBarH2 = jEditorPane2.getScrollPane().getHorizontalScrollBar();

        scrollBarH1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH1.getValue();
                if (value == horizontalScroll1ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max1 == ext1) horizontalScroll2ChangedValue = 0;
                else horizontalScroll2ChangedValue = (int) (((long) value * (max2 - ext2)) / (max1 - ext1));
                horizontalScroll1ChangedValue = -1;
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        scrollBarH2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH2.getValue();
                if (value == horizontalScroll2ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max2 == ext2) horizontalScroll1ChangedValue = 0;
                else horizontalScroll1ChangedValue = (int) (((long) value * (max1 - ext1)) / (max2 - ext2));
                horizontalScroll2ChangedValue = -1;
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
            }
        });
    }

    private void customizeEditor(JEditorPane editor) {
        StyledDocument doc;
        Document document = editor.getDocument();
        try {
            doc = (StyledDocument) editor.getDocument();
        } catch(ClassCastException e) {
            doc = new DefaultStyledDocument();
            try {
                doc.insertString(0, document.getText(0, document.getLength()), null);
            } catch (BadLocationException ble) {
                // leaving the document empty
            }
            editor.setDocument(doc);
        }
    }

    WeakHashMap<JEditorPane, PropertyChangeListener> propertyChangeListeners = new WeakHashMap<JEditorPane, PropertyChangeListener>(2);
    private void addChangeListeners() {
        // using rather weak listeners, repeated ancestorRemoved/ancestorAdded creates and attaches number of new listeners and consumes memory
        PropertyChangeListener list = new java.beans.PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("font".equals(evt.getPropertyName())) {
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            diffChanged();
                            initGlobalSizes();
                            jEditorPane1.onUISettingsChanged();
                            getComponent().revalidate();
                            getComponent().repaint();
                        }
                    });
                }
            }
        };
        propertyChangeListeners.put(jEditorPane1.getEditorPane(), list);
        jEditorPane1.getEditorPane().addPropertyChangeListener(WeakListeners.propertyChange(list, jEditorPane1.getEditorPane())); //NOI18N
        list = new java.beans.PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("font".equals(evt.getPropertyName())) {
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            diffChanged();
                            initGlobalSizes();
                            jEditorPane2.onUISettingsChanged();
                            getComponent().revalidate();
                            getComponent().repaint();
                        }
                    });
                }
            }
        };
        propertyChangeListeners.put(jEditorPane2.getEditorPane(), list);
        jEditorPane2.getEditorPane().addPropertyChangeListener(WeakListeners.propertyChange(list, jEditorPane2.getEditorPane())); //NOI18N
    }

    private synchronized void diffChanged() {
        diffSerial++;   // we need to re-compute decorations, font size changed
    }

    private void setSource1 (StreamSource ss, Document sdoc) throws IOException {
        firstSourceAvailable = false; 
        EditorKit kit = jEditorPane1.getEditorPane().getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N

        baseDocument = sdoc;
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        if (!Boolean.TRUE.equals(skipFile)) {
            if (jEditorPane1.getEditorPane().getUI() instanceof BaseTextUI) {
                if (sdoc == null) {
                    Reader r = ss.createReader();
                    if (r != null) {
                        firstSourceAvailable = true;
                        try {
                            kit.read(r, doc, 0);
                        } catch (javax.swing.text.BadLocationException e) {
                            throw new IOException("Can not locate the beginning of the document."); // NOI18N
                        } finally {
                            r.close();
                        }
                    }
                } else {
                    firstSourceAvailable = true;
                }
            } else {
                firstSourceUnsupportedTextUI = true;
            }
        }
        jEditorPane1.initActions();        
        jEditorPane1.getEditorPane().setDocument(doc);
        customizeEditor(jEditorPane1.getEditorPane());
    }
    
    private Document getSourceDocument(StreamSource ss) {
        Document sdoc = null;
        FileObject fo = ss.getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                if (dao.getPrimaryFile() == fo) {
                    EditorCookie ec = dao.getCookie(EditorCookie.class);
                    if (ec != null) {
                        try {
                            sdoc = ec.openDocument();
                        } catch (UserQuestionException ex) {
                            boolean open = !Boolean.TRUE.equals(skipFile);
                            if (skipFile == null) {
                                NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(ex.getLocalizedMessage(),
                                    NbBundle.getMessage(EditableDiffView.class, "EditableDiffView.ConfirmOpenningTitle"), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION); //NOI18N
                                open = DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION);
                            }
                            if (open) {
                                LOG.log(Level.INFO, "User acepted UQE: {0}", fo.getPath()); //NOI18N
                                ex.confirmed();
                                sdoc = ec.openDocument();
                            } else {
                                sdoc = null;
                                this.skipFile = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // fallback to other means of obtaining the source
            }
        } else {
            sdoc = ss.getLookup().lookup(Document.class);
        }
        return sdoc;
    }

    private void setSource2 (StreamSource ss, Document sdoc) throws IOException {
        secondSourceAvailable = false;
        EditorKit kit = jEditorPane2.getEditorPane().getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        
        modifiedDocument = sdoc;
        if (sdoc != null && ss.isEditable()) {
            DataObject dao = (DataObject) sdoc.getProperty(Document.StreamDescriptionProperty);
            if (dao != null) {
                if (dao instanceof MultiDataObject) {
                    MultiDataObject mdao = (MultiDataObject) dao;
                    for (MultiDataObject.Entry entry : mdao.secondaryEntries()) {
                        if (entry instanceof CookieSet.Factory) {
                            CookieSet.Factory factory = (CookieSet.Factory) entry;
                            EditorCookie ec = factory.createCookie(EditorCookie.class);
                            Document entryDocument = ec.getDocument();
                            if (entryDocument == sdoc && ec instanceof EditorCookie.Observable) {
                                editableCookie = (EditorCookie.Observable) ec;
                                editableDocument = sdoc;
                                editorUndoRedo = getUndoRedo(ec);
                            }
                        }
                    }
                }
                if (editableCookie == null) {
                    EditorCookie cookie = dao.getCookie(EditorCookie.class);
                    if (cookie instanceof EditorCookie.Observable) {
                        editableCookie = (EditorCookie.Observable) cookie;
                        editableDocument = sdoc;
                        editorUndoRedo = getUndoRedo(cookie);
                    }
                }
            }
        }
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        if (sdoc != null || !Boolean.TRUE.equals(skipFile)) {
            if (jEditorPane2.getEditorPane().getUI() instanceof BaseTextUI) {
                if (sdoc == null) {
                    Reader r = ss.createReader();
                    if (r != null) {
                        secondSourceAvailable = true;
                        try {
                            kit.read(r, doc, 0);
                        } catch (javax.swing.text.BadLocationException e) {
                            throw new IOException("Can not locate the beginning of the document."); // NOI18N
                        } finally {
                            r.close();
                        }
                    }
                } else {
                    secondSourceAvailable = true;
                }
            } else {
                secondSourceUnsupportedTextUI = true;
            }
        }
        jEditorPane2.initActions();
        view.putClientProperty(UndoRedo.class, editorUndoRedo);
        jEditorPane2.getEditorPane().setDocument(doc);
        jEditorPane2.getEditorPane().setEditable(editableCookie != null);
        if (doc instanceof NbDocument.CustomEditor) {
            Component c = ((NbDocument.CustomEditor)doc).createEditor(jEditorPane2.getEditorPane());
            if (c instanceof JComponent) {
                jEditorPane2.setCustomEditor((JComponent)c);
            }
        }
        
        customizeEditor(jEditorPane2.getEditorPane());
        jViewport2 = jEditorPane2.getScrollPane().getViewport();
        joinScrollBars();
    }

    private void setTextualContent () {
        if (jTabbedPane.getSelectedComponent() == textualPanel) {
            countTextualDiff();
        } else {
            jTabbedPane.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged (ChangeEvent e) {
                    if (jTabbedPane.getSelectedComponent() == textualPanel) {
                        jTabbedPane.removeChangeListener(this);
                        countTextualDiff();
                    }
                }
            });
        }
        textualEditorPane.setEditable(false);
    }

    private void countTextualDiff () {
        final EditorKit kit = textualEditorPane.getEditorKit();
        rp.post(new Runnable() {
            @Override
            public void run() {
                synchronized (DIFFING_LOCK) {
                    Document doc = kit.createDefaultDocument();
                    doc.putProperty("mimeType", CONTENT_TYPE_DIFF); //NOI18N
                    StyledDocument sdoc = doc instanceof StyledDocument ? (StyledDocument) doc : null;
                    textualRefreshTask = new TextualDiffRefreshTask(sdoc);
                    textualRefreshTask.refresh(diffs);
                }
            }
        });
    }

    private TextualDiffRefreshTask textualRefreshTask;
    @NbBundle.Messages({
        "Diff.dev_null=/dev/null"
    })
    private class TextualDiffRefreshTask implements Cancellable {

        final StyledDocument out;
        private boolean canceled;

        public TextualDiffRefreshTask(StyledDocument out) {
            this.out = out;
        }

        public void refresh (Difference[] differences) {
            canceled = false;
            synchronized (this) {
                boolean docReady = false;
                if (out != null) {
                    try {
                        exportDiff(differences);
                        docReady = true;
                    } catch (IOException ex) {
                        Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, null, ex);
                    }
                }
                if (isCanceled()) {
                    return;
                }
                final boolean textualDiffReady = docReady;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (textualDiffReady) {
                            textualEditorPane.setDocument(out);
                            textualEditorPane.setCaretPosition(0);
                        } else {
                            textualPanel.remove(textualEditorPane);
                            NoContentPanel ncp = new NoContentPanel(NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoContent")); // NOI18N
                            textualPanel.add(ncp);
                        }
                    }
                });
            }
        }

        private void exportDiff (Difference[] differences) throws IOException {
            Reader r1 = null;
            Reader r2 = null;
            
            if (differences == null || !firstSourceAvailable || !secondSourceAvailable) {
                differences = computeDiff(true);
            }

            try {
                final InputStream is;
                r1 = getReader(jEditorPane1.getEditorPane().getDocument());
                if (r1 == null) {
                    r1 = new StringReader(""); // NOI18N
                }
                if (isCanceled()) {
                    return;
                }
                r2 = getReader(jEditorPane2.getEditorPane().getDocument());
                if (r2 == null) {
                    r2 = new StringReader(""); // NOI18N
                }
                if (isCanceled()) {
                    return;
                }
                TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                        firstSourceAvailable ? "a/" + name1 : Bundle.Diff_dev_null(),
                        secondSourceAvailable ? "b/" + name2 : Bundle.Diff_dev_null(),
                        null, null, r1, r2, differences);
                info.setContextMode(true, 3);
                final String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
                if (isCanceled()) {
                    return;
                }
                NbDocument.runAtomic(out, new Runnable() {
                    @Override
                    public void run() {
                        String sep = System.getProperty("line.separator"); // NOI18N
                        try {
                            out.remove(0, out.getLength());
                            out.insertString(0, new StringBuilder("# This patch file was generated by NetBeans IDE").append(sep) //NOI18N
                                    .append("# It uses platform neutral UTF-8 encoding and \\n newlines.").append(sep) //NOI18N
                                    .append(diffText).toString(), null);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(EditableDiffView.class.getName()).log(Level.WARNING, null, ex);
                        }
                    }
                });
            } finally {
                if (r1 != null) {
                    try {
                        r1.close();
                    } catch (Exception e) {
                    }
                }
                if (r2 != null) {
                    try {
                        r2.close();
                    } catch (Exception e) {
                    }
                }
            }
        }

        @Override
        public boolean cancel() {
            return canceled = true;
        }

        boolean isCanceled () {
            return canceled;
        }
    }
    
    private UndoRedo.Manager getUndoRedo(EditorCookie cookie) {
        // TODO: working around #96543 
        try {
            Method method = CloneableEditorSupport.class.getDeclaredMethod("getUndoRedo"); // NOI18N
            method.setAccessible(true);
            return (UndoRedo.Manager) method.invoke(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshEditableDocument();
                }
            }); 
        }
    }

    public void setSourceTitle(JLabel label, String title) {
        label.setText(title);
        label.setToolTipText(title);
        // Set the minimum size in 'x' direction to a low value, so that the splitter can be moved to corner locations
        label.setMinimumSize(new Dimension(3, label.getMinimumSize().height));
    }
    
    public void setDocument1(Document doc) {
        if (doc != null) {
            jEditorPane1.getEditorPane().setDocument(doc);
        }
    }
    
    public void setDocument2(Document doc) {
        if (doc != null) {
            jEditorPane2.getEditorPane().setDocument(doc);
        }
    }

    private void refreshDiff(int delayMillis) {
        refreshDiffTask.schedule(delayMillis);
    }

    public class RefreshDiffTask implements Runnable {

        @Override
        public void run() {
            synchronized (DIFFING_LOCK) {
                final Difference[] differences = computeDiff(false);
                if (textualRefreshTask != null) {
                    textualRefreshTask.refresh(differences);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        diffs = differences;
                        if (diffs != NO_DIFFERENCES) {
                            diffChanged();
                        }
                        if (getDifferenceIndex() >= diffs.length) updateCurrentDifference(null);
                        view.revalidate();
                        support.firePropertyChange(DiffController.PROP_DIFFERENCES, null, null);
                        jEditorPane1.setCurrentDiff(diffs);
                        jEditorPane2.setCurrentDiff(diffs);
                        refreshDividerSize();
                        view.repaint();
                        diffMarkprovider.refresh();
                        if (diffs.length > 0 && !Boolean.TRUE.equals(getJComponent().getClientProperty(PROP_SMART_SCROLLING_DISABLED))) {
                            if (EditableDiffView.this.askedLineLocation != null) {
                                setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, EditableDiffView.this.askedLineLocation);
                            } else if (getCurrentDifference() == -1) {
                                setCurrentDifference(0);
                            }
                        }
                    }
                });
            }
        }
    }

    private Difference[] computeDiff (boolean includeUnavailable) {

        if(editableDocument != null) { 
            // refresh fo before computing the diff, external changes might not have been recognized in some setups
            // see also issue #210834
            DataObject dao = (DataObject) editableDocument.getProperty(Document.StreamDescriptionProperty);
            if (dao != null) {
                Set<FileObject> files = dao.files();
                if(files != null) {
                    for (FileObject fo : files) {
                        LOG.log(Level.FINE, "refreshing FileOBject {0}", fo); // NOI18N
                        fo.refresh();
                    }
                } else {
                    LOG.log(Level.FINE, "no FileObjects to refresh for {0}", dao); // NOI18N
                }
            } else {
                LOG.log(Level.FINE, "no DataObject to refresh"); // NOI18N
            }
        }

        if (!includeUnavailable && (!secondSourceAvailable || !firstSourceAvailable)) {
            return NO_DIFFERENCES;
        }

        Reader first = includeUnavailable && !firstSourceAvailable ? new StringReader("") : getReader(jEditorPane1.getEditorPane().getDocument());
        Reader second = includeUnavailable && !secondSourceAvailable ? new StringReader("") : getReader(jEditorPane2.getEditorPane().getDocument());
        if (first == null || second == null) {
            return NO_DIFFERENCES;
        }

        DiffProvider diff = DiffModuleConfig.getDefault().getDefaultDiffProvider();
        Difference[] diffs;
        try {
            return diff.computeDiff(first, second);
        } catch (IOException e) {
            diffs = NO_DIFFERENCES;
        }
        return diffs;
    }

    /**
     * Runs under a read lock
     * @param doc
     * @return
     */
    private Reader getReader (final Document doc) {
        final Reader[] reader = new Reader[1];
        doc.render(new Runnable() {
            @Override
            public void run() {
                try {
                    reader[0] = new StringReader(doc.getText(0, doc.getLength()));
                } catch (BadLocationException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        });
        return reader[0];
    }
    
    private void repairTextUI (JEditorPane pane) {
        TextUI ui = pane.getUI();
        if (!(ui instanceof BaseTextUI)) {
            // use plain editor
            pane.setEditorKit(CloneableEditorSupport.getEditorKit(CONTENT_TYPE_PLAIN)); //NOI18N
        }
    }

    private void refreshDividerSize() {
        Font font = jSplitPane1.getFont();
        if (font == null) return;
        FontMetrics fm = jSplitPane1.getFontMetrics(jSplitPane1.getFont());
        String maxDiffNumber = Integer.toString(Math.max(1, diffs.length));
        int neededWidth = fm.stringWidth(maxDiffNumber + " /" + maxDiffNumber);
        jSplitPane1.setDividerSize(Math.max(neededWidth, INITIAL_DIVIDER_SIZE));
    }

    synchronized int getDiffSerial() {
        return diffSerial;
    }

    static Difference getFirstDifference(Difference [] diff, int line) {
        if (line < 0) return null;
        for (int i = 0; i < diff.length; i++) {
            Difference difference = diff[i];
            if (line < difference.getFirstStart()) return null;
            if (difference.getType() == Difference.ADD && line == difference.getFirstStart()) return difference;
            if (line <= difference.getFirstEnd()) return difference;
        }
        return null;
    }

    static Difference getSecondDifference(Difference [] diff, int line) {
        if (line < 0) return null;
        for (int i = 0; i < diff.length; i++) {
            Difference difference = diff[i];
            if (line < difference.getSecondStart()) return null;
            if (difference.getType() == Difference.DELETE && line == difference.getSecondStart()) return difference;
            if (line <= difference.getSecondEnd()) return difference;
        }
        return null;
    }
    
    Color getColorLines() {
        return colorLines;
    }

    /**
     * Integration provider for the error stripe.
     */
    private class EditableDiffMarkProvider extends MarkProvider {

        private List<Mark> marks;

        public EditableDiffMarkProvider() {
            marks = getMarksForDifferences();
        }

        @Override
        public List<Mark> getMarks() {
            return marks;
        }

        void refresh() {
            List<Mark> oldMarks = marks;
            marks = getMarksForDifferences();
            firePropertyChange(PROP_MARKS, oldMarks, marks);
        }

        private List<Mark> getMarksForDifferences() {
            if (diffs == null) return Collections.emptyList();
            List<Mark> retMarks = new ArrayList<Mark>(diffs.length);
            for (int i = 0; i < diffs.length; i++) {
                Difference difference = diffs[i];
                retMarks.add(new DiffMark(difference, getColor(difference)));
            }
            return retMarks;
        }
    }
}
