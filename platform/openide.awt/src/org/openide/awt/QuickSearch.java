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
package org.openide.awt;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 * Quick search infrastructure for an arbitrary component.
 * When quick search is attached to a component, it listens on key events going
 * to the component and displays a quick search field.
 * 
 * @author Martin Entlicher
 * @since 7.43
 */
public class QuickSearch {
    
    @StaticResource
    private static final String ICON_FIND = "org/openide/awt/resources/quicksearch/find.png";      // NOI18N
    @StaticResource
    private static final String ICON_FIND_WITH_MENU = "org/openide/awt/resources/quicksearch/findMenu.png"; // NOI18N
    private static final Object CLIENT_PROPERTY_KEY = new Object();
    
    private final JComponent component;
    private final Object constraints;
    private final Callback callback;
    private final JMenu popupMenu;
    private final boolean asynchronous;
    private boolean enabled = true;
    private SearchTextField searchTextField;
    private KeyAdapter quickSearchKeyAdapter;
    private SearchFieldListener searchFieldListener;
    private JPanel searchPanel;
    private final RequestProcessor rp;
    private static enum QS_FIRE { UPDATE, NEXT, MAX }
    private AnimationTimer animationTimer;
    private boolean alwaysShown = false;
    private volatile boolean hasSearchText = false;
    
    private QuickSearch(JComponent component, Object constraints,
                        Callback callback, boolean asynchronous, JMenu popupMenu) {
        this.component = component;
        this.constraints = constraints;
        this.callback = callback;
        this.asynchronous = asynchronous;
        this.popupMenu = popupMenu;
        if (asynchronous) {
            rp = new RequestProcessor(QuickSearch.class);
        } else {
            rp = null;
        }
        setUpSearch();
    }
    
    /**
     * Attach quick search to a component with given constraints.
     * It listens on key events going to the component and displays a quick search
     * field.
     * 
     * @param component The component to attach to
     * @param constraints The constraints that are used to add the search field
     * to the component. It's passed to {@link JComponent#add(java.awt.Component, java.lang.Object)}
     * when adding the quick search UI to the component.
     * @param callback The call back implementation, which is notified from the
     * quick search field submissions.
     * @return An instance of QuickSearch class.
     */
    public static QuickSearch attach(JComponent component, Object constraints,
                                     Callback callback) {
        return attach(component, constraints, callback, false, null);
    }
    
    /**
     * Attach quick search to a component with given constraints.
     * It listens on key events going to the component and displays a quick search
     * field.
     * 
     * @param component The component to attach to
     * @param constraints The constraints that are used to add the search field
     * to the component. It's passed to {@link JComponent#add(java.awt.Component, java.lang.Object)}
     * when adding the quick search UI to the component.
     * @param callback The call back implementation, which is notified from the
     * quick search field submissions.
     * @return An instance of QuickSearch class.
     */
    public static QuickSearch attach(JComponent component, Object constraints,
                                     Callback callback, boolean asynchronous) {
        return attach(component, constraints, callback, asynchronous, null);
    }
    
