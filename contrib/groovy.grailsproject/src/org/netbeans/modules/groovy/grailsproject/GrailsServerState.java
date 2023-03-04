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

package org.netbeans.modules.groovy.grailsproject;

import java.net.URL;
import org.netbeans.api.project.Project;

/**
 *
 * @author schmidtm, Petr Hejl
 */
public class GrailsServerState {

    private final Project project;

    /** <i>GuardedBy("this")</i> */
    private Process process;

    /** <i>GuardedBy("this")</i> */
    private boolean debug;

    /** <i>GuardedBy("this")</i> */
    private URL url;

    public GrailsServerState(Project prj) {
        this.project = prj;
    }

    public synchronized boolean isRunning() {
        if (process == null) {
            return false;
        }
        try {
            int exitVal = process.exitValue();
            return false;
        } catch (IllegalThreadStateException ex) {
            return true;
        }
    }

    public synchronized Process getProcess() {
        return process;
    }

    public synchronized void setProcess(Process process) {
        this.process = process;
    }

    public synchronized URL getRunningUrl() {
        if (isRunning()) {
            return url;
        }
        return null;
    }

    public synchronized void setRunningUrl(URL url) {
        this.url = url;
    }

    public synchronized boolean isDebug() {
        return debug;
    }

    public synchronized void setDebug(boolean debug) {
        this.debug = debug;
    }

}
