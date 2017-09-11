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

package org.netbeans.modules.autoupdate.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous
 */
public class UpdateUnitProviderPanel extends javax.swing.JPanel {
    private DocumentListener listener;
    private FocusListener focusNameListener;
    private FocusListener focusUrlListener;
    private final JButton bOK = new JButton(NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel_OK"));//NOI18N
    private Set<String> namesOfProviders = null;
    private final boolean isEdit;
    private final String originalName;
    private final Icon errorIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/error.png", false);

    /** Creates new form UpdateUnitProviderPanel */
    public UpdateUnitProviderPanel(boolean isActive, String name, String url, boolean editing) {
        isEdit = editing;
        originalName = name;
        initComponents();
        addListeners ();
        tfURL.setText(url);
        tfName.setText(name);
        cbActive.setSelected(isActive);
        getAccessibleContext().setAccessibleName("ACN_UpdateCenterCustomizer");
        getAccessibleContext().setAccessibleDescription("ACD_UpdateCenterCustomizer");
    }

    JButton getOKButton() {
        return bOK;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addListeners ();
    }

    public void updateErrorMessage(String error) {
        if (error == null) {
            errorLabel.setText("");
            errorLabel.setIcon(null);
            bOK.setEnabled(true);
        } else {
            errorLabel.setText(error);
            errorLabel.setIcon(errorIcon);
            bOK.setEnabled(false);
        }
    }

    private void addListeners () {
        if (listener == null) {
            listener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent arg0) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent arg0) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    update();
                }

                private void update() {
                    updateErrorMessage(getErrorMessage());
                }

                private String getErrorMessage() {
                    final String providerName = getProviderName();
                    String url = getProviderURL();
                    if (providerName.length() == 0) {
                        return NbBundle.getMessage(UpdateUnitProviderPanel.class,
                                "UpdateUnitProviderPanel.error.name.short");
                    } else if (url.length() == 0) {
                        return NbBundle.getMessage(UpdateUnitProviderPanel.class,
                                "UpdateUnitProviderPanel.error.url.short");
                    }

                    if (!(isEdit && providerName.equals(originalName)) &&
                            getNamesOfProviders().contains(providerName)) {
                        return NbBundle.getMessage(UpdateUnitProviderPanel.class,
                                "UpdateUnitProviderPanel.error.name.dup");
                    }

                    boolean isOk = true;
                    try {
                        new URI(url).toURL();
                        //#132506
                        if (url.startsWith("http:") && !url.startsWith("http://")) {
                            isOk = false;
                        }
                        if (isOk) {
                            //workaround for Issue #160766
                            //Use "_removed" here instead of UpdateUnitProviderImpl.REMOVED_MASK since is private and not in api/spi
                            if ((providerName + "_removed").length() > Preferences.MAX_NAME_LENGTH) {
                                return NbBundle.getMessage(UpdateUnitProviderPanel.class,
                                        "UpdateUnitProviderPanel.error.name.toolong");
                            }
                        }
                    } catch (MalformedURLException x) {
                        isOk = false;
                    } catch (URISyntaxException x) {
                        isOk = false;
                    } catch (IllegalArgumentException x) {
                        isOk = false;
                    }
                    if(!isOk) {
                        return NbBundle.getMessage(UpdateUnitProviderPanel.class,
                                "UpdateUnitProviderPanel.error.url.invalid");
                    }
                    return null;
                }
            };
            focusNameListener = new FocusListener () {
                int currentSelectionStart = 0;
                int currentSelectionEnd = 0;
                @Override
                public void focusGained(FocusEvent e) {
                    if (e.getOppositeComponent () != null) {
                        tfName.selectAll ();
                    } else {
                        tfName.select (currentSelectionStart, currentSelectionEnd);
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    currentSelectionStart = tfName.getSelectionStart ();
                    currentSelectionEnd = tfName.getSelectionEnd ();
                    tfName.select (0, 0);
                }
            };
            tfName.addFocusListener (focusNameListener);

            focusUrlListener = new FocusListener () {
                int currentSelectionStart = 0;
                int currentSelectionEnd = 0;
                @Override
                public void focusGained(FocusEvent e) {
                    if (e.getOppositeComponent () != null) {
                        tfURL.selectAll ();
                    } else {
                        tfURL.select (currentSelectionStart, currentSelectionEnd);
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    currentSelectionStart = tfURL.getSelectionStart ();
                    currentSelectionEnd = tfURL.getSelectionEnd ();
                    tfURL.select (0, 0);
                }
            };
            tfURL.addFocusListener (focusUrlListener);

            tfName.getDocument().addDocumentListener(listener);
            tfURL.getDocument().addDocumentListener(listener);
        }
    }
    
    private Set<String> getNamesOfProviders () {
        if (namesOfProviders == null) {
            namesOfProviders = new HashSet<String> ();
            for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false)) {
                namesOfProviders.add (p.getDisplayName ());
            }
        }
        return namesOfProviders;
    }

    private void removeListener() {
        if (listener != null) {
            tfName.getDocument().removeDocumentListener(listener);
            tfURL.getDocument().removeDocumentListener(listener); 
            tfName.removeFocusListener (focusNameListener);
            tfURL.removeFocusListener (focusUrlListener);
            listener = null;
        }        
    }   
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        removeListener();
    }

    
    public String getDisplayName() {
        return NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel_displayName");//NOI18N
    }
    
    public boolean isActive() {
        return cbActive.isSelected();
    }
    
    public String getProviderName() {
        return tfName.getText().trim();
    }
    
    public String getProviderURL() {
        return tfURL.getText().trim();
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lName = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lURL = new javax.swing.JLabel();
        tfURL = new javax.swing.JTextField();
        cbActive = new javax.swing.JCheckBox();
        errorLabel = new javax.swing.JLabel();

        lName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lName, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lName.text")); // NOI18N

        lURL.setLabelFor(tfURL);
        org.openide.awt.Mnemonics.setLocalizedText(lURL, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lURL.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbActive, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.cbActive.text")); // NOI18N
        cbActive.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lName)
                            .addComponent(lURL))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfURL, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                            .addComponent(tfName, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                            .addComponent(cbActive))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lName)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbActive)
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lURL)
                    .addComponent(tfURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        lName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lName.adesc")); // NOI18N
        lURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lURL.adesc")); // NOI18N
        cbActive.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.cbActive.adesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbActive;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel lName;
    private javax.swing.JLabel lURL;
    private javax.swing.JTextField tfName;
    private javax.swing.JTextField tfURL;
    // End of variables declaration//GEN-END:variables
    
}
