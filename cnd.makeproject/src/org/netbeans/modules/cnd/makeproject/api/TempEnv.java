/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
