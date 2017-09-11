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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * Rather dummy implementation of the Java Platform, but sufficient for communication
 * inside the Wizard.
 */
public final class NewJ2SEPlatform extends J2SEPlatformImpl implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NewJ2SEPlatform.class.getName());

    private boolean valid;

    public static NewJ2SEPlatform create (FileObject installFolder) throws IOException {
        assert installFolder != null;
        Map<String,String> platformProperties = new HashMap<> ();
        return new NewJ2SEPlatform (null,Collections.singletonList(installFolder.toURL()),platformProperties,Collections.<String,String>emptyMap());
    }

    private NewJ2SEPlatform (String name, List<URL> installFolders, Map<String,String> platformProperties, Map<String,String> systemProperties) {
        super(name, name, installFolders, platformProperties, systemProperties,null,null);
    }

    public boolean isValid () {
        return this.valid;
    }

    /**
     * Actually performs the detection and stores relevant information
     * in this Iterator
     */
    @Override
    public void run() {
        try {
            //Verify all needed tools
            for (String toolName : PlatformConvertor.IMPORTANT_TOOLS) {
                if (findTool(toolName) == null) {
                    return;
                }
            }
            FileObject java = findTool("java"); //NOI18N
            if (java == null) {
                return;
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null) {
                return;
            }
            String javapath = javaFile.getAbsolutePath();
            String filePath = File.createTempFile("nb-platformdetect", "properties").getAbsolutePath(); //NOI18N
            final String probePath = getSDKProperties(javapath, filePath);
            File f = new File(filePath);
            Properties p = new Properties();
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
            }
            final Collection<? extends FileObject> instFolders = getInstallFolders();
            final Map<String,String> m = new HashMap<>(p.size());
            for (Enumeration en = p.keys(); en.hasMoreElements(); ) {
                String k = (String)en.nextElement();
                String v = p.getProperty(k);
                if (J2SEPlatformImpl.SYSPROP_USER_DIR.equals(k)) {
                    v = ""; //NOI18N
                }
                v = Util.fixSymLinks (k,v, instFolders);
                m.put(k, v);
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(
                        Level.FINEST,
                        "Platform properties: {0}", //NOI18N
                        m);
            }
            this.setSystemProperties(Util.filterProbe(m, probePath));
            this.valid = true;
            f.delete();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot execute probe process", ex);
            this.valid = false;
        }
    }

    private String getSDKProperties(String javaPath, String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seplatform-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.java.j2seplatform.wizard.SDKProbe";
            command[4] = path;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Executing: %s %s %s %s %s", command[0],command[1],command[2],command[3],command[4]));
            }
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new IOException(String.format("Java process exit code: %d", exitValue));  //NOI18N
            }
            return command[2];
        } catch (InterruptedException ex) {
            IOException e = new IOException(ex);
            throw e;
        }
    }
}
