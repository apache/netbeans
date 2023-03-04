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
package org.netbeans.modules.editor.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.*;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.search.ReplacePattern;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeymap;
import org.netbeans.modules.editor.search.SearchPropertiesSupport.SearchProperties;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * This is an implementation of a Firefox(TM) style Incremental Search Side Bar.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class SearchBar extends JPanel implements PropertyChangeListener {
    private static SearchBar searchbarInstance = null;
    private static final Logger LOG = Logger.getLogger(SearchBar.class.getName());
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private static final Color NOT_FOUND = UIManager.getColor("nb.errorForeground");
    private static final Color INVALID_REGEXP = Color.red;
    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars
    private static final Color DEFAULT_FG_COLOR = isCurrentLF("Nimbus") ? UIManager.getColor("text") : UIManager.getColor("textText"); //NOI18N
    private WeakReference<JTextComponent> actualTextComponent;
    private final List<PropertyChangeListener> actualComponentListeners = new LinkedList<>();
    private FocusAdapter focusAdapterForComponent;
    private int regainFocusState; // 0==do not regain; 1==regain; 2==check-on-next-focusGained
    private KeyListener keyListenerForComponent;
    private CaretListener caretListenerForComponent;
    private PropertyChangeListener propertyChangeListenerForComponent;
    private final JLabel findLabel;
    private final JComboBox<String> incSearchComboBox;
    private final JTextComponent incSearchTextField;
    private final DocumentListener incSearchTextFieldListener;
    private boolean hadFocusOnIncSearchTextField = false;
    private final JButton findNextButton;
    private final JButton findPreviousButton;
    private final JButton selectAllButton;
    private final JToggleButton matchCase;
    private final JToggleButton wholeWords;
    private final JToggleButton regexp;
    private final JToggleButton highlight;
    private final JToggleButton wrapAround;
    private final JButton closeButton;
    private final JLabel matches;
    private int numOfMatches = -1;
    private SearchProperties searchProps = SearchPropertiesSupport.getSearchProperties();
    private boolean popupMenuWasCanceled = false;
    private Rectangle actualViewPort;
    private boolean highlightCanceled = false;
    private boolean whenOpenedWasNotVisible = false;
    private boolean lastIncrementalSearchWasSuccessful = true;

    public static SearchBar getInstance() {
        if (searchbarInstance == null) {
            searchbarInstance = new SearchBar();
        }
        return searchbarInstance;
    }

    /*
     * default getInstance
     */
    public static SearchBar getInstance(JTextComponent component) {
        SearchBar searchbarIns = getInstance();
        if (searchbarIns.getActualTextComponent() != component) {
            searchbarIns.setActualTextComponent(component);
        }
        return searchbarIns;
    }

    @SuppressWarnings("unchecked")
    private SearchBar() {
        loadSearchHistory();
        addEscapeKeystrokeFocusBackTo(this);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setFocusCycleRoot(true);
        setForeground(DEFAULT_FG_COLOR); //NOI18N
        setBorder(new SeparatorBorder());

        add(Box.createHorizontalStrut(8)); //spacer in the beginnning of the toolbar

        SearchComboBox<String> scb = new SearchComboBox<>();
        incSearchComboBox = scb;
        incSearchComboBox.setFocusable(false);
        incSearchComboBox.addPopupMenuListener(new SearchPopupMenuListener());
        incSearchTextField = scb.getEditorPane();
        //todo fix no effect
        incSearchTextField.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); //NOI18N
        incSearchTextFieldListener = createIncSearchTextFieldListener(incSearchTextField);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
        addEnterKeystrokeFindNextTo(incSearchTextField);
        addShiftEnterKeystrokeFindPreviousTo(incSearchTextField);
        addAltEnterKeystrokeSelect(incSearchTextField);
        if (getCurrentKeyMapProfile().startsWith("Emacs")) { // NOI18N
            emacsProfileFix(incSearchTextField);
        }
        incSearchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hadFocusOnIncSearchTextField = true;
            }
        });

        findLabel = new JLabel();
        Mnemonics.setLocalizedText(findLabel, NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N
        findLabel.setLabelFor(incSearchTextField);
        add(findLabel);
        add(incSearchComboBox);

        final JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);
        add(leftSeparator);

        findPreviousButton = SearchButton.createButton("org/netbeans/modules/editor/search/resources/find_previous.png", "CTL_FindPrevious"); // NOI18N
        findPreviousButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); //NOI18N
        findPreviousButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
        add(findPreviousButton);
        findNextButton = SearchButton.createButton("org/netbeans/modules/editor/search/resources/find_next.png", "CTL_FindNext"); // NOI18N
        findNextButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); //NOI18N
        findNextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
            }
        });
        add(findNextButton);
        
        selectAllButton = SearchButton.createButton("org/netbeans/modules/editor/search/resources/select_all.png", "CTL_SelectAll"); // NOI18N
        selectAllButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_SelectAllText")); //NOI18N
        selectAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll();
            }
        });
        add(selectAllButton);
        
        final JToolBar.Separator rightSeparator = new JToolBar.Separator();
        rightSeparator.setOrientation(SwingConstants.VERTICAL);
        add(rightSeparator);

        matchCase = SearchButton.createToggleButton("org/netbeans/modules/editor/search/resources/matchCase.png"); //NOI18N
        processToggleButton(matchCase, EditorFindSupport.FIND_MATCH_CASE);
        matchCase.setToolTipText(NbBundle.getMessage(SearchBar.class, "TT_MatchCase")); //NOI18N
        add(matchCase);

        wholeWords = SearchButton.createToggleButton("org/netbeans/modules/editor/search/resources/wholeWord.png"); //NOI18N
        processToggleButton(wholeWords, EditorFindSupport.FIND_WHOLE_WORDS);
        wholeWords.setToolTipText(NbBundle.getMessage(SearchBar.class, "TT_WholeWords")); //NOI18N
        add(wholeWords);

        regexp = SearchButton.createToggleButton("org/netbeans/modules/editor/search/resources/regexp.png"); //NOI18N
        processToggleButton(regexp, EditorFindSupport.FIND_REG_EXP);
        regexp.setToolTipText(NbBundle.getMessage(SearchBar.class, "TT_Regexp")); //NOI18N
        add(regexp);

        highlight = SearchButton.createToggleButton("org/netbeans/modules/editor/search/resources/highlight.png"); //NOI18N
        processToggleButton(highlight, EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
        highlight.setToolTipText(NbBundle.getMessage(SearchBar.class, "TT_Highlight")); //NOI18N
        add(highlight);

        wrapAround = SearchButton.createToggleButton("org/netbeans/modules/editor/search/resources/wrapAround.png" ); //NOI18N
        processToggleButton(wrapAround, EditorFindSupport.FIND_WRAP_SEARCH);
        add(wrapAround);
        wrapAround.setToolTipText(NbBundle.getMessage(SearchBar.class, "TT_WrapAround")); //NOI18N

        selectCheckBoxes();
        addCheckBoxesActions(incSearchTextField);
        EditorFindSupport.getInstance().addPropertyChangeListener(WeakListeners.propertyChange(this, EditorFindSupport.getInstance()));
        
        matches = new JLabel();
        add(Box.createHorizontalGlue());
        add(matches);

        add(Box.createHorizontalStrut(8)); //spacer in the ending of the toolbar

        closeButton = createCloseButton();
        add(closeButton);
        
        setVisible(false);
        usageLogging();
    }

    void addCheckBoxesActions(JTextComponent incSearchTextField) {
        String key = "matchcasekey";
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), key);
        incSearchTextField.getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                matchCase.doClick();
            }
        } );

        key = "wholewordkey";
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK), key);
        incSearchTextField.getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wholeWords.doClick();
            }
        } );

        key = "regexpkey";
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_DOWN_MASK), key);
        incSearchTextField.getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                regexp.doClick();
            }
        } );

        key = "highlightkey";
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK), key);
        incSearchTextField.getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                highlight.doClick();
            }
        } );

        key = "wraparoundkey";
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK), key);
        incSearchTextField.getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                wrapAround.doClick();
            }
        } );
    }

    void notifyFocusLost(PropertyChangeEvent focusLostRegistryEvt) {
        Component origFocusedComponent = (Component) focusLostRegistryEvt.getOldValue();
        Component focusGainingComponent = (Component) focusLostRegistryEvt.getNewValue();
        // If the searchbar (or replacebar) had a focus and the focus is lost to a text component registered
        // in EditorRegistry then attempt to regain the focus later.
        if (origFocusedComponent == incSearchTextField ||
                    origFocusedComponent == ReplaceBar.getInstance(SearchBar.this).getReplaceTextField() ||
                    origFocusedComponent == getActualTextComponent()) // ER reports actual TC when focus in search field
        {
            if (focusGainingComponent == null) { // Must check on focusGained
                regainFocusState = 2;
            } else if (focusGainingComponent instanceof JTextComponent &&
                EditorRegistry.componentList().contains(focusGainingComponent))
            {
                regainFocusState = 1;
            } else {
                regainFocusState = 0;
            }
        }
    }
    
    void checkRegainFocus(PropertyChangeEvent focusGainedRegistryEvt) {
        if (regainFocusState == 2) { // On focusLost the focus gaining component was null
            Component origFocusedComponent = (Component) focusGainedRegistryEvt.getOldValue();
            // Check if the focus came from the actual component of this searchbar
            // (other components like OutlineView for search results or find-usages results mean no focus regain
            if (origFocusedComponent == null || // Can be used when opening a new editor
                    origFocusedComponent == getActualTextComponent() ||
                    origFocusedComponent instanceof JFrame) // This parent is used when switching tabs so it must be allowed here
            {
                regainFocusState = 1;
            } else {
                regainFocusState = 0;
            }
            if (regainFocusState == 1) {
                ReplaceBar replaceBar = ReplaceBar.getInstance(this);
                JTextComponent c;
                if (this.hadFocusOnTextField()) {
                    if (replaceBar.isVisible() && (c = getActualTextComponent()) != null && c.isEditable()) {
                        replaceBar.gainFocus(true);
                    } else {
                        this.gainFocus(true);
                    }
                }
            }
        }
    }
    
    private static class SearchHistoryUtility {

        public static List<EditorFindSupport.SPW> convertFromSearchHistoryToEditorFindSupport(List<SearchPattern> searchPatterns) {
            List<EditorFindSupport.SPW> history = new ArrayList<>();
            for (int i = 0; i < searchPatterns.size(); i++) {
                SearchPattern sptr = searchPatterns.get(i);
                EditorFindSupport.SPW spwrap = new EditorFindSupport.SPW(sptr.getSearchExpression(),
                        sptr.isWholeWords(), sptr.isMatchCase(), sptr.isRegExp());
                history.add(spwrap);
            }
            return history;
        }
        
        public static List<EditorFindSupport.RP> convertFromReplaceHistoryToEditorFindSupport(List<ReplacePattern> replacePatterns) {
            List<EditorFindSupport.RP> history = new ArrayList<>();
            for (int i = 0; i < replacePatterns.size(); i++) {
                ReplacePattern rp = replacePatterns.get(i);
                EditorFindSupport.RP spwrap = new EditorFindSupport.RP(rp.getReplaceExpression(), rp.isPreserveCase());
                history.add(spwrap);
            }
            return history;
        }
    }
    private static PropertyChangeListener searchSelectedPatternListener;
    private static PropertyChangeListener editorHistoryChangeListener;

    private static void loadSearchHistory() {
        searchSelectedPatternListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null || evt.getPropertyName() == null) {
                    return;
                }
                switch(evt.getPropertyName()) {
                    case SearchHistory.ADD_TO_HISTORY:
                        EditorFindSupport.getInstance().setHistory(
                            SearchHistoryUtility.convertFromSearchHistoryToEditorFindSupport(SearchHistory.getDefault().getSearchPatterns()));
                        break;
                    case SearchHistory.ADD_TO_REPLACE:
                        EditorFindSupport.getInstance().setReplaceHistory(
                            SearchHistoryUtility.convertFromReplaceHistoryToEditorFindSupport(SearchHistory.getDefault().getReplacePatterns()));
                        break;
                        
                }
            }
        };

        editorHistoryChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null || evt.getPropertyName() == null) {
                    return;
                }
                switch (evt.getPropertyName()) {
                    case EditorFindSupport.FIND_HISTORY_PROP:
                        EditorFindSupport.SPW spw = (EditorFindSupport.SPW) evt.getNewValue();
                        if (spw == null || spw.getSearchExpression() == null || "".equals(spw.getSearchExpression())) { //NOI18N
                            return;
                        }
                        SearchPattern sp = SearchPattern.create(spw.getSearchExpression(),
                                spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp());
                        SearchHistory.getDefault().add(sp);
                        if (!SearchBar.getInstance().incSearchTextField.getText().equals(sp.getSearchExpression())) {
                            SearchBar.getInstance().incSearchTextField.setText(sp.getSearchExpression());
                        }
                        break;
                    case EditorFindSupport.FIND_HISTORY_CHANGED_PROP:
                        EditorFindSupport.getInstance().setHistory(
                                SearchHistoryUtility.convertFromSearchHistoryToEditorFindSupport(SearchHistory.getDefault().getSearchPatterns()));
                        break;
                    case EditorFindSupport.REPLACE_HISTORY_PROP:
                        EditorFindSupport.RP rp = (EditorFindSupport.RP) evt.getNewValue();
                        if (rp == null || rp.getReplaceExpression() == null || "".equals(rp.getReplaceExpression())) { //NOI18N
                            return;
                        }
                        ReplacePattern replacePattern = ReplacePattern.create(rp.getReplaceExpression(), rp.isPreserveCase());
                        SearchHistory.getDefault().addReplace(replacePattern);
                        break;
                    case EditorFindSupport.REPLACE_HISTORY_CHANGED_PROP:
                        EditorFindSupport.getInstance().setReplaceHistory(
                                SearchHistoryUtility.convertFromReplaceHistoryToEditorFindSupport(SearchHistory.getDefault().getReplacePatterns()));
                        break;
                }
            }
        };

        SearchHistory.getDefault().addPropertyChangeListener(searchSelectedPatternListener);
        EditorFindSupport.getInstance().addPropertyChangeListener(editorHistoryChangeListener);
    }

    private static void usageLogging() {
        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.editor"); // NOI18N
        LogRecord rec = new LogRecord(Level.INFO, "USG_SEARCH_TYPE"); // NOI18N
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        rec.setParameters(new Object[] {prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default")}); // NOI18N
        rec.setLoggerName(logger.getName());
        logger.log(rec);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt != null) {
            selectCheckBoxes();
        }
    }

    void updateIncSearchComboBoxHistory(String incrementalSearchText) {
        EditorFindSupport.getInstance().addToHistory(new EditorFindSupport.SPW(incrementalSearchText,
                wholeWords.isSelected(), matchCase.isSelected(), regexp.isSelected()));
        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
        // Add the text to the top of the list
        for (int i = incSearchComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = incSearchComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                incSearchComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel<String>) incSearchComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        incSearchComboBox.setSelectedIndex(0);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
    }

    private KeyListener createKeyListenerForComponent() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if(looseFocus()) {
                        e.consume();
                    };
                    ReplaceBar replaceBarInstance = ReplaceBar.getInstance(SearchBar.this);
                    if (replaceBarInstance.isVisible()) {
                        replaceBarInstance.looseFocus();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
    }
    
    private CaretListener createCaretListenerForComponent() {
        return new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                if (SearchBar.getInstance().isVisible()) {
                    int num = SearchBar.getInstance().getNumOfMatches();
                    SearchBar.getInstance().showNumberOfMatches(null, num);
                }
            }
        };
    }

    private FocusAdapter createFocusAdapterForComponent() {
        return new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getOppositeComponent() instanceof JRootPane) {
                    // Hack for linux where invoking Find from main menu caused focus gained on editor
                    // even when openning quick search
                    return;
                }
                hadFocusOnIncSearchTextField = false;
                if (isClosingSearchType() && !ReplaceBar.getInstance(SearchBar.getInstance()).isVisible()) {
                    looseFocus();
                }
            }
        };
    }

    private PropertyChangeListener createPropertyChangeListenerForComponent() {
        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null || !"keymap".equals(evt.getPropertyName())) { // NOI18N
                    return;
                }
                JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                if (lastFocusedComponent == null) {
                    return;
                }
                Keymap keymap = lastFocusedComponent.getKeymap();

                if (keymap instanceof MultiKeymap) {
                    MultiKeymap multiKeymap = (MultiKeymap) keymap;

                    Action[] actions = lastFocusedComponent.getActions();
                    for (Action action : actions) { 
                        String actionName = (String) action.getValue(Action.NAME);
                        if (actionName == null) {
                            LOG.log(Level.WARNING, "SearchBar: Null Action.NAME property of action: {0}\n", action); //NOI18N
                        } else if (actionName.equals(BaseKit.findNextAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findNext();
                                        }
                                    });
                        } else if (actionName.equals(BaseKit.findPreviousAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findPrevious();
                                        }
                                    });
                        }
                    }
                }
            }

            private void keystrokeForSearchAction(MultiKeymap multiKeymap, Action searchAction, AbstractAction newSearchAction) {
                KeyStroke[] keyStrokes = multiKeymap.getKeyStrokesForAction(searchAction);
                if (keyStrokes != null) {
                    InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    for (KeyStroke ks : keyStrokes) {
                        LOG.log(Level.FINE, "found {1} search action, {0}", new Object[]{ks, searchAction.getValue(Action.NAME)}); //NOI18N
                        inputMap.put(ks, (String) searchAction.getValue(Action.NAME));
                    }
                    getActionMap().put((String) searchAction.getValue(Action.NAME), newSearchAction);
                }
            }
        };
        pcl.propertyChange(new PropertyChangeEvent(this, "keymap", null, null)); //NOI18N
        return pcl;
    }

     private void addAltEnterKeystrokeSelect(JTextComponent incSearchTextField) {
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.ALT_MASK, true),
                "select-all"); // NOI18N
        incSearchTextField.getActionMap().put("select-all", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll();
            }
        });
    }
     
    private void addShiftEnterKeystrokeFindPreviousTo(JTextComponent incSearchTextField) {
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
                "incremental-find-previous"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-previous", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
    }

    private void addEnterKeystrokeFindNextTo(JTextComponent incSearchTextField) {
        incSearchTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                "incremental-find-next"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-next", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
                if (isClosingSearchType() && !ReplaceBar.getInstance(SearchBar.getInstance()).isVisible()) {
                    looseFocus();
                }
            }
        });
    }


    private DocumentListener createIncSearchTextFieldListener(final JTextComponent incSearchTextField) {
        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                incrementalSearch();
            }
        });
        searchDelayTimer.setRepeats(false);

        // listen on text change
        return new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() > 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                }
                searchDelayTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() <= 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                }
                searchDelayTimer.restart();
            }
        };
    }

    private JButton createCloseButton() {
        JButton button = CloseButtonFactory.createBigCloseButton();
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
        button.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_CloseIncrementalSearchSidebar")); // NOI18N
        return button;
    }
    
    void processToggleButton(JToggleButton button, final String findConstant) {
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                incrementalSearch();
            }
        });
    }

    // TODO: remove after replace icons
    JCheckBox createCheckBox(String resName, final String findConstant) {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        Mnemonics.setLocalizedText(checkBox, NbBundle.getMessage(SearchBar.class, resName));
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                incrementalSearch();
            }
        });
        checkBox.setMargin(BUTTON_INSETS);
        checkBox.setFocusable(false);
        return checkBox;
    }

    private void selectCheckBoxes() {
        wholeWords.setSelected(getFindSupportValue(EditorFindSupport.FIND_WHOLE_WORDS));
        wholeWords.setEnabled(!getRegExp());
        matchCase.setSelected(getFindSupportValue(EditorFindSupport.FIND_MATCH_CASE));
        regexp.setSelected(getRegExp());
        highlight.setSelected(getFindSupportValue(EditorFindSupport.FIND_HIGHLIGHT_SEARCH));
        wrapAround.setSelected(getFindSupportValue(EditorFindSupport.FIND_WRAP_SEARCH));
    }

    // Treat Emacs profile specially in order to fix #191895
    private void emacsProfileFix(final JTextComponent incSearchTextField) {
        class JumpOutOfSearchAction extends AbstractAction {

            private final String actionName;

            public JumpOutOfSearchAction(String n) {
                actionName = n;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
                if (getActualTextComponent() != null) {
                    ActionEvent ev = new ActionEvent(getActualTextComponent(), e.getID(), e.getActionCommand(), e.getModifiers());
                    Action action = getActualTextComponent().getActionMap().get(actionName);
                    action.actionPerformed(ev);
                }
            }
        }
        String actionName = "caret-begin-line"; // NOI18N
        Action a1 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a1);
        actionName = "caret-end-line"; // NOI18N
        Action a2 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a2);

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, InputEvent.CTRL_MASK, false), "caret-up-alt"); // NOI18N
        actionName = "caret-up"; // NOI18N
        Action a3 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-up-alt", a3); // NOI18N

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK, false), "caret-down-alt"); // NOI18N
        actionName = "caret-down"; // NOI18N
        Action a4 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-down-alt", a4); // NOI18N
    }
    // From org.netbeans.modules.editor.settings.storage.EditorSettingsImpl
    private static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N
    private static final String KEYMAPS_FOLDER = "Keymaps"; // NOI18N

    /*
     * This method is verbatim copy from class
     * org.netbeans.modules.editor.settings.storage.EditorSettingsImpl bacause
     * we don't want to introduce the dependency between this module and Editor
     * Setting Storage module.
     */
    public static String getCurrentKeyMapProfile() {
        String currentKeyMapProfile = null;
        FileObject fo = FileUtil.getConfigFile(KEYMAPS_FOLDER);
        if (fo != null) {
            Object o = fo.getAttribute(FATTR_CURRENT_KEYMAP_PROFILE);
            if (o instanceof String) {
                currentKeyMapProfile = (String) o;
            }
        }
        if (currentKeyMapProfile == null) {
            currentKeyMapProfile = DEFAULT_PROFILE;
        }
        return currentKeyMapProfile;
    }

    @Override
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorSearchBar"; // NOI18N
    }

    void addEscapeKeystrokeFocusBackTo(JComponent component) {
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                "loose-focus"); // NOI18N
        component.getActionMap().put("loose-focus", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!popupMenuWasCanceled) {
                    looseFocus();
                    if (isClosingSearchType()) {
                        getActualTextComponent().scrollRectToVisible(actualViewPort);
                    }
                } else {
                    popupMenuWasCanceled = false;
                }
            }
        });
    }

    private static boolean isClosingSearchType() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default").equals("closing"); // NOI18N
    }

    public void gainFocus(boolean persistanceStatus) {
        String lastSearch = "";
        if (!isClosingSearchType()) {
            lastSearch = incSearchTextField.getText();
            Object findWhat = searchProps.getProperty(EditorFindSupport.FIND_WHAT);
            if (findWhat instanceof String) {
                lastSearch = (String) findWhat;
            }
        }
        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
        SearchComboBoxEditor.changeToOneLineEditorPane((JEditorPane) incSearchTextField);
        addEnterKeystrokeFindNextTo(incSearchTextField);

        MutableComboBoxModel<String> comboBoxModelIncSearch = ((MutableComboBoxModel<String>) incSearchComboBox.getModel());
        for (int i = comboBoxModelIncSearch.getSize() - 1; i >= 0; i--) {
            comboBoxModelIncSearch.removeElementAt(i);
        }
        for (EditorFindSupport.SPW spw : EditorFindSupport.getInstance().getHistory()) {
            comboBoxModelIncSearch.addElement(spw.getSearchExpression());
        }
        if (!isClosingSearchType()) {
            incSearchTextField.setText(lastSearch);
        }
        if (!isVisible() && isClosingSearchType()) {
            whenOpenedWasNotVisible = true;
        }
        if (whenOpenedWasNotVisible) {
            incSearchTextField.setText("");
            whenOpenedWasNotVisible = false;
        }
        hadFocusOnIncSearchTextField = true;
        setVisible(true);
        initBlockSearch(persistanceStatus);
        EditorFindSupport.getInstance().setFocusedTextComponent(getActualTextComponent());

        incSearchTextField.requestFocusInWindow();

        boolean empty = incSearchTextField.getText().isEmpty();
        if (!empty) { // preselect the text in incremental search text field
            incSearchTextField.selectAll();
        }
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceButton().setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceAllButton().setEnabled(!empty);

        actualViewPort = getActualTextComponent().getVisibleRect();
        if (!isClosingSearchType() && highlightCanceled) {
            searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
            highlightCanceled = false;
        }
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
    }

    public boolean looseFocus() {
        hadFocusOnIncSearchTextField = false;
        if (!isVisible()) {
            return false;
        }
        EditorFindSupport.getInstance().setBlockSearchHighlight(0, 0);
        EditorFindSupport.getInstance().incSearchReset();
        EditorFindSupport.getInstance().setFocusedTextComponent(null);
        if (getActualTextComponent() != null) {
            org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "");
            getActualTextComponent().requestFocusInWindow();
        }
        setVisible(false);
        if (!isClosingSearchType() && getFindSupportValue(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
            searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.FALSE);
            highlightCanceled = true;
        }
        searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
        searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
        searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
        EditorFindSupport.getInstance().putFindProperties(searchProps.getProperties());
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                searchProps.saveToPrefs();
            }
        });
        return true;
    }

    private void incrementalSearch() {
        if (getActualTextComponent() == null || getActualTextComponent().getCaret() == null) {
            return;
        }
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;

        // Enable/disable the pre/next buttons
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceButton().setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceAllButton().setEnabled(!empty);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        findSupport.putFindProperties(getSearchProperties());

        // search starting at current caret position
        int caretPosition = getActualTextComponent().getSelectionStart();
        if (isClosingSearchType()) {
            caretPosition = getActualTextComponent().getCaretPosition();
        }
        if (findSupport.incSearch(searchProps.getProperties(), caretPosition) || empty) {
            // text found - reset incremental search text field's foreground
            incSearchTextField.setForeground(DEFAULT_FG_COLOR); //NOI18N
            org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "", StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND);
            showNumberOfMatches(findSupport, -1);
            lastIncrementalSearchWasSuccessful = true;
        } else {
                // text not found - indicate error in incremental search
            // text field with red foreground
            incSearchTextField.setForeground(NOT_FOUND);
            org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), NbBundle.getMessage(
                    SearchBar.class, "find-not-found", incrementalSearchText),
                    StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND); //NOI18N
            if (lastIncrementalSearchWasSuccessful) {
                Toolkit.getDefaultToolkit().beep();
                lastIncrementalSearchWasSuccessful = false;
            }
            showNumberOfMatches(findSupport, 0);
        }
    }

    void findNext() {
        find(true);
    }

    void findPrevious() {
        find(false);
    }
    
    void selectAll() {
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        JTextComponent textComponent = getActualTextComponent();
        Document doc = textComponent.getDocument();
        Caret caret = textComponent.getCaret();
        if(caret instanceof EditorCaret) {
            EditorCaret editorCaret = (EditorCaret) caret;
            try {
                int[] blocks = findSupport.getBlocks(new int[]{-1, -1}, doc, 0, doc.getLength());
                if(blocks[0] >= 0 && blocks.length % 2 == 0) {
                    List<Position> newCarets = new ArrayList<>(editorCaret.getCarets().size() << 1);
                    for (int i = 0; i < blocks.length; i += 2) {
                        int start = blocks[i];
                        int end = blocks[i+1];
                        if(start == -1 || end == -1) {
                            break;
                        }
                        Position startPos = doc.createPosition(start);
                        Position endPos = doc.createPosition(end);
                        newCarets.add(endPos);
                        newCarets.add(startPos);
                    }
                    
                    editorCaret.replaceCarets(newCarets, null); // TODO handle biases
                    
                    textComponent.requestFocusInWindow();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public int getNumOfMatches() {
        return numOfMatches;
    }

    /**
     * @param findSupport if null EditorFindSupport.getInstance() is used
     * @param numMatches if numOfMatches < 0, calculate numOfMatches and position, else show numOfMatches.
     * @return 
     */
    
    public int lastCurrentPosStart = -1;
    public int lastCurrentPosEnd = -1;
    public int showNumberOfMatches(EditorFindSupport findSupport, int numMatches) {
        if (findSupport == null) {
            findSupport = EditorFindSupport.getInstance();
        }
        int pos = 0;
        int currentposStart = getActualTextComponent().getSelectionStart();
        int currentposEnd = getActualTextComponent().getSelectionEnd();
        if (numMatches < 0 || (lastCurrentPosStart == currentposStart && lastCurrentPosEnd == currentposEnd)) {
            numMatches = -1;
            boolean notFound = true;
            try {
                int[] blocks = findSupport.getBlocks(new int[]{-1, -1}, getActualTextComponent().getDocument(), 0, getActualTextComponent().getDocument().getLength());
                for (int i = 0; i < blocks.length; i++) {
                    if (blocks[i] > 0) {
                        numMatches++;
                        if (blocks[i] < currentposStart) {
                            pos++;
                        }
                        if (blocks[i] == currentposStart && i+1 < blocks.length && blocks[i+1] == currentposEnd) {
                            notFound = false;
                        }
                    } else if (blocks[i] == 0 && i + 1 < blocks.length && blocks[i+1] > 0) {
                        numMatches++;
                    }
                }
          
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            numMatches = numMatches == 0 ? 0 : (numMatches + 1) / 2;
            pos = (pos == 0 ? 0 : (pos + 1) / 2) + 1;
            if (pos > numMatches) {
                pos = 0;
            }
            if (notFound) {
                pos = 0;
            }
        }
        lastCurrentPosStart = currentposStart;
        lastCurrentPosEnd = currentposEnd;
        if (incSearchTextField.getText().isEmpty()) {
            Mnemonics.setLocalizedText(matches, ""); //NOI18N
        } else if (numMatches == 0) {
            Mnemonics.setLocalizedText(matches, NbBundle.getMessage(SearchBar.class, "0_matches")); //NOI18N
        } else if (numMatches == 1) {
            Mnemonics.setLocalizedText(matches, NbBundle.getMessage(SearchBar.class, "1_matches")); //NOI18N
        } else {
            if (pos == 0) {
                Mnemonics.setLocalizedText(matches, NbBundle.getMessage(SearchBar.class, "n_matches", numMatches)); //NOI18N
            } else {
                Mnemonics.setLocalizedText(matches, NbBundle.getMessage(SearchBar.class, "i_n_matches", pos, numMatches)); //NOI18N
            }
        }
        this.numOfMatches  = numMatches;
        return numMatches;
    }

    private void find(boolean next) {
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;
        updateIncSearchComboBoxHistory(incrementalSearchText);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        Map<String, Object> actualfindProps = getSearchProperties();
        findSupport.putFindProperties(actualfindProps);

        if (findSupport.find(actualfindProps, !next) || empty) {
            // text found - reset incremental search text field's foreground
            incSearchTextField.setForeground(DEFAULT_FG_COLOR); //NOI18N
            showNumberOfMatches(findSupport, -1);
        } else {
            // text not found - indicate error in incremental search text field with red foreground
            incSearchTextField.setForeground(NOT_FOUND);
            showNumberOfMatches(findSupport, 0);
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @SuppressWarnings("unchecked")
    void initBlockSearch(boolean persistanceStatus) {
        JTextComponent c = getActualTextComponent();
        String selText;
        int startSelection;
        int endSelection;
        boolean blockSearchVisible = false;

        if (c != null) {
            startSelection = c.getSelectionStart();
            endSelection = c.getSelectionEnd();

            Document doc = c.getDocument();
            if (doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument) doc;
                try {
                    int startLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, startSelection);
                    int endLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, endSelection);
                    if (endLine > startLine) {
                        blockSearchVisible = true;
                    }
                } catch (BadLocationException ble) {
                }
            }

            // caretPosition = bwdSearch.isSelected() ? c.getSelectionEnd() : c.getSelectionStart();

            if (!blockSearchVisible && !persistanceStatus) {
                selText = c.getSelectedText();
                if (selText != null && selText.length() > 0) {
                    int n = selText.indexOf('\n');
                    if (n >= 0) {
                        selText = selText.substring(0, n);
                    }
                    incSearchTextField.setText(selText);
                    searchProps.setProperty(EditorFindSupport.FIND_WHAT, selText);
                } else {
                    if (isClosingSearchType()) {
                        String findWhat = (String) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_WHAT);
                        if (findWhat != null && findWhat.length() > 0) {
                            incSearchTextField.setText(findWhat);
                        }
                    }

                }
            }

            int blockSearchStartOffset = blockSearchVisible ? startSelection : 0;
            int blockSearchEndOffset = blockSearchVisible ? endSelection : 0;

            try {
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH, blockSearchVisible);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_START, doc.createPosition(blockSearchStartOffset));
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_END, doc.createPosition(blockSearchEndOffset));
                EditorFindSupport.getInstance().setBlockSearchHighlight(blockSearchStartOffset, blockSearchEndOffset);
            } catch (BadLocationException ble) {
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
            }

            EditorFindSupport.getInstance().putFindProperties(searchProps.getProperties());
            showNumberOfMatches(null, -1);
        }
    }

    boolean getFindSupportValue(String findConstant) {
        Boolean b = (Boolean) searchProps.getProperty(findConstant);
        return b != null ? b.booleanValue() : false;
    }

    private void switchFindSupportValue(String findConstant) {
        searchProps.setProperty(findConstant, !getFindSupportValue(findConstant));
    }

    boolean getRegExp() {
        return getFindSupportValue(EditorFindSupport.FIND_REG_EXP);
    }

    boolean hadFocusOnTextField() {
        return hadFocusOnIncSearchTextField;
    }

    void lostFocusOnTextField() {
        hadFocusOnIncSearchTextField = false;
    }

    void setActualTextComponent(JTextComponent component) {
        if (getActualTextComponent() != null) {
            getActualTextComponent().removeFocusListener(focusAdapterForComponent);
            getActualTextComponent().removePropertyChangeListener(propertyChangeListenerForComponent);
            getActualTextComponent().removeKeyListener(keyListenerForComponent);
            getActualTextComponent().removeCaretListener(caretListenerForComponent);
        }
        if (focusAdapterForComponent == null) {
            focusAdapterForComponent = createFocusAdapterForComponent();
        }
        if (propertyChangeListenerForComponent == null) {
            propertyChangeListenerForComponent = createPropertyChangeListenerForComponent();
        }
        if (keyListenerForComponent == null) {
            keyListenerForComponent = createKeyListenerForComponent();
        }
        if (caretListenerForComponent == null) {
            caretListenerForComponent = createCaretListenerForComponent();
        }
        component.addCaretListener(caretListenerForComponent);
        component.addFocusListener(focusAdapterForComponent);
        component.addPropertyChangeListener(propertyChangeListenerForComponent);
        component.addKeyListener(keyListenerForComponent);
        for (PropertyChangeListener pcl : actualComponentListeners) {
            pcl.propertyChange(new PropertyChangeEvent(this, "actualTextComponent", getActualTextComponent(), component)); //NOI18N
        }
        actualTextComponent = new WeakReference<>(component);
        EditorFindSupport.getInstance().setFocusedTextComponent(getActualTextComponent());
        //This is for component without highlighting
        if (Boolean.TRUE.equals(component.getClientProperty("searchbar.hideHighlightIcon"))) { //NOI18N
            highlight.setVisible(false);
        } else if (!highlight.isVisible()) {
            highlight.setVisible(true);
        }
    }

    void addActualComponentListener(PropertyChangeListener propertyChangeListener) {
        actualComponentListeners.add(propertyChangeListener);
    }

    public JTextComponent getActualTextComponent() {
        return actualTextComponent != null ? actualTextComponent.get() : null;
    }

    JTextComponent getIncSearchTextField() {
        return incSearchTextField;
    }

    JButton getCloseButton() {
        return closeButton;
    }

    JLabel getFindLabel() {
        return findLabel;
    }

    JButton getFindNextButton() {
        return findNextButton;
    }

    JButton getFindPreviousButton() {
        return findPreviousButton;
    }

    public Map<String, Object> getSearchProperties() {
        searchProps.setProperty(EditorFindSupport.FIND_WHAT, incSearchTextField.getText());
        searchProps.setProperty(EditorFindSupport.FIND_MATCH_CASE, matchCase.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_WHOLE_WORDS, wholeWords.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_REG_EXP, regexp.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        searchProps.setProperty(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, highlight.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_WRAP_SEARCH, wrapAround.isSelected());
        return searchProps.getProperties();
    }

    public void setSearchProperties(SearchProperties searchProperties) {
        searchProps = searchProperties;
        selectCheckBoxes();
    }

    public boolean isPopupMenuWasCanceled() {
        return popupMenuWasCanceled;
    }

    public void setPopupMenuWasCanceled(boolean popupMenuWasCanceled) {
        this.popupMenuWasCanceled = popupMenuWasCanceled;
    }

    public JComboBox<String> getIncSearchComboBox() {
        return incSearchComboBox;
    }

    private class SearchPopupMenuListener implements PopupMenuListener {
        private boolean canceled = false;

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (!canceled) {
                Object selectedItem = getIncSearchComboBox().getModel().getSelectedItem();
                if (selectedItem instanceof String) {
                    String findWhat = (String) selectedItem;
                    for (EditorFindSupport.SPW spw : EditorFindSupport.getInstance().getHistory()) {
                        if (findWhat.equals(spw.getSearchExpression())) {
                            searchProps.setProperty(EditorFindSupport.FIND_REG_EXP, spw.isRegExp());
                            break;
                        }
                    }
                }
            } else {
                canceled = false;
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            canceled = true;
            SearchBar.getInstance().setPopupMenuWasCanceled(true);
        }
    };

    private static final class SeparatorBorder implements Border {
        private static final int BORDER_WIDTH = 1;
        private final Insets INSETS = new Insets(BORDER_WIDTH, 0, 0, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color originalColor = g.getColor();
            g.setColor (UIManager.getColor ("controlShadow")); //NOI18N
            g.drawLine(0, 0, c.getWidth(), 0);
            g.setColor(originalColor);
        }

        @Override public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        @Override public boolean isBorderOpaque() {
            return true;
        }
    }
    
    private static boolean isCurrentLF(String lf) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        String lfID = laf.getID();
        return lf.equals(lfID);
    }
}
