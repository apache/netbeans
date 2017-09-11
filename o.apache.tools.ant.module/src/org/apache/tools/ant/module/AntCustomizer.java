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

package org.apache.tools.ant.module;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * Implementation of one panel in Options Dialog.
 * @author Jan Jancura, Jesse Glick
 */
@OptionsPanelController.Keywords(keywords={"ant"}, location=JavaOptions.JAVA, tabTitle= "#Ant")
public class AntCustomizer extends JPanel implements ActionListener {
    
    private static final RequestProcessor RP = new RequestProcessor(AntCustomizer.class);

    private List<File> classpath = Collections.emptyList();
    private Map<String,String> properties = Collections.emptyMap();
    private boolean         changed = false;
    private boolean         listen = false;
    private File            originalAntHome;
    private final Node.Property classpathProperty;
    private final Node.Property propertiesProperty;

    public AntCustomizer() {
        initComponents();
        bAntHome.addActionListener (this);
        ((DefaultComboBoxModel) cbVerbosity.getModel()).removeAllElements(); // just have prototype for form editor
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_warn"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_info"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_verbose"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_debug"));
        cbSaveFiles.addActionListener (this);
        cbReuseOutput.addActionListener (this);
        cbAlwaysShowOutput.addActionListener (this);
        cbVerbosity.addActionListener (this);
        classpathProperty = new PropertySupport.ReadWrite<NbClassPath>("classpath", NbClassPath.class, null, null) {
            public NbClassPath getValue() throws IllegalAccessException, InvocationTargetException {
                return new NbClassPath(classpath.toArray(new File[classpath.size()]));
            }
            public void setValue(NbClassPath val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                String cp = val.getClassPath();
                if (cp.startsWith("\"") && cp.endsWith("\"")) {
                    // *@%!* NbClassPath.getClassPath semantics.
                    cp = cp.substring(1, cp.length() - 1);
                }
                classpath = new ArrayList<File>();
                for (String f : cp.split(Pattern.quote(File.pathSeparator))) {
                    if(!f.trim().isEmpty()) {
                        classpath.add(new File(f));
                    }
                }
                fireChanged();
            }
        };
        propertiesProperty = new PropertySupport.ReadWrite<Properties>("properties", Properties.class, null, null) {
            public Properties getValue() throws IllegalAccessException, InvocationTargetException {
                Properties p = new Properties();
                p.putAll(properties);
                return p;
            }
            public void setValue(Properties val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                properties = NbCollections.checkedMapByCopy(val, String.class, String.class, true);
                fireChanged();
            }
        };
        setUpPropertyPanels();
    }

    private void setUpPropertyPanels() {
        classpathPanel.removeAll();
        PropertyPanel pp = new PropertyPanel(classpathProperty, PropertyPanel.PREF_CUSTOM_EDITOR);
        classpathPanel.add(pp);
        classpathLabel.setLabelFor(pp);
        propertiesPanel.removeAll();
        pp = new PropertyPanel(propertiesProperty, PropertyPanel.PREF_CUSTOM_EDITOR);
        propertiesPanel.add(pp);
        propertiesLabel.setLabelFor(pp);
    }
    
    void update () {
        listen = false;
        classpath = AntSettings.getExtraClasspath();
        properties = AntSettings.getProperties();
        setUpPropertyPanels();
        originalAntHome = AntSettings.getAntHome();
            
        tfAntHome.setText(originalAntHome != null ? originalAntHome.toString() : null);
        cbSaveFiles.setSelected(AntSettings.getSaveAll());
        cbReuseOutput.setSelected(AntSettings.getAutoCloseTabs());
        cbAlwaysShowOutput.setSelected(AntSettings.getAlwaysShowOutput());
        cbVerbosity.setSelectedIndex(AntSettings.getVerbosity() - 1);
        updateAntVersion();
        changed = false;
        initialized = true;
        listen = true;
    }
    
    private void updateAntVersion() { // #107094: asynch, since it can be slow
        lAntVersion.setText(NbBundle.getMessage(AntCustomizer.class, "LBL_please_wait"));
        RP.post(new Runnable() {
            public void run() {
                final String version = AntSettings.getAntVersion();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        lAntVersion.setText("(" + version + ")");
                    }
                });
            }
        });
    }
    
    private boolean initialized = false;
    
    void applyChanges () {
        if (!initialized) return;
        String antHome = tfAntHome.getText ().trim ();
        AntSettings.setAntHome(new File(antHome));
        if (AntSettings.getAutoCloseTabs() != cbReuseOutput.isSelected()) {
            AntSettings.setAutoCloseTabs(cbReuseOutput.isSelected());
        }
        if (AntSettings.getSaveAll() != cbSaveFiles.isSelected()) {
            AntSettings.setSaveAll(cbSaveFiles.isSelected());
        }
        if (AntSettings.getAlwaysShowOutput() != cbAlwaysShowOutput.isSelected()) {
            AntSettings.setAlwaysShowOutput(cbAlwaysShowOutput.isSelected());
        }
        if (AntSettings.getVerbosity() != cbVerbosity.getSelectedIndex() + 1) {
            AntSettings.setVerbosity(cbVerbosity.getSelectedIndex() + 1);
        }
        if (!AntSettings.getProperties().equals(properties)) {
            AntSettings.setProperties(properties);
        }
        if (!AntSettings.getExtraClasspath().equals(classpath)) {
            AntSettings.setExtraClasspath(classpath);
        }
        changed = false;
    }
    
    void cancel () {
        if (AntSettings.getAntHome() != originalAntHome) {
            AntSettings.setAntHome(originalAntHome);
        }
        changed = false;
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    private void fireChanged() {
        changed = !AntSettings.getAntHome().getPath().equals(tfAntHome.getText().trim())
                || AntSettings.getAutoCloseTabs() != cbReuseOutput.isSelected()
                || AntSettings.getSaveAll() != cbSaveFiles.isSelected()
                || AntSettings.getAlwaysShowOutput() != cbAlwaysShowOutput.isSelected()
                || AntSettings.getVerbosity() != cbVerbosity.getSelectedIndex() + 1
                || !AntSettings.getProperties().equals(properties)
                || !AntSettings.getExtraClasspath().equals(classpath);
    }
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        Object o = e.getSource ();
        if (o == cbAlwaysShowOutput) {
            fireChanged();
        } else
        if (o == cbReuseOutput) {
            fireChanged();
        } else
        if (o == cbSaveFiles) {
            fireChanged();
        } else
        if (o == cbVerbosity) {
            fireChanged();
        } else
        if (o == bAntHome) {
            JFileChooser chooser = new JFileChooser (tfAntHome.getText ());
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            int r = chooser.showDialog (
                SwingUtilities.getWindowAncestor (this),
                NbBundle.getMessage(AntCustomizer.class, "Select_Directory")
            );
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile ();
                if (!new File (new File (file, "lib"), "ant.jar").isFile ()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(AntCustomizer.class, "Not_a_ant_home", file),
                        NotifyDescriptor.Message.WARNING_MESSAGE
                    ));
                    return;
                }
                tfAntHome.setText (file.getAbsolutePath ());
                AntSettings.setAntHome(file);
                updateAntVersion();
                fireChanged();
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel antHomeLabel = new javax.swing.JLabel();
        tfAntHome = new javax.swing.JTextField();
        bAntHome = new javax.swing.JButton();
        bAntHomeDefault = new javax.swing.JButton();
        lAntVersion = new javax.swing.JLabel();
        cbSaveFiles = new javax.swing.JCheckBox();
        cbReuseOutput = new javax.swing.JCheckBox();
        cbAlwaysShowOutput = new javax.swing.JCheckBox();
        cbVerbosity = new javax.swing.JComboBox();
        javax.swing.JLabel verbosityLabel = new javax.swing.JLabel();
        classpathLabel = new javax.swing.JLabel();
        classpathPanel = new javax.swing.JPanel();
        propertiesLabel = new javax.swing.JLabel();
        propertiesPanel = new javax.swing.JPanel();

        antHomeLabel.setLabelFor(tfAntHome);
        org.openide.awt.Mnemonics.setLocalizedText(antHomeLabel, NbBundle.getMessage(AntCustomizer.class, "Ant_Home")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bAntHome, NbBundle.getMessage(AntCustomizer.class, "Ant_Home_Button")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bAntHomeDefault, NbBundle.getMessage(AntCustomizer.class, "Ant_Home_Default_Button")); // NOI18N
        bAntHomeDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAntHomeDefaultActionPerformed(evt);
            }
        });

        lAntVersion.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(lAntVersion, org.openide.util.NbBundle.getMessage(AntCustomizer.class, "AntCustomizer.lAntVersion.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSaveFiles, NbBundle.getMessage(AntCustomizer.class, "Save_Files")); // NOI18N
        cbSaveFiles.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbReuseOutput, NbBundle.getMessage(AntCustomizer.class, "Reuse_Output")); // NOI18N
        cbReuseOutput.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbAlwaysShowOutput, NbBundle.getMessage(AntCustomizer.class, "Always_Show_Output")); // NOI18N
        cbAlwaysShowOutput.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        cbVerbosity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal" }));

        verbosityLabel.setLabelFor(cbVerbosity);
        org.openide.awt.Mnemonics.setLocalizedText(verbosityLabel, NbBundle.getMessage(AntCustomizer.class, "Verbosity")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(classpathLabel, org.openide.util.NbBundle.getMessage(AntCustomizer.class, "AntCustomizer.classpathLabel.text")); // NOI18N

        classpathPanel.setBackground(new java.awt.Color(153, 0, 204));
        classpathPanel.setForeground(new java.awt.Color(153, 0, 204));
        classpathPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(propertiesLabel, org.openide.util.NbBundle.getMessage(AntCustomizer.class, "AntCustomizer.propertiesLabel.text")); // NOI18N

        propertiesPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(antHomeLabel)
                .addGap(49, 49, 49)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbAlwaysShowOutput)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lAntVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(tfAntHome, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(bAntHome)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(bAntHomeDefault)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cbReuseOutput)
                                .addComponent(cbSaveFiles))
                            .addContainerGap()))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(verbosityLabel)
                    .addComponent(classpathLabel)
                    .addComponent(propertiesLabel))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbVerbosity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classpathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(antHomeLabel)
                    .addComponent(bAntHomeDefault)
                    .addComponent(bAntHome)
                    .addComponent(tfAntHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lAntVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSaveFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbReuseOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAlwaysShowOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbVerbosity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(verbosityLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(classpathLabel)
                    .addComponent(classpathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(propertiesLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bAntHomeDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAntHomeDefaultActionPerformed
        AntSettings.setAntHome(null);
        File antHome = AntSettings.getAntHome();
        if (antHome != null) {
            tfAntHome.setText(antHome.getAbsolutePath());
        } else {
            tfAntHome.setText(null);
        }
        updateAntVersion();
        fireChanged();
    }//GEN-LAST:event_bAntHomeDefaultActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAntHome;
    private javax.swing.JButton bAntHomeDefault;
    private javax.swing.JCheckBox cbAlwaysShowOutput;
    private javax.swing.JCheckBox cbReuseOutput;
    private javax.swing.JCheckBox cbSaveFiles;
    private javax.swing.JComboBox cbVerbosity;
    private javax.swing.JLabel classpathLabel;
    private javax.swing.JPanel classpathPanel;
    private javax.swing.JLabel lAntVersion;
    private javax.swing.JLabel propertiesLabel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JTextField tfAntHome;
    // End of variables declaration//GEN-END:variables
    
}
