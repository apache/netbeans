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

package org.netbeans.core.startup;

// LIMITED INTERACTIONS with APIs and UI--may use ModuleInstall,
// and FileSystems API, and localized messages (but not notification),
// in addition to what is permitted for central classes (utility APIs
// and ModuleInfo-related things). Should be possible to use without
// the rest of core.

/** Representation of the history of the module.
 * @author Jesse Glick
 */
// XXX no longer useful, could probably delete, and deprecate Module.history
public final class ModuleHistory {
    
    private final String jar;
    private String info;
    
    /** Create a module history with essential information.
     * You also need to specify a relative or absolute JAR name.
     */
    public ModuleHistory(String jar) {
        assert jar != null;
        this.jar = jar;
    }

    ModuleHistory(String jar, String info) {
        this(jar);
        this.info = info;
    }
    
    /**
     * The name of the JAR relative to the installation, or
     * an absolute path.
     */
    String getJar() {
        return jar;
    }

    @Override public String toString() {
        return info != null ? info : jar;
    }
    
}
