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
package org.netbeans.modules.java.project.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceLevelQuery.Profile;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
class FixProfile extends javax.swing.JPanel {
 
    private final JButton okOption;
    private final LibsModel libsModel;
    private Profile reqProfile;

    /**
     * Creates new form FixProfile
     */
    FixProfile(
            @NonNull final JButton okOption,
            @NonNull final Profile currentProfile,
            @NonNull final Collection<? extends ProfileProblemsProviderImpl.Reference> state) {
        assert okOption != null;
        assert currentProfile != null;
        assert state != null;
        this.okOption = okOption;
        this.libsModel = new LibsModel (currentProfile, state);
        libsModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent lde) {
                checkOkOption();
            }

            @Override
            public void intervalRemoved(ListDataEvent lde) {
                checkOkOption();
            }

            @Override
            public void contentsChanged(ListDataEvent lde) {
                checkOkOption();
            }
        });
        this.reqProfile = ProfileProblemsProviderImpl.requiredProfile(state, currentProfile);
        initComponents();
        remove.setEnabled(false);
        brokenLibs.setModel(libsModel);
        brokenLibs.setCellRenderer(new LibsRenderer(brokenLibs));
        brokenLibs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        brokenLibs.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                remove.setEnabled(brokenLibs.getSelectedIndex() != -1);
            }
        });
        brokenLibs.setSelectedIndex(0);
        profiles.setRenderer(new ProfilesRenderer());
        changeProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                libsModel.updateProfile(changeProfile.isSelected());
                checkOkOption();
            }
        });
        updateProfiles();
        checkOkOption();
    }

    boolean shouldUpdateProfile() {
        return changeProfile.isSelected();
    }

    @CheckForNull
    Profile getProfile() {
        final Object selObj = profiles.getSelectedItem();
        if (selObj instanceof Profile) {
            return (Profile) selObj;
        } else {
            return null;
        }
    }

    @NonNull
    Collection<? extends ProfileProblemsProviderImpl.Reference> getRootsToRemove() {
        return libsModel.getRemovedRooots();
    }    

    private void updateProfiles() {
        profiles.removeAllItems();
        for (Profile profile : Profile.values()) {
            if (profile.compareTo(reqProfile) >= 0) {
                profiles.addItem(profile);
            }
        }
        profiles.setSelectedItem(reqProfile);
    }

    private void checkOkOption() {
        okOption.setEnabled(libsModel.getSize() == 0);
    }

    private static final class ProfilesRenderer extends DefaultListCellRenderer {
        @Override        
        public Component getListCellRendererComponent(
                @NonNull final JList jlist,
                @NullAllowed Object o,
                final int i,
                final boolean bln,
                final boolean bln1) {
            if (o instanceof Profile) {
                o = ((Profile)o).getDisplayName();
            }
            return super.getListCellRendererComponent(jlist, o, i, bln, bln1);
        }
    }
    
    private static final class LibsRenderer extends DefaultListCellRenderer {

        private static final int LIGHTER_COLOR_COMPONENT = 80;
        
        private final JLabel root;
        private final JLabel profile;
        private final JPanel container;

        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgSelectionColor;
        private Color fgSelectionColor;

        LibsRenderer(@NonNull final JList list) {
            this.root = new JLabel();
            this.root.setHorizontalAlignment(LEFT);
            this.root.setOpaque(false);
            this.root.setFont(list.getFont());
            this.profile = new JLabel();
            this.profile.setHorizontalAlignment(RIGHT);
            this.profile.setOpaque(false);
            this.profile.setFont(list.getFont());
            this.container = new JPanel();
            this.container.setLayout(new BorderLayout());
            this.container.add (this.root, BorderLayout.WEST);
            this.container.add (this.profile, BorderLayout.EAST);
            
            fgColor = list.getForeground();
            fgColorLighter = new Color(
                Math.min(255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                Math.min(255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                Math.min(255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT));
            bgColor = new Color(list.getBackground().getRGB());
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();
        }

        @NbBundle.Messages({
        "FMT_RootWithProfile={0} ({1})",
        "MSG_InvalidProfile=<Invalid>"
        })
        @Override
        public Component getListCellRendererComponent(
                @NonNull final JList jlist,
                @NullAllowed Object o,
                final int i,
                final boolean isSelected,
                final boolean hasFocus) {

            if (isSelected) {
                root.setForeground(fgSelectionColor);
                profile.setForeground(fgSelectionColor);
                container.setBackground(bgSelectionColor);
            } else {
                root.setForeground(fgColor);
                profile.setForeground(fgColorLighter);
                container.setBackground(bgColor);
            }

            if (o instanceof ProfileProblemsProviderImpl.Reference) {
                final ProfileProblemsProviderImpl.Reference e = (ProfileProblemsProviderImpl.Reference) o;
                root.setText(e.getDisplayName());
                root.setIcon(e.getIcon());
                final Profile requiredProfile = e.getRequiredProfile();
                if (requiredProfile == null) {
                    profile.setText(String.format(
                        "<html><font color=\"#A40000\">%s", //NOI18N
                        Bundle.MSG_InvalidProfile()));
                } else {
                    profile.setText(requiredProfile.getDisplayName());
                }
                container.setToolTipText(e.getToolTipText());
            } else {
                root.setText("");   //NOI18N
                root.setIcon(null);
                profile.setText("");    //NOI18N
                container.setToolTipText(null);
            }
            return container;
        }
    }

    private static final class LibsModel extends AbstractListModel {

        private final Profile currentProfile;
        private final Collection<? extends ProfileProblemsProviderImpl.Reference> state;
        private final Set<ProfileProblemsProviderImpl.Reference> toRemove;
        private final List<ProfileProblemsProviderImpl.Reference> data;
        private boolean updated;

        LibsModel(
                @NonNull final Profile currentProfile,
                @NonNull Collection<? extends ProfileProblemsProviderImpl.Reference> state) {
            this.currentProfile = currentProfile;
            this.state = state;
            this.toRemove = new HashSet<ProfileProblemsProviderImpl.Reference>();
            this.data = new ArrayList<ProfileProblemsProviderImpl.Reference>();
            refresh();
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int i) {
            if (i<0 || i>=data.size()) {
                throw new IndexOutOfBoundsException(
                    String.format(
                        "Index: %d, Size: %d",  //NOI18N
                        i,
                        data.size()));
            }
            return data.get(i);
        }

        void removeRoots(@NonNull final Collection<? extends ProfileProblemsProviderImpl.Reference> roots) {
            final int oldSize = getSize();
            toRemove.addAll(roots);
            refresh();
            final int newSize = getSize();
            fireContentsChanged(this, 0, Math.max(oldSize, newSize));
        };

        @NonNull
        Profile requiredProfile() {
            return ProfileProblemsProviderImpl.requiredProfile(data, currentProfile);
        }

        void updateProfile(final boolean  update) {
            final int oldSize = getSize();
            updated = update;
            refresh();
            final int newSize = getSize();
            fireContentsChanged(this, 0, Math.max(oldSize, newSize));
        }

        @NonNull
        Collection<? extends ProfileProblemsProviderImpl.Reference> getRemovedRooots() {
            return Collections.unmodifiableCollection(toRemove);
        }

        private void refresh() {
            data.clear();
            for (ProfileProblemsProviderImpl.Reference ref : state) {
                if (!toRemove.contains(ref) &&
                    !(updated && ref.getRequiredProfile() != null)) {
                    data.add(ref);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        remove = new javax.swing.JButton();
        changeProfile = new javax.swing.JCheckBox();
        profiles = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        brokenLibs = new javax.swing.JList();

        jLabel1.setLabelFor(brokenLibs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FixProfile.class, "LBL_FixProfile_BrokenLibs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remove, org.openide.util.NbBundle.getMessage(FixProfile.class, "LBL_FixProfile_remove")); // NOI18N
        remove.setToolTipText(org.openide.util.NbBundle.getMessage(FixProfile.class, "TIP_FixProfile_Remove")); // NOI18N
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLibrary(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(changeProfile, org.openide.util.NbBundle.getMessage(FixProfile.class, "LBL_FixProfile_changeProfile")); // NOI18N

        jScrollPane1.setViewportView(brokenLibs);
        brokenLibs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FixProfile.class, "AD_FixProfile_BrokenLibs")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 202, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(changeProfile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profiles, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(remove)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(remove)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changeProfile)
                    .addComponent(profiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FixProfile.class, "FixProfile.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        changeProfile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FixProfile.class, "AD_FixProfile_changeProfile")); // NOI18N
        profiles.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FixProfile.class, "AN_FixProfile_Profiles")); // NOI18N
        profiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FixProfile.class, "AD_FixProfile_Profiles")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removeLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLibrary
        final Object[] selection =  brokenLibs.getSelectedValues();
        final Set<ProfileProblemsProviderImpl.Reference> rootsToRemove = new HashSet<ProfileProblemsProviderImpl.Reference>();
        for (Object e : selection) {
            rootsToRemove.add((ProfileProblemsProviderImpl.Reference)e);
        }
        libsModel.removeRoots(rootsToRemove);
        if (libsModel.getSize() > 0) {
            brokenLibs.setSelectedIndex(0);
        }
        reqProfile = libsModel.requiredProfile();
        updateProfiles();
    }//GEN-LAST:event_removeLibrary

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList brokenLibs;
    private javax.swing.JCheckBox changeProfile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox profiles;
    private javax.swing.JButton remove;
    // End of variables declaration//GEN-END:variables
}
