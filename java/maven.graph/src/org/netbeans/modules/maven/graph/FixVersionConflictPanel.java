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

package org.netbeans.modules.maven.graph;

import org.netbeans.modules.java.graph.GraphNode;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import static org.netbeans.modules.maven.graph.Bundle.*;
import org.openide.util.NbBundle.Messages;
import org.netbeans.modules.java.graph.GraphNodeImplementation;

/**
 *
 * @author Dafe Simonek
 */
public class FixVersionConflictPanel extends javax.swing.JPanel {

    private DependencyGraphScene scene;
    private GraphNode<MavenDependencyNode> conflictNode;
    private List<ArtifactVersion> clashingVersions;
    private ExclusionTargets eTargets;

    public FixVersionConflictPanel (DependencyGraphScene scene, GraphNode<MavenDependencyNode> node) {
        this.scene = scene;
        this.conflictNode = node;

        initComponents();

        ExclTargetRenderer render = new ExclTargetRenderer(excludesList, this);
        excludesList.setCellRenderer(render);
        excludesList.addMouseListener(render);
        excludesList.addKeyListener(render);

        eTargets = new ExclusionTargets(conflictNode, getClashingVersions().get(0));

        visualizeRecommendations(computeRecommendations());
    }

    FixDescription getResult() {
        FixDescription res = new FixDescription();
        res.isSet = addSetCheck.isSelected();
        res.version2Set = res.isSet ? (ArtifactVersion) versionList.getSelectedValue() : null;
        res.isExclude = excludeCheck.isSelected();
        if (res.isExclude) {
            res.exclusionTargets = new HashSet<Artifact>();
            res.conflictParents = new HashSet<MavenDependencyNode>();
            ListModel lm = excludesList.getModel();
            for (int i = 0; i < lm.getSize(); i++) {
                ExclTargetEntry entry = (ExclTargetEntry) lm.getElementAt(i);
                if (entry.isSelected) {
                    res.exclusionTargets.add(entry.artif);
                    res.conflictParents.addAll(eTargets.getConflictParents(entry.artif));
                }
            }
        }
        return res;
    }

    private void addSetCheckChanged() {
        boolean isSel = addSetCheck.isSelected();
        versionL.setEnabled(isSel);
        versionList.setEnabled(isSel);
        if (isSel && versionList.getSelectedValue() == null) {
            versionList.setSelectedIndex(0);
        }
    }

    private void excludeCheckChanged() {
        boolean isSel = excludeCheck.isSelected();
        fromDirectL.setEnabled(isSel);
        excludesList.setEnabled(isSel);
    }

    private String getClashingVersionsAsText () {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (ArtifactVersion av : getClashingVersions()) {
            if (!isFirst) {
                sb.append(", "); //NOI18N
            } else {
                isFirst = false;
            }
            sb.append(av.toString());
        }
        return sb.toString();
    }

    private List<ArtifactVersion> getClashingVersions () {
        if (clashingVersions == null) {
            clashingVersions = new ArrayList<ArtifactVersion>();
            clashingVersions.add(new DefaultArtifactVersion(conflictNode.getImpl().getArtifact().getVersion()));
            Set<MavenDependencyNode> deps = conflictNode.getDuplicatesOrConflicts();
            ArtifactVersion av = null;
            for (MavenDependencyNode dn : deps) {
                if (dn.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                    av = new DefaultArtifactVersion(dn.getArtifact().getVersion());
                    if (!clashingVersions.contains(av)) {
                        clashingVersions.add(av);
                    }
                }
            }
            Collections.sort(clashingVersions);
            Collections.reverse(clashingVersions);
        }
        return clashingVersions;
    }


    static class FixDescription {
        boolean isSet = false;
        boolean isExclude = false;
        ArtifactVersion version2Set = null;
        Set<Artifact> exclusionTargets = null;
        Set<MavenDependencyNode> conflictParents = null;
    }

