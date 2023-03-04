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
package org.netbeans.modules.profiler.nbimpl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Bachorik
 */
public class StartProfilerTask extends Task {
    private String freeformStr = "";
    private boolean isFreeForm = false;
    private AtomicBoolean connectionCancel = new AtomicBoolean();
    
    @Override
    public void execute() throws BuildException {
        ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
        if (s == null && isFreeForm) {
            File baseDir = getProject().getBaseDir();
            if (baseDir != null) {
                Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(baseDir));
                if (p != null) {
                    s = ProfilerLauncher.Session.createSession(p);
                }
            }
            
        }
        if (s != null) {
            Map<String, String> props = s.getProperties();
            if (props != null) {
                for(Map.Entry<String, String> e : props.entrySet()) {
                    getProject().setProperty(e.getKey(), e.getValue());
                }
                if (isFreeForm) {
                    getProject().setProperty("profiler.configured", "true"); // NOI18N
                }
                
                getProject().addBuildListener(new BuildEndListener(connectionCancel));
                
                if (!NetBeansProfiler.getDefaultNB().startEx(s.getProfilingSettings(), s.getSessionSettings(), connectionCancel)) {
                    throw new BuildException("User abort"); // NOI18N
                }
            }
        }
    }
    
    public void setFreeform(String val) {
        freeformStr = val;
        isFreeForm = Boolean.parseBoolean(val);
    }
    
    public String getFreeform() {
        return freeformStr;
    }
}
