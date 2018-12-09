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

package org.netbeans.installer.products.nb.uml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            UML_CLUSTER}, ID);
    }
    
    public void install(final Progress progress) throws InstallationException {
	super.install(progress);
        
        LogManager.log("Configuring UML...");
	
        // get the list of suitable netbeans ide installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one, integrate with it and resolve the dependency
        final File nbLocation = sources.get(0).getInstallationLocation();
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        /////////////////////////////////////////////////////////////////////////////
        // telelogic doors integration
        if(SystemUtils.isWindows()) {
            try {
                LogManager.indent();
                progress.setDetail(
                        getString("CL.install.telelogic.integration")); // NOI18N
                configureTelelogicDoors(nbLocation, progress, true);
            } catch (IOException ex) {
                throw new InstallationException(
                        getString("CL.install.error.telelogic.integration"),
                        ex);
            } finally {
                LogManager.unindent();
            }
        }
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        super.uninstall(progress);
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
        List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        if(SystemUtils.isWindows()) {            
            try {
                LogManager.indent();
                progress.setDetail(
                        getString("CL.uninstall.telelogic.integration")); // NOI18N
                configureTelelogicDoors(nbLocation, progress, false);
            } catch (IOException ex) {
                throw new UninstallationException(
                        getString("CL.uninstall.error.telelogic.integration"),
                        ex);
            } finally {
                LogManager.unindent();
            }
        }
    }
    
    private void configureTelelogicDoors(
            final File nbLocation, 
            final Progress progress, 
            final boolean install) throws IOException {
        try {
            
            File    location = new File(getProduct().getInstallationLocation(), 
                    UML_CLUSTER);
            String  doorsBin = new File(location, CONFIG_DOORS_LOCATION).getPath();
            
            ////////////////////////////////////////////////////////////////////
            // First configuration step:
            // Integrate (if install) with telelogic itself
            // If uninstall then skip this step
            if(install) {
                LogManager.log(ErrorLevel.DEBUG,
                        "... running script that integrates Telelogic Doors with NetBeans UML");
                
                String processPathEnv = SystemUtils.getEnvironmentVariable(PATH_ENV);
                if(processPathEnv==null) {
                    processPathEnv = StringUtils.EMPTY_STRING;
                }
                
                if(!processPathEnv.contains(doorsBin)) {
                    processPathEnv += File.pathSeparator + doorsBin;
                    SystemUtils.setEnvironmentVariable(PATH_ENV,processPathEnv);
                }
                
                SystemUtils.executeCommand(nbLocation, new String [] {
                    CSCRIPT,
                    new File(location, CONFIG_DOORS_COMMAND).getPath(),
                    nbLocation.getAbsolutePath()
                });
            } else {
                LogManager.log(ErrorLevel.DEBUG,
                        "... cancel telelogic doors integration");
            }
            
            ////////////////////////////////////////////////////////////////////
            // Second configuration step:
            // Install   : modify system/user PATH variable so the necessary .dll would be in path
	    // 	           Due to the discussion at 
	    //             http://www.netbeans.org/issues/show_bug.cgi?id=98633   
            //             the manual adding of the addon is switched off
            // Uninstall : remove the necessary path from the the system envvar PATH
            LogManager.log(ErrorLevel.DEBUG,
                    "... modify PATH environent variable within windows registry");
            WindowsRegistry winReg =
                    ((WindowsNativeUtils) SystemUtils.getNativeUtils()).
                    getWindowsRegistry();
            
            // set appropriate environment scope
            EnvironmentScope scope = winReg.canModifyKey(
                    WindowsRegistry.HKEY_LOCAL_MACHINE,
                    WindowsNativeUtils.ALL_USERS_ENVIRONMENT_KEY) ?
                        EnvironmentScope.ALL_USERS :
                        EnvironmentScope.CURRENT_USER;
            
            LogManager.log(ErrorLevel.DEBUG,
                    "... environment access level is " + scope.toString());
            
            String pathValue = SystemUtils.getEnvironmentVariable(PATH_ENV, scope, false);
            LogManager.log(ErrorLevel.DEBUG,
                    "... old PATH : " + pathValue);
            if (pathValue==null && !install) {
                // no PATH env variable in the registry and is not an installation process
                return;
            }
            
            pathValue = (pathValue == null) ? StringUtils.EMPTY_STRING : pathValue;
            
            if (install) {
                /* // commented due to the comments above
		if(!pathValue.contains(doorsBin)) {
                    pathValue += File.pathSeparator + doorsBin;
                }
		*/ 
            } else {
                // remove all occurences of the path to DLL directory
                pathValue = pathValue.
                        replace(File.pathSeparator + doorsBin, StringUtils.EMPTY_STRING). // remove ";%path%""
                        replace(doorsBin, StringUtils.EMPTY_STRING); //remove "%path%" if they still exist
                
            }
            
	    if(!install) {                
                LogManager.log(ErrorLevel.DEBUG,
                    "... new PATH : " + pathValue);	    
		// last parameters is true because PATH in most cases set to expandable
                SystemUtils.setEnvironmentVariable(PATH_ENV, pathValue, scope, true);            
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "... Telelogic Doors configuration finished");
            
        } catch (NativeException ex) {
            IOException e = new IOException();
            e.initCause(ex);
            throw e;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String UML_CLUSTER =
            "{uml-cluster}"; // NOI18N
    public static final String ID =
            "UML"; // NOI18N
    
    private static final String PATH_ENV = "PATH"; // NOI18N
    
    private static final String CONFIG_DOORS_LOCATION =            
            "modules" + File.separator + // NOI18N
            "DoorsIntegrationFiles" + File.separator +
            "modules" +  File.separator + "bin";// NOI18N
    
    private static final String CSCRIPT = "cscript"; // NOI18N
    
    private static final String CONFIG_DOORS_COMMAND =
            "modules" + File.separator + // NOI18N
            "DoorsIntegrationFiles" + File.separator + // NOI18N
            "configDoors.vbs"; // NOI18N
}
