/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
