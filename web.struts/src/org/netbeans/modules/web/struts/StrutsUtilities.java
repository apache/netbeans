/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
