/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * Adapted from {@code org.netbeans.modules.apisupport.jnlplauncher.InstalledFileLocatorImpl}.
 */
class OSGiInstalledFileLocator extends InstalledFileLocator {

    private static final String FILES = "OSGI-INF/files/";

    private final BundleContext context;

    public OSGiInstalledFileLocator(BundleContext context) {
        this.context = context;
    }

    // XXX locateAll impl

    public @Override File locate(String relativePath, String codeNameBase, boolean localized) {
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
            if (codeNameBase != null && relativePath.equals("modules/" + codeNameBase.replace('.', '-') + ".jar")) {
                for (Bundle owner : context.getBundles()) {
                    if (!owner.getSymbolicName().equals(codeNameBase)) {
                        continue;
                    }
                    String loc = owner.getLocation();
                    if (loc.startsWith("file:")) {
                        return Utilities.toFile(URI.create(loc));
                    }
                }
                return null;
            }
            String storage = context.getProperty(Constants.FRAMEWORK_STORAGE);
            if (storage == null) {
                return null;
            }
            File f = unpackedLocation(storage, relativePath);
            if (f.exists()) {
                return f;
            }
            for (Bundle owner : context.getBundles()) {
                if (codeNameBase != null && !owner.getSymbolicName().equals(codeNameBase)) {
                    continue;
                }
                switch (owner.getState()) {
                case Bundle.INSTALLED:
                case Bundle.UNINSTALLED:
                    continue;
                }
                if (owner.getResource(FILES + relativePath) == null) {
                    continue;
                }
                // OK, this bundle seems to own the file. Unpack everything it contains at once.
                String execFilesS = (String) owner.getHeaders().get("NetBeans-Executable-Files");
                List<String> execFiles = execFilesS == null ? Collections.<String>emptyList() : Arrays.asList(execFilesS.split(","));
                try {
                    for (URL resource : NbCollections.iterable(NbCollections.checkedEnumerationByFilter(owner.findEntries(FILES, null, true), URL.class, true))) {
                        String bundlepath = resource.getPath();
                        if (bundlepath.endsWith("/")) {
                            continue;
                        }
                        String name = bundlepath.substring(("/" + FILES).length());
                        File f2 = unpackedLocation(storage, name);
//                        System.err.println("unpacking " + resource + " to " + f2);
                        File dir = f2.getParentFile();
                        if (!dir.isDirectory() && !dir.mkdirs()) {
                            throw new IOException("Could not make " + dir);
                        }
                        InputStream is = resource.openStream();
                        try {
                            OutputStream os = new FileOutputStream(f2);
                            try {
                                byte[] buf = new byte[4096];
                                int read;
                                while ((read = is.read(buf)) != -1) {
                                    os.write(buf, 0, read);
                                }
                            } finally {
                                os.close();
                            }
                        } finally {
                            is.close();
                        }
                        if (execFiles.contains(name)) {
                            f2.setExecutable(true);
                        }
                    }
                    return f;
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
        return null;
    }

    private File unpackedLocation(String storage, String relativePath) {
        return new File(storage, "files/" + relativePath.replace('/', File.separatorChar)); // NOI18N
    }

}
