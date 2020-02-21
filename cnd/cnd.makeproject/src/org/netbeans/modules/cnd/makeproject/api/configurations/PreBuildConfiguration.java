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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class PreBuildConfiguration implements Cloneable {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration preBuildCommandWorkingDir;
    private StringConfiguration preBuildCommand;
    private BooleanConfiguration preBuildFirst;
    
    private static final RequestProcessor RP = new RequestProcessor("MakeConfiguration", 1); // NOI18N
    
    // Constructors
    public PreBuildConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        preBuildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        preBuildCommand = new StringConfiguration(null, ""); // NOI18N
        preBuildFirst = new BooleanConfiguration(false);
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    public void setPreBuildFirst(BooleanConfiguration preBuildFirst){
        this.preBuildFirst = preBuildFirst;
    }
    
    public BooleanConfiguration getPreBuildFirst(){
        return preBuildFirst;
    }
    
    // Working Dir
    public StringConfiguration getPreBuildCommandWorkingDir() {
        return preBuildCommandWorkingDir;
    }
    
    // Working Dir
    public String getPreBuildCommandWorkingDirValue() {
        if (preBuildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return preBuildCommandWorkingDir.getValue();
        }
    }
    
    public void setPreBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.preBuildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Pre-Build Command
    public StringConfiguration getPreBuildCommand() {
        return preBuildCommand;
    }
    
    public void setPreBuildCommand(StringConfiguration buildCommand) {
        this.preBuildCommand = buildCommand;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsPreBuildCommandFileObject() {        
        String path = getAbsPreBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsPreBuildCommandWorkingDir() {
        String wd;
        if (getPreBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getPreBuildCommandWorkingDirValue())) {
            wd = getPreBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getPreBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }

    // Clone and assign
    public void assign(PreBuildConfiguration conf) {
        getPreBuildCommandWorkingDir().assign(conf.getPreBuildCommandWorkingDir());
        getPreBuildCommand().assign(conf.getPreBuildCommand());
        getPreBuildFirst().assign(conf.getPreBuildFirst());
    }

    @Override
    public PreBuildConfiguration clone() {
        PreBuildConfiguration clone = new PreBuildConfiguration(getMakeConfiguration());
        clone.setPreBuildCommandWorkingDir(getPreBuildCommandWorkingDir().clone());
        clone.setPreBuildCommand(getPreBuildCommand().clone());
        clone.setPreBuildFirst(getPreBuildFirst().clone());
        return clone;
    }

    private ExecutionEnvironment getSourceExecutionEnvironment() {
        ExecutionEnvironment env = null;
        MakeConfiguration mc = this.getMakeConfiguration();
        if (mc != null) {
            return FileSystemProvider.getExecutionEnvironment(mc.getBaseFSPath().getFileSystem());
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }
}
