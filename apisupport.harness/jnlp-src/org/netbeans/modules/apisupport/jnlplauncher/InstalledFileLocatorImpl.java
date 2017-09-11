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

package org.netbeans.modules.apisupport.jnlplauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Special locator for JNLP mode.
 * Locates files in userdir; JARs with the special META-INF/clusterpath/$relpath entry;
 * and JARs present in extra-files.jar.
 * @author Jesse Glick
 */
@ServiceProvider(service=InstalledFileLocator.class, supersedes="org.netbeans.core.startup.InstalledFileLocatorImpl")
public class InstalledFileLocatorImpl extends InstalledFileLocator {

    public InstalledFileLocatorImpl() {}

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        if (localized) {
            int i = relativePath.lastIndexOf('.');
            String baseName, ext;
            if (i == -1 || i < relativePath.lastIndexOf('/')) {
                baseName = relativePath;
                ext = "";
            } else {
                baseName = relativePath.substring(0, i);
                ext = relativePath.substring(i);
            }
            Iterator<String> it = NbBundle.getLocalizingSuffixes();
            while (it.hasNext()) {
                String locName = baseName + it.next() + ext;
                File f = locate(locName, codeNameBase, false);
                if (f != null) {
                    return f;
                }
            }
        } else {
            String userdir = System.getProperty("netbeans.user");
            if (userdir != null) {
                File f = new File(userdir, relativePath.replace('/', File.separatorChar));
                if (f.exists()) {
                    return f;
                }
            }
            String resource = "META-INF/clusterpath/" + relativePath;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL found = loader.getResource(resource);
            if (found != null) {
                String foundS = found.toExternalForm();
                String prefix = "jar:";
                String suffix = "!/" + resource;
                if (foundS.startsWith(prefix) && foundS.endsWith(suffix)) {
                    String infix = foundS.substring(prefix.length(), foundS.length() - suffix.length());
                    if (infix.startsWith("file:")) {
                        File jar = Utilities.toFile(URI.create(infix));
                        assert jar.isFile();
                        return jar;
                    }
                }
            }
            try {
                InputStream is = loader.getResourceAsStream("META-INF/files/" + relativePath);
                if (is != null) {
                    try {
                        // XXX could try to cache previously created files
                        File temp = File.createTempFile("nbjnlp-", relativePath.replaceFirst("^.+/", ""));
                        temp.deleteOnExit();
                        OutputStream os = new FileOutputStream(temp);
                        try {
                            byte[] buf = new byte[4096];
                            int read;
                            while ((read = is.read(buf)) != -1) {
                                os.write(buf, 0, read);
                            }
                        } finally {
                            os.close();
                        }
                        return temp;
                    } finally {
                        is.close();
                    }
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

}
