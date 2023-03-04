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

package org.netbeans.nbbuild.extlibs;

import java.io.File;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Defines a property release.files.extra.
 * Value is comma-separated list of those project properties whose name begins with "release."
 * where the remainder of the property name describes an existing file path (relative to basedir).
 */
public class ReleaseFilesExtra extends Task {

    private String property;
    public void setProperty(String property) {
        this.property = property;
    }

    public @Override void execute() throws BuildException {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String,Object> entry : ((Map<String,Object>) getProject().getProperties()).entrySet()) {
            String k = entry.getKey();
            if (k.startsWith("release.")) {
                File f = getProject().resolveFile(k.substring(8).replaceFirst("!/.+$", ""));
                if (!f.isFile()) {
                    log("No such release file: " + f, Project.MSG_VERBOSE);
                    continue;
                }
                if (b.length() > 0) {
                    b.append(',');
                }
                b.append((String) entry.getValue());
            }
        }
        getProject().setNewProperty(property, b.toString());
    }

}
