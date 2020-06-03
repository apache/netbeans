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

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.toolchain.ToolsCacheManagerImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public abstract class ToolsCacheManager {

    public abstract ServerUpdateCache getServerUpdateCache();

    public abstract void setHosts(Collection<? extends ServerRecord> list);

    public abstract CompilerSetManager getCompilerSetManagerCopy(ExecutionEnvironment env, boolean initialize);

    public abstract void addCompilerSetManager(CompilerSetManager newCsm);

    public abstract void setDefaultRecord(ServerRecord defaultRecord);

    public abstract void applyChanges();

    public abstract void discardChanges();
    
    abstract public Collection<? extends ServerRecord> getHosts();

    abstract public ServerRecord getDefaultHostRecord();
    
    abstract public void clear();
    
    abstract public void cancel();
    
    abstract public boolean hasCache();
    
    abstract public void applyChanges(final ServerRecord selectedRecord);
    
    abstract public CompilerSetManager restoreCompilerSets(CompilerSetManager oldCsm);
    
    public static ToolsCacheManager createInstance(){
        return createInstance(false);
    }

    public static ToolsCacheManager createInstance(boolean initialize){
        return new ToolsCacheManagerImpl(initialize);
    }

    public static void addChangeListener(ChangeListener l) {
        ToolsCacheManagerImpl.addChangeListener(l);
    }

    public static void removeChangeListener(ChangeListener l) {
        ToolsCacheManagerImpl.removeChangeListener(l);
    }

    protected ToolsCacheManager() {
        if (!getClass().equals(ToolsCacheManagerImpl.class)) {
            throw new UnsupportedOperationException("this class can not be overriden by clients"); // NOI18N
        }
    }
}
