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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Manifest;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Task which checks content of an update center to make sure module dependencies
 * are internally consistent. Can optionally also check against a previous update
 * center snapshot to make sure that updates of modules marked as newer (i.e. with
 * newer specification versions) would result in a consistent snapshot as well.
 * If there are any modules which cannot be loaded, the build fails with a description.
 * <p>
 * Actual NBMs are not downloaded. Everything necessary is present just in
 * the update center XML descriptor.
 * <p>
 * You must specify a classpath to load the NB module system from.
 * It should suffice to include those JARs in the NB platform cluster's <code>lib</code> folder.
 * @author Jesse Glick
 */
public final class VerifyUpdateCenter extends Task {

    public VerifyUpdateCenter() {}

    private URI updates;
    public void setUpdates(File f) {
        updates = f.toURI();
    }
    public void setUpdatesURL(URI u) {
        updates = u;
    }

    private URI oldUpdates;
    public void setOldUpdates(File f) {
        if (f.isFile()) {
            oldUpdates = f.toURI();
        } else {
            throw new BuildException("No such file: " + f, getLocation());
        }
    }
    public void setOldUpdatesURL(URI u) {
        if (u.toString().length() > 0) {
            oldUpdates = u;
        }
    }

    private Path classpath = new Path(getProject());
    public void addConfiguredClasspath(Path p) {
        classpath.append(p);
    }

    private File reportFile;
    /** JUnit-format XML result file to generate, rather than halting the build. */
    public void setReport(File report) {
        this.reportFile = report;
    }

    public @Override void execute() throws BuildException {
        if (updates == null) {
            throw new BuildException("you must specify updates");
        }
        Map<String,String> pseudoTests = new LinkedHashMap<>();
        ClassLoader loader = new AntClassLoader(getProject(), classpath);
        Set<Manifest> manifests = loadManifests(updates);
        checkForProblems(findInconsistencies(manifests, loader), "Inconsistency(ies) in " + updates, "synchronicConsistency", pseudoTests);
        if (pseudoTests.get("synchronicConsistency") == null) {
            log(updates + " is internally consistent", Project.MSG_INFO);
            if (oldUpdates != null) {
                Map<String,Manifest> updated = new HashMap<>();
                for (Manifest m : loadManifests(oldUpdates)) {
                    updated.put(findCNB(m), m);
                }
                if (!findInconsistencies(new HashSet<>(updated.values()), loader).isEmpty()) {
                    log(oldUpdates + " is already inconsistent, skipping update check", Project.MSG_WARN);
                    JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
                    return;
                }
                SortedSet<String> updatedCNBs = new TreeSet<>();
                Set<String> newCNBs = new HashSet<>();
                for (Manifest m : manifests) {
                    String cnb = findCNB(m);
                    newCNBs.add(cnb);
                    boolean doUpdate = true;
                    Manifest old = updated.get(cnb);
                    if (old != null) {
                        String oldspec = old.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        String newspec = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        doUpdate = specGreaterThan(newspec, oldspec);
                    }
                    if (doUpdate) {
                        updated.put(cnb, m);
                        updatedCNBs.add(cnb);
                    }
                }
                SortedMap<String,SortedSet<String>> updateProblems = findInconsistencies(new HashSet<>(updated.values()), loader);
                updateProblems.keySet().retainAll(newCNBs); // ignore problems in now-deleted modules
                checkForProblems(updateProblems, "Inconsistency(ies) in " + updates + " relative to " + oldUpdates, "diachronicConsistency", pseudoTests);
                if (pseudoTests.get("diachronicConsistency") == null) {
                    log(oldUpdates + " after updating " + updatedCNBs + " from " + updates + " remains consistent");
                }
            }
        }
        JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
    }

    @SuppressWarnings("unchecked")
    private SortedMap<String,SortedSet<String>> findInconsistencies(Set<Manifest> manifests, ClassLoader loader) throws BuildException {
        try {
            return (SortedMap<String,SortedSet<String>>) loader.loadClass("org.netbeans.core.startup.ConsistencyVerifier").
                    getMethod("findInconsistencies", Set.class).invoke(null, manifests);
        } catch (Exception x) {
            throw new BuildException(x, getLocation());
        }
    }

