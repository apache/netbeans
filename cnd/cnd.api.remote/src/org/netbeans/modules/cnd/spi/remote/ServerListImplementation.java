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

package org.netbeans.modules.cnd.spi.remote;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.api.remote.*;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * This is a place holder for a RemoteServerList which will be implemented in cnd.remote.
 * 
 */
public interface ServerListImplementation {
    
    public abstract Collection<? extends ServerRecord> getRecords();
    
    public abstract void setDefaultRecord(ServerRecord record);
    
    public abstract List<ExecutionEnvironment> getEnvironments();

    public abstract ServerRecord get(ExecutionEnvironment env);

    public abstract ServerRecord get(Project project);
    
    public abstract ServerRecord getDefaultRecord();
    
    public abstract void set(List<ServerRecord> records, ServerRecord defaultRecord);

    public ServerRecord addServer(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory, boolean asDefault, boolean connect);

    public abstract boolean isValidExecutable(ExecutionEnvironment env, String path);

    public abstract  ServerRecord createServerRecord(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void save();
}
