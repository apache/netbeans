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

import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Merge the arguments supplied via any {@code tryme.arg.*} properties into the
 * property {@code tryme.args}.
 */
public class MergeTrymeArgs extends Task {
    public @Override void execute() throws BuildException {
        StringBuilder tryMeArgs = new StringBuilder();
        for(Entry<String,Object> e: new TreeMap<>(getProject().getProperties()).entrySet()) {
            if(e.getKey().startsWith("tryme.arg.")) {
                tryMeArgs.append(" ");
                tryMeArgs.append(e.getValue());
            }
        }
        getProject().setNewProperty("tryme.args", tryMeArgs.toString());
    }
}
