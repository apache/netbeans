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
package org.netbeans.modules.cnd.makeproject.api.wizards;

import java.util.Collection;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 */
public class BuildSupport {
    public interface BuildFile {
        public String getFile();
        public String getCleanCommandLine(String arguments, String workingDir);
        public String getBuildCommandLine(String arguments, String workingDir);
        public String validate(ExecutionEnvironment ee, CompilerSet compilerSet);
    }

    public interface BuildFileProvider {
        BuildFile findBuildFileInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet);
        BuildFile scriptToBuildFile(String script);
        boolean isSupported(BuildFile script);
        FileFilter[] getFileFilter();
        public String getHint();
    }
    
    private BuildSupport() {
    }
    
    public static BuildFile findBuildFileInFolder(FileObject folder, ExecutionEnvironment ee, CompilerSet compilerSet) {
        BuildFile findConfigureScript = null;
        for (BuildFileProvider provider : getBuildFileProviders()) {
            findConfigureScript = provider.findBuildFileInFolder(folder, ee, compilerSet);
            if (findConfigureScript != null) {
                break;
            }
        }
        return findConfigureScript;
    }

    public static BuildFile scriptToBuildFile(String script) {
        BuildFile findConfigureScript = null;
        for (BuildFileProvider provider : getBuildFileProviders()) {
            findConfigureScript = provider.scriptToBuildFile(script);
            if (findConfigureScript != null) {
                break;
            }
        }
        return findConfigureScript;
    }
    
    public static BuildFileProvider getBuildFileProvider(BuildFile script) {
        for (BuildFileProvider provider : getBuildFileProviders()) {
            if (provider.isSupported(script)) {
                return provider;
            }
        }
        return null;
    }

    public static Collection<? extends BuildFileProvider> getBuildFileProviders() {
        Lookup.Result<BuildFileProvider> res = Lookup.getDefault().lookupResult(BuildFileProvider.class);
        return res.allInstances();
    }
}
