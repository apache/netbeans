/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.scenebuilder.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.*;
import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public class GrowingComboBox extends javax.swing.JPanel {
    private static final String SEPARATOR = "---";
    final public static class GrowingListModel<T> implements ComboBoxModel {
        final private Set<ListDataListener> listeners = new CopyOnWriteArraySet<ListDataListener>();
        final private List<T> predefinedList = new ArrayList<T>();
        final private List<T> userList = new ArrayList<T>();
        
        private T selected = null;
        private Action growAction;
        
        public GrowingListModel() {
            this.growAction = new AbstractAction("Browse...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // ignore
                }
            };
        }
        
        void setGrowAction(Action ga) {
            this.growAction = ga;
            fireDataChanged();
        }
        
        Action getGrowAction() {
            return this.growAction;
        }
        
        @Override
        final public Object getSelectedItem() {
            return selected;
        }

        @Override
        @SuppressWarnings("unchecked")
        final public void setSelectedItem(final Object anItem) {
            if (anItem != null) {
                if (anItem.equals(SEPARATOR)) return;
                if (anItem instanceof Action) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ((Action)anItem).actionPerformed(new ActionEvent(this, 0, "grow"));
                        }
                    });
                    
                } else {
                    selected = (T)anItem;
                }
            }
        }

        @Override
        final public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        final public Object getElementAt(int index) {
            int sep1 = getSep1Pos();
            int sep2 = getSep2Pos();
            
            if (index < sep1) {
                return predefinedList.get(index);
            }
            if (index == sep1)  return SEPARATOR;
            if (index < sep2) {
                return userList.get(index - sep1 - 1);
            }
            if (index == sep2)  return SEPARATOR;
            if (index == getActionPos()) return growAction;
            
            throw new IndexOutOfBoundsException();
        }

        @Override
        final public int getSize() {
            return getActionPos() + 1;
        }

        @Override
        final public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
        
        final public List<T> getPredefined() {
            return Collections.unmodifiableList(predefinedList);
        }
        
        final public List<T> getUserDefined() {
            return Collections.unmodifiableList(userList);
        }
        
        final public void setPredefined(List<T> predefined) {
            int stop = getSep1Pos();
            predefinedList.clear();
            if (stop > 0) {
                fireDataRemoved(0, stop);
            }
            predefinedList.addAll(predefined);
            stop = getSep1Pos();
            if (selected == null && !predefinedList.isEmpty()) selected = predefinedList.get(0);
            fireDataAdded(0, stop > 0 ? stop : 0);
        }
        
        final public void setUserDefined(List<T> user) {
            int stop = getSep2Pos();
            userList.clear();
            if (stop > 0) {
                fireDataRemoved(getUserListStartPos(), stop);
            }
            userList.addAll(user);
            stop = getSep2Pos();
            if (selected == null && !userList.isEmpty()) selected = userList.get(0);
            fireDataAdded(getUserListStartPos(), stop);
        }
        
        final public void addPredefined(T ... predefined) {
            int stop1 = getSep1Pos();
            predefinedList.addAll(Arrays.asList(predefined));
            int stop2 = getSep1Pos();
            if (selected == null && !predefinedList.isEmpty()) selected = predefinedList.get(0);
            fireDataAdded(stop1 > -1 ? stop1 : 0, stop2);
        }
        
        final public void addUserDefined(T ... userdefined)  {
            int stop1 = getSep2Pos();
            userList.addAll(Arrays.asList(userdefined));
            int stop2 = getSep2Pos();
            if (selected == null && !userList.isEmpty()) selected = userList.get(0);
            fireDataAdded(stop1 > -1 ? stop1 : 0, stop2);
        }
        
        private void fireDataAdded(int start, int stop) {
            if (stop <= start) return;
            for(ListDataListener l : listeners) {
                l.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, start, stop));
            }
        }
        
        private void fireDataRemoved(int start, int stop) {
            for(ListDataListener l : listeners) {
                l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, start, stop));
            }
        }
        
        private void fireDataChanged() {
            int stop = getSize() - 1;
            for(ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, stop));
            }
        }
        
        private int getUserListStartPos() {
            return getSep1Pos() + 1;
        }
        
        private int getActionPos() {
            return getSep2Pos() + 1;
        }
        
        private int getSep1Pos() {
            if (predefinedList.isEmpty()) return -1;
            return predefinedList.size();
        }
        
        private int getSep2Pos() {
            if (predefinedList.isEmpty() && userList.isEmpty()) return -1;
            if (predefinedList.isEmpty()) return userList.size();
            if (userList.isEmpty()) return predefinedList.size();
            return predefinedList.size() + userList.size() + 1;
        }
    }

    private String nullSelectionMessage = "<null>";
    
    /**
     * Creates new form Combo1
     */
    public GrowingComboBox() {
        initComponents();
        combo.setRenderer(new DefaultListCellRenderer(){
            private JSeparator separator;

            {
                separator = new JSeparator(JSeparator.HORIZONTAL);
            }

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                if (SEPARATOR.equals(value)) {
                    return separator;
                }
                
                String s = "";
                if (value != null) {
                    if (value instanceof Action) {
                        s = (String)((Action)value).getValue(Action.NAME);
                    } else {
                        s = value.toString();
                    }
                } else {
                    s = nullSelectionMessage;
                }
                return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
            }
        });
        combo.setModel(new GrowingListModel());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        combo = new javax.swing.JComboBox();

        combo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combo, 0, 155, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox combo;
    // End of variables declaration//GEN-END:variables

    //<editor-fold defaultstate="collapsed" desc="Delegating to JComboBox">
    // ### Start ### Delegating to JComboBox
    public void showPopup() {
        combo.showPopup();
    }
    
    public void setSelectedItem(Object anObject) {
        combo.setSelectedItem(anObject);
    }
    
    public void setSelectedIndex(int anIndex) {
        combo.setSelectedIndex(anIndex);
    }
    
    public void setPrototypeDisplayValue(Object prototypeDisplayValue) {
        combo.setPrototypeDisplayValue(prototypeDisplayValue);
    }
    
    public void setPopupVisible(boolean v) {
        combo.setPopupVisible(v);
    }
    
    public void setMaximumRowCount(int count) {
        combo.setMaximumRowCount(count);
    }
    
    public void setLightWeightPopupEnabled(boolean aFlag) {
        combo.setLightWeightPopupEnabled(aFlag);
    }
    
    public void setKeySelectionManager(KeySelectionManager aManager) {
        combo.setKeySelectionManager(aManager);
    }
    
    public void setEnabled(boolean b) {
        combo.setEnabled(b);
    }
    
    public void setEditor(ComboBoxEditor anEditor) {
        combo.setEditor(anEditor);
    }
    
    public void setEditable(boolean aFlag) {
        combo.setEditable(aFlag);
    }
    
    public boolean selectWithKeyChar(char keyChar) {
        return combo.selectWithKeyChar(keyChar);
    }
    
    public void removePopupMenuListener(PopupMenuListener l) {
        combo.removePopupMenuListener(l);
    }
    
    public void removeItemListener(ItemListener aListener) {
        combo.removeItemListener(aListener);
    }
    
    public void removeActionListener(ActionListener l) {
        combo.removeActionListener(l);
    }
    
    public void processKeyEvent(KeyEvent e) {
        combo.processKeyEvent(e);
    }
    
    public boolean isPopupVisible() {
        return combo.isPopupVisible();
    }
    
    public boolean isLightWeightPopupEnabled() {
        return combo.isLightWeightPopupEnabled();
    }
    
    public boolean isEditable() {
        return combo.isEditable();
    }
    
    public void hidePopup() {
        combo.hidePopup();
    }
    
    public Object[] getSelectedObjects() {
        return combo.getSelectedObjects();
    }
    
    public Object getSelectedItem() {
        return combo.getSelectedItem();
    }
    
    public int getSelectedIndex() {
        return combo.getSelectedIndex();
    }
    
    public Object getPrototypeDisplayValue() {
        return combo.getPrototypeDisplayValue();
    }
    
    public PopupMenuListener[] getPopupMenuListeners() {
        return combo.getPopupMenuListeners();
    }
    
    public int getMaximumRowCount() {
        return combo.getMaximumRowCount();
    }
    
    public KeySelectionManager getKeySelectionManager() {
        return combo.getKeySelectionManager();
    }
    
    public ItemListener[] getItemListeners() {
        return combo.getItemListeners();
    }
    
    public int getItemCount() {
        return combo.getItemCount();
    }
    
    public Object getItemAt(int index) {
        return combo.getItemAt(index);
    }
    
    public void firePopupMenuWillBecomeVisible() {
        combo.firePopupMenuWillBecomeVisible();
    }
    
    public void firePopupMenuWillBecomeInvisible() {
        combo.firePopupMenuWillBecomeInvisible();
    }
    
    public void firePopupMenuCanceled() {
        combo.firePopupMenuCanceled();
    }
    
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        combo.configureEditor(anEditor, anItem);
    }
    
    public void addPopupMenuListener(PopupMenuListener l) {
        combo.addPopupMenuListener(l);
    }
    
    public void addItemListener(ItemListener aListener) {
        combo.addItemListener(aListener);
    }
    
    public void addActionListener(ActionListener l) {
        combo.addActionListener(l);
    }
    
    public void actionPerformed(ActionEvent e) {
        combo.actionPerformed(e);
    }
    
    // ### End ### Delegating to JComboBox
    //</editor-fold>
    
    public <T> void setModel(GrowingListModel<T> model) {
        combo.setModel(model);
    }
    
    @SuppressWarnings("unchecked")
    public <T> GrowingListModel<T> getModel() {
        return (GrowingListModel<T>)combo.getModel();
    }
    
    public void setNullSelectionMessage(String msg) {
        nullSelectionMessage = msg;
        getModel().fireDataChanged();
    }
    
    public String getNullSelectionMessage() {
        return nullSelectionMessage;
    }
    
    public Action getGrowAction() {
        return getModel().getGrowAction();
    }
    
    public void setGrowAction(Action growAction) {
        getModel().setGrowAction(growAction);
    }
}
