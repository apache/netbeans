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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Scans for known modules.
 * Precise algorithm summarized in issue #42681 and issue #58966.
 * @author Jesse Glick
 */
final class ModuleListParser {

    private static Map<File,Map<String,Entry>> SOURCE_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    private static Map<File,Map<String,Entry>> SUITE_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    private static Map<File,Entry> STANDALONE_SCAN_CACHE = new HashMap<File,Entry>();
    private static Map<File,Map<String,Entry>> BINARY_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    
    /** Clear caches. Cf. #71130. */
    public static void resetCaches() {
        SOURCE_SCAN_CACHE.clear();
        SUITE_SCAN_CACHE.clear();
        STANDALONE_SCAN_CACHE.clear();
        BINARY_SCAN_CACHE.clear();
    }
    
    /** Synch with org.netbeans.modules.apisupport.project.universe.ModuleList.FOREST: */
    private static final String[] FOREST = {
        /*root*/null,
        "contrib",
        "otherlicenses",
        // do not scan in misc; any real modules would have been put in contrib
        // Will there be other subtrees in the future (not using suites)?
    };
    /**
     * Find all NBM projects in a root, possibly from cache.
     */
    private static Map<String,Entry> scanNetBeansOrgSources(File root, Map<String,Object> properties, Project project) throws IOException {
        Map<String,Entry> entries = SOURCE_SCAN_CACHE.get(root);
        if (entries == null) {
            // Similar to #62221: if just invoked from a module in standard clusters, only scan those clusters (faster):
            Set<String> standardModules = new HashSet<>();
            boolean doFastScan = false;
            String basedir = (String) properties.get("basedir");
            String clusterList = (String) properties.get("nb.clusters.list");
            if (basedir != null) {
                File basedirF = new File(basedir);
                if (clusterList == null) {
                    String config = (String) properties.get("cluster.config");
                    if (config != null) {
                        clusterList = (String) properties.get("clusters.config." + config + ".list");
                    }
                }
                if (clusterList != null) {
                    StringTokenizer tok = new StringTokenizer(clusterList, ", ");
                    while (tok.hasMoreTokens()) {
                        String clusterName = tok.nextToken();
                        String moduleList = (String) properties.get(clusterName);
                        if (moduleList != null) {
                            // Hack to treat libs.junit4 as if it were in platform for purposes of building, yet build to another cluster.
                            if (clusterName.equals("nb.cluster.platform")) {
                                moduleList += ",libs.junit4";
                            } else if (clusterName.equals("nb.cluster.stableuc")) {
                                moduleList = moduleList.replace(",libs.junit4", "");
                            }


                            StringTokenizer tok2 = new StringTokenizer(moduleList, ", ");
                            while (tok2.hasMoreTokens()) {
                                String module = tok2.nextToken();
                                standardModules.add(module);
                                // doFastScan |= new File(root, module.replace('/', File.separatorChar)).equals(basedirF);
                            }
                        }
                    }
                }
            }
            String p = (String) properties.get("netbeans.dest.dir"); // NOI18N
            int hash = root.hashCode() * 7 + (p == null ? 1 : p.hashCode());
            File scanCache = new File(System.getProperty("java.io.tmpdir"), "nb-scan-cache-" + String.format("%x", hash) + "-" + (doFastScan ? "standard" : "full") + ".ser");
            if (scanCache.isFile()) {
                if (project != null) {
                    project.log("Loading module list from " + scanCache);
                }
                try {
                    try (InputStream is = new FileInputStream(scanCache)) {
                        ObjectInput oi = new ObjectInputStream(new BufferedInputStream(is));
                        @SuppressWarnings("unchecked") Map<File,Long[]> timestampsAndSizes = (Map) oi.readObject();
                        boolean matches = true;
                        for (Map.Entry<File,Long[]> entry : timestampsAndSizes.entrySet()) {
                            File f = entry.getKey();
                            if (f.lastModified() != entry.getValue()[0] || f.length() != entry.getValue()[1]) {
                                if (project != null) {
                                    project.log("Cache ignored due to modifications in " + f);
                                }
                                matches = false;
                                break;
                            }
                        }
                        if (doFastScan) {
                            @SuppressWarnings("unchecked") Set<String> storedStandardModules = (Set) oi.readObject();
                            if (!standardModules.equals(storedStandardModules)) {
                                Set<String> added = new TreeSet<>(standardModules);
                                added.removeAll(storedStandardModules);
                                Set<String> removed = new TreeSet<>(storedStandardModules);
                                removed.removeAll(standardModules);
                                project.log("Cache ignored due to changes in modules among standard clusters: + " + added + " - " + removed);
                                matches = false;
                            }
                        }
                        File myProjectXml = project.resolveFile("nbproject/project.xml");
                        if (myProjectXml.isFile() && !timestampsAndSizes.containsKey(myProjectXml)) {
                            project.log("Cache ignored since it has no mention of " + myProjectXml);
                            matches = false; // #118098
                        }
                        if (matches) {
                            @SuppressWarnings("unchecked") Map<String,Entry> _entries = (Map) oi.readObject();
                            entries = _entries;
                            if (project != null) {
                                project.log("Loaded modules: " + entries.keySet(), Project.MSG_DEBUG);
                            }
                        }
                    }
                } catch (Exception x) {
                    if (project != null) {
                        project.log("Error loading " + scanCache + ": " + x, Project.MSG_WARN);
                    }
                }
            }
            if (entries == null) {
                entries = new HashMap<>();
                Map<File,Long[]> timestampsAndSizes = new HashMap<>();
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "cluster.properties"), timestampsAndSizes);
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "build.properties"), timestampsAndSizes);
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "user.build.properties"), timestampsAndSizes);

                doFastScan = false;

                if (doFastScan) {
                    if (project != null) {
                        project.log("Scanning for modules in " + root + " among standard clusters");
                    }
                    for (String module : standardModules) {
                        scanPossibleProject(new File(root, module.replace('/', File.separatorChar)), entries, properties, module, ModuleType.NB_ORG, project, timestampsAndSizes);
                    }
                } else {
                    // Might be an extra module (e.g. something in contrib); need to scan everything.
                    if (project != null) {
                        project.log("Scanning for modules in " + root);
                        project.log("Quick scan mode disabled since " + basedir + " not among standard modules of " + root + " which are " + standardModules, Project.MSG_VERBOSE);
                    }
                    List<String> subDirs = new ArrayList<>();
                    subDirs.addAll(Arrays.asList(FOREST));
                    String allClusters = (String) properties.get("clusters.config.full.list");
                    if(allClusters == null) {
                        allClusters = "";
                    }
                    StringTokenizer tok = new StringTokenizer(allClusters, ", ");
                    while (tok.hasMoreTokens()) {
                        String clusterName = tok.nextToken();
                        String clusterDir = (String) properties.get(clusterName + ".dir");
                        if (subDirs.contains(clusterDir)) {
                            continue;
                        }
                        subDirs.add(clusterDir);
                    }
                    for (String tree : subDirs) {
                        File dir = tree == null ? root : new File(root, tree);
                        File[] kids = dir.listFiles();
                        if (kids == null) {
                            continue;
                        }
                        for (File kid : kids) {
                            if (!kid.isDirectory()) {
                                continue;
                            }
                            String name = kid.getName();
                            String path = tree == null ? name : tree + "/" + name;
                            scanPossibleProject(kid, entries, properties, path, ModuleType.NB_ORG, project, timestampsAndSizes);
                        }
                        
                    }
                }
                if (project != null) {
                    project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
                    project.log("Cache depends on files: " + timestampsAndSizes.keySet(), Project.MSG_DEBUG);
                }
                scanCache.getParentFile().mkdirs();
                try (OutputStream os = new FileOutputStream(scanCache)) {
                    ObjectOutput oo = new ObjectOutputStream(os);
                    oo.writeObject(timestampsAndSizes);
                    if (doFastScan) {
                        oo.writeObject(standardModules);
                    }
                    oo.writeObject(entries);
                    oo.flush();
                }
            }
            SOURCE_SCAN_CACHE.put(root, entries);
        }
        return entries;
    }

    private static void registerTimestampAndSize(File f, Map<File,Long[]> timestampsAndSizes) {
        if (timestampsAndSizes != null) {
            timestampsAndSizes.put(f, new Long[] {f.lastModified(), f.length()});
        }
    }
    
    /**
     * Check a single dir to see if it is an NBM project, and if so, register it.
     */
    private static boolean scanPossibleProject(File dir, Map<String,Entry> entries, Map<String,Object> properties,
            String path, ModuleType moduleType, Project project, Map<File,Long[]> timestampsAndSizes) throws IOException {
        File nbproject = new File(dir, "nbproject");
        File projectxml = new File(nbproject, "project.xml");
        if (!projectxml.isFile()) {
            return false;
        }
        registerTimestampAndSize(projectxml, timestampsAndSizes);
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(projectxml.toURI().toString()),
                                     false, true, /*XXX*/null, null);
        } catch (Exception e) { // SAXException, IOException (#60295: e.g. encoding problem in XML)
            // Include \n so that following line can be hyperlinked
            throw (IOException) new IOException("Error parsing project file\n" + projectxml + ": " + e.getMessage()).initCause(e);
        }
        Element typeEl = XMLUtil.findElement(doc.getDocumentElement(), "type", ParseProjectXml.PROJECT_NS);
        if (!XMLUtil.findText(typeEl).equals("org.netbeans.modules.apisupport.project")) {
            return false;
        }
        Element configEl = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
        Element dataEl = ParseProjectXml.findNBMElement(configEl, "data");
        if (dataEl == null) {
            if (project != null) {
                project.log(projectxml.toString() + ": warning: module claims to be a NBM project but is missing <data xmlns=\"" + ParseProjectXml.NBM_NS3 + "\">; maybe an old NB 4.[01] project?", Project.MSG_WARN);
            }
            return false;
        }
        Element cnbEl = ParseProjectXml.findNBMElement(dataEl, "code-name-base");
        String cnb = XMLUtil.findText(cnbEl);
        if (moduleType == ModuleType.NB_ORG && project != null) {
            String expectedDirName = abbreviate(cnb);
            String actualDirName = dir.getName();
            if (!actualDirName.equals(expectedDirName)) {
                throw new IOException("Expected module to be in dir named " + expectedDirName + " but was actually found in dir named " + actualDirName);
            }
        }
        // Clumsy but the best way I know of to evaluate properties.
        Project fakeproj = new Project();
        if (project != null) {
            // Try to debug any problems in the following definitions (cf. #59849).
            for(BuildListener bl: project.getBuildListeners()) {
                fakeproj.addBuildListener(bl);
            }
        }
        fakeproj.setBaseDir(dir); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        switch (moduleType) {
        case NB_ORG:
            // do nothing here
            break;
        case SUITE:
            faketask.setFile(new File(nbproject, "private/suite-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "suite.properties"));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/private/platform-private.properties")));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/platform.properties")));
            faketask.execute();
            break;
        case STANDALONE:
            faketask.setFile(new File(nbproject, "private/platform-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "platform.properties"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        File privateProperties = new File(nbproject, "private/private.properties".replace('/', File.separatorChar));
        registerTimestampAndSize(privateProperties, timestampsAndSizes);
        faketask.setFile(privateProperties);
        faketask.execute();
        File projectProperties = new File(nbproject, "project.properties");
        registerTimestampAndSize(projectProperties, timestampsAndSizes);
        faketask.setFile(projectProperties);
        faketask.execute();
        faketask.setFile(null);
        faketask.setName("module.jar.dir");
        faketask.setValue("modules");
        faketask.execute();
        assert fakeproj.getProperty("module.jar.dir") != null : fakeproj.getProperties();
        faketask.setName("module.jar.basename");
        faketask.setValue(cnb.replace('.', '-') + ".jar");
        faketask.execute();
        faketask.setName("module.jar");
        faketask.setValue(fakeproj.replaceProperties("${module.jar.dir}/${module.jar.basename}"));
        faketask.execute();
        String id = null;
        switch (moduleType) {
        case NB_ORG:
            assert path != null;
            // Find the associated cluster.
            // first try direct mapping in nbbuild/netbeans/moduleCluster.properties
            String[] clusterDir = { (String) properties.get(path + ".dir") };
            if (clusterDir[0] != null) {
                final String subStr = clusterDir[0].substring(clusterDir[0].lastIndexOf('/') + 1);
                if (path.startsWith(subStr + "/")) {
                    id = path.substring(subStr.length() + 1);
                }
                clusterDir[0] = subStr;
            } else {
                id = SetCluster.findClusterAndId("cluster.dir", clusterDir, properties, path, faketask, "extra");
            }
            faketask.setName("cluster.dir");
            faketask.setValue(clusterDir[0]);
            faketask.execute();
            faketask.setName("netbeans.dest.dir");
            faketask.setValue(properties.get("netbeans.dest.dir"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${netbeans.dest.dir}/${cluster.dir}"));
            faketask.execute();
            break;
        case SUITE:
            assert path == null;
            faketask.setName("suite.dir");
            faketask.setValue(properties.get("suite.dir"));
            faketask.execute();
            faketask.setName("suite.build.dir");
            faketask.setValue(fakeproj.replaceProperties("${suite.dir}/build"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${suite.build.dir}/cluster"));
            faketask.execute();
            break;
        case STANDALONE:
            assert path == null;
            faketask.setName("build.dir");
            faketask.setValue(fakeproj.replaceProperties("${basedir}/build"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${build.dir}/cluster"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        File jar = fakeproj.resolveFile(fakeproj.replaceProperties("${cluster}/${module.jar}"));
        List<File> exts = new ArrayList<>();
        for (Element ext : XMLUtil.findSubElements(dataEl)) {
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element binaryOrigin = ParseProjectXml.findNBMElement(ext, "binary-origin");
            File origBin = null;
            if (binaryOrigin != null) {
                String reltext = XMLUtil.findText(binaryOrigin);
                String nball = (String) properties.get("nb_all");
                if (nball != null) {
                    faketask.setName("nb_all");
                    faketask.setValue(nball);
                    faketask.execute();
                }
                fakeproj.setBaseDir(dir);
                origBin = fakeproj.resolveFile(fakeproj.replaceProperties(reltext));
            } 

            File resultBin = null;
            if (origBin == null || !origBin.exists()) {
                Element runtimeRelativePath = ParseProjectXml.findNBMElement(ext, "runtime-relative-path");
                if (runtimeRelativePath == null) {
                    throw new IOException("Have malformed <class-path-extension> in " + projectxml);
                }
                String reltext = XMLUtil.findText(runtimeRelativePath);
                if (reltext != null) {
                    // No need to evaluate property refs in it - it is *not* substitutable-text in the schema.
                    String nball = (String) properties.get("nb_all");
                    resultBin = new File(jar.getParentFile(), reltext.replace('/', File.separatorChar));
                    resultBin = fixFxRtJar(resultBin, nball);
                }
            }

            if (origBin != null) {
                exts.add(origBin);
            }

            if (resultBin != null) {
                exts.add(resultBin);
            }
        }
        List<String> prereqs = new ArrayList<>();
        List<String> rundeps = new ArrayList<>();
        Element depsEl = ParseProjectXml.findNBMElement(dataEl, "module-dependencies");
        if (depsEl == null) {
            throw new IOException("Malformed project file " + projectxml);
        }
        Element testDepsEl = ParseProjectXml.findNBMElement(dataEl,"test-dependencies");
         //compileDeps = Collections.emptyList();
        Map<String,String[]> compileTestDeps = new HashMap<>();
        if (testDepsEl != null) {
            for (Element depssEl : XMLUtil.findSubElements(testDepsEl)) {
                String testtype = ParseProjectXml.findTextOrNull(depssEl,"name") ;
                if (testtype == null) {
                    throw new IOException("Must declare <name>unit</name> (e.g.) in <test-type> in " + projectxml);
                }
                List<String> compileDepsList = new ArrayList<>();
                for (Element dep : XMLUtil.findSubElements(depssEl)) {
                    if (dep.getTagName().equals("test-dependency")) {
                        if (ParseProjectXml.findNBMElement(dep,"test") != null)  {
                            compileDepsList.add(ParseProjectXml.findTextOrNull(dep, "code-name-base"));
                        } 
                    }
                }
                compileTestDeps.put(testtype, compileDepsList.toArray(new String[0]));
            }
        } 
        for (Element dep : XMLUtil.findSubElements(depsEl)) {
            Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
            if (cnbEl2 == null) {
                throw new IOException("Malformed project file " + projectxml);
            }
            String cnb2 = XMLUtil.findText(cnbEl2);
            rundeps.add(cnb2);
            if (ParseProjectXml.findNBMElement(dep, "build-prerequisite") == null) {
                continue;
            }
            prereqs.add(cnb2);
        }
        String cluster = fakeproj.getProperty("cluster.dir"); // may be null
        Entry entry = new Entry(cnb, jar, exts.toArray(new File[exts.size()]), dir, path, id == null ? path : id,
                prereqs.toArray(new String[prereqs.size()]), 
                cluster, 
                rundeps.toArray(new String[rundeps.size()]),
                compileTestDeps,
                new File(dir, "module-auto-deps.xml")
                );
        if (entries.containsKey(cnb)) {
            throw new IOException("Duplicated module " + cnb + ": found in " + entries.get(cnb) + " and " + entry);
        } else {
            entries.put(cnb, entry);
        }
        return true;
    }

    private static File fixFxRtJar(File resultBin, String nball) {
        final String path = resultBin.getPath().replace(File.separatorChar, '/');
        if (!resultBin.exists() && path.contains("${java.home}/lib/ext/jfxrt.jar")) {
            String jhm = System.getProperty("java.home");
            resultBin = new File(new File(new File(new File(jhm), "lib"), "ext"), "jfxrt.jar");
            if (!resultBin.exists()) {
                File jdk7 = new File(new File(new File(jhm), "lib"), "jfxrt.jar");
                if (jdk7.exists()) {
                    resultBin = jdk7;
                } else if (nball != null) {
                    File external = new File(new File(new File(new File(nball), "libs.javafx"), "external"), "jfxrt.jar");
                    if (external.exists()) {
                        resultBin = external;
                    }
                }
            }
        }
        return resultBin;
    }
    static String abbreviate(String cnb) {
        return cnb.replaceFirst("^org\\.netbeans\\.modules\\.", ""). // NOI18N
                   replaceFirst("^org\\.netbeans\\.(libs|lib|api|spi|core)\\.", "$1."). // NOI18N
                   replaceFirst("^org\\.netbeans\\.", "o.n."). // NOI18N
                   replaceFirst("^org\\.openide\\.", "openide."). // NOI18N
                   replaceFirst("^org\\.", "o."). // NOI18N
                   replaceFirst("^com\\.sun\\.", "c.s."). // NOI18N
                   replaceFirst("^com\\.", "c."); // NOI18N
    }
    
    /**
     * Find all modules in a binary build, possibly from cache.
     */
    private static Map<String,Entry> scanBinaries(Project project, File[] clusters) throws IOException {
        Map<String,Entry> allEntries = new HashMap<>();

        for (File cluster : clusters) {
            Map<String, Entry> entries = BINARY_SCAN_CACHE.get(cluster);
            if (entries == null) {
                if (project != null) {
                    project.log("Scanning for modules in " + cluster);
                }
                entries = new HashMap<>();
                doScanBinaries(cluster, entries);
                if (project != null) {
                    project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
                }
                BINARY_SCAN_CACHE.put(cluster, entries);
            }
            allEntries.putAll(entries);
        }
        return allEntries;
    }
    
    private static final String[] MODULE_DIRS = {
        "modules",
        "modules/eager",
        "modules/autoload",
        "lib",
        "core",
    };
    /**
     * Look for all possible modules in a NB build.
     * Checks modules/{,autoload/,eager/}*.jar as well as well-known core/*.jar and lib/boot.jar in each cluster.
     * XXX would be slightly more precise to check config/Modules/*.xml rather than scan for module JARs.
     */
    private static void doScanBinaries(File cluster, Map<String,Entry> entries) throws IOException {
        File moduleAutoDepsDir = new File(new File(cluster, "config"), "ModuleAutoDeps");
            for (String moduleDir : MODULE_DIRS) {
                if (moduleDir.endsWith("lib") && !cluster.getName().contains("platform")) {
                    continue;
                }
                File dir = new File(cluster, moduleDir.replace('/', File.separatorChar));
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] jars = dir.listFiles();
                if (jars == null) {
                    throw new IOException("Cannot examine dir " + dir);
                }
                for (File m : jars) {
                    scanOneBinary(m, cluster, entries, moduleAutoDepsDir);
                }
            }

            final File configDir = new File(new File(cluster, "config"), "Modules");
            File[] configs = configDir.listFiles();
            XPathExpression expr = null;
            DocumentBuilder b = null;
            if (configs != null) {
                for (File xml : configs) {
                    // TODO, read location, scan
                    final String fileName = xml.getName();
                    if (!fileName.endsWith(".xml")) {
                        continue;
                    }
                    if (entries.containsKey(fileName.substring(0, fileName.length() - 4).replace('-', '.'))) {
                        continue;
                    }
                    try {
                        if (expr == null) {
                            expr = XPathFactory.newInstance().newXPath().compile("/module/param[@name='jar']");
                            b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            b.setEntityResolver(new EntityResolver() {
                                public InputSource resolveEntity(String publicId, String systemId)
                                throws SAXException, IOException {
                                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                                }
                            });
                        }
                        Document doc = b.parse(xml);
                        String res = expr.evaluate(doc);
                        File jar = new File(cluster, res.replace('/', File.separatorChar));
                        if (!jar.isFile()) {
                            if (cluster.getName().equals("ergonomics")) {
                                // this is normal
                                continue;
                            }
                            throw new BuildException("Cannot find module " + jar + " from " + xml);
                        }
                        scanOneBinary(jar, cluster, entries, moduleAutoDepsDir);
                    } catch (Exception ex) {
                        throw new BuildException(ex);
                    }
                }
            }
    }
    
    private static Map<String,Entry> scanSuiteSources(Map<String,Object> properties, Project project) throws IOException {
        File basedir = new File((String) properties.get("basedir"));
        String suiteDir = (String) properties.get("suite.dir");
        if (suiteDir == null) {
            throw new IOException("No definition of suite.dir in " + basedir);
        }
        File suite = FileUtils.getFileUtils().resolveFile(basedir, suiteDir);
        if (!suite.isDirectory()) {
            throw new IOException("No such suite " + suite);
        }
        Map<String,Entry> entries = SUITE_SCAN_CACHE.get(suite);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in suite " + suite);
            }
            entries = new HashMap<>();
            doScanSuite(entries, suite, properties, project);
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            SUITE_SCAN_CACHE.put(suite, entries);
        }
        return entries;
    }
    
    private static void doScanSuite(Map<String,Entry> entries, File suite, Map<String,Object> properties, Project project) throws IOException {
        Project fakeproj = new Project();
        fakeproj.setBaseDir(suite); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        faketask.setFile(new File(suite, "nbproject/private/private.properties".replace('/', File.separatorChar)));
        faketask.execute();
        faketask.setFile(new File(suite, "nbproject/project.properties".replace('/', File.separatorChar)));
        faketask.execute();
        String modulesS = fakeproj.getProperty("modules");
        if (modulesS == null) {
            throw new IOException("No definition of modules in " + suite);
        }
        String[] modules = Path.translatePath(fakeproj, modulesS);
        for (int i = 0; i < modules.length; i++) {
            File module = new File(modules[i]);
            if (!module.isDirectory()) {
                throw new IOException("No such module " + module + " referred to from " + suite);
            }
            if (!scanPossibleProject(module, entries, properties, null, ModuleType.SUITE, project, null)) {
                throw new IOException("No valid module found in " + module + " referred to from " + suite);
            }
        }
    }
    
    private static Entry scanStandaloneSource(Map<String,Object> properties, Project project) throws IOException {
        if (properties.get("project") == null) return null; //Not a standalone module
        File basedir = new File((String) properties.get("project"));
        Entry entry = STANDALONE_SCAN_CACHE.get(basedir);
        if (entry == null) {
            Map<String,Entry> entries = new HashMap<>();
            if (!scanPossibleProject(basedir, entries, properties, null, ModuleType.STANDALONE, project, null)) {
                throw new IOException("No valid module found in " + basedir);
            }
            assert entries.size() == 1;
            entry = entries.values().iterator().next();
            STANDALONE_SCAN_CACHE.put(basedir, entry);
        }
        return entry;
    }
    
    /** all module entries, indexed by cnb */
    private final Map<String,Entry> entries;
    
    /**
     * Initiates scan if not already parsed.
     * Properties interpreted:
     * <ol>
     * <li> ${nb_all} - location of NB sources (used only for netbeans.org modules)
     * <li> ${netbeans.dest.dir} - location of NB build (used only for NB.org modules)
     * <li> ${cluster.path.final} - location of clusters to build against,
     * created from ${cluster.path} specified in platform.properties file (used only for suite and standalone modules)
     * <li> ${basedir} - directory of the project initiating the scan (most significant for standalone modules)
     * <li> ${suite.dir} - directory of the suite (used only for suite modules)
     * <li> ${nb.cluster.TOKEN} - list of module paths included in cluster TOKEN (comma-separated) (used only for netbeans.org modules)
     * <li> ${nb.cluster.TOKEN.dir} - directory in ${netbeans.dest.dir} where cluster TOKEN is built (used only for netbeans.org modules)
     * <li> ${project} - basedir for standalone modules
     * </ol>
     * @param properties some properties to be used (see above)
     * @param type the type of project
     * @param project a project ref, only for logging (may be null with no loss of semantics)
     */
    public ModuleListParser(Map<String,Object> properties, ModuleType type, Project project) throws IOException {
        String nball = (String) properties.get("nb_all");
        File basedir = new File((String) properties.get("basedir"));
        final FileUtils fu = FileUtils.getFileUtils();

        if (type != ModuleType.NB_ORG) {
            // add extra clusters
            String suiteDirS = (String) properties.get("suite.dir");
            boolean hasSuiteDir = suiteDirS != null && suiteDirS.length() > 0;
            String clusterPath = (String) properties.get("cluster.path.final");
            File[] clusters = null;

            if (clusterPath != null) {
                String[] clustersS;
                if (hasSuiteDir) {
                    // resolve suite modules against fake suite project
                    Project fakeproj = new Project();
                    fakeproj.setBaseDir(new File(suiteDirS));
                    clustersS = Path.translatePath(fakeproj, clusterPath);
                } else {
                    clustersS = Path.translatePath(project, clusterPath);
                }
                clusters = new File[clustersS.length];
                if (clustersS != null && clustersS.length > 0) {
                    for (int j = 0; j < clustersS.length; j++) {
                        File cluster = new File(clustersS[j]);
                        if (! cluster.isDirectory()) {
                            throw new IOException("No such cluster " + cluster + " referred to from ${cluster.path.final}: " + clusterPath);
                        }
                        clusters[j] = cluster;
                    }
                }
            }

            if (clusters == null || clusters.length == 0)
                throw new IOException("Invalid ${cluster.path.final}: " + clusterPath);

            // External module.
            if (nball != null && project != null) {
                project.log("You must *not* declare <suite-component/> or <standalone/> for a netbeans.org module in " + basedir + "; fix project.xml to use the /2 schema", Project.MSG_WARN);
            }
            entries = scanBinaries(project, clusters);
            if (type == ModuleType.SUITE) {
                entries.putAll(scanSuiteSources(properties, project));
            } else {
                assert type == ModuleType.STANDALONE;
                Entry e = scanStandaloneSource(properties, project);
                entries.put(e.getCnb(), e);
            }
        } else {
            // netbeans.org module.
            String buildS = (String) properties.get("netbeans.dest.dir");
            if (buildS == null) {
                throw new IOException("No definition of netbeans.dest.dir in " + basedir);
            }
            // Resolve against basedir, and normalize ../ sequences and so on in case they are used.
            // Neither operation is likely to be needed, but just in case.
            File build = fu.normalize(fu.resolveFile(basedir, buildS).getAbsolutePath());
            if (nball == null) {
                throw new IOException("You must declare either <suite-component/> or <standalone/> for an external module in " + new File((String) properties.get("basedir")));
            }
            String nbBuildDir = (String) properties.get("nb.build.dir");
            if (!build.equals(new File(new File(nball, "nbbuild"), "netbeans")) && !(nbBuildDir != null && build.equals(new File(nbBuildDir, "netbeans")))) {
                // Potentially orphaned module to be built against specific binaries, plus perhaps other source deps.
                if (!build.isDirectory()) {
                    throw new IOException("No such netbeans.dest.dir: " + build);
                }
                // expand clusters in build
                File[] clusters = build.listFiles();
                if (clusters == null) {
                    throw new IOException("Cannot examine dir " + build);
                }
                entries = scanBinaries(project, clusters);
                // Add referenced module in case it does not appear otherwise.
                Entry e = scanStandaloneSource(properties, project);
                if (e != null) {
                    entries.put(e.getCnb(), e);
                }
                entries.putAll(scanNetBeansOrgSources(new File(nball), properties, project));
            } else {
                entries = scanNetBeansOrgSources(new File(nball), properties, project);
            }
        }
    }
    /**
     * Find all entries in this list.
     * @return a set of all known entries
     */
    public Set<Entry> findAll() {
        return new HashSet<>(entries.values());
    }
    
    /**
     * Find one entry by code name base.
     * @param cnb the desired code name base
     * @return the matching entry or null
     */
    public Entry findByCodeNameBase(String cnb) {
        return entries.get(cnb);
    }

    
    /** parse Openide-Module-Module-Dependencies entry
     * @return array of code name bases
     */
    private static String[] parseRuntimeDependencies(String moduleDependencies) {
        if (moduleDependencies == null) {
            return new String[0];
        }
        List<String> cnds = new ArrayList<>();
        StringTokenizer toks = new StringTokenizer(moduleDependencies,",");
        while (toks.hasMoreTokens()) {
            String token = toks.nextToken().trim();
            // substring cnd/x
            int slIdx = token.indexOf('/');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            // substring cnd' 'xx
            slIdx = token.indexOf(' ');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            // substring cnd > 
            slIdx = token.indexOf('>');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            token = token.trim();
            if (token.length() > 0) {
               cnds.add(token);
            }
        }
        return cnds.toArray(new String[cnds.size()]);
    }

    static boolean scanOneBinary(File m, File cluster, Map<String, Entry> entries, File moduleAutoDepsDir) throws IOException {
        if (!m.getName().endsWith(".jar")) {
            return true;
        }
        JarFile jf;
        try {
            jf = new JarFile(m);
        } catch (IOException x) {
            throw new IOException("could not open " + m + ": " + x, x);
        }
        File dir = m.getParentFile();
        try {
            Attributes attr = jf.getManifest().getMainAttributes();
            String codename = JarWithModuleAttributes.extractCodeName(attr);
            if (codename == null) {
                return true;
            }
            String codenamebase;
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                codenamebase = codename;
            } else {
                codenamebase = codename.substring(0, slash);
            }

            String cp = attr.getValue("Class-Path");
            File[] exts;
            if (cp == null) {
                exts = new File[0];
            } else {
                String[] pieces = cp.split(" +");
                exts = new File[pieces.length];
                for (int l = 0; l < pieces.length; l++) {
                    String append = URLDecoder.decode(pieces[l].replace('/', File.separatorChar), "UTF-8");
                    exts[l] = fixFxRtJar(new File(dir, append), null);
                }
            }
            String moduleDependencies = attr.getValue("OpenIDE-Module-Module-Dependencies");


            Entry entry = new Entry(codenamebase, m, exts, null, null, null, null, cluster.getName(),
                    parseRuntimeDependencies(moduleDependencies), Collections.<String,String[]>emptyMap(),
                    new File(moduleAutoDepsDir, codenamebase.replace('.', '-') + ".xml"));
            Entry prev = entries.put(codenamebase, entry);
            if (prev != null && !prev.equals(entry)) {
                throw new IOException("Duplicated module " + codenamebase + ": found in " + prev + " and " + entry);
            }
        } finally {
            jf.close();
        }
        return false;
    }
    
    /**
     * One entry in the file.
     */
    @SuppressWarnings("serial") // really want it to be incompatible if format changes
    public static final class Entry implements Serializable {

        private final String cnb;
        private final File jar;
        private final File[] classPathExtensions;
        private final File sourceLocation;
        private final String netbeansOrgPath;
        private final String netbeansOrgId;
        private final String[] buildPrerequisites;
        private final String clusterName;
        private final String[] runtimeDependencies; 
        // dependencies on other tests
        private final Map<String,String[]> testDependencies;
        private final File moduleAutoDeps;
        
        Entry(String cnb, File jar, File[] classPathExtensions, File sourceLocation, String netbeansOrgPath, String netbeansOrgId,
                String[] buildPrerequisites, String clusterName,String[] runtimeDependencies, Map<String,String[]> testDependencies, File moduleAutoDeps) {
            this.cnb = cnb;
            this.jar = jar;
            this.classPathExtensions = classPathExtensions;
            this.sourceLocation = sourceLocation;
            this.netbeansOrgPath = netbeansOrgPath;
            this.netbeansOrgId = netbeansOrgId;
            this.buildPrerequisites = buildPrerequisites;
            this.clusterName = clusterName;
            this.runtimeDependencies = runtimeDependencies;
            assert testDependencies != null;
            this.testDependencies = testDependencies;
            this.moduleAutoDeps = moduleAutoDeps;
        }
        
        /**
         * Get the code name base, e.g. org.netbeans.modules.ant.grammar.
         */
        public String getCnb() {
            return cnb;
        }
        
        /**
         * Get the absolute JAR location, e.g. .../ide/modules/org-netbeans-modules-ant-grammar.jar.
         */
        public File getJar() {
            return jar;
        }
        
        /**
         * Get a list of extensions to the class path of this module (may be empty).
         */
        public File[] getClassPathExtensions() {
            return classPathExtensions;
        }

        /**
         * @return the sourceLocation, may be null.
         */
        public File getSourceLocation() {
            return sourceLocation;
        }
        
        /**
         * Get the path within netbeans.org, if this is a netbeans.org module (else null).
         */
        public String getNetbeansOrgPath() {
            return netbeansOrgPath;
        }
        
        /**
         * Get a list of declared build prerequisites (or null for sourceless entries).
         * Each entry is a code name base.
         */
        public String[] getBuildPrerequisites() {
            return buildPrerequisites;
        }
        /** Get runtime dependencies, OpenIDE-Module-Dependencies entry. 
         * Each entry is a code name base.
         */
        public String[] getRuntimeDependencies() {
            return runtimeDependencies;
        }
        
        /**
         * Return the name of the cluster in which this module resides.
         * If this entry represents an external module in source form,
         * then the cluster will be null. If the module represents a netbeans.org
         * module or a binary module in a platform, then the cluster name will
         * be the (base) name of the directory containing the "modules" subdirectory
         * (sometimes "lib" or "core") where the JAR is.
         */
        public String getClusterName() {
            return clusterName;
        }
        
        public Map<String,String[]> getTestDependencies() {
            return testDependencies;
        }

        /**
         * Gets the module auto dependencies XML file.
         * @return a file location (may or may not exist)
         */
        public File getModuleAutoDeps() {
            return moduleAutoDeps;
        }

        public @Override String toString() {
            return (sourceLocation != null ? sourceLocation : jar).getAbsolutePath();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if ((this.cnb == null) ? (other.cnb != null) : !this.cnb.equals(other.cnb)) {
                return false;
            }
            if (this.jar != other.jar && (this.jar == null || !this.jar.equals(other.jar))) {
                return false;
            }
            if (!Arrays.deepEquals(this.classPathExtensions, other.classPathExtensions)) {
                return false;
            }
            if (this.sourceLocation != other.sourceLocation && (this.sourceLocation == null || !this.sourceLocation.equals(other.sourceLocation))) {
                return false;
            }
            if ((this.netbeansOrgPath == null) ? (other.netbeansOrgPath != null) : !this.netbeansOrgPath.equals(other.netbeansOrgPath)) {
                return false;
            }
            if ((this.netbeansOrgId == null) ? (other.netbeansOrgId != null) : !this.netbeansOrgId.equals(other.netbeansOrgId)) {
                return false;
            }
            if (!Arrays.deepEquals(this.buildPrerequisites, other.buildPrerequisites)) {
                return false;
            }
            if ((this.clusterName == null) ? (other.clusterName != null) : !this.clusterName.equals(other.clusterName)) {
                return false;
            }
            if (!Arrays.deepEquals(this.runtimeDependencies, other.runtimeDependencies)) {
                return false;
            }
            if (this.testDependencies != other.testDependencies && (this.testDependencies == null || !this.testDependencies.equals(other.testDependencies))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (this.cnb != null ? this.cnb.hashCode() : 0);
            hash = 83 * hash + (this.jar != null ? this.jar.hashCode() : 0);
            hash = 83 * hash + Arrays.deepHashCode(this.classPathExtensions);
            hash = 83 * hash + (this.sourceLocation != null ? this.sourceLocation.hashCode() : 0);
            hash = 83 * hash + (this.netbeansOrgPath != null ? this.netbeansOrgPath.hashCode() : 0);
            hash = 83 * hash + (this.netbeansOrgId != null ? this.netbeansOrgId.hashCode() : 0);
            hash = 83 * hash + Arrays.deepHashCode(this.buildPrerequisites);
            hash = 83 * hash + (this.clusterName != null ? this.clusterName.hashCode() : 0);
            hash = 83 * hash + Arrays.deepHashCode(this.runtimeDependencies);
            hash = 83 * hash + (this.testDependencies != null ? this.testDependencies.hashCode() : 0);
            return hash;
        }

        String getNetbeansOrgId() {
            return netbeansOrgId;
        }


    }

}
