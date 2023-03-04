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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.editor.bookmarks.BookmarkHistory;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.openide.util.Utilities;

/**
 * Popup switcher for bookmarks history.
 *
 * @author Miloslav Metelka
 */
public final class BookmarkHistoryPopup implements KeyListener {
    
    // -J-Dorg.netbeans.modules.editor.bookmarks.ui.BookmarkHistoryPopup.level=FINE
    private static final Logger LOG = Logger.getLogger(BookmarkHistoryPopup.class.getName());

    private static BookmarkHistoryPopup INSTANCE = new BookmarkHistoryPopup();
    
    public static BookmarkHistoryPopup get() {
        return INSTANCE;
    }

    private JDialog popup;
    
    private JTable table;
    
    private BookmarksTableModel tableModel;
    
    private int selectedEntryIndex;
    
    private JLabel descriptionLabel;
    
    private Component lastFocusedComponent;
    
    private int keepOpenedModifiers;
    
    private KeyStroke gotoNextKeyStroke;
    
    private KeyStroke gotoPreviousKeyStroke;
    
    private BookmarkHistoryPopup() {
    }
    
    public void show(boolean gotoNext) {
        if (popup != null) { // Refresh in case it already exists
            hide();
        }
        lastFocusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if ((gotoPreviousKeyStroke = BookmarkUtils.findKeyStroke("bookmark.history.popup.previous")) != null) { //NOI18N
            keepOpenedModifiers = modifiersBits(gotoPreviousKeyStroke.getModifiers());
            gotoNextKeyStroke = BookmarkUtils.findKeyStroke("bookmark.history.popup.next"); //NOI18N
        } else {
            keepOpenedModifiers = 0; // Keep opened until Escape pressed
        }

        descriptionLabel = new JLabel();
        descriptionLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        Font font = descriptionLabel.getFont();
        font = new Font(font.getName(), font.getStyle(), (int) (0.9f * font.getSize()));
        descriptionLabel.setFont(font);
        selectedEntryIndex = -1;
        table = createTable();
        table.setBorder(new LineBorder(table.getForeground()));
        Rectangle screenBounds = Utilities.getUsableScreenBounds();
        initTable(screenBounds);
        // At least one entry -> select either first or last entry
        selectEntry(gotoNext ? (tableModel.getEntryCount() > 2 ? 1: 0) : (tableModel.getEntryCount() > 1 ? tableModel.getEntryCount() - 2 : tableModel.getEntryCount() - 1));

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints;
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        panel.add(table, constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        panel.add(new JSeparator(SwingConstants.HORIZONTAL), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        panel.add(descriptionLabel, constraints);

        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(java.awt.SystemColor.controlDkShadow), 
                BorderFactory.createEmptyBorder(4, 4, 4, 4))); 

        Dimension prefSize = panel.getPreferredSize();
        int x = screenBounds.x + (screenBounds.width - prefSize.width) / 2;
        int y = screenBounds.y + (screenBounds.height - prefSize.height) / 2;
        popup = new JDialog();
        popup.setModal(true);
        popup.setAlwaysOnTop(true);
        popup.setUndecorated(true);
        popup.getContentPane().add(panel);
        popup.setLocation(x, y);
        popup.pack();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "BookmarkHistoryPopup.show: keepOpenedModifiers={0} bounds={1}\n", // NOI18N
                    new Object[]{keepOpenedModifiers, popup.getBounds()});
        }
        popup.setVisible(true);
    }
    
    public void hide() {
        if (popup != null) {
            table.removeKeyListener(this);
            popup.setVisible(false);
            popup.dispose();
            popup = null;
            table = null;
            tableModel = null;
            descriptionLabel = null;
        }
    }
    
    private JTable createTable() {
        List<BookmarkInfo> historyBookmarks = BookmarkHistory.get().historyBookmarks();
        BookmarksNodeTree nodeTree = new BookmarksNodeTree();
        Map<BookmarkInfo, BookmarkNode> bookmark2NodeMap = nodeTree.createBookmark2NodeMap();
        List<BookmarkNode> entries = new ArrayList<BookmarkNode>(historyBookmarks.size() + 1);
        entries.add(bookmark2NodeMap.get(BookmarkInfo.BOOKMARKS_WINDOW));
        for (BookmarkInfo bookmark : historyBookmarks) {
            BookmarkNode bookmarkNode = bookmark2NodeMap.get(bookmark);
            assert bookmarkNode != null;
            entries.add(bookmarkNode);
        }
        Collections.reverse(entries);
        tableModel = new BookmarksTableModel(true);
        assert !entries.contains(null);
        tableModel.setEntries(entries);
        return new JTable(tableModel);
    }
    
    private void initTable(Rectangle maxBounds) {
        table.setShowGrid(false);
        table.setCellSelectionEnabled(true);
        table.setAutoscrolls(false);
        table.setRowHeight(table.getRowHeight() + 4); // +4 for icon
        // Get Graphics resp. FontRenderContext from an off-screen image
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        Font font = table.getFont();
        FontMetrics fm = g.getFontMetrics(font);
        int maxWidth = 1;
        int maxHeight = fm.getHeight() + 2; // Extra two pixels for spaces between lines
        for (int i = tableModel.getEntryCount() - 1; i >= 0; i--) {
            String value = (String) tableModel.getValueAt(i, 0);
            int stringWidth = fm.stringWidth(value);
            maxWidth = Math.max(maxWidth, stringWidth);
        }
        maxWidth += 25; // Add icon width
        maxWidth += 12; // Add extra space occupied by cell borders? etc.
        int columnEntryCount = maxBounds.height / maxHeight;
        int columnCount = tableModel.setColumnEntryCount(columnEntryCount);
        table.setTableHeader(null);
        TableCellRenderer cellRenderer = new BookmarkNodeRenderer(true);
        TableColumnModel columnModel = table.getColumnModel(); // 1 column by default
        TableColumn column = columnModel.getColumn(0);
        column.setCellRenderer(cellRenderer);
        column.setPreferredWidth(maxWidth);
        while (columnModel.getColumnCount() < columnCount) {
            column = new TableColumn(columnModel.getColumnCount());
            column.setPreferredWidth(maxWidth);
            column.setCellRenderer(cellRenderer);
            columnModel.addColumn(column);
        }

        table.addKeyListener(this);
    }
    
    private void selectEntry(int entryIndex) {
        assert (entryIndex >= 0) : "entryIndex=" + entryIndex + " < 0"; // NOI18N
        int[] rowColumn = new int[2];
        if (selectedEntryIndex != -1) {
            tableModel.entryIndex2rowColumn(selectedEntryIndex, rowColumn);
            table.changeSelection(rowColumn[0], rowColumn[1], true, false);
        }
        this.selectedEntryIndex = entryIndex;
        tableModel.entryIndex2rowColumn(selectedEntryIndex, rowColumn);
        table.changeSelection(rowColumn[0], rowColumn[1], true, false);
        descriptionLabel.setText(tableModel.getToolTipText(rowColumn[0], rowColumn[1]));
        Dimension labelSize = descriptionLabel.getSize();
        if (labelSize != null) {
            if (descriptionLabel.getPreferredSize().width > labelSize.width) {
                descriptionLabel.revalidate();
            }
        }
    }
    
    private void selectNext() {
        int nextIndex = (selectedEntryIndex + 1) % tableModel.getEntryCount();
        selectEntry(nextIndex);
    }
    
    private void selectPrevious() {
        int prevIndex = (selectedEntryIndex <= 0) ? (tableModel.getEntryCount() - 1) : selectedEntryIndex - 1;
        selectEntry(prevIndex);
    }
    
    private void openBookmark(BookmarkInfo bookmark) {
        if (bookmark != null) {
            if (bookmark == BookmarkInfo.BOOKMARKS_WINDOW) {
                BookmarksView.openView();
            } else {
                BookmarkUtils.postOpenEditor(bookmark);
            }
        }
    }
    
    private BookmarkInfo getSelectedBookmark() {
        return (selectedEntryIndex != -1) ? tableModel.getEntry(selectedEntryIndex).getBookmarkInfo() : null;
    }
    
    private void returnFocus() {
        if (lastFocusedComponent != null) {
            lastFocusedComponent.requestFocus();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("BookmarkHistoryPopup.keyPressed: e=" + e + '\n');
        }
        int keyCode = e.getKeyCode();
        if (gotoPreviousKeyStroke != null && gotoPreviousKeyStroke.getKeyCode() == keyCode) {
            e.consume();
            selectPrevious();
        } else if (gotoNextKeyStroke != null && gotoNextKeyStroke.getKeyCode() == keyCode) {
            e.consume();
            selectNext();
        } else {
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    BookmarkInfo selectedBookmark = getSelectedBookmark();
                    hide();
                    openBookmark(selectedBookmark);
                    e.consume();
                    break;

                case KeyEvent.VK_ESCAPE:
                    e.consume();
                    hide();
                    returnFocus();
                    break;

                case KeyEvent.VK_DOWN:
                    e.consume();
                    selectNext();
                    break;

                case KeyEvent.VK_UP:
                    e.consume();
                    selectPrevious();
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int modBits = modifiersBits(e.getModifiersEx());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("BookmarkHistoryPopup.keyReleased: e=" + e + ", modBits=" + modBits + '\n'); // NOI18N
        }
        if (keepOpenedModifiers != 0 && (modBits & keepOpenedModifiers) != keepOpenedModifiers) {
            e.consume();
            BookmarkInfo selectedBookmark = getSelectedBookmark();
            hide();
            openBookmark(selectedBookmark);
        }
    }

    private static int modifiersBits(int modifiers) {
        return modifiers & (
                InputEvent.SHIFT_DOWN_MASK |
                InputEvent.CTRL_DOWN_MASK |
                InputEvent.ALT_DOWN_MASK |
                InputEvent.META_DOWN_MASK
                );
    }

}
