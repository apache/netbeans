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

package org.netbeans.modules.web.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author  mkuchtiak
 * @author Petr Slechta
 */
public class Utilities {

    private Utilities() {
    }

    /** Checks if the given file name can be created in the target folder.
     *
     * @param dir target directory
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    public static String canUseFileName (java.io.File dir, String relativePath, String objectName, String extension) {
        String newObjectName=objectName;
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (objectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        // check file name
        
        if (!checkFileName(objectName)) {
            return NbBundle.getMessage (Utilities.class, "MSG_invalid_filename", newObjectName); // NOI18N
        }
        // test if the directory is correctly specified
        FileObject folder = null;
        if (dir!=null) {
            try {
                 folder = org.openide.filesystems.FileUtil.toFileObject(dir);
            } catch(java.lang.IllegalArgumentException ex) {
                 return NbBundle.getMessage (Utilities.class, "MSG_invalid_path", relativePath); // NOI18N
            }
        }
            
        // test whether the selected folder on selected filesystem is read-only or exists
        if (folder!=  null) {
            // target filesystem should be writable
            if (!folder.canWrite ()) {
                return NbBundle.getMessage (Utilities.class, "MSG_fs_is_readonly"); // NOI18N
            }

            if (folder.getFileObject (newObjectName) != null) {
                return NbBundle.getMessage (Utilities.class, "MSG_file_already_exist", newObjectName); // NOI18N
            }

            if (org.openide.util.Utilities.isWindows ()) {
                if (checkCaseInsensitiveName (folder, newObjectName)) {
                    return NbBundle.getMessage (Utilities.class, "MSG_file_already_exist", newObjectName); // NOI18N
                }
            }
        }

        // all ok
        return null;
    }
    
    // helper check for windows, its filesystem is case insensitive (workaround the bug #33612)
    /** Check existence of file on case insensitive filesystem.
     * Returns true if folder contains file with given name and extension.
     * @param folder folder for search
     * @param name name of file
     * @param extension extension of file
     * @return true if file with name and extension exists, false otherwise.
     */    
    private static boolean checkCaseInsensitiveName (FileObject folder, String name) {
        // bugfix #41277, check only direct children
        java.util.Enumeration children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = (FileObject) children.nextElement ();
            if (name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean checkFileName(String str) {
        char c[] = str.toCharArray();
        for (int i=0;i<c.length;i++) {
            if (c[i]=='\\') return false;
            if (c[i]=='/') return false;
        }
        return true;
    }
    
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    public static boolean isJavaEE6Plus(TemplateWizard wizard) {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Profile profile = wm.getJ2eeProfile();
            return profile != null && profile.isAtLeast(Profile.JAVA_EE_6_WEB);
        }
        return false;
    }

    public static WebModule findWebModule(TemplateWizard wizard) {
        Project project = Templates.getProject(wizard);
        return WebModule.getWebModule(project.getProjectDirectory());
    }
}
