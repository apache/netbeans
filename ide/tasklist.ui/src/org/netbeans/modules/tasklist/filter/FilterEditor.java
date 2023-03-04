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

package org.netbeans.modules.tasklist.filter;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  sa154850
 */
public class FilterEditor extends JPanel implements PropertyChangeListener {
    
    private HashMap<KeywordsFilter, KeywordsPanel> filter2keywords = new HashMap<KeywordsFilter, KeywordsPanel>(10);
    private HashMap<TypesFilter, TypesPanel> filter2types = new HashMap<TypesFilter, TypesPanel>(10);
    
    /** Reference to orginal filterRepository this dialog act upon.
     * It is not changed until ok or apply is pressed */
    private FilterRepository filterRepository;
    /**
     * Contains temporary data (cloned filters) for the list and also selection
     * model for the list.
     */
    private FilterModel filterModel;
    
    private JButton btnOk;
    private JButton btnCancel;
    
    /** Creates new form FilterEditor */
    public FilterEditor( FilterRepository filters ) {
        initComponents();
        this.filterRepository = filters;
        if( filterRepository.size() == 0 )
            filterRepository.add( filterRepository.createNewFilter() );
        this.filterModel = new FilterModel( filterRepository );
        
        init();
    }
    
    public boolean showWindow() {
        DialogDescriptor dd = new DialogDescriptor( this, NbBundle.getMessage( FilterEditor.class, "LBL_FilterEditor" ), true, //NOI18N
                new Object[] { btnOk, btnCancel }, btnOk, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, null );
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog( dd );
        
        dlg.setVisible(true);
        if( btnOk.equals( dd.getValue() ) ) {
            updateFilters();
            return true;
        }
        return false;
    }

    private void init() {
        initComponents();
        
        // init filters-listbox model
        lstFilters.setModel(filterModel);
        lstFilters.setSelectionModel(filterModel.selection);
        lstFilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // try to select just applied filter
        
        TaskFilter selected = filterRepository.getActive();
        if( null != selected ) {
            int selIndex = filterModel.getIndexOf( selected );
            lstFilters.setSelectedIndex( selIndex );
        }
        
        if (filterModel.getSelectedIndex() == -1) {
            if (filterModel.getSize() > 0) {
                lstFilters.setSelectedIndex(0);
            }
        }
        
        txtFilterName.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(e);}
            public void insertUpdate(DocumentEvent e) { update(e);}
            public void removeUpdate(DocumentEvent e) { update(e);}
            
