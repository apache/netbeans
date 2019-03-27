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

package org.netbeans.installer.products.nb.ergonomics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel.BundleType;

/**
 *
 
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ERGONOMICS_CLUSTER = 
            "{ergonomics-cluster}"; // NOI18N
    private static final String JAVA_CLUSTER =
            "{java-cluster}"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            ERGONOMICS_CLUSTER}, null);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        String type = System.getProperty(NbWelcomePanel.WELCOME_PAGE_TYPE_PROPERTY);
        if(type!=null && BundleType.getType(type).equals(BundleType.JAVA)) {
            // Issue 157484. JavaSE should be enabled in "Java" distribution
            // http://www.netbeans.org/issues/show_bug.cgi?id=157484
            List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
            final Product nbProduct =
                Registry.getInstance().getProducts(dependencies.get(0)).get(0);
            final File nbLocation = nbProduct.getInstallationLocation();
            try {
                NetBeansUtils.addCluster(nbLocation, ERGONOMICS_CLUSTER, JAVA_CLUSTER);
            } catch (IOException e) {
                LogManager.log(ErrorLevel.WARNING, e);
            }
        }
    }
}
