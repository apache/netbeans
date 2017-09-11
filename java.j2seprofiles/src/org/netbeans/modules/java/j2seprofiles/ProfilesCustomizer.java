/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seprofiles;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 *
 * @author Tomas Zezula
 */
class ProfilesCustomizer extends javax.swing.JPanel implements ActionListener {

    private final Analyzer.CustomizerContext<Void,ProfilesCustomizer> context;

    /**
     * Creates new form ProfilesCustomizer
     */
    ProfilesCustomizer(@NonNull final Analyzer.CustomizerContext<Void,ProfilesCustomizer> context) {
        assert context != null;
        this.context = context;
        initComponents();
        profiles.setRenderer(new ProfileKeyRenderer());        
        initProfiles();
        profiles.addActionListener(this);
    }

    @Override
    public void actionPerformed(@NonNull final ActionEvent event) {
        final Object selection = profiles.getSelectedItem();
        final ProfileKey key = selection instanceof ProfileKey ?
                (ProfileKey) selection :
                ProfileKey.forProject();
        final SourceLevelQuery.Profile profile = key.getProfile();
        if (profile != null) {
            context.getSettings().put(
                ProfilesCustomizerProvider.PROP_PROFILE_TO_CHECK,
                profile.getName());
        } else {
            //XXX: Preferences.remove() does not work override with something
            context.getSettings().put(
                ProfilesCustomizerProvider.PROP_PROFILE_TO_CHECK,
                "<tombstone>"); //NOI18N
        }
    }


    private void initProfiles() {
        profiles.removeAllItems();
        profiles.addItem(ProfileKey.forProject());
        for (SourceLevelQuery.Profile profile : SourceLevelQuery.Profile.values()) {
            if (profile != SourceLevelQuery.Profile.DEFAULT) {
                profiles.addItem(ProfileKey.forProfile(profile));
            }
        }
        final String profileName = context.getSettings().get(
                ProfilesCustomizerProvider.PROP_PROFILE_TO_CHECK, null);
        final SourceLevelQuery.Profile profile = profileName == null ?
                null :
                SourceLevelQuery.Profile.forName(profileName);
        profiles.setSelectedItem(
                profile == null ?
                ProfileKey.forProject() :
                ProfileKey.forProfile(profile));
    }

    private static final class ProfileKeyRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProfileKey) {
                value = ((ProfileKey)value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }


    private static final class ProfileKey {

        private final Union2<SourceLevelQuery.Profile,String> profile;

        private ProfileKey(@NonNull final Union2<SourceLevelQuery.Profile,String> profile) {
            assert profile != null;
            this.profile = profile;
        }

        @Override
        public String toString() {
            return String.format(
                "ProfileKey[%s]",   //NOI18N
                profile.hasFirst() ? profile.first() : profile.second());
        }

        @Override
        public int hashCode() {
            return profile.hashCode();
        }

        @Override
        public boolean equals(@NonNull final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ProfileKey)) {
                return false;
            }
            return this.profile.equals(((ProfileKey)other).profile);
        }

        @NonNull
        String getDisplayName() {
            return profile.hasFirst() ? profile.first().getDisplayName() : profile.second();
        }

        @CheckForNull
        SourceLevelQuery.Profile getProfile() {
            return profile.hasFirst() ? profile.first() : null;
        }

        @NonNull
        static ProfileKey forProfile(@NonNull final SourceLevelQuery.Profile profile) {
            assert profile != null;
            return new ProfileKey(Union2.<SourceLevelQuery.Profile,String>createFirst(profile));
        }

        @NonNull
        @NbBundle.Messages({
            "MSG_ProfileFromProject=Project's Profile"
        })
        static ProfileKey forProject() {
            return new ProfileKey(Union2.<SourceLevelQuery.Profile,String>createSecond(Bundle.MSG_ProfileFromProject()));
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
        profiles = new javax.swing.JComboBox();

        jLabel1.setLabelFor(profiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ProfilesCustomizer.class, "TXT_ProfilesCustomizer_profiles")); // NOI18N

        profiles.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(profiles, 0, 251, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(profiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProfilesCustomizer.class, "AD_ProfilesCustomizer_profiles")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox profiles;
    // End of variables declaration//GEN-END:variables
}