    /**
     * Attach quick search to a component with given constraints.
     * It listens on key events going to the component and displays a quick search
     * field.
     * 
     * @param component The component to attach to
     * @param constraints The constraints that are used to add the search field
     * to the component. It's passed to {@link JComponent#add(java.awt.Component, java.lang.Object)}
     * when adding the quick search UI to the component.
     * @param callback The call back implementation, which is notified from the
     * quick search field submissions.
     * @param popupMenu A pop-up menu, that is displayed on the find icon, next to the search
     * field. This allows customization of the search criteria. The pop-up menu
     * is taken from {@link JMenu#getPopupMenu()}.
     * @return An instance of QuickSearch class.
     */
    public static QuickSearch attach(JComponent component, Object constraints,
                                     Callback callback, JMenu popupMenu) {
        return attach(component, constraints, callback, false, popupMenu);
    }
    /**
     * Attach quick search to a component with given constraints.
     * It listens on key events going to the component and displays a quick search
     * field.
     * 
     * @param component The component to attach to
     * @param constraints The constraints that are used to add the search field
     * to the component. It's passed to {@link JComponent#add(java.awt.Component, java.lang.Object)}
     * when adding the quick search UI to the component.
     * @param callback The call back implementation, which is notified from the
     * quick search field submissions.
     * @param asynchronous Set whether the quick search notifies the call back
     * asynchronously, or not.
     * By default, Callback is notified synchronously on EQ thread.
     * If <code>true</code>, three notification methods are called asynchronously
     * on a background thread. These are
     * {@link Callback#quickSearchUpdate(java.lang.String)},
     * {@link Callback#showNextSelection(javax.swing.text.Position.Bias)},
     * {@link Callback#findMaxPrefix(java.lang.String)}.
     * If <code>false</code> all methods are called synchronously on EQ thread.
     * @param popupMenu A pop-up menu, that is displayed on the find icon, next to the search
     * field. This allows customization of the search criteria. The pop-up menu
     * is taken from {@link JMenu#getPopupMenu()}.
     * @return An instance of QuickSearch class.
     */
    public static QuickSearch attach(JComponent component, Object constraints,
                                     Callback callback, boolean asynchronous, JMenu popupMenu) {
        Object qso = component.getClientProperty(CLIENT_PROPERTY_KEY);
        if (qso instanceof QuickSearch) {
            throw new IllegalStateException("A quick search is attached to this component already, detach it first."); // NOI18N
        } else {
            QuickSearch qs = new QuickSearch(component, constraints, callback, asynchronous, popupMenu);
            component.putClientProperty(CLIENT_PROPERTY_KEY, qs);
            return qs;
        }
    }
    
    /**
     * Detach the quick search from the component it was attached to.
     */
    public void detach() {
        setEnabled(false);
        component.putClientProperty(CLIENT_PROPERTY_KEY, null);
    }

    /**
     * Test whether the quick search field is always shown. 
     * This is <code>false</code> by default.
     * @return <code>true</code> when the search field is always shown,
     *                           <code>false</code> otherwise.
     * @since 7.49
     */
    public boolean isAlwaysShown() {
        return alwaysShown;
    }

    /**
     * Set whether the quick search field should always be shown.
     * @param alwaysShown <code>true</code> to always show the search field,
     *                           <code>false</code> otherwise.
     * @since 7.49
     */
    public void setAlwaysShown(boolean alwaysShown) {
        this.alwaysShown = alwaysShown;
        if(alwaysShown) {
            displaySearchField();
        } else {
            removeSearchField();
        }
    }
    
    /**
     * Test whether the quick search is enabled. This is <code>true</code>
     * by default.
     * @return <code>true</code> when the quick search is enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the enabled state of the quick search.
     * This allows to activate/deactivate the quick search functionality.
     * @param enabled <code>true</code> to enable the quick search,
     *                <code>false</code> otherwise.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return ;
        }
        this.enabled = enabled;
        if (enabled) {
            component.addKeyListener(quickSearchKeyAdapter);
        } else {
            removeSearchField();
            component.removeKeyListener(quickSearchKeyAdapter);
        }
    }
    
    /**
     * Process this key event in addition to the key events obtained from the
     * component we're attached to.
     * @param ke a key event to process.
     */
    public void processKeyEvent(KeyEvent ke) {
        if (!isEnabled()) {
            return ;
        }
        if (searchPanel != null) {
            if (!isKeyEventInSearchFieldIgnored(ke)) {
                searchTextField.setCaretPosition(searchTextField.getText().length());
                searchTextField.processKeyEvent(ke);
            }
        } else {
            switch(ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    quickSearchKeyAdapter.keyPressed(ke);
                    break;
                case KeyEvent.KEY_RELEASED:
                    quickSearchKeyAdapter.keyReleased(ke);
                    break;
                case KeyEvent.KEY_TYPED:
                    quickSearchKeyAdapter.keyTyped(ke);
                    break;
            }
        }
    }
    
