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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ZipResource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Task to scan all XML layers in a NB installation
 * and report on which modules registers which files.
 * @author Jesse Glick
 */
public class LayerIndex extends Task {

    public LayerIndex() {}

    List<FileSet> filesets = new ArrayList<>();
    public void addConfiguredModules(FileSet fs) {
        filesets.add(fs);
    }

    private File output;
    public void setOutput(File f) {
        output = f;
    }

    private File serviceOutput;
    public void setServiceOutput(File f) {
        serviceOutput = f;
    }

    private String resourceId;
    private List<Resource> resources;
    /** If this parameter is provided, then this tasks creates a resource
     * composed from all the layerfiles and makes it accessible under this refId
     * @param id the refId to associate the collection with
     */
    public void setResourceId(String id) {
        resourceId = id;
        resources = new ZipArray();
    }

    @Override
    public void execute() throws BuildException {
        if (filesets.isEmpty()) {
            throw new BuildException();
        }
        SortedMap<String,String> files = new TreeMap<>(); // layer path -> cnb
        SortedMap<String,SortedMap<String,String>> labels = new TreeMap<>(); // layer path -> cnb -> label
        final Map<String,Integer> positions = new TreeMap<>(); // layer path -> position
        SortedMap<String,SortedMap<String,Set<String>>> serviceImpls = new TreeMap<>(); // path -> interface -> [impl]
        Map<String,Integer> servicePositions = new HashMap<>(); // impl -> position
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            for (String path : ds.getIncludedFiles()) {
                File jar = new File(basedir, path);
                try {
                    try (JarFile jf = new JarFile(jar)) {
                        Manifest mf = jf.getManifest();
                        if (mf == null) {
                            continue;
                        }
                        String modname = JarWithModuleAttributes.extractCodeName(mf.getMainAttributes());
                        if (modname == null) {
                            continue;
                        }
                        // XXX services.txt has e.g. "SERVICE org.openide.filesystems.MIMEResolver\n PROVIDER org.netbeans.modules.java.hints.test.Utilities$JavaMimeResolver"
                        // which is misleading since this pseudomodule is used only in unit tests
                        // maybe define Normally-Disabled: true in manifest.mf and skip from here (and disabledAutoloads)?
                        String cnb = modname.replaceFirst("/\\d+$", "");
                        String layer = mf.getMainAttributes().getValue("OpenIDE-Module-Layer");
                        if (layer != null) {
                            if (resources != null) {
                                ZipResource res = new LayerResource(jar, layer, layer.replaceFirst("/[^/]+$", "").replace('/', '.') + ".xml");
                                resources.add(res);
                            } else {
                                parse(jf.getInputStream(jf.getEntry(layer)), files, labels, positions, cnb, jf);
                            }
                        }
                        ZipEntry generatedLayer = jf.getEntry("META-INF/generated-layer.xml");
                        if (generatedLayer != null) {
                            if (resources != null) {
                                ZipResource res = new LayerResource(jar, generatedLayer.getName(), cnb + "-generated.xml");
                                resources.add(res);
                            } else {
                                parse(jf.getInputStream(generatedLayer), files, labels, positions, cnb + "@", jf);
                            }
                        }
                        if (serviceOutput != null) {
                            // Could remember CNBs too.
                            parseServices(jf, serviceImpls, servicePositions);
                        }
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        if (resources != null) {
            assignReferences();
            return;
        }

        try {
            writeLayerIndex(files, positions, labels);
            if (serviceOutput != null) {
                writeServiceIndex(serviceImpls, servicePositions);
            }
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
    }

    @SuppressWarnings("unchecked")
    private void assignReferences() {
        getProject().getReferences().put(resourceId, resources);
    }

    static String shortenCNB(String cnb) {
        if (cnb != null) {
            return cnb.replaceFirst("^org\\.netbeans\\.", "o.n.").replaceFirst("^org\\.openide\\.", "o.o.").replaceFirst("\\.modules\\.", ".m.");
        } else {
            return "";
        }
    }

    private String shortenPath(String path) {
        return path.replaceAll("(^|/)org-netbeans-", "$1o-n-").replaceAll("(^|/)org-openide-", "$1o-o-").replaceAll("-modules-", "-m-")
                .replaceAll("(^|/)org\\.netbeans\\.", "$1o.n.").replaceAll("(^|/)org\\.openide\\.", "$1o.o.").replaceAll("\\.modules\\.", ".m.");
    }

    private void parse(InputStream is, final Map<String,String> files, final SortedMap<String,SortedMap<String,String>> labels,
            final Map<String,Integer> positions, final String cnb, final JarFile jf) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        f.newSAXParser().parse(is, new DefaultHandler() {
            String prefix = "";
            void register(String path) {
                if (!files.containsKey(path)) {
                    files.put(path, cnb);
                } else if (!cnb.equals(files.get(path))) {
                    // Possibly >1 owner, but consider layer.xml vs. generated-layer.xml.
                    if (cnb.equals(files.get(path) + "@")) {
                        // leave alone
                    } else if ((cnb + "@").equals(files.get(path))) { // mark as defined in layer.xml
                        files.put(path, cnb);
                    } else { // different modules
                        files.put(path, null);
                    }
                }
            }
            @Override
            public void startElement(String uri, String localName, String qName,  Attributes attributes) throws SAXException {
                if (qName.equals("folder")) {
                    String n = attributes.getValue("name");
                    prefix += n + "/";
                    register(prefix);
                } else if (qName.equals("file")) {
                    String n = attributes.getValue("name");
                    prefix += n;
                    register(prefix);
                } else if (qName.equals("attr") && attributes.getValue("name").equals("SystemFileSystem.localizingBundle")) {
                    String bundle = attributes.getValue("stringvalue");
                    if (bundle != null) {
                        loadDisplayName(bundle,prefix.replaceAll("/$", ""));
                    } else {
                        log("No stringvalue for SystemFileSystem.localizingBundle on " + prefix + " in " + cnb, Project.MSG_WARN);
                    }
                } else if (qName.equals("attr") && attributes.getValue("name").equals("displayName")) {
                    String bundleKey = attributes.getValue("bundlevalue");
                    if (bundleKey != null) {
                        String[] bundlevalue = bundleKey.split("#", 2);
                        loadDisplayName(bundlevalue[0], bundlevalue[1]);
                    } else {
                        String literal = attributes.getValue("stringvalue");
                        if (literal != null) {
                            loadDisplayName(literal);
                        }
                    }
                } else if (qName.equals("attr") && attributes.getValue("name").equals("position")) {
                    String intvalue = attributes.getValue("intvalue");
                    if (intvalue != null && /* #107550 */ !intvalue.equals("0")) {
                        try {
                            positions.put(prefix, Integer.parseInt(intvalue));
                        } catch (NumberFormatException x) {
                            throw new SAXException(x);
                        }
                    }
                }
            }
            private void loadDisplayName(String bundle, String key) throws SAXException {
                Properties props = new Properties();
                try {
                    ZipEntry entry = jf.getEntry(bundle.replace('.', '/') + ".properties");
                    if (entry == null) {
                        /* Should be covered by ValidateLayerConsistencyTest.testLocalizingBundles:
                        log(bundle + " not found in reference from " + prefix + " in " + cnb, Project.MSG_WARN);
                         */
                        return;
                    }
                    props.load(jf.getInputStream(entry));
                } catch (IOException x) {
                    throw new SAXException(x);
                }
                String label = props.getProperty(key);
                if (label == null) {
                    /* Should be covered by ValidateLayerConsistencyTest.testLocalizingBundles:
                    log("Key " + key + " not found in " + bundle + " from " + cnb, Project.MSG_WARN);
                     */
                    return;
                }
                loadDisplayName(label);
            }
            private void loadDisplayName(String label) {
                SortedMap<String,String> cnb2label = labels.get(prefix);
                if (cnb2label == null) {
                    cnb2label = new TreeMap<>();
                    labels.put(prefix, cnb2label);
                }
                cnb2label.put(cnb, label);
            }
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (qName.equals("folder")) {
                    prefix = prefix.replaceFirst("[^/]+/$", "");
                } else if (qName.equals("file")) {
                    prefix = prefix.replaceFirst("[^/]+$", "");
                }
            }
            @Override
            public InputSource resolveEntity(String pub, String sys) throws IOException, SAXException {
                return new InputSource(new StringReader(""));
            }
        });
    }

    private void parseServices(JarFile jf, SortedMap<String,SortedMap<String,Set<String>>> serviceImplsByPath,
            Map<String,Integer> servicePositions) throws IOException {
        Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            String path, xface;
            if (name.startsWith("META-INF/services/")) {
                path = "";
                xface = name.substring("META-INF/services/".length());
            } else if (name.startsWith("META-INF/namedservices/")) {
                String rest = name.substring("META-INF/namedservices/".length());
                int x = rest.lastIndexOf('/');
                path = rest.substring(0, x);
                xface = rest.substring(x + 1);
            } else {
                continue;
            }
            try (InputStream is = jf.getInputStream(entry)) {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String lastImpl = null;
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("#position=") && lastImpl != null) {
                        servicePositions.put(lastImpl, Integer.parseInt(line.substring("#position=".length())));
                    } else if (line.startsWith("#-") || (line.length() > 0 && !line.startsWith("#"))) {
                        lastImpl = line;
                        SortedMap<String,Set<String>> serviceImpls = serviceImplsByPath.get(path);
                        if (serviceImpls == null) {
                            serviceImpls = new TreeMap<>();
                            serviceImplsByPath.put(path, serviceImpls);
                        }
                        Set<String> impls = serviceImpls.get(xface);
                        if (impls == null) {
                            impls = new HashSet<>();
                            serviceImpls.put(xface, impls);
                        }
                        impls.add(lastImpl);
                    }
                }
            }
        }
    }

    private static final class ZipArray extends ArrayList<Resource>
    implements ResourceCollection {
        public boolean isFilesystemOnly() {
            return false;
        }

        @Override
        public Stream<Resource> stream() {
            return super.stream();
        }
    }

    private static final class LayerResource extends ZipResource {
        private final String name;
        
        private LayerResource(
            File jar, String path, String name) throws ZipException {
            super(jar, "UTF-8", new org.apache.tools.zip.ZipEntry(path));
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            final ZipFile z = new ZipFile(getZipfile(), ZipFile.OPEN_READ);
            ZipEntry ze = z.getEntry(super.getName());
            if (ze == null) {
                z.close();
                throw new BuildException("no entry " + getName() + " in "
                                         + getArchive());
            }
            return z.getInputStream(ze);
        }

    }

    private void writeLayerIndex(SortedMap<String,String> files, final Map<String,Integer> positions,
            SortedMap<String,SortedMap<String,String>> labels) throws IOException {
        int maxlength = 0;
        for (String cnb : files.values()) {
            maxlength = Math.max(maxlength, shortenCNB(cnb).length());
        }
        PrintWriter pw = output != null ? new PrintWriter(output, "UTF-8") : null;
        Map<String,String> virtualEntries = computeMIMELookupEntries(files.keySet());
        updateMap(files, virtualEntries);
        updateMap(positions, virtualEntries);
        updateMap(labels, virtualEntries);
        SortedSet<String> layerPaths = new TreeSet<>(new LayerPathComparator(positions));
        layerPaths.addAll(files.keySet());
        SortedSet<String> remaining = new TreeSet<>(files.keySet());
        remaining.removeAll(layerPaths);
        assert remaining.isEmpty() : remaining;
        for (String path : layerPaths) {
            String cnb = files.get(path);
            String line = String.format("%-" + maxlength + "s %s", shortenCNB(cnb), shortenPath(path));
            if (virtualEntries.containsKey(path)) {
                line += " (merged)";
            }
            Integer pos = positions.get(path);
            if (pos != null) {
                line += String.format(" @%d", pos);
            }
            SortedMap<String,String> cnb2Label = labels.get(path);
            if (cnb2Label != null) {
                if (cnb2Label.size() == 1 && cnb2Label.keySet().iterator().next().equals(cnb)) {
                    line += String.format(" (\"%s\")", cnb2Label.values().iterator().next());
                } else {
                    for (Map.Entry<String,String> labelEntry : cnb2Label.entrySet()) {
                        line += String.format(" (%s: \"%s\")", shortenCNB(labelEntry.getKey()), labelEntry.getValue());
                    }
                }
            }
            if (pw != null) {
                pw.println(line);
            } else {
                log(line);
            }
        }
        if (pw != null) {
            pw.close();
        }
        if (output != null) {
            log(output + ": layer index written");
        }
    }

    /**
     * Map from virtual file paths to original literal file path.
     * E.g. Editors/text/html/Popup/foo.instance -> Editors/Popup/foo.instance
     * See ValidateLayerConsistencyTest.testFolderOrdering for comparison.
     */
    private Map<String,String> computeMIMELookupEntries(Set<String> files) {
        Pattern editorFolderPattern = Pattern.compile("Editors/(application|text)/([^/]+)(.*/)");
        Map<String,String> result = new HashMap<>();
        for (String editorFolder : files) {
            Matcher m = editorFolderPattern.matcher(editorFolder);
            if (!m.matches()) {
                continue;
            }
            // $0="Editors/text/html/Popup/" $1="text" $2="html" $3="/Popup/"
            List<String> prefixen = new ArrayList<>(2);
            prefixen.add("Editors" + m.group(3)); // "Editors/Popup/"
            if (m.group(2).endsWith("+xml")) { // Editors/text/x-ant+xml/Popup/
                prefixen.add("Editors/" + m.group(1) + "/xml" + m.group(3)); // Editors/text/xml/Popup/
            }
            for (String prefix : prefixen) {
                for (String file : files) {
                    if (file.startsWith(prefix)) { // "Editors/Popup/foo.instance"
                        String basename = file.substring(prefix.length());
                        if (basename.contains("/")) {
                            // Would technically be correct to show, but usually irrelevant.
                            continue;
                        }
                        String virtual = editorFolder + basename; // Editors/text/html/Popup/foo.instance
                        if (!files.contains(virtual)) {
                            result.put(virtual, file);
                        }
                    }
                }
            }
        }
        return result;
    }

    private <T> void updateMap(Map<String,T> map, Map<String,String> virtualEntries) {
        for (Map.Entry<String,String> entry : virtualEntries.entrySet()) {
            String orig = entry.getValue();
            if (map.containsKey(orig)) {
                map.put(entry.getKey(), map.get(orig));
            }
        }
    }

    private static class LayerPathComparator implements Comparator<String> {
        private final Map<String,Integer> positions;
        public LayerPathComparator(Map<String,Integer> positions) {
            this.positions = positions;
        }
        public int compare(String p1, String p2) {
            StringTokenizer tok1 = new StringTokenizer(p1, "/");
            StringTokenizer tok2 = new StringTokenizer(p2, "/");
            String prefix = "";
            while (tok1.hasMoreTokens()) {
                String piece1 = tok1.nextToken();
                if (tok2.hasMoreTokens()) {
                    String piece2 = tok2.nextToken();
                    if (piece1.equals(piece2)) {
                        prefix += piece1 + "/";
                    } else {
                        Integer pos1 = pos(prefix + piece1);
                        Integer pos2 = pos(prefix + piece2);
                        if (pos1 == null) {
                            if (pos2 == null) {
                                return piece1.compareTo(piece2);
                            } else {
                                return 1;
                            }
                        } else {
                            if (pos2 == null) {
                                return -1;
                            } else {
                                int diff = pos1 - pos2;
                                if (diff != 0) {
                                    return diff;
                                } else {
                                    return piece1.compareTo(piece2);
                                }
                            }
                        }
                    }
                } else {
                    return 1;
                }
            }
            if (tok2.hasMoreTokens()) {
                return -1;
            }
            assert p1.equals(p2) : p1 + " vs. " + p2;
            return 0;
        }
        Integer pos(String path) {
            return positions.containsKey(path) ? positions.get(path) : positions.get(path + "/");
        }
    }

    private void writeServiceIndex(SortedMap<String,SortedMap<String,Set<String>>> serviceImpls,
            final Map<String,Integer> servicePositions) throws IOException {
        try (PrintWriter pw = new PrintWriter(serviceOutput, "UTF-8")) {
            for (Map.Entry<String,SortedMap<String,Set<String>>> mainEntry : serviceImpls.entrySet()) {
                String path = mainEntry.getKey();
                for (Map.Entry<String,Set<String>> entry : mainEntry.getValue().entrySet()) {
                    pw.print("SERVICE " + entry.getKey());
                    if (path.length() > 0) {
                        pw.println(" under " + path);
                    } else {
                        pw.println();
                    }
                    SortedSet<String> impls = new TreeSet<>(new ServiceComparator(servicePositions));
                    impls.addAll(entry.getValue());
                    Set<String> masked = new HashSet<>();
                    for (String impl : impls) {
                        if (impl.startsWith("#-")) {
                            masked.add(impl);
                            masked.add(impl.substring(2));
                        }
                    }
                    impls.removeAll(masked);
                    for (String impl : impls) {
                        if (servicePositions.containsKey(impl)) {
                            impl += " @" + servicePositions.get(impl);
                        }
                        pw.println("  PROVIDER " + impl);
                    }
                }
            }
        }
        log(serviceOutput + ": service index written");
    }

    private static class ServiceComparator implements Comparator<String> {
        private final Map<String,Integer> servicePositions;
        public ServiceComparator(Map<String,Integer> servicePositions) {
            this.servicePositions = servicePositions;
        }
        public int compare(String i1, String i2) {
            Integer pos1 = servicePositions.get(i1);
            Integer pos2 = servicePositions.get(i2);
            if (pos1 == null) {
                if (pos2 == null) {
                    return i1.compareTo(i2);
                } else {
                    return 1;
                }
            } else {
                if (pos2 == null) {
                    return -1;
                } else {
                    int diff = pos1 - pos2;
                    if (diff != 0) {
                        return diff;
                    } else {
                        return i1.compareTo(i2);
                    }
                }
            }
        }
    }

}
