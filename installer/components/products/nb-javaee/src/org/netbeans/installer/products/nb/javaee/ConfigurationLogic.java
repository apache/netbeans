/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.installer.products.nb.javaee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants

    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    //private static final String VISUALWEB_CLUSTER =
    //        "{visualweb-cluster}"; // NOI18N
    //private static final String IDENTITY_CLUSTER =
    //        "{identity-cluster}"; // NOI18N
    private static final String ID =
            "WEBEE"; // NOI18N
    private static final String MOBILITY_END_2_END_KIT =
            "org-netbeans-modules-mobility-end2end-kit";
    private static final String NB_JAVAME_UID = "nb-javame";
    private static final String MOBILITY_CLUSTER =
            "{mobility-cluster}";
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    public ConfigurationLogic() throws InitializationException {
        super(new String[]{ENTERPRISE_CLUSTER},
                    /* VISUALWEB_CLUSTER,
                    IDENTITY_CLUSTER},*/ ID);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
        final Product nbProduct =
                Registry.getInstance().getProducts(dependencies.get(0)).get(0);
        final File installLocation = nbProduct.getInstallationLocation();

        for (Product product : Registry.getInstance().getInavoidableDependents(nbProduct)) {
            if (product.getUid().equals(NB_JAVAME_UID) && product.getStatus().equals(Status.INSTALLED)) {
                //mobility installed, enable end2end kit                
                NetBeansUtils.setModuleStatus(product.getInstallationLocation(),
                        MOBILITY_CLUSTER,
                        MOBILITY_END_2_END_KIT,
                        true);
                break;
            }
        }

        //get bundled registry to perform further runtime integration
        //http://wiki.netbeans.org/NetBeansInstallerIDEAndRuntimesIntegration
        Registry bundledRegistry = new Registry();
        try {
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry(
                    (bundledRegistryUri != null) ? bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.tomcat.integration")); // NOI18N

            final List<Product> tomcats =
                    Registry.getInstance().getProducts("tomcat");

            Product productToIntegrate = null;
            for (Product tomcat : tomcats) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        tomcat.getUid(), tomcat.getVersion());
                if (tomcat.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File location = tomcat.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        productToIntegrate = tomcat;
                        break;
                    }
                }
            }
            if (productToIntegrate == null) {
                for (Product tomcat : tomcats) {
                    if (tomcat.getStatus() == Status.INSTALLED) {
                        final File location = tomcat.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            productToIntegrate = tomcat;
                            break;
                        }
                    }
                }
            }
            if (productToIntegrate != null) {
                final File location = productToIntegrate.getInstallationLocation();
                LogManager.log("... integrate " + nbProduct.getDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);
                registerTomcat(installLocation, location);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.tomcat.integration"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.glassfish.integration")); // NOI18N

            final List<Product> glassfishes =
                    Registry.getInstance().queryProducts(
                    new ProductFilter("glassfish-mod", Registry.getInstance().getTargetPlatform()));

            Product glassfishToIntegrate = null;
            for (Product glassfish : glassfishes) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        glassfish.getUid(), glassfish.getVersion());
                if (glassfish.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File location = glassfish.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        glassfishToIntegrate = glassfish;
                        break;
                    }
                }
            }
            if (glassfishToIntegrate == null) {
                for (Product glassfish : glassfishes) {
                    if (glassfish.getStatus() == Status.INSTALLED) {
                        final File location = glassfish.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            glassfishToIntegrate = glassfish;
                            break;
                        }
                    }
                }
            }
            if (glassfishToIntegrate != null) {
                File gfLocation = glassfishToIntegrate.getInstallationLocation();
                if (!isGlassFishRegistred(installLocation)) {
                    LogManager.log("... integrate " + getSystemDisplayName() + " with " + glassfishToIntegrate.getDisplayName() + " installed at " + gfLocation);
                    registerGlassFish(installLocation, gfLocation);
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.glassfish.integration"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
    }

    private boolean isGlassFishRegistred(File nbLocation) throws IOException {
        return new File(nbLocation, "nb/config/GlassFishEE6WC/Instances/glassfish_autoregistered_instance").exists();
    }

    private boolean registerGlassFish(File nbLocation, File gfLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String[] cp = {
            "platform/core/core.jar",
            "platform/core/core-base.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "platform/lib/org-openide-util-ui.jar",
            "enterprise/modules/org-netbeans-modules-j2eeapis.jar",
            "enterprise/modules/org-netbeans-modules-j2eeserver.jar",
            "enterprise/modules/org-netbeans-modules-glassfish-common.jar",
            "enterprise/modules/org-netbeans-modules-glassfish-tooling.jar"
        };
        for (String c : cp) {
            File f = new File(nbLocation, c);
            if (!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for GlassFish integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.glassfish.common.registration.AutomaticRegistration";
        List<String> commands = new ArrayList<String>();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add(nbCluster.getAbsolutePath());
        commands.add(new File(gfLocation, "glassfish").getAbsolutePath());

        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }

    private boolean registerTomcat(File nbLocation, File tomcatLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String[] cp = {
            "platform/core/core.jar",
            "platform/core/core-base.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "platform/lib/org-openide-util-ui.jar",
            "enterprise/modules/org-netbeans-modules-j2eeapis.jar",
            "enterprise/modules/org-netbeans-modules-j2eeserver.jar",
            "enterprise/modules/org-netbeans-modules-tomcat5.jar"                
        };
        for (String c : cp) {
            File f = new File(nbLocation, c);
            if (!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for Tomcat integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.tomcat5.registration.AutomaticRegistration";
        List<String> commands = new ArrayList<String>();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add("--add");
        commands.add(nbCluster.getAbsolutePath());
        commands.add(tomcatLocation.getAbsolutePath());
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }    
}
