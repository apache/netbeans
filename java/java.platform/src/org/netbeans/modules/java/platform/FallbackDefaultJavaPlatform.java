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

package org.netbeans.modules.java.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * Basic impl in case no other providers can be found.
 * @author Jesse Glick
 * @author Tomas Zezula
 */
public final class FallbackDefaultJavaPlatform extends JavaPlatform {

    private static final Logger LOG = Logger.getLogger(FallbackDefaultJavaPlatform.class.getName());
    private static final Map<String,String> JAVADOC_URLS;
    static {
        final Map<String,String> m = new HashMap<>();
        m.put("1.5", "https://docs.oracle.com/javase/1.5.0/docs/api/"); // NOI18N
        m.put("1.6", "https://docs.oracle.com/javase/6/docs/api/"); // NOI18N
        m.put("1.7", "https://docs.oracle.com/javase/7/docs/api/"); // NOI18N
        m.put("1.8", "https://docs.oracle.com/javase/8/docs/api/"); // NOI18N
        JAVADOC_URLS = Collections.unmodifiableMap(m);
    }

    private static FallbackDefaultJavaPlatform instance;

    private volatile List<URL> javadoc;
    private volatile ClassPath src;

    private FallbackDefaultJavaPlatform() {
        setSystemProperties(NbCollections.checkedMapByFilter(System.getProperties(), String.class, String.class, false));
    }

    @Override
    public String getDisplayName() {
        return System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.version"); // NOI18N
    }

    @Override
    public Map<String,String> getProperties() {
        return Collections.singletonMap("platform.ant.name", "default_platform");
    }

    private static ClassPath sysProp2CP(String propname) {
        String sbcp = System.getProperty(propname);
        if (sbcp == null) {
            return null;
        }
        List<URL> roots = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(sbcp, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            File f = new File(tok.nextToken());
            if (!f.exists()) {
                continue;
            }
            URL u;
            try {
                File normf = FileUtil.normalizeFile(f);
                u = Utilities.toURI(normf).toURL();
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            } 
            if (FileUtil.isArchiveFile(u)) {
                u = FileUtil.getArchiveRoot(u);
            }
            roots.add(u);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
    }

    private static ClassPath sampleClass2CP(Class prototype) {
        CodeSource cs = prototype.getProtectionDomain().getCodeSource();
        return ClassPathSupport.createClassPath(cs != null ? new URL[] {cs.getLocation()} : new URL[0]);
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        // XXX ignore standard extensions etc.
        ClassPath cp = sysProp2CP("sun.boot.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(Object.class);
    }

    @Override
    public ClassPath getStandardLibraries() {
        ClassPath cp = sysProp2CP("java.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(/* likely in startup CP */ Dependency.class);
    }

    @Override
    public String getVendor() {
        return System.getProperty("java.vm.vendor");
    }

    @Override
    public Specification getSpecification() {
        return new Specification(/*J2SEPlatformImpl.PLATFORM_J2SE*/"j2se", Dependency.JAVA_SPEC); // NOI18N
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return Collections.singleton(FileUtil.toFileObject(FileUtil.normalizeFile(new File(System.getProperty("java.home"))))); // NOI18N
    }

    @Override
    public FileObject findTool(String toolName) {
        return null; // XXX too complicated, probably unnecessary for this purpose
    }

    @Override
    public ClassPath getSourceFolders() {
        ClassPath res = src;
        if (res == null) {
            res = src = ClassPathSupport.createClassPath(
                getSources(getInstallFolders()));
        }
        return res;
    }

    @Override
    public List<URL> getJavadocFolders() {
        List<URL> res = javadoc;
        if (res == null) {
            final SpecificationVersion version = getSpecification().getVersion();
            final URL root = toURL(JAVADOC_URLS.get(version.toString()));
            res = javadoc = root == null ?
                Collections.<URL>emptyList() :
                Collections.<URL>singletonList(root);
        }
        return res;
    }

    @NonNull
    private static FileObject[] getSources(@NonNull final Collection<? extends FileObject> installFolders) {
        if (installFolders.isEmpty()) {
            return new FileObject[0];
        }
        final FileObject installFolder = installFolders.iterator().next();
        if (installFolder == null || !installFolder.isValid()) {
            return new FileObject[0];
        }
        FileObject src = installFolder.getFileObject("src.zip");    //NOI18N
        if (src == null) {
            src = installFolder.getFileObject("src.jar");    //NOI18N
        }
        if (src == null || !src.canRead()) {
            return new FileObject[0];
        }
        FileObject root = FileUtil.getArchiveRoot(src);
        if (root == null) {
            return new FileObject[0];
        }
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            FileObject reloc = root.getFileObject("src");   //NOI18N
            if (reloc != null) {
                root = reloc;
            }
        }
        return new FileObject[]{root};
    }

    @CheckForNull
    private static URL toURL(@NullAllowed final String root) {
        if (root == null) {
            return null;
        }
        try {
            return new URL (root);
        } catch (MalformedURLException e) {
            LOG.log(
                Level.WARNING,
                "Invalid Javadoc root: {0}",    //NOI18N
                root);
            return null;
        }
    }

    public static synchronized FallbackDefaultJavaPlatform getInstance() {
        if (instance == null) {
            instance = new FallbackDefaultJavaPlatform();
        }
        return instance;
    }

}
