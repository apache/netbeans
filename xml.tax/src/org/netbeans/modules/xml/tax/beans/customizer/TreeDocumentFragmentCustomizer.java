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

import org.netbeans.tax.TreeDocumentFragment;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentFragmentCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID =6588833590608041474L;


    //
    // init
    //

    /** Creates new customizer TreeDocumentFragmentCustomizer. */
    public TreeDocumentFragmentCustomizer () {
        super ();
        initComponents ();
        versionLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_docFrag_version")); // NOI18N
        encodingLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_docFrag_encoding")); // NOI18N
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeDocumentFragment getDocumentFragment () {
        return (TreeDocumentFragment)getTreeObject ();
    }
    
    /**
     * It will be called from AWT thread and it will never be caller during init stage.
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeDocumentFragment.PROP_VERSION)) {
            updateVersionComponent ();
        } else if (pche.getPropertyName ().equals (TreeDocumentFragment.PROP_ENCODING)) {
            updateEncodingComponent ();
        }
    }
    
    /**
     */
    protected final void updateDTDVersion () {
        if ( cbVersion.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getDocumentFragment ().setVersion (text2null ((String) cbVersion.getSelectedItem ()));
        } catch (TreeException exc) {
            updateVersionComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateVersionComponent () {
        cbVersion.setSelectedItem (null2text (getDocumentFragment ().getVersion ()));
    }
    
    /**
     */
    protected final void updateDTDEncoding () {
        if ( cbEncoding.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getDocumentFragment ().setEncoding (text2null ((String) cbEncoding.getSelectedItem ()));
        } catch (TreeException exc) {
            updateEncodingComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateEncodingComponent () {
        cbEncoding.setSelectedItem (null2text (getDocumentFragment ().getEncoding ()));
    }
    
    /**
     */
    protected void initComponentValues () {
        updateVersionComponent ();
        updateEncodingComponent ();
    }
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        cbVersion.setEnabled (editable);
        cbEncoding.setEnabled (editable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        versionLabel = new javax.swing.JLabel();
        cbVersion = new javax.swing.JComboBox();
        encodingLabel = new javax.swing.JLabel();
        cbEncoding = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        versionLabel.setText(Util.THIS.getString ("PROP_docFrag_version"));
        versionLabel.setLabelFor(cbVersion);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(versionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(cbVersion, gridBagConstraints);

        encodingLabel.setText(Util.THIS.getString ("PROP_docFrag_encoding"));
        encodingLabel.setLabelFor(cbEncoding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(encodingLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(cbEncoding, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbVersion;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JComboBox cbEncoding;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel encodingLabel;
    // End of variables declaration//GEN-END:variables
    
}
