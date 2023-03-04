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

package org.netbeans.modules.gradle.javaee.api.ui.support;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.api.j2ee.core.Profile;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class JavaEEProfileComboBoxModel extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {

    final ProfileModel[] profiles;
    ProfileModel selectedProfile;

    public JavaEEProfileComboBoxModel() {
        profiles = null;
    }


    @Override
    public int getSize() {
        return profiles.length;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem != null) {
            for (ProfileModel profile : profiles) {
                if (profile.profile.equals(anItem)) {
                    selectedProfile = profile;
                    return;
                }
            }
        }
        selectedProfile = null;
    }

    @Override
    public Object getSelectedItem() {
        return selectedProfile != null ? selectedProfile.profile : null;
    }

    @Override
    public Profile getElementAt(int index) {
        return profiles[index].profile;
    }

    private static class ProfileModel {
        final Profile profile;

        public ProfileModel(Profile profile) {
            this.profile = profile;
        }

        @Override
        public String toString() {
            return profile.getDisplayName();
        }
    }
}
