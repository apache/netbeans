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
