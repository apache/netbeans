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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.codegen.ConstructorGenerator;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 */
public class ConstructorPanel extends JPanel {

    private JLabel constructorSelectorLabel;
    private SuperConstructorSelectorPanel constructorSelector;
    private JLabel fieldSelectorLabel;
    private ElementSelectorPanel fieldSelector;

    /** Creates new form ConstructorPanel */
    public ConstructorPanel(ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        initComponents();
        if (fieldsDescription != null) {
            fieldSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(fieldSelectorLabel, gridBagConstraints);
            fieldSelector = new ElementSelectorPanel(fieldsDescription, false, true, false);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(fieldSelector, gridBagConstraints);
            Mnemonics.setLocalizedText(fieldSelectorLabel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor_select")); //NOI18N
            fieldSelectorLabel.setLabelFor(fieldSelector);
            fieldSelector.doInitialExpansion(1);
        }
        if (constructorDescription != null) {
            constructorSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, fieldsDescription != null ? 0 : 12, 6, 12);
            add(constructorSelectorLabel, gridBagConstraints);
            constructorSelector = new SuperConstructorSelectorPanel(constructorDescription);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, fieldsDescription != null ? 0 : 12, 0, 12);
            add(constructorSelector, gridBagConstraints);
            Mnemonics.setLocalizedText(constructorSelectorLabel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_super_constructor_select")); //NOI18N
            constructorSelectorLabel.setLabelFor(constructorSelector);
        }
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ConstructorGenerator.class, "A11Y_Generate_Constructor"));
    }

    public CsmConstructor getInheritedConstructor() {
        if (constructorSelector == null) {
            return null;
        }
        List<CsmDeclaration> handles = constructorSelector.getSelectedElements();
        return (handles.size() == 1 ? (CsmConstructor) handles.get(0) : null);
    }

    public List<CsmConstructor> getInheritedConstructors() {
        if (constructorSelector == null) {
            return Collections.<CsmConstructor>emptyList();
        }
        List<?> decls = constructorSelector.getSelectedElements();
        // we know that list contains only constructors
        @SuppressWarnings("unchecked")
        List<CsmConstructor> ctrs = (List<CsmConstructor>) decls;
        return ctrs;
    }

    public List<CsmField> getVariablesToInitialize() {
        if (fieldSelector == null) {
            return Collections.<CsmField>emptyList();
        }
        List<?> decls = fieldSelector.getSelectedElements();
        // we know that list contains only fields
        @SuppressWarnings("unchecked")
        List<CsmField> fields = (List<CsmField>) decls;
        return fields;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
