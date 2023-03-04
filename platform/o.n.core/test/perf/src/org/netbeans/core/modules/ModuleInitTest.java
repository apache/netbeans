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

// To run me:
// cd nb_all
// ant -f nbbuild/build.xml
// java -classpath core/netbeans/lib/ext/boot.jar:core/netbeans/lib/core.jar:openide/netbeans/lib/openide.jar:performance/src:junit/external/junit-3.7.jar:core/test/perf/src:core/external/xerces-2.0.2.jar:core/external/xml-apis-1.0b2.jar org.netbeans.core.modules.ModuleInitTest

package org.netbeans.core.modules;

import java.io.*;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import org.w3c.dom.*;
import org.apache.xml.serialize.*;

import org.netbeans.core.NbTopManager;
import org.netbeans.performance.Benchmark;
import org.openide.xml.XMLUtil;

// initial run (04-sep-02) with empty dummy modules under JDK 1.4.1rc:
// 0    - 0.92Mb, 2.75s
// 10   - 1.44Mb, 3.36s
// 100  - 1.71Mb, 4.5s
// 1000 - 7.21Mb, 9-12s

// another run (04-sep-02) with module dependencies:
// 0    - 0.92Mb, 2.77s
// 10   - 1.41Mb, 3.37s
// 100  - 1.69Mb, 4.3s
// 1000 - 7.72Mb, 10-12s
// So dependencies only seem to have a small startup time impact
// for a lot of modules (hundreds, a second or so).

// 7ms/module to read manifest & .xml file, plus .25ms/module to get file list from Modules/ (mostly file access?)
// .35ms/module to check dependency & orderings
// 2.4ms/module to open JAR & create classloader
// .2ms/m to look for nonexistent sections [already improving]
// .14ms/m to look for nonexistent module installs [how to improve?]

// after some tweaks (05-sep-02), measured inside NB w/ term:
// 0    - 0.87Mb, 3.0s
// 10   - 1.32Mb, 3.7s
// 100  - 1.73Mb, 4.6s
// 1000 - 6.29Mb, 11.3s
// measured outside from a shell:
// 0    - 1.15Mb, 2.9s
// 10   - 0.82Mb, 3.1s
// 100  - 1.66Mb, around 4s
// 1000 - 5.74Mb, 10.5s
// after adding 1000 files to each module:
// 1000 - 6.82Mb, 13.2s
// after adding 0 - 250 files to each module:
// 1000 - 6.64Mb, around 12s
// after adding 23 files to each module's layer:
// 1000 - 19.00Mb, around 17s
// with some attributes too, 2 on 1/5 of folders and 1 on 1/3 of files:
// 1000 - 20.3Mb, around 17.5s

// so about 5.5ms/m for layer parsing, <1ms/m for opening a bigger JAR

// After ModuleList opt to not use Crimson to parse Modules/*.xml:
// 1000 - 21.1Mb, 16.4s
// detail timing: now only 3.8ms/m to load manifest + parse XML

/**
 * Benchmark measuring initialization of the module system.
 * Covers parsing of module config XML files; opening JARs;
 * reading their manifests; computing dependencies; loading XML layers;
 * turning everything on.
 * @author Jesse Glick
 * @see "#26786"
 */
