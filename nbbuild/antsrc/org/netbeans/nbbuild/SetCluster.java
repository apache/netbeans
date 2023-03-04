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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/** Settigns the given property to cluster value
 *
 * @author Michal Zlamal
 */
public class SetCluster extends Task {
    private String name = null;
    private String clusterName = null;
    private String cluster;
    private String thisModuleName = null;
    private String defaultLocation = null;
    
    /** Sets the name of property which should contain the value */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Sets the name of property which should contain name of cluster */
    public void setClusterName(String name) {
	this.clusterName = name;
    }
    
    /** Name of a cluster */
    public void setCluster (String cluster) {
        this.cluster = cluster;
    }
    
    /** Name of this module */
    public void setModule(String module) {
        thisModuleName = module;
    }

    /** Location of default cluster */
    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }
    
    public @Override void execute() throws BuildException {
        if (name == null) {
            throw new BuildException("Name of property to set have to be specified",this.getLocation());
        }
        if (cluster != null) {
            String clusterDir = this.getProject().getProperty(cluster + ".dir");
            if (clusterDir == null) throw new BuildException( "Property: " + cluster + ".dir have to be defined", this.getLocation());
            this.getProject().setProperty( name, clusterDir );
            return;
        }
        if (thisModuleName == null) {
            throw new BuildException("The name of current module have to be set", getLocation());
        }

        Property faketask = new Property();
        faketask.setProject(getProject());
        faketask.setLocation(this.getLocation());

        String[] clusterDir = { null };
        Map<String, Object> properties = getProject().getProperties();
        findClusterAndId(name, clusterDir, properties, thisModuleName, faketask, defaultLocation);
    }

    static String findClusterAndId(String propertyName, String[] clusterDir, Map<String, Object> properties, String path, Property faketask, final String fallback) throws BuildException {
        String id = null;
        clusterDir[0] = findClusterForAModule(propertyName, properties, path, clusterDir[0], faketask);
        if (clusterDir[0] == null && path.contains("/")) {
            final String shortPath = path.substring(path.lastIndexOf('/') + 1);
            clusterDir[0] = findClusterForAModule(propertyName, properties, shortPath, clusterDir[0], faketask);
            if (clusterDir[0] != null) {
                id = shortPath;
            }
        }
        if (clusterDir[0] == null) {
            if (fallback == null) {
                throw new BuildException("No default cluster location defined", faketask.getLocation());
            }

            clusterDir[0] = fallback;   // fallback
        }
        return id;
    }

    private static String findClusterForAModule(String propertyName, Map<String, Object> properties, String path, String clusterDir, Property faketask) throws BuildException {
        // not found, try indirect nbbuild/cluster.properties
        for (Map.Entry<String,Object> entry : properties.entrySet()) {
            String val = (String) entry.getValue();
            String[] modules = val.split(", *");
            if (Arrays.asList(modules).contains(path)) {
                String key = entry.getKey();
                clusterDir = (String) properties.get(key + ".dir");
                if (clusterDir != null) {
                    faketask.setName(propertyName);
                    faketask.setValue(clusterDir);
                    faketask.execute();
                    break;
                }
            }
        }
        return clusterDir;
    }

}
