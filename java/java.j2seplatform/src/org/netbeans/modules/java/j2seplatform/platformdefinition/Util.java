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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs.NBJRTUtil;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

public class Util {

    public static final String PROTO_HTTP = "http";              //NOI18N
    public static final String PROTO_HTTPS = "https";            //NOI18N
    public static final String PROTO_FILE = "file";              //NOI18N
    private static final String JFXRT_PATH = "lib/jfxrt.jar";    //NOI18N
    private static final String MODULES_FOLDER = "modules";      //NOI18N
    private static final String MODULE_INFO = "module-info.class";  //NOI18N
    private static final SpecificationVersion OLD_JDK9 = new SpecificationVersion("1.9");   //NOI18N
    public static final SpecificationVersion JDK9 = new SpecificationVersion("9");     //NOI18N

    private static final Logger LOG = Logger.getLogger(Util.class.getName());
    //Properties used by IDE which should be fixed not to use resolved symlink
    private static final Set<String> propertiesToFix;
    static {
        final Set<String> p = new HashSet<>();
        p.add ("sun.boot.class.path");    //NOI18N
        p.add ("sun.boot.library.path");  //NOI18N
        p.add ("java.library.path");      //NOI18N
        p.add ("java.ext.dirs");          //NOI18N
        p.add ("java.home");              //NOI18N
        p.add ("java.endorsed.dirs");     //NOI18N
        propertiesToFix = Collections.unmodifiableSet(p);
    }
    private static final List<Function<FileObject,FileObject>> TOOLS_VARIANTS;
    static {
        final List<Function<FileObject,FileObject>> l = new ArrayList<>();
        jdk: l.add((installFolder) -> installFolder.getFileObject("bin"));       //NOI18N
        graalvm: l.add((installFolder) -> installFolder.getFileObject("jdk/bin"));   //NOI18N
        TOOLS_VARIANTS = Collections.unmodifiableList(l);
    }

    private Util () {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @NonNull
    static ClassPath createClassPath(@NonNull final String classpath) {
        Parameters.notNull("classpath", classpath);
        List<PathResourceImplementation> list = new ArrayList<>();
        addPath(classpath, list);
        return ClassPathSupport.createClassPath(list);
    }

    @CheckForNull
    static ClassPath createModulePath(@NonNull final Collection<FileObject> installFolders) {
        final List<PathResourceImplementation> modules = new ArrayList<>();
        for (FileObject installFolder : installFolders) {
            final File installDir = FileUtil.toFile(installFolder);
            final URI imageURI = installDir == null ?
                    null :
                    NBJRTUtil.getImageURI(installDir);
            if (imageURI != null) {
                try {
                    final FileObject root = getModulesRoot(URLMapper.findFileObject(imageURI.toURL()));
                    for (FileObject module : root.getChildren()) {
                        modules.add(ClassPathSupport.createResource(module.toURL()));
                    }
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                }
                break;
            } else {
                final FileObject modulesFolder = installFolder.getFileObject(MODULES_FOLDER);
                if (modulesFolder != null) {
                    Arrays.stream(modulesFolder.getChildren())
                            .filter((fo) -> fo.isFolder() && fo.getFileObject(MODULE_INFO) != null)
                            .map((fo) -> ClassPathSupport.createResource(fo.toURL()))
                            .forEach(modules::add);
                }
            }
        }
        return modules.isEmpty() ?
                null :
                ClassPathSupport.createClassPath(modules);
    }

    // XXX this method could probably be removed... use standard FileUtil stuff
    @CheckForNull
    static URL getRootURL  (@NonNull final File f) {
        try {
            URL url = Utilities.toURI(f).toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot (url);
            } else if (!f.exists()) {
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL (surl+"/");
                }
            } else if (f.isFile()) {
                //Slow but it will be called only in very rare cases:
                //file on the classpath for which isArchiveFile returned false
                try {
                    ZipFile z = new ZipFile (f);
                    z.close();
                    url = FileUtil.getArchiveRoot (url);
                } catch (IOException e) {
                    url = null;
                }
            }
            return url;
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }


