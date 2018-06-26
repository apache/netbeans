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
