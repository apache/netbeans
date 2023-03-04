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
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Produces a list of all compile-time dependencies between unit tests of modules.
 */
public class ModuleTestDependencies extends Task {

    private File output;

    /**
     * Configures the output file.
     * @param output a file to generate
     */
    public void setOutput(File output) {
        this.output = output;
    }

    private File reverseOutput;
    /**
     * An optional output file for test dependencies going to forbidden clusters.
     * @param reverseOutput a file to generate
     */
    public void setReverseOutput(File reverseOutput) {
        this.reverseOutput = reverseOutput;
    }

    /**
     * Runs the task.
     * @throws BuildException for the usual reasons
     */
    public @Override void execute() throws BuildException {
        try {
            Hashtable<String,Object> props = getProject().getProperties();
            ModuleListParser mlp = new ModuleListParser(props, ModuleType.NB_ORG, getProject());
            SortedMap<String,SortedSet<String>> deps = new TreeMap<>();
            SortedMap<String,SortedSet<String>> reverseDeps = reverseOutput != null ? new TreeMap<>() : null;
            File nball = new File((String) props.get("nb_all"));
            for (ModuleListParser.Entry entry : mlp.findAll()) {
                String myCnb = entry.getCnb();
                String myCluster = entry.getClusterName();
                if (myCluster.equals("extra")) {
                    continue;
                }
                String myCnbAndCluster = myCnb + " (" + myCluster + ")";
                File projectXml = new File(nball, (entry.getNetbeansOrgPath() + "/nbproject/project.xml").replace('/', File.separatorChar));
                Document pDoc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, /*XXX*/null, null);
                Element config = getConfig(projectXml, pDoc);
                Element td = ParseProjectXml.findNBMElement(config, "test-dependencies");
                if (td != null) {
                    String clusterDeps = getProject().getProperty("nb.cluster." + myCluster + ".depends");
                    if (clusterDeps == null) {
                        throw new BuildException("no property ${nb.cluster." + myCluster + ".depends} defined");
                    }
                    Set<String> allowed = new HashSet<>();
                    allowed.add(myCluster);
                    allowed.add("harness");
                    for (String piece : clusterDeps.split(",")) {
                        allowed.add(piece.replaceFirst("^nb[.]cluster[.]", ""));
                    }
                    for (Element depGroup : XMLUtil.findSubElements(td)) {
                        String testType = ParseProjectXml.findTextOrNull(depGroup, "name");
                        for (Element dep : XMLUtil.findSubElements(depGroup)) {
                            String targetCnb = ParseProjectXml.findTextOrNull(dep, "code-name-base");
                            if (targetCnb == null || targetCnb.equals(myCnb)) {
                                continue;
                            }
                            ModuleListParser.Entry depEntry = mlp.findByCodeNameBase(targetCnb);
                            String targetCluster = depEntry != null ? depEntry.getClusterName() : "extra";
                            String targetCnbAndCluster = targetCnb + " (" + targetCluster + ")";
                            if (ParseProjectXml.TestDeps.UNIT.equals(testType) && ParseProjectXml.findNBMElement(dep, "test") != null && ParseProjectXml.findNBMElement(dep, "compile-dependency") != null) {
                                SortedSet<String> depsForMe = deps.get(myCnbAndCluster);
                                if (depsForMe == null) {
                                    depsForMe = new TreeSet<>();
                                    deps.put(myCnbAndCluster, depsForMe);
                                }
                                depsForMe.add(targetCnbAndCluster);
                            }
                            if (reverseDeps != null && !targetCnb.equals("org.netbeans.libs.junit4") && !allowed.contains(targetCluster)) {
                                SortedSet<String> depsForMe = reverseDeps.get(myCnbAndCluster);
                                if (depsForMe == null) {
                                    depsForMe = new TreeSet<>();
                                    reverseDeps.put(myCnbAndCluster, depsForMe);
                                }
                                depsForMe.add(targetCnbAndCluster);
                            }
                        }
                    }
                }
            }
            log(output + ": generating test dependencies");
            PrintWriter pw = new PrintWriter(output);
            for (Map.Entry<String,SortedSet<String>> entry : deps.entrySet()) {
                pw.printf("MODULE %s\n", entry.getKey());
                for (String dep : entry.getValue()) {
                    pw.printf("  REQUIRES %s\n", dep);
                }
            }
            pw.flush();
            pw.close();
            if (reverseDeps != null) {
                log(reverseOutput + ": generating reverse test dependencies");
                pw = new PrintWriter(reverseOutput);
                for (Map.Entry<String,SortedSet<String>> entry : reverseDeps.entrySet()) {
                    pw.printf("MODULE %s\n", entry.getKey());
                    for (String dep : entry.getValue()) {
                        pw.printf("  REQUIRES %s\n", dep);
                    }
                }
                pw.flush();
                pw.close();
            }
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }

    private Element getConfig(File projectXml, Document pDoc) throws BuildException {
        Element e = pDoc.getDocumentElement();
        Element c = XMLUtil.findElement(e, "configuration", ParseProjectXml.PROJECT_NS);
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = ParseProjectXml.findNBMElement(c, "data");
        if (d == null) {
            throw new BuildException("No <data> in " + projectXml, getLocation());
        }
        return d;
    }
    
}
