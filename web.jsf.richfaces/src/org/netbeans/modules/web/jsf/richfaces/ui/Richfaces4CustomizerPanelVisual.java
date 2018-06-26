/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * Richfaces4CustomizerPanelVisual.java
 *
 * Created on Jun 22, 2011, 1:25:30 PM
 */
package org.netbeans.modules.web.jsf.richfaces.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.web.jsf.richfaces.Richfaces4Customizer;
import org.netbeans.modules.web.jsf.richfaces.Richfaces4Implementation;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class Richfaces4CustomizerPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider {

    public static final RequestProcessor RP = new RequestProcessor("JSF Component Libraries Updater", 1);
    public static final Logger LOGGER = Logger.getLogger(Richfaces4CustomizerPanelVisual.class.getName());

    private volatile Set<Library> richfacesLibraries = new HashSet<Library>();
    private final Richfaces4Customizer customizer;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    /** Creates new form Richfaces4CustomizerPanelVisual */
    public Richfaces4CustomizerPanelVisual(Richfaces4Customizer customizer) {
        this.customizer = customizer;
        initComponents();
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Richfaces4CustomizerPanelVisual.this.customizer.fireChange();
            }
        });

        initLibraries(true);

        richfacesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void initLibraries(final boolean firstInit) {
        long time = System.currentTimeMillis();
        final List<String> registeredRichfaces = new ArrayList<String>();

        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                for (Library library : Richfaces4Customizer.getRichfacesLibraries()) {
                    registeredRichfaces.add(library.getDisplayName());
                    richfacesLibraries.add(library);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setLibrariesComboBox(richfacesComboBox, registeredRichfaces);

                        if (firstInit && !richfacesLibraries.isEmpty()) {
                            setDefaultComboBoxValues();
                        } else {
                            customizer.setFixedLibrary(!registeredRichfaces.isEmpty());
                            changeSupport.fireChange();
                        }
                    }
                });
            }
        });

        LOGGER.log(Level.FINEST, "Time spent in {0} initLibraries = {1} ms", //NOI18N
                new Object[]{this.getClass().getName(), System.currentTimeMillis() - time});   //NOI18N
    }

    private void setDefaultComboBoxValues() {
        Preferences preferences = Richfaces4Implementation.getRichfacesPreferences();
        richfacesComboBox.setSelectedItem(preferences.get(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, ""));
    }

    private void setLibrariesComboBox(JComboBox comboBox, List<String> items) {
        comboBox.setModel(new DefaultComboBoxModel(items.toArray()));
        comboBox.setEnabled(!items.isEmpty());
    }

    public String getErrorMessage() {
        if (richfacesLibraries == null || richfacesLibraries.isEmpty()) {
            return NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "LBL_MissingRichFaces"); //NOI18N
        }
        return null;
    }

    public String getWarningMessage() {
        if (richfacesLibraries == null || !richfacesLibraries.isEmpty()) {
            Library library = LibraryManager.getDefault().getLibrary(getRichFacesLibrary());
            if (library == null) {
                return null;
            }

            List<URL> content = library.getContent("classpath"); //NOI18N
            StringBuilder recommendedJars = new StringBuilder();
            if (library != null) {
                Set<Entry<String, String>> entrySet = Richfaces4Implementation.RF_DEPENDENCIES.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    try {
                        if (!ClasspathUtil.containsClass(content, entry.getKey())) {
                            recommendedJars.append(entry.getValue()).append(", ");
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }
                if (!"".equals(recommendedJars.toString())) {
                    return NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class,
                            "LBL_MissingDependency", recommendedJars .toString().substring(0,
                            recommendedJars .toString().length() - 2)); //NOI18N
                }
            }
        }
        return null;
    }

    public String getRichFacesLibrary() {
        return (String)richfacesComboBox.getSelectedItem();
    }

   @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(Richfaces4CustomizerPanelVisual.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        richfacesLibraryLabel = new javax.swing.JLabel();
        richfacesComboBox = new javax.swing.JComboBox();
        newLibraryButton = new javax.swing.JButton();
        richfacesInfoLabel = new javax.swing.JLabel();

        richfacesLibraryLabel.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.richfacesLibraryLabel.text")); // NOI18N

        richfacesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Search RichFaces Libraries..." }));

        newLibraryButton.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.newLibraryButton.text")); // NOI18N
        newLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLibraryButtonActionPerformed(evt);
            }
        });

        richfacesInfoLabel.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.richfacesInfoLabel.text")); // NOI18N
        richfacesInfoLabel.setPreferredSize(new java.awt.Dimension(100, 15));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(richfacesLibraryLabel)
                        .addGap(11, 11, 11)
                        .addComponent(richfacesComboBox, 0, 395, Short.MAX_VALUE))
                    .addComponent(newLibraryButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(richfacesInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(richfacesLibraryLabel)
                    .addComponent(richfacesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newLibraryButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(richfacesInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLibraryButtonActionPerformed
        LibrariesCustomizer.showCreateNewLibraryCustomizer(LibraryManager.getDefault());
        initLibraries(false);
    }//GEN-LAST:event_newLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton newLibraryButton;
    private javax.swing.JComboBox richfacesComboBox;
    private javax.swing.JLabel richfacesInfoLabel;
    private javax.swing.JLabel richfacesLibraryLabel;
    // End of variables declaration//GEN-END:variables

}
