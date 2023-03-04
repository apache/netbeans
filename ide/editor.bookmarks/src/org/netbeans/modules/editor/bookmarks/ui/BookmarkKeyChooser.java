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
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarksPersistence;
import org.netbeans.modules.editor.bookmarks.FileBookmarks;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.openide.util.NbBundle;

/**
 * Visual chooser for bookmark key shows list of cells for 0-9 and A-Z.
 *
 * @author Miloslav Metelka
 */
public class BookmarkKeyChooser implements KeyListener, ActionListener {
    
    private static BookmarkKeyChooser INSTANCE = new BookmarkKeyChooser();
    
    public static BookmarkKeyChooser get() {
        return INSTANCE;
    }
    
    private BookmarkKeyChooser() {
    }

    private BookmarkInfo result;
    
    private Runnable runOnClose;
    
    private Map<Character,BookmarkInfo> key2bookmark;
    
    private Dialog dialog;
    
    private JButton closeButton;
    
    public void show(Component parent, Runnable runOnClose) {
        this.runOnClose = runOnClose;
        key2bookmark = new HashMap<Character, BookmarkInfo>(2 * 46, 0.5f);
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            // Open projects should have their bookmarks loaded before invocation of this method
            // so that it's possible to enumerate present keys properly
            for (ProjectBookmarks projectBookmarks : lockedBookmarkManager.activeProjectBookmarks()) {
                for (FileBookmarks fileBookmarks : projectBookmarks.getFileBookmarks()) {
                    for (BookmarkInfo bookmark : fileBookmarks.getBookmarks()) {
                        String key = bookmark.getKey();
                        if (key != null && key.length() > 0) {
                            key2bookmark.put(key.charAt(0), bookmark);
                        }
                    }
                }
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
        
        JPanel cellPanel = new JPanel();
        if (key2bookmark.size() > 0) {
            cellPanel.setLayout(new GridLayout(4, 10, 2, 2));
            addCells(cellPanel, 10 * 4, '1', '9', '0', '0', 'A', 'Z');
        } else { // No bookmarks with keys
            cellPanel.setLayout(new GridLayout(2, 1, 2, 2));
            JLabel noKeysLabel = new JLabel(NbBundle.getMessage(BookmarkKeyChooser.class, "LBL_keyChooserNoActiveKeys"), JLabel.CENTER);
            JLabel noKeysHelpLabel = new JLabel(NbBundle.getMessage(BookmarkKeyChooser.class, "LBL_keyChooserNoActiveKeysHelp"), JLabel.CENTER);
            cellPanel.add(noKeysLabel);
            cellPanel.add(noKeysHelpLabel);
        }
        cellPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        closeButton = new JButton(NbBundle.getMessage(BookmarkKeyChooser.class, "CTL_keyChooserCloseButton")); // NOI18N

        dialog = org.netbeans.editor.DialogSupport.createDialog(
                NbBundle.getMessage(BookmarkKeyChooser.class, "CTL_keyChooserTitle"), // NOI18N
                cellPanel, true, // modal
                new JButton[] { closeButton }, false, // bottom buttons
                0, // defaultIndex = 0 => allow close by Enter key too
                0, // cancelIndex
                this // ActionListener
        );
        dialog.pack();
        
        Point parentMidPoint = new Point(parent.getWidth() / 2, parent.getHeight() / 2);
        SwingUtilities.convertPointToScreen(parentMidPoint, parent);
        dialog.setBounds(
                parentMidPoint.x - dialog.getWidth() / 2,
                parentMidPoint.y - dialog.getHeight() / 2,
                dialog.getWidth(),
                dialog.getHeight()
        );
        closeButton.addKeyListener(this);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
    
    public BookmarkInfo getandClearResult() {
        BookmarkInfo ret = result;
        result = null;
        return ret;
    }

    private void dispose() {
        closeButton.removeKeyListener(this);
        dialog.setVisible(false);
        dialog.dispose();
        key2bookmark = null;
        closeButton = null;
        dialog = null;
        Runnable r = runOnClose;
        runOnClose = null;
        r.run();
    }
    
    private void addCells(JPanel cellPanel, int cellCount, char... startToLastPairs) {
        Color foreColor = UIManager.getColor("Table.foreground");
        Color backColor = UIManager.getColor("Table.background");
        Color selForeColor = UIManager.getColor("Table.selectionForeground");
        Color selBackColor = UIManager.getColor("Table.selectionBackground");
        Border cellBorder = new LineBorder(Color.BLACK, 1, true);
        int cellId = 0;
        for (int i = 0; i < startToLastPairs.length;) {
            char start = startToLastPairs[i++];
            char last = startToLastPairs[i++];
            while (start <= last) {
                JLabel cell = new JLabel(" " + start + " ", SwingConstants.CENTER) {
                    @Override
                    public void paintComponent(Graphics g) {
                        Rectangle clip = g.getClipBounds();
                        Color origColor = g.getColor();
                        g.setColor(getBackground());
                        g.fillRect(clip.x, clip.y, clip.width, clip.height);
                        g.setColor(origColor);
                        super.paintComponent(g);
                    }
                };
                final BookmarkInfo bookmark = key2bookmark.get(start);
                if (bookmark != null) {
                    cell.setForeground(selForeColor);
                    cell.setBackground(selBackColor);
                    cell.setToolTipText(bookmark.getDescription(true, false, false));
                    cell.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                result = bookmark;
                                dispose();
                            }
                        }
                    });
                } else {
                    cell.setForeground(foreColor);
                    cell.setBackground(backColor);
                    cell.setToolTipText(NbBundle.getMessage(BookmarkKeyChooser.class,
                            "CTL_keyChooserUnoccupiedBookmarkKey")); // NOI18N
                }
                cell.setBorder(cellBorder);
                cellPanel.add(cell);
                cellId++;
                start++;
            }
        }
        // Fill in remaining cells (otherwise only would be 9x4)
        while (cellId < cellCount) {
            cellPanel.add(new JPanel());
            cellId++;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Does not get delivered
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        char ch = 0;
        if (KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
            ch = (char) ((keyCode - KeyEvent.VK_0) + '0');
        } else if (KeyEvent.VK_A <= keyCode && keyCode <= KeyEvent.VK_Z) {
            ch = (char) ((keyCode - KeyEvent.VK_A) + 'A');
        }
        if (ch != 0) {
            BookmarkInfo bookmark = key2bookmark.get(ch);
            if (bookmark != null) {
                result = bookmark;
                dispose();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
