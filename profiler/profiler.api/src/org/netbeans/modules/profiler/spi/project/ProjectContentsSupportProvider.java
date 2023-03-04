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
package org.netbeans.modules.profiler.spi.project;

import org.netbeans.lib.profiler.client.ClientUtils;
import org.openide.filesystems.FileObject;

/**
 * Provider of support for configuring profiling roots and instrumentation filter from a project.
 *
 * @author Jiri Sedlacek
 */
public abstract class ProjectContentsSupportProvider {    
    
    /**
     * Returns array of profiling roots for the defined context.
     * 
     * @param profiledClassFile profiled file or null for profiling the entire project
     * @param profileSubprojects true if profiling also project's subprojects, false for profiling just the project
     * @return array of profiling roots for the defined context
     */
    public abstract ClientUtils.SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile, boolean profileSubprojects);
    
    /**
     * Returns instrumentation filter for the defined context.
     * 
     * @param profileSubprojects true if profiling also project's subprojects, false for profiling just the project
     * @return instrumentation filter for the defined context
     */
    public abstract String getInstrumentationFilter(boolean profileSubprojects);
    
    /**
     * Resets the ProjectContentsSupport instance after submitting or cancelling the Select Profiling Task dialog.
     */
    public abstract void reset();
    
    
//    public static class Basic extends ProjectContentsSupportProvider {
//        
//        private static final SourceCodeSelection[] EMPTY_SELECTION = new ClientUtils.SourceCodeSelection[0];
//
//        @Override
//        public SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile, boolean profileSubprojects) {
//            return EMPTY_SELECTION;
//        }
//
//        @Override
//        public SimpleFilter getInstrumentationFilter(boolean profileSubprojects) {
//            return SimpleFilter.NO_FILTER;
//        }
//        
//    }
    
}
