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

package org.netbeans.modules.apisupport.project.ui.wizard.moduleinstall;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Data model used across the <em>New Module Installer</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {

    static final String OPENIDE_MODULE_INSTALL = "OpenIDE-Module-Install"; // NOI18N
    static final String BUNDLE_ACTIVATOR = "Bundle-Activator"; // NOI18N
    static final String IMPORT_PACKAGE = "Import-Package"; // NOI18N
    private static final String INSTALLER_CLASS_NAME = "Installer"; // NOI18N
    
    private CreatedModifiedFiles cmf;
    
    DataModel(final WizardDescriptor wiz) {
        super(wiz);
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            regenerate();
        }
        return cmf;
    }
    
    private void regenerate() {
        cmf = new CreatedModifiedFiles(getProject());

        boolean osgi = false;
        try {
            osgi = getProject().getLookup().lookup(NbModuleProvider.class).hasDependency("org.netbeans.libs.osgi"); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // obtain unique class name
        String className = INSTALLER_CLASS_NAME;
        String path = getDefaultPackagePath(className + ".java", false); // NOI18N
        int i = 0;
        while (alreadyExist(path)) {
            className = INSTALLER_CLASS_NAME + '_' + ++i;
            path = getDefaultPackagePath(className + ".java", false); // NOI18N
        }
        
        // generate .java file for ModuleInstall
        Map<String, String> basicTokens = new HashMap<String, String>();
        basicTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        basicTokens.put("CLASS_NAME", className); // NOI18N
        // XXX use nbresloc URL protocol rather than
        // DataModel.class.getResource(...) and all such a cases below
        FileObject template;
        if (osgi) {
            template = CreatedModifiedFiles.getTemplate("moduleActivator.java"); // NOI18N
        } else {
            template = CreatedModifiedFiles.getTemplate("moduleInstall.java"); // NOI18N
            cmf.add(cmf.addModuleDependency("org.openide.modules")); // NOI18N
            cmf.add(cmf.addModuleDependency("org.openide.util.ui")); // NOI18N
        }
        cmf.add(cmf.createFileWithSubstitutions(path, template, basicTokens));
        
        
        // add manifest attribute
        Map<String, String> attribs = new HashMap<String, String>();
        if (osgi) {
            attribs.put(BUNDLE_ACTIVATOR, getPackageName() + '.' + className);
            attribs.put(IMPORT_PACKAGE, "org.osgi.framework"); // NOI18N
        } else {
            attribs.put(OPENIDE_MODULE_INSTALL, getPackageName().replace('.','/') + '/' + className + ".class"); // NOI18N
        }
        cmf.add(cmf.manifestModification(null, attribs));
    }
    
    private void reset() {
        cmf = null;
    }
    
    public @Override void setPackageName(String packageName) {
        super.setPackageName(packageName);
        reset();
    }
    
    private boolean alreadyExist(String relPath) {
        return getProject().getProjectDirectory().getFileObject(relPath) != null;
    }
    
}
