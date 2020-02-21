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

package org.netbeans.modules.cnd.makeproject.api;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbPreferences;

/**
 * Manages maps of temporary environment variables entered by user in Resolve dialogue
 */

public final class TempEnv {

    private static final Map<ExecutionEnvironment, TempEnv> instances = new HashMap<>();
    
    public static TempEnv getInstance(ExecutionEnvironment env) {
        synchronized (instances) {
            TempEnv instance = instances.get(env);
            if (instance == null) {
                instance = new TempEnv(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }
    
    private final ExecutionEnvironment execEnv;
    private final Object lock = new Object();
    private final Map<String, EnvElement> envVars = new HashMap<>();

    private TempEnv(ExecutionEnvironment env) {
        this.execEnv = env;
        Preferences node = getPreferences();
        try {
            for (String key : node.keys()) {
                String val = node.get(key, null);
                if (val != null) {
                    envVars.put(key, new EnvElement(val, false));
                }
            }
        } catch (BackingStoreException | IllegalStateException  ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    private void storeTemporaryEnv() {
        assert Thread.holdsLock(lock);
        Preferences node = getPreferences();
        envVars.entrySet().forEach((entry) -> {
            node.put(entry.getKey(), entry.getValue().value);
        });
    }
    
    private Preferences getPreferences() {
        String id = ExecutionEnvironmentFactory.toUniqueID(execEnv).replace(':', '_').replace('@', '_');
        return NbPreferences.forModule(TempEnv.class).node(id);
    }

    public boolean hasTemporaryEnv() {
        synchronized (lock) {
            return envVars != null && ! envVars.isEmpty();
        }
    }

    public boolean isTemporaryEnvSet(String key) {
        synchronized (lock) {
            EnvElement e = envVars.get(key);
            if (e != null) {
                return e.explicit;
            }
        }
        return false;
    }

    public void addTemporaryEnv(Map<String, String> map2fill) {
        synchronized (lock) {
            envVars.entrySet().forEach((entry) -> {
                String key = entry.getKey();
                if (!map2fill.containsKey(key)) {
                    map2fill.put(key, entry.getValue().value);
                }
            });
        }
    }

    public String getTemporaryEnv(String key) {
        synchronized (lock) {
            EnvElement value = envVars.get(key);
            return value == null ? null : value.value;
        }
    }

    public void setTemporaryEnv(String key, String value) {
        synchronized (lock) {
            envVars.put(key, new EnvElement(value, true));
            storeTemporaryEnv();
        }
    }
    
    private static class EnvElement {      
        public final String value;
        public final boolean explicit;
        public EnvElement(String value, boolean explicit) {
            this.value = value;
            this.explicit = explicit;
        }        
        @Override
        public String toString() {
            return value + (explicit ? " [explicit]" : " [restored]"); //NOI18N
        }        
    }    
}
