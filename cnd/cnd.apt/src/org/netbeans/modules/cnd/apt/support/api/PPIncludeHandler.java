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
package org.netbeans.modules.cnd.apt.support.api;

import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface PPIncludeHandler {
    // TODO: (get/set)State methods and interface could be hidded on impl layer,
    // because used only by PreprocHandler
    /*
     * save/restore state of handler
     */
    public State getState();
    public void setState(State state);
    
    /** immutable state object of include handler */    
    public interface State {
    };
    
    /**
     * returns the first file where include stack started
     */
    public StartEntry getStartEntry();
    
    public enum IncludeState {
        Success,
        Fail,
        Recursive
    }
    
    /*
     * 
     * notify about inclusion
     * @param path included file absolute path
     * @param line #include directive line
     * @param offset #include directive offset
     * @param resolvedDirIndex index of resolved directory in lists of include paths
     * @param inclDirIndex index in file
     * @return IncludeState.Recursive if inclusion is recursive and was prohibited
     */
    public IncludeState pushInclude(FileSystem fs, CharSequence path, int line, int offset, int resolvedDirIndex, int inclDirIndex);
    
    /*
     * notify about finished inclusion
     */
    public CharSequence popInclude();
    
    /**
     * include stack entry
     * - line where #include directive was
     * - resolved #include directive as absolute included path
     */
    public interface IncludeInfo {
        public FileSystem getFileSystem();
        public CharSequence getIncludedPath();
        public int getIncludeDirectiveLine();
        public int getIncludeDirectiveOffset();
        public int getResolvedDirectoryIndex();
        public int getIncludeDirectiveIndex();
    }     
}
