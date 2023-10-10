/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Insert into the live project some all-* targets.
 * This means they do not need to manually written into nbbuild/build.xml.
 * Targets are only added for projectized modules which do not already
 * have all-* entries in the build script.
 * Generally, an all-* target for a projectized module in a named cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml, but which are not included in the same cluster as this module.
 * Therefore cluster dependencies still have to be manually set.
 * An all-* target for a projectized module without a specific cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml.
 * cluster.properties must have already been read for this task to work.
 * @author Jesse Glick
 */
public final class InsertModuleAllTargets extends Task {
    
    public InsertModuleAllTargets() {}
    
    boolean checkModules = false;
    public void setCheckModules( boolean check ) {
        checkModules = check;
    }

    boolean useClusters = true;
    public void setUseClusters(boolean b) {
        useClusters = b;
    }
    
    public @Override void execute() throws BuildException {
        try {
            Project prj = getProject();
            @SuppressWarnings("unchecked")
            Set<String> existingTargets = prj.getTargets().keySet();
            if (existingTargets.contains("all-openide.util")) {
                log("Already seem to have inserted targets into this project; will not do it twice", Project.MSG_VERBOSE);
                return;
            }
            Hashtable<String,Object> props = prj.getProperties();

            if (checkModules) {
                boolean missingModules = false;
                String[] clusters = ((String) props.get("nb.clusters.list")).split(", *");
                String nb_all = (String) props.get("nb_all");
                if (nb_all == null)
                    throw new BuildException("Can't file 'nb_all' property, probably not in the NetBeans build system");
                File nbRoot = new File(nb_all);
                for( String cluster: clusters) {
                    if (props.get(cluster) == null) 
                        throw new BuildException("Cluster '"+cluster+"' has got empty list of modules. Check configuration of that cluster.",getLocation());
                    String[] clusterModules = ((String) props.get(cluster)).split(", *");
                    for( String module: clusterModules) {
                        File moduleBuild = new File(nbRoot, module + File.separator + "build.xml");
                        if (!moduleBuild.exists() || !moduleBuild.isFile()) {
                            String clusterDir = (String) props.get(cluster + ".dir");
                            File subModuleBuild = new File(new File(nbRoot, clusterDir), module + File.separator + "build.xml");
                            if (!subModuleBuild.exists() || !subModuleBuild.isFile()) {
                                missingModules = true;
                                log("This module is missing from checkout: " + module + " - at least can't find: " + moduleBuild.getAbsolutePath());
                            }
                        }
                    }
                }
                if (missingModules) {
                    String clusterConfig = (String) props.get("cluster.config");
                    throw new BuildException("Some modules according your cluster config '" + clusterConfig + "' are missing from checkout, see messages above.",getLocation());
                }
            }
            
            Map<String,String> clustersOfModules = new HashMap<>();
            if (useClusters) {
                for (Map.Entry<String,Object> pair : props.entrySet()) {
                    String cluster = pair.getKey();
                    if (!cluster.startsWith("nb.cluster.") || cluster.endsWith(".depends") || cluster.endsWith(".dir")) {
                        continue;
                    }
                    for (String module : ((String) pair.getValue()).split(", *")) {
                        clustersOfModules.put(module, cluster);
                    }
                }
            }
            ModuleListParser mlp = new ModuleListParser(props, ModuleType.NB_ORG, prj);
            SortedMap<String,ModuleListParser.Entry> entries = new TreeMap<>();
            for (ModuleListParser.Entry entry : mlp.findAll()) {
                String path = entry.getNetbeansOrgPath();
                if (path == null) continue; // It is taken from binary
                entries.put(path, entry);
            }
           
            for (ModuleListParser.Entry entry : entries.values()) {
                String path = entry.getNetbeansOrgPath();
                assert path != null : entry;
                String trg = "all-" + entry.getNetbeansOrgId();
                if (existingTargets.contains(trg)) {
                    log("Not adding target " + trg + " because one already exists", Project.MSG_INFO);
                    continue;
                }
                String[] prereqsAsCnb = entry.getBuildPrerequisites();
                StringBuilder namedDeps = new StringBuilder("init");
                String myCluster = clustersOfModules.get(entry.getNetbeansOrgId());
                if (myCluster != null) {
                    String clusterDep = "all-cluster-" + myCluster;
                    if (!prj.getTargets().containsKey(clusterDep)) {
                        Target t = new Target();
                        t.setName(clusterDep);
                        t.setLocation(getLocation());
                        t.setDepends("init");
                        prj.addTarget(t);
                        CallTarget call = (CallTarget) prj.createTask("antcall");
                        call.setTarget("build-one-cluster-dependencies");
                        call.setInheritAll(false);
                        Property param = call.createParam();
                        param.setName("one.cluster.dependencies");
                        param.setValue(props.get(myCluster + ".depends"));
                        param = call.createParam();
                        param.setName("one.cluster.name");
                        param.setValue("this-cluster");
                        t.addTask(call);
                    }
                    namedDeps.append(",").append(clusterDep);
                }
                for (String cnb : prereqsAsCnb ) {
                    ModuleListParser.Entry other = mlp.findByCodeNameBase(cnb);
                    if (other == null) {
                        log("Cannot find build prerequisite " + cnb + " of " + entry, Project.MSG_WARN);
                        continue;
                    }
                    String otherId = other.getNetbeansOrgId();
                    if (otherId == null) continue; // Do not add the all-module dependency for module which is in the binaries
                    String otherCluster = clustersOfModules.get(otherId);
                    if (myCluster == null || otherCluster == null || myCluster.equals(otherCluster)) {
                        namedDeps.append(",all-");
                        namedDeps.append(otherId);
                    }
                }
                String namedDepsS = namedDeps.toString();
                log("Adding target " + trg + " with depends=\"" + namedDepsS + "\"", Project.MSG_VERBOSE);
                Target t = new Target();
                t.setName(trg);
                t.setLocation(getLocation());
                t.setDepends(namedDepsS);
                prj.addTarget(t);
                Echo echo = (Echo) prj.createTask("echo");
                echo.setMessage("Building " + path + "...");
                t.addTask(echo);
                Ant ant = (Ant) prj.createTask("ant");
                ant.setDir(prj.resolveFile("../" + path));
                ant.setTarget("netbeans");
                Property property = ant.createProperty();
                property.setName(ParseProjectXml.DO_NOT_RECURSE);
                property.setValue("true");
                t.addTask(ant);
            }
        } catch (IOException e) {
            throw new BuildException(e.toString(), e, getLocation());
        }
    }
}
