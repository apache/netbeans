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
package org.netbeans.modules.ide.ergonomics.ant;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.BaseFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.taskdefs.Concat;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.StringResource;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.zip.ZipEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Extracts icons and bundles from layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ExtractLayer extends Task
implements FileNameMapper, URIResolver, EntityResolver {
    private List<FileSet> moduleSet = new ArrayList<FileSet>();
    public void addConfiguredModules(FileSet fs) {
        moduleSet.add(fs);
    }
    private List<FileSet> entries = new ArrayList<FileSet>();
    public void addConfiguredEntries(FileSet fs) {
        entries.add(fs);
    }

    private File output;
    public void setDestDir(File f) {
        output = f;
    }
    private File bundle;
    public void setBundle(File f) {
        bundle = f;
    }
    private String clusterName;
    public void setClusterName(String n) {
        clusterName = n;
    }
    private FilterChain bundleFilter;
    public void addConfiguredBundleFilter(FilterChain b) {
        bundleFilter = b;
    }

    private File badgeFile;
    public void setBadgeIcon(File f) {
        badgeFile = f;
    }

    @Override
    public void execute() throws BuildException {
        if (moduleSet.isEmpty()) {
            throw new BuildException();
        }
        if (output == null) {
            throw new BuildException();
        }
        if (clusterName == null) {
            throw new BuildException();
        }
        BufferedImage badgeIcon;
        try {
            badgeIcon = badgeFile == null ? ImageIO.read(ExtractLayer.class.getResourceAsStream("badge.png")) : ImageIO.read(badgeFile);
        } catch (IOException ex) {
            throw new BuildException("Error reading " + badgeFile, ex);
        }

        Transformer ft;
        Transformer rt;
        Transformer et;
        Transformer bt;


        try {
            StreamSource fullpaths;
            StreamSource relative;
            StreamSource entryPoints;
            StreamSource bundleEntryPoints;
            URL fu = ExtractLayer.class.getResource("full-paths.xsl");
            URL ru = ExtractLayer.class.getResource("relative-refs.xsl");
            URL eu = ExtractLayer.class.getResource("entry-points.xsl");
            URL bu = ExtractLayer.class.getResource("entry-points-to-bundle.xsl");
            fullpaths = new StreamSource(fu.openStream());
            relative = new StreamSource(ru.openStream());
            entryPoints = new StreamSource(eu.openStream());
            bundleEntryPoints = new StreamSource(bu.openStream());

            SAXTransformerFactory fack;
            fack = (SAXTransformerFactory)TransformerFactory.newInstance();
            assert Boolean.TRUE.equals(fack.getFeature(SAXTransformerFactory.FEATURE));
            fack.setURIResolver(this);

            ft = fack.newTransformer(fullpaths);
            rt = fack.newTransformer(relative);
            rt.setParameter("cluster.name", clusterName);
            et = fack.newTransformer(entryPoints);
            et.setParameter("cluster.name", clusterName);
            bt = fack.newTransformer(bundleEntryPoints);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }

        StringBuilder modules = new StringBuilder();
        String sep = "\n    ";
        ByteArrayOutputStream uberLayer = new ByteArrayOutputStream();
        try {
            uberLayer.write("<?xml version='1.0' encoding='UTF-8'?>\n".getBytes(StandardCharsets.UTF_8));
            uberLayer.write("<filesystem>\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException iOException) {
            throw new BuildException(iOException);
        }
        ByteArrayOutputStream bundleHeader = new ByteArrayOutputStream();
        StreamResult bundleOut = new StreamResult(bundleHeader);
        StreamResult uberOut = new StreamResult(uberLayer);
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        for (FileSet fs : moduleSet) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            for (String path : ds.getIncludedFiles()) {
                File jar = new File(basedir, path);
                try {
                    JarFile jf = new JarFile(jar);
                    try {
                        Manifest mf = jf.getManifest();
                        if (mf == null) {
                            continue;
                        }
                        String modname = mf.getMainAttributes().getValue("OpenIDE-Module");
                        if (modname == null) {
                            continue;
                        }
                        String skip = mf.getMainAttributes().getValue("FeaturesOnDemand-Proxy-Layer");
                        if ("false".equals(skip)) {
                            continue;
                        }
                        String show = mf.getMainAttributes().getValue("AutoUpdate-Show-In-Client");
                        String base = modname.replaceFirst("/[0-9]+$", "");
                        if (!"false".equals(show)) {
                            modules.append(sep).append(base);
                            sep = ",\\\n    ";
                        }

                        String mflayer = mf.getMainAttributes().getValue("OpenIDE-Module-Layer");
                        if (mflayer != null) {
                            String n = mflayer.replaceFirst("/[^/]+$", "").replace('/', '.') + ".xml";
                            et.setParameter("filename", n);
                            et.transform(createSource(jf, jf.getEntry(mflayer)), uberOut);
                            bt.transform(createSource(jf, jf.getEntry(mflayer)), bundleOut);
                        }
                        java.util.zip.ZipEntry generatedLayer = jf.getEntry("META-INF/generated-layer.xml");
                        if (generatedLayer != null) {
                            et.setParameter("filename", base + "-generated.xml");
                            et.transform(createSource(jf, generatedLayer), uberOut);
                            bt.transform(createSource(jf, generatedLayer), bundleOut);
                        }

                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        Pattern concatPattern;
        Pattern copyPattern;
        String uberText = null;
        byte[] uberArr = null;
        DuplKeys duplKeys = null;
        try {
            uberLayer.write("</filesystem>\n".getBytes(StandardCharsets.UTF_8));
            uberText = uberLayer.toString("UTF-8");
            uberArr = uberLayer.toByteArray();
            log("uberLayer for " + clusterName + "\n" + uberText, Project.MSG_VERBOSE);
            
            Set<String> concatregs = new TreeSet<String>();
            Set<String> copyregs = new TreeSet<String>();
            Map<String,String> keys = new TreeMap<String,String>();
            parse(new ByteArrayInputStream(uberArr), concatregs, copyregs, keys);

            log("Concats: " + concatregs, Project.MSG_VERBOSE);
            log("Copies : " + copyregs, Project.MSG_VERBOSE);

            StringBuilder sb = new StringBuilder();
            sep = "";
            for (String s : concatregs) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            concatPattern = Pattern.compile(sb.toString());

            sb = new StringBuilder();
            sep = "";
            for (String s : copyregs) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            copyPattern = Pattern.compile(sb.toString());

            duplKeys = new DuplKeys(keys.keySet());
        } catch (Exception ex) {
            throw new BuildException("Cannot parse layers: " + ex.getMessage(), ex);
        }
        Map<String,ResArray> bundles = new HashMap<String,ResArray>();
        bundles.put("", new ResArray());
        ResArray icons = new ResArray();

        for (FileSet fs : entries == null ? moduleSet : entries) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            for (String path : ds.getIncludedFiles()) {
                File jar = new File(basedir, path);
                try {
                    JarFile jf = new JarFile(jar);
                    try {
                        Enumeration<JarEntry> en = jf.entries();
                        while (en.hasMoreElements()) {
                            JarEntry je = en.nextElement();
                            if (concatPattern.matcher(je.getName()).matches()) {
                                ZipEntry zipEntry = new ZipEntry(je);
                                String noExt = je.getName().replaceFirst("\\.[^\\.]*$", "");
                                int index = noExt.indexOf("_");
                                String suffix = index == -1 ? "" : noExt.substring(index + 1);
                                ResArray ra = bundles.get(suffix);
                                if (ra == null) {
                                    ra = new ResArray();
                                    bundles.put(suffix, ra);
                                }
                                ra.add(new ZipResource(jar, "UTF-8", zipEntry));
                                ra.add(new StringResource("\n\n"));
                            }
                            if (copyPattern.matcher(je.getName()).matches()) {
                                ZipEntry zipEntry = new ZipEntry(je);
                                Resource zr = new ZipResource(jar, "UTF-8", zipEntry);
                                if (badgeIcon != null) {
                                    icons.add(new IconResource(zr, badgeIcon));
                                } else {
                                    icons.add(zr);
                                }
                            }
                        }
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        for (Map.Entry<String, ResArray> entry : bundles.entrySet()) {
            ResArray ra = entry.getValue();
            
            Concat concat = new Concat();
            concat.setProject(getProject());
            ra.add(new StringResource(""));
            concat.add(ra);
            concat.setDestfile(localeVariant(bundle, entry.getKey()));
            {
                FilterChain ch = new FilterChain();
                ch.add(duplKeys);
                concat.addFilterChain(ch);
                concat.addFilterChain(bundleFilter);
            }
            Concat.TextElement te = new Concat.TextElement();
            te.setProject(getProject());
            te.addText("\n\n\ncnbs=\\" + modules + "\n\n");
            te.setFiltering(false);
            String antProjects = new String(bundleHeader.toByteArray(), StandardCharsets.UTF_8);
            te.addText(antProjects + "\n\n");
            concat.addFooter(te);
            concat.execute();
        }

        {
            HashMap<String,Resource> names = new HashMap<String,Resource>();
            HashSet<String> duplicates = new HashSet<String>();
            for (Resource r : icons) {
                String name = r.getName();
                Resource prev = names.put(name, r);
                if (prev != null) {
                    if (prev.getName().equals(r.getName())) {
                        continue;
                    }
                    duplicates.add(r.getName());
                    duplicates.add(prev.getName());
                }
            }
            if (!duplicates.isEmpty()) {
                throw new BuildException("Duplicated resources are forbidden: " + duplicates.toString().replace(',', '\n'));
            }
        }

        Copy copy = new Copy();
        copy.setProject(getProject());
        copy.add(icons);
        copy.setTodir(output);
        copy.add(this);
        copy.execute();

        try {
            StreamSource orig = new StreamSource(new ByteArrayInputStream(uberArr));
            DOMResult tmpRes = new DOMResult();
            ft.transform(orig, tmpRes);

            Node filesystem = tmpRes.getNode().getFirstChild();
            String n = filesystem.getNodeName();
            assert n.equals("filesystem") : n;
            if (filesystem.getChildNodes().getLength() > 0) {
                DOMSource tmpSrc = new DOMSource(tmpRes.getNode());
                StreamResult gen = new StreamResult(new File(output, "layer.xml"));
                rt.transform(tmpSrc, gen);
            }
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }


    private void parse(
        final InputStream is,
        final Set<String> concat, final Set<String> copy,
        final Map<String,String> additionalKeys
    ) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        f.newSAXParser().parse(is, new DefaultHandler() {
            String prefix = "";
            @Override
            public void startElement(String uri, String localName, String qName,  Attributes attributes) throws SAXException {
                if (qName.equals("folder")) {
                    String n = attributes.getValue("name");
                    prefix += n + "/";
                } else if (qName.equals("file")) {
                    String n = attributes.getValue("name");
                    addResource(attributes.getValue("url"), true);
                    prefix += n;
                } else if (qName.equals("attr")) {
                    String name = attributes.getValue("name");
                    if (name.equals("SystemFileSystem.localizingBundle")) {
                        String bundlepath = attributes.getValue("stringvalue").replace('.', '/') + ".*properties";
                        concat.add(bundlepath);
			String key;
                        if (prefix.endsWith("/")) {
			    key = prefix.substring(0, prefix.length() - 1);
                        } else {
                            key = prefix;
                        }
			additionalKeys.put(key, bundlepath);
                    } else if (name.equals("iconResource") || name.equals("iconBase")) {
                        String s = attributes.getValue("stringvalue");
                        if (s == null) {
                            throw new BuildException("No stringvalue attribute for " + name);
                        }
                        addResource("nbresloc:" + s, false);
                    } else if (attributes.getValue("bundlevalue") != null) {
                        String bundlevalue = attributes.getValue("bundlevalue");
                        int idx = bundlevalue.indexOf('#');
                        String bundle = bundlevalue.substring(0, idx);
                        String key = bundlevalue.substring(idx + 1);
                        String bundlepath = bundle.replace('.', '/') + ".*properties";

			String prev = additionalKeys.put(key, bundle);

                        if (prev != null && !bundle.equals(prev)) {
                            throw new IllegalStateException("key " + key + " from " + bundlepath + " was already defined among " + prev);
                        }
                        concat.add(bundlepath);
                    } else {
                        addResource(attributes.getValue("urlvalue"), false);
                    }
                }
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

            private void addResource(String url, boolean localAllowed) throws BuildException {
                if (url == null) {
                    return;
                }
                if (url.startsWith("nbres:")) {
                    url = "nbresloc:" + url.substring(6);
                }
                final String prfx = "nbresloc:";
                if (!url.startsWith(prfx)) {
                    if (localAllowed) {
                        if (url.startsWith("/")) {
                            copy.add(url.substring(1));
                        } else {
                            copy.add(".*/" + url);
                        }
                        return;
                    } else {
                        throw new BuildException("Unknown urlvalue was: " + url);
                    }
                } else {
                    url = url.substring(prfx.length());
                    if (url.startsWith("/")) {
                        url = url.substring(1);
                    }
                }
                url = url.replaceFirst("(\\.[^\\.])+$*", ".*$1");
                copy.add(url);
            }
        });
    }

    private static File localeVariant(File base, String locale) {
        if (locale.length() == 0) {
            return base;
        }
        String name = base.getName().replaceFirst("\\.", "_" + locale + ".");
        return new File(base.getParentFile(), name);
    }

    public void setFrom(String arg0) {
    }

    public void setTo(String arg0) {
    }

    /** Dash instead of slash file mapper */
    public String[] mapFileName(String fileName) {
        return new String[] { fileName.replace('/', '-') };
    }

    public Source resolve(String href, String base) throws TransformerException {
        return null;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    private Source createSource(JarFile jf, java.util.zip.ZipEntry entry)  {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setValidating(false);
            DocumentBuilder b = f.newDocumentBuilder();
            b.setEntityResolver(this);
            Document doc = b.parse(jf.getInputStream(entry));
            return new DOMSource(doc);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    private static final class ResArray implements ResourceCollection {
        private final List<Resource> delegate = new ArrayList<>();

        public boolean isFilesystemOnly() {
            return false;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        public Stream<? extends Resource> stream() {
            return delegate.stream();
        }

        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Iterator<Resource> iterator() {
            return delegate.iterator();
        }

        @Override
        public void forEach(Consumer<? super Resource> action) {
            delegate.forEach(action);
        }

        @Override
        public Spliterator<Resource> spliterator() {
            return delegate.spliterator();
        }

        public boolean add(Resource r) {
            return delegate.add(r);
        }
    }

    private class DuplKeys extends BaseFilterReader
    implements ChainableReader {
        private final Set<String> acceptKeys;
        private Map<String,String> map;
        private String line;
        private int lineIdx;
        
        public DuplKeys(Set<String> acceptKeys) {
            this.acceptKeys = acceptKeys;
        }

        public DuplKeys(Reader in, Set<String> acceptKeys) {
            super(new BufferedReader(in));
            this.acceptKeys = acceptKeys;
        }

        @Override
        public Reader chain(Reader rdr) {
            return new DuplKeys(rdr, acceptKeys);
        }
        
        private BufferedReader in() {
           return (BufferedReader) in;
        }

        @Override
        public int read() throws IOException {
            int equals;
            String key;
            
            for (;;) {
                if (line != null) {
                    final int len = line.length();
                    if (lineIdx < len) {
                        return line.charAt(lineIdx++);
                    } else {
                        if (len > 0 && line.charAt(len - 1)  == '\\') {
                            line = in().readLine();
                            lineIdx = 0;
                        } else {
                            line = null;
                        }
                        return '\n';
                    }
                }
                do { 
                    line = in().readLine();
                    if (line == null) {
                        return -1;
                    }
                } while (line.startsWith("#"));
                lineIdx = 0;

                equals = line.indexOf('=');
                if (equals == -1) {
                    line = null;
                    continue;
                }
                key = line.substring(0, equals).trim();
                if (!acceptKeys.contains(key)) {
                    line = null;
                    continue;
                }
                final String value = line.substring(equals + 1);
                if (map == null) {
                    map = new HashMap<String, String>();
                }
                if (map.containsKey(key)) {
                    final String oldValue = map.get(key);
                    if (!value.equals(oldValue)) {
                        final String msg = "The key " + key + 
                            " is duplicated and values are not identical: '" + 
                            value + "' and '" + oldValue + "'!";
                        throw new BuildException(msg);
                    }
                    // ignore the line
                    line = null;
                    continue;
                }
                map.put(key, value);
                continue;
            }
        }
        
        
    }
}
