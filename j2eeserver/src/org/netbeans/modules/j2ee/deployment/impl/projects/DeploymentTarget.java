/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.deployment.impl.projects;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.openide.filesystems.FileUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.j2ee.deployment.config.ConfigSupportImpl;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.TargetModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.WebTargetModuleID;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** 
 *
 * @author George FinKlang
 * @author Petr Hejl
 */
public final class DeploymentTarget {
    
    private final J2eeModuleProvider moduleProvider;
    private final String clientName;
    private ServerString server;
    private TargetModule[] targetModules;
    
    public DeploymentTarget(J2eeModuleProvider moduleProvider, String clientName) {
        this.moduleProvider = moduleProvider;
        this.clientName = clientName;
    }
    
    public J2eeModule getModule() {
        return moduleProvider.getJ2eeModule ();
    }

    public J2eeModuleProvider getModuleProvider() {
        return moduleProvider;
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return moduleProvider.getModuleChangeReporter ();
    }

    public ResourceChangeReporter getResourceChangeReporter() {
        return moduleProvider.getResourceChangeReporter();
    }
    
    /**
     * This will return url to invoke webbrowser for web client.
     * If there is no webclient, null will be returned.
     */
    public String getClientUrl(String partUrl) {
        // determine client module
        J2eeModule clientModule = null;
        String url = null;
        
        if (moduleProvider instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider ear = (J2eeApplicationProvider) moduleProvider;
            J2eeModuleProvider clientProvider = getChildModuleProvider(ear, clientName);
            if (clientProvider != null)
                clientModule = clientProvider.getJ2eeModule();
            else {
                //findWebUrl(null) will take care to find a first weburl it sees, but just to be sure...
                J2eeApplication jmc = (J2eeApplication) ear.getJ2eeModule();
                J2eeModule[] modules = jmc.getModules();
                for (int i=0; i<modules.length; i++) {
                    if (J2eeModule.Type.WAR.equals(modules[i].getType())) {
                        clientModule = modules[i];
                        break;
                    }
                }
            }
        } else {
            clientModule = moduleProvider.getJ2eeModule();
        }

        if (clientModule != null && clientModule.getType().equals(J2eeModule.Type.WAR)) {
            url = findWebUrl(clientModule);
            if (url != null) {
                StringBuilder sb = new StringBuilder(url);
                int length = sb.length();
                if (length > 0 && (sb.charAt(length - 1) == '/')) { // NOI18N
                    sb.setLength(length - 1);
                }
                if (partUrl.startsWith("/") || partUrl.length() == 0) { // NOI18N
                    sb.append(partUrl);
                } else {
                    sb.append('/').append(partUrl); // NOI18N
                }
                return sb.toString();
            } else {
                return null;
            }
        }
        return null;
    }

    private J2eeModuleProvider getChildModuleProvider(J2eeModuleProvider jmp, String uri) {
        if (uri == null)
            return null;
        J2eeModuleProvider child = null;
        if (jmp instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider jap = (J2eeApplicationProvider) jmp;
            child = jap.getChildModuleProvider(uri);
            if (child == null) {
                String root = "/" ; // NOI18N
                if (uri.startsWith(root)) {
                    uri = uri.substring(1);
                } else {
                    uri = root + uri;
                }
                child = jap.getChildModuleProvider(uri);
            }
            // see issue #160406
            if (child == null) {
                Pattern pattern = Pattern.compile(
                        Pattern.quote(normalizeUri(uri) + ".") + "(war|jar)"); // NOI18N
                for(J2eeModuleProvider prov : jap.getChildModuleProviders()) {
                    String childUri = prov.getJ2eeModule().getUrl();
                    if (childUri != null && pattern.matcher(childUri).matches()) {
                        return prov;
                    }
                }
            }
        }
        return child;
    }

    private TargetModule getTargetModule() {
        TargetModule[] mods = getTargetModules();
        if (mods == null || mods.length == 0) {
            return null;
        }

        if (mods[0].delegate() != null) {
            return mods[0];
        }


        mods[0].initDelegate(J2eeModuleAccessor.getDefault().getJsrModuleType(getModule().getType()));
        return mods[0];
    }

