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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;

/**
 * For each of specified property values calls target task of a specified name
 * with property set to one of these values
 *
 * @author  Radim Kubacki
 */
public class Repeat extends Task {
    
    private List<String> values;
    private String target;
    private String startdir;
    private String name;
    
    //
    // init
    //
    
    public Repeat() {
        values = new ArrayList<>();
        target    = null;
    }

    //
    // itself
    //

    /** Name of property that will be set for each call. */
    public void setName (String s) {
        log ("SET name = " + s, Project.MSG_DEBUG);

        name = s;
    }
    
    /** Comma separated list of values. */
    public void setValues (String s) {
        log ("SET values = " + s, Project.MSG_DEBUG);

        StringTokenizer tok = new StringTokenizer (s, ", ");
        values = new ArrayList<>();
        while ( tok.hasMoreTokens() ) {
            values.add (tok.nextToken().trim());
        }
    }
    
    /** Name of target which will be used with ant task. If not specified,
     * owning target name is used.
     */
    public void setTarget (String s) {
        log ("SET target = " + s, Project.MSG_DEBUG);

        target = s;
    }

    /** Execute this task. */
    public void execute () throws BuildException {        
        if ( values.isEmpty() ) {
            throw new BuildException("You must set at least one value!", getLocation());
        }

        if ( target == null ) {
            throw new BuildException("Target must be set!", getLocation());
        }

        for (String val : values) {
            log ("Process '" + val + "' location with '" + target + "' target ...", Project.MSG_VERBOSE);
            
            CallTarget antCall = (CallTarget) getProject().createTask("antcall");
            antCall.init();
            antCall.setLocation(getLocation());
            
            // ant.setDir (dir);
            antCall.setTarget (target);
            Property prop = antCall.createParam();
            prop.setName(name);
            prop.setValue(val);
            
            antCall.execute();
        }
    }
    
}