    /** Checks the circumstances of version conflict and offers solution.
     *
     * @return description of found recommended solution
     */
    private FixDescription computeRecommendations() {
        FixDescription recs = new FixDescription();

        boolean isDirect = conflictNode.getPrimaryLevel() == 1;
        ArtifactVersion usedVersion = new DefaultArtifactVersion(
                conflictNode.getImpl().getArtifact().getVersion());
        ArtifactVersion newAvailVersion = getClashingVersions().get(0);

        // case: direct dependency to older version -> recommend update to newer
        if (isDirect && usedVersion.compareTo(newAvailVersion) < 0) {
            recs.isSet = true;
            recs.version2Set = newAvailVersion;
        }

        // case: more then one exclusion target, several of them are "good guys"
        // which means they have non conflicting dependency on newer version ->
        // recommend adding dependency exclusion to all but mentioned "good" targets
        Set<Artifact> nonConf = eTargets.getNonConflicting();
        if (!nonConf.isEmpty() && eTargets.getAll().size() > 1) {
            recs.isExclude = true;
            recs.exclusionTargets = eTargets.getConflicting();
        }

        // last try - brute force -> recommend exclude all and add dependency in some cases
        if (!recs.isSet && !recs.isExclude) {
            if (usedVersion.compareTo(newAvailVersion) < 0) {
                recs.isSet = true;
                recs.version2Set = newAvailVersion;
                recs.isExclude = true;
                recs.exclusionTargets = eTargets.getAll();
            }
        }

        return recs;
    }

    @Messages({
        "FixVersionConflictPanel.addSetCheck.text={0} Direct Dependency",
        "FixVersionConflictPanel.excludeCheck.text=Exclude Transitive Dependency"
    })
    private void visualizeRecommendations(FixDescription recs) {
        addSetCheck.setText(FixVersionConflictPanel_addSetCheck_text(getSetText()));
        addSetCheck.setSelected(recs.isSet);
        addSetCheckChanged();

        List<ArtifactVersion> versions = getClashingVersions();
        DefaultListModel<ArtifactVersion> model = new DefaultListModel<>();
        for (ArtifactVersion av : versions) {
            model.addElement(av);
        }
        versionList.setModel(model);
        versionList.setSelectedIndex(0);

        if (recs.version2Set != null) {
            versionList.setSelectedValue(recs.version2Set, true);
        }

        excludeCheck.setText(FixVersionConflictPanel_excludeCheck_text());
        excludeCheck.setSelected(recs.isExclude);
        excludeCheckChanged();

        Set<Artifact> exclTargets = eTargets.getAll();
        if (!exclTargets.isEmpty()) {
            DefaultListModel<ExclTargetEntry> lModel = new DefaultListModel<>();
            for (Artifact exc : exclTargets) {
                lModel.addElement(new ExclTargetEntry(exc,
                        recs.exclusionTargets != null && recs.exclusionTargets.contains(exc)));
            }
            excludesList.setModel(lModel);
        } else {
            excludeCheck.setEnabled(false);
        }

        updateSummary();
    }

    @Messages({
        "LBL_SetDep=Set",
        "LBL_AddDep=Add"
    })
    private String getSetText () {
        return conflictNode.getPrimaryLevel() == 1 ? LBL_SetDep() : LBL_AddDep();
    }

    @Messages({
        "FixVersionConflictPanel.sumPart1.text={0} direct dependency on version <b>{1}</b> of <b>{2}</b>.",
        "FixVersionConflictPanel.sumPart2.text=Exclude transitive dependency on <b>{0}</b> from direct dependency on <b>{1}</b>.",
        "FixVersionConflictPanel.noChanges=No changes.",
        "FixVersionConflictPanel.sumContent.text=<html>{0} {1}</html>"
    })
    private void updateSummary () {
        FixDescription curFix = getResult();
        String part1 = "", part2 = "";
        if (curFix.isSet && curFix.version2Set != null) {
            part1 = FixVersionConflictPanel_sumPart1_text(getSetText(), curFix.version2Set.toString(), conflictNode.getImpl().getArtifact().getArtifactId());
        }
        if (curFix.isExclude && !curFix.exclusionTargets.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Artifact art : curFix.exclusionTargets) {
                if (!isFirst) {
                    sb.append(", ");
                } else {
                    isFirst = false;
                }
                sb.append(art.getArtifactId());
            }
            part2 = FixVersionConflictPanel_sumPart2_text(conflictNode.getImpl().getArtifact().getArtifactId(), sb);
        }

        if (part1.isEmpty() && part2.isEmpty()) {
            part1 = FixVersionConflictPanel_noChanges();
        }

        if (!part1.isEmpty() && !part2.isEmpty()) {
            part1 = part1 + " ";
        }

        sumContent.setText(FixVersionConflictPanel_sumContent_text(part1, part2));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSeparator1 = new javax.swing.JSeparator();
        fixesP = new javax.swing.JPanel();
        addSetP = new javax.swing.JPanel();
        addSetCheck = new javax.swing.JCheckBox();
        versionL = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        versionList = new javax.swing.JList();
        excludeP = new javax.swing.JPanel();
        excludeCheck = new javax.swing.JCheckBox();
        fromDirectL = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        excludesList = new javax.swing.JList();
        fixPossibL = new javax.swing.JLabel();
        summaryL = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        sumContent = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        conflictL = new javax.swing.JLabel();

