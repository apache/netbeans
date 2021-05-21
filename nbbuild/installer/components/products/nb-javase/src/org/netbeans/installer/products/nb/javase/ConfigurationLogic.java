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

package org.netbeans.installer.products.nb.javase;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.JavaFXUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String JAVA_CLUSTER = 
            "{java-cluster}"; // NOI18N
    private static final String APISUPPORT_CLUSTER = 
            "{apisupport-cluster}"; // NOI18N
    private static final String HARNESS_CLUSTER = 
            "{harness-cluster}"; // NOI18N
    private static final String PROFILER_CLUSTER =
            "{profiler-cluster}"; // NOI18N
    private static final String ID = 
            "JAVA"; // NOI18N
    private static final String JUNIT_ACCEPTED_PROPERTY =
            "junit.accepted"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            JAVA_CLUSTER, 
            APISUPPORT_CLUSTER, 
            HARNESS_CLUSTER,
            PROFILER_CLUSTER}, ID);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        String junitAccepted = getProduct().getProperty(JUNIT_ACCEPTED_PROPERTY);
        if(junitAccepted != null) {
            final List<Dependency> dependencies =
                    getProduct().getDependencyByUid(BASE_IDE_UID);
            final List<Product> sources =
                    Registry.getInstance().getProducts(dependencies.get(0));

            // pick the first one and integrate with it
            final File nbLocation = sources.get(0).getInstallationLocation();
            String licenseAcceptedText = junitAccepted.equals("true")? 
                getString("CL.junit.accepted.tag") :
                getString("CL.junit.denied.tag");
            LogManager.log("Adding " + licenseAcceptedText + " to license_accepted file");            
            try {
                NetBeansUtils.createLicenseAcceptedMarker(nbLocation, licenseAcceptedText);// NOI18N
            } catch (IOException e) {
                throw new InstallationException(
                        getString("CL.install.error.license.accepted"), // NOI18N
                        e);
            }
        }
        
        // register JavaFX if installed
        final Product product = getProduct();        
        File installationlocation = getProduct().getInstallationLocation();
        File sdkLocation;
        File runtimeLocation;
        
        boolean javaFXBundled = false;
        File jdkHome = getJdkLocation(product);
        if (jdkHome != null) {
            javaFXBundled = JavaFXUtils.jdkContainsJavaFX(jdkHome);
        }            
        
        if (javaFXBundled) {
            sdkLocation = jdkHome;
            runtimeLocation = new File(jdkHome.getAbsoluteFile(), "jre");
        } else {                    
            sdkLocation = getInstalledFXSDKLocation(product);
            runtimeLocation = getInstalledFXRuntimeLocation(product);
        }                               
        
        try {
            if (installationlocation != null && sdkLocation != null && runtimeLocation != null) {
                JavaFXUtils.registerJavaFX(installationlocation, sdkLocation, runtimeLocation);
            }
        } catch (IOException ex) {
            LogManager.log("... cannot execute commad to register JavaFX", ex);
        }
    }    
    
    private File getInstalledFXSDKLocation (Product product) {
        String sdkPath = JavaFXUtils.getJavaFXSDKInstallationPath(product.getPlatforms().get(0));
        File sdkLocation = null;
        if (sdkPath != null) {
            sdkLocation = new File(sdkPath);
        }
        return sdkLocation;
    }
    
    private File getInstalledFXRuntimeLocation (Product product) {
        String runtimePath = JavaFXUtils.getJavaFXRuntimeInstallationPath(product.getPlatforms().get(0));
        File runtimeLocation = null;
        if (runtimePath != null) {
            runtimeLocation = new File(runtimePath);
        }
        return runtimeLocation;
    }
    
    private File getJdkLocation (Product product) {          
        List <Product> products = Registry.getInstance().getProducts("nb-all"); // NOI18N
        String jdkHomePath = null;
        
        File installLocation = product.getInstallationLocation();
        for (Product p : products) {
            if(installLocation.equals(p.getInstallationLocation())) {
                jdkHomePath = p.getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY);
                if (jdkHomePath != null) {
                    break;
                }
            }
        }
        
        return jdkHomePath != null ? new File(jdkHomePath) : null;        
    }
}
