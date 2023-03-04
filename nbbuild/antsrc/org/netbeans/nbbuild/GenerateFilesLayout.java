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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Creates a list of files in the distribution.
 */
public class GenerateFilesLayout extends Task {
    
    private FileSet fs;
    public void addConfiguredFiles(FileSet fs) {
        this.fs = fs;
    }
    
    private File output;
    public void setOutput(File output) {
        this.output = output;
    }
    
    public @Override void execute() throws BuildException {
        if (fs == null || output == null) {
            throw new BuildException("Must set files and output");
        }
        DirectoryScanner ds = fs.getDirectoryScanner();
        try {
            Map</*cluster*/String,Map</*path*/String,/*cnb*/String>> ownersByCluster = new HashMap<>();
            int maxlength = 0;
            for (String cluster : ds.getIncludedDirectories()) {
                if (cluster.isEmpty() || cluster.indexOf(File.separatorChar) != -1) {
                    continue;
                }
                Map<String,String> owners = new HashMap<>();
                File updateTracking = new File(ds.getBasedir(), cluster + "/update_tracking");
                if (!updateTracking.isDirectory()) {
                    log("No such dir: " + updateTracking, Project.MSG_WARN);
                    continue;
                }
                for (File xml : updateTracking.listFiles()) {
                    Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, null);
                    String cnb = LayerIndex.shortenCNB(doc.getDocumentElement().getAttribute("codename").replaceFirst("/[0-9]+$", ""));
                    maxlength = Math.max(maxlength, cnb.length());
                    NodeList nl = doc.getElementsByTagName("file");
                    for (int i = 0; i < nl.getLength(); i++) {
                        String file = ((Element) nl.item(i)).getAttribute("name");
                        owners.put(file, cnb);
                    }
                }
                ownersByCluster.put(cluster, owners);
            }
            try (Writer w = new FileWriter(output)) {
                PrintWriter pw = new PrintWriter(w);
                for (String incl : ds.getIncludedFiles()) {
                    String inclSlash = incl.replace(File.separatorChar, '/');
                    int slash = inclSlash.indexOf('/');
                    if (slash == -1) {
                        // Files at top level are ignored.
                        continue;
                    }
                    if (inclSlash.matches("[^/]+/update_tracking/[^/]+[.]xml")) {
                        // Not considered an actual part of the cluster, but rather its metadata.
                        continue;
                    }
                    Map<String,String> owners = ownersByCluster.get(inclSlash.substring(0, slash));
                    String owner = owners != null ? owners.get(inclSlash.substring(slash + 1)) : null;
                    if (owner == null) {
                        owner = "???";
                    }
                    pw.printf("%-" + maxlength + "s %s\n", owner, inclSlash);
                }
                pw.flush();
                pw.close();
            }
        } catch (IOException | SAXException x) {
            throw new BuildException(x, getLocation());
        }
        log(output + ": generated");
    }

}
