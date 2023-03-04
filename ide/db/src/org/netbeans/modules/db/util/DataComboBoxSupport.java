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

package org.netbeans.modules.db.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This is an utility class for filling combo boxes with some data (usually
 * some items). The combo box has a "New item" item allowing
 * the user to invoke the adding of new items to the combo box. The client of
 * this class should provide a {@link DataComboBoxModel} and call
 * the {@link #connect} method.
 *
 * @author Andrei Badea
 */
public final class DataComboBoxSupport {
    private final DataComboBoxModel dataModel;
    private final boolean allowAdding;

    private Object previousItem = null;
    private Object previousNonSpecialItem = null;

    private boolean performingNewItemAction = false;

    /**
     * Serves as the new item. Not private because used in tests.
     */
    final Object NEW_ITEM = new Object() {
        @Override
        public String toString() {
            return dataModel.getNewItemDisplayName();
        }
    };

    /** Not private because used in tests. */
    DataComboBoxSupport(JComboBox comboBox, DataComboBoxModel dataModel, boolean allowAdding) {
        this.dataModel = dataModel;
        this.allowAdding = allowAdding;
        
        comboBox.setEditable(false);

        comboBox.setModel(new ItemComboBoxModel());

        comboBox.setRenderer(new ItemListCellRenderer());
        comboBox.addActionListener(new ItemActionListener());
        comboBox.addPopupMenuListener(new ItemPopupMenuListener());
    }

    /**
     * Connects a combo box with the specified combo box model.
     */
    public static void connect(JComboBox comboBox, DataComboBoxModel dataModel) {
        connect(comboBox, dataModel, true);
    }

    /**
     * Connects a combo box with the specified combo box model.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void connect(JComboBox comboBox, DataComboBoxModel dataModel, boolean allowAdding) {
        new DataComboBoxSupport(comboBox, dataModel, allowAdding);
    }

    private boolean isSpecialItem(Object item) {
        return item == NEW_ITEM;
    }

    private void setPreviousNonSpecialItem(JComboBox comboBox) {
        if (comboBox.getSelectedItem() == NEW_ITEM) {
            // no new item added
            comboBox.setSelectedItem(previousNonSpecialItem);
        }
    }

    private class ItemComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {

        // XXX intervalAdded() and intervalRemoved() are not implemented,
        // but it is enough for the connection and drivers combo boxes

        @SuppressWarnings("LeakingThisInConstructor")
        public ItemComboBoxModel() {
            getDelegate().addListDataListener(this);
        }

        @Override
        public Object getElementAt(int index) {
            if (allowAdding) {
                if (getSize() == 1) {
                    // there is just NEW_ITEM
                    if (index == 0) {
                        return NEW_ITEM;
                    } else {
                        throw new IllegalStateException("Index out of bounds: " + index); // NOI18N
                    }
                }

                // there are the delegate items and NEW_ITEM
                if (index >= 0 && index < getDelegate().getSize()) {
                    return getDelegate().getElementAt(index);
                } else if (index == getSize() - 1) {
                    return NEW_ITEM;
                } else {
                    throw new IllegalStateException("Index out of bounds: " + index); // NOI18N
                }
            } else {
                // there are no other items than those of the delegate
                return getDelegate().getElementAt(index);
            }
        }

        @Override
        public int getSize() {
            // 1 = NEW_ITEM
            if (allowAdding) {
                return getDelegate().getSize() == 0 ? 1 : getDelegate().getSize() + 1;
            } else {
                return getDelegate().getSize();
            }
        }

        @Override
        public void setSelectedItem(Object anItem) {
            previousItem = getDelegate().getSelectedItem();

            if (!isSpecialItem(previousItem)) {
                previousNonSpecialItem = previousItem;
            }

            getDelegate().setSelectedItem(anItem);
        }

        @Override
        public Object getSelectedItem() {
            return getDelegate().getSelectedItem();
        }

        public Object getPreviousItem() {
            return previousItem;
        }

        private ComboBoxModel getDelegate() {
            return dataModel.getListModel();
        }

        private int getItemIndex(Object item) {
            if (item == null) {
                return -1;
            }
            for (int i = 0; i < getSize(); i++ ) {
                if (getElementAt(i).equals(item)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            throw new UnsupportedOperationException("This is currently not supported.");
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            throw new UnsupportedOperationException("This is currently not supported.");
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    private class ItemListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            JLabel label = (JLabel)component;

            if (value != null && !isSpecialItem(value)) {
                String displayName = dataModel.getItemDisplayName(value);
                label.setText(dataModel.getItemDisplayName(value));
                label.setToolTipText(dataModel.getItemTooltipText(value));
            } else if (value != null) {
                label.setText(value.toString());
                label.setToolTipText(null);
            }

            return label;
        }
    }

    private final class ItemActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JComboBox comboBox = (JComboBox)e.getSource();

            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem == NEW_ITEM) {
                performingNewItemAction = true;
                try {
                    comboBox.setPopupVisible(false);
                    dataModel.newItemActionPerformed();
                } finally {
                    performingNewItemAction = false;
                }

                setPreviousNonSpecialItem(comboBox);
                // we (or maybe the client) have just selected an item inside an actionPerformed event,
                // which will not send another actionPerformed event for the new item. 
                // We need to make sure all listeners get an event for the new item,
                // thus...
                final Object newSelectedItem = comboBox.getSelectedItem();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        comboBox.setSelectedItem(newSelectedItem);
                    }
                });
            }
        }
    }

    private final class ItemPopupMenuListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (!performingNewItemAction) {
                setPreviousNonSpecialItem((JComboBox)e.getSource());
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            // without the check the previous non-special item would be displayed
            // while calling DataComboBoxModel.newItemActionPerformed() 
            // instead of NEW_ITEM, but this is unwanted. Same for
            // popupMenuWillBecomeImvisible().
            if (!performingNewItemAction) {
                setPreviousNonSpecialItem((JComboBox)e.getSource());
            }
        }
    }
}
