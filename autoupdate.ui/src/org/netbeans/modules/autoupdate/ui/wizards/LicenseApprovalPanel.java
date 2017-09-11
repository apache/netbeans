/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class LicenseApprovalPanel extends javax.swing.JPanel {
    public static final String LICENSE_APPROVED = "license-approved";
    private List<UpdateElement> license4plugins;
    
    /** Creates new form LicenseApprovalPanel */
    public LicenseApprovalPanel (InstallUnitWizardModel model, boolean isApproved) {
        initComponents ();
        cbAccept.setSelected (isApproved);
        if (model != null) {
            writeLicenses(model);
        } else {
            cbAccept.setEnabled (false);
            taLicense.setEnabled (false);
        }
    }
    
    Collection<String> getLicenses () {
        assert license4plugins != null : "Licenses must found.";
        if (license4plugins == null && license4plugins.isEmpty ()) {
            return Collections.emptyList ();
        }
		Set<String> licenses = new HashSet<String>();
		for (UpdateElement el : license4plugins) {
			licenses.add(el.getLicence());
		}
		return licenses;
	}

    Collection<String> getLicenseIds() {
        assert license4plugins != null : "Licenses must found.";
        if (license4plugins == null && license4plugins.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> licenseIds = new HashSet<String>();
        for (UpdateElement el : license4plugins) {
            licenseIds.add(el.getLicenseId());
        }
        return licenseIds;
    }

    private void goOverLicenses (InstallUnitWizardModel model) {
        for (UpdateElement el : model.getAllUpdateElements()) {
            if (el.getLicence() != null) {
                if (license4plugins == null) {
                    license4plugins = new ArrayList<UpdateElement>();
                }

                if (! OperationType.UPDATE.equals(model.getOperation()) || ! Utilities.isLicenseIdApproved(el.getLicenseId())) {
                    Logger.getLogger(LicenseApprovalPanel.class.getName()).log(Level.FINE, "{0}[{1}] hasn''t been accepted yet.", new Object[]{el, el.getLicenseId()});
                    license4plugins.add(el);
                }
            }
        }
    }
    
    public boolean isApproved () {
        return cbAccept.isSelected ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbAccept = new javax.swing.JCheckBox();
        cbPlugins = new javax.swing.JComboBox();
        spLicense = new javax.swing.JScrollPane();
        taLicense = new javax.swing.JTextArea();
        lbPlugins = new javax.swing.JLabel();
        tpTitle = new javax.swing.JTextPane();

        org.openide.awt.Mnemonics.setLocalizedText(cbAccept, org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel.cbAccept.text")); // NOI18N
        cbAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAcceptActionPerformed(evt);
            }
        });

        cbPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPluginsActionPerformed(evt);
            }
        });

        taLicense.setEditable(false);
        taLicense.setColumns(20);
        taLicense.setLineWrap(true);
        taLicense.setRows(5);
        taLicense.setWrapStyleWord(true);
        taLicense.setMargin(new java.awt.Insets(0, 4, 0, 4));
        spLicense.setViewportView(taLicense);
        taLicense.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taLicenses_ACN")); // NOI18N
        taLicense.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taLicenses_ACD")); // NOI18N

        lbPlugins.setLabelFor(cbPlugins);
        org.openide.awt.Mnemonics.setLocalizedText(lbPlugins, org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel.lbPlugins.text")); // NOI18N

        tpTitle.setEditable(false);
        tpTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tpTitle.setText(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taTitle_Text")); // NOI18N
        tpTitle.setOpaque(false);
        tpTitle.setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tpTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbPlugins)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbPlugins, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(spLicense)
                    .addComponent(cbAccept, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tpTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbPlugins)
                    .addComponent(lbPlugins, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spLicense, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAccept)
                .addContainerGap())
        );

        cbAccept.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_cbAccept_ACN")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbAcceptActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cbAcceptActionPerformed
        firePropertyChange (LICENSE_APPROVED, null, cbAccept.isSelected ());
    }//GEN-LAST:event_cbAcceptActionPerformed

	private void cbPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPluginsActionPerformed
		// This is designed on purpose to make the user feel that
		//  license do refresh when new plugin is selected
		taLicense.setText("");
		final int delay = 100;
		RequestProcessor.getDefault().post(new Runnable() {

                        @Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {

                                        @Override
					public void run() {
						UpdateElement el = license4plugins.get(cbPlugins.getSelectedIndex());
						taLicense.setText(el.getLicence());
						taLicense.setCaretPosition(0);
					}
				});
			}
		}, delay);
	}//GEN-LAST:event_cbPluginsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAccept;
    private javax.swing.JComboBox cbPlugins;
    private javax.swing.JLabel lbPlugins;
    private javax.swing.JScrollPane spLicense;
    private javax.swing.JTextArea taLicense;
    private javax.swing.JTextPane tpTitle;
    // End of variables declaration//GEN-END:variables

	private void writeLicenses(InstallUnitWizardModel model) {
		goOverLicenses(model);
		List<String> pluginsModel = new ArrayList<String>();
		String fmt = NbBundle.getMessage(LicenseApprovalPanel.class,
				"LicenseApprovalPanel_cbPlugins_ItemFormat");
		for (UpdateElement el : license4plugins) {
			String formatted = MessageFormat.format(fmt,
					new Object[]{el.getDisplayName(), el.getSpecificationVersion()});
			pluginsModel.add(formatted);
		}

		cbPlugins.setModel(new DefaultComboBoxModel(pluginsModel.toArray()));
		if (! pluginsModel.isEmpty()) {
                    cbPlugins.setSelectedIndex(0);
                }
	}

}
