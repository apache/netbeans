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

package org.netbeans.nbbuild;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Converts a set of NetBeans modules into OSGi bundles.
 * Since there are some aspects of the translation that must be sensitive to
 * context in order to preserve as closely as possible the semantics of the
 * NetBeans module system, processing proceeds in two phases:
 * <ol>
 * <li>Each module in the input is opened and scanned for packages which it defines
 *     (including in its {@code Class-Path} extensions), and for packages which it
 *     (statically) refers to (not including packages it itself defines, or any
 *     packages in the {@code java.*} namespace). {@code OpenIDE-Module-Hide-Classpath-Packages}
 *     declarations are also tracked, and a list of packages which seem to be
 *     exported from this module (if any) is collected.
 * <li>Each module in the input is reopened. Now it is actually converted to an
 *     OSGi bundle. Package import and export information from the first phase is
 *     used to decide how to represent this bundle's imports:
 *     <ol>
 *     <li>Packages hidden by this bundle, or one of its direct dependencies, are
 *         never to be represented in the OSGi manifest.
 *     <li>Packages exported by one of this bundle's dependencies can also be skipped,
 *         since {@code Require-Bundle} will pick them all up.
 *     <li>Packages in the {@code org.osgi.*} namespace can be imported.
 *     <li>Packages from the known Java Platform API can be imported.
 *     <li>All other packages can be dynamically imported. Perhaps these will be
 *         available somehow at runtime, perhaps from the system bundle.
 *     </ol>
 * </ol>
 */
public class MakeOSGi extends Task {

    private File destdir;
    private List<ResourceCollection> modules = new ArrayList<>();
    
