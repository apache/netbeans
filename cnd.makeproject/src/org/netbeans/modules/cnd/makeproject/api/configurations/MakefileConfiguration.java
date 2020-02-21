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

import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;

public class MakefileConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    
    private StringConfiguration buildCommandWorkingDir;
    private StringConfiguration buildCommand;
    private StringConfiguration cleanCommand;
    private StringConfiguration output;
    
    // Constructors
    public MakefileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        buildCommand = new StringConfiguration(null, MakeArtifact.MAKE_MACRO); // NOI18N
        cleanCommand = new StringConfiguration(null, MakeArtifact.MAKE_MACRO+" clean"); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    // Working Dir
    public StringConfiguration getBuildCommandWorkingDir() {
        return buildCommandWorkingDir;
    }
    
    // Working Dir
    public String getBuildCommandWorkingDirValue() {
        if (buildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return buildCommandWorkingDir.getValue();
        }
    }
    
    public void setBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.buildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Build Command
    public StringConfiguration getBuildCommand() {
        return buildCommand;
    }
    
    public void setBuildCommand(StringConfiguration buildCommand) {
        this.buildCommand = buildCommand;
    }
    
    // Build Command
    public StringConfiguration getCleanCommand() {
        return cleanCommand;
    }
    
    public void setCleanCommand(StringConfiguration cleanCommand) {
        this.cleanCommand = cleanCommand;
    }
    
    // Output
    public StringConfiguration getOutput() {
        return output;
    }
    
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }
    
    // Extra
    public boolean canBuild() {
        return getBuildCommand().getValue().length() > 0;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsBuildCommandFileObject() {        
        String path = getAbsBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsBuildCommandWorkingDir() {
        String wd;
        if (getBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getBuildCommandWorkingDirValue())) {
            wd = getBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }
    
    public boolean canClean() {
        return getCleanCommand().getValue().length() > 0;
    }
    
    public String getAbsOutput() {
        if (getOutput().getValue().length() == 0) {
            return ""; // NOI18N
        } else if (CndPathUtilities.isPathAbsolute(getOutput().getValue())) {
            return getOutput().getValue();
        } else {
            return getMakeConfiguration().getBaseDir() + "/" + getOutput().getValue(); // NOI18N
        }
    }
    
    // Clone and assign
    public void assign(MakefileConfiguration conf) {
        // MakefileConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getBuildCommandWorkingDir().assign(conf.getBuildCommandWorkingDir());
        getBuildCommand().assign(conf.getBuildCommand());
        getCleanCommand().assign(conf.getCleanCommand());
        getOutput().assign(conf.getOutput());
    }

    @Override
    public MakefileConfiguration clone() {
        MakefileConfiguration clone = new MakefileConfiguration(getMakeConfiguration());
        clone.setBuildCommandWorkingDir(getBuildCommandWorkingDir().clone());
        clone.setBuildCommand(getBuildCommand().clone());
        clone.setCleanCommand(getCleanCommand().clone());
        clone.setOutput(getOutput().clone());
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
