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

package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/**
 *
 * @author Martin Adamek
 */
public class PersistenceLibraryPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "PersistenceLibraryPanel_isValid"; //NOI18N
    
    private LibraryImplementation libImpl;
    private Color nbErrorForeground;
    private Color nbWarningForeground;
    
    static final String ERROR_GIF = "org/netbeans/modules/dialogs/error.gif"; //NOI18N
    static final String WARNING_GIF = "org/netbeans/modules/dialogs/warning.gif"; //NOI18N
    
    public PersistenceLibraryPanel(LibraryImplementation libImpl) {
        initComponents();
        this.libImpl = libImpl;
        J2SEVolumeCustomizer classPathCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_CLASSPATH);
        classPathCustomizer.setObject(libImpl);
        tabbedPane.add(classPathCustomizer);
        J2SEVolumeCustomizer srcCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_SRC);
        srcCustomizer.setObject(libImpl);
        tabbedPane.add(srcCustomizer);
        J2SEVolumeCustomizer javadocCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_JAVADOC);
        javadocCustomizer.setObject(libImpl);
        tabbedPane.add(javadocCustomizer);
        tabbedPane.setMnemonicAt(0, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_ClasspathTab").charAt(0)); // NOI18N
        tabbedPane.setMnemonicAt(1, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_SourcesTab").charAt(0)); // NOI18N
        tabbedPane.setMnemonicAt(2, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_JavadocTab").charAt(0)); // NOI18N
        // set foreground color for error messages
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        // set foreground color for warning messages
        nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (nbWarningForeground == null) {
            nbWarningForeground = new Color(51, 51, 51); // Label.foreground
        }
        // create default name for new library
        LibraryManager lm = LibraryManager.getDefault();
        String libraryName = "PersistenceLibrary";
        int index = 1;
        while (lm.getLibrary(libraryName + index) != null) {
            index++;
        }
        libraryNameTextField.setText(libraryName + index);
        // listen on libray name changes
        libraryNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
        // listen on library changes (added/removed content)
        libImpl.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                checkValidity();
            }
        });
    }
    
    @Override
     public void addNotify() {
        super.addNotify();
        this.libraryNameTextField.requestFocus();
        this.libraryNameTextField.selectAll();
    }
    
    public void apply() {
        libImpl.setName(libraryNameTextField.getText().trim());
        PersistenceLibrarySupport.getDefault().addLibrary(libImpl);
    }
    
    void checkValidity() {
        String libraryName = libraryNameTextField.getText();
        if (libraryName.trim().equals("")) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_EmptyName"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (LibraryManager.getDefault().getLibrary(libraryName) != null) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_LibraryExists"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (!containsEntityManager()) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_NoEntityManager"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (!containsPersistenceProvider()) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_NoPersistenceProvider"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else {
            setErrorMessage("", true);
            firePropertyChange(IS_VALID, false, true);
        }
    }
    
    private void setErrorMessage(String msg, Boolean canContinue) {
        errorMessage.setForeground(nbErrorForeground);
        if (msg != null && msg.trim().length() > 0 && canContinue != null) {
            if (canContinue.booleanValue()) {
                errorMessage.setIcon(ImageUtilities.loadImageIcon(WARNING_GIF, false));
                errorMessage.setForeground(nbWarningForeground);
            } else {
                errorMessage.setIcon(ImageUtilities.loadImageIcon(ERROR_GIF, false));
            }
            errorMessage.setToolTipText(msg);
        } else {
            errorMessage.setIcon(null);
            errorMessage.setToolTipText(null);
        }
        
        errorMessage.setText(msg);
    }
    
    private boolean containsEntityManager() {
        return PersistenceLibrarySupport.containsClass(libImpl, "javax.persistence.EntityManager"); //NOI18N
    }
    
    private boolean containsPersistenceProvider() {
        return PersistenceLibrarySupport.containsService(libImpl, "javax.persistence.spi.PersistenceProvider"); //NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        libraryNameTextField = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        errorMessage = new javax.swing.JLabel();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/library/Bundle").getString("MNE_LibraryName").charAt(0));
        jLabel1.setLabelFor(libraryNameTextField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceLibraryPanel.class, "LBL_LibraryName")); // NOI18N

        errorMessage.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(libraryNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                    .addComponent(errorMessage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(libraryNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorMessage)
                .addContainerGap())
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/library/Bundle"); // NOI18N
        libraryNameTextField.getAccessibleContext().setAccessibleName(bundle.getString("LBL_LibraryName")); // NOI18N
        libraryNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField libraryNameTextField;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