    /**
     * Mandatory destination directory. Bundles will be created here.
     */
    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }
    
    /**
     * Adds a set of module JARs.
     * It is permitted for them to be JARs anywhere on disk,
     * but it is best if they are in a cluster structure
     * with ../update_tracking/*.xml present
     * so that associated files can be included in the bundle.
     */
    public void add(ResourceCollection modules) {
        this.modules.add(modules);
    }

    static class Info {
        final Set<String> importedPackages = new TreeSet<>();
        final Set<String> exportedPackages = new TreeSet<>();
        final Set<String> hiddenPackages = new TreeSet<>();
        final Set<String> hiddenSubpackages = new TreeSet<>();
    }

    public @Override void execute() throws BuildException {
        if (destdir == null) {
            throw new BuildException("missing destdir");
        }
        List<File> jars = new ArrayList<>();
        List<File> fragments = new ArrayList<>();
        Map<String,Info> infos = new HashMap<>();
        log("Prescanning JARs...");
        for (ResourceCollection rc : modules) {
            Iterator<?> it = rc.iterator();
            while (it.hasNext()) {
                File jar = ((FileResource) it.next()).getFile();
                log("Prescanning " + jar, Project.MSG_VERBOSE);
                if (jar.getParentFile().getName().equals("locale")) {
                    fragments.add(jar);
                    continue;
                }
                try {
                    try (JarFile jf = new JarFile(jar)) {
                        Info info = new Info();
                        String cnb = prescan(jf, info, this);
                        if (cnb == null) {
                            log(jar + " does not appear to be either a module or a bundle; skipping", Project.MSG_WARN);
                        } else if (SKIPPED_PSEUDO_MODULES.contains(cnb)) {
                            log("Skipping " + jar);
                        } else if (infos.containsKey(cnb)) {
                            log(jar + " appears to not be the only module named " + cnb, Project.MSG_WARN);
                        } else {
                            infos.put(cnb, info);
                            jars.add(jar);
                        }
                    }
                } catch (Exception x) {
                    throw new BuildException("Could not prescan " + jar + ": " + x, x, getLocation());
                }
            }
        }
        for (File jar : jars) {
            try {
                process(jar, infos);
            } catch (Exception x) {
                throw new BuildException("Could not process " + jar + ": " + x, x, getLocation());
            }
        }
        for (File jar : fragments) {
            try {
                processFragment(jar);
            } catch (Exception x) {
                throw new BuildException("Could not process " + jar + ": " + x, x, getLocation());
            }
        }
    }

    static String prescan(JarFile module, Info info, Task task) throws Exception {
        Manifest manifest = module.getManifest();
        if (manifest == null) {
            return null;
        }
        Attributes attr = manifest.getMainAttributes();
        String cnb = attr.getValue("OpenIDE-Module");
        if ("org.netbeans.libs.osgi".equals(cnb)) {
            // Otherwise get e.g. CCE: org.netbeans.core.osgi.Activator cannot be cast to org.osgi.framework.BundleActivator
            return cnb;
        }
        Set<String> availablePackages = new TreeSet<>();
        scanClasses(module, info.importedPackages, availablePackages, task);
        File antlib = new File(module.getName().replaceFirst("([/\\\\])modules([/\\\\][^/\\\\]+)", "$1ant$1nblib$2"));
        if (antlib.isFile()) {
            // ant/nblib/org-netbeans-modules-debugger-jpda-ant.jar references com.sun.jdi.* packages.
            // AntBridge.MainClassLoader.findClass will refuse to load these,
            // since it is expected that the module loader, thus also AuxClassLoader, can load them.
            // So we need to DynamicImport-Package these packages so that will be true.
            Set<String> antlibPackages = new HashSet<>();
            try (JarFile antlibJF = new JarFile(antlib)) {
                scanClasses(antlibJF, antlibPackages, new HashSet<>(), task);
            }
            for (String antlibImport : antlibPackages) {
                if (!antlibImport.startsWith("org.apache.tools.") && !availablePackages.contains(antlibImport)) {
                    info.importedPackages.add(antlibImport);
                }
            }
        }
        if (cnb != null) {
            cnb = cnb.replaceFirst("/\\d+$", "");
            String hide = attr.getValue("OpenIDE-Module-Hide-Classpath-Packages");
            if (hide != null) {
                for (String piece : hide.split("[, ]+")) {
                    if (piece.isEmpty()) {
                        continue;
                    }
                    if (piece.endsWith(".*")) {
                        info.hiddenPackages.add(piece.substring(0, piece.length() - ".*".length()));
                    } else if (piece.endsWith(".**")) {
                        info.hiddenSubpackages.add(piece.substring(0, piece.length() - ".**".length()));
                    } else {
                        throw new IOException("Bad OpenIDE-Module-Hide-Classpath-Packages piece: " + piece);
                    }
                }
            }
            String pp = attr.getValue("OpenIDE-Module-Public-Packages");
            String implVersion = attr.getValue("OpenIDE-Module-Implementation-Version");
            if (implVersion != null && implVersion.matches("\\d+") &&
                    /* not just e.g. 201005242201 */ attr.getValue("OpenIDE-Module-Build-Version") != null) {
                // Since we have no idea who might be using these packages, have to make everything public.
                info.exportedPackages.addAll(availablePackages);
                pp = null;
            }
            if (pp != null && !pp.equals("-")) {
                for (String p : pp.split("[, ]+")) {
                    if (p.isEmpty()) {
                        continue;
                    }
                    if (p.endsWith(".*")) {
                        info.exportedPackages.add(p.substring(0, p.length() - ".*".length()));
                    } else {
                        if (!p.endsWith(".**")) {
                            throw new IllegalArgumentException("Invalid package export: " + p);
                        }
                        for (String actual : availablePackages) {
                            if (actual.equals(p.substring(0, p.length() - ".**".length()))
                                    || actual.startsWith(p.substring(0, p.length() - "**".length()))) {
                                info.exportedPackages.add(actual);
                            }
                        }
                    }
                }
            }
        } else { // #180201
            cnb = JarWithModuleAttributes.extractCodeName(attr);
            if (cnb == null) {
                return null;
            }
            String exportPackage = attr.getValue("Export-Package");
            if (exportPackage != null) {
                // http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
                for (String piece : exportPackage.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")) {
                    info.exportedPackages.add(piece.trim().replaceFirst(";.+", ""));
                }
            }
        }
        return cnb;
    }

    private File findDestFile(String bundleName, String bundleVersion) throws IOException {
        File destFile = new File(destdir, bundleName + (bundleVersion != null ? "-" + bundleVersion : "") + ".jar");
        for (File stale : destdir.listFiles()) {
            if (stale.getName().matches("\\Q" + bundleName + "\\E(-.+)?[.]jar") && !stale.equals(destFile)) {
                log("Deleting copy under old name: " + stale);
                if (!stale.delete()) {
                    throw new IOException("Could not delete: " + stale);
                }
            }
        }
        return destFile;
    }

    private void process(File module, Map<String,Info> infos) throws Exception {
        try (JarFile jar = new JarFile(module)) {
            Manifest netbeans = jar.getManifest();
            Attributes netbeansAttr = netbeans.getMainAttributes();
            if (netbeansAttr.getValue("Bundle-SymbolicName") != null) { // #180201
                File bundleFile = findDestFile(JarWithModuleAttributes.extractCodeName(netbeansAttr), netbeansAttr.getValue("Bundle-Version"));
                if (bundleFile.lastModified() > module.lastModified()) {
                    log("Skipping " + module + " since " + bundleFile + " is newer", Project.MSG_VERBOSE);
                    return;
                }
                Copy copy = new Copy();
                copy.setProject(getProject());
                copy.setOwningTarget(getOwningTarget());
                copy.setFile(module);
                copy.setTofile(bundleFile);
                copy.setVerbose(true);
                copy.execute();
                return;
            }
            Manifest osgi = new Manifest();
            Attributes osgiAttr = osgi.getMainAttributes();
            translate(netbeansAttr, osgiAttr, infos);
            String cnb = osgiAttr.getValue("Bundle-SymbolicName").replaceFirst(";.+", "");
            File bundleFile = findDestFile(cnb, osgiAttr.getValue("Bundle-Version"));
            if (bundleFile.lastModified() > module.lastModified()) {
                log("Skipping " + module + " since " + bundleFile + " is newer", Project.MSG_VERBOSE);
                return;
            }
            log("Processing " + module + " into " + bundleFile);
            String dynamicImports = osgiAttr.getValue("DynamicImport-Package");
            if (dynamicImports != null) {
                log(cnb + " has imports of no known origin: " + dynamicImports, Project.MSG_WARN);
                log("(you may need to define org.osgi.framework.system.packages.extra in your OSGi container)");
            }
            Properties localizedStrings = new Properties();
            String locbundle = netbeansAttr.getValue("OpenIDE-Module-Localizing-Bundle");
            if (locbundle != null) {
                try (InputStream is = jar.getInputStream(jar.getEntry(locbundle))) {
                    localizedStrings.load(is);
                }
                osgiAttr.putValue("Bundle-Localization", locbundle.replaceFirst("[.]properties$", ""));
            }
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Name", "Bundle-Name");
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Display-Category", "Bundle-Category");
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Short-Description", "Bundle-Description");
            Map<String,File> bundledFiles = findBundledFiles(module, cnb);
            // XXX any use for OpenIDE-Module-Long-Description?
            String classPath = netbeansAttr.getValue("Class-Path");
            if (classPath != null) {
                StringBuilder bundleCP = new StringBuilder();
                for (String rawEntry : classPath.split("[, ]+")) {
                    String entry = URLDecoder.decode(rawEntry, "UTF-8");
                    if (entry.startsWith("${java.home}")) {
                        continue;
                    }
                    String clusterPath = new URI(module.getParentFile().getName() + "/" + entry).normalize().toString();
                    if (bundledFiles.containsKey(clusterPath)) {
                        bundleCP.append("/OSGI-INF/files/").append(clusterPath).append(",");
                    } else {
                        log("Class-Path entry " + entry + " from " + module + " does not correspond to any apparent cluster file", Project.MSG_WARN);
                    }
                }
                osgiAttr.putValue("Bundle-Classpath", bundleCP + ".");
            }
            StringBuilder execFiles = null;
            for (Map.Entry<String,File> bundledFile : bundledFiles.entrySet()) {
                // XXX would be better to use ${nbm.executable.files} as specified by project
                // (since building bundles on Windows will never set this even if running on Unix)
                // but this information is available only during the project's build or in its NBM
                if (bundledFile.getValue().canExecute()) {
                    String name = bundledFile.getKey();
                    if (execFiles == null) {
                        execFiles = new StringBuilder(name);
                    } else {
                        execFiles.append(',').append(name);
                    }
                }
            }
            if (execFiles != null) {
                osgiAttr.putValue("NetBeans-Executable-Files", execFiles.toString());
            }
            // XXX modules/lib/*.dll/so => Bundle-NativeCode (but syntax is rather complex)
            try (OutputStream bundle = new FileOutputStream(bundleFile);
                    ZipOutputStream zos = new JarOutputStream(bundle, osgi)) {
                Set<String> parents = new HashSet<>();
                Enumeration<? extends ZipEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String path = entry.getName();
                    if (path.endsWith("/") || path.equals("META-INF/MANIFEST.MF")) {
                        continue;
                    }
                    try (InputStream is = jar.getInputStream(entry)) {
                        writeEntry(zos, path, is, parents);
                    }
                }
                for (Map.Entry<String,File> bundledFile : bundledFiles.entrySet()) {
                    try (InputStream is = new FileInputStream(bundledFile.getValue())) {
                        writeEntry(zos, "OSGI-INF/files/" + bundledFile.getKey(), is, parents);
                    }
                }
                zos.finish();
            }
        }
    }

    /**
     * Translate NetBeans module metadata to OSGi equivalents.
     * @param netbeans manifest header to be read
     * @param osgi manifest header to be written
     * @param infos information about imported and exported packages among all processed JARs
     */
    void translate(Attributes netbeans, Attributes osgi, Map<String,Info> infos) throws Exception {
        osgi.putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        osgi.putValue("Bundle-ManifestVersion", "2");
        String codename = netbeans.getValue("OpenIDE-Module");
        if (codename == null) {
            throw new IllegalArgumentException("Does not appear to be a NetBeans module");
        }
        String cnb = codename.replaceFirst("/\\d+$", "");
        osgi.putValue("Bundle-SymbolicName", cnb);
        if (cnb.equals("org.netbeans.core.osgi")) {
            osgi.putValue("Bundle-Activator", "org.netbeans.core.osgi.Activator");
        }
        Info myInfo = infos.get(cnb);
        String spec = netbeans.getValue("OpenIDE-Module-Specification-Version");
        String bundleVersion = null;
        if (spec != null) {
            bundleVersion = threeDotsWithMajor(spec, codename);
            String buildVersion = netbeans.getValue("OpenIDE-Module-Build-Version");
            if (buildVersion == null) {
                buildVersion = netbeans.getValue("OpenIDE-Module-Implementation-Version");
            }
            if (buildVersion != null) {
                bundleVersion += "." + buildVersion.replaceAll("[^a-zA-Z0-9_-]", "_");
            }
            osgi.putValue("Bundle-Version", bundleVersion);
        }
        // OpenIDE-Module-Friends is ignored since OSGi has no apparent equivalent
        // (could use mandatory export constraints but friends would then
        // need to use Import-Package to access, rather than Require-Bundle,
        // which would require knowing which packages are being imported by that dep)
        if (!myInfo.exportedPackages.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (String p : myInfo.exportedPackages) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(p);
            }
            osgi.putValue("Export-Package", b.toString());
        }
        for (String attrToCopy : new String[] {"OpenIDE-Module-Layer", "OpenIDE-Module-Install"}) {
            String val = netbeans.getValue(attrToCopy);
            if (val != null) {
                osgi.putValue(attrToCopy, val);
            }
        }
        StringBuilder requireBundles = new StringBuilder();
        if (!STARTUP_PSEUDO_MODULES.contains(cnb)) {
            // do not need to import any API, just need it to be started:
            requireBundles.append("org.netbeans.core.osgi");
        }
        Set<String> imports = new TreeSet<>(myInfo.importedPackages);
        hideImports(imports, myInfo);
        String dependencies = netbeans.getValue("OpenIDE-Module-Module-Dependencies");
        if (dependencies != null) {
            for (String dependency : dependencies.split(" *, *")) {
                String depCnb = translateDependency(requireBundles, dependency);
                if (depCnb == null) {
                    continue;
                }
                Info imported = infos.get(depCnb);
                if (imported != null) {
                    imports.removeAll(imported.exportedPackages);
                    hideImports(imports, imported);
                } else {
                    log("dependency " + depCnb + " of " + cnb + " not found in batch; imports may not be correct", Project.MSG_WARN);
                }
            }
        }
        if (requireBundles.length() > 0) {
            osgi.putValue("Require-Bundle", requireBundles.toString());
        }
        StringBuilder staticImports = new StringBuilder();
        StringBuilder dynamicImports = new StringBuilder();
        for (String pkg : imports) {
            StringBuilder b = isOSGiOrJavaPlatform(pkg) ? staticImports : dynamicImports;
            // JRE-specific dependencies will not be exported by Felix by default.
            // But Felix can be configured to offer any desired packages from the framework.
            // Do not use DynamicImport-Package where not really needed, as it can lead to deadlocks in Felix:
            // ModuleImpl.findClassOrResourceByDelegation -> Felix.acquireGlobalLock
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(pkg);
        }
        if (staticImports.length() > 0) {
            osgi.putValue("Import-Package", staticImports.toString());
        }
        if (dynamicImports.length() > 0) {
            osgi.putValue("DynamicImport-Package", dynamicImports.toString());
        }
        // ignore OpenIDE-Module-Package-Dependencies; rarely used, and bytecode analysis is probably more accurate anyway
        String javaDeps = netbeans.getValue("OpenIDE-Module-Java-Dependencies");
        if (javaDeps != null) {
            Matcher m = Pattern.compile("Java > (1.[6-9])").matcher(javaDeps); // 1.5 is not supported anyway
            if (m.matches()) {
                osgi.putValue("Bundle-RequiredExecutionEnvironment", "JavaSE-" + m.group(1));
            }
        }
        for (String tokenAttr : new String[] {"OpenIDE-Module-Provides", "OpenIDE-Module-Needs"}) {
            String v = netbeans.getValue(tokenAttr);
            if (v != null) {
                osgi.putValue(tokenAttr, v);
            }
        }
        String v = netbeans.getValue("OpenIDE-Module-Requires");
        if (v != null) {
            StringBuilder b = null;
            for (String tok : v.split("[, ]+")) {
                if (!tok.matches("org.openide.modules.ModuleFormat\\d+")) {
                    if (b == null) {
                        b = new StringBuilder(tok);
                    } else {
                        b.append(", ").append(tok);
                    }
                }
            }
            if (b != null) {
                osgi.putValue("OpenIDE-Module-Requires", b.toString());
            }
        }
        // autoload, eager status are ignored since OSGi has no apparent equivalent
    }

    private boolean isOSGiOrJavaPlatform(String pkg) {
        if (pkg.startsWith("org.osgi.")) {
            return true;
        }
        return JAVA_PLATFORM_PACKAGES.contains(pkg);
    }

    private static void hideImports(Set<String> imports, Info info) {
        imports.removeAll(info.hiddenPackages);
        Iterator<String> it = imports.iterator();
        while (it.hasNext()) {
            String p = it.next();
            for (String prefix : info.hiddenSubpackages) {
                if (p.equals(prefix) || p.startsWith(prefix + ".")) {
                    it.remove();
                    break;
                }
            }
        }
    }

    private static void writeEntry(ZipOutputStream zos, String path, InputStream data, Set<String> parents) throws IOException {
        int size = Math.max(data.available(), 100);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = data.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        writeEntry(zos, path, baos.toByteArray(), parents);
    }
    private static void writeEntry(ZipOutputStream zos, String path, byte[] data, Set<String> parents) throws IOException {
        assert path.length() > 0 && !path.endsWith("/") && !path.startsWith("/") && path.indexOf("//") == -1 : path;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                String parent = path.substring(0, i + 1);
                if (parents.add(parent)) {
                    ZipEntry ze = new ZipEntry(parent);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(0);
                    ze.setCrc(0);
                    zos.putNextEntry(ze);
                    zos.closeEntry();
                }
            }
        }
        ZipEntry ze = new ZipEntry(path);
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        ze.setCrc(crc.getValue());
        zos.putNextEntry(ze);
        zos.write(data, 0, data.length);
        zos.closeEntry();
    }

    // copied from NetigsoModuleFactory
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

    static String translateDependency(StringBuilder b, String dependency) throws IllegalArgumentException {
        Matcher m = Pattern.compile("([^/ >=]+)(?:/(\\d+)(?:-(\\d+))?)? *(?:(=|>) *(.+))?").matcher(dependency);
        if (!m.matches()) {
            throw new IllegalArgumentException("bad dep: " + dependency);
        }
        String depCnb = m.group(1);
        if (SKIPPED_PSEUDO_MODULES.contains(depCnb)) {
            return null;
        }
        String depMajLo = m.group(2);
        String depMajHi = m.group(3);
        String comparison = m.group(4);
        String version = m.group(5);
        if (b.length() > 0) {
            b.append(", ");
        }
        b.append(depCnb);
        if (!"=".equals(comparison)) {
            if (version == null) {
                version = "0";
            }
            String targetVersion = threeDotsWithMajor(version, depMajLo == null ? "" : "x/" + depMajLo);
            b.append(";bundle-version=\"[").append(targetVersion).append(",");
            b.append(100 * ((depMajHi != null ? Integer.parseInt(depMajHi) : depMajLo != null ? Integer.parseInt(depMajLo) : 0) + 1));
            b.append(")\"");
        }
        return depCnb;
    }

    private void handleDisplayAttribute(Properties props, Attributes netbeans, Attributes osgi, String netbeansHeader, String osgiHeader) throws IOException {
        String val = netbeans.getValue(netbeansHeader);
        if (val != null) {
            osgi.putValue(osgiHeader, val);
        } else if (props.containsKey(netbeansHeader)) {
            osgi.putValue(osgiHeader, "%" + netbeansHeader);
        }
    }

    private Map<String,File> findBundledFiles(File module, String cnb) throws Exception {
        Map<String,File> result = new HashMap<>();
        if (module.getParentFile().getName().matches("modules|core|lib")) {
            File cluster = module.getParentFile().getParentFile();
            File updateTracking = new File(new File(cluster, "update_tracking"), cnb.replace('.', '-') + ".xml");
            if (updateTracking.isFile()) {
                Document doc = XMLUtil.parse(new InputSource(updateTracking.toURI().toString()), false, false, null, null);
                NodeList nl = doc.getElementsByTagName("file");
                for (int i = 0; i < nl.getLength(); i++) {
                    String path = ((Element) nl.item(i)).getAttribute("name");
                    if (path.matches("config/(Modules|ModuleAutoDeps)/.+[.]xml|lib/nbexec.*")) {
                        continue;
                    }
                    File f = new File(cluster, path);
                    if (f.equals(module)) {
                        continue;
                    }
                    if (f.isFile()) {
                        result.put(path, f);
                    } else {
                        log("did not find " + f + " specified in " + updateTracking, Project.MSG_WARN);
                    }
                }
            } else {
                log("did not find expected " + updateTracking, Project.MSG_WARN);
            }
        } else {
            log("JAR " + module + " not found in expected cluster layout", Project.MSG_WARN);
        }
        return result;
    }

    private static void scanClasses(JarFile module, Set<String> importedPackages, Set<String> availablePackages, Task task) throws Exception {
        Map<String, byte[]> classfiles = new TreeMap<>();
        VerifyClassLinkage.read(module, classfiles, new HashSet<>(Collections.singleton(new File(module.getName()))), task, null);
        for (Map.Entry<String,byte[]> entry : classfiles.entrySet()) {
            String available = entry.getKey();
            int idx = available.lastIndexOf('.');
            if (idx != -1) {
                availablePackages.add(available.substring(0, idx));
            }
            for (String clazz : VerifyClassLinkage.dependencies(entry.getValue())) {
                if (classfiles.containsKey(clazz)) {
                    // Part of the same module; probably not an external import.
                    continue;
                }
                if (clazz.startsWith("java.")) {
                    // No need to declare as an import.
                    continue;
                }
                idx = clazz.lastIndexOf('.');
                if (idx != -1) {
                    importedPackages.add(clazz.substring(0, idx));
                }
            }
        }
    }

    private void processFragment(File fragment) throws Exception {
        String cnb = findFragmentHost(fragment);
        File bundleFile = new File(destdir, fragment.getName());
        if (bundleFile.lastModified() > fragment.lastModified()) {
            log("Skipping " + fragment + " since " + bundleFile + " is newer", Project.MSG_VERBOSE);
            return;
        }
        log("Processing " + fragment + " into " + bundleFile);
        Manifest mf = new Manifest();
        Attributes attr = mf.getMainAttributes();
        attr.putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        attr.putValue("Bundle-ManifestVersion", "2");
        attr.putValue("Bundle-SymbolicName", cnb + "-branding");
        attr.putValue("Fragment-Host", cnb);
        try (JarFile jar = new JarFile(fragment);
                OutputStream bundle = new FileOutputStream(bundleFile);
                ZipOutputStream zos = new JarOutputStream(bundle, mf)) {
            Set<String> parents = new HashSet<>();
            Enumeration<? extends ZipEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = entry.getName();
                if (path.endsWith("/") || path.equals("META-INF/MANIFEST.MF")) {
                    continue;
                }   try (InputStream is = jar.getInputStream(entry)) {
                    writeEntry(zos, path, is, parents);
                }
            }
            zos.finish();
        }
    }

    static String findFragmentHost(File jar) {
        String cnb = jar.getName().replaceFirst("(_[a-z][a-z0-9]*)*[.]jar$", "").replace('-', '.');
        // Historical naming patterns:
        if (cnb.equals("core") && jar.getParentFile().getParentFile().getName().equals("core")) {
            return "org.netbeans.core.startup";
        } else if (cnb.equals("boot") && jar.getParentFile().getParentFile().getName().equals("lib")) {
            return "org.netbeans.bootstrap";
        } else {
            return cnb;
        }
    }

    /**
     * List of packages guaranteed to be in the Java platform.
     * Taken from Felix's default.properties@1347814#jre-1.6 but same as JDK 6's package-list.
     * FELIX-2572 updated this to not include some packages actually in the JRE
     * (rt.jar and src.zip); can cause problems in some cases (cf. bug #210325 comment #11).
     */
    private static final Set<String> JAVA_PLATFORM_PACKAGES = new TreeSet<String>(Arrays.asList(
        "javax.accessibility",
        "javax.activation",
        "javax.activity",
        "javax.annotation",
        "javax.annotation.processing",
        "javax.crypto",
        "javax.crypto.interfaces",
        "javax.crypto.spec",
        "javax.imageio",
        "javax.imageio.event",
        "javax.imageio.metadata",
        "javax.imageio.plugins.bmp",
        "javax.imageio.plugins.jpeg",
        "javax.imageio.spi",
        "javax.imageio.stream",
        "javax.jws",
        "javax.jws.soap",
        "javax.lang.model",
        "javax.lang.model.element",
        "javax.lang.model.type",
        "javax.lang.model.util",
        "javax.management",
        "javax.management.loading",
        "javax.management.modelmbean",
        "javax.management.monitor",
        "javax.management.openmbean",
        "javax.management.relation",
        "javax.management.remote",
        "javax.management.remote.rmi",
        "javax.management.timer",
        "javax.naming",
        "javax.naming.directory",
        "javax.naming.event",
        "javax.naming.ldap",
        "javax.naming.spi",
        "javax.net",
        "javax.net.ssl",
        "javax.print",
        "javax.print.attribute",
        "javax.print.attribute.standard",
        "javax.print.event",
        "javax.rmi",
        "javax.rmi.CORBA",
        "javax.rmi.ssl",
        "javax.script",
        "javax.security.auth",
        "javax.security.auth.callback",
        "javax.security.auth.kerberos",
        "javax.security.auth.login",
        "javax.security.auth.spi",
        "javax.security.auth.x500",
        "javax.security.cert",
        "javax.security.sasl",
        "javax.sound.midi",
        "javax.sound.midi.spi",
        "javax.sound.sampled",
        "javax.sound.sampled.spi",
        "javax.sql",
        "javax.sql.rowset",
        "javax.sql.rowset.serial",
        "javax.sql.rowset.spi",
        "javax.swing",
        "javax.swing.border",
        "javax.swing.colorchooser",
        "javax.swing.event",
        "javax.swing.filechooser",
        "javax.swing.plaf",
        "javax.swing.plaf.basic",
        "javax.swing.plaf.metal",
        "javax.swing.plaf.multi",
        "javax.swing.plaf.synth",
        "javax.swing.table",
        "javax.swing.text",
        "javax.swing.text.html",
        "javax.swing.text.html.parser",
        "javax.swing.text.rtf",
        "javax.swing.tree",
        "javax.swing.undo",
        "javax.tools",
        "javax.transaction",
        "javax.transaction.xa",
        "javax.xml",
        "javax.xml.bind",
        "javax.xml.bind.annotation",
        "javax.xml.bind.annotation.adapters",
        "javax.xml.bind.attachment",
        "javax.xml.bind.helpers",
        "javax.xml.bind.util",
        "javax.xml.crypto",
        "javax.xml.crypto.dom",
        "javax.xml.crypto.dsig",
        "javax.xml.crypto.dsig.dom",
        "javax.xml.crypto.dsig.keyinfo",
        "javax.xml.crypto.dsig.spec",
        "javax.xml.datatype",
        "javax.xml.namespace",
        "javax.xml.parsers",
        "javax.xml.soap",
        "javax.xml.stream",
        "javax.xml.stream.events",
        "javax.xml.stream.util",
        "javax.xml.transform",
        "javax.xml.transform.dom",
        "javax.xml.transform.sax",
        "javax.xml.transform.stax",
        "javax.xml.transform.stream",
        "javax.xml.validation",
        "javax.xml.ws",
        "javax.xml.ws.handler",
        "javax.xml.ws.handler.soap",
        "javax.xml.ws.http",
        "javax.xml.ws.soap",
        "javax.xml.ws.spi",
        "javax.xml.ws.wsaddressing",
        "javax.xml.xpath",
        "org.ietf.jgss",
        "org.omg.CORBA",
        "org.omg.CORBA.DynAnyPackage",
        "org.omg.CORBA.ORBPackage",
        "org.omg.CORBA.TypeCodePackage",
        "org.omg.CORBA.portable",
        "org.omg.CORBA_2_3",
        "org.omg.CORBA_2_3.portable",
        "org.omg.CosNaming",
        "org.omg.CosNaming.NamingContextExtPackage",
        "org.omg.CosNaming.NamingContextPackage",
        "org.omg.Dynamic",
        "org.omg.DynamicAny",
        "org.omg.DynamicAny.DynAnyFactoryPackage",
        "org.omg.DynamicAny.DynAnyPackage",
        "org.omg.IOP",
        "org.omg.IOP.CodecFactoryPackage",
        "org.omg.IOP.CodecPackage",
        "org.omg.Messaging",
        "org.omg.PortableInterceptor",
        "org.omg.PortableInterceptor.ORBInitInfoPackage",
        "org.omg.PortableServer",
        "org.omg.PortableServer.CurrentPackage",
        "org.omg.PortableServer.POAManagerPackage",
        "org.omg.PortableServer.POAPackage",
        "org.omg.PortableServer.ServantLocatorPackage",
        "org.omg.PortableServer.portable",
        "org.omg.SendingContext",
        "org.omg.stub.java.rmi",
        "org.w3c.dom",
        "org.w3c.dom.bootstrap",
        "org.w3c.dom.events",
        "org.w3c.dom.ls",
        "org.xml.sax",
        "org.xml.sax.ext",
        "org.xml.sax.helpers"
    ));

    private static final Set<String> SKIPPED_PSEUDO_MODULES = new HashSet<String>(Arrays.asList(
            "org.eclipse.osgi",
            "org.netbeans.core.netigso",
            "org.netbeans.modules.netbinox",
            "org.netbeans.libs.osgi",
            "org.netbeans.libs.felix"
    ));

    private static final Set<String> STARTUP_PSEUDO_MODULES = new HashSet<String>(Arrays.asList(
            "org.openide.util.lookup",
            "org.openide.util.ui",
            "org.openide.util",
            "org.openide.modules",
            "org.netbeans.bootstrap",
            "org.openide.filesystems",
            "org.openide.filesystems.compat8",
            "org.netbeans.core.startup",
            "org.netbeans.core.startup.base",
            "org.netbeans.core.osgi",
            "org.eclipse.osgi"
    ));

}
