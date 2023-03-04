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

import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Condition that determines whether a given module exports an API.
 * This is true if any of the following hold:
 * <ol>
 * <li>The module lists public API packages.
 * </ol>
 * This condition must be called from a module build, which is why it accepts no parameters:
 * various properties are picked up from the project. Only works for nb.org modules.
 */
public class ExportedAPICondition extends ProjectComponent implements Condition {

    public boolean eval() throws BuildException {
        Hashtable<String,Object> props = getProject().getProperties();
        if (props.get("public.packages").equals("-")) {
            log("No exported packages", Project.MSG_VERBOSE);
            return false;
        }
        String friends = (String) props.get("friends");
        if (friends == null) {
            log("Public API", Project.MSG_VERBOSE);
            return true;
        }
        /* Disabled to avoid spamming api-changes@netbeans.org.
        ModuleListParser mlp;
        try {
            mlp = new ModuleListParser(props, ModuleType.NB_ORG, getProject());
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        String mycluster = mlp.findByCodeNameBase(props.get("code.name.base.dashes").replace('-', '.')).getClusterName();
        for (String friend : friends.split(", ")) {
            ModuleListParser.Entry entry = mlp.findByCodeNameBase(friend);
            if (entry == null) {
                log("External friend " + friend, Project.MSG_VERBOSE);
                return true;
            }
            String cluster = entry.getClusterName();
            if (!mycluster.equals(cluster)) {
                log("Friend " + friend + " is in cluster " + cluster + " rather than " + mycluster, Project.MSG_VERBOSE);
                return true;
            }
        }
        log("No friends outside cluster", Project.MSG_VERBOSE);
         */
        return false;
    }

}
