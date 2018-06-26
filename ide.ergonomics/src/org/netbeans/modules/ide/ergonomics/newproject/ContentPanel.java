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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ide.ergonomics.newproject;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.openide.util.RequestProcessor;

public final class ContentPanel extends JPanel {
    static String FINDING_MODULES = "finding-modules";
    private String name = null;

    /** Creates new form InstallMissingModulesWizardVisualPanel1 */
    public ContentPanel (String name) {
        initComponents();
        this.name = name;
        this.spTable.setVisible (false);
        this.tModules.setVisible (false);
    }

    public @Override String getName() {
        return name;
    }

    @Override
    public void addNotify () {
        super.addNotify ();
        FeatureManager.getInstance().create(new Runnable () {
            public void run () {
                firePropertyChange (FINDING_MODULES, null, Boolean.TRUE);
            }
        }).schedule(200);
    }

    private void doReplaceComponents (JComponent... comps) {
        assert pCentral != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        pCentral.removeAll ();
        pCentral.repaint ();
        addComponents (comps);
    }
    
    private void doAddComponents (JComponent... comps) {
        assert pCentral != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        if (comps != null) {
            for (JComponent c : comps) {
                if (c != null) {
                    pCentral.add (c);
                }
            }
        }
        pCentral.revalidate ();
        revalidate ();
    }
    
    public void replaceComponents (final JComponent... comps) {
        if (SwingUtilities.isEventDispatchThread ()) {
            doReplaceComponents (comps);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    doReplaceComponents (comps);
                }
            });
        }
    }
    
    public void addComponents (final JComponent... comps) {
        if (SwingUtilities.isEventDispatchThread ()) {
            doAddComponents (comps);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    doAddComponents (comps);
                }
            });
        }
    }
    
    private static String prepareToolTip (String original) {
        String res = "";
        res = "<html>" + original.replaceAll (",", "<br>")+ "</html>"; // NOI18N
        return res;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pCentral = new javax.swing.JPanel();
        spTable = new javax.swing.JScrollPane();
        tModules = new JTable () {
            public Component prepareRenderer (TableCellRenderer renderer,
                int rowIndex,
                int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent && vColIndex != 0) {
                    JComponent jc = (JComponent) c;
                    jc.setToolTipText (prepareToolTip ((String) getValueAt (rowIndex, vColIndex)));
                }
                return c;
            }
        };

        pCentral.setLayout(new javax.swing.BoxLayout(pCentral, javax.swing.BoxLayout.Y_AXIS));

        tModules.setRowSelectionAllowed(false);
        spTable.setViewportView(tModules);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pCentral, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spTable, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pCentral, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addGap(25, 25, 25)
                .addComponent(spTable, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pCentral;
    private javax.swing.JScrollPane spTable;
    private javax.swing.JTable tModules;
    // End of variables declaration//GEN-END:variables

}

