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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        Set<String> allClusterModules = new TreeSet<String>();
        for (Set<String> s : clusters.values()) {
            allClusterModules.addAll(s);
        }
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
        Map<String,List<String>> allClusters = new HashMap<String,List<String>>();
        for (Map.Entry<String,Object> clusterDef : properties.entrySet()) {
            Matcher m = clusterNamePat.matcher(clusterDef.getKey());
            if (!m.matches()) {
                continue;
            }
            allClusters.put(m.group(1), splitToList((String) clusterDef.getValue(), clusterDef.getKey()));
        }
        allClusters.get("experimental").removeAll(allClusters.get("betauc")); // intentionally a superset
        allClusters.get("betauc").removeAll(allClusters.get("stableuc")); // ditto
        for (Map.Entry<String,List<String>> entry : allClusters.entrySet()) {
            String name = entry.getKey();
            List<String> modules = entry.getValue();
            Set<String> modulesS = new HashSet<String>(modules);
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
            List<String> sorted = new ArrayList<String>(modules);
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
        List<String> r = new ArrayList<String>(Arrays.asList(list.split(",")));
        assert !r.contains(null) : r;
        assert !r.contains("") : r;
        return r;
    }
    private Set<String> splitToSet(String list, String what) {
        List<String> elements = splitToList(list, what);
        Set<String> set = new HashSet<String>(elements);
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
        Map<String,Set<String>> clusters = new TreeMap<String,Set<String>>();
        for (String cluster : splitToSet(l, fullConfig)) {
            l = (String) clusterProperties.get(cluster);
            if (l == null) {
                throw new BuildException(clusterPropertiesFile + ": no definition for " + cluster);
            }
            clusters.put(cluster, new TreeSet<String>(splitToSet(l, fullConfig)));
        }
        return clusters;
    }

}
