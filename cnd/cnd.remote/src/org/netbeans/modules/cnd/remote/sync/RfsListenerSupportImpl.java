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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.remote.api.RfsListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * 
 */
public class RfsListenerSupportImpl {

    private final ExecutionEnvironment execEnv;
    private final List<RfsListener> listeners = new ArrayList<>();
    
    private static final Map<ExecutionEnvironment, RfsListenerSupportImpl> instances = new HashMap<>();
    
    public static RfsListenerSupportImpl getInstanmce(ExecutionEnvironment execEnv) {
        synchronized (instances) {
            RfsListenerSupportImpl instance = instances.get(execEnv);
            if (instance == null) {
                instance = new RfsListenerSupportImpl(execEnv);
                instances.put(execEnv, instance);
            }
            return instance;
        }        
    }
    
    private RfsListenerSupportImpl(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }    
    
    public void addListener(RfsListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
    
    public void removeListener(RfsListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    public void fireFileChanged(File localFile, String remotePath) {
        RfsListener[] listenersCopy;
        synchronized (listeners) {
            listenersCopy = listeners.toArray(new RfsListener[listeners.size()]);
        }
        for (RfsListener listener : listenersCopy) {
            listener.fileChanged(execEnv, localFile, remotePath);
        }
    }
}
