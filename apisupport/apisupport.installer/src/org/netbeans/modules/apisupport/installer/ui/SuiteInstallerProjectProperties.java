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

package org.netbeans.modules.apisupport.installer.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JToggleButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author avm
 */
public class SuiteInstallerProjectProperties {

    public static final String GENERATE_FOR_WINDOWS = "os-windows";
    public static final String GENERATE_FOR_LINUX = "os-linux";
    public static final String GENERATE_FOR_SOLARIS = "os-solaris";
    public static final String GENERATE_FOR_MAC = "os-macosx";

    public static final String LICENSE_TYPE = "license-type";
    public static final String LICENSE_TYPE_NO = "no";
    public static final String LICENSE_TYPE_FILE = "file";
    public static final String LICENSE_TYPE_CUSTOM = "custom";
    public static final String LICENSE_FILE = "license-file";
    
    private final Project suiteProject;
    final JToggleButton.ToggleButtonModel windowsModel;
    final JToggleButton.ToggleButtonModel linuxModel;
    final JToggleButton.ToggleButtonModel solarisModel;
    final JToggleButton.ToggleButtonModel macModel;
    final LicenseComboBoxModel licenseModel;

    public SuiteInstallerProjectProperties(Project suiteProject) {
        this.suiteProject = suiteProject;
        Preferences prefs = prefs(suiteProject);
        windowsModel = new JToggleButton.ToggleButtonModel();
        windowsModel.setSelected(prefs.getBoolean(GENERATE_FOR_WINDOWS, Utilities.isWindows()));
        linuxModel = new JToggleButton.ToggleButtonModel();
        linuxModel.setSelected(prefs.getBoolean(GENERATE_FOR_LINUX, Utilities.getOperatingSystem() == Utilities.OS_LINUX));
        solarisModel = new JToggleButton.ToggleButtonModel();
        solarisModel.setSelected(prefs.getBoolean(GENERATE_FOR_SOLARIS, Utilities.getOperatingSystem() == Utilities.OS_SOLARIS));
        macModel = new JToggleButton.ToggleButtonModel();
        macModel.setSelected(prefs.getBoolean(GENERATE_FOR_MAC, Utilities.isMac()));

        // license model:
        ResourceBundle rb = NbBundle.getBundle(SuiteInstallerProjectProperties.class);
        Enumeration<String> keys = rb.getKeys();
        String prefix = "SuiteInstallerProjectProperties.license.type.";
        List<String> names = new ArrayList<String>();
        List<String> types = new ArrayList<String>();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(prefix)) {
                String type = key.substring(prefix.length());
                String value = NbBundle.getMessage(SuiteInstallerProjectProperties.class, key);
                if (type.equals(LICENSE_TYPE_NO)) {
                    //No License is the first option
                    names.add(0, value);
                    types.add(0, type);
                } else {
                    names.add(value);
                    types.add(type);
                }
            }
        }
        licenseModel = new LicenseComboBoxModel(suiteProject, names, types);

        String licenseFileName = prefs.get(LICENSE_FILE, null);
        String licenseType = prefs.get(LICENSE_TYPE, null);
        if (licenseFileName != null) {
            File licenseFile = new File(licenseFileName);
            if (!licenseFile.isAbsolute()) {
                licenseFile = PropertyUtils.resolveFile(FileUtil.toFile(suiteProject.getProjectDirectory()), licenseFileName);
            }
            
            licenseModel.getNames().add(licenseFile.getAbsolutePath());
            licenseModel.getTypes().add(LICENSE_TYPE_FILE);
            String name = licenseModel.getNames().get(licenseModel.getNames().size() - 1);
            licenseModel.setSelectedItem(name);             
        }
        
        else if (licenseType != null) {
            int index = licenseModel.getTypes().indexOf(licenseType);
            if (index != -1) {
                licenseModel.setSelectedItem(licenseModel.getNames().get(index));
            }
        }
    }

    public void store() throws IOException {
        Preferences prefs = prefs(suiteProject);
        prefs.putBoolean(GENERATE_FOR_WINDOWS, windowsModel.isSelected());
        prefs.putBoolean(GENERATE_FOR_LINUX, linuxModel.isSelected());
        prefs.putBoolean(GENERATE_FOR_SOLARIS, solarisModel.isSelected());
        prefs.putBoolean(GENERATE_FOR_MAC, macModel.isSelected());
        String licenseName = (String) licenseModel.getSelectedItem();
        if (licenseName != null) {
            int index = licenseModel.getNames().indexOf(licenseName);
            if (index != -1) {
                String type = licenseModel.getTypes().get(index);
                if (type.equals(LICENSE_TYPE_FILE)) {
                    File suiteLocation = FileUtil.toFile(suiteProject.getProjectDirectory());
                    File f = PropertyUtils.resolveFile(suiteLocation, licenseName);
                    String rel = PropertyUtils.relativizeFile(suiteLocation, f);
                    if (rel != null) {
                        prefs.put(LICENSE_FILE, rel);
                    } else {
                        prefs.put(LICENSE_FILE, f.getAbsolutePath());
                    }
                    prefs.remove(LICENSE_TYPE);
                } else {
                    prefs.put(LICENSE_TYPE, type);
                    prefs.remove(LICENSE_FILE);
                }
            }
        }
    }

    public static Preferences prefs(Project suiteProject) {
        return ProjectUtils.getPreferences(suiteProject, SuiteInstallerProjectProperties.class, true);
    }

}
