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
package org.netbeans.modules.netbinox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Manifest;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** This is fake bundle, created by the Netigso infrastructure.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoBundleFile extends BundleFile {
    private final BundleData data;
    NetigsoBundleFile(File base, BundleData data) {
        super(base);
        this.data = data;
    }

    @Override
    public File getFile(String string, boolean bln) {
        return null;
    }

    @Override
    public BundleEntry getEntry(String entry) {
        if ("META-INF/MANIFEST.MF".equals(entry)) { // NOI18N
            return new BundleEntry() {
                @Override
                public InputStream getInputStream() throws IOException {
                    for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                        if (data.getLocation().endsWith(mi.getCodeNameBase())) {
                            return fakeManifest(mi);
                        }
                    }
                    throw new IOException("Cannot find " + data.getLocation());
                }

                @Override
                public long getSize() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public String getName() {
                    return "META-INF/MANIFEST.MF"; // NOI18N
                }

                @Override
                public long getTime() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public URL getLocalURL() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public URL getFileURL() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        return null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String string) {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public boolean containsDir(String string) {
        return false;
    }

    private static InputStream fakeManifest(ModuleInfo m) throws IOException {
        String exp = (String) m.getAttribute("OpenIDE-Module-Public-Packages"); // NOI18N
        if ("-".equals(exp)) { // NOI18N
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        man.getMainAttributes().putValue("Bundle-ManifestVersion", "2"); // NOI18N
        man.getMainAttributes().putValue("Bundle-SymbolicName", m.getCodeNameBase()); // NOI18N

        if (m.getSpecificationVersion() != null) {
            String spec = threeDotsWithMajor(m.getSpecificationVersion().toString(), m.getCodeName());
            man.getMainAttributes().putValue("Bundle-Version", spec.toString()); // NOI18N
        }
        if (exp != null) {
            man.getMainAttributes().putValue("Export-Package", exp.replaceAll("\\.\\*", "")); // NOI18N
        } else {
            man.getMainAttributes().putValue("Export-Package", m.getCodeNameBase()); // NOI18N
        }
        man.write(os);
        return new ByteArrayInputStream(os.toByteArray());
    }
    private static String threeDotsWithMajor(String version, String withMajor) {
        int indx = withMajor.indexOf('/');
        int major = 0;
        if (indx > 0) {
            major = Integer.parseInt(withMajor.substring(indx + 1));
        }
        String[] segments = (version + ".0.0.0").split("\\.");
        assert segments.length >= 3 && segments[0].length() > 0;

        return (Integer.parseInt(segments[0]) + major * 100) + "."  + segments[1] + "." + segments[2];
    }
}
