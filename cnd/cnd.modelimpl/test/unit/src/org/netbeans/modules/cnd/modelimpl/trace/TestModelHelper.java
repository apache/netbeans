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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 */
public final class TestModelHelper implements TraceModel.ParsingTimeResultListener {
    private static final Object LOCK = new Object();
    private final TraceModel traceModel;
    private CharSequence projectName;
    private NativeProject platformProject;
    private TraceModel.ParsingTimeResultListener delegate;
    /**
     * Creates a new instance of TestModelHelper
     */
    public TestModelHelper(boolean clearCache) {
        this(clearCache, null);
    }

    public TestModelHelper(boolean clearCache, TraceModelFileFilter filter) {
        synchronized (LOCK) {
            traceModel = new TraceModel(clearCache, filter);
        }
        traceModel.addParsingTimeResultListener(this);
    }
    
    public TraceModel getTraceModel() {
        return traceModel;
    }
    
    public void addParsingTimeResultListener(TraceModel.ParsingTimeResultListener delegate) {
        this.delegate = delegate;
    }

    public void initParsedProject(String projectRoot, 
            List<String> sysIncludes, List<String> usrIncludes, List<String> libProjectsPaths) throws Exception {
        synchronized (LOCK) {
            traceModel.setIncludePaths(sysIncludes, usrIncludes, libProjectsPaths);
            traceModel.test(new String[]{projectRoot}, System.out, System.err);
            getProject();
        }
    } 
    
    public void initParsedProject(String projectRoot) throws Exception {
        synchronized (LOCK) {
            traceModel.test(new String[]{projectRoot}, System.out, System.err);
            getProject();
        }
    }     
    
    public ProjectBase getProject(){
        synchronized (LOCK) {
            ProjectBase project = traceModel.getProject();
            if (projectName == null) {
                projectName = project.getName();
                platformProject = (NativeProject) project.getPlatformProject();
            }
            return project;
        }
    }

    public ProjectBase reopenProject() {
        synchronized (LOCK) {
            assert platformProject != null;
            ProjectBase project = traceModel.reopenProject(platformProject);
            return project;
        }
    }

    public CharSequence getProjectName() {
        assert projectName != null;
        return projectName;
    }

    public void resetProject() {
        synchronized (LOCK) {
            traceModel.resetProject();
        }
    }

    public CsmModel getModel(){
        synchronized (LOCK) {
            return traceModel.getModel();
        }
    }
    
    public void shutdown(boolean clearCache) {
        synchronized (LOCK) {
            traceModel.shutdown(clearCache);
            TextIndexStorageManager.shutdown();
        }
    }

    @Override
    public String toString() {
        return "TestModelHelper{" + "projectName=" + projectName + '}';
    }

    @Override
    public void notifyParsingTime(TraceModel.TestResult parsingTime) {
        if (delegate != null) {
            delegate.notifyParsingTime(parsingTime);
        }
    }
}