    /**
     * Find the web URL for the given client module.
     * If null is passed, or when plugin failed to resolve the child module url,
     * this will attempt to return the first web url it sees.
     */
    private String findWebUrl(J2eeModule client) {
        TargetModule module = getTargetModule();
        if (module == null) {
            return null;
        }
        if (getModule() == client) { // stand-alone web
            return getUrl(module);
        }
        
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(module.getInstanceUrl());
        IncrementalDeployment mur = instance.getIncrementalDeployment();
        String clientModuleUri = client == null ? "" : client.getUrl();        
        clientModuleUri = normalizeUri(clientModuleUri);

        TargetModuleID[] children = module.getChildTargetModuleID();
        String urlString = null;
        TargetModuleID tmid = null;
        for (int i=0; children != null && i<children.length; i++) {
            // remember when see one, just for a rainy day
            if (urlString == null || urlString.trim().equals("")) {
                urlString = getUrl(children[i]);
            }
            
            String uri = children[i].getModuleID(); //NOI18N
            if (mur != null) {
                uri = mur.getModuleUrl(children[i]);
            } else {
                int j = uri.indexOf('#');
                if (j > -1) {
                    uri = uri.substring(j+1);
                }
            }
            
            uri = normalizeUri(uri);
            if (clientModuleUri.equalsIgnoreCase(uri)) {
                tmid = children[i];
                break;
            }
        }
        // prefer the matched
        if (tmid != null) {
            urlString = getUrl(tmid);
        } else if (children == null || children.length == 0) {
            urlString = getUrl(module);
        }
        
        return urlString;
    }
    
    private static String normalizeUri(String uri) {
        if (!uri.startsWith("/")) { // NOI18N
            return "/" + uri; // NOI18N
        }
        return uri;
    }

    private static String getUrl(TargetModuleID id) {
        if (id instanceof WebTargetModuleID) {
            URL u = ((WebTargetModuleID) id).resolveWebURL();
            if (u != null) {
                return u.toString();
            }
        }
        return id.getWebURL();
    }
    
    public File getConfigurationFile() {
        return J2eeModuleProviderAccessor.getDefault().getConfigSupportImpl(moduleProvider).getConfigurationFile();
    }
    
    public ServerString getServer() {
        if (server == null) {
            String instanceID = moduleProvider.getServerInstanceID ();
            ServerInstance inst = ServerRegistry.getInstance ().getServerInstance (instanceID);
            if (inst == null) {
                throw new RuntimeException(NbBundle.getMessage(DeploymentTarget.class, "MSG_TargetServerNotFound",instanceID));
            }
            server = new ServerString(inst);
        }
        return server;
    }
    
    public TargetModule[] getTargetModules() {
        if (targetModules == null || targetModules.length == 0) {
            String fname = getTargetModuleFileName();
            if (fname == null) {
                return null;
            }
            targetModules = TargetModule.load(getServer(), fname);
        }
        return targetModules.clone();
    }
    
    public void setTargetModules(TargetModule[] targetModules) {
        this.targetModules = targetModules.clone();
        for (int i=0; i< targetModules.length; i++) {
            String fname = getTargetModuleFileName();
            if (fname != null) {
                targetModules[i].updateTimestamp();
                targetModules[i].save(fname);
            }
        }
    }
    
    public ModuleConfigurationProvider getModuleConfigurationProvider() {
        return J2eeModuleProviderAccessor.getDefault().getConfigSupportImpl(moduleProvider);
    }
    
    public J2eeModuleProvider.ConfigSupport getConfigSupport () {
        return moduleProvider.getConfigSupport();
    }

    private String getTargetModuleFileName() {
        ConfigSupportImpl config = J2eeModuleProviderAccessor.getDefault().getConfigSupportImpl(moduleProvider);
        FileObject fo = config.getProjectDirectory();
        if (fo != null) {
            File file = FileUtil.toFile(fo);
            if (file != null) {
                // non-zero (but very low) probability of collision in names
                return TargetModule.shortNameFromPath(file.getAbsolutePath());
            }
        }

        try {
            if (getModule().getContentDirectory() != null) {
                File file = FileUtil.toFile(getModule().getContentDirectory());
                if (file != null) {
                    return TargetModule.shortNameFromPath(file.getAbsolutePath());
                }
            }
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }

        String name = moduleProvider.getDeploymentName();
        if (name != null) {
            return name;
        }
        return J2eeModuleProviderAccessor.getDefault().getConfigSupportImpl(moduleProvider).getDeploymentName();
    }
    
    public String getDeploymentName() {
        return moduleProvider.getDeploymentName();
    }
}
