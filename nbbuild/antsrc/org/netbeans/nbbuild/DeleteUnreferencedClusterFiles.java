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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Removes files from clusters which are not listed as belonging to any NBM.
 * @see <a href="http://www.netbeans.org/nonav/issues/show_bug.cgi?id=111946">issue #111946</a>
 */
public class DeleteUnreferencedClusterFiles extends Task {

    private DirSet clusters;
    /**
     * Set of cluster directories to scan.
     * Any dir lacking an update_tracking subdir is automatically ignored.
     */
    public void addConfiguredClusters(DirSet clusters) {
        this.clusters = clusters;
    }
    
    private PatternSet patterns;
    /**
     * Option to remove files from those checks
     */
    public void addConfiguredSelection(PatternSet p) {
        patterns = p;
    }

    private File report;
    /** Write any errors to a JUnit report rather than halting build. */
    public void setReport(File report) {
        this.report = report;
    }

    public @Override void execute() throws BuildException {
        StringBuilder missingFiles = new StringBuilder();
        StringBuilder extraFiles = new StringBuilder();
        StringBuilder duplicatedFiles = new StringBuilder();
        for (String incl : clusters.getDirectoryScanner().getIncludedDirectories()) {
            File cluster = new File(clusters.getDir(), incl);
            File updateTracking = new File(cluster, "update_tracking");
            if (!updateTracking.isDirectory()) {
                continue;
            }
            Map</*path*/String,/*CNB*/String> files = new HashMap<>();
            for (File module : updateTracking.listFiles()) {
                if (!module.getName().endsWith(".xml")) {
                    continue;
                }
                try {
                    Document doc = XMLUtil.parse(new InputSource(module.toURI().toString()), false, false, null, null);
                    String cnb = doc.getDocumentElement().getAttribute("codename").replaceFirst("/[0-9]+$", "");
                    NodeList nl = doc.getElementsByTagName("file");
                    for (int i = 0; i < nl.getLength(); i++) {
                        String file = ((Element) nl.item(i)).getAttribute("name");
                        if (new File(cluster, file).isFile()) {
                            String prev = files.put(file, cnb);
                            if (prev != null) {
                                duplicatedFiles.append("\ntwo registrations of the same file: " + file + " (from " + prev + " and " + cnb + ")");
                            }
                        } else {
                            missingFiles.append("\n" + cnb + ": missing " + file);
                        }
                    }
                } catch (Exception x) {
                    throw new BuildException("Parsing " + module + ": " + x, x, getLocation());
                }
            }
            scanForExtraFiles(cluster, "", files.keySet(), cluster.getName(), extraFiles);
        }
        Map<String,String> pseudoTests = new LinkedHashMap<>();
        pseudoTests.put("testMissingFiles", missingFiles.length() > 0 ? "Some files were missing" + missingFiles : null);
        pseudoTests.put("testExtraFiles", extraFiles.length() > 0 ? "Some extra files were present" + extraFiles : null);
        pseudoTests.put("testDuplicatedFiles", duplicatedFiles.length() > 0 ? "Some files were registered in two or more NBMs" + duplicatedFiles : null);
        JUnitReportWriter.writeReport(this, null, report, pseudoTests);
    }

    private void scanForExtraFiles(File d, String prefix, Set<String> files, String cluster, StringBuilder extraFiles) {
        if (prefix.equals("update_tracking/")) {
            return;
        }
        for (String n : d.list()) {
            File f = new File(d, n);
            if (f.getName().equals(".lastModified")) {
                continue;
            }
            if (f.isDirectory()) {
                scanForExtraFiles(f, prefix + n + "/", files, cluster, extraFiles);
            } else {
                String path = prefix + n;
                if ( patterns != null )
                    for( String p: patterns.getExcludePatterns(getProject()) ) 
                        if (SelectorUtils.matchPath(p, path)) return;
                    
                if (!files.contains(path)) {
                    extraFiles.append("\n" + cluster + ": untracked file " + path);
                    f.delete();
                }
            }
        }
    }

}