        addSetCheck.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.addSetCheck.text")); // NOI18N
        addSetCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSetCheckActionPerformed(evt);
            }
        });

        versionL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.versionL.text")); // NOI18N

        versionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                versionListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(versionList);

        javax.swing.GroupLayout addSetPLayout = new javax.swing.GroupLayout(addSetP);
        addSetP.setLayout(addSetPLayout);
        addSetPLayout.setHorizontalGroup(
            addSetPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addSetPLayout.createSequentialGroup()
                .addGroup(addSetPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addSetCheck)
                    .addComponent(versionL))
                .addGap(46, 46, 46))
            .addGroup(addSetPLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        addSetPLayout.setVerticalGroup(
            addSetPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addSetPLayout.createSequentialGroup()
                .addComponent(addSetCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        excludeCheck.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.excludeCheck.text")); // NOI18N
        excludeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeCheckActionPerformed(evt);
            }
        });

        fromDirectL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.fromDirectL.text")); // NOI18N

        jScrollPane2.setViewportView(excludesList);

        javax.swing.GroupLayout excludePLayout = new javax.swing.GroupLayout(excludeP);
        excludeP.setLayout(excludePLayout);
        excludePLayout.setHorizontalGroup(
            excludePLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(excludePLayout.createSequentialGroup()
                .addGroup(excludePLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(excludeCheck)
                    .addComponent(fromDirectL))
                .addContainerGap(46, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        excludePLayout.setVerticalGroup(
            excludePLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(excludePLayout.createSequentialGroup()
                .addComponent(excludeCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fromDirectL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout fixesPLayout = new javax.swing.GroupLayout(fixesP);
        fixesP.setLayout(fixesPLayout);
        fixesPLayout.setHorizontalGroup(
            fixesPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fixesPLayout.createSequentialGroup()
                .addComponent(addSetP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fixesPLayout.setVerticalGroup(
            fixesPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addSetP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(excludeP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        fixPossibL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.fixPossibL.text")); // NOI18N

        summaryL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.summaryL.text")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sumContent.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.sumContent.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(sumContent, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        conflictL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.conflictL.text", new Object[] {conflictNode.getImpl().getArtifact().getArtifactId(), getClashingVersionsAsText()})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(conflictL, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryL)
                    .addComponent(fixPossibL)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                            .addComponent(fixesP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixPossibL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixesP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(summaryL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void excludeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeCheckActionPerformed
        excludeCheckChanged();
        updateSummary();
}//GEN-LAST:event_excludeCheckActionPerformed

    private void addSetCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSetCheckActionPerformed
        addSetCheckChanged();
        updateSummary();
    }//GEN-LAST:event_addSetCheckActionPerformed

    private void versionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_versionListValueChanged
        updateSummary();
    }//GEN-LAST:event_versionListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addSetCheck;
    private javax.swing.JPanel addSetP;
    private javax.swing.JLabel conflictL;
    private javax.swing.JCheckBox excludeCheck;
    private javax.swing.JPanel excludeP;
    private javax.swing.JList excludesList;
    private javax.swing.JLabel fixPossibL;
    private javax.swing.JPanel fixesP;
    private javax.swing.JLabel fromDirectL;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel sumContent;
    private javax.swing.JLabel summaryL;
    private javax.swing.JLabel versionL;
    private javax.swing.JList versionList;
    // End of variables declaration//GEN-END:variables


    static class ExclusionTargets {

        // mapping; target artifact for exclusion -> set of versions of conflicting
        // artifact that target currently define by its dependencies
        Map<Artifact, Set<ArtifactVersion>> targets2Versions;

        // mapping; target artifact for exclusion -> related set of parents of conflicting
        // artifact in dependency graph
        Map<Artifact, Set<MavenDependencyNode>> targets2ConfPar;

        GraphNode<MavenDependencyNode> conflictNode;
        ArtifactVersion usedVersion, newestVersion;

        ExclusionTargets(GraphNode<MavenDependencyNode> conflictNode, ArtifactVersion newestVersion) {
            this.conflictNode = conflictNode;
            this.newestVersion = newestVersion;
            this.usedVersion = new DefaultArtifactVersion(
                conflictNode.getImpl().getArtifact().getVersion());

            initialize ();
        }

        private void initialize () {
            targets2Versions = new HashMap<>();
            targets2ConfPar = new HashMap<>();
            MavenDependencyNode curDn = null;
            MavenDependencyNode parent = null;

            List<MavenDependencyNode> allDNs = new ArrayList<MavenDependencyNode>(
                    conflictNode.getDuplicatesOrConflicts());
            
            // prevent conflictNode itself to be included in exclusion targets
            if (conflictNode.getPrimaryLevel() > 1) {
                allDNs.add(conflictNode.getImpl());
            }

            for (MavenDependencyNode dn : allDNs) {
                curDn = dn;
                parent = curDn.getParent();
                // bad luck with no parent...
                if (parent == null) {
                    continue;
                }
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                    curDn = curDn.getParent();
                }
                
                Set<MavenDependencyNode> confPar = targets2ConfPar.get(curDn.getArtifact());
                if (confPar == null) {
                    confPar = new HashSet<MavenDependencyNode>();
                    targets2ConfPar.put(curDn.getArtifact(), confPar);
                }
                confPar.add(dn.getParent());

                Set<ArtifactVersion> versions = targets2Versions.get(curDn.getArtifact());
                if (versions == null) {
                    versions = new HashSet<ArtifactVersion>();
                    targets2Versions.put(curDn.getArtifact(), versions);
                }
                versions.add(new DefaultArtifactVersion(dn.getArtifact().getVersion()));
            }
        }

        public Set<Artifact> getAll () {
            return targets2Versions.keySet();
        }

        /**
         * Find "good guys' between exclusion targets, which means they have non
         * conflicting dependency on newer version and doesn't contribute to conflict
         */
        public Set<Artifact> getNonConflicting () {
            Set<Artifact> result = new HashSet<Artifact>();
            for (Artifact art : getAll()) {
                if (isNonConflicting(art)) {
                    result.add(art);
                }
            }
            return result;
        }

        public Set<Artifact> getConflicting () {
            Set<Artifact> result = new HashSet<Artifact>();
            for (Artifact art : getAll()) {
                if (!isNonConflicting(art)) {
                    result.add(art);
                }
            }
            return result;
        }

        public boolean isNonConflicting (Artifact art) {
            Set<ArtifactVersion> versions = targets2Versions.get(art);
            if (versions != null && versions.size() == 1) {
                if (newestVersion.equals(versions.iterator().next())) {
                    return true;
                }
            }
            return false;
        }

        public Set<MavenDependencyNode> getConflictParents (Artifact art) {
            return targets2ConfPar.get(art);
        }

    } // ExclusionTargets

    private static class ExclTargetEntry {
        Artifact artif;
        boolean isSelected = false;

        ExclTargetEntry(Artifact artif, boolean isSelected) {
            this.artif = artif;
            this.isSelected = isSelected;
        }
    }

    private static class ExclTargetRenderer extends JCheckBox
            implements ListCellRenderer, MouseListener, KeyListener {

        private JList parentList;
        private FixVersionConflictPanel parentPanel;

        ExclTargetRenderer (JList list, FixVersionConflictPanel parentPanel) {
            this.parentList = list;
            this.parentPanel = parentPanel;
        }

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ExclTargetEntry entry = (ExclTargetEntry)value;

            setText(entry.artif.getArtifactId());
            setSelected(entry.isSelected);
            setEnabled(list.isEnabled());
            setOpaque(isSelected && list.isEnabled());

            if (isSelected && list.isEnabled()) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override public void mouseClicked(MouseEvent e) {
            int idx = parentList.locationToIndex(e.getPoint());
            if (idx == -1) {
                return;
            }
            Rectangle rect = parentList.getCellBounds(idx, idx);
            if (rect.contains(e.getPoint())) {
                doCheck();
            }
        }

        @Override public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                doCheck();
            }
        }

        private void doCheck() {
            int index = parentList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            ExclTargetEntry ge = (ExclTargetEntry) parentList.getModel().getElementAt(index);
            ge.isSelected = !ge.isSelected;
            parentList.repaint();
            parentPanel.updateSummary();
        }

        @Override public void mousePressed(MouseEvent e) {}

        @Override public void mouseReleased(MouseEvent e) {}

        @Override public void mouseEntered(MouseEvent e) {}

        @Override public void mouseExited(MouseEvent e) {}

        @Override public void keyTyped(KeyEvent e) {}

        @Override public void keyReleased(KeyEvent e) {}

    } // ExclTargetRenderer

}
