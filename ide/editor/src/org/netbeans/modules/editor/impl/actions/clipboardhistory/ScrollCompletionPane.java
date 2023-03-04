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

package org.netbeans.modules.editor.impl.actions.clipboardhistory;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.impl.actions.ClipboardHistoryAction;

/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* borrowed org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane
*/
public class ScrollCompletionPane extends JScrollPane {

    private static final Logger LOG = Logger.getLogger(ScrollCompletionPane.class.getName());
    
    private static final String COMPLETION_UP = "completion-up"; //NOI18N
    private static final String COMPLETION_DOWN = "completion-down"; //NOI18N
    private static final String COMPLETION_PGUP = "completion-pgup"; //NOI18N
    private static final String COMPLETION_PGDN = "completion-pgdn"; //NOI18N
    private static final String COMPLETION_BEGIN = "completion-begin"; //NOI18N
    private static final String COMPLETION_END = "completion-end"; //NOI18N
    
    private static final int ACTION_COMPLETION_UP = 1;
    private static final int ACTION_COMPLETION_DOWN = 2;
    private static final int ACTION_COMPLETION_PGUP = 3;
    private static final int ACTION_COMPLETION_PGDN = 4;
    private static final int ACTION_COMPLETION_BEGIN = 5;
    private static final int ACTION_COMPLETION_END = 6;
    
    static final int POPUP_VERTICAL_GAP = 1;

    private ListCompletionView view;
    private JLabel topLabel;

    private Dimension minSize;
    private Dimension maxSize;
    private Dimension scrollBarSize;

    public ScrollCompletionPane(JTextComponent component, ClipboardHistory data, String title, ListSelectionListener listener, MouseListener mouseListener) {
        this( component, data, title, listener, mouseListener, null );
    }

