/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle, Inc.
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
    public Enumeration getEntryPaths(String string) {
        return Collections.enumeration(Collections.emptyList());
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
