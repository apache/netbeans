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
package org.netbeans.modules.web.wizards.targetpanel.providers;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;

import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelUIManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.wizards.FileType;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class AbstractOptionPanelManager implements TargetPanelUIManager<FileType> {

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initValues(org.netbeans.modules.target.iterator.api.TargetChooserPanel, org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI)
     */
    public void initValues( TargetChooserPanel<FileType> panel,
            TargetChooserPanelGUI<FileType> uiPanel )
    {
        if (panel.getSourceGroups()!=null && 
                panel.getSourceGroups().length>0) 
        {
            myWebModule = WebModule.getWebModule(panel.getSourceGroups()[0].
                    getRootFolder());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#changeUpdate(javax.swing.event.DocumentEvent, org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void changeUpdate( DocumentEvent e , TargetChooserPanel<FileType> panel) {
    }

    public void initFolderValue( TargetChooserPanel<FileType> panel, String target,
            JTextField field )
    {
        field.setText( target == null ? "" : target ); // NOI18N         
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getOptionPanel()
     */
    public JPanel getOptionPanel() {
        return myOptionsPanel;
    }

    public void initComponents( JPanel mainPanel , final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel ) 
    {
        doInitComponents(mainPanel, panel, uiPanel );
    }
    
    protected int doInitComponents( JPanel mainPanel ,final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel) {
        int gridy=0;
        
        myButtonGroup = new javax.swing.ButtonGroup();
        myDescriptionArea = new javax.swing.JTextArea();
        mySegmentBox = new javax.swing.JCheckBox();
        int segmenty;
        myDescriptionLabel = new javax.swing.JLabel();
        myOptionLabel = new javax.swing.JLabel();
        myJspSyntaxButton = new javax.swing.JRadioButton();
        
        myOptionsPanel = new javax.swing.JPanel();

        myOptionsPanel.setLayout(new GridBagLayout());

        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        //gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(myOptionsPanel, gridBagConstraints);
        
        mySegmentBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxChanged(evt, panel , uiPanel );
            }
        });

        myOptionLabel.setText(NbBundle.getMessage(AbstractOptionPanelManager.class, 
                "LBL_Options"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        myOptionsPanel.add(myOptionLabel, gridBagConstraints);

        gridy = initSyntaxButton( gridy, panel , uiPanel);
        
        myButtonGroup.add(myJspSyntaxButton);
        myJspSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxChanged(evt, panel , uiPanel );
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        segmenty = gridy;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        myOptionsPanel.add(myJspSyntaxButton, gridBagConstraints);

        gridy = initAdditionalSyntaxButton( gridy ,panel , uiPanel );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = segmenty;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        mySegmentBox.setMnemonic(NbBundle.getMessage(AbstractOptionPanelManager.class, 
                "A11Y_JspSegment_mnem").charAt(0));
        myOptionsPanel.add(mySegmentBox, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        myOptionsPanel.add(new javax.swing.JPanel(), gridBagConstraints);
        
        myDescriptionLabel.setText(NbBundle.getMessage(AbstractOptionPanelManager.class, 
                "LBL_description"));
        myDescriptionLabel.setDisplayedMnemonic(NbBundle.getMessage(AbstractOptionPanelManager.class, 
                "A11Y_Description_mnem").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        myDescriptionLabel.setLabelFor(myDescriptionArea);
        mainPanel.add(myDescriptionLabel, gridBagConstraints);

        myDescriptionArea.setEditable(false);
        myDescriptionArea.setLineWrap(true);
        myDescriptionArea.setRows(2);
        myDescriptionArea.setWrapStyleWord(true);
        myDescriptionArea.setOpaque(false);
        myDescriptionArea.getAccessibleContext().setAccessibleDescription(myDescriptionLabel.getText());
        myDescriptionArea.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 2.0;
        mainPanel.add(myDescriptionArea, gridBagConstraints);
        
        return gridy;
    }
    
    protected JCheckBox getSegmentBox(){
        return mySegmentBox;
    }
    
    protected ButtonGroup getButtonGroup(){
        return myButtonGroup;
    }
    
    protected JRadioButton getJspSyntaxButton(){
        return myJspSyntaxButton;
    }
    
    protected JTextArea getDescription(){
        return myDescriptionArea;
    }
    
    protected WebModule getWebModule(){
        return myWebModule;
    }
    
    protected abstract int initAdditionalSyntaxButton( int grid, 
            TargetChooserPanel<FileType> panel, TargetChooserPanelGUI<FileType> uiPanel );

    protected abstract int initSyntaxButton( int grid ,TargetChooserPanel<FileType> panel ,
            TargetChooserPanelGUI<FileType> uiPanel);

    protected abstract void checkBoxChanged(ItemEvent evt, TargetChooserPanel<FileType> panel ,
            TargetChooserPanelGUI<FileType> uiPanel ) ;

    private JPanel myOptionsPanel;
    private ButtonGroup myButtonGroup;
    private JTextArea myDescriptionArea;
    private JScrollPane myScrollPane;
    private JRadioButton myJspSyntaxButton;
    private JCheckBox mySegmentBox;
    private JLabel myDescriptionLabel, myOptionLabel;
    
    private WebModule myWebModule;
}
