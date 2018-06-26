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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitconf.ui.client;

import javax.swing.JCheckBox;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class TransportPanelClient extends SectionInnerPanel {

    private Node node;
    private Binding binding;
    private boolean inSync = false;
    private JaxWsModel jaxwsmodel;

    private Project project;
   
    public TransportPanelClient(SectionView view, Node node, Binding binding, JaxWsModel jaxWsModel) {
        super(view);
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;

        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            project = FileOwnerQuery.getOwner(fo);
        } else {
            JAXWSLightSupport supp = node.getLookup().lookup(JAXWSLightSupport.class);
            project = FileOwnerQuery.getOwner(supp.getWsdlFolder(true));
        }

        initComponents();
        /* issue 232988: the background color issues with dark metal L&F
        optimalEncChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        optimalTransportChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */

        addImmediateModifier(optimalEncChBox);
        addImmediateModifier(optimalTransportChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        setChBox(optimalEncChBox, TransportModelHelper.isAutoEncodingEnabled(binding));
        setChBox(optimalTransportChBox, TransportModelHelper.isAutoTransportEnabled(binding));
        
        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {

            Util.checkMetroLibrary(project);

            if (source.equals(optimalEncChBox)) {
                TransportModelHelper.setAutoEncoding(binding, optimalEncChBox.isSelected());
            }

            if (source.equals(optimalTransportChBox)) {
                TransportModelHelper.enableAutoTransport(binding, optimalTransportChBox.isSelected());
            }
            enableDisable();
        }
    }

    private void enableDisable() {
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        optimalEncChBox.setEnabled(!amSec);
        optimalTransportChBox.setEnabled(!amSec);
    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optimalEncChBox = new javax.swing.JCheckBox();
        optimalTransportChBox = new javax.swing.JCheckBox();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(optimalEncChBox, org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalEncoding")); // NOI18N
        optimalEncChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optimalEncChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(optimalTransportChBox, org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalTransport")); // NOI18N
        optimalTransportChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optimalTransportChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optimalTransportChBox)
                    .addComponent(optimalEncChBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optimalEncChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optimalTransportChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optimalEncChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalEncoding_ACSN")); // NOI18N
        optimalEncChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalEncoding_ACSD")); // NOI18N
        optimalTransportChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalTransport_ACSN")); // NOI18N
        optimalTransportChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalTransport_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox optimalEncChBox;
    private javax.swing.JCheckBox optimalTransportChBox;
    // End of variables declaration//GEN-END:variables
    
}
