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
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Michal Zlamal
 */
public class GetModuleName extends Task {
    String name;
    String id;
    File root;

    // XXX this is a lousy attr name; conventional for such attrs to
    // end in 'property' so you realize they refer to a property name
    public void setName (String name) {
        this.name = name;
    }

    public void setId (String id) {
        this.id = id;
    }

    /** Root directory of the whole project - ${nb_all}
     * @param root the root
     */
    public void setRoot(File root) {
        this.root = root;
    }
     
    @Override
    public void execute() throws BuildException {
        if (name == null) 
            throw new BuildException("You must set the property name, where to store the module name", this.getLocation());
        if (root == null)
            throw new BuildException("You must set the root dir", this.getLocation());
        try {
            File dir = this.getProject ().getBaseDir ();
            if (dir.toString ().endsWith (java.io.File.separatorChar + "test")) {
                // when looking for base dir for tests
                dir = dir.getParentFile ();
            }
            String rootdir = root.getCanonicalPath();
            StringBuilder modulename = new StringBuilder ();
            while (dir != null) {
                if (dir.getCanonicalPath ().equals (rootdir)) {
                    break;
                }
                if (modulename.length () > 0) {
                    modulename.insert (0, '/');
                }
                modulename.insert (0, dir.getName ());
                dir = dir.getParentFile ();
            }
            
            //log("Basedir: " + basedir + " rootdir: " + rootdir);
            if (dir == null) {
                throw new BuildException("This module (" + this.getProject().getBaseDir() + ") is on different path than the root dir", this.getLocation());
            }
            final String mName = modulename.toString();
            this.getProject().setNewProperty(name, mName);
            if (id != null) {
                int last = mName.lastIndexOf('/');
                this.getProject().setNewProperty(id, mName.substring(last + 1));
            }
        }
        catch (IOException ex) {
            throw new BuildException("Root dir or module's base dir wasn't recognized", ex, this.getLocation());
        }
    }
    
}
