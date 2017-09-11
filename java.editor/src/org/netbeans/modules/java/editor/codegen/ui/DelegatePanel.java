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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.DelegateMethodGenerator;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class DelegatePanel extends javax.swing.JPanel implements PropertyChangeListener {

    private JTextComponent component;
    private ElementHandle<TypeElement> handle;
    private ElementSelectorPanel delegateSelector;
    private ElementSelectorPanel methodSelector;

    /** Creates new form DelegatePanel */
    public DelegatePanel(JTextComponent component, ElementHandle<TypeElement> handle, ElementNode.Description description) {
        this.component = component;
        this.handle = handle;
        initComponents();
        delegateSelector = new ElementSelectorPanel(description, false);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        delegatePanel.add(delegateSelector, gridBagConstraints);
        delegateSelector.getExplorerManager().addPropertyChangeListener(this);
        
        methodSelector = new ElementSelectorPanel(null, false);
        methodSelector.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodPanel.add(methodSelector, gridBagConstraints);
        
        delegateLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_field_select")); //NOI18N
        delegateLabel.setLabelFor(delegateSelector);
        methodLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method_select")); //NOI18N
        methodLabel.setLabelFor(methodSelector);
        
        delegateSelector.doInitialExpansion(1);
        jSplitPane1.setDividerLocation(0.5);
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DelegateMethodGenerator.class, "A11Y_Generate_Delegate"));
    }

    public ElementHandle<? extends Element> getDelegateField() {
        List<ElementHandle<? extends Element>> handles = delegateSelector.getTreeSelectedElements();
        return handles.size() == 1 ? handles.get(0) : null;
    }

    public List<ElementHandle<? extends Element>> getDelegateMethods() {
        return ((ElementSelectorPanel)methodSelector).getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        methodPanel = new javax.swing.JPanel();
        methodLabel = new javax.swing.JLabel();
        delegatePanel = new javax.swing.JPanel();
        delegateLabel = new javax.swing.JLabel();

        setFocusable(false);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setResizeWeight(0.5);

        methodPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodPanel.add(methodLabel, gridBagConstraints);

        jSplitPane1.setRightComponent(methodPanel);

        delegatePanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        delegatePanel.add(delegateLabel, gridBagConstraints);

        jSplitPane1.setLeftComponent(delegatePanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void propertyChange(PropertyChangeEvent evt) {
        if ( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
            SwingUtilities.invokeLater(new Runnable() {                 
                public void run() {
                    ElementHandle<? extends VariableElement> fieldHandle = (ElementHandle<? extends VariableElement>) getDelegateField();
                    methodSelector.setRootElement(handle == null || fieldHandle == null ? null : DelegateMethodGenerator.getAvailableMethods(component, handle, fieldHandle), false);
                    methodSelector.doInitialExpansion(-1);            
                }
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel delegateLabel;
    public javax.swing.JPanel delegatePanel;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JLabel methodLabel;
    public javax.swing.JPanel methodPanel;
    // End of variables declaration//GEN-END:variables
}
