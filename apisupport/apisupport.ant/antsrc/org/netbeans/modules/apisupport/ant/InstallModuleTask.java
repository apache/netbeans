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

package org.netbeans.modules.apisupport.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.netbeans.core.startup.TestModuleDeployer;

// Note that Ant tasks in general are not internationalized.

public class InstallModuleTask extends Task {

    private File module = null;
    private String action = null;

    public static class Action extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { /*XXX: "install", "uninstall",*/ "reinstall" }; // NOI18N
        }
    }

    public void setModule (File f) {
        module = f;
    }

    public void setAction (Action a) {
        action = a.getValue ();
    }

    public void execute () throws BuildException {
        if (module == null) throw new BuildException ("Required attribute: module", getLocation()); // NOI18N
        if (action == null) throw new BuildException ("Required attribute: action", getLocation()); // NOI18N
        try {
            if (action.equals ("reinstall")) { // NOI18N
                TestModuleDeployer.deployTestModule(module);
            } else {
                throw new BuildException ("Unsupported action: " + action, getLocation()); // NOI18N
            }
        } catch (IOException ioe) {
            throw new BuildException (ioe, getLocation());
        }
    }

}