    private Set<Manifest> loadManifests(URI u) throws BuildException {
        try {
            Document doc = XMLUtil.parse(new InputSource(u.toString()), false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver());
            Set<Manifest> manifests = new HashSet<>();
            boolean foundJUnit = false;
            NodeList nl = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nl.getLength(); i++) {
                Element m = (Element) nl.item(i);
                Manifest mani = new Manifest();
                NamedNodeMap map = m.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Attr a = (Attr) map.item(j);
                    mani.getMainAttributes().putValue(a.getName(), a.getValue());
                }
                Element module = (Element) m.getParentNode();
                for (String pseudoAttr : new String[] {"autoload", "eager"}) {
                    if (module.getAttribute(pseudoAttr).equals("true")) {
                        mani.getMainAttributes().putValue(pseudoAttr, "true");
                    }
                }
                manifests.add(mani);
                if ("org.netbeans.libs.junit4".equals(mani.getMainAttributes().getValue("OpenIDE-Module"))) {
                    foundJUnit = true;
                }
            }
            if (!foundJUnit) { // hack - pretend that this module is still in the platform cluster
                manifests.add(new Manifest(new ByteArrayInputStream("OpenIDE-Module: org.netbeans.libs.junit4\nOpenIDE-Module-Specification-Version: 1.13\n\n".getBytes())));
            }
            return manifests;
        } catch (FileNotFoundException x) {
            log("Could not load: " + u, x, Project.MSG_WARN);
            return Collections.emptySet();
        } catch (Exception x) {
            throw new BuildException("Could not load " + u, x, getLocation());
        }
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            throw new IllegalArgumentException();
        }
        return name.replaceFirst("/\\d+$", "");
    }

    private static boolean specGreaterThan(String newspec, String oldspec) {
        if (newspec == null) {
            return false;
        }
        if (oldspec == null) {
            return true;
        }
        String[] olddigits = oldspec.split("\\.");
        String[] newdigits = newspec.split("\\.");
        int oldlen = olddigits.length;
        int newlen = newdigits.length;
        int max = Math.max(oldlen, newlen);
        for (int i = 0; i < max; i++) {
            int oldd = (i < oldlen) ? Integer.parseInt(olddigits[i]) : 0;
            int newd = (i < newlen) ? Integer.parseInt(newdigits[i]) : 0;
            if (oldd != newd) {
                return newd > oldd;
            }
        }
        return false;
    }

    /* XXX feedback:
The test says things like:
--
Problems found for module org.netbeans.api.debugger.jpda: [The module org.netbeans.modules.java.source would also need to be installed., The module org.netbeans.modules.parsing.api would also need to be installed.]
--
but there is no problem whatsoever in module org.netbeans.api.debugger.jpda - it only happens to depend on module(s) that have problem. The error for java.source (which is hard to find in the plethora of warnings similar to the api.debugger.jpda's) says something like:
--
Problems found for module org.netbeans.modules.java.source: [The module named org.netbeans.modules.editor.indent.project/0-1 was needed and not found., The module named org.netbeans.modules.editor.lib/2 was needed and not found., The module org.netbeans.modules.editor.indent would also need to be installed., The module org.netbeans.modules.editor.lib2 would also need to be installed., The module org.netbeans.modules.java.preprocessorbridge would also need to be installed., The module org.netbeans.modules.options.editor would also need to be installed., The module org.netbeans.modules.parsing.api would also need to be installed., The module org.netbeans.modules.refactoring.api would also need to be installed.]
--
which is quite confusing IMO - it does not say anything about the fact that this relates to the "original" version of java.source that was not "upgraded" on the AUC because the spec. version did not change. Even though I knew what needs to be done for the test to pass before I looked at the test results, it took me a while figure out what exactly the test verifies. It would be great if at least the "false" warnings (like the one about api.debugger.jpda) could be suppressed.
     */
    private void checkForProblems(SortedMap<String,SortedSet<String>> problems, String msg, String testName, Map<String,String> pseudoTests) {
        if (!problems.isEmpty()) {
            StringBuilder message = new StringBuilder(msg);
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                message.append("\nProblems found for module ").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            pseudoTests.put(testName, message.toString());
        } else {
            pseudoTests.put(testName, null);
        }
    }

}
