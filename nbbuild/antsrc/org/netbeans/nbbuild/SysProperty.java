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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** Sets a system property in the running JVM. Use with care. Needed to
 * increase XML entities parsing limit before processing out Javadoc with
 * {@code <ant file="template.xml" .../>}.
 */
public final class SysProperty extends Task {
    private String name;
    private String value;

    public void setProperty(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void execute() throws BuildException {
        String current = System.getProperty(name);
        if (current == null) {
            System.setProperty(name, value);
        } else {
            getProject().log(this, "Not setting property " + name + " as it already has a value " + current, Project.MSG_WARN);
        }
    }
}
