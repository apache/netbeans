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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Task to scan all XML layers in a NB installation and write include/exclude
 * patterns under OptionsExport folder to a file (based on LayerIndex.java).
 *
 * @author Jesse Glick, Jiri Skrivanek
 */
public class LayerOptionsImport extends Task {

    public LayerOptionsImport() {
    }
    List<FileSet> filesets = new ArrayList<>();

    public void addConfiguredModules(FileSet fs) {
        filesets.add(fs);
    }
    private File output;

    public void setOutput(File f) {
        output = f;
    }

    @Override
    public void execute() throws BuildException {
        if (filesets.isEmpty()) {
            throw new BuildException();
        }
        SortedMap<String, String> files = new TreeMap<>(); // layer path -> cnb
        final SortedMap<String, Map<String, String>> attributesMap = new TreeMap<>(); // layer path -> attribute name -> value
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
                        String modname = mf.getMainAttributes().getValue("OpenIDE-Module");
                        if (modname == null) {
                            continue;
                        }
                        String cnb = modname.replaceFirst("/\\d+$", "");
                        String layer = mf.getMainAttributes().getValue("OpenIDE-Module-Layer");
                        if (layer != null) {
                            parse(jf.getInputStream(jf.getEntry(layer)), files, cnb, attributesMap);
                        }
                        ZipEntry generatedLayer = jf.getEntry("META-INF/generated-layer.xml");
                        if (generatedLayer != null) {
                            parse(jf.getInputStream(generatedLayer), files, cnb, attributesMap);
                        }
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        try {
            PrintWriter pw = output != null ? new PrintWriter(output) : null;
            for (Map.Entry<String, String> it : files.entrySet()) {
                String path = it.getKey();

                if (!path.startsWith("OptionsExport")) {
                    continue;
                }
                Map<String, String> name2value = attributesMap.get(path);
                if (name2value != null) {
                    String cnb = it.getValue();
                    String origin = String.format("#%s %s", cnb, path);
                    if (pw != null) {
                        pw.println(origin);
                    } else {
                        log(origin);
                    }
                    for (Map.Entry<String, String> entry : name2value.entrySet()) {
                        String name = entry.getKey();

                        if (name.matches("(in|ex)clude") || name.equals("translate")) {
                            String value = entry.getValue();
                            if (value != null && value.length() > 0) {
                                String line = String.format("%s %s", name, value);
                                if (pw != null) {
                                    pw.println(line);
                                } else {
                                    log(line);
                                }
                            }
                        }
                    }
                }
            }
            if (pw != null) {
                pw.close();
            }
        } catch (FileNotFoundException x) {
            throw new BuildException(x, getLocation());
        }
        if (output != null) {
            log(output + ": NetBeans import written");
        }
    }

    private void parse(InputStream is, final Map<String, String> files,
            final String cnb, final Map<String, Map<String, String>> attributesMap) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        f.newSAXParser().parse(is, new DefaultHandler() {

            String prefix = "";

            void register(String path) {
                if (files.containsKey(path)) {
                    files.put(path, null); // >1 owner
                } else {
                    files.put(path, cnb);
                }
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.equals("folder")) {
                    String n = attributes.getValue("name");
                    prefix += n + "/";
                    register(prefix);
                } else if (qName.equals("file")) {
                    String n = attributes.getValue("name");
                    prefix += n;
                    register(prefix);
                } else if (qName.equals("attr") && (attributes.getValue("name").matches("(in|ex)clude")
			|| attributes.getValue("name").equals("translate"))) {
                    String attrName = attributes.getValue("name");
                    String attrValue = attributes.getValue("stringvalue");
		    if(attrName.equals("translate")) {
			prefix = "OptionsExport/".concat(cnb);
			register(prefix);
		    }
                    Map<String, String> name2value = attributesMap.get(prefix);
                    if (name2value == null) {
                        name2value = new HashMap<>();
                        attributesMap.put(prefix, name2value);
                    }
                    name2value.put(attrName, attrValue);
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
        });
    }
}
