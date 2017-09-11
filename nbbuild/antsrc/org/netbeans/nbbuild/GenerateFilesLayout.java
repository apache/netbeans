/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            Map</*cluster*/String,Map</*path*/String,/*cnb*/String>> ownersByCluster = new HashMap<String,Map<String,String>>();
            int maxlength = 0;
            for (String cluster : ds.getIncludedDirectories()) {
                if (cluster.isEmpty() || cluster.indexOf(File.separatorChar) != -1) {
                    continue;
                }
                Map<String,String> owners = new HashMap<String,String>();
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
            Writer w = new FileWriter(output);
            try {
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
            } finally {
                w.close();
            }
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        } catch (SAXException x) {
            throw new BuildException(x, getLocation());
        }
        log(output + ": generated");
    }

}
