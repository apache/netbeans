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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Locates files which should have the execute bit set.
 */
public class FindExecutables extends Task {

    private String allmodules;
    public void setAllmodules(String allmodules) {
        this.allmodules = allmodules;
    }

    private String exeIncludeProperty;
    public void setExeIncludeProperty(String exeIncludeProperty) {
        this.exeIncludeProperty = exeIncludeProperty;
    }

    public @Override void execute() throws BuildException {
        StringBuilder b = new StringBuilder();
        for (String module : allmodules.split(",")) {
            // Have to replace substitutions in e.g. o.jruby.distro/nbproject/project.properties:
            final AtomicReference<String> execFiles = new AtomicReference<>();
            final String prefix = "Load.";
            class Load extends Property {
                protected @Override void addProperty(String n, String v) {
                    addProperty(n, (Object) v);
                }
                protected void addProperty(String n, Object v) {
                    if (n.equals(prefix + "nbm.executable.files")) {
                        execFiles.set((String) v);
                    }
                }
            }
            Load load = new Load();
            load.setProject(getProject());
            load.setPrefix(prefix);
            load.setFile(new File(getProject().getProperty("nb_all"), module + "/nbproject/project.properties"));
            load.execute();
            if (execFiles.get() != null) {
                String cluster = getProject().getProperty(module + ".dir").replaceFirst(".+[/\\\\]", "");
                for (String pattern : execFiles.get().split(",")) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(cluster).append('/').append(pattern);
                }
            }
        }
        getProject().setNewProperty(exeIncludeProperty, b.toString());
    }

}
