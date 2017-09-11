/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    static private String AUTOUPDATE_TYPES = "Services/AutoupdateType"; //NOI18N
    private String AUTOUPDATE_SERVICE_TYPE = "_update_center"; //NOI18N
    static private String AUTOUPDATE_SETTINGS_TYPE_EXT = "settings"; //NOI18N
    static private String AUTOUPDATE_INSTANCE_TYPE_EXT = "instance"; //NOI18N
    static private String AUTOUPDATE_MODULE = "org.netbeans.modules.autoupdate"; // NOI18N
    static private String AUTOUPDATE_MODULE_NEW = "org.netbeans.modules.autoupdate.services"; // NOI18N

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

