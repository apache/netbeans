/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.javaee.api.ui.support;

import java.util.Collections;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class JavaEEServerComboBoxModel extends AbstractListModel<J2eePlatform> implements ComboBoxModel<J2eePlatform> {

    final J2eePlatformModel[] platforms;
    J2eePlatformModel selectedPlatform;

    private JavaEEServerComboBoxModel(J2eeModule.Type moduleType, Profile javaeeProfile) {
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs(Collections.singleton(moduleType), javaeeProfile);
        TreeSet<J2eePlatformModel> order = new TreeSet<>();
        for (String serverInstanceID : serverInstanceIDs) {
            try {
                order.add(new J2eePlatformModel(serverInstanceID));
            } catch (InstanceRemovedException ex) {
                //Shall not happen, however if it would we
                //Simply not add this item to the list
            }
        }
        platforms = order.toArray(new J2eePlatformModel[0]);
    }


    public static ComboBoxModel<J2eePlatform> createJavaEEServerComboBoxModel(String selectedinstanceId, J2eeModule.Type moduleType, Profile javaeeProfile) {
        JavaEEServerComboBoxModel model = new JavaEEServerComboBoxModel(moduleType, javaeeProfile);
        model.setSelectedItem(selectedinstanceId);
        return model;
    }

    public static String getServerInstanceID(J2eePlatform platform) {
        if (platform == null) return null;

        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        for (String serverInstanceID : serverInstanceIDs) {
            try {
                J2eePlatform p = Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
                if (platform.equals(p)) {
                    return serverInstanceID;
                }
            } catch (InstanceRemovedException ex) {
            }
        }
        return null;
    }

    @Override
    public int getSize() {
        return platforms.length;
    }

    @Override
    public J2eePlatform getElementAt(int index) {
        return platforms[index].platform;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem != null) {
            for (J2eePlatformModel model : platforms) {
                if (model.platform.equals(anItem)) {
                    selectedPlatform = model;
                    return;
                }
            }
        }
        selectedPlatform = null;
    }

    void setSelectedItem(String anItem) {
        if (anItem != null) {
            for (J2eePlatformModel model : platforms) {
                if (model.serverInstanceId.equals(anItem)) {
                    selectedPlatform = model;
                    break;
                }
            }
        } else {
            selectedPlatform = null;
        }
    }

    @Override
    public J2eePlatform getSelectedItem() {
        return selectedPlatform != null ? selectedPlatform.platform : null;
    }

    private static class J2eePlatformModel implements Comparable<J2eePlatformModel> {
        final String serverInstanceId;
        final J2eePlatform platform;

        public J2eePlatformModel(String serverInstanceId) throws InstanceRemovedException {
            this.serverInstanceId = serverInstanceId;
            platform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
        }

        @Override
        public String toString() {
            String ret = platform.getDisplayName();
            return ret != null ? ret : "";
        }


        @Override
        public int compareTo(J2eePlatformModel o) {
            return toString().compareTo(o.toString());
        }


    }
}
