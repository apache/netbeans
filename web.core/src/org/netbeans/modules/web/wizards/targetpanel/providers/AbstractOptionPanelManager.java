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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
