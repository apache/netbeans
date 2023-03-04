/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Jiricka
 */
public final class JSPServletFinder {
    
    public static final String SERVLET_FINDER_CHANGED = "servlet-finder-changed"; // NOI18N
    
    private Project project;

    /** Returns JSPServletFinder for the project that contains given file.
     * @return null if the file is not in any project
     */
    public static JSPServletFinder findJSPServletFinder(FileObject f) {
        Project prj = FileOwnerQuery.getOwner (f);
        return prj == null ? null : new JSPServletFinder(prj);
    }
    
    /** Creates a new instance of JspServletFinderImpl */
    private JSPServletFinder (Project project) {
        this.project = project;
    }
    
    private J2eeModuleProvider getProvider() {
        return (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
    }
    
    /** Returns the server instance currently selected for the module associated with this JSPServletFinder.
     * May return null.
     */
    private ServerString getServerString() {
        J2eeModuleProvider dl = getProvider ();
        if (dl == null)
            return null;
        ServerInstance instance = ServerRegistry.getInstance ().getServerInstance (dl.getServerInstanceID ());
        return instance == null ? null : new ServerString (instance);
    }
    
    
    private String getWebURL() {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        try {
            return provider.getConfigSupport().getWebContextRoot();
        } catch (ConfigurationException e) {
            return null;
        }
    }
    
    /** Returns the FindJSPServlet class associated with this JSPServletFinder.
     * May return null.
     */
    private FindJSPServlet getServletFinder() {
        ServerString serverS = getServerString();
        if (serverS == null)
            return null;
        ServerInstance inst = serverS.getServerInstance();
        if (inst == null)
            return null;
        return inst.getFindJSPServlet();
    }
    
    public File getServletTempDirectory () {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        //TargetModuleID moduleID = getTargetModuleID();
        //if (moduleID == null)
        //    return null;
        return find.getServletTempDirectory(webURL);
        
/*        try {
            J2eeDeploymentLookup dl = getDeploymentLookup();
            J2eeProfileSettings settings = dl.getJ2eeProfileSettings();
            DeploymentTargetImpl target = new DeploymentTargetImpl(settings, dl);
            ServerString serverS = target.getServer();
            ServerInstance inst = serverS.getServerInstance();
            DeploymentManager dm = inst.getDeploymentManager();
System.out.println("getting servlet temp directory - dm is  " + dm);
            TargetModuleID mod[] = dm.getAvailableModules(ModuleType.WAR, serverS.toTargets());
            TargetModuleID mod0 = null; // PENDING - find by web URI
            FindJSPServlet find = inst.getFindJSPServlet();
System.out.println("getting servlet temp directory - find is " + find);
            return find.getServletTempDirectory(mod0);
        }
        catch (TargetException e) {
            // PENDING
            return null;
        }*/
    }
 
    public String getServletResourcePath(String jspResourcePath) {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        if (null == jspResourcePath)
            throw new NullPointerException("jspResourcePath");
        return find.getServletResourcePath(webURL, jspResourcePath);
    }
 
    public String getServletEncoding(String jspResourcePath) {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        return find.getServletEncoding(webURL, jspResourcePath);
    }
 
    @CheckForNull
    public String getServletBasePackageName() {
        FindJSPServlet find = getServletFinder();
        if (!(find instanceof FindJSPServlet2)) {
            return null;
        }
        String webURL = getWebURL();
        if (webURL == null) {
            return null;     
        }
        return ((FindJSPServlet2) find).getServletBasePackageName(webURL);
    }
    
    @CheckForNull
    public String getServletSourcePath(String jspRelativePath) {
        FindJSPServlet find = getServletFinder();
        if (!(find instanceof FindJSPServlet2)) {
            return null;
        }
        String webURL = getWebURL();
        if (webURL == null) {
            return null;     
        }
        return ((FindJSPServlet2) find).getServletSourcePath(webURL, jspRelativePath);
    }    
    
    public OldJSPDebug.JspSourceMapper getSourceMapper(String jspResourcePath) {
        // PENDING
        return null;
    }
 
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // PENDING
    }
 
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // PENDING
    }
}
