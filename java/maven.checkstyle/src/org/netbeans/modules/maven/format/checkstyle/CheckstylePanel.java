/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



package org.netbeans.modules.maven.format.checkstyle;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
http://checkstyle.sourceforge.net/config_blocks.html#LeftCurly
http://checkstyle.sourceforge.net/config_blocks.html#RightCurly
http://checkstyle.sourceforge.net/config_sizes.html#LineLength
http://checkstyle.sourceforge.net/config_blocks.html#NeedBraces
http://checkstyle.sourceforge.net/config_whitespace.html#WhitespaceAfter
http://checkstyle.sourceforge.net/config_whitespace.html#WhitespaceAround
http://checkstyle.sourceforge.net/config_whitespace.html#ParenPad
 *
 * @author mkleint
 */
public class CheckstylePanel extends javax.swing.JPanel implements HelpCtx.Provider {
    private final ModelHandle2 handle;
    private final ProjectCustomizer.Category category;
    private boolean generated = false;
    private final CheckBoxUpdater checkboxUpdater;


    CheckstylePanel(ModelHandle2 hndl, ProjectCustomizer.Category cat) {
        initComponents();
        this.handle = hndl;
        category = cat;
        checkboxUpdater = new CheckBoxUpdater(cbEnable) {
            
            private String modifiedValue;
            
            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            @Override
                public void performOperation(POMModel model) {
                    Properties modprops = model.getProject().getProperties();
                    if (modprops == null) {
                        modprops = model.getFactory().createProperties();
                        model.getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_CHECKSTYLE_FORMATTING, modifiedValue);
                }
                
            };
            
            @Override
            public Boolean getValue() {
                String val = modifiedValue;
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_CHECKSTYLE_FORMATTING);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, true);
                }
                if (val != null) {
                    Boolean ret = Boolean.parseBoolean(val);
                    return ret;
                }
                return null;
            }

            @Override
            public boolean getDefaultValue() {
                return Boolean.FALSE;
            }

            @Override
            public void setValue(Boolean value) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                
                String val = value != null ? value.toString() : null;
                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.

                if (handle.getProject().getProperties().containsKey(Constants.HINT_CHECKSTYLE_FORMATTING)) {
                    modifiedValue = val;
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, null, true);
                    }
                    return;
                }
                handle.setRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, val, true);
            }
        };

        btnLearnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLearnMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://maven.apache.org/plugins/maven-checkstyle-plugin"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        });

    }

    @Override
    public void addNotify() {
        super.addNotify();
        boolean defines = AuxPropsImpl.definesCheckStyle(handle.getProject());
        boolean missing = !defines && !generated;
        lblMissing.setVisible(missing);
        btnMissing.setVisible(missing);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbEnable = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblMissing = new javax.swing.JLabel();
        btnMissing = new javax.swing.JButton();
        btnLearnMore = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(cbEnable, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.cbEnable.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.lblHint.text")); // NOI18N
        lblHint.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(lblMissing, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.lblMissing.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMissing, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.btnMissing.text")); // NOI18N
        btnMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMissingActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnLearnMore, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.btnLearnMore.text")); // NOI18N
        btnLearnMore.setBorderPainted(false);
        btnLearnMore.setContentAreaFilled(false);
        btnLearnMore.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbEnable)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblMissing)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMissing))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbEnable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMissing)
                    .addComponent(btnMissing))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMissingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMissingActionPerformed
        generated = true;
        //generate now
        handle.addPOMModification(new ModelOperation<POMModel>() {

            @Override
            public void performOperation(POMModel mdl) {
        Reporting rep = mdl.getProject().getReporting();
        if (rep == null) {
            rep = mdl.getFactory().createReporting();
            mdl.getProject().setReporting(rep);
        }
        ReportPlugin plg = rep.findReportPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_CHECKSTYLE);
        if (plg == null) {
            plg = mdl.getFactory().createReportPlugin();
            plg.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plg.setArtifactId(Constants.PLUGIN_CHECKSTYLE);
            Configuration conf = mdl.getFactory().createConfiguration();
            conf.setSimpleParameter("configLocation", "config/sun_checks.xml"); //NOI18N
            plg.setConfiguration(conf);
            rep.addReportPlugin(plg);
        }
            }
        });
        
        //hide the button, we're done
        lblMissing.setVisible(false);
        btnMissing.setVisible(false);


    }//GEN-LAST:event_btnMissingActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLearnMore;
    private javax.swing.JButton btnMissing;
    private javax.swing.JCheckBox cbEnable;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMissing;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("maven_settings");
    } 
}
