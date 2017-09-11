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

package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Displays an alert asking user to pick a running build to stop.
 * @author Jesse Glick
 */
final class StopBuildingAlert extends JPanel {
    
    static List<BuildExecutionSupport.Item> selectProcessToKill(List<BuildExecutionSupport.Item> toStop) {
        // Add all threads, sorted by display name.
        DefaultListModel model = new DefaultListModel();
        StopBuildingAlert alert = new StopBuildingAlert();
        final JList list = alert.buildsList;
        Comparator<BuildExecutionSupport.Item> comp = new Comparator<BuildExecutionSupport.Item>() {
            private final Collator coll = Collator.getInstance();
            @Override
            public int compare(BuildExecutionSupport.Item t1, BuildExecutionSupport.Item t2) {
                String n1 = t1.getDisplayName();
                String n2 = t2.getDisplayName();
                int r = coll.compare(n1, n2);
                if (r != 0) {
                    return r;
                } else {
                    // Arbitrary. XXX Note that there is no way to predict which is
                    // which if you have more than one build running. Ideally it
                    // would be subsorted by creation time, probably.
                    return System.identityHashCode(t1) - System.identityHashCode(t2);
                }
            }
        };
        SortedSet<BuildExecutionSupport.Item> items = new TreeSet<BuildExecutionSupport.Item>(comp);
        items.addAll(toStop);

        for (BuildExecutionSupport.Item t : items) {
            model.addElement(t);
        }
        list.setModel(model);
        list.setSelectedIndex(0);
        // Make a dialog with buttons "Stop Building" and "Cancel".
        DialogDescriptor dd = new DialogDescriptor(alert, NbBundle.getMessage(StopBuildingAlert.class, "TITLE_SBA"));
        dd.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
        final JButton stopButton = new JButton(NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_stop"));
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                stopButton.setEnabled(list.getSelectedValue() != null);
            }
        });
        dd.setOptions(new Object[] {stopButton, DialogDescriptor.CANCEL_OPTION});
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        List<BuildExecutionSupport.Item> toRet = new ArrayList<BuildExecutionSupport.Item>();
        if (dd.getValue() == stopButton) {
            Object[] selectedItems = list.getSelectedValues();
            for (Object o : selectedItems) {
                toRet.add((BuildExecutionSupport.Item)o);
            }
        }
        return toRet;

    }
    
    
    private StopBuildingAlert() {
        initComponents();
        buildsList.setCellRenderer(new ProcessCellRenderer());
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        introLabel = new javax.swing.JLabel();
        buildsLabel = new javax.swing.JLabel();
        buildsScrollPane = new javax.swing.JScrollPane();
        buildsList = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(introLabel, org.openide.util.NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_intro")); // NOI18N

        buildsLabel.setLabelFor(buildsList);
        org.openide.awt.Mnemonics.setLocalizedText(buildsLabel, org.openide.util.NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_select")); // NOI18N

        buildsScrollPane.setViewportView(buildsList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buildsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buildsLabel)
                            .addComponent(introLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(28, 28, 28))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(introLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buildsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buildsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel buildsLabel;
    public javax.swing.JList buildsList;
    public javax.swing.JScrollPane buildsScrollPane;
    public javax.swing.JLabel introLabel;
    // End of variables declaration//GEN-END:variables

    private final class ProcessCellRenderer extends DefaultListCellRenderer/*<Thread>*/ {
        
        public ProcessCellRenderer() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            BuildExecutionSupport.Item t = (BuildExecutionSupport.Item) value;
            String displayName = t.getDisplayName();
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
        
    }
    
}
