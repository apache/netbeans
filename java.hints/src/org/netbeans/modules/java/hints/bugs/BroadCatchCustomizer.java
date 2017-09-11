/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.hints.ui.ClassNameList;
import org.netbeans.modules.java.hints.ui.InnerPanelSupport;
import org.netbeans.modules.java.hints.ui.TypeAcceptor;

/**
 *
 * @author sdedic
 */
public class BroadCatchCustomizer extends javax.swing.JPanel 
    implements ActionListener, ChangeListener {
    private Preferences prefs;
    private ClassNameList listClasses;
    /**
     * Creates new form BroadCatchCustomizer
     */
    public BroadCatchCustomizer(Preferences prefs) {
        this.prefs = prefs;
        initComponents();
        listClasses = new ClassNameList().restrictTypes(new TypeAcceptor<ElementHandle<TypeElement>, CompilationController>() {
            @Override
            public boolean accept(ElementHandle<TypeElement> item, CompilationController c) {
                TypeElement el = item.resolve(c);
                TypeElement thr = c.getElements().getTypeElement("java.lang.Throwable"); // NOI18N
                return c.getTypes().isSubtype(el.asType(), thr.asType());
            }
        });
        listClasses.addChangeListener(this);
        classHolder.add(listClasses);
        
        cbCommonTypes.addActionListener(this);
        cbSuppressUmbrellas.addActionListener(this);
        
        initList(prefs.get(BroadCatchBlock.OPTION_UMBRELLA_LIST, BroadCatchBlock.DEFAULT_UMBRELLA_LIST));

        cbSuppressUmbrellas.setSelected(
            prefs.getBoolean(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, BroadCatchBlock.DEFAULT_EXCLUDE_UMBRELLA));
        cbCommonTypes.setSelected(
            !prefs.getBoolean(BroadCatchBlock.OPTION_EXCLUDE_COMMON, BroadCatchBlock.DEFAULT_EXCLUDE_COMMON));
        
        enableUmbrella();
        prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, cbSuppressUmbrellas.isSelected());
        prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_COMMON, !cbCommonTypes.isSelected());
    }
    
    private void initList(String val) {
        listClasses.setClassNames(val);
        prefs.put(BroadCatchBlock.OPTION_UMBRELLA_LIST, val);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        prefs.put(BroadCatchBlock.OPTION_UMBRELLA_LIST, listClasses.getClassNameList());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == cbCommonTypes) {
            prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_COMMON, !cbCommonTypes.isSelected());
        } else if (src == cbSuppressUmbrellas) {
            prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, cbSuppressUmbrellas.isSelected());
            enableUmbrella();
        }
    }
    
    private void enableUmbrella() {
        boolean enable = cbSuppressUmbrellas.isEnabled() && cbSuppressUmbrellas.isSelected();
        InnerPanelSupport.enablePanel(classHolder, enable);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbCommonTypes = new javax.swing.JCheckBox();
        cbSuppressUmbrellas = new javax.swing.JCheckBox();
        classHolder = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(360, 169));

        org.openide.awt.Mnemonics.setLocalizedText(cbCommonTypes, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.cbCommonTypes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSuppressUmbrellas, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.cbSuppressUmbrellas.text")); // NOI18N

        classHolder.setLayout(new java.awt.GridLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbSuppressUmbrellas)
                    .addComponent(cbCommonTypes))
                .addContainerGap(140, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(classHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbCommonTypes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSuppressUmbrellas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(classHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCommonTypes;
    private javax.swing.JCheckBox cbSuppressUmbrellas;
    private javax.swing.JPanel classHolder;
    // End of variables declaration//GEN-END:variables
}
