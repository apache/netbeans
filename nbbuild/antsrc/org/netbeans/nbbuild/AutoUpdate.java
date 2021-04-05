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

package org.netbeans.nbbuild;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.netbeans.nbbuild.AutoUpdateCatalogParser.ModuleItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AutoUpdate extends Task {
    private List<Modules> modules = new ArrayList<>();
    private FileSet nbmSet;
    private File download;
    private File dir;
    private File cluster;
    private URL catalog;
    private boolean force;

    public void setUpdateCenter(URL u) {
        catalog = u;
    }

    public FileSet createNbms() {
        if (nbmSet != null) {
            throw new BuildException("Just one nbms set allowed");
        }
        nbmSet = new FileSet();
        return nbmSet;
    }

    public void setInstallDir(File dir) {
        this.dir = dir;
    }

    public void setToDir(File dir) {
        this.cluster = dir;
    }
    
    public void setDownloadDir(File dir) {
        this.download = dir;
    }

    /** Forces rewrite even the version of a module is not newer */
    public void setForce(boolean force) {
        this.force = force;
    }

    public Modules createModules() {
        final Modules m = new Modules();
        modules.add(m);
        return m;
    }

    @Override
    public void execute() throws BuildException {
        boolean downloadOnly = false;
        if ((dir != null) == (cluster != null)) {
            if (dir == null && cluster == null && download != null) {
                log("Going to download NBMs only to " + download);
                downloadOnly = true;
            } else {
                throw new BuildException("Specify either todir or installdir");
            }
        }
        Map<String, ModuleItem> units;
        if (catalog != null) {
            try {
                units = AutoUpdateCatalogParser.getUpdateItems(catalog, catalog, this);
            } catch (IOException ex) {
                throw new BuildException(ex.getMessage(), ex);
            }
        } else {
            if (nbmSet == null) {
                throw new BuildException("Specify updatecenter or list of NBMs");
            }
            DirectoryScanner s = nbmSet.getDirectoryScanner(getProject());
            File basedir = s.getBasedir();
            units = new HashMap<>();
            for (String incl : s.getIncludedFiles()) {
                File nbm = new File(basedir, incl);
                try {
                    URL u = new URL("jar:" + nbm.toURI() + "!/Info/info.xml");
                    Map<String, ModuleItem> map;
                    final URL url = nbm.toURI().toURL();
                    try {
                        map = AutoUpdateCatalogParser.getUpdateItems(u, url, this);
                    } catch (FileNotFoundException ex) {
                        JarFile f = new JarFile(nbm);
                        Document doc = XMLUtil.createDocument("module");
                        MakeUpdateDesc.fakeOSGiInfoXml(f, nbm, doc);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        XMLUtil.write(doc, os);
                        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                        map = AutoUpdateCatalogParser.getUpdateItems(u, is, url, this);
                    }
                    assert map.size() == 1;
                    Map.Entry<String, ModuleItem> entry = map.entrySet().iterator().next();
                    units.put(entry.getKey(), entry.getValue().changeDistribution(url));
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }
        }

        Map<String,List<String>> installed;
        if (dir != null) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] arr = dir.listFiles();
            if (arr == null) {
                throw new BuildException("installdir must be existing directory: " + dir);
            }
            installed = findExistingModules(arr);
        } else {
            installed = findExistingModules(cluster);
        }


        for (ModuleItem uu : units.values()) {
            if (!matches(uu.getCodeName(), uu.targetcluster)) {
                continue;
            }
            log("found module: " + uu, Project.MSG_VERBOSE);
            List<String> info = installed.get(uu.getCodeName());
            if (info != null && !uu.isNewerThan(info.get(0))) {
                log("Version " + info.get(0) + " of " + uu.getCodeName() + " is up to date", Project.MSG_VERBOSE);
                if (!force) {
                    continue;
                }
            }

            byte[] bytes = new byte[4096];
            File tmp = null;
            boolean delete = false;
            File lastM = null;
            try {
                if (download == null && uu.getURL().getProtocol().equals("file")) {
                    try {
                        tmp = new File(uu.getURL().toURI());
                    } catch (URISyntaxException ex) {
                        tmp = null;
                    }
                    if (!tmp.exists()) {
                        tmp = null;
                    }
                }
                final String dash = uu.getCodeName().replace('.', '-');
                if (tmp == null) {
                    if (download != null) {
                        tmp = new File(download, dash + ".nbm");
                        String v = readVersion(tmp);
                        if (v != null && !uu.isNewerThan(v)) {
                            log("Version " + v + " of " + tmp + " is up to date", Project.MSG_VERBOSE);
                            if (!force) {
                                continue;
                            }
                        }
                    } else {
                        tmp = File.createTempFile(dash, ".nbm");
                        tmp.deleteOnExit();
                        delete = true;
                    }
                    if (info == null) {
                        log(uu.getCodeName() + " is not present, downloading version " + uu.getSpecVersion(), Project.MSG_INFO);
                    } else {
                        log("Version " + info.get(0) + " of " + uu.getCodeName() + " needs update to " + uu.getSpecVersion(), Project.MSG_INFO);
                    }
                    Get get = new Get();
                    get.setProject(getProject());
                    get.setTaskName("get:" + uu.getCodeName());
                    get.setSrc(uu.getURL());
                    get.setDest(tmp);
                    get.setVerbose(true);
                    get.execute();
                }
                if (downloadOnly) {
                    continue;
                }

                File whereTo;
                if (dir != null) {
                    if (uu.targetcluster == null) {
                        throw new BuildException("Specify todir, not installdir, since " + dash + ".nbm does not define target cluster", getLocation());
                    }
                    whereTo = new File(dir, uu.targetcluster);
                } else {
                    whereTo = cluster;
                }
                whereTo.mkdirs();
                lastM = new File(whereTo, ".lastModified");
                lastM.createNewFile();

                if (info != null) {
                    for (int i = 1; i < info.size(); i++) {
                        File oldFile = new File(whereTo, info.get(i).replace('/', File.separatorChar));
                        oldFile.delete();
                    }
                }

                Document doc = XMLUtil.createDocument("module");
                Element module = doc.getDocumentElement();
                module.setAttribute("codename", uu.getCodeName());
                Element module_version = (Element) module.appendChild(doc.createElement("module_version"));
                module_version.setAttribute("install_time", String.valueOf(System.currentTimeMillis()));
                module_version.setAttribute("last", "true");
                module_version.setAttribute("origin", "Ant"); // XXX set to URL origin
                module_version.setAttribute("specification_version", uu.getSpecVersion());

                try (JarFile zf = new JarFile(tmp)) {
                Manifest manifest = zf.getManifest();
                if (manifest == null || manifest.getMainAttributes().getValue("Bundle-SymbolicName") == null) { // regular NBM
                Enumeration<? extends ZipEntry> en = zf.entries();
                while (en.hasMoreElements()) {
                    ZipEntry zipEntry = en.nextElement();
                    if (!zipEntry.getName().startsWith("netbeans/")) {
                        continue;
                    }
                    if (zipEntry.getName().endsWith("/")) {
                        continue;
                    }
                    String relName = zipEntry.getName().substring(9);
                    File trgt = new File(whereTo, relName.replace('/', File.separatorChar));
                    trgt.getParentFile().mkdirs();
                    log("Writing " + trgt, Project.MSG_VERBOSE);

                    InputStream is = zf.getInputStream(zipEntry);
                    boolean doUnpack200 = false;
                    if(relName.endsWith(".jar.pack.gz") && zf.getEntry(zipEntry.getName().substring(0, zipEntry.getName().length() - 8))==null) {
                        doUnpack200 = true;
                    }
                    OutputStream os;
                    AtomicLong assumedCRC = null;
                    if (relName.endsWith(".external")) {
                        assumedCRC = new AtomicLong();
                        is = externalDownload(is, assumedCRC);
                        if (assumedCRC.longValue() == -1L) {
                            assumedCRC = null;
                        }
                        File dest = new File(trgt.getParentFile(), trgt.getName().substring(0, trgt.getName().length() - 9));
                        os = new FileOutputStream(dest);
                        relName = relName.substring(0, relName.length() - ".external".length());
                    } else {
                        os = new FileOutputStream(trgt);
                    }
                    CRC32 crc = new CRC32();
                    for (;;) {
                        int len = is.read(bytes);
                        if (len == -1) {
                            break;
                        }
                        if(!doUnpack200) {
                            crc.update(bytes, 0, len);
                        }
                        os.write(bytes, 0, len);
                    }
                    is.close();
                    os.close();
                    long crcValue = crc.getValue();
                    if(doUnpack200) {
                        File dest = new File(trgt.getParentFile(), trgt.getName().substring(0, trgt.getName().length() - 8));
                        log("Unpacking " + trgt + " to " + dest, Project.MSG_VERBOSE);
                        unpack200(trgt, dest);
                        trgt.delete();
                        crcValue = getFileCRC(dest);
                        relName = relName.substring(0, relName.length() - 8);
                    }
                    if (assumedCRC != null && assumedCRC.get() != crcValue) {
                        throw new BuildException("Expecting CRC " + assumedCRC.get() + " but was " + crcValue);
                    }
                    Element file = (Element) module_version.appendChild(doc.createElement("file"));
                    file.setAttribute("crc", String.valueOf(crcValue));
                    file.setAttribute("name", relName);
                }
                } else { // OSGi
                    String relName = "config/Modules/" + dash + ".xml";
                    File configModulesXml = new File(whereTo, relName);
                    configModulesXml.getParentFile().mkdirs();
                    try (PrintWriter w = new PrintWriter(configModulesXml)) {
                        w.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n<module name=\"");
                        w.print(uu.getCodeName());
                        w.print("\">\n    <param name=\"autoload\">true</param>\n    <param name=\"eager\">false</param>\n    <param name=\"jar\">modules/");
                        w.print(dash);
                        w.print(".jar</param>\n    <param name=\"reloadable\">false</param>\n</module>\n");
                    }
                    Element file = (Element) module_version.appendChild(doc.createElement("file"));
                    file.setAttribute("crc", String.valueOf(getFileCRC(configModulesXml)));
                    file.setAttribute("name", relName);
                    relName = "modules/" + dash + ".jar";
                    File bundle = new File(whereTo, relName);
                    bundle.getParentFile().mkdirs();
                    FileUtils.getFileUtils().copyFile(tmp, bundle);
                    file = (Element) module_version.appendChild(doc.createElement("file"));
                    file.setAttribute("crc", String.valueOf(getFileCRC(bundle)));
                    file.setAttribute("name", relName);
                }
                }
                File tracking = new File(new File(whereTo, "update_tracking"), dash + ".xml");
                log("Writing tracking file " + tracking, Project.MSG_VERBOSE);
                tracking.getParentFile().mkdirs();
                OutputStream config = new FileOutputStream(tracking);
                try {
                    XMLUtil.write(doc, config);
                } finally {
                    config.close();
                }
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                if (delete && tmp != null) {
                    tmp.delete();
                }
                if (lastM != null) {
                    lastM.setLastModified(System.currentTimeMillis());
                }
            }
        }
    }
    
    private InputStream externalDownload(InputStream is, AtomicLong crc) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        URLConnection conn;
        crc.set(-1L);
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("CRC:")) {
                crc.set(Long.parseLong(line.substring(4).trim()));
            }
            if (line.startsWith("URL:")) {
                String url = line.substring(4).trim();
                for (;;) {
                    int index = url.indexOf("${");
                    if (index == -1) {
                        break;
                    }
                    int end = url.indexOf("}", index);
                    String propName = url.substring(index + 2, end);
                    final String propVal = System.getProperty(propName);
                    if (propVal == null) {
                        throw new IOException("Can't find property " + propName);
                    }
                    url = url.substring(0, index) + propVal + url.substring(end + 1);
                }
                log("Trying external URL: " + url, Project.MSG_INFO);
                try {
                    conn = new URL(url).openConnection();
                    conn.connect();
                    return conn.getInputStream();
                } catch (IOException ex) {
                    log("Cannot connect to " + url, Project.MSG_WARN);
                    try {
                        logThrowable(ex);
                    } catch (LinkageError err) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        throw new IOException("Cannot resolve external references");
    }

    private void logThrowable(IOException ex) {
        log("Details", ex, Project.MSG_VERBOSE);
    }

    
    public static boolean unpack200(File src, File dest) {
        // Copy of ModuleUpdater.unpack200        
        String unpack200Executable = new File(System.getProperty("java.home"),
                "bin/unpack200" + (isWindows() ? ".exe" : "")).getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(unpack200Executable, src.getAbsolutePath(), dest.getAbsolutePath());
        pb.directory(src.getParentFile());
        int result = 1;
        try {
            //maybe reuse start() method here?
            Process process = pb.start();
            //TODO: Need to think of unpack200/lvprcsrv.exe issues
            //https://netbeans.org/bugzilla/show_bug.cgi?id=117334
            //https://netbeans.org/bugzilla/show_bug.cgi?id=119861
            result = process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result == 0;
    }
    private static boolean isWindows() {
        String os = System.getProperty("os.name"); // NOI18N
        return (os != null && os.toLowerCase().startsWith("windows"));//NOI18N
    }

    private static long getFileCRC(File file) throws IOException {
        BufferedInputStream bsrc = null;
        CRC32 crc = new CRC32();
        try {
            bsrc = new BufferedInputStream( new FileInputStream( file ) );
            byte[] bytes = new byte[1024];
            int i;
            while( (i = bsrc.read(bytes)) != -1 ) {
                crc.update(bytes, 0, i );
            }
        }
        finally {
            if ( bsrc != null )
                bsrc.close();
        }
        return crc.getValue();
    }

    private boolean matches(String cnb, String targetCluster) {
        for (Modules ps : modules) {
            if (ps.clusters != null) {
                if (targetCluster == null) {
                    continue;
                }
                if (!ps.clusters.matcher(targetCluster).matches()) {
                    continue;
                }
            }

            if (ps.pattern.matcher(cnb).matches()) {
                return true;
            }
        }
        return false;
    }

    private Map<String,List<String>> findExistingModules(File... clusters) {
        Map<String,List<String>> all = new HashMap<>();
        for (File c : clusters) {
            File mc = new File(c, "update_tracking");
            final File[] arr = mc.listFiles();
            if (arr == null) {
                continue;
            }
            for (File m : arr) {
                try {
                    parseVersion(m, all);
                } catch (Exception ex) {
                    log("Cannot parse " + m, ex, Project.MSG_WARN);
                }
            }
        }
        return all;
    }
    
    private String readVersion(File nbm) {
        String infoXml = "jar:" + nbm.toURI() + "!/Info/info.xml";
        try {
            XPathFactory f = XPathFactory.newInstance();
            Document doc = XMLUtil.parse(new InputSource(infoXml), false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver());
            String res = f.newXPath().evaluate("module/manifest/@OpenIDE-Module-Specification-Version", doc);
            if (res.length() == 0) {
                throw new IOException("Not found tag OpenIDE-Module-Specification-Version!");
            }
            return res;
        } catch (Exception ex) {
            log("Cannot parse " + infoXml + ": " + ex, ex, Project.MSG_WARN);
            return null;
        }
    }

    private void parseVersion(final File config, final Map<String,List<String>> toAdd) throws Exception {
        class P extends DefaultHandler {
            String name;
            List<String> arr;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("module".equals(qName)) {
                    name = attributes.getValue("codename");
                    int slash = name.indexOf('/');
                    if (slash > 0) {
                        name = name.substring(0, slash);
                    }
                    return;
                }
                if ("module_version".equals(qName)) {
                    String version = attributes.getValue("specification_version");
                    if (name == null || version == null) {
                        throw new BuildException("Cannot find version in " + config);
                    }
                    arr = new ArrayList<>();
                    arr.add(version);
                    toAdd.put(name, arr);
                    return;
                }
                if ("file".equals(qName)) {
                    arr.add(attributes.getValue("name"));
                }
            }

            @Override
            public InputSource resolveEntity(String string, String string1) throws IOException, SAXException {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        }
        P p = new P();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(config, p);
    }

    public static final class Modules {
        Pattern pattern;
        Pattern clusters;

        public void setIncludes(String regExp) {
            pattern = Pattern.compile(regExp);
        }

        public void setClusters(String regExp) {
            clusters = Pattern.compile(regExp);
        }
    }
}
