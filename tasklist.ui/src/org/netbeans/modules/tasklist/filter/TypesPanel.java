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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;
import org.netbeans.modules.tasklist.ui.checklist.CheckList;

/**
 *
 * @author  sa154850
 */
final class TypesPanel extends JPanel {
    
    private CheckList lstTypes;
    private List<? extends ScannerDescriptor> providers;
    private boolean[] providerState;
    
    private TypesFilter filter;
    
    public TypesPanel( TypesFilter filter ) {
        this.filter = filter;
        init();
        if( "Metal".equals( UIManager.getLookAndFeel().getID() ) ) //NOI18N
            setOpaque( true );
        else 
            setOpaque( false );
    }
    
    public boolean isValueValid() {
        return checkVisibleLimit();
    }

    private void init() {
        initComponents();
        
        providers = ScannerDescriptor.getDescriptors();

        List<ScannerDescriptor> toRemove = new ArrayList<ScannerDescriptor>();
        for (ScannerDescriptor scannerDescriptor : providers) {
            if (scannerDescriptor.getDisplayName() == null || scannerDescriptor.getDisplayName().isEmpty()) {
                filter.setEnabled(scannerDescriptor.getType(), false);
                toRemove.add(scannerDescriptor);
            }
        }
        providers.removeAll(toRemove);

        providerState = new boolean[providers.size()];
        String[] names = new String[providers.size()];
        for( int i=0; i<names.length; i++ ) {
            names[i] = providers.get( i ).getDisplayName();
        }
        String[] descs = new String[providers.size()];
        for (int i = 0; i < descs.length; i++) {
            descs[i] = providers.get(i).getDescription();
        }
        lstTypes = new CheckList(providerState, names, descs);
        lstTypes.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        lstTypes.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                int selIndex = lstTypes.getSelectedIndex();
                boolean enableOptions = false;
                if( selIndex >= 0 ) {
                    ScannerDescriptor tp = providers.get( selIndex );
                    enableOptions = null != tp.getOptionsPath();
                }
                btnOptions.setEnabled( enableOptions );
            }
        });
        lstTypes.getModel().addListDataListener( new ListDataListener() {
            public void intervalAdded(ListDataEvent arg0) {
            }
            public void intervalRemoved(ListDataEvent arg0) {
            }

            public void contentsChanged(ListDataEvent arg0) {
                putClientProperty( FilterCondition.PROP_VALUE_VALID, new Boolean( isValueValid() ) );
            }
        });
        
        btnOptions.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int selIndex = lstTypes.getSelectedIndex();
                if( selIndex >= 0 ) {
                    ScannerDescriptor tp = providers.get( selIndex );
                    if( null != tp.getOptionsPath() ) {
                        OptionsDisplayer.getDefault().open( tp.getOptionsPath() );
                    }
                }
            }
        });
        scrollTypes.setViewportView( lstTypes );
        
        showFilter( filter );
    }
    
    private void showFilter( TypesFilter filter ) {
        for( int i=0; i<providerState.length; i++ ) {
            ScannerDescriptor tp = providers.get( i );
            providerState[i] = null != filter && filter.isEnabled( tp.getType() );
        }
        txtVisibleLimit.setText( null == filter ? "" : String.valueOf(filter.getTaskCountLimit()) ); //NOI18N
        lstTypes.setEnabled( null != filter );
        txtVisibleLimit.setEnabled( null != filter );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblVisibleLimit = new javax.swing.JLabel();
        txtVisibleLimit = new javax.swing.JTextField();
        btnOptions = new javax.swing.JButton();
        scrollTypes = new javax.swing.JScrollPane();

        setOpaque(false);

        lblVisibleLimit.setLabelFor(txtVisibleLimit);
        org.openide.awt.Mnemonics.setLocalizedText(lblVisibleLimit, org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.lblVisibleLimit.text")); // NOI18N

        txtVisibleLimit.setText(org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.txtVisibleLimit.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOptions, org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.btnOptions.text")); // NOI18N
        btnOptions.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(scrollTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOptions))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblVisibleLimit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVisibleLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnOptions)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblVisibleLimit)
                            .addComponent(txtVisibleLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOptions;
    private javax.swing.JLabel lblVisibleLimit;
    private javax.swing.JScrollPane scrollTypes;
    private javax.swing.JTextField txtVisibleLimit;
    // End of variables declaration//GEN-END:variables
    
    public TypesFilter getFilter() {
        if (filter != null) {
            for( int i=0; i<providerState.length; i++ ) {
                ScannerDescriptor tp = providers.get( i );
                filter.setEnabled( tp.getType(),  providerState[i] );
            }
            filter.setTaskCountLimit( getVisibleLimit() );
        }
        return filter;
    }

    private int getVisibleLimit() {
        int limit = null == filter ? 100 : filter.getTaskCountLimit();
        try {
            String strLimit = txtVisibleLimit.getText();
            int tmp = Integer.parseInt( strLimit );
            if( tmp > 0 )
                limit = tmp;
        } catch( NumberFormatException nfE ) {
            //ignore
        }
        return limit;
    }
    
    private boolean checkVisibleLimit() {
        try {
            String strLimit = txtVisibleLimit.getText();
            int limit = Integer.parseInt( strLimit );
            return limit > 0;
        } catch( NumberFormatException nfE ) {
            return false;
        }
    }
}
