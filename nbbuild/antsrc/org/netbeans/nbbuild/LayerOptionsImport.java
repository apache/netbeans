/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    List<FileSet> filesets = new ArrayList<FileSet>();

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
        SortedMap<String, String> files = new TreeMap<String, String>(); // layer path -> cnb
        final SortedMap<String, Map<String, String>> attributesMap = new TreeMap<String, Map<String, String>>(); // layer path -> attribute name -> value
        for (FileSet fs : filesets) {
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
                        String cnb = modname.replaceFirst("/\\d+$", "");
                        String layer = mf.getMainAttributes().getValue("OpenIDE-Module-Layer");
                        if (layer != null) {
                            parse(jf.getInputStream(jf.getEntry(layer)), files, cnb, attributesMap);
                        }
                        ZipEntry generatedLayer = jf.getEntry("META-INF/generated-layer.xml");
                        if (generatedLayer != null) {
                            parse(jf.getInputStream(generatedLayer), files, cnb, attributesMap);
                        }
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        try {
            PrintWriter pw = output != null ? new PrintWriter(output) : null;
            for (String path : files.keySet()) {
                if (!path.startsWith("OptionsExport")) {
                    continue;
                }
                Map<String, String> name2value = attributesMap.get(path);
                if (name2value != null) {
                    String cnb = files.get(path);
                    String origin = String.format("#%s %s", cnb, path);
                    if (pw != null) {
                        pw.println(origin);
                    } else {
                        log(origin);
                    }
                    for (String name : name2value.keySet()) {
                        if (name.matches("(in|ex)clude") || name.equals("translate")) {
                            String value = name2value.get(name);
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
                        name2value = new HashMap<String, String>();
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
