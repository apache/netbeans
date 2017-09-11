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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author  Dusan Balek
 */
public class EqualsHashCodePanel extends JPanel {

    private ElementSelectorPanel equalsSelector;
    private ElementSelectorPanel hashCodeSelector;
    
    private JLabel equalsLabel;
    private JLabel hashCodeLabel;

    /** Creates new form EqualsHashCodePanel */
    public EqualsHashCodePanel(ElementNode.Description description, boolean generateEquals, boolean generateHashCode) {
        assert generateEquals || generateHashCode;
        
        initComponents();
        
        GridBagConstraints gridBagConstraints;

        PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        };

        if( generateEquals ) {
            equalsLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(equalsLabel, gridBagConstraints);

            equalsSelector = new ElementSelectorPanel(description, false);
            equalsSelector.getExplorerManager().addPropertyChangeListener(pcl);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(equalsSelector, gridBagConstraints);
        
            equalsLabel.setLabelFor(equalsSelector);
        }

        if( generateHashCode ) {
            hashCodeLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_hashcode_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, generateEquals ? 0 : 12, 6, 12);
            add(hashCodeLabel, gridBagConstraints);

            hashCodeSelector = new ElementSelectorPanel( ElementNode.Description.deepCopy(description), false);
            hashCodeSelector.getExplorerManager().addPropertyChangeListener(pcl);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, generateEquals ? 0 : 12, 0, 12);
            add(hashCodeSelector, gridBagConstraints);
        
            hashCodeLabel.setLabelFor(hashCodeSelector);
        }
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EqualsHashCodeGenerator.class, "A11Y_Generate_EqualsHashCode"));
    }
    
    
    public List<ElementHandle<? extends Element>> getEqualsVariables() {
        if( null == equalsSelector )
            return null;
        return ((ElementSelectorPanel)equalsSelector).getSelectedElements();
    }

    public List<ElementHandle<? extends Element>> getHashCodeVariables() {
        if( null == hashCodeSelector )
            return null;
        return ((ElementSelectorPanel)hashCodeSelector).getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
