/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.git.ui.selectors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ItemSelector<I extends Item> implements ListSelectionListener {
    private final ItemsPanel panel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean deletesAllowed;

    public ItemSelector(String title) {
        panel = new ItemsPanel();
        panel.btnAllowDestructiveActions.setVisible(false);
        Mnemonics.setLocalizedText(panel.titleLabel, title); 
        panel.list.setCellRenderer(new ItemRenderer());
        attachListeners();
    }
    
    public JPanel getPanel() {
       return panel;
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void setBranches(List<I> branches) {
        Collections.sort(branches);
        DefaultListModel<I> model = new DefaultListModel<>();
        for (I i : branches) {
            model.addElement(i);
            if (i.isDestructive()) {
                panel.btnAllowDestructiveActions.setVisible(true);
            }
        }
        panel.list.setModel(model);        
        //inform listeners like select all/none buttons
        changeSupport.fireChange();
    }
    
    @SuppressWarnings("unchecked")
    public List<I> getSelectedBranches() {
        List<I> ret = new ArrayList<I>(panel.list.getModel().getSize());
        for (int i = 0; i < panel.list.getModel().getSize(); i++) {
            I item = (I)panel.list.getModel().getElementAt(i);
            if(item.isSelected) {
                ret.add(item);
            }
        }
        return ret;
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (e.getSource() == panel.list.getSelectionModel()) {
                changeSupport.fireChange();
            }
        }
    }

    public void setEnabled(boolean b) {
        panel.list.setEnabled(b);
        panel.titleLabel.setEnabled(b);
    }

    public boolean isEmpty() {
        return panel.list.getModel().getSize() == 0;
    }
    
    /**
     * Selects all or deselects all items in the list of items.
     *
     * @param newState true, if select all, false if select none
     */
    private void selectAll(boolean newState) {
        for (int i = 0; i < panel.list.getModel().getSize(); i++) {
            Item item = (Item) panel.list.getModel().getElementAt(i);
            // allways allow deselect but allow select only when the delete is allowed
            if (!newState || isSelectedStateAllowed(item)) {
                item.isSelected = newState;
            }
        }
        panel.list.repaint();
        changeSupport.fireChange();
    }
    
    private void attachListeners () {
        panel.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                switchSelection(panel.list.locationToIndex(e.getPoint()));
            }
        });
        panel.list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased (KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    switchSelection(panel.list.getSelectedIndex());
                }
            }
        });
     
        panel.btnSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(true);
            }
        });
        panel.btnSelectNone.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(false);
            }
        });
        panel.btnAllowDestructiveActions.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deletesAllowed = !deletesAllowed;
                boolean fireChange = false;
                if (!deletesAllowed) {
                    int maxItemsCount = panel.list.getModel().getSize();
                    for (int i = 0; i < maxItemsCount; i++) {
                        Item item = (Item) panel.list.getModel().getElementAt(i);
                        if (item.isDestructive()) {
                            fireChange = item.isSelected;
                            item.isSelected = false;
                        }
                    }
                }
                panel.list.repaint();
                if (deletesAllowed) {
                    Mnemonics.setLocalizedText(panel.btnAllowDestructiveActions, NbBundle.getMessage(ItemsPanel.class, "ItemsPanel.btnDisableDestructiveActions.text")); //NOI18N
                } else {
                    Mnemonics.setLocalizedText(panel.btnAllowDestructiveActions, NbBundle.getMessage(ItemsPanel.class, "ItemsPanel.btnAllowDestructiveActions.text")); //NOI18N
                }
                if (fireChange) {
                    changeSupport.fireChange();
                }
            }
        });

        changeSupport.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedItemsCount = 0;
                int maxItemsCount = panel.list.getModel().getSize();
                for (int i = 0; i < maxItemsCount; i++) {
                    Item item = (Item) panel.list.getModel().getElementAt(i);
                    if (item.isSelected) {
                        selectedItemsCount++;
                    }
                }
                //sync the buttons
                panel.btnSelectAll.setEnabled(selectedItemsCount<maxItemsCount);
                panel.btnSelectNone.setEnabled(selectedItemsCount>0);
            }
        });
    };   
    
    private void switchSelection(int index) {
        if (index != -1) {
            Item item = (Item) panel.list.getModel().getElementAt(index);
            if (isSelectedStateAllowed(item)) {
                item.isSelected = !item.isSelected;
                panel.list.repaint();
                changeSupport.fireChange();
            }
        }
    }
    
    private boolean isSelectedStateAllowed (Item item) {
        return !item.isDestructive() || deletesAllowed;
    }
    
    public class ItemRenderer implements ListCellRenderer {
        private JCheckBox renderer = new JCheckBox();
        private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
        public ItemRenderer() {
            renderer.setBorder(noFocusBorder);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            renderer.setBackground(list.getBackground());
            renderer.setForeground(list.getForeground());
            renderer.setEnabled(list.isEnabled());
            renderer.setFont(list.getFont());
            renderer.setFocusPainted(false);
            renderer.setBorderPainted(true);
            
            if(value instanceof ItemSelector.Item) {
                Item item = (Item) value;
                renderer.setText("<html>" + item.getText() + "</html>");
                renderer.setToolTipText(item.getTooltipText());
                renderer.setSelected(item.isSelected);
                renderer.setEnabled(!item.isDestructive() || deletesAllowed);
            }
            renderer.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return renderer;
        }
    }
    
    
    public abstract static class Item implements Comparable<Item> {
        boolean isSelected;
        private final boolean isDestructive;

        protected Item (boolean selected, boolean isDestructive) {
            this.isSelected = selected;
            this.isDestructive = isDestructive;
        }
        
        public abstract String getText();
        public abstract String getTooltipText();

        public final boolean isDestructive () {
            return isDestructive;
        }
    }
    
}
