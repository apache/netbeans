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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeConditionalSection;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeConditionalSectionCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID =-396968653847909885L;


    //
    // init
    //

    /** Creates new customizer TreeConditionalSectionCustomizer. */
    public TreeConditionalSectionCustomizer () {
        super ();
        
        initComponents ();
        initAccessibility();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeConditionalSection getConditionalSection () {
        return (TreeConditionalSection)getTreeObject ();
    }
    
    /**
     * It will be called from AWT thread and it will never be caller during init stage.
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeConditionalSection.PROP_INCLUDE)) {
            updateIncludeIgnoreComponent ();
        } else if (pche.getPropertyName ().equals (TreeConditionalSection.PROP_IGNORED_CONTENT)) {
            updateIgnoredContentComponent ();
        }
    }
    
    
    protected void updateIncludeIgnoreComponent () {
        includeRadioButton.setSelected (getConditionalSection ().isInclude ());
        ignoreRadioButton.setSelected (!!! getConditionalSection ().isInclude ());
        
        ignoredContentScrollPane.setVisible (!!! getConditionalSection ().isInclude ());
        fillPanel.setVisible (getConditionalSection ().isInclude ());
    }
    
    protected void updateConditionalSectionInclude () {
        try {
            getConditionalSection ().setInclude (includeRadioButton.isSelected ());
        } catch (TreeException exc) {
            updateIncludeIgnoreComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    protected void updateIgnoredContentComponent () {
        ignoredContentTextArea.setText (null2text (getConditionalSection ().getIgnoredContent ()));
    }
    
    protected void updateConditionalSectionIgnoredContent () {
        try {
            getConditionalSection ().setIgnoredContent (text2null (ignoredContentTextArea.getText ()));
        } catch (TreeException exc) {
            updateIgnoredContentComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    
    /**
     */
    protected void initComponentValues () {
        updateIncludeIgnoreComponent ();
        updateIgnoredContentComponent ();
    }
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        includeRadioButton.setEnabled (editable); //???
        ignoreRadioButton.setEnabled (editable); //???
        ignoredContentTextArea.setEditable (editable);
    }
    
    
    private void initAccessibility() {
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeConditionalSectionCustomizer"));
        ignoredContentTextArea.getAccessibleContext ().setAccessibleName (Util.THIS.getString ("ACSN_ignoredContentPane"));
        includeRadioButton.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_includeRadioButton"));
        ignoreRadioButton.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_ignoreRadioButton"));        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        includeButtonGroup = new javax.swing.ButtonGroup();
        includeRadioButton = new javax.swing.JRadioButton();
        ignoreRadioButton = new javax.swing.JRadioButton();
        ignoredContentScrollPane = new javax.swing.JScrollPane();
        ignoredContentTextArea = new javax.swing.JTextArea();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 100));
        includeRadioButton.setSelected(true);
        includeRadioButton.setText(Util.THIS.getString ("PROP_condSection_include"));
        includeButtonGroup.add(includeRadioButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(includeRadioButton, gridBagConstraints);

        ignoreRadioButton.setText(Util.THIS.getString ("PROP_condSection_ignore"));
        includeButtonGroup.add(ignoreRadioButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(ignoreRadioButton, gridBagConstraints);

        ignoredContentScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));
        ignoredContentScrollPane.setViewportView(ignoredContentTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(ignoredContentScrollPane, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton ignoreRadioButton;
    private javax.swing.JScrollPane ignoredContentScrollPane;
    private javax.swing.ButtonGroup includeButtonGroup;
    private javax.swing.JTextArea ignoredContentTextArea;
    private javax.swing.JRadioButton includeRadioButton;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
}
