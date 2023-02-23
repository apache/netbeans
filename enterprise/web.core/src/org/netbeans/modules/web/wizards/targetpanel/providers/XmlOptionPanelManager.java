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
package org.netbeans.modules.web.wizards.targetpanel.providers;

import java.awt.GridBagConstraints;

import javax.swing.JRadioButton;

import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.web.wizards.FileType;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
abstract class XmlOptionPanelManager extends AbstractOptionPanelManager {
    
    boolean isXml() {
        if ( getXmlSyntaxButton() == null ){
            return false;
        }
        return getXmlSyntaxButton().isSelected();
    }

    boolean isSegment() {
        if ( getSegmentBox() == null ){
            return false;
        }
        return getSegmentBox().isSelected();
    }

    @Override
    protected int initAdditionalSyntaxButton( int gridy, 
            final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel) 
    {
        myXmlSyntaxButton.setMnemonic(NbBundle.getMessage(XmlOptionPanelManager.class, 
                "A11Y_JspXml_mnem").charAt(0));
        getButtonGroup().add(myXmlSyntaxButton);
        myXmlSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxChanged(evt, panel, uiPanel);
            }
        });

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getOptionPanel().add(myXmlSyntaxButton, gridBagConstraints);
        return gridy;
    }

    @Override
    protected int initSyntaxButton( int grid , TargetChooserPanel<FileType> panel, 
            TargetChooserPanelGUI<FileType> uiPanel ) 
    {
        myXmlSyntaxButton = new javax.swing.JRadioButton();
        getJspSyntaxButton().setSelected(true);
        return grid;
    }
    
    protected JRadioButton getXmlSyntaxButton(){
        return myXmlSyntaxButton;
    }
    
    private JRadioButton myXmlSyntaxButton;

}
