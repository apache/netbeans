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

package org.netbeans.installer.products.javaeesdk.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * The Java EE SDK Samples can be only installed in the Sun Java System Application Server 
 * as it requires <samples>/common.properties file to exist
 * 
 * 
 
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/products/javaeesdk/samples/wizard.xml"; // NOI18N
    
    private static final String APPSERVER_UID =
            "sjsas"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(Progress progress) throws InstallationException {
        File location = getProduct().getInstallationLocation();
        File samplesLocation = new File(location, "samples");
        File bpProjectLocation = new File(samplesLocation, "bp-project");
        File template  = new File(bpProjectLocation, "build.properties.sample");
        File dest      = new File(bpProjectLocation, "build.properties");
        FilesList list = getProduct().getInstalledFiles();
        
        FileInputStream fis  = null;
        try {
            String contents = FileUtils.readFile(template);
            Properties props = new Properties();
            fis = new FileInputStream(new File(samplesLocation, "common.properties"));
            props.load(fis);
            String adminUser  = props.getProperty("admin.user");
            String httpPort   = props.getProperty("appserver.instance.port");
            String adminPort  = props.getProperty("admin.port");
            String hostname   = props.getProperty("admin.host");
            String asLocation = props.getProperty("com.sun.aas.installRoot");
            
            contents = contents.
                    replace("javaee.server.name=localhost", "javaee.server.name=" + hostname).
                    replace("javaee.server.port=8080", "javaee.server.port=" + httpPort).
                    replace("javaee.adminserver.port=4848","javaee.adminserver.port=" + adminPort).
                    replace("javaee.server.username=admin","javaee.server.username=" + adminUser).
                    replace("javaee.home=c:/Sun/SDK", "javaee.home=" + asLocation);
            
            list.add(FileUtils.writeFile(dest, contents));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.samples.configuration"),e);
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
        
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    public boolean registerInSystem() {
        return false;
    }
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
    
    @Override
    public Text getLicense() {
       return null;
    }    
}
