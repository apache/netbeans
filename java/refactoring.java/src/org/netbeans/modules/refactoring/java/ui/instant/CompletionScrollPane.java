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

package org.netbeans.modules.refactoring.java.ui.instant;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.ExtKit;

/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* @author Miloslav Metelka, Martin Roskanin, Dusan Balek
* @version 1.00
*/

public class CompletionScrollPane extends JScrollPane {
    
    private static final String ESCAPE = "escape"; //NOI18N
    private static final String COMPLETION_UP = "completion-up"; //NOI18N
    private static final String COMPLETION_DOWN = "completion-down"; //NOI18N
    private static final String COMPLETION_PGUP = "completion-pgup"; //NOI18N
    private static final String COMPLETION_PGDN = "completion-pgdn"; //NOI18N
    private static final String COMPLETION_BEGIN = "completion-begin"; //NOI18N
    private static final String COMPLETION_END = "completion-end"; //NOI18N
    private static final String COMPLETION_SUBITEMS_SHOW = "completion-subitems-show"; //NOI18N
    
    private static final int ACTION_ESCAPE = 0;
    private static final int ACTION_COMPLETION_UP = 1;
    private static final int ACTION_COMPLETION_DOWN = 2;
    private static final int ACTION_COMPLETION_PGUP = 3;
    private static final int ACTION_COMPLETION_PGDN = 4;
    private static final int ACTION_COMPLETION_BEGIN = 5;
    private static final int ACTION_COMPLETION_END = 6;
    private static final int ACTION_COMPLETION_SUBITEMS_SHOW = 7;
    
    private final JPanel view;
    
    private List dataObj;
    
    private JLabel topLabel;
    
    public CompletionScrollPane(JTextComponent editorComponent, MouseListener mouseListener) {
        
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Use maximumSize property to store the limit of the preferred size
        setMaximumSize(new Dimension(400, 300));
        // At least 2 items; do -1 for title height
        int maxVisibleRowCount = Math.max(2,
            getMaximumSize().height / CompletionLayout.COMPLETION_ITEM_HEIGHT - 1);

        // Add the completion view
        view = new JPanel();
        JCheckBox checkbox = new JCheckBox("Rename comments");
        checkbox.setFocusable(false);
        view.add(checkbox);
        JCheckBox checkbox1 = new JCheckBox("Rename Test Class");
        checkbox1.setFocusable(false);
        view.add(checkbox1);
        setViewportView(view);
        installKeybindings(editorComponent);
    }
    
    public void setData(List data, String title, int selectedIndex) {
        dataObj = data;
        setTitle(title);
        // Force the viewport preferred size to be taken into account
        // Otherwise the scroll pane attempts to retain its size
        // so e.g. if the number of visible rows increases so the vertical
        // scrollbar would be needed the scrollpane does not increase
        // its preferred size.
        // Resetting of viewport fixes the problem.
        setViewportView(getViewport().getView());
    }
    
//    public CompletionItem getSelectedCompletionItem() {
//        Object ret = view.getSelectedValue();
//        return ret instanceof CompletionItem ? (CompletionItem) ret : null;
//    }
    
//    public int getSelectedIndex() {
//        return view.getSelectedIndex();
//    }
    
//    public Point getSelectedLocation() {
//        Rectangle r = view.getCellBounds(getSelectedIndex(), getSelectedIndex());
//        Point p = new Point(r.getLocation());
//        SwingUtilities.convertPointToScreen(p, view);
//        p.x += r.width;
//        return p;
//    }
    
    public @Override Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        Dimension labelSize = topLabel != null ? topLabel.getPreferredSize() : new Dimension(0, 0);
        Dimension maxSize = getMaximumSize();
        if (labelSize.width > prefSize.width) {
            prefSize.width = labelSize.width;
        }
        if (prefSize.width > maxSize.width) {
            prefSize.width = maxSize.width;
        }
        // Height is covered by maxVisibleRowCount value
        return prefSize;
    }
    
    protected JPanel getView() {
        return view;
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

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey, JTextComponent component) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (component != null && editorActionName != null) {
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

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName, JTextComponent component){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke, component);
        for (KeyStroke key : keys) {
            getInputMap().put(key, actionName);
        }
        getActionMap().put(actionName, new CompletionPaneAction(action));
    }

    private void installKeybindings(JTextComponent component) {
	// Register Escape key
        registerKeybinding(ACTION_ESCAPE, ESCAPE,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        ExtKit.escapeAction, component);

        // Register up key
        registerKeybinding(ACTION_COMPLETION_UP, COMPLETION_UP,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        BaseKit.upAction, component);

        // Register down key
        registerKeybinding(ACTION_COMPLETION_DOWN, COMPLETION_DOWN,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        BaseKit.downAction, component);

        // Register PgDn key
        registerKeybinding(ACTION_COMPLETION_PGDN, COMPLETION_PGDN,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
        BaseKit.pageDownAction, component);

        // Register PgUp key
        registerKeybinding(ACTION_COMPLETION_PGUP, COMPLETION_PGUP,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
        BaseKit.pageUpAction, component);

        // Register home key
        registerKeybinding(ACTION_COMPLETION_BEGIN, COMPLETION_BEGIN,
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
        BaseKit.beginLineAction, component);

        // Register end key
        registerKeybinding(ACTION_COMPLETION_END, COMPLETION_END,
        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        BaseKit.endLineAction, component);

        registerKeybinding(ACTION_COMPLETION_SUBITEMS_SHOW, COMPLETION_SUBITEMS_SHOW,
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK),
        null, component);
    }

    List testGetData() {
        return dataObj;
    }
    
    private class CompletionPaneAction extends AbstractAction {
        private final int action;

        private CompletionPaneAction(int action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (action) {
                case ACTION_ESCAPE:
//                    if (CompletionImpl.get().hideCompletion(false)) {
//                        LogRecord r = new LogRecord(Level.FINE, "COMPL_CANCEL"); // NOI18N
//                        CompletionImpl.uilog(r);
//                    }
                    break;
                case ACTION_COMPLETION_UP:
//                    view.up();
                    break;
                case ACTION_COMPLETION_DOWN:
//                    view.down();
                    break;
                case ACTION_COMPLETION_PGUP:
//                    view.pageUp();
                    break;
                case ACTION_COMPLETION_PGDN:
//                    view.pageDown();
                    break;
                case ACTION_COMPLETION_BEGIN:
//                    view.begin();
                    break;
                case ACTION_COMPLETION_END:
//                    view.end();
                    break;
                case ACTION_COMPLETION_SUBITEMS_SHOW:
//                    CompletionImpl.get().showCompletionSubItems();
                    break;
            }
        }
    }
}
