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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.execute.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.PropertySplitter;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  mkleint
 */
public class RunGoalsPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(RunGoalsPanel.class);

    private List<NetbeansActionMapping> historyMappings;
    private int historyIndex = 0;
    private TextValueCompleter goalcompleter;
    private TextValueCompleter profilecompleter;
    private NbMavenProjectImpl project;

    /** Creates new form RunGoalsPanel */
    public RunGoalsPanel() {
        initComponents();
        cbRememberActionPerformed(null);
        historyMappings = new ArrayList<NetbeansActionMapping>();
        btnPrev.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/back.png", false)); //NOI18N
        btnNext.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/forward.png", false)); //NOI18N

        goalcompleter = new TextValueCompleter(new ArrayList<String>(0), txtGoals, " "); //NOI18N
        goalcompleter.setLoading(true);
        // doing lazy.. 
        RP.post(new Runnable() {
            @Override public void run() {
                GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
                if (provider != null) {
                    final Set<String> strs = provider.getAvailableGoals();
                    try {
                        List<String> phases = EmbedderFactory.getProjectEmbedder().getLifecyclePhases();
                        strs.addAll(phases);
                    } catch (Exception e) {
                        // oh wel just ignore..
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            goalcompleter.setValueList(strs, false);//do not bother about partial results, too many intermediate apis..
                        }
                    });
                }
            }
        });

        profilecompleter = new TextValueCompleter(new ArrayList<String>(0), txtProfiles, " ");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        txtGoals.requestFocus();

    }

    private void readProfiles(final Project mavenProject) {
        profilecompleter.setLoading(true);
        RP.post(new Runnable() {
            @Override public void run() {
                ProjectProfileHandler profileHandler = mavenProject.getLookup().lookup(ProjectProfileHandler.class);
                final List<String> ret = profileHandler.getAllProfiles();
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        profilecompleter.setValueList(ret, false);
                    }
                });
            }
        });
    }

    private static String createSpaceSeparatedList(List<String> list) {
        StringBuilder b = new StringBuilder();
        for (String s : list) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(s);
        }
        return b.toString();
    }

    public void readMapping(NetbeansActionMapping mapp, NbMavenProjectImpl project, ActionToGoalMapping historyMappings) {
        this.historyMappings.clear();
        this.historyMappings.addAll(historyMappings.getActions());
        this.historyMappings.add(mapp);
        historyIndex = this.historyMappings.size();
        readProfiles(project);
        this.project = project;
        moveHistory(-1);
    }

    public void readConfig(final RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        }
        historyMappings.clear();
        btnNext.setVisible(false);
        btnPrev.setVisible(false);
        txtGoals.setText(createSpaceSeparatedList(config.getGoals()));
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
            if (buf.length() > 0) {
                buf.append('\n');// NOI18N
            }
            buf.append(entry.getKey()).append('=').append(entry.getValue());// NOI18N
        }
        epProperties.setText(ActionMappings.createPropertiesList(config.getProperties()));
        epProperties.setCaretPosition(0);
        txtProfiles.setText(createSpaceSeparatedList(config.getActivatedProfiles()));
        
        setUpdateSnapshots(config.isUpdateSnapshots());
        setOffline(config.isOffline() != null ? config.isOffline().booleanValue() : false);
        setRecursive(config.isRecursive());
        setShowDebug(config.isShowDebug());
        if(config.getProject()!=null){
            readProfiles(config.getProject());
        }
    }

    private void readMapping(NetbeansActionMapping mapp) {
        txtGoals.setText(createSpaceSeparatedList(mapp.getGoals()));
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String,String> entry : mapp.getProperties().entrySet()) {
            if (buf.length() > 0) {
                buf.append('\n');// NOI18N
            }
            buf.append(entry.getKey()).append('=').append(entry.getValue());// NOI18N
        }
        epProperties.setText(buf.toString());
        epProperties.setCaretPosition(0);
        txtProfiles.setText(createSpaceSeparatedList(mapp.getActivatedProfiles()));
    }

    public void applyValues(NetbeansActionMapping mapp) {
        StringTokenizer tok = new StringTokenizer(txtGoals.getText().trim());
        List<String> lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        mapp.setGoals(lst.size() > 0 ? lst : null);

        mapp.setProperties(ActionMappings.convertStringToActionProperties(epProperties.getText()));

        tok = new StringTokenizer(txtProfiles.getText().trim(), " ,");
        lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        mapp.setActivatedProfiles(lst);
        mapp.setRecursive(cbRecursive.isSelected());

    }

    public void applyValues(BeanRunConfig rc) {
        StringTokenizer tok = new StringTokenizer(txtGoals.getText().trim());
        List<String> lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        rc.setGoals(lst.size() > 0 ? lst : Collections.singletonList("install")); //NOI18N
        tok = new StringTokenizer(txtProfiles.getText().trim());
        lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        rc.setActivatedProfiles(lst);

        PropertySplitter split = new PropertySplitter(epProperties.getText());
        String token = split.nextPair();
        while (token != null) {
            String[] prp = StringUtils.split(token, "=", 2); //NOI18N
            if (prp.length == 2) {
                rc.setProperty(prp[0], prp[1]);
            }
            token = split.nextPair();
        }
        rc.setRecursive(isRecursive());
        rc.setShowDebug(isShowDebug());
        rc.setUpdateSnapshots(isUpdateSnapshots());
        rc.setOffline(Boolean.valueOf(isOffline()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblGoals = new javax.swing.JLabel();
        txtGoals = new javax.swing.JTextField();
        lblProfiles = new javax.swing.JLabel();
        txtProfiles = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbRecursive = new javax.swing.JCheckBox();
        cbOffline = new javax.swing.JCheckBox();
        cbDebug = new javax.swing.JCheckBox();
        cbUpdateSnapshots = new javax.swing.JCheckBox();
        btnNext = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        cbRemember = new javax.swing.JCheckBox();
        txtRemember = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        btnAddProps = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        epProperties = new javax.swing.JEditorPane();

        lblGoals.setLabelFor(txtGoals);
        org.openide.awt.Mnemonics.setLocalizedText(lblGoals, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Goals")); // NOI18N

        lblProfiles.setLabelFor(txtProfiles);
        org.openide.awt.Mnemonics.setLocalizedText(lblProfiles, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Profiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Properties")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbRecursive, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Recursive")); // NOI18N
        cbRecursive.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRecursive.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbOffline, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Offline")); // NOI18N
        cbOffline.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbOffline.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Debug")); // NOI18N
        cbDebug.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbDebug.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbUpdateSnapshots, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Update_Snapshots")); // NOI18N
        cbUpdateSnapshots.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbUpdateSnapshots.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "TIP_Next")); // NOI18N
        btnNext.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "TIP_Prev")); // NOI18N
        btnPrev.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRemember, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Remember")); // NOI18N
        cbRemember.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRemember.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbRemember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRememberActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnAddProps, "&Add >");
        btnAddProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPropsActionPerformed(evt);
            }
        });

        epProperties.setContentType("text/x-properties"); // NOI18N
        jScrollPane2.setViewportView(epProperties);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbRecursive)
                            .addComponent(cbOffline))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbDebug)
                            .addComponent(cbUpdateSnapshots)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblGoals)
                            .addComponent(lblProfiles)
                            .addComponent(jLabel2)
                            .addComponent(btnAddProps))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtGoals)
                            .addComponent(txtProfiles)
                            .addComponent(jScrollPane2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnPrev)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext)
                        .addGap(52, 52, 52)
                        .addComponent(cbRemember)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRemember, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGoals)
                    .addComponent(txtGoals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfiles)
                    .addComponent(txtProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(39, 39, 39)
                        .addComponent(btnAddProps))
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbRecursive)
                    .addComponent(cbUpdateSnapshots))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbOffline)
                    .addComponent(cbDebug))
                .addGap(34, 34, 34)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrev)
                    .addComponent(btnNext)
                    .addComponent(cbRemember)
                    .addComponent(txtRemember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        moveHistory(1);
    }//GEN-LAST:event_btnNextActionPerformed

    private void moveHistory(int step) {
        historyIndex = historyIndex + step;
        readMapping(historyMappings.get(historyIndex));
        btnPrev.setEnabled(historyIndex != 0);
        btnNext.setEnabled(historyIndex != (historyMappings.size() - 1));
    }

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        moveHistory(-1);
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnAddPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPropsActionPerformed
        ActionMappings.showAddPropertyPopupMenu(btnAddProps, epProperties, txtGoals, project);
    }//GEN-LAST:event_btnAddPropsActionPerformed

    private void cbRememberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRememberActionPerformed
        txtRemember.setEnabled(cbRemember.isSelected());
    }//GEN-LAST:event_cbRememberActionPerformed

    public boolean isOffline() {
        return cbOffline.isSelected();
    }

    public boolean isShowDebug() {
        return cbDebug.isSelected();
    }

    public void setOffline(boolean b) {
        cbOffline.setSelected(b);
    }

    public void setShowDebug(boolean b) {
        cbDebug.setSelected(b);
    }

    public void setUpdateSnapshots(boolean b) {
        cbUpdateSnapshots.setSelected(b);
    }

    public void setRecursive(boolean b) {
        cbRecursive.setSelected(b);
    }

    public boolean isRecursive() {
        return cbRecursive.isSelected();
    }

    public boolean isUpdateSnapshots() {
        return cbUpdateSnapshots.isSelected();
    }

    public String isRememberedAs() {
        if (cbRemember.isSelected()) {
            String txt = txtRemember.getText().trim();
            if (txt.length() > 0) {
                return txt;
            }
        }
        return null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProps;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbOffline;
    private javax.swing.JCheckBox cbRecursive;
    private javax.swing.JCheckBox cbRemember;
    private javax.swing.JCheckBox cbUpdateSnapshots;
    private javax.swing.JEditorPane epProperties;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblGoals;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JTextField txtGoals;
    private javax.swing.JTextField txtProfiles;
    private javax.swing.JTextField txtRemember;
    // End of variables declaration//GEN-END:variables
}
