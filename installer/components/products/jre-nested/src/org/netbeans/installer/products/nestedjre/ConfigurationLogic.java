/**
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
package org.netbeans.installer.products.nestedjre;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;

/**
 *
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {

    private static final String JRE_NESTED = "{jre-nested}"; // NOI18N
    private static final String ID = "jre-nested"; // NOI18N
    
    public ConfigurationLogic() throws InitializationException {                    
        super(new String[]{JRE_NESTED}, ID);
    }

    @Override
    public void install(final Progress progress) throws InstallationException {
        final Product product = getProduct();
        File installLocation = product.getInstallationLocation();        
        
        // #255871 - Wrong path for installing JRE
        List<Product> products = Registry.getInstance().getProductsToInstall();
        for(Product p : products) {
            if(p.getUid().equals(BASE_IDE_UID)) {
                installLocation = new File(p.getInstallationLocation(), JavaUtils.JRE_NESTED_SUBDIR);
                product.setInstallationLocation(installLocation);
                p.setProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY, installLocation.getAbsolutePath());
            }
        }
        
        final File jvmTempLocation = new File(System.getProperty("java.home"));
        final FilesList filesList = product.getInstalledFiles();
       
        try {
            FilesList copiedFiles = FileUtils.copyNestedJRE(jvmTempLocation, installLocation, progress);

            List<File> deletedFiles = deletePacks(copiedFiles);
            List<File> files = copiedFiles.toList();
            files.removeAll(deletedFiles);

            filesList.add(files);
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationLogic.class.getName()).log(Level.SEVERE, null, ex);
        }

        progress.setPercentage(Progress.COMPLETE);
    }

    @Override
    public List<WizardComponent> getWizardComponents() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean registerInSystem() {
        return false;
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }

    private List<File> deletePacks(FilesList filesList) {
        List<File> deleted = new ArrayList();

        if (filesList != null) {
            List<File> files = filesList.toList();

            for (File file : files) {
                if (file.getName().endsWith("pack.gz")) { //NOI18N
                    try {
                        FileUtils.deleteFile(file);
                    } catch (IOException ex) {
                        Logger.getLogger(ConfigurationLogic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    deleted.add(file);
                }
            }
        }

        return deleted;
    }
}
