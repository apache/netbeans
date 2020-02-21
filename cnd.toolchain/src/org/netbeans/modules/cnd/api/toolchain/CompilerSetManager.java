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

package org.netbeans.modules.cnd.api.toolchain;

import java.io.Writer;
import java.util.List;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerAccessorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public abstract class CompilerSetManager {
    /**
     * Find or create a default CompilerSetManager for the given key. A default
     * CSM is one which is active in the system. A non-default is one which gets
     * created but has no affect unless its made default.
     *
     * For instance, the Build Tools tab (on C/C++ Tools->Options) creates a non-Default
     * CSM and only makes it default if the OK button is pressed. If Cancel is pressed,
     * it never becomes default.
     *
     * @param env specifies execution environment
     * @return A default CompilerSetManager for the given key
     */
    public static CompilerSetManager get(ExecutionEnvironment env) {
        return CompilerSetManagerAccessorImpl.getDefault(env);
    }

    public abstract ExecutionEnvironment getExecutionEnvironment();
    
    public abstract CompilerSet getCompilerSet(String name);

    public abstract List<CompilerSet> getCompilerSets();

    public abstract CompilerSet getDefaultCompilerSet();

    public abstract boolean isDefaultCompilerSet(CompilerSet cs);

    public abstract int getPlatform();

    public abstract void setDefault(CompilerSet newDefault);

    /**
     * CAUTION: this is a slow method. It should NOT be called from the EDT thread
     */
    public abstract void initialize(boolean save, boolean runCompilerSetDataLoader, Writer reporter);

    public abstract boolean cancel();

    public abstract void finishInitialization();

    public abstract boolean isEmpty();

    public abstract boolean isPending();

    public abstract boolean isUninitialized();
    //add methods below as API/SPI should give the client the possibility to create own UI
    //in ODCS, for example
    
    public abstract boolean isComplete();
    
    public abstract void add(CompilerSet cs);
    
    public abstract void remove(CompilerSet cs);

    protected CompilerSetManager() {
        if (!getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("this class can not be overriden by clients"); // NOI18N
        }
    }

}
