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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
            Map</*path*/String,/*CNB*/String> files = new HashMap<String,String>();
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
        Map<String,String> pseudoTests = new LinkedHashMap<String,String>();
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
