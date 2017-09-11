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

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

/** Setting given property to list of clusters on which module depends
 *
 * @author Michal Zlamal
 */
public class GetDependsClusters extends Task {
    private String name = null;
    private String propertiesList = null;
    private String thisModuleName = null;
    
    /** Comma separated list of properties. One of those properties should contain the name of module from what it ran. */
    public void setList( String propertiesList ) {
        this.propertiesList = propertiesList;
    }
    
    /** Name of property to set */
    public void setName(String name) {
        this.name = name;
    }
    
    public void execute() throws BuildException {
        if (name == null)
            throw new BuildException("Name of property to set have to be specified",this.getLocation());
        if (propertiesList == null)
            throw new BuildException("List of clusters have to be specified",this.getLocation());
        
        thisModuleName = this.getOwningTarget().getName();
        if (!thisModuleName.startsWith("all-"))
            throw new BuildException("This task could be used only in targets \"all-{modulename}\"",this.getLocation());
        thisModuleName = thisModuleName.substring("all-".length());
        
        StringTokenizer tokens = new StringTokenizer( propertiesList, " \t\n\f\r," );
        while (tokens.hasMoreTokens()) {
            String property = tokens.nextToken().trim();
            String list = this.getProject().getProperty( property );
            if (list == null) throw new BuildException("Property: " + property + " is not defined anywhere",this.getLocation());
            StringTokenizer modTokens = new StringTokenizer(list," \t\n\f\r,");
            while (modTokens.hasMoreTokens()) {
                String module = modTokens.nextToken();
                log( property + " " + module, Project.MSG_VERBOSE );
                if (module.equals(thisModuleName)) {
                    String clusterDepends = this.getProject().getProperty(property + ".depends");
                    if (clusterDepends == null) throw new BuildException( "Property: " + property + ".depends have to be defined", this.getLocation());
                    log( "Property: " + name + " will be set to " + clusterDepends, Project.MSG_VERBOSE);
                    this.getProject().setProperty( name, clusterDepends );
                    return;
                }
            }
        }
        log("No cluster list with this module: " + thisModuleName + " was found. Assume that this module " + thisModuleName + " depends on all clusters: " + propertiesList, Project.MSG_WARN);
        log( "Property: " + name + " will be set to " + propertiesList, Project.MSG_VERBOSE);
        this.getProject().setProperty( name, propertiesList );
        //	throw new BuildException("No cluster list with this module: " + thisModuleName + " was found.",this.getLocation());
    }
}
