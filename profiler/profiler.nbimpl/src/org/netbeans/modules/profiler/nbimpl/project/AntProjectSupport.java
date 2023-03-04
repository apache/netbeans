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
package org.netbeans.modules.profiler.nbimpl.project;

import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Support for profiling Ant projects.
 *
 * @author Jiri Sedlacek
 */
public final class AntProjectSupport {
    
    /**
     * A constant indicating "Profile Project" action
     */
    public static final int TARGET_PROFILE = 1;

    /**
     * A constant indicating "Profile File" action
     */
    public static final int TARGET_PROFILE_SINGLE = 2;

    /**
     * A constant indicating "Profile Test" action
     */
    public static final int TARGET_PROFILE_TEST = 3;

    /**
     * A constant indicating "Profile Single Test" action
     */
    public static final int TARGET_PROFILE_TEST_SINGLE = 4;
    
    
    private static AntProjectSupport DEFAULT;
    
    private final AntProjectSupportProvider provider;
    
    /**
     * Returns build script of a project.
     * 
     * @return build script of a project
     */
    public FileObject getProjectBuildScript() {
        return provider.getProjectBuildScript();
    }
    
    /**
     * Returns build script according to provided file name.
     * 
     * @param buildFileName file name of the build script
     * @return build script according to provided file name
     */
    public FileObject getProjectBuildScript(String buildFileName) {
        return provider.getProjectBuildScript(buildFileName);
    }
    
    /**
     * Configures profiling properties passed to the Ant environment.
     * 
     * @param props properties
     * @param profiledClassFile profiled file or null for profiling the entire project
     */
    public void configurePropertiesForProfiling(Map props, FileObject profiledClassFile) {
        provider.configurePropertiesForProfiling(props, profiledClassFile);
    }
    
    
    private AntProjectSupport(AntProjectSupportProvider provider) {
        this.provider = provider;
    }
    
    private static synchronized AntProjectSupport defaultImpl() {
        if (DEFAULT == null)
            DEFAULT = new AntProjectSupport(new AntProjectSupportProvider.Basic());
        return DEFAULT;
    }
    
    
    /**
     * Returns AntProjectSupport instance for the provided project.
     * 
     * @param project project
     * @return AntProjectSupport instance for the provided project
     */
    public static AntProjectSupport get(Lookup.Provider project) {
        AntProjectSupportProvider provider =
                project != null ? project.getLookup().lookup(AntProjectSupportProvider.class) : null;
        if (provider == null) return defaultImpl();
        else return new AntProjectSupport(provider);
    }
    
}
