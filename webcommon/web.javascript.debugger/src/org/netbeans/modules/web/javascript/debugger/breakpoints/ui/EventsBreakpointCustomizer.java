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
package org.netbeans.modules.web.javascript.debugger.breakpoints.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.breakpoints.EventsBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.HelpCtx;

/**
 *
 * @author Martin Entlicher
 */
public class EventsBreakpointCustomizer extends javax.swing.JPanel implements ControllerProvider, HelpCtx.Provider {

    private final EventsBreakpoint eb;
    private boolean createBreakpoint;
    private final CustomizerController controller;
    private final int checkBoxWidth = new JCheckBox().getPreferredSize().width;
    
    private static EventsBreakpoint createEventsBreakpoint(boolean[] createCheckPtr) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint b : breakpoints) {
            if (b instanceof EventsBreakpoint) {
                return (EventsBreakpoint) b;
            }
        }
        createCheckPtr[0] = true;
        return new EventsBreakpoint();
    }

    /**
     * Creates new form EventBreakpointCustomizer
     */
    public EventsBreakpointCustomizer() {
        this (new boolean[1]);
    }
    
    /** Helper constructor */
    private EventsBreakpointCustomizer(boolean[] createCheckPtr) {
        this (createEventsBreakpoint(createCheckPtr));
        this.createBreakpoint = createCheckPtr[0];
    }
    
    /**
     * Creates new form EventBreakpointCustomizer
     */
    public EventsBreakpointCustomizer(EventsBreakpoint eb) {
        this.eb = eb;
        initComponents();
        initLists();
        fillCategoryEvents();
        selectFirst();
        controller = new CustomizerController();
    }
    
    private void initLists() {
        categoryList.setCellRenderer(new CategoryCheckBoxCellRenderer());
        categoryList.addListSelectionListener(new CategorySelectionListener());
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addMouseListener(new CheckBoxMouseListener(true));
        actionList.setCellRenderer(new EventsCheckBoxCellRenderer());
        actionList.addMouseListener(new CheckBoxMouseListener(false));
    }
    
    private void fillCategoryEvents() {
        Set<String> allEventCategories = EventsBreakpoint.getAllEventCategories();
        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        for (String category : allEventCategories) {
            categoryModel.addElement(category);
        }
        categoryList.setModel(categoryModel);
    }
    
    private void fillEvents(String category) {
        DefaultListModel<String> eventsModel = new DefaultListModel<>();
        Set<String> allEvents = EventsBreakpoint.getAllEvents(category);
        for (String event : allEvents) {
            eventsModel.addElement(event);
        }
        actionList.setModel(eventsModel);
    }
    
    /** Find the first active event and select the appropriate category. */
    private void selectFirst() {
        Set<String> events = eb.getEvents();
        if (events.isEmpty()) {
            categoryList.setSelectedIndex(0);
            return ;
        }
        Set<String> allEventCategories = EventsBreakpoint.getAllEventCategories();
        for (String category : allEventCategories) {
            for (String event : EventsBreakpoint.getAllEvents(category)) {
                if (events.contains(event)) {
                    categoryList.setSelectedValue(category, true);
                    return;
                }
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoryLabel = new javax.swing.JLabel();
        categoryList = new javax.swing.JList();
        actionLabel = new javax.swing.JLabel();
        actionList = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(EventsBreakpointCustomizer.class, "EventsBreakpointCustomizer.categoryLabel.text")); // NOI18N

        categoryList.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(actionLabel, org.openide.util.NbBundle.getMessage(EventsBreakpointCustomizer.class, "EventsBreakpointCustomizer.actionLabel.text")); // NOI18N

        actionList.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryList, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(categoryLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actionLabel)
                    .addComponent(actionList, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryLabel)
                    .addComponent(actionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryList, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                    .addComponent(actionList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actionLabel;
    private javax.swing.JList actionList;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JList categoryList;
    // End of variables declaration//GEN-END:variables

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerEventsBreakpointJavaScript"); // NOI18N
    }
    
    private static JCheckBox createCheckBoxToRender() {
        JCheckBox chb = new JCheckBox();
        chb.setBorderPaintedFlat(true);
        return chb;
    }
    
    private static void setupCheckBoxToRender(JCheckBox chb, JList list, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            chb.setBackground(list.getSelectionBackground());
            chb.setForeground(list.getSelectionForeground());
        } else {
            chb.setBackground(list.getBackground());
            chb.setForeground(list.getForeground());
        }
    }
    
    private class CategoryCheckBoxCellRenderer implements ListCellRenderer {
        
        private JCheckBox chb;
        
        public CategoryCheckBoxCellRenderer() {
            chb = createCheckBoxToRender();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setupCheckBoxToRender(chb, list, isSelected, cellHasFocus);
            String category = (String) value;
            chb.setText(category);
            Set<String> allCategoryEvents = EventsBreakpoint.getAllEvents(category);
            boolean containsSome = false;
            boolean containsAll = true;
            for (String event : allCategoryEvents) {
                if (eb.hasEvent(event)) {
                    containsSome = true;
                } else {
                    containsAll = false;
                }
            }
            boolean mixed = (containsSome && !containsAll);
            chb.getModel().setArmed(mixed);
            chb.getModel().setPressed(mixed);
            chb.setSelected(containsAll);
            return chb;
        }
        
    }
    
    private class EventsCheckBoxCellRenderer implements ListCellRenderer {
        
        private JCheckBox chb;
        
        public EventsCheckBoxCellRenderer() {
            chb = createCheckBoxToRender();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setupCheckBoxToRender(chb, list, false, cellHasFocus);
            String event = (String) value;
            chb.setText(event);
            chb.setSelected(eb.hasEvent(event));
            return chb;
        }
        
    }
    
    private class CategorySelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = categoryList.getSelectedIndex();
            if (index >= 0) {
                String category = (String) categoryList.getModel().getElementAt(index);
                fillEvents(category);
            } else {
                actionList.setModel(new DefaultListModel());
            }
        }
        
    }
    
    private class CheckBoxMouseListener implements MouseListener {
        
        private boolean isCategory;
        
        CheckBoxMouseListener(boolean isCategory) {
            this.isCategory = isCategory;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (isCategory) {
                String category = getSelectedText(categoryList, e);
                if (category == null) {
                    return ;
                }
                Set<String> allCategoryEvents = EventsBreakpoint.getAllEvents(category);
                boolean containsAll = true;
                for (String event : allCategoryEvents) {
                    if (!eb.hasEvent(event)) {
                        containsAll = false;
                    }
                }
                if (!containsAll) { // SelectAll
                    for (String event : allCategoryEvents) {
                        eb.addEvent(event);
                    }
                } else { // Unselect all
                    for (String event : allCategoryEvents) {
                        eb.removeEvent(event);
                    }
                }
            } else {
                String event = getSelectedText(actionList, e);
                if (event == null) {
                    return ;
                }
                if (eb.hasEvent(event)) {
                    eb.removeEvent(event);
                } else {
                    eb.addEvent(event);
                }
            }
            categoryList.repaint();
            actionList.repaint();
        }
        
        private String getSelectedText(JList list, MouseEvent e) {
            int index = list.locationToIndex(e.getPoint());
            if (index >= 0) {
                int x = e.getX();
                if (x < checkBoxWidth) {
                    return (String) list.getModel().getElementAt(index);
                }
            }
            return null;
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
        
    }
    
    private class CustomizerController implements Controller {
        
        @Override
        public boolean ok() {
            if (createBreakpoint) {
                DebuggerManager.getDebuggerManager().addBreakpoint(eb);
            }
            return true;
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
    }
}