    private boolean isKeyEventInSearchFieldIgnored(KeyEvent ke) {
        // Ignore DELETE key events unless the search field has focus
        if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
            return !searchTextField.isFocusOwner();
        } else {
            return false;
        }
    }

    private void fireQuickSearchUpdate(String searchText) {
        if (asynchronous) {
            rp.post(new LazyFire(QS_FIRE.UPDATE, searchText));
        } else {
            callback.quickSearchUpdate(searchText);
        }
        hasSearchText = true;
    }
    
    private void fireShowNextSelection(boolean forward) {
        if (asynchronous) {
            rp.post(new LazyFire(QS_FIRE.NEXT, forward));
        } else {
            callback.showNextSelection(forward);
        }
    }
    
    private void findMaxPrefix(String prefix, Consumer<String> newPrefixSetter) {
        if (asynchronous) {
            rp.post(new LazyFire(QS_FIRE.MAX, prefix, newPrefixSetter));
        } else {
            prefix = callback.findMaxPrefix(prefix);
            newPrefixSetter.accept(prefix);
        }
    }
    
    private void setUpSearch() {
        searchTextField = new SearchTextField();
        // create new key listeners
        quickSearchKeyAdapter = (
            new KeyAdapter() {
            @Override
                public void keyTyped(KeyEvent e) {
                    int modifiers = e.getModifiers();
                    int keyCode = e.getKeyCode();
                    char c = e.getKeyChar();

                    //#43617 - don't eat + and -
                    //#98634 - and all its duplicates dont't react to space
                    if ((c == '+') || (c == '-') || (c==' ')) return; // NOI18N

                    if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                        return;
                    }

                    if (Character.isISOControl(c) ||
                            (keyCode == KeyEvent.VK_SHIFT) ||
                            (keyCode == KeyEvent.VK_ESCAPE)) return;

                    displaySearchField();
                    
                    final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                    searchTextField.setText(String.valueOf(stroke.getKeyChar()));

                    e.consume();
                }
            }
        );
        if (isEnabled()) {
            component.addKeyListener(quickSearchKeyAdapter);
        }
        // Create a the "multi-event" listener for the text field. Instead of
        // adding separate instances of each needed listener, we're using a
        // class which implements them all. This approach is used in order 
        // to avoid the creation of 4 instances which takes some time
        searchFieldListener = new SearchFieldListener();
        searchTextField.addKeyListener(searchFieldListener);
        searchTextField.addFocusListener(searchFieldListener);
        Document searchDoc = searchTextField.getDocument();
        searchDoc.addDocumentListener(searchFieldListener);
        if (searchDoc instanceof AbstractDocument) {
            ((AbstractDocument) searchDoc).setDocumentFilter(searchFieldListener.new ReplaceFilter());
        }
        if(isAlwaysShown()) {
            displaySearchField();
        }
    }
    
    private void displaySearchField() {
        if (searchPanel != null || !isEnabled()) {
            return;
        }
        searchTextField.setOriginalFocusOwner();
        searchTextField.setFont(component.getFont());
        searchPanel = new SearchPanel(component, isAlwaysShown());
        final JLabel lbl;
        if (popupMenu != null) {
            lbl = new JLabel(org.openide.util.ImageUtilities.loadImageIcon(ICON_FIND_WITH_MENU, false));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e, lbl);
                }
            });
        } else {
            lbl = new JLabel(org.openide.util.ImageUtilities.loadImageIcon(ICON_FIND, false));
        }
        if (asynchronous) {
            animationTimer = new AnimationTimer(lbl, lbl.getIcon());
        } else {
            animationTimer = null;
        }
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            if (popupMenu != null) {
                final JPopupMenu dummy = new JPopupMenu();
                dummy.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dummy.setVisible(false);
                            }
                        });
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });

                searchTextField.putClientProperty("JTextField.Search.FindPopup", dummy); //NOI18N
                searchTextField.putClientProperty("JTextField.Search.FindAction", new ActionListener() { //NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        maybeShowPopup(null, searchTextField);
                    }
                });
            }
        } else {
            searchPanel.add(lbl);
        }
        searchPanel.add(searchTextField);
        searchPanel.setBackground(component.getBackground());
        lbl.setLabelFor(searchTextField);
        searchTextField.setColumns(10);
        searchTextField.setMaximumSize(searchTextField.getPreferredSize());
        searchTextField.putClientProperty("JTextField.variant", "search"); //NOI18N
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        if (constraints == null) {
            component.add(searchPanel);
        } else {
            component.add(searchPanel, constraints);
        }
        component.invalidate();
        component.revalidate();
        component.repaint();
        searchTextField.requestFocus();
        searchTextField.selectAll(); // Select an existing text for an easy rewrite
    }
    
    protected void maybeShowPopup(MouseEvent evt, Component comp) {
        if (evt != null && !SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }
        JPopupMenu pm = popupMenu.getPopupMenu();
        pm.show(comp, 0, comp.getHeight() - 1);
        searchTextField.setText("");
        searchTextField.requestOriginalFocusOwner();
    }
    
    private void removeSearchField() {
        if (isAlwaysShown()) {
            return;
        }
        if (searchPanel == null) {
            return;
        }
        if (animationTimer != null) {
            animationTimer.stopProgressAnimation();
        }
        Component sp = searchPanel;
        searchPanel = null;
        component.remove(sp);
        component.invalidate();
        component.revalidate();
        component.repaint();
    }
    
    /** Accessed from test. */
    JTextField getSearchField() {
        return searchTextField;
    }
    
    /**
     * Utility method, that finds a greatest common prefix of two supplied
     * strings.
     * 
     * @param str1 The first string
     * @param str2 The second string
     * @param ignoreCase Whether to ignore case in the comparisons
     * @return The greatest common prefix of the two strings.
     */
    public static String findMaxPrefix(String str1, String str2, boolean ignoreCase) {
        int n1 = str1.length();
        int n2 = str2.length();
        int i = 0;
        if (ignoreCase) {
            for ( ; i < n1 && i < n2; i++) {
                char c1 = Character.toUpperCase(str1.charAt(i));
                char c2 = Character.toUpperCase(str2.charAt(i));
                if (c1 != c2) {
                    break;
                }
            }
        } else {
            for ( ; i < n1 && i < n2; i++) {
                char c1 = str1.charAt(i);
                char c2 = str2.charAt(i);
                if (c1 != c2) {
                    break;
                }
            }
        }
        return str1.substring(0, i);
    }
    
    private static final class AnimationTimer {
        
        private final JLabel jLabel;
        private final Icon findIcon;
        private final Timer animationTimer;
        
        public AnimationTimer(final JLabel jLabel, Icon findIcon) {
            this.jLabel = jLabel;
            this.findIcon = findIcon;
            animationTimer = new Timer(100, new ActionListener() {

                ImageIcon icons[];
                int index = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (icons == null) {
                        icons = new ImageIcon[8];
                        for (int i = 0; i < 8; i++) {
                            icons[i] = ImageUtilities.loadImageIcon("org/openide/awt/resources/quicksearch/progress_" + i + ".png", false);  //NOI18N
                        }
                    }
                    jLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 6));
                    jLabel.setIcon(icons[index]);
                    //mac os x
                    jLabel.repaint();

                    index = (index + 1) % 8;
                }
            });
        }
        
        public void startProgressAnimation() {
            if (animationTimer != null && !animationTimer.isRunning()) {
                animationTimer.start();
            }
        }

        public void stopProgressAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
                jLabel.setIcon(findIcon);
                jLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }
        }

    }

    private class LazyFire implements Runnable {
        
        private final QS_FIRE fire;
        //private final QuickSearchListener[] qsls;
        private final String searchText;
        private final boolean forward;
        private final Consumer<String> newPrefixSetter;
        
        LazyFire(QS_FIRE fire, String searchText) {
            this(fire, searchText, true, null);
        }
        
        LazyFire(QS_FIRE fire, boolean forward) {
            this(fire, null, forward);
        }
        
        LazyFire(QS_FIRE fire, String searchText, boolean forward) {
            this(fire, searchText, forward, null);
        }
        
        LazyFire(QS_FIRE fire, String searchText,
                 Consumer<String> newPrefixSetter) {
            this(fire, searchText, true, newPrefixSetter);
        }
        
        LazyFire(QS_FIRE fire, String searchText, boolean forward,
                 Consumer<String> newPrefixSetter) {
            this.fire = fire;
            //this.qsls = qsls;
            this.searchText = searchText;
            this.forward = forward;
            this.newPrefixSetter = newPrefixSetter;
            animationTimer.startProgressAnimation();
        }

        @Override
        public void run() {
            try {
            switch (fire) {
                case UPDATE:    callback.quickSearchUpdate(searchText);//fireQuickSearchUpdate(qsls, searchText);
                                break;
                case NEXT:      callback.showNextSelection(forward);//fireShowNextSelection(qsls, forward);
                                break;
                case MAX:       String mp = callback.findMaxPrefix(searchText);//String mp = findMaxPrefix(qsls, searchText);
                                newPrefixSetter.accept(mp);
                                break;
            }
            } finally {
                animationTimer.stopProgressAnimation();
            }
        }
    }
    
    private static class SearchPanel extends JPanel {
        
        public static final boolean isAquaLaF =
                "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
        private JComponent component;
        private boolean alwaysShown = false;
    
        public SearchPanel(JComponent component, boolean alwaysShown) {
            this.component = component;
            this.alwaysShown = alwaysShown;
            if (isAquaLaF) {
                setBorder(BorderFactory.createEmptyBorder(9,6,8,2));
            } else {
                setBorder(BorderFactory.createEmptyBorder(2,6,2,2));
            }
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isAquaLaF && g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                if (alwaysShown) {
                    g2d.setColor(component.getBackground());
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, UIManager.getColor("NbExplorerView.quicksearch.background.top"), //NOI18N
                            0, getHeight(), UIManager.getColor("NbExplorerView.quicksearch.background.bottom")));//NOI18N
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(UIManager.getColor("NbExplorerView.quicksearch.border")); //NOI18N
                    g2d.drawLine(0, 0, getWidth(), 0);
                }
            } else {
                super.paintComponent(g);
            }
        }
    }

    /** searchTextField manages focus because it handles VK_ESCAPE key */
    private class SearchTextField extends JTextField {
        
        private WeakReference<Component> originalFocusOwner = new WeakReference<Component>(null);
        
        public SearchTextField() {
        }
        
        void setOriginalFocusOwner() {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null && component.isAncestorOf(focusOwner)) {
                originalFocusOwner = new WeakReference<Component>(focusOwner);
            } else {
                originalFocusOwner = new WeakReference<Component>(component);
            }
        }
        
        void requestOriginalFocusOwner() {
            SwingUtilities.invokeLater(
                new Runnable() {
                    //additional bugfix - do focus change later or removing
                    //the component while it's focused will cause focus to
                    //get transferred to the next component in the
                    //parent focusTraversalPolicy *after* our request
                    //focus completes, so focus goes into a black hole - Tim
                    @Override
                    public void run() {
                        Component fo = originalFocusOwner.get();
                        if (fo != null) {
                            fo.requestFocusInWindow();
                        }
                    }
                }
            );
        }
        
        @Override
        public boolean isManagingFocus() {
            return true;
        }

        @Override
        public void processKeyEvent(KeyEvent ke) {
            //override the default handling so that
            //the parent will never receive the escape key and
            //close a modal dialog
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                ke.consume();
                searchFieldListener.ignoreEvents = true;
                try {
                    // Clear the text after ESC
                    setText("");                                                // NOI18N
                } finally {
                    searchFieldListener.ignoreEvents = false;
                }
                // bugfix #32909, reqest focus when search field is removed
                requestOriginalFocusOwner();
                //fireQuickSearchCanceled();
                callback.quickSearchCanceled();
                hasSearchText = false;
            } else {
                if (!hasSearchText) {
                    int keyCode = ke.getKeyCode();
                    if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP ||
                        keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                        keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_F3) {
                        // Ignore movement events when search text was not set
                        return ;
                    }
                }
                super.processKeyEvent(ke);
            }
        }
    };
    
    private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {
        
        private boolean ignoreEvents;
        private boolean ignoreRemove;

        SearchFieldListener() {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreEvents) return;
            searchForNode();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (ignoreEvents) return;
            searchForNode();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (ignoreEvents || ignoreRemove) return;
            searchForNode();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                searchTextField.requestOriginalFocusOwner();
                ignoreEvents = true;
                try {
                    // Clear the text after ESC
                    searchTextField.setText("");                                // NOI18N
                } finally {
                    ignoreEvents = false;
                }
                //fireQuickSearchCanceled();
                callback.quickSearchCanceled();
                hasSearchText = false;
                e.consume();
            } else if (keyCode == KeyEvent.VK_UP || (keyCode == KeyEvent.VK_F3 && e.isShiftDown())) {
                fireShowNextSelection(false);
                // Stop processing the event here. Otherwise it's dispatched
                // to the tree too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_F3) {
                fireShowNextSelection(true);
                // Stop processing the event here. Otherwise it's dispatched
                // to the tree too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_TAB) {
                findMaxPrefix(searchTextField.getText(), new Consumer<String>() {
                    @Override
                    public void accept(final String maxPrefix) {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    accept(maxPrefix);
                                    searchTextField.transferFocus();
                                }
                            });
                            return ;
                        }
                        ignoreEvents = true;
                        try {
                            searchTextField.setText(maxPrefix);
                        } finally {
                            ignoreEvents = false;
                        }
                    }
                });

                e.consume();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                removeSearchField();
                //fireQuickSearchConfirmed();
                callback.quickSearchConfirmed();

                component.requestFocusInWindow();
                e.consume();
            }
        }

        /** Searches for a node in the tree. */
        private void searchForNode() {
            String text = searchTextField.getText();
            if (text.isEmpty() && isAlwaysShown()) {
                callback.quickSearchCanceled();
                hasSearchText = false;
            } else {
                fireQuickSearchUpdate(text);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (e.getSource() == searchTextField) {
                // make sure nothing is selected
                int n = searchTextField.getText().length();
                searchTextField.select(n, n);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.isTemporary() || isAlwaysShown()) {
                return ;
            }
            Component oppositeComponent = e.getOppositeComponent();
            if (e.getSource() != searchTextField) {
                ((Component) e.getSource()).removeFocusListener(this);
            }
            if (oppositeComponent instanceof JMenuItem || oppositeComponent instanceof JPopupMenu) {
                oppositeComponent.addFocusListener(this);
                return ;
            }
            if (oppositeComponent == searchTextField) {
                return ;
            }
            if (searchPanel != null) {
                removeSearchField();
                //fireQuickSearchConfirmed();
                callback.quickSearchCanceled();
                hasSearchText = false;
            }
        }
        
        private class ReplaceFilter extends DocumentFilter {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // Replace might do remove before insert. Suppress remove so that we get only one event with the final text
                if (text != null && !text.isEmpty()) {
                    // will insert
                    ignoreRemove = true;
                }
                try {
                    super.replace(fb, offset, length, text, attrs);
                } finally {
                    ignoreRemove = false;
                }
            }

        }

    }
    
    /**
     * Call back interface, that is notified with the submissions to the quick search field.
     * 
     * @author Martin Entlicher
     * @since 7.43
     */
    public static interface Callback {
        
        /**
         * Called with an updated search text.
         * When {@link #isAsynchronous()} is <code>false</code>
         * it's called in EQ thread, otherwise, it's called in a background thread.
         * The client should update the visual representation of the search results
         *  and then return.<p>
         * This method is called to initiate and update the search process.
         * @param searchText The new text to search for.
         */
        void quickSearchUpdate(String searchText);

        /**
         * Called to select a next occurrence of the search result.
         * When {@link #isAsynchronous()} is <code>false</code>
         * it's called in EQ thread, otherwise, it's called in a background thread.
         * The client should update the visual representation of the search results
         * and then return.<p>
         * @param forward The direction of the next search result.
         *                <code>true</code> for forward direction,
         *                <code>false</code> for backward direction.
         */
        void showNextSelection(boolean forward);

        /**
         * Find the maximum prefix among the search results, that starts with the provided string.
         * This method is called when user press TAB in the search field, to auto-complete
         * the maximum prefix.
         * When {@link #isAsynchronous()} is <code>false</code>
         * it's called in EQ thread, otherwise, it's called in a background thread.
         * Utility method {@link QuickSearch#findMaxPrefix(java.lang.String, java.lang.String, boolean)}
         * can be used by the implementation.
         * @param prefix The prefix to start with
         * @return The maximum prefix.
         */
        String findMaxPrefix(String prefix);

        /**
         * Called when the quick search is confirmed by the user.
         * This method is called in EQ thread always.
         */
        void quickSearchConfirmed();

        /**
         * Called when the quick search is canceled by the user.
         * This method is called in EQ thread always.
         */
        void quickSearchCanceled();

    }

}
