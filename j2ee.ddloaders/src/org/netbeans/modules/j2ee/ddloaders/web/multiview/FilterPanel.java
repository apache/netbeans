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

import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.NbBundle;

/**
 * @author  mkuchtiak
 */
public class FilterPanel extends SectionInnerPanel {
    private DDDataObject dObj;
    private Filter filter;
    private javax.swing.JButton linkButton;
    private FilterParamsPanel filterParamsPanel;
    private FilterMappingsTablePanel filterMappingsPanel;
    
    /** Creates new form FilterPanel */
    public FilterPanel(SectionView sectionView, DDDataObject dObj,Filter filter) {
        super(sectionView);
        this.dObj=dObj;
        this.filter=filter;
        initComponents();

        // Filter Name
        filterNameTF.setText(filter.getFilterName());
        addValidatee(filterNameTF);
        
        // description
        Utils.makeTextAreaLikeTextField(descriptionTA,filterNameTF);
        descriptionTA.setText(filter.getDefaultDescription());
        addModifier(descriptionTA);
        

        // Init Params
        InitParamTableModel model = new InitParamTableModel();
        filterParamsPanel = new FilterParamsPanel(dObj, model);
        filterParamsPanel.setModel(filter,filter.getInitParam());       
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 0);
        add(filterParamsPanel, gridBagConstraints);
        
        filterClassTF.setText(filter.getFilterClass());
        addValidatee(filterClassTF);
 
        linkButton = new LinkButton(this, filter, "ClassName"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkButton, NbBundle.getMessage(FilterPanel.class, "LBL_goToSource"));
        add(linkButton, gridBagConstraints);
        setAccessibility();
    }
    
    private void setAccessibility() {
        filterParamsLabel.setLabelFor(filterParamsPanel.getTable());
    }
    
    public void linkButtonPressed(Object ddBean, String property) {
        if ("ClassName".equals(property)) { // NOI18N
            DDUtils.openEditorFor(dObj,((Filter)ddBean).getFilterClass());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        filterNameLabel = new javax.swing.JLabel();
        filterNameTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();
        filterClassLabel = new javax.swing.JLabel();
        filterClassTF = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filler = new javax.swing.JPanel();
        filterParamsLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        filterNameLabel.setLabelFor(filterNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(filterNameLabel, org.openide.util.NbBundle.getMessage(FilterPanel.class, "LBL_filterName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(filterNameLabel, gridBagConstraints);

        filterNameTF.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(filterNameTF, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTA);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(FilterPanel.class, "LBL_description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(descriptionLabel, gridBagConstraints);

        descriptionTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(descriptionTA, gridBagConstraints);

        filterClassLabel.setLabelFor(filterClassTF);
        org.openide.awt.Mnemonics.setLocalizedText(filterClassLabel, org.openide.util.NbBundle.getMessage(FilterPanel.class, "LBL_filterClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(filterClassLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(filterClassTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(FilterPanel.class, "LBL_browse")); // NOI18N
        browseButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        add(browseButton, gridBagConstraints);

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(filterParamsLabel, org.openide.util.NbBundle.getMessage(FilterPanel.class, "LBL_initParams")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        add(filterParamsLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String className = DDUtils.getResourcePath(groups,fo);
                if (className.length()>0 && !className.equals(filterClassTF.getText())) {
                    dObj.modelUpdatedFromUI();
                    dObj.setChangedFromUI(true);
                    filterClassTF.setText(className);
                    filter.setFilterClass(className);
                    dObj.setChangedFromUI(false);
                    getSectionView().checkValidity();
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel filterClassLabel;
    private javax.swing.JTextField filterClassTF;
    private javax.swing.JLabel filterNameLabel;
    private javax.swing.JTextField filterNameTF;
    private javax.swing.JLabel filterParamsLabel;
    // End of variables declaration//GEN-END:variables
    
    public javax.swing.JComponent getErrorComponent(String name) {
        if ("FilterName".equals(name)) return filterNameTF; //NOI18N
        else if ("FilterClass".equals(name)) return filterClassTF; //NOI18N
        return null;
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==filterNameTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Filter Name",filterNameTF));
                return;
            }
            Filter[] filters = dObj.getWebApp().getFilter();
            for (int i=0;i<filters.length;i++) {
                if (filter!=filters[i] && val.equals(filters[i].getFilterName())) {
                    getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, val, filterNameTF));
                    return;
                }
            }
            getSectionView().getErrorPanel().clearError();
        } else if (comp==filterClassTF) {
            String text = (String)value;
            if (text.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Filter Class",filterClassTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==filterNameTF) {
            String text = (String)value;
            // change Filter-mappings
            FilterMapping[] maps = DDUtils.getFilterMappings(dObj.getWebApp(), filter);
            for (int i=0;i<maps.length;i++) {
                maps[i].setFilterName(text);
            }
            // change Filter-name
            filter.setFilterName(text);
            //change panel title, node name
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(filter);
            enclosingPanel.setTitle(((FiltersMultiViewElement.FiltersView)getSectionView()).getFilterTitle(filter));
            enclosingPanel.getNode().setDisplayName(text);
        } else if (source==filterClassTF) {
            String text = (String)value;
            filter.setFilterClass(text.length()==0?null:text);
        } else if (source==descriptionTA) {
            String text = (String)value;
            filter.setDescription(text.length()==0?null:text);
        }
    }
    
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source==filterNameTF) {
            filterNameTF.setText(filter.getFilterName());
        } else if (source==filterClassTF) {
            filterClassTF.setText(filter.getFilterClass());
        }
    }
    
    /** This will be called before model is changed from this panel
     */
    @Override
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    @Override
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
 }
