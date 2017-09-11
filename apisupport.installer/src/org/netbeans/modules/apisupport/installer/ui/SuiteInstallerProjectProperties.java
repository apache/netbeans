/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    public static final String USE_PACK200_COMPRESSION = "pack200-enabled";

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
    final JToggleButton.ToggleButtonModel pack200Model;
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
        pack200Model = new JToggleButton.ToggleButtonModel();
        pack200Model.setSelected(prefs.getBoolean(USE_PACK200_COMPRESSION, false));

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
        prefs.putBoolean(USE_PACK200_COMPRESSION, pack200Model.isSelected());
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
