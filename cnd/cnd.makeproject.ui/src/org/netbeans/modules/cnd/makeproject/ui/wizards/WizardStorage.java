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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

/**
 *
 */
public interface WizardStorage {

    void setMode(boolean isSimple);

    String getProjectPath();

    FileObject getSourcesFileObject();

    void setProjectPath(String path);

    void setSourcesFileObject(FileObject fileObject);

    String getConfigure();

    String getMake();

    void setMake(FileObject makefileFO);

    String getFlags();

    String getRealFlags();

    String getRealCommand();

    void setFlags(String flags);

    boolean isSetMain();

    void setSetMain(boolean setMain);

    boolean isBuildProject();

    void setBuildProject(boolean buildProject);

    void setCompilerSet(CompilerSet cs);

    CompilerSet getCompilerSet();

    void setExecutionEnvironment(ExecutionEnvironment ee);

    ExecutionEnvironment getExecutionEnvironment();

    void setSourceExecutionEnvironment(ExecutionEnvironment sourceEnv);

    ExecutionEnvironment getSourceExecutionEnvironment();

    void setDefaultCompilerSet(boolean defaultCompilerSet);

    boolean isDefaultCompilerSet();

    void setFullRemoteEnv(ExecutionEnvironment fuulRemoteEnv);

    ExecutionEnvironment getFullRemoteEnv();
    
}
