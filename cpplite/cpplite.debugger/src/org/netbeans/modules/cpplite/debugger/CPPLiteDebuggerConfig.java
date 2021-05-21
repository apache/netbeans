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

package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.util.List;

/**
 *
 * @author lahvac
 */
public final class CPPLiteDebuggerConfig {

    private final List<String> executable;
    private final File directory;
    private final String debugger;

    public CPPLiteDebuggerConfig(List<String> executable, File directory, String debugger) {
        this.executable = executable;
        this.directory = directory;
        this.debugger = debugger;
    }

    public String getDisplayName() {
        return (!executable.isEmpty()) ? executable.get(0) : "<empty>";
    }

    public List<String> getExecutable() {
        return executable;
    }

    public File getDirectory() {
        return directory;
    }

    public String getDebugger() {
        return debugger;
    }
}
