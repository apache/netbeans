/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                String execFilesS = owner.getHeaders().get("NetBeans-Executable-Files");
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
