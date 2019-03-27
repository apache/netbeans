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

package org.netbeans.installer.products.nb.soa;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            SOA_CLUSTER}, ID);
    }
    
    @Override
    public void install(Progress progress) throws InstallationException {
	super.install(progress);
        final File soaLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies = 
                getProduct().getDependencyByUid(BASE_IDE_UID);
        List<Product> sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final Product nbProduct = sources.get(0);
        final File nbLocation = nbProduct.getInstallationLocation();
        
        // get the list of suitable openesb installations
        dependencies = 
                getProduct().getDependencyByUid(OPENESB_UID);
        sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final Product openesbProduct = sources.get(0);
        final File openesbLocation = openesbProduct.getInstallationLocation();
        
        //// get the list of suitable access manager installations
        //dependencies = 
        //        getProduct().getDependencyByUid(AM_UID);
        //sources = 
        //        Registry.getInstance().getProducts(dependencies.get(0));
        //
        //// pick the first one and integrate with it
        //final Product amProduct = sources.get(0);
        //final File amLocation = amProduct.getInstallationLocation();
        //
        //// get the list of suitable glassfish installations
        //dependencies = 
        //        amProduct.getDependencyByUid(GLASSFISH_UID);
        //sources = 
        //        Registry.getInstance().getProducts(dependencies.get(0));
        //
        //// pick the first one and integrate with it
        //final Product glassfishProduct = sources.get(0);
        //final File glassfishLocation = glassfishProduct.getInstallationLocation();
        //
        //final File amConfigFile = new File(GlassFishUtils.getDomainConfig(
        //        glassfishLocation, GlassFishUtils.DEFAULT_DOMAIN), AM_CONFIG_FILE);
        //
        ///////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.netbeans.conf.am")); // NOI18N
        //
        //    NetBeansUtils.setJvmOption(
        //            nbLocation, 
        //            JVM_OPTION_AM_CONFIG, 
        //            amConfigFile.getAbsolutePath(), 
        //            true);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.netbeans.conf.am"), // NOI18N
        //            e);
        //}
    }
    
    @Override
    public void uninstall(Progress progress) throws UninstallationException {
        super.uninstall(progress);
        
        final File soaLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies = 
                getProduct().getDependencyByUid(BASE_IDE_UID);
        List<Product> sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final Product nbProduct = sources.get(0);
        final File nbLocation = nbProduct.getInstallationLocation();
        
        ///////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.uninstall.netbeans.conf.am")); // NOI18N
        //
        //    NetBeansUtils.removeJvmOption(nbLocation, JVM_OPTION_AM_CONFIG);
        //} catch (IOException e) {
        //    throw new UninstallationException(
        //            getString("CL.uninstall.error.netbeans.conf.am"), // NOI18N
        //            e);
        //}
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String SOA_CLUSTER =
            "{soa-cluster}"; // NOI18N
    public static final String ID =
            "SOA"; // NOI18N
    
    public static final String GLASSFISH_UID =
            "glassfish"; // NOI18N
    public static final String APPSERVER_UID =
            "sjsas"; // NOI18N

    public static final String OPENESB_UID =
            "openesb"; // NOI18N
    public static final String AM_UID = 
            "sjsam"; // NOI18N
    
    public static final String JVM_OPTION_AM_CONFIG = 
            "-DAM_CONFIG_FILE"; // NOI18N
    public static final String JVM_OPTION_GLASSFISH = 
            "-Dcom.sun.aas.installRoot"; // NOI18N
            
    public static final String AM_CONFIG_FILE = 
            "AMConfig.properties"; // NOI18N
}
