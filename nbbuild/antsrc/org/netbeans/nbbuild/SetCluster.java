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
