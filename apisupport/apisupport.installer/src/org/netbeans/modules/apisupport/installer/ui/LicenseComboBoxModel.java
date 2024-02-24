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
package org.netbeans.modules.apisupport.installer.ui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Dmitry Lipin
 */
public class LicenseComboBoxModel implements ComboBoxModel {

    private List<ListDataListener> listeners;
    private List<String> names;
    private List<String> types;
    private String selectedItem;
    private boolean selectedItemFromList;
    private Project suiteProject;

    public LicenseComboBoxModel(Project suiteProject, List<String> names, List<String> types) {
        this.suiteProject = suiteProject;
        this.names = new LinkedList<String>();
        this.names.addAll(names);

        this.types = new LinkedList<String>();
        this.types.addAll(types);

        this.listeners = new LinkedList<ListDataListener>();

        if (names.size() > 0) {
            this.selectedItem = names.get(0);
            this.selectedItemFromList = true;
        } else {
            this.selectedItem = "";
            this.selectedItemFromList = false;
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getNames() {
        return names;
    }

    public String getName() {
        if (selectedItemFromList) {
            return names.get(names.indexOf(selectedItem));
        } else {
            return selectedItem;
        }
    }

    // comboboxmodel ////////////////////////////////////////////////////////////
    public void setSelectedItem(Object item) {
        String oldSelectedItem = selectedItem;
        selectedItem = (String) item;

        if (names.indexOf(item) != -1) {
            if (types.get(names.indexOf(selectedItem)).equals(
                    SuiteInstallerProjectProperties.LICENSE_TYPE_CUSTOM)) {

                File home = FileUtil.toFile(suiteProject.getProjectDirectory());
                File licenseFile = new FileChooserBuilder("installer-license-dir").setTitle(NbBundle.getMessage(LicenseComboBoxModel.class, "InstallerPanel_License.FileChooser.Title")).
                        setDefaultWorkingDirectory(home).setFilesOnly(true).showOpenDialog();
                if (licenseFile != null) {
                    names.add(licenseFile.getAbsolutePath());
                    types.add(SuiteInstallerProjectProperties.LICENSE_TYPE_FILE);
                    selectedItem = names.get(names.size() - 1);
                } else {
                    selectedItem = oldSelectedItem;
                }
            }
            selectedItemFromList = true;
        } else {
            selectedItemFromList = false;
        }

        fireContentsChanged(-1);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return names.size();
    }

    public Object getElementAt(int index) {
        return names.get(index);
    }

    public void addListDataListener(ListDataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListDataListener(ListDataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    // private //////////////////////////////////////////////////////////////////
    private void fireContentsChanged(int index) {
        final ListDataListener[] clone;
        synchronized (listeners) {
            clone = listeners.toArray(new ListDataListener[0]);
        }

        final ListDataEvent event = new ListDataEvent(
                this,
                ListDataEvent.CONTENTS_CHANGED,
                index,
                index);

        for (ListDataListener listener : clone) {
            listener.contentsChanged(event);
        }
    }
}