            private void update(DocumentEvent e) {
                try {
                    filterModel.setCurrentFilterName(e.getDocument().getText(0, e.getDocument().getLength()));
                    propertyChange(null);
                } catch (BadLocationException ex) {
                    //ignore
                }
            }
        });
        
        
        // hook list selection
        lstFilters.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showFilter(filterModel.getSelectedFilter());
                    btnRemoveFilter.setEnabled( filterModel.getSelectedIndex() != -1 );
                    txtFilterName.setEnabled( filterModel.getSelectedIndex() != -1 );
                }
            }
        });
        
        showFilter(filterModel.getSelectedFilter());
        btnRemoveFilter.setEnabled(filterModel.getSelectedIndex() != -1);
        
        
        btnOk= new JButton( NbBundle.getMessage(FilterEditor.class, "btnOk") ); //NOI18N
        btnCancel = new JButton( NbBundle.getMessage(FilterEditor.class, "btnCancel") ); //NOI18N
    }
    
    /**
     * Initializes the editor to the state when <filter> is selected in the list
     * and it is shown on the right side. It can be used to propagate values in
     * both directions - from list to pane and opposite and also to both at once.
     */
    private void showFilter(final TaskFilter filter) {
        if( null == filter ) {
            txtFilterName.setText( null );
            panelKeywords.removeAll();
            panelTypes.removeAll();
            panelKeywords.add( new KeywordsPanel(null), BorderLayout.CENTER );
            panelTypes.add( new TypesPanel(null), BorderLayout.CENTER );
        } else {
            KeywordsPanel kPanel = filter2keywords.get( filter.getKeywordsFilter() );
            if( kPanel == null ) {
                kPanel = new KeywordsPanel( filter.getKeywordsFilter() == null ? new KeywordsFilter() : filter.getKeywordsFilter() );
                filter2keywords.put( filter.getKeywordsFilter(), kPanel );
                kPanel.addPropertyChangeListener( FilterCondition.PROP_VALUE_VALID, this );
            }

            panelKeywords.removeAll();
            panelKeywords.add( kPanel, BorderLayout.CENTER );
            kPanel.setVisible( true );

            TypesPanel tPanel = filter2types.get( filter.getTypesFilter() );
            if( tPanel == null ) {
                tPanel = new TypesPanel( filter.getTypesFilter() == null ? new TypesFilter() : filter.getTypesFilter() );
                filter2types.put( filter.getTypesFilter(), tPanel );
                tPanel.addPropertyChangeListener( FilterCondition.PROP_VALUE_VALID, this );
            }

            panelTypes.removeAll();
            panelTypes.add( tPanel,BorderLayout.CENTER );
            tPanel.setVisible( true );
            
            tabs.requestFocus();
            
            // select the active filter
            if (filterModel.getSelectedFilter() != filter) { // check to prevent cycle in notifications
                lstFilters.setSelectedIndex(filterModel.getIndexOf(filter));
            }

            txtFilterName.setText( filter.getName() );
        }
        
        panelKeywords.validate();
        panelKeywords.repaint();
        panelTypes.validate();
        panelTypes.repaint();
    }
    
    /**
     * Lift of isValueValid to FiltersPanel
     */
    public boolean isValueValid() {
        for( KeywordsPanel fp : filter2keywords.values() ) {
            if( !fp.isValueValid() ) 
                return false;
        }
        for( TypesPanel tp : filter2types.values() ) {
            if( !tp.isValueValid() ) 
                return false;
        }
        if( txtFilterName.getText().length() == 0 )
            return false;
        return true;
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        if( null != btnOk )
            btnOk.setEnabled( isValueValid() );
    }
    
    /**
     * Reads data from the form into the filter repository
     * that was passed-in in the constructor (returned by {@link #getFilterRepository})
     */
    void updateFilters() {
        filterRepository.clear();             // throw away all original filters
        
        Iterator filterIt = filterModel.iterator();
        while (filterIt.hasNext()) {
            TaskFilter f = (TaskFilter)filterIt.next();
            if( filter2keywords.get( f.getKeywordsFilter() ) != null )
                f.setKeywordsFilter( filter2keywords.get( f.getKeywordsFilter() ).getFilter() ); // has panel, was touched
            
            if( filter2types.get( f.getTypesFilter() ) != null )
                f.setTypesFilter( filter2types.get( f.getTypesFilter() ).getFilter() ); // has panel, was touched
            
            filterRepository.add( f );
        }
        if( filterModel.getSelectedFilter()!= null ) {
            filterRepository.setActive(filterModel.getSelectedFilter());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFilters = new javax.swing.JLabel();
        scrollFilters = new javax.swing.JScrollPane();
        lstFilters = new javax.swing.JList();
        btnNewFilter = new javax.swing.JButton();
        btnRemoveFilter = new javax.swing.JButton();
        tabs = new javax.swing.JTabbedPane();
        panelTypes = new javax.swing.JPanel();
        panelKeywords = new javax.swing.JPanel();
        lblFilterName = new javax.swing.JLabel();
        txtFilterName = new javax.swing.JTextField();

        lblFilters.setLabelFor(lstFilters);
        org.openide.awt.Mnemonics.setLocalizedText(lblFilters, org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.lblFilters.text")); // NOI18N

        lstFilters.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstFilters.setToolTipText(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.lstFilters.toolTipText")); // NOI18N
        scrollFilters.setViewportView(lstFilters);

        org.openide.awt.Mnemonics.setLocalizedText(btnNewFilter, org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.btnNewFilter.text")); // NOI18N
        btnNewFilter.setToolTipText(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.btnNewFilter.toolTipText")); // NOI18N
        btnNewFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onNewFilter(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveFilter, org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.btnRemoveFilter.text")); // NOI18N
        btnRemoveFilter.setToolTipText(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.btnRemoveFilter.toolTipText")); // NOI18N
        btnRemoveFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onRemoveFilter(evt);
            }
        });

        panelTypes.setOpaque(false);
        panelTypes.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.panelTypes.TabConstraints.tabTitle"), panelTypes); // NOI18N

        panelKeywords.setOpaque(false);
        panelKeywords.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.panelKeywords.TabConstraints.tabTitle"), panelKeywords); // NOI18N

        lblFilterName.setLabelFor(txtFilterName);
        org.openide.awt.Mnemonics.setLocalizedText(lblFilterName, org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.lblFilterName.text")); // NOI18N

        txtFilterName.setToolTipText(org.openide.util.NbBundle.getMessage(FilterEditor.class, "FilterEditor.txtFilterName.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFilters)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNewFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveFilter))
                    .addComponent(scrollFilters))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFilterName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFilterName, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFilters)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollFilters, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewFilter)
                            .addComponent(btnRemoveFilter)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFilterName)
                            .addComponent(txtFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void onRemoveFilter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onRemoveFilter
    int i = filterModel.getSelectedIndex();
    if (i != -1) {
        TaskFilter f = filterModel.get(i);
        filterModel.remove(i);
        filter2keywords.remove( f.getKeywordsFilter() );
        filter2types.remove( f.getTypesFilter() );
    }
    
}//GEN-LAST:event_onRemoveFilter

private void onNewFilter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onNewFilter
    TaskFilter f = filterRepository.createNewFilter();
    filterModel.add( f );
    showFilter( f );
    
}//GEN-LAST:event_onNewFilter


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewFilter;
    private javax.swing.JButton btnRemoveFilter;
    private javax.swing.JLabel lblFilterName;
    private javax.swing.JLabel lblFilters;
    private javax.swing.JList lstFilters;
    private javax.swing.JPanel panelKeywords;
    private javax.swing.JPanel panelTypes;
    private javax.swing.JScrollPane scrollFilters;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTextField txtFilterName;
    // End of variables declaration//GEN-END:variables
    
    
    private static class FilterModel extends AbstractListModel {
        
        public DefaultListSelectionModel selection = new DefaultListSelectionModel();
        public ArrayList<TaskFilter> filters;
        
        public FilterModel( FilterRepository rep ) {
            filters = new ArrayList<TaskFilter>( rep.size() * 2 );
            int selectedi = 0;
            for( TaskFilter f : rep.getFilters() ) {
                if( f == rep.getActive() )
                    selection.setSelectionInterval(selectedi, selectedi);
                filters.add( (TaskFilter)f.clone() );
                selectedi++;
            }
        }
        
        public Iterator iterator() {
            return filters.iterator();
        }
        
        public Object getElementAt(int index) {
            return filters.get(index).getName();
        }
        
        public int getSize() {
            return filters.size();
        }
        
        public TaskFilter getSelectedFilter() {
            if (getSelectedIndex() > -1) {
                return filters.get( getSelectedIndex() );
            } else
                return null;
        }
        
        public int getSelectedIndex() {
            int i1 = selection.getMinSelectionIndex(), i2 = selection.getMaxSelectionIndex();
            if (i1 == i2 && i1 >= 0 && i1 < filters.size()) {
                return i1;
            } else {
                return -1;
            }
        }
        
        public void remove(int i) {
            int s = getSelectedIndex();
            if (s != -1) {
                filters.remove(i);
                fireIntervalRemoved(this, i, i);
                
                if (i < s) {
                    selection.setSelectionInterval(s-1, s-1);
                } if (i == s) {
                    selection.setSelectionInterval(100,0);
                }
            }
        }
        
        public TaskFilter get(int i ) {
            return (TaskFilter)filters.get(i);
        }
        
        public boolean add(TaskFilter f) {
            if (filters.add(f)) {
                fireIntervalAdded(this, filters.size()-1, filters.size()-1);
                return true;
            } else
                return false;
        }
        
        public int getIndexOf(TaskFilter f) {
            return filters.indexOf(f);
        }
        
        public void setCurrentFilterName(String name) {
            int selIndex = getSelectedIndex();
            if( selIndex >= 0 ) {
                get(selIndex).setName(name);
                fireContentsChanged(this, selIndex, selIndex);
            }
        }
    }
}