public class ModuleInitTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(ModuleInitTest.class);
    }
    
    private static Map[] parseParams(String[] ps) {
        Map[] m = new Map[ps.length];
        for (int i = 0; i < ps.length; i++) {
            m[i] = new HashMap(); // Map<String,String|Integer>
            StringTokenizer tok = new StringTokenizer(ps[i], ",");
            while (tok.hasMoreTokens()) {
                String kv = tok.nextToken();
                int x = kv.indexOf('=');
                String k = kv.substring(0, x);
                String v = kv.substring(x + 1);
                try {
                    m[i].put(k, new Integer(v));
                } catch (NumberFormatException nfe) {
                    m[i].put(k, v);
                }
            }
        }
        return m;
    }
    
    public ModuleInitTest(String name) {
        super(name, parseParams(new String[] {
            // Simple scalability test:
            "modules=0,jarSize=200,layerSize=23",
            "modules=10,jarSize=200,layerSize=23",
            "modules=100,jarSize=200,layerSize=23",
            "modules=1000,jarSize=200,layerSize=23",
            /* Test manifest caching:
            "modules=100,jarSize=200,layerSize=0,-Dnetbeans.cache.manifests=false",
            "modules=100,jarSize=200,layerSize=0,-Dnetbeans.cache.manifests=true",
            "modules=1000,jarSize=200,layerSize=0,-Dnetbeans.cache.manifests=false",
            "modules=1000,jarSize=200,layerSize=0,-Dnetbeans.cache.manifests=true",
            */
        }));
    }
    
    private static File getTmpDir() {
        File ramdisk = new File("/dev/shm");
        if (ramdisk.isDirectory() && ramdisk.canWrite()) {
            return ramdisk;
        } else {
            return new File(System.getProperty("java.io.tmpdir"));
        }
    }
    private File topdir = new File(getTmpDir(), "ModuleInitTest");
    private File homedir = new File(topdir, "home");
    private File skeldir = new File(topdir, "skeluser");
    private File userdir = new File(topdir, "user");
    private Map lastParams = null;
    
    protected void setUp() throws Exception {
        Map params = (Map)getArgument();
        if (!params.equals(lastParams)) {
            //System.out.println("Setup: " + params);
            if (homedir.exists()) {
                deleteRec(homedir);
            }
            File mods = new File(homedir, "modules");
            File amods = new File(mods, "autoload");
            amods.mkdirs();
            File emods = new File(mods, "eager");
            emods.mkdirs();
            createModules(params, mods, amods, emods);
            new File(homedir, "system").mkdirs();
            // Priming run to create system/Modules directory:
            if (skeldir.exists()) {
                deleteRec(skeldir);
            }
            runNB(homedir, skeldir, false, params);
            lastParams = params;
        }
        // On every run, copy the primed skeleton user dir to the real location,
        // then start NB with the copied user dir.
        if (userdir.exists()) {
            deleteRec(userdir);
        }
        copyRec(skeldir, userdir);
    }
    protected void tearDown() throws Exception {
    }
    
    private static void deleteRec(File x) throws IOException {
        File[] kids = x.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (!x.delete()) throw new IOException("Could not delete: " + x);
    }
    private static void copyStream(InputStream is, OutputStream os) throws IOException {
        try {
            byte[] b = new byte[4096];
            int i;
            while ((i = is.read(b)) != -1) {
                os.write(b, 0, i);
            }
        } finally {
            is.close();
        }
    }
    private static void copyRec(File x, File y) throws IOException {
        if (x.isDirectory()) {
            if (!y.mkdirs()) throw new IOException("Could not mkdir: " + y);
            String[] kids = x.list();
            if (kids == null) throw new IOException("Could not list: " + x);
            for (int i = 0; i < kids.length; i++) {
                copyRec(new File(x, kids[i]), new File(y, kids[i]));
            }
        } else {
            y.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(y);
            try {
                copyStream(new FileInputStream(x), os);
            } finally {
                os.close();
            }
        }
    }
    
    // Sorry, I can't easily draw this dependency graph.
    // It is complicated - that is the whole point.
    private static final int cyclesize = 8;
    // Base names of cyclic pattern of modules:
    private static final String[] names = {"aut1", "prv1", "reg1", "aut2", "reg2", "reg3", "dis1", "eag1"};
    // Types: 0 = regular, 1 = autoload, 2 = eager
    private static final int[] types = {1, 1, 0, 1, 0, 0, 0, 2};
    // Intra-set dependencies (list of indices of other modules):
    private static final int[][] intradeps = {{}, {}, {0}, {}, {0, 3}, {3}, {3}, {4, 5}};
    // Inter-set dependencies (list of indices of analogous modules in the previous cycle):
    private static final int[][] interdeps = {{0}, {}, {2}, {}, {}, {}, {6}, {5}};
    // Whether the module should be permitted to be enabled or not:
    private static final boolean[] enabled = {true, true, true, true, true, true, false, true};
    // Provided tokens from the module; freeform, but if ends in '#' that will get subst'd w/ cycle
    private static final String[][] provides = {{}, {"tok#"}, {}, {}, {}, {}, {}, {}};
    // Required tokens; same syntax as above.
    private static final String[][] requires = {{}, {}, {"tok#"}, {}, {}, {}, {}, {}};
    // E.g.: module #16 (0-indexed) is in the third cycle and is a reg1, thus named reg1_002.
    // It depends on module #14, aut1_002 (from intradeps) and module #9, reg1_001 (from interdeps).
    // It also depends on tok_002, provided only by module #15, prv1_002.
    // All of these will be enabled, along with some other modules (indirectly).
    static {
        if (names.length != cyclesize ||
                types.length != cyclesize ||
                intradeps.length != cyclesize ||
                interdeps.length != cyclesize ||
                enabled.length != cyclesize ||
                provides.length != cyclesize ||
                requires.length != cyclesize) {
            throw new Error();
        }
    }
    
    private void createModules(Map params, File mods, File amods, File emods) throws IOException {
        int size = ((Integer)params.get("modules")).intValue();
        int jarSize = ((Integer)params.get("jarSize")).intValue();
        int layerSize = ((Integer)params.get("layerSize")).intValue();
        File[] moddirs = {mods, amods, emods}; // indexed by types[n]
        for (int i = 0; i < size; i++) {
            int which = i % cyclesize;
            int cycle = i / cyclesize;
            String cycleS = Integer.toString(cycle);
            // Enough for 1000*cyclesize modules to sort nicely:
            while (cycleS.length() < 3) cycleS = "0" + cycleS;
            Manifest mani = new Manifest();
            Attributes attr = mani.getMainAttributes();
            attr.putValue("Manifest-Version", "1.0");
            String nameBase = names[which] + "_" + cycleS;
            String name = "com.testdomain." + nameBase;
            String nameSlashes = name.replace('.', '/');
            attr.putValue("OpenIDE-Module", name + "/1");
            // Avoid requiring javahelp:
            attr.putValue("OpenIDE-Module-IDE-Dependencies", "IDE/1 > 2.2");
            attr.putValue("OpenIDE-Module-Specification-Version", "1.0");
            StringBuffer deps = null;
            for (int j = 0; j < intradeps[which].length; j++) {
                if (deps == null) {
                    deps = new StringBuffer(1000);
                } else {
                    deps.append(", ");
                }
                deps.append("com.testdomain." + names[intradeps[which][j]]);
                deps.append('_');
                deps.append(cycleS);
                deps.append("/1 > 1.0");
            }
            if (cycle > 0) {
                String oldCycleS = Integer.toString(cycle - 1);
                while (oldCycleS.length() < 3) oldCycleS = "0" + oldCycleS;
                for (int j = 0; j < interdeps[which].length; j++) {
                    if (deps == null) {
                        deps = new StringBuffer(1000);
                    } else {
                        deps.append(", ");
                    }
                    deps.append("com.testdomain." + names[interdeps[which][j]]);
                    deps.append('_');
                    deps.append(oldCycleS);
                    deps.append("/1 > 1.0");
                }
            }
            if (!enabled[which]) {
                if (deps == null) {
                    deps = new StringBuffer(1000);
                } else {
                    deps.append(", ");
                }
                // An impossible dependency.
                deps.append("honest.man.in.washington");
            }
            if (deps != null) {
                attr.putValue("OpenIDE-Module-Module-Dependencies", deps.toString());
            }
            if (provides[which].length > 0) {
                StringBuffer buf = new StringBuffer(100);
                for (int j = 0; j < provides[which].length; j++) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    String tok = provides[which][j];
                    if (tok.endsWith("#")) {
                        tok = tok.substring(0, tok.length() - 1) + "_" + cycleS;
                    }
                    buf.append("com.testdomain." + tok);
                }
                attr.putValue("OpenIDE-Module-Provides", buf.toString());
            }
            if (requires[which].length > 0) {
                StringBuffer buf = new StringBuffer(100);
                for (int j = 0; j < requires[which].length; j++) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    String tok = requires[which][j];
                    if (tok.endsWith("#")) {
                        tok = tok.substring(0, tok.length() - 1) + "_" + cycleS;
                    }
                    buf.append("com.testdomain." + tok);
                }
                attr.putValue("OpenIDE-Module-Requires", buf.toString());
            }
            // Files in JAR other than manifest:
            Map contents = new TreeMap(); // Map<String,byte[]>
            String locb = nameSlashes + "/Bundle.properties";
            contents.put(locb, ("OpenIDE-Module-Name=Module #" + i + "\n").getBytes());
            attr.putValue("OpenIDE-Module-Localizing-Bundle", locb);
            contents.put(nameSlashes + "/foo", "stuff here\n".getBytes());
            contents.put(nameSlashes + "/subdir/foo", "more stuff here\n".getBytes());
            for (int j = 0; j < jarSize; j++) {
                contents.put(nameSlashes + "/sub/subdir/file" + j, new byte[j]);
            }
            // XML layer:
            Map layer = new TreeMap(); // Map<String,byte[]|null>
            // Construct a binomial tree, module #i contributing the next layerSize files (leaves):
            int start = i * layerSize;
            int end = (i + 1) * layerSize;
            for (int j = start; j < end; j++) {
                String filename = "file" + j + ".txt";
                int bit = 0;
                int x = j;
                while (x > 0) {
                    if (x % 2 == 1) {
                        filename = "dir" + bit + "/" + filename;
                    }
                    bit++;
                    x /= 2;
                }
                layer.put(filename, (j % 2 == 0) ? ("Contents #" + j + "\n").getBytes() : null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(layer.size() * 100 + 1);
            Document doc = XMLUtil.createDocument("filesystem", null, "-//NetBeans//DTD Filesystem 1.1//EN", "http://www.netbeans.org/dtds/filesystem-1_1.dtd");
            doc.getDocumentElement().appendChild(doc.createComment(" Layer filenames for module #" + i + ": " + layer.keySet() + " "));
            Iterator it = layer.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                String filename = (String)e.getKey();
                byte[] filebytes = (byte[])e.getValue();
                Element el = doc.getDocumentElement();
                StringTokenizer tok = new StringTokenizer(filename, "/");
                while (tok.hasMoreTokens()) {
                    String piece = tok.nextToken();
                    if (tok.hasMoreTokens()) {
                        // A dir. Look for it...
                        Element child = null;
                        NodeList kids = el.getChildNodes();
                        for (int j = 0; j < kids.getLength(); j++) {
                            Node n = kids.item(j);
                            if (!(n instanceof Element)) continue;
                            Element kid = (Element)n;
                            if (!kid.getNodeName().equals("folder")) continue;
                            if (kid.getAttribute("name").equals(piece)) {
                                child = kid;
                                break;
                            }
                        }
                        if (child == null) {
                            child = doc.createElement("folder");
                            child.setAttribute("name", piece);
                            // Attributes, on every fifth folder:
                            if (Math.abs(filename.hashCode()) % 5 == 0) {
                                Element a = doc.createElement("attr");
                                a.setAttribute("name", "SystemFileSystem.localizingBundle");
                                a.setAttribute("stringvalue", name + ".Bundle");
                                child.appendChild(a);
                                a = doc.createElement("attr");
                                a.setAttribute("name", "SystemFileSystem.icon");
                                a.setAttribute("urlvalue", "nbresloc:/" + nameSlashes + "/resources/" + piece + ".gif");
                                child.appendChild(a);
                            }
                            el.appendChild(child);
                        }
                        el = child;
                    } else {
                        // This is a file. It is not already in the doc - because
                        // of the map.
                        Element child = doc.createElement("file");
                        child.setAttribute("name", piece);
                        if (filebytes != null) {
                            String contentsName = "resources/layerfile" + Integer.toHexString(filename.hashCode());
                            contents.put(nameSlashes + "/" + contentsName, filebytes);
                            child.setAttribute("url", contentsName);
                        }
                        // Attributes, on every third file:
                        if (Math.abs(filename.hashCode()) % 3 == 0) {
                            Element a = doc.createElement("attr");
                            a.setAttribute("name", "instanceOf");
                            a.setAttribute("stringvalue", name + ".Whatever");
                            child.appendChild(a);
                        }
                        el.appendChild(child);
                    }
                }
            }
            XMLSerializer ser = new XMLSerializer(baos, new OutputFormat(doc, "UTF-8", true));
            ser.serialize(doc);
            String layerName = nameSlashes + "/layer.xml";
            contents.put(layerName, baos.toByteArray());
            attr.putValue("OpenIDE-Module-Layer", layerName);
            OutputStream os = new FileOutputStream(new File(moddirs[types[which]], nameBase + ".jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, mani);
                Set addedDirs = new HashSet(1000); // Set<String>
                it = contents.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    String filename = (String)e.getKey();
                    byte[] filebytes = (byte[])e.getValue();
                    String dircheck = filename;
                    while (true) {
                        int idx = dircheck.lastIndexOf('/');
                        if (idx == -1) break;
                        dircheck = dircheck.substring(0, idx);
                        if (!addedDirs.add(dircheck)) break;
                        JarEntry je = new JarEntry(dircheck + "/");
                        je.setMethod(ZipEntry.STORED);
                        je.setSize(0);
                        je.setCompressedSize(0);
                        je.setCrc(0);
                        jos.putNextEntry(je);
                    }
                    JarEntry je = new JarEntry(filename);
                    je.setMethod(ZipEntry.DEFLATED);
                    //je.setSize(filebytes.length);
                    jos.putNextEntry(je);
                    jos.write(filebytes);
                }
                jos.close();
            } finally {
                os.close();
            }
        }
    }
    
    public void testInitModuleSystem() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            runNB(homedir, userdir, true, (Map)getArgument());
        }
    }
    
    private String cp = refinecp(System.getProperty("java.class.path"));
    
    private void runNB(File homedir, File userdir, boolean log, Map params) throws IOException {
        List cmd = new ArrayList(Arrays.asList(new String[] {
            "java",
            "-Xms24m",
            "-Xmx96m",
            "-Dnetbeans.security.nocheck=true",
            "-Dnetbeans.home=" + homedir.getAbsolutePath(),
            "-Dnetbeans.user=" + userdir.getAbsolutePath(),
            "-Dnetbeans.modules.quiet=true",
            "-Dlog=" + log,
            "-classpath",
            cp,
        }));
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            String key = (String)e.getKey();
            if (key.startsWith("-D")) {
                cmd.add(key + "=" + e.getValue());
            }
        }
        //if (log) cmd.add("-Dorg.netbeans.log.startup=print");
        cmd.add("org.netbeans.core.modules.ModuleInitTest$Main");
        //System.out.println("Running: " + cmd);
        Process p = Runtime.getRuntime().exec((String[])cmd.toArray(new String[cmd.size()]));
        new Copier(p.getInputStream(), System.out).start();
        new Copier(p.getErrorStream(), System.err).start();
        try {
            int stat = p.waitFor();
            if (stat != 0) {
                throw new IOException("Command failed (status " + stat + "): " + cmd);
            }
        } catch (InterruptedException ie) {
            throw new IOException(ie.toString());
        }
    }
    
    /** Remove openide/test/perf/src/ if present since it overrides ErrorManager badly.
     */
    private static String refinecp(String cp) {
        StringBuffer b = new StringBuffer(cp.length());
        StringTokenizer t = new StringTokenizer(cp, File.pathSeparator);
        while (t.hasMoreTokens()) {
            File f = new File(t.nextToken());
            if (f.isDirectory()) {
                if (new File(new File(new File(f, "org"), "openide"), "ErrorManagerTest.java").exists() ||
                        new File(new File(new File(f, "org"), "openide"), "ErrorManagerTest.class").exists()) {
                    //System.err.println("Removing " + f + " from classpath");
                    continue;
                }
            }
            if (b.length() != 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(f);
        }
        return b.toString();
    }
    
    private static final class Copier extends Thread {
        private final InputStream is;
        private final PrintStream ps;
        public Copier(InputStream is, PrintStream ps) {
            this.is = is;
            this.ps = ps;
        }
        @Override
        public void run() {
            try {
                copyStream(is, ps);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public static final class Main {
        public static void main(String[] x) {
            NbTopManager.get();
            if (Boolean.getBoolean("log")) {
                Runtime r = Runtime.getRuntime();
                r.gc();
                double megs = (r.totalMemory() - r.freeMemory()) / 1024.0 / 1024.0;
                System.err.println("Used memory: " + new DecimalFormat("0.00 Mb").format(megs));
            }
        }
    }
    
}
