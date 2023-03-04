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

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Data model used across the <em>New Update Center Wizard</em>.
 * @author Jiri Rechtacek
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    private static String AUTOUPDATE_TYPES = "Services/AutoupdateType"; //NOI18N
    private String AUTOUPDATE_SERVICE_TYPE = "_update_center"; //NOI18N
    private static String AUTOUPDATE_SETTINGS_TYPE_EXT = "settings"; //NOI18N
    private static String AUTOUPDATE_INSTANCE_TYPE_EXT = "instance"; //NOI18N
    private static String AUTOUPDATE_MODULE = "org.netbeans.modules.autoupdate"; // NOI18N
    private static String AUTOUPDATE_MODULE_NEW = "org.netbeans.modules.autoupdate.services"; // NOI18N

    private CreatedModifiedFiles cmf;
    
    // third panel data (Name, and Location)
    private String ucUrl;
    private String ucDisplayName;

    DataModel(WizardDescriptor wiz) {
        super(wiz);
    }
    
    private CreatedModifiedFiles regenerate () {
        if (cmf == null) {
            cmf = new CreatedModifiedFiles (getProject ());
        }

        boolean newAPI = true;
        try {
            newAPI = getModuleInfo().getDependencyVersion(AUTOUPDATE_MODULE_NEW) != null;
        } catch (IOException x) {
            Logger.getLogger(DataModel.class.getName()).log(Level.INFO, null, x);
        }
        String extension = (newAPI) ? AUTOUPDATE_INSTANCE_TYPE_EXT : AUTOUPDATE_SETTINGS_TYPE_EXT;
        FileObject template = newAPI ? null : CreatedModifiedFiles.getTemplate("update_center.xml"); // NOI18N
        String serviceTypeName = getModuleInfo().getCodeNameBase ().replace ('.', '_') + AUTOUPDATE_SERVICE_TYPE; // NOI18N
        FileSystem layer = LayerHandle.forProject (getProject ()).layer (false);
        
        String pathToAutoUpdateType = AUTOUPDATE_TYPES + '/' + serviceTypeName + '.' + extension;
        int sequence = 0;
        if (layer != null) {
            FileObject f;
            do {
                f = layer.findResource (pathToAutoUpdateType);
                if (f != null) {
                    pathToAutoUpdateType = AUTOUPDATE_TYPES + '/' + serviceTypeName + '_' + ++sequence + '.' + extension;
                }
            } while (f != null);
        }
        String codename = null;
        NbModuleProvider mp = getProject().getLookup().lookup(NbModuleProvider.class);
        Manifest mani = Util.getManifest(mp.getManifestFile());
        if (mani != null) {
            codename = mani.getMainAttributes().getValue("OpenIDE-Module"); // NOI18N
        }
        if (codename == null) {
            codename = getModuleInfo().getCodeNameBase();
        }
        final Map<String, String> substitutionTokens = newAPI ? null : Collections.singletonMap("MODULECODENAME", codename);        
        cmf.add(cmf.createLayerEntry(pathToAutoUpdateType, template, substitutionTokens, null, null)); // NOI18N
        
        if (newAPI) {
            cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, "instanceCreate", "methodvalue:org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory.createUpdateProvider")); //NOI18N
            cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, "instanceOf", "org.netbeans.spi.autoupdate.UpdateProvider")); //NOI18N            
        }        
        String url_key_base = getModuleInfo().getCodeNameBase ().replace ('.', '_') + AUTOUPDATE_SERVICE_TYPE; //NOI18N
        String url_key = sequence == 0 ? url_key_base : url_key_base + '_' + sequence; // NOI18N
        cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, "enabled", Boolean.TRUE)); //NOI18N
        
        // write into bundle
        ManifestManager mm = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false);
        String localizingBundle = mm.getLocalizingBundle ();
        localizingBundle = localizingBundle.substring (0, localizingBundle.indexOf ('.'));
        localizingBundle = localizingBundle.replace ('/', '.');
        cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, 
                "displayName", "bundlevalue:" + localizingBundle + "#" + pathToAutoUpdateType));
        cmf.add(cmf.createLayerAttribute(pathToAutoUpdateType, "url", "bundlevalue:" + localizingBundle + "#" + url_key)); //NOI18N
        
        cmf.add (cmf.bundleKeyDefaultBundle (pathToAutoUpdateType, ucDisplayName));
        cmf.add (cmf.bundleKeyDefaultBundle (url_key, ucUrl));
        
        // add dependency to autoupdate module
        cmf.add(cmf.addModuleDependency(newAPI ? AUTOUPDATE_MODULE_NEW : AUTOUPDATE_MODULE, null, null, false));
        
        return cmf;
    }
    
    CreatedModifiedFiles refreshCreatedModifiedFiles() {
        return regenerate ();
    }
    
    void setUpdateCenterURL (String url) {
        this.ucUrl = url;
    }
    
    String getUpdateCenterURL () {
        return ucUrl != null ? ucUrl : ""; //NOI18N
    }
    
    void setUpdateCenterDisplayName (String name) {
        this.ucDisplayName = name;
    }
    
    String getUpdateCenterDisplayName () {
        return ucDisplayName != null ? ucDisplayName : ""; //NOI18N
    }
    
}

