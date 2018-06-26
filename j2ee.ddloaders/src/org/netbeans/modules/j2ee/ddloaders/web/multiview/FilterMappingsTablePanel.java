/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
