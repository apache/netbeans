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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Task to sort the list of modules in a suite by their declared build dependencies.
 * @author Jesse Glick
 */
public class SortSuiteModules extends Task {
    private boolean sortTests;
    private Path unsortedModules;
    /**
     * Set a list of modules in the suite.
     * Each entry should be a project base directory.
     */
    public void setUnsortedModules(Path unsortedModules) {
        this.unsortedModules = unsortedModules;
    }

    private String sortedModulesProperty;
    /**
     * Set a property name in which to store a sorted path of module base directories.
     */
    public void setSortedModulesProperty(String sortedModulesProperty) {
        this.sortedModulesProperty = sortedModulesProperty;
    }

    /** Is enabled sorting test dependencies?
     */
    public boolean isSortTests() {
        return sortTests;
    }

    /** Enable or disable sorting test dependenciens. Default value is false.
     */
    public void setSortTests(boolean sortTests) {
        this.sortTests = sortTests;
    }

    public SortSuiteModules() {}

    public @Override void execute() throws BuildException {
        if (unsortedModules == null) {
            throw new BuildException("Must set unsortedModules");
        }
        if (sortedModulesProperty == null) {
            throw new BuildException("Must set sortedModulesProperty");
        }
        Map<String,File> basedirsByCNB = new TreeMap<>();
        Map<String,List<String>> buildDeps = new HashMap<>();
        for (String piece : unsortedModules.list()) {
            File d = new File(piece);
            File projectXml = new File(d, "nbproject" + File.separatorChar + "project.xml");
            if (!projectXml.isFile()) {
                throw new BuildException("Cannot open " + projectXml, getLocation());
            }
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, null, null);
            } catch (IOException | SAXException e) {
                throw new BuildException("Error parsing " + projectXml + ": " + e, e, getLocation());
            }
            Element config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
            if (config == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Element data = ParseProjectXml.findNBMElement(config, "data");
            if (data == null) {
                log("Skipping " + projectXml + " as it does not look like a module project", Project.MSG_WARN);
                continue;
            }
            Element cnbEl = ParseProjectXml.findNBMElement(data, "code-name-base");
            if (cnbEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            String cnb = XMLUtil.findText(cnbEl);
            basedirsByCNB.put(cnb, d);
            List<String> deps = new LinkedList<>();
            Element depsEl = ParseProjectXml.findNBMElement(data, "module-dependencies");
            if (depsEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            for (Element dep : XMLUtil.findSubElements(depsEl)) {
                if (ParseProjectXml.findNBMElement(dep, "build-prerequisite") == null &&
                    // Just build-prerequisite would not prevent "...will first try to build..." from ParseProjectXml,
                    // since that builds transitive runtime dependencies (e.g. from *.kit) first.
                    ParseProjectXml.findNBMElement(dep, "run-dependency") == null) {
                    continue;
                }
                Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
                if (cnbEl2 == null) {
                    throw new BuildException("Malformed project file " + projectXml, getLocation());
                }
                String cnb2 = XMLUtil.findText(cnbEl2);
                deps.add(cnb2);
            }
            buildDeps.put(cnb, deps);

            // create test dependencies
            if (isSortTests()) {
                Element testDepsEl = ParseProjectXml.findNBMElement(data,"test-dependencies");
                if (testDepsEl != null) {
                    // <test-type>
                    for(Element testDep: XMLUtil.findSubElements(testDepsEl)) {
                        for(Element dep: XMLUtil.findSubElements(testDep)) {
                            if (ParseProjectXml.findNBMElement(dep, "test") == null) {
                                continue;
                            }
                            Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
                            if (cnbEl2 == null) {
                                throw new BuildException("No cobase found for test-dependency");
                            }
                            String cnb2 = XMLUtil.findText(cnbEl2);
                            deps.add(cnb2);
                        }
                    }
                }
            }
        }
        for (List<String> deps: buildDeps.values()) {
            deps.retainAll(basedirsByCNB.keySet());
        }
        Map<String,List<String>> reversedDeps = new HashMap<>();
        for (Map.Entry<String,List<String>> entry : buildDeps.entrySet()) {
            for (String from : entry.getValue()) {
                String to = entry.getKey();
                List<String> tos = reversedDeps.get(from);
                if (tos == null) {
                    reversedDeps.put(from, tos = new ArrayList<>());
                }
                tos.add(to);
            }
        }
        List<String> cnbs;
        try {
            cnbs = topologicalSort(basedirsByCNB.keySet(), reversedDeps);
        } catch (TopologicalSortException x) {
            throw new BuildException(x.getMessage(), x, getLocation());
        }
        StringBuilder path = new StringBuilder();
        for (String cnb: cnbs) {
            assert basedirsByCNB.containsKey(cnb);
            if (path.length() > 0) {
                path.append(File.pathSeparatorChar);
            }
            path.append(basedirsByCNB.get(cnb).getAbsolutePath());
        }
        getProject().setNewProperty(sortedModulesProperty, path.toString());
    }
    // Stolen from org.openide.util.Utilities:
    private static <T> List<T> topologicalSort(Collection<T> c, Map<? super T, ? extends Collection<? extends T>> edges)
    throws TopologicalSortException {
        Map<T,Boolean> finished = new HashMap<>();
        List<T> r = new ArrayList<>(Math.max(c.size(), 1));
        List<T> cRev = new ArrayList<>(c);
        Collections.reverse(cRev);

        Iterator<T> it = cRev.iterator();

        while (it.hasNext()) {
            List<T> cycle = visit(it.next(), edges, finished, r);

            if (cycle != null) {
                throw new TopologicalSortException("Cycle detected: " + cycle.toString());
            }
        }

        Collections.reverse(r);
        if (r.size() != c.size()) {
            r.retainAll(c);
        }

        return r;
    }
    private static <T> List<T> visit(
        T node,
        Map<? super T, ? extends Collection<? extends T>> edges,
        Map<T,Boolean> finished,
        List<T> r
    ) {
        Boolean b = finished.get(node);

        //System.err.println("node=" + node + " color=" + b);
        if (b != null) {
            if (b) {
                return null;
            }

            ArrayList<T> cycle = new ArrayList<>();
            cycle.add(node);
            finished.put(node, null);

            return cycle;
        }

        Collection<? extends T> e = edges.get(node);

        if (e != null) {
            finished.put(node, Boolean.FALSE);

            Iterator<? extends T> it = e.iterator();

            while (it.hasNext()) {
                List<T> cycle = visit(it.next(), edges, finished, r);

                if (cycle != null) {
                    if (cycle instanceof ArrayList) {
                        // if cycle instanceof ArrayList we are still in the
                        // cycle and we want to collect new members
                        if (Boolean.FALSE == finished.get(node)) {
                            // another member in the cycle
                            cycle.add(node);
                        } else {
                            // we have reached the head of the cycle
                            // do not add additional cycles anymore
                            Collections.reverse(cycle);

                            // changing cycle to not be ArrayList
                            cycle = Collections.unmodifiableList(cycle);
                        }
                    }

                    // mark this node as tested
                    finished.put(node, Boolean.TRUE);

                    // and report an error
                    return cycle;
                }
            }
        }

        finished.put(node, Boolean.TRUE);
        r.add(node);

        return null;
    }

    private static final class TopologicalSortException extends Exception {

        public TopologicalSortException() {
        }

        public TopologicalSortException(String message) {
            super(message);
        }

        public TopologicalSortException(String message, Throwable cause) {
            super(message, cause);
        }

        public TopologicalSortException(Throwable cause) {
            super(cause);
        }

    }
}
