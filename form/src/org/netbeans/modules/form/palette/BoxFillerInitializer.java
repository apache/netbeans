/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.palette;

import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import org.netbeans.modules.form.RADComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Initializer of {@code Box.Filler} {@code PaletteItem}s.
 *
 * @author Jan Stola
 */
class BoxFillerInitializer implements PaletteItem.ComponentInitializer {
    private String initializerId;
    private int width;
    private int height;
    
    @Override
    public boolean prepare(PaletteItem item, FileObject classPathRep) {
        initializerId = item.getComponentInitializerId();
        boolean dialogOKClosed = true;
        WidthHeightPanel panel = null;
        if ("Box.Filler.HorizontalStrut".equals(initializerId)) { // NOI18N
            panel = new WidthHeightPanel(true, false);
            dialogOKClosed = showDialog(panel, "BoxFillerInitializer.HorizontalStrut"); // NOI18N
        } else if ("Box.Filler.VerticalStrut".equals(initializerId)) { // NOI18N
            panel = new WidthHeightPanel(false, true);
            dialogOKClosed = showDialog(panel, "BoxFillerInitializer.VerticalStrut"); // NOI18N
        } else if ("Box.Filler.RigidArea".equals(initializerId)) { // NOI18N
            panel = new WidthHeightPanel(true, true);
            dialogOKClosed = showDialog(panel, "BoxFillerInitializer.RigidArea"); // NOI18N
        }
        if (panel != null && dialogOKClosed) {
            width = panel.getFillerWidth();
            height = panel.getFillerHeight();
        }
        return dialogOKClosed;
    }

    @Override
    public void initializeComponent(RADComponent metaComp) {
        metaComp.setAuxValue(RADComponent.AUX_VALUE_CLASS_DETAILS, initializerId);
        if ("Box.Filler.Glue".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(Short.MAX_VALUE, Short.MAX_VALUE)); // NOI18N
        } else if ("Box.Filler.HorizontalGlue".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(Short.MAX_VALUE, 0)); // NOI18N
        } else if ("Box.Filler.VerticalGlue".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(0, Short.MAX_VALUE)); // NOI18N
        } else if ("Box.Filler.HorizontalStrut".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "minimumSize", new Dimension(width, 0)); // NOI18N
            setProperty(metaComp, "preferredSize", new Dimension(width, 0)); // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(width, Short.MAX_VALUE)); // NOI18N
        } else if ("Box.Filler.VerticalStrut".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "minimumSize", new Dimension(0, height)); // NOI18N
            setProperty(metaComp, "preferredSize", new Dimension(0, height)); // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(Short.MAX_VALUE, height)); // NOI18N
        } else if ("Box.Filler.RigidArea".equals(initializerId)) { // NOI18N
            setProperty(metaComp, "minimumSize", new Dimension(width, height)); // NOI18N
            setProperty(metaComp, "preferredSize", new Dimension(width, height)); // NOI18N
            setProperty(metaComp, "maximumSize", new Dimension(width, height)); // NOI18N
        }
    }

    private boolean showDialog(WidthHeightPanel panel, String titleKey) {
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(BoxFillerInitializer.class, titleKey),
                true,
                null);
        HelpCtx.setHelpIDString(panel, "f1_gui_filler_html"); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        return (dd.getValue() == DialogDescriptor.OK_OPTION);
    }

    private void setProperty(RADComponent metacomp, String propName, Dimension value) {
        try {
            Node.Property prop = metacomp.getPropertyByName(propName);
            prop.setValue(value);
        } catch (IllegalAccessException iaex) {
            Logger.getLogger(BoxFillerInitializer.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
        } catch (InvocationTargetException itex) {
            Logger.getLogger(BoxFillerInitializer.class.getName()).log(Level.INFO, itex.getMessage(), itex);
        }
    }

    private static class WidthHeightPanel extends JPanel {
        private JSpinner widthField;
        private JSpinner heightField;
        
        WidthHeightPanel(boolean showWidth, boolean showHeight) {
            ResourceBundle bundle = NbBundle.getBundle(BoxFillerInitializer.class);
            JLabel widthLabel = new JLabel(bundle.getString("BoxFillerInitializer.width")); // NOI18N
            JLabel heightLabel = new JLabel(bundle.getString("BoxFillerInitializer.height")); // NOI18N
            widthField = new JSpinner(new SpinnerNumberModel());
            heightField = new JSpinner(new SpinnerNumberModel());
            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(widthLabel)
                        .addComponent(heightLabel))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(widthField)
                        .addComponent(heightField))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(widthLabel)
                        .addComponent(widthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(heightLabel)
                        .addComponent(heightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            widthLabel.setVisible(showWidth);
            heightLabel.setVisible(showHeight);
            widthField.setVisible(showWidth);
            heightField.setVisible(showHeight);
        }
        
        int getFillerWidth() {
            return (Integer)widthField.getValue();
        }
        
        int getFillerHeight() {
            return (Integer)heightField.getValue();
        }
    }

}
