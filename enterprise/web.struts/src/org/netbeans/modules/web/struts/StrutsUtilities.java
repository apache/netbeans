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
package org.netbeans.modules.web.struts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.ui.StrutsConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Struts utilities methods.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class StrutsUtilities {

    private StrutsUtilities() {
    }

    /**
     * Enables Struts support in the given web project. If the enabling was done outside standard Add framework wizard,
     * default initial values are used.
     *
     * @param webModule webModule to extend
     * @param panel configuration panel, can be {@code null} - in that case are default values used
     * @return set of newly created files
     */
    public static Set<FileObject> enableStruts(WebModule webModule, StrutsConfigurationPanel panel) {
        if (panel == null) {
            panel = new StrutsConfigurationPanel(null, null, true);
        }

        FileObject fo = webModule.getDocumentBase();
        Project project = FileOwnerQuery.getOwner(fo);
        Set result = new HashSet();

        Library lib = LibraryManager.getDefault().getLibrary("struts");                         //NOI18N
        if (lib != null) {
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sgs.length > 0) {
                try {
                    ProjectClassPathModifier.addLibraries(new Library[] {lib}, sgs[0].getRootFolder(), ClassPath.COMPILE);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            try {
                FileObject webInf = webModule.getWebInf();
                if (webInf == null) {
                    webInf = FileUtil.createFolder(webModule.getDocumentBase(), "WEB-INF"); //NOI18N
                }
                assert webInf != null;
                FileSystem fs = webInf.getFileSystem();
                fs.runAtomicAction(new StrutsFrameworkProvider.CreateStrutsConfig(webModule, panel));
                result.add(webModule.getDocumentBase().getFileObject("welcomeStruts", "jsp"));
            } catch (FileNotFoundException exc) {
                Exceptions.printStackTrace(exc);
            } catch (IOException exc) {
                Logger.getLogger("global").log(Level.INFO, null, exc);
            }
        }
        return result;
    }

    /**
     * Says whether the Struts support is available in given web module.
     * @param wm webmodule to be examined
     * @return {@code true} if the support is enabled, {@code false} otherwise
     */
    public static boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        // The JavaEE 5 introduce web modules without deployment descriptor.
        // In such wm can not be struts used.
        FileObject dd = wm.getDeploymentDescriptor();
        return (dd != null && StrutsConfigUtilities.getActionServlet(dd) != null);
    }

}
