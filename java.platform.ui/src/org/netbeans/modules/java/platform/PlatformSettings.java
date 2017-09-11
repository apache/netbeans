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
package org.netbeans.modules.java.platform;

import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;



/**
 *
 * @author Tomas  Zezula
 */
public class PlatformSettings {
    private static final PlatformSettings INSTANCE = new PlatformSettings();
    private static final String PROP_PLATFORMS_FOLDER = "platformsFolder"; //NOI18N
    private static final String[] APPLE_JAVAVM_FRAMEWORK_PATHS = new String[] {
        "/Library/Java/JavaVirtualMachines/",                       //NOI18N            // JDK bundles provided via the Developer package, developer previews, and 3rd party JVM
        "/System/Library/Java/JavaVirtualMachines/",                //NOI18N            //The location of the Java SE runtime home
        "/System/Library/Frameworks/JavaVM.framework/Versions/",    //NOI18N            //Old location
    };

    public PlatformSettings () {

    }

    public String displayName() {
        return NbBundle.getMessage(PlatformSettings.class,"TXT_PlatformSettings");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(PlatformSettings.class);
    }

    public File getPlatformsFolder () {
        File f = null;
        final String folderName = getPreferences().get(PROP_PLATFORMS_FOLDER, null);
        if (folderName == null) {
            if (Utilities.isMac()) {
                final FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return !name.startsWith(".");   //NOI18N
                    }
                };
                for (String path : APPLE_JAVAVM_FRAMEWORK_PATHS) {
                    final File tmp = new File (path);
                    if (tmp.canRead() && tmp.list(filter).length > 0) {
                        f = tmp;
                        break;
                    }
                }
            }
            if (f == null) {
                f = new File(System.getProperty("user.home"));  //NOI18N
                File tmp;
                while ((tmp = f.getParentFile())!=null) {
                    f = tmp;
                }
            }
        } else {
            f = new File (folderName);
        }
        return f;
    }

    public void setPlatformsFolder (File file) {
        getPreferences().put(PROP_PLATFORMS_FOLDER, file.getAbsolutePath());
    }


    public synchronized static PlatformSettings getDefault () {
        return INSTANCE;
    }
}
