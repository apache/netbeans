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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Analyzes build.properties and cluster.properties and tries to diagnose any problems.
 * Also produces a summary of moduleconfig contents which is written to a golden file.
 * @author Jesse Glick
 */
public final class CheckModuleConfigs extends Task {
    
    private File nbroot;
    
    public CheckModuleConfigs() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }

    public @Override void execute() throws BuildException {
        if (nbroot == null) {
            throw new BuildException("Must define 'nbroot' param", getLocation());
        }
        File clusterPropertiesFile = new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties");
        Map<String,Object> properties = getProject().getProperties();
        Map<String,Set<String>> clusters = loadModuleClusters(properties, clusterPropertiesFile);

        // Check that javadoc <= full cluster config:
        Set<String> javadoc = splitToSet(getProject().getProperty("config.javadoc.all"), "config.javadoc.all");
        for (Set<String> modules : clusters.values()) {
            javadoc.removeAll(modules);
        }
        if (!javadoc.isEmpty()) {
            throw new BuildException(new File(nbroot, "nbbuild" + File.separatorChar + "build.properties") +
                    ": javadoc config contains entries not in known clusters: " + javadoc);
        }
        // Verify sorting and overlaps:
        Pattern clusterNamePat = Pattern.compile("nb\\.cluster\\.([^.]+)");
        Map<String,List<String>> allClusters = new HashMap<>();
        for (Map.Entry<String,Object> clusterDef : properties.entrySet()) {
            Matcher m = clusterNamePat.matcher(clusterDef.getKey());
            if (!m.matches()) {
                continue;
            }
            allClusters.put(m.group(1), splitToList((String) clusterDef.getValue(), clusterDef.getKey()));
        }

        for (Map.Entry<String,List<String>> entry : allClusters.entrySet()) {
            String name = entry.getKey();
            List<String> modules = entry.getValue();
            Set<String> modulesS = new HashSet<>(modules);
            if (modulesS.size() < modules.size()) {
                throw new BuildException("duplicates found in " + name + ": " + modules);
            }
            for (Map.Entry<String,List<String>> entry2 : allClusters.entrySet()) {
                String other = entry.getKey();
                if (other.equals(name)) {
                    continue;
                }
                if (modulesS.removeAll(entry2.getValue())) {
                    throw new BuildException("some entries in " + name + " also found in " + other);
                }
            }
            List<String> sorted = new ArrayList<>(modules);
            Collections.sort(sorted);
            if (!sorted.equals(modules)) {
                throw new BuildException("unsorted list for " + name + ": " + modules);
            }
        }
    }
    
    private List<String> splitToList(String list, String what) {
        if (list.length() == 0) {
            return Collections.emptyList();
        }
        // !list.matches("[^\\s,]+(,[^\\s,]+)*") exhausts stack for long module lists
        if (list.matches(".*\\s.*|^,.*|.*,$|.*,,.*")) {
            throw new BuildException("remove whitespaces or fix leading/trailing commas in " + what + ": " + list);
        }
        List<String> r = new ArrayList<>(Arrays.asList(list.split(",")));
        assert !r.contains(null) : r;
        assert !r.contains("") : r;
        return r;
    }
    private Set<String> splitToSet(String list, String what) {
        List<String> elements = splitToList(list, what);
        Set<String> set = new HashSet<>(elements);
        for (String s : set) {
            elements.remove(s);
        }
        if (!elements.isEmpty()) { // #147690
            throw new BuildException("duplicates found in " + what + ": " + elements);
        }
        return set;
    }
    
    private Map<String,Set<String>> loadModuleClusters(Map<String,Object> clusterProperties, File clusterPropertiesFile) {
        String fullConfig = "clusters.config.full.list";
        String l = (String) clusterProperties.get(fullConfig);
        if (l == null) {
            throw new BuildException(clusterPropertiesFile + ": no definition for clusters.config.full.list");
        }
        Map<String,Set<String>> clusters = new TreeMap<>();
        for (String cluster : splitToSet(l, fullConfig)) {
            l = (String) clusterProperties.get(cluster);
            if (l == null) {
                throw new BuildException(clusterPropertiesFile + ": no definition for " + cluster);
            }
            clusters.put(cluster, new TreeSet<>(splitToSet(l, fullConfig)));
        }
        return clusters;
    }

}
