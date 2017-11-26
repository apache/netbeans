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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

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

        Map<String,String> clusterByModule = new HashMap<String,String>(); // e.g. "serverplugins/jboss4" => "j2ee"
        for (Object key : getProject().getProperties().keySet()) {
            String property = (String) key;
            String clusterDir = getProject().getProperty(property + ".dir");
            if (clusterDir == null) {
                continue;
            }
            String list = this.getProject().getProperty( property );
            assert list != null : property;
            Set<String> modules = new HashSet<String>();
            StringTokenizer modTokens = new StringTokenizer(list," \t\n\f\r,");
            while (modTokens.hasMoreTokens()) {
                String module = modTokens.nextToken();
                if (module.equals(thisModuleName)) {
                    // We found the list referring to this module
                    log( "Property: " + name + " will be set to " + clusterDir, Project.MSG_VERBOSE);
                    this.getProject().setProperty( name, clusterDir ); // XXX setNewProperty?
		    if (clusterName != null) { // Set also cluster name property
                        log( "Property: " + clusterName + " will be set to " + property, Project.MSG_VERBOSE);
			this.getProject().setProperty( clusterName, property ); // XXX setNewProperty?
		    }
                    return;
                }
                String otherCluster = clusterByModule.put(module, clusterDir);
                if (otherCluster != null && !otherCluster.equals(clusterDir)) {
                    throw new BuildException("Module " + module + " found in two clusters: " + otherCluster + " and " + clusterDir, getLocation());
                }
                if (!modules.add(module)) {
                    throw  new BuildException("Module " + module + " repeated in cluster definition " + property, getLocation());
                }
            }
        }
       log("No cluster list with this module: " + thisModuleName + " was found. Using default cluster location: " + defaultLocation, Project.MSG_WARN);
       if (defaultLocation == null)
           throw new BuildException("No default cluster location defined", this.getLocation());

       log( "Property: " + name + " will be set to " + defaultLocation, Project.MSG_VERBOSE);
       this.getProject().setProperty( name, defaultLocation );
    }
}
