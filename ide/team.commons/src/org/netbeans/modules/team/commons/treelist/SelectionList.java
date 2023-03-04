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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ListUI;
import org.openide.util.Utilities;

/**
 * Special list displaying model items that provide custom renderers. Also paints
 * mouse-over effect.
 *
 * @author S. Aubrecht
 */
public final class SelectionList extends JList<ListNode> {

    static final int INSETS_LEFT = 5;
    static final int INSETS_TOP = 5;
    static final int INSETS_BOTTOM = 5;
    static final int INSETS_RIGHT = 5;

    private static final int MAX_VISIBLE_ROWS = 10;

    private static final String ACTION_SELECT = "selectProject"; // NOI18N
    private static final String ACTION_SHOW_POPUP = "showPopup"; // NOI18N

    private int mouseOverRow = -1;

    private final RendererImpl renderer = new RendererImpl();
    static final int ROW_HEIGHT = Math.max(16, new JLabel("X").getPreferredSize().height); // NOI18N
    private final ListListener nodeListener;

    public SelectionList() {
        setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setBorder( BorderFactory.createEmptyBorder() );
        setOpaque( false );
        setBackground( new Color(0,0,0,0) );
        setFixedCellHeight(ROW_HEIGHT + INSETS_TOP + INSETS_BOTTOM + 2);
        setCellRenderer(renderer);
        setVisibleRowCount( MAX_VISIBLE_ROWS );
        
        ToolTipManager.sharedInstance().registerComponent(this);

        addFocusListener( new FocusAdapter() {

            @Override
            public void focusGained( FocusEvent e ) {
                if( getSelectedIndex() < 0 && isShowing() && getModel().getSize() > 0 ) {
                    setSelectedIndex( 0 );
                }
            }
        });
        
        nodeListener = new ListListener() {
            @Override
            public void contentChanged(ListNode node) {
                int index = ((DefaultListModel) getModel()).indexOf(node);
                if (index >= 0) {
                    repaintRow(index);
                }
            }
            @Override
            public void contentSizeChanged(ListNode node) {
                // resize the whole dialog in case this is in one
                SelectionListModel model = (SelectionListModel) getModel();
                int index = model.indexOf(node);
                if (index >= 0) {
                    model.fireContentsChanged(node, index, index);
                    Container p = SelectionList.this;
                    while((p = p.getParent()) != null) {
                        if(p instanceof JDialog) {
                            invalidate();
                            revalidate();
                            ((JDialog)p).pack();
                            return;
                        }
                    }
                }
            }
        };

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mouseEntered( MouseEvent e ) {
                mouseMoved( e );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                setMouseOver( -1 );
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                int row = locationToIndex( e.getPoint() );
                setMouseOver( row );
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger() || e.isConsumed()) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    int index = locationToIndex(e.getPoint());
                    selectProjectAtIndex(index);
                }
            }
        };

        addMouseMotionListener( adapter );

        addMouseListener( adapter );

        initKeysAndActions();
    }

    private void selectProjectAtIndex(int index) {
        if (index >= 0 && index < getModel().getSize()) {
            Object value = getModel().getElementAt(index);
            if (value instanceof ListNode) {
                ActionListener al = ((ListNode)value).getDefaultAction();
                if (al != null) {
                    al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }
            }
        }
    }

    private void initKeysAndActions() {
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));

        InputMap imp = getInputMap();
        ActionMap am = getActionMap();
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_SELECT);
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), ACTION_SHOW_POPUP);
        am.put(ACTION_SELECT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectProjectAtIndex(getSelectedIndex());
            }
        });
        am.put(ACTION_SHOW_POPUP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = getSelectedIndex();
                if (index >= 0 && index < getModel().getSize()) {
                    showPopupMenuAt(index, getUI().indexToLocation(SelectionList.this, index));
                }
            }
        });
    }

    int getMouseOverRow() {
        return mouseOverRow;
    }

    private void setMouseOver( int newRow ) {
        int oldRow = mouseOverRow;
        mouseOverRow = newRow;
        repaintRow( oldRow );
        repaintRow( mouseOverRow );
    }

    private void repaintRow( int row ) {
        if( row >= 0 && row < getModel().getSize() ) {
            Rectangle rect = getCellBounds( row, row );
            if( null != rect )
                repaint( rect );
        }
    }

    @Override
    public void setUI(ListUI ui) {
        super.setUI(new SelectionListUI());
    }

    /**
     * Show popup menu from actions provided by node at given index (if any).
     *
     * @param rowIndex
     * @param location
     */
    private void showPopupMenuAt(int rowIndex, Point location) {
        ListNode node = getModel().getElementAt(rowIndex);
        Action[] actions = node.getPopupActions();

        if (null == actions || actions.length == 0) {
            return;
        }
        JPopupMenu popup = Utilities.actionsToPopup(actions, this);
        popup.show(this, location.x, location.y);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            Point p = event.getPoint();
            int index = locationToIndex(p);
            ListCellRenderer<? super ListNode> r = getCellRenderer();
            Rectangle cellBounds;

            if (index != -1 && r != null && (cellBounds =
                    getCellBounds(index, index)) != null
                    && cellBounds.contains(p.x, p.y)) {
                ListSelectionModel lsm = getSelectionModel();
                Component rComponent = r.getListCellRendererComponent(
                        this, getModel().getElementAt(index), index,
                        lsm.isSelectedIndex(index),
                        (hasFocus() && (lsm.getLeadSelectionIndex()
                        == index)));

                if (rComponent instanceof JComponent) {
                    rComponent.setBounds(cellBounds);
                    rComponent.doLayout();
                    MouseEvent newEvent;

                    p.translate(-cellBounds.x, -cellBounds.y);
                    newEvent = new MouseEvent(rComponent, event.getID(),
                            event.getWhen(),
                            event.getModifiers(),
                            p.x, p.y, event.getClickCount(),
                            event.isPopupTrigger());

                    String tip = ((JComponent) rComponent).getToolTipText(
                            newEvent);

                    if (tip != null) {
                        return tip;
                    }
                }
            }
        }
        return super.getToolTipText();
    }

    @Override
    public int getVisibleRowCount() {
        return Math.min( MAX_VISIBLE_ROWS, getModel().getSize() );
    }

    public void setItems( List<ListNode> items ) {
        SelectionListModel model = new SelectionListModel();
        for( ListNode item : items ) {
            model.addElement( item );
            item.setListener(nodeListener);
        }
        setModel( model );
    }    

    static class RendererImpl extends DefaultListCellRenderer {

        public RendererImpl() {
        }

        @Override
        public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            if (!(value instanceof ListNode)) {
                //shoudln't happen
                return new JLabel();
            }
            if( list instanceof SelectionList ) {
                isSelected |= index == ((SelectionList)list).getMouseOverRow();
            }
            ListNode node = (ListNode) value;
            int rowHeight = list.getFixedCellHeight();
            int rowWidth = list.getWidth();
            JScrollPane scroll = ( JScrollPane ) SwingUtilities.getAncestorOfClass( JScrollPane.class, list);
            if( null != scroll )
                rowWidth = scroll.getViewport().getWidth();
            Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();

            return node.getListRenderer(foreground, background, isSelected, cellHasFocus, rowHeight, rowWidth);
        }
    }

    private static class SelectionListUI extends AbstractListUI {

        @Override
        boolean showPopupAt( int rowIndex, Point location ) {
            JList list = this.list;
            if (!(list instanceof SelectionList)) {
                return false;
            }

            ((SelectionList) list).showPopupMenuAt(rowIndex, location);
            return true;
        }
    }
    
    private static class SelectionListModel extends DefaultListModel<ListNode> {
        @Override
        protected void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1); 
        }
    }
    
}