    /**
     * Returns normalized name from display name.
     * The normalized name should be used in the Ant properties and external files.
     * @param displayName
     * @return String
     */
    public static String normalizeName (String displayName) {
        final StringBuilder normalizedName = new StringBuilder ();
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c =='-' || c =='.') {  //NOI18N
                normalizedName.append(c);
            }
            else {
                normalizedName.append('_');
            }
        }
        return normalizedName.toString();
    }

    /**
     * Returns specification version of the given platform.
     *
     * @return instance of SpecificationVersion representing the version; never null
     */
    public static SpecificationVersion getSpecificationVersion(JavaPlatform plat) {
         String version = plat.getSystemProperties().get("java.specification.version");   // NOI18N
         if (version == null) {
             version = "1.1";
         }
         return fixJDK9SpecVersion(makeSpec(version));
    }


    public static FileObject findTool (String toolName, Collection<FileObject> installFolders) {
        return findTool (toolName, installFolders, null);
    }

    public static FileObject findTool (String toolName, Collection<FileObject> installFolders, String archFolderName) {
        assert toolName != null;
        return installFolders.stream()
                .flatMap((f) -> TOOLS_VARIANTS.stream().map((p) -> p.apply(f)))
                .filter((f) -> f != null)
                .map((f) -> archFolderName != null ? f.getFileObject(archFolderName) : f)
                .filter((f) -> f != null)
                .map((f) -> f.getFileObject(toolName, Utilities.isWindows() ? "exe" : null))    //NOI18N
                .filter((f) -> f != null)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get JRE extension JARs/ZIPs.
     * @param extPath a native-format path for e.g. jre/lib/ext
     * @return a native-format classpath for extension JARs and ZIPs found in it
     */
    public static String getExtensions (String extPath) {
        if (extPath == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        final StringTokenizer tk = new StringTokenizer (extPath, File.pathSeparator);
        while (tk.hasMoreTokens()) {
            File extFolder = FileUtil.normalizeFile(new File(tk.nextToken()));
            File[] files = extFolder.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (!f.exists()) {
                        //May happen, eg. broken link, it is safe to ignore it
                        //since it is an extension directory, but log it.
                        LOG.log(
                                Level.WARNING,
                                NbBundle.getMessage(Util.class,"MSG_BrokenExtension"),
                                new Object[] {
                                    f.getName(),
                                    extFolder.getAbsolutePath()
                                });
                        continue;
                    }
                    if (Utilities.isMac() && "._.DS_Store".equals(f.getName())) {  //NOI18N
                        //Ignore Apple temporary ._.DS_Store files in the lib/ext folder
                        continue;
                    }
                    FileObject fo = FileUtil.toFileObject(f);
                    if (fo == null) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot create FileObject for file: {0} exists: {1}", //NOI18N
                                new Object[]{
                                    f.getAbsolutePath(),
                                    f.exists()
                                });
                        continue;
                    }
                    if (!FileUtil.isArchiveFile(fo)) {
                        // #42961: Mac OS X has e.g. libmlib_jai.jnilib.
                        continue;
                    }
                    sb.append(File.pathSeparator);
                    sb.append(files[i].getAbsolutePath());
                }
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.substring(File.pathSeparator.length());
    }

    /**
     * Tests if the {@link URL} represents a remote target.
     * @param url to test
     * @return true is the {@link URL} is remote
     */
    public static boolean isRemote(@NonNull final URL url) {
        Parameters.notNull("url", url); //NOI18N
        return isRemoteProtocol(url.getProtocol());
    }

    /**
     * Tests if the protocol represents a remote target.
     * @param protocol to test
     * @return true is the protocol is remote
     */
    public static boolean isRemoteProtocol(@NonNull final String protocol) {
        Parameters.notNull("protocol", protocol);   //NOI18N
        return PROTO_HTTP.equals(protocol) || PROTO_HTTPS.equals(protocol);
    }

    /**
     * Fixes system properties like sun.boot.class.path if they contains resolved
     * symbolic link.
     */
    @NullUnknown
    public static String fixSymLinks (
            @NonNull final String key,
            @NullAllowed final String value,
            @NonNull final Collection<? extends FileObject> installFolders) {
        if (value != null && propertiesToFix.contains (key)) {
            try {
                String[] pathElements = value.split(File.pathSeparator);
                boolean changed = false;
                for (FileObject installFolder : installFolders) {
                    final File f = FileUtil.toFile (installFolder);
                    if (f != null) {
                        String path = f.getAbsolutePath();
                        String canonicalPath = f.getCanonicalPath();
                        if (!path.equals(canonicalPath)) {
                            for (int i=0; i<pathElements.length; i++) {
                                if (pathElements[i].startsWith(canonicalPath)) {
                                    pathElements[i] = path + pathElements[i].substring(canonicalPath.length());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
                if (changed) {
                    final StringBuilder sb = new StringBuilder ();
                    for (int i = 0; i<pathElements.length; i++) {
                        if (i > 0) {
                            sb.append(File.pathSeparatorChar);
                        }
                        sb.append(pathElements[i]);
                    }
                    return sb.toString();
                }
            } catch (IOException ioe) {
                //Returns the original value
            }
        }
        return value;
    }

    public static String removeNBArtifacts(
            @NonNull final String key,
            @NullAllowed String value) {
        if (value != null && "java.class.path".equals(key)) {   //NOI18N
            String nbHome = System.getProperty("netbeans.home");    //NOI18N
            if (nbHome != null) {
                if (!nbHome.endsWith(File.separator)) {
                    nbHome = nbHome + File.separatorChar;
                }
                final String[] elements = PropertyUtils.tokenizePath(value);
                final List<String> newElements = new ArrayList<>(elements.length);
                for (String element : elements) {
                    if (!element.startsWith(nbHome)) {
                        newElements.add(element);
                    }
                }
                if (elements.length != newElements.size()) {
                    value = newElements.stream()
                            .collect(Collectors.joining(File.pathSeparator));
                }
            }
        }
        return value;
    }

    @NonNull
    public static Collection<FileObject> toFileObjects(Collection<? extends URL> urls) {
        if (urls.isEmpty()) {
            return Collections.emptySet();
        }
        final Collection<FileObject> result = new ArrayList<> (urls.size());
        for (URL url : urls) {
            final FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                result.add (fo);
            }
        }
        return result;
    }

    @NullUnknown
    public static Map<String,String> filterProbe (
            @NullAllowed final Map<String,String> p,
            @NullAllowed final String probePath) {
        if (p != null) {
            final String val = p.get(J2SEPlatformImpl.SYSPROP_JAVA_CLASS_PATH);
            if (val != null) {
                p.put(J2SEPlatformImpl.SYSPROP_JAVA_CLASS_PATH, filterProbe(val, probePath));
            }
        }
        return p;
    }

    private static String filterProbe (String v, final String probePath) {
        if (v != null) {
            final String[] pes = PropertyUtils.tokenizePath(v);
            final StringBuilder sb = new StringBuilder ();
            for (String pe : pes) {
                if (probePath != null ?  probePath.equals(pe) : (pe != null &&
                pe.endsWith("org-netbeans-modules-java-j2seplatform-probe.jar"))) { //NOI18N
                    //Skeep
                }
                else {
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparatorChar);
                    }
                    sb.append(pe);
                }
            }
            v = sb.toString();
        }
        return v;
    }

    // copy pasted from org.openide.modules.Dependency:
    /** Try to make a specification version from a string.
     * Deal with errors gracefully and try to recover something from it.
     * E.g. "1.4.0beta" is technically erroneous; correct to "1.4.0".
     */
    private static SpecificationVersion makeSpec(String vers) {
        if (vers != null) {
            try {
                return new SpecificationVersion(vers);
            } catch (NumberFormatException nfe) {
                System.err.println("WARNING: invalid specification version: " + vers); // NOI18N
            }
            do {
                vers = vers.substring(0, vers.length() - 1);
                try {
                    return new SpecificationVersion(vers);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            } while (vers.length() > 0);
        }
        // Nothing decent in it at all; use zero.
        return new SpecificationVersion("0"); // NOI18N
    }

    private static SpecificationVersion fixJDK9SpecVersion(@NonNull final SpecificationVersion version) {
        return  OLD_JDK9.equals(version) ?
                JDK9 :
                version;
    }

    @CheckForNull
    private static PathResourceImplementation getJfxRt(@NonNull final Collection<? extends FileObject> installFolders) {
        for (FileObject installFolder : installFolders) {
            final FileObject jfxrt = installFolder.getFileObject(JFXRT_PATH);
            if (jfxrt != null && FileUtil.isArchiveFile(jfxrt)) {
                return ClassPathSupport.createResource(FileUtil.getArchiveRoot(jfxrt.toURL()));
            }
        }
        return null;
    }

    private static void addPath(
            @NullAllowed final String path,
            @NonNull final List<? super PathResourceImplementation> into) {
        if (path != null && !path.isEmpty()) {
            final StringTokenizer tokenizer = new StringTokenizer(path, File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                File f = FileUtil.normalizeFile(new File(item));
                URL url = getRootURL (f);
                if (url!=null) {
                    into.add(ClassPathSupport.createResource(url));
                }
            }
        }
    }

    @NonNull
    private static FileObject getModulesRoot(@NonNull final FileObject jrtRoot) {
        final FileObject modules = jrtRoot.getFileObject("modules");    //NOI18N
        //jimage v1 - modules are located in the root
        //jimage v2 - modules are located in "modules" folder
        return modules == null ?
            jrtRoot :
            modules;
    }
}
