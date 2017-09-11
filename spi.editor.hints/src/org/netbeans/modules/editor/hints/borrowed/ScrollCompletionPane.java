/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.hints.borrowed;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

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
import org.netbeans.editor.*;
import org.netbeans.modules.editor.hints.FixData;
import org.netbeans.modules.editor.hints.HintsUI;
import org.netbeans.spi.editor.hints.Fix;

/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* @author Miloslav Metelka, Martin Roskanin, Dusan Balek
* @version 1.00
*/

public class ScrollCompletionPane extends JScrollPane {

    private static final Logger LOG = Logger.getLogger(ScrollCompletionPane.class.getName());
    
    private static final String COMPLETION_UP = "completion-up"; //NOI18N
    private static final String COMPLETION_DOWN = "completion-down"; //NOI18N
    private static final String COMPLETION_PGUP = "completion-pgup"; //NOI18N
    private static final String COMPLETION_PGDN = "completion-pgdn"; //NOI18N
    private static final String COMPLETION_BEGIN = "completion-begin"; //NOI18N
    private static final String COMPLETION_END = "completion-end"; //NOI18N
    private static final String COMPLETION_RIGHT = "completion-right"; //NOI18N
    private static final String COMPLETION_LEFT = "completion-left"; //NOI18N

    private static final int ACTION_COMPLETION_UP = 1;
    private static final int ACTION_COMPLETION_DOWN = 2;
    private static final int ACTION_COMPLETION_PGUP = 3;
    private static final int ACTION_COMPLETION_PGDN = 4;
    private static final int ACTION_COMPLETION_BEGIN = 5;
    private static final int ACTION_COMPLETION_END = 6;
    private static final int ACTION_COMPLETION_RIGHT = 7;
    private static final int ACTION_COMPLETION_LEFT = 8;

    private JTextComponent component;

    private ListCompletionView view;
    private JLabel topLabel;

    private Dimension minSize;
    private Dimension maxSize;
    private Dimension scrollBarSize;

    public ScrollCompletionPane(JTextComponent component, FixData fixes, String title, ListSelectionListener listener) {
        this( component, fixes, title, listener, null );
    }

    public ScrollCompletionPane(JTextComponent component, FixData fixes, String title, ListSelectionListener listener, Dimension maxSize ) {
        this.component = component;
        
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
        //maxSize = parseDimension(prefs.get(SimpleValueNames.COMPLETION_PANE_MAX_SIZE, null), new Dimension(400, 300));
        if( maxSize != null ) {
            this.maxSize = maxSize;
            setMaximumSize(maxSize);
        }

        // Add the completion view
        view = new ListCompletionView();
        setBackground(view.getBackground());
//        view.addListSelectionListener(listener);
        view.setResult(fixes);
        resetViewSize();
        setViewportView(view);

        setTitle(title);
        installKeybindings();
        setFocusable (false);
        view.setFocusable (false);
    }
    
    public ListCompletionView getView() {
        return view;
    }

    public void reset(FixData fixes, String title) {
        view.setResult(fixes);
        resetViewSize();
        setTitle(title);
    }

    public Fix getSelectedFix() {
        Object ret = view.getSelectedValue();
        return ret instanceof Fix ? (Fix) ret : null;
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
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
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

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke);
        for (int i = 0; i < keys.length; i++) {
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new CompletionPaneAction(action));
    }

    private void installKeybindings() {
        // Register up key
        registerKeybinding(ACTION_COMPLETION_UP, COMPLETION_UP,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        BaseKit.upAction
        );

        // Register down key
        registerKeybinding(ACTION_COMPLETION_DOWN, COMPLETION_DOWN,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        BaseKit.downAction
        );

        // Register PgDn key
        registerKeybinding(ACTION_COMPLETION_PGDN, COMPLETION_PGDN,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
        BaseKit.pageDownAction
        );

        // Register PgUp key
        registerKeybinding(ACTION_COMPLETION_PGUP, COMPLETION_PGUP,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
        BaseKit.pageUpAction
        );

        // Register home key
        registerKeybinding(ACTION_COMPLETION_BEGIN, COMPLETION_BEGIN,
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
        BaseKit.beginLineAction
        );

        // Register end key
        registerKeybinding(ACTION_COMPLETION_END, COMPLETION_END,
        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        BaseKit.endLineAction
        );

        // Register right key
        registerKeybinding(ACTION_COMPLETION_RIGHT, COMPLETION_RIGHT,
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
        BaseKit.forwardAction
        );

        // Register left key
        registerKeybinding(ACTION_COMPLETION_LEFT, COMPLETION_LEFT,
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
        BaseKit.backwardAction
        );
    }

    private class CompletionPaneAction extends AbstractAction {
        private int action;

        private CompletionPaneAction(int action) {
            this.action = action;
        }

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
                case ACTION_COMPLETION_RIGHT:
                    view.right();
                    break;
                case ACTION_COMPLETION_LEFT:
                    HintsUI.getDefault().undoOnePopup();
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
