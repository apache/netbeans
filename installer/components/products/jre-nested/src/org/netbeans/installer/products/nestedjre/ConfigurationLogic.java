/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
