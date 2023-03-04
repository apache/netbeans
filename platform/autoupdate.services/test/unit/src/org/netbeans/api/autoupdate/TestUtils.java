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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Radek Matous, Jirka Rechtacek
 */
public class TestUtils {
    
    private static UpdateItem item = null;
    private static ModuleManager mgr = null;
        
    public static void setUserDir(String path) {    
        System.setProperty ("netbeans.user", path);
    }
    
    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        return new File (System.getProperty ("netbeans.home")); // NOI18N
    }
    
    public static void setPlatformDir (String path) {    
        System.setProperty ("netbeans.home", path);
    }
        
    public static void testInit() {
        mgr = Main.getModuleSystem().getManager();
        assert mgr != null;
    }

    public static File getFile(NbTestCase t, URL res) throws IOException {
        File create;
        String name = res.getFile().replaceAll(".*/", "").replaceAll(".jar$", "");
        for (int i = 0; ; i++) {
            String add = i == 0 ? ".jar" : i + ".jar";
            create = new File(t.getWorkDir(), name + add);
            if (!create.exists()) {
                break;
            }
        }
        
        FileOutputStream os = new FileOutputStream(create);
        FileUtil.copy(res.openStream(), os);
        os.close();
        
        return create;
    }

    public static class CustomItemsProvider implements UpdateProvider {
        public String getName() {
            return "items-with-custom-installer";
        }

        public String getDisplayName() {
            return "Provides item with own custom installer";
        }

        public String getDescription () {
            return null;
        }

        public Map<String, UpdateItem> getUpdateItems() {
            return Collections.singletonMap ("hello-installer", getUpdateItemWithCustomInstaller ());
        }

        public boolean refresh(boolean force) {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }
    
    private static CustomInstaller customInstaller = new CustomInstaller () {
        public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
            assert false : "Don't call unset installer";
            return false;
        }
    };
    
    
    public static void setCustomInstaller (CustomInstaller installer) {
        customInstaller = installer;
    }

    public static UpdateItem getUpdateItemWithCustomInstaller () {
        if (item != null) return item;
        String codeName = "hello-installer";
        String specificationVersion = "0.1";
        String displayName = "Hello Component";
        String description = "Hello I'm a component with own installer";
        URL distribution = null;
        try {
            distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-engine-1-1.nbm");
            //distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/executable-jar.jar");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "2815";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        CustomInstaller ci = createCustomInstaller ();
        assert ci != null;
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        item = UpdateItem.createNativeComponent (
                                                    codeName,
                                                    specificationVersion,
                                                    downloadSize,
                                                    null, // dependencies
                                                    displayName,
                                                    description,
                                                    false, false, "my-cluster",
                                                    ci,
                                                    license);
        return item;
    }
    
    private static CustomInstaller createCustomInstaller () {
        return new CustomInstaller () {
            public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
                assert item != null;
                return customInstaller.install (codeName, specificationVersion, handle);
            }
        };
    }
}
