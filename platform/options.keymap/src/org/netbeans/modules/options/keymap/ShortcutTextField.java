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
package org.netbeans.modules.options.keymap;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;

/**
 * TextField displaying typed shortcuts
 * @author Max Sauer
 */
public class ShortcutTextField extends JTextField {

    private Popup popup;
    JList list  = new JList();
    JScrollPane pane = new JScrollPane();

    public ShortcutTextField(String text) {
        super(text);
        pane.setViewportView(list);
        pane.setMaximumSize(new Dimension(350, 350));
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.addKeyListener(new ShortcutListener(true));
        this.addKeyListener(new ShortcutCompletionListener());
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // close popup upon confirmation
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
            }
        });

        this.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                JTextField textField = (JTextField) e.getComponent();
                ((ShortcutListener)textField.getKeyListeners()[0]).clear(); //XXX clear buffer in a more clean way
                textField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedIndex = ((JList) e.getSource()).getSelectedIndex();
                confirm(selectedIndex);
            }
        });
    }

    public ShortcutTextField() {
        this("");
    }

    private void confirm(int selectedIndex) {
        if (selectedIndex != -1) {
            Object elementAt = list.getModel().getElementAt(selectedIndex);
            this.setText(elementAt.toString());
        }
        popup.hide();
        popup = null;
    }

    private void ensureSelectionVisible(int index) {
        Rectangle bounds = list.getCellBounds(index, index);
        if (bounds != null)
            list.scrollRectToVisible(bounds);
    }


    /**
     * Listens on typing in shortcut textfield and displays completion
     * popup with free shortcuts
     */
    private class ShortcutCompletionListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            JTextField tf = (JTextField) e.getSource();
            int selectedIndex = list.getSelectedIndex();

            list.setListData(getFreeShortcuts(tf.getText()));
            final int keyCode = e.getKeyCode();

            if (popup == null) {
                //in case popup is not shown and user confirms/cancels, do not show popup
                if(keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
                    return ;
                }

                Point p = new Point(tf.getX(), tf.getY());
                SwingUtilities.convertPointToScreen(p, tf.getParent());
                
                popup = PopupFactory.getSharedInstance().getPopup(tf, pane, p.x, p.y + tf.getHeight() + 1);
            }

            pane.setPreferredSize(new Dimension(list.getPreferredSize().width + pane.getVerticalScrollBar().getPreferredSize().width + 2, Math.min(350, list.getPreferredSize().height) + 5));
            popup.show();

            switch (keyCode) {

                case KeyEvent.VK_DOWN: {
                    int index = selectedIndex == -1 || selectedIndex == list.getModel().getSize() - 1 ? 0 : selectedIndex + 1;
                    list.setSelectedIndex(index);
                    ensureSelectionVisible(index);
                    e.consume();
                    break;
                }
                
                case KeyEvent.VK_UP: {
                    int index = selectedIndex == -1 ? 0 : selectedIndex - 1;
                    if (selectedIndex == 0)
                        index = list.getModel().getSize() - 1;
                    list.setSelectedIndex(index);
                    ensureSelectionVisible(index);
                    e.consume();
                    break;
                }

                case KeyEvent.VK_ESCAPE: {
                    popup.hide();
                    popup = null;
                    e.consume();
                    break;
                }

                case KeyEvent.VK_ENTER: {
                    confirm(selectedIndex);
                    break;
                }

            }

        }
    }

    /**
     * Provides all system-specific currently presumably free shortcuts
     * with provided prefix
     * @param prefix the prefix the shortcut should begin with
     * @return set of free shortcuts
     */
    private Vector<String> getFreeShortcuts(String prefix) {
        Vector<String> vec = new Vector<String>();
        for (String s : getAllFreeShortcuts()) {
            if (s.startsWith(prefix)) {
                vec.add(s);
            }
        }
        if(vec.size() == 0)
            vec.add(NbBundle.getMessage(ShortcutTextField.class, "No_Free_Shortcut", prefix)); // NOI18N
        return vec;
    }
    
    /**
     * @return set of all, system specific, currently unoccupied shortcuts
     */
    private Set<String> getAllFreeShortcuts() {
        // I know that the next line is not the smartest code on earth:
        KeymapViewModel model = 
                ((KeymapPanel)SwingUtilities.getAncestorOfClass(KeymapPanel.class, this))
                .getModel();
        //get shortcut cache (used shortcuts in current profile)
        Set<String> allCurrentlyUsedShortcuts = model.getMutableModel().getAllCurrentlyUsedShortcuts();

        //substract the cache from all possible shortcut set
        Set<String> result = new LinkedHashSet<String>();
        result.addAll(ShortcutProvider.getSet());
        result.removeAll(allCurrentlyUsedShortcuts);

        return result;
    }

}

