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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.math.BigDecimal;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class FilterMappingsTablePanel extends DefaultTablePanel {
    private FilterMappingsTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    private SectionView view;
    
    /** Creates new form FilterMappingsTablePanel */
    public FilterMappingsTablePanel(final SectionView view, final DDDataObject dObj, final FilterMappingsTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        this.view=view;
        webApp = dObj.getWebApp();
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int row = getTable().getSelectedRow();
                String filterName = (String)model.getValueAt(row,0);
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                model.removeRow(row);
                dObj.setChangedFromUI(false);
                // updating filter's panel title
                Filter filter = (Filter)webApp.findBeanByName("Filter","FilterName",filterName); //NOI18N
                if (filter!=null) {
                    SectionPanel panel = view.findSectionPanel(filter);
                    panel.setTitle(((FiltersMultiViewElement.FiltersView)view).getFilterTitle(filter));
                }
            }
        });
        addButton.addActionListener(new TableActionListener(true));
        editButton.addActionListener(new TableActionListener(false));
    }

    void setModel(WebApp webApp, FilterMapping[] mappings) {
        model.setData(webApp,mappings);
        this.webApp=webApp;
    }
    
    private static boolean isWebApp25(WebApp webApp) {
        BigDecimal ver = new BigDecimal(webApp.getVersion());
        return ver.compareTo(new BigDecimal(WebApp.VERSION_2_5)) >= 0;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String[] allFilters = DDUtils.getFilterNames(webApp);
            String[] allServlets = DDUtils.getServletNames(webApp);
            int row = (add?-1:getTable().getSelectedRow());
            FilterMapping mapping = null;
            if (add) {
                try {
                   mapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                } catch (ClassNotFoundException ex) {}
            } else {
                mapping = webApp.getFilterMapping(row);
            }
            final FilterMappingPanel dialogPanel = new FilterMappingPanel(
                    mapping,allFilters,allServlets , isWebApp25(webApp));
            final EditDialog dialog = new EditDialog(dialogPanel,
                NbBundle.getMessage(FilterMappingsTablePanel.class,
                    "TTL_filterMapping"),                       //NOI18N
                add) {
                protected String validate() {
                    if (!dialogPanel.hasFilterNames()){
                         return  NbBundle.getMessage(
                                 FilterMappingsTablePanel.class,
                                 "LBL_no_filters");             // NOI18N
                    }
                    String urlPattern = dialogPanel.getUrlPattern();
                    if ( urlPattern == null ){
                        urlPattern = "";
                    }
                    urlPattern = urlPattern.replace(',', ' ').trim();
                    if (dialogPanel.getUrlRB().isSelected() && 
                            urlPattern.length()==0)
                    {
                        return  NbBundle.getMessage(FilterMappingsTablePanel.class,
                                "TXT_missingURL");              //NOI18N
                    }
                    if ( dialogPanel.getServletNameRB().isSelected()){
                        String[] names = dialogPanel.getServletNames();
                        if ( names == null || names.length ==0 ){
                            return  NbBundle.getMessage(FilterMappingsTablePanel.class,
                                "TXT_missingServletName");      //NOI18N
                        }
                    }
                    return null;
                }
            };
            if (allFilters==null || allFilters.length==0 ) // Disable OK with error message
                dialog.checkValues();
            else if (add) // 
                dialog.setValid(false); // Disable OK
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getUrlTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getServletNamesList().getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent arg0) {
                    dialog.checkValues();
                }
            });
            dialogPanel.getUrlRB().addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialog.checkValues();
                }
            });
            dialogPanel.getServletNameRB().addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialog.checkValues();
                }
            });
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getUrlTF().getDocument().removeDocumentListener(docListener);
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String filterName = dialogPanel.getFilterName();
                String urlPattern = dialogPanel.getUrlPattern();
                String[] servletNames = dialogPanel.getServletNames();
                String[] dispatcher = dialogPanel.getDispatcherTypes();
                if (add) {
                    model.addRow(new Object[]{filterName,urlPattern,servletNames,
                        dispatcher});
                } else {
                    String oldName = (String)model.getValueAt(row,0);
                    model.editRow(row, new Object[]{filterName,urlPattern,
                        servletNames,dispatcher});
                    // udating title for filter panel with old name
                    if (!filterName.equals(oldName)) {
                        Filter filter = (Filter)webApp.findBeanByName("Filter",
                                "FilterName",oldName); //NOI18N
                        if (filter!=null) {
                            SectionPanel panel = view.findSectionPanel(filter);
                            panel.setTitle(((FiltersMultiViewElement.FiltersView)view).
                                    getFilterTitle(filter));
                        }
                    }
                }
                dObj.setChangedFromUI(false);
                // updating filter's panel title
                Filter filter = (Filter)webApp.findBeanByName("Filter","FilterName",
                        filterName); //NOI18N
                if (filter!=null) {
                    SectionPanel panel = view.findSectionPanel(filter);
                    panel.setTitle(((FiltersMultiViewElement.FiltersView)view).
                            getFilterTitle(filter));
                }
            }
        }
    }
}