    public ScrollCompletionPane(JTextComponent component, ClipboardHistory data, String title, ListSelectionListener listener, MouseListener mouseListener, Dimension maxSize ) {
        
        // Compute size of the scrollbars
        Dimension smallSize = super.getPreferredSize();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        scrollBarSize = super.getPreferredSize();
        scrollBarSize.width -= smallSize.width;
        scrollBarSize.height -= smallSize.height;
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);              
        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(java.awt.SystemColor.controlDkShadow), 
                BorderFactory.createEmptyBorder(4, 4, 4, 4))); 
        setViewportBorder( null );
        
        // initialize sizes, why are we using the same values as for the CC popup ??
        String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component);
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        minSize = parseDimension(prefs.get(SimpleValueNames.COMPLETION_PANE_MIN_SIZE, null), new Dimension(60, 17));
        setMinimumSize(minSize);

        //Resize upto edge of screenborder, not COMPLETION_PANE_MAX_SIZE
        maxSize = parseDimension(prefs.get(SimpleValueNames.COMPLETION_PANE_MAX_SIZE, null), new Dimension(400, 300));
        if( maxSize != null ) {
            this.maxSize = maxSize;
            setMaximumSize(maxSize);
        }

        // Add the completion view
        view = new ListCompletionView(mouseListener);
        setBackground(view.getBackground());
        view.addListSelectionListener(listener);
        view.setResult(data);
        resetViewSize();
        setViewportView(view);

        setTitle(title);
        installKeybindings(component);
        setFocusable (false);
        view.setFocusable (false);
    }
    
    public ListCompletionView getView() {
        return view;
    }

    public void reset(ClipboardHistory data, String title) {
        view.setResult(data);
        resetViewSize();
        setTitle(title);
    }
    

    public ClipboardHistoryElement getSelectedValue() {
        Object ret = view.getSelectedValue();
        return ret instanceof ClipboardHistoryElement? (ClipboardHistoryElement) ret : null;
    }
    
    public @Override Dimension getPreferredSize() {
        Dimension ps = super.getPreferredSize();

        /* Add size of the vertical scrollbar by default. This could be improved
        * to be done only if the height exceeds the bounds. */
        int width = ps.width + scrollBarSize.width;
        boolean displayHorizontalScrollbar = width > maxSize.width;
        width = Math.max(Math.max(width, minSize.width),
                            getTitleComponentPreferredSize().width);
        width = Math.min(width, maxSize.width);

        int height = displayHorizontalScrollbar ? ps.height + scrollBarSize.height : ps.height;
        height = Math.min(height, maxSize.height);
        height = Math.max(height, minSize.height);
        return new Dimension(width, height);
    }

    private void resetViewSize() {
        Dimension viewSize = view.getPreferredSize();
        if (viewSize.width > maxSize.width - scrollBarSize.width) {
            viewSize.width = maxSize.width - scrollBarSize.width;
            view.setPreferredSize(viewSize);
        }
    }
    
    private void setTitle(String title) {
        if (title == null) {
            if (topLabel != null) {
                setColumnHeader(null);
                topLabel = null;
            }
        } else {
            if (topLabel != null) {
                topLabel.setText(title);
            } else {
                topLabel = new JLabel(title);
                topLabel.setForeground(Color.blue);
                topLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
                setColumnHeaderView(topLabel);
            }
        }
    }

    private Dimension getTitleComponentPreferredSize() {
        return topLabel != null ? topLabel.getPreferredSize() : new Dimension();
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(JTextComponent component, String editorActionName, KeyStroke defaultKey) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (component != null) {
            Action a = component.getActionMap().get(editorActionName);
            Keymap km = component.getKeymap();
            if (a != null && km != null) {
                KeyStroke[] keys = km.getKeyStrokesForAction(a);
                if (keys != null && keys.length > 0) {
                    ret = keys;
                }
            }
        }
        return ret;
    }

    private void registerKeybinding(JTextComponent component, int action, String actionName, KeyStroke stroke, String editorActionName){
        KeyStroke[] keys = findEditorKeys(component, editorActionName, stroke);
        for (int i = 0; i < keys.length; i++) {
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new CompletionPaneAction(action));
    }

    private void installKeybindings(JTextComponent component) {
        // Register up key
        registerKeybinding(component, ACTION_COMPLETION_UP, COMPLETION_UP,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        BaseKit.upAction
        );

        // Register down key
        registerKeybinding(component, ACTION_COMPLETION_DOWN, COMPLETION_DOWN,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        BaseKit.downAction
        );

        // Register down key for clipboard history shortcut
        KeyStroke defaultKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        KeyStroke[] findEditorKeys = findEditorKeys(component, ClipboardHistoryAction.NAME, defaultKeystroke);
        for (KeyStroke ks : findEditorKeys) {
            getInputMap().put(ks, COMPLETION_DOWN);
        }

        // Register PgDn key
        registerKeybinding(component, ACTION_COMPLETION_PGDN, COMPLETION_PGDN,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
        BaseKit.pageDownAction
        );

        // Register PgUp key
        registerKeybinding(component, ACTION_COMPLETION_PGUP, COMPLETION_PGUP,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
        BaseKit.pageUpAction
        );

        // Register home key
        registerKeybinding(component, ACTION_COMPLETION_BEGIN, COMPLETION_BEGIN,
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
        BaseKit.beginLineAction
        );
        
        // Register end key
        registerKeybinding(component, ACTION_COMPLETION_END, COMPLETION_END,
        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        BaseKit.endLineAction
        );
    }

    private class CompletionPaneAction extends AbstractAction {
        private int action;

        private CompletionPaneAction(int action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            switch (action) {
                case ACTION_COMPLETION_UP:
                    view.up();
                    break;
                case ACTION_COMPLETION_DOWN:
                    view.down();
                    break;
                case ACTION_COMPLETION_PGUP:
                    view.pageUp();
                    break;
                case ACTION_COMPLETION_PGDN:
                        view.pageDown();
                    break;
                case ACTION_COMPLETION_BEGIN:
                        view.begin();
                    break;
                case ACTION_COMPLETION_END:
                        view.end();
                    break;                   
            }
        }
    }

    private static Dimension parseDimension(String s, Dimension d) {
        int arr[] = new int[2];
        int i = 0;
        
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

            while (st.hasMoreElements()) {
                if (i > 1) {
                    return d;
                }
                try {
                    arr[i] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    LOG.log(Level.WARNING, null, nfe);
                    return d;
                }
                i++;
            }
        }
        
        if (i != 2) {
            return d;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }
}
