/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class PasswordManager {

    private static final boolean keepPasswordsInMemory;
    private static final String KEY_PREFIX = "remote.user.info.password."; // NOI18N
    private static final String STORE_PREFIX = "remote.user.info.store."; // NOI18N
    private final Map<String, char[]> cache = Collections.synchronizedMap(new HashMap<String, char[]>());
    private boolean keyringIsActivated = false;
    private static PasswordManager instance = new PasswordManager();

    static {
        // This flag, when set to true, means that user's password will be kept
        // in memory while NB is running. It will be used for re-establishing a
        // connection if it is *unexpectedly* broken. On explicit host
        // disconnect (on user's request), the password will be removed from the
        // memory disregarding this flag.
        keepPasswordsInMemory = Boolean.getBoolean("remote.user.password.keep_in_memory"); // NOI18N
    }

    private PasswordManager() {
    }

    public static PasswordManager getInstance() {
        return instance;
    }

    /**
     *
     * @param execEnv
     * @return password from memory or from Keyring if user selected "remember
     * password" in previous IDE invocation
     */
    public char[] getPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            char[] cachedPassword = cache.get(key);
            if (cachedPassword != null) {
                Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) found password in memory", execEnv); // NOI18N
                return cachedPassword;
            }
        }
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (stored) {
            keyringIsActivated = true;
            char[] keyringPassword = Keyring.read(KEY_PREFIX + key);
            if (keepPasswordsInMemory && keyringPassword != null) {
                char[] old = cache.put(key, Arrays.copyOf(keyringPassword, keyringPassword.length));
                if (old != null) {
                    Arrays.fill(old, 'x');
                }
            }
            Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) found password in keyring", execEnv); // NOI18N
            return keyringPassword;
        }

        Logger.getInstance().log(Level.FINEST, "PasswordManager.get({0}) failed to find password", execEnv); // NOI18N
        return null;
    }

    /**
     * Store password in memory. If user select "remember password" option
     * password is stored in Keyring.
     *
     * @param execEnv
     * @param password
     * @param rememberPassword
     */
    public void storePassword(ExecutionEnvironment execEnv, char[] password, boolean rememberPassword) {
        setRememberPassword(execEnv, rememberPassword);
        put(execEnv, password);
    }

    /**
     * Update password in memory. If user selected "remember password" option
     * before password is updated in Keyring.
     *
     * @param execEnv
     * @param password
     */
    private void put(ExecutionEnvironment execEnv, char[] password) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            char[] old;
            if (password != null) {
                old = cache.put(key, Arrays.copyOf(password, password.length));
                Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, non-null) stored password in memory", execEnv); // NOI18N
            } else {
                Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, null) cleared password from memory", execEnv); // NOI18N
                old = cache.put(key, null);
            }
            if (old != null) {
                Arrays.fill(old, 'x');
            }
        }
        boolean store = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (store) {
            keyringIsActivated = true;
            if (password == null) {
                Keyring.delete(KEY_PREFIX + key);
            } else {
                Keyring.save(KEY_PREFIX + key, password,
                        NbBundle.getMessage(PasswordManager.class, "PasswordManagerPasswordFor", execEnv.getDisplayName())); // NOI18N
            }
            Logger.getInstance().log(Level.FINEST, "PasswordManager.put({0}, non-null) stored password in keyring", execEnv); // NOI18N
        }
    }

    /**
     * Should be called on disconnect.
     */
    /*package*/ void onExplicitDisconnect(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        cache.remove(key);
    }

    /**
     * Remove password from memory and Keyring
     *
     * @param execEnv
     */
    public void clearPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            cache.remove(key);
        }
        NbPreferences.forModule(PasswordManager.class).remove(STORE_PREFIX + key);
        if (keyringIsActivated) {
            Keyring.delete(KEY_PREFIX + key);
        }
        Logger.getInstance().log(Level.FINEST, "PasswordManager.clearPassword({0})", execEnv); // NOI18N
    }

    /**
     * Remove password from memory and Keyring
     *
     * @param execEnv
     */
    public void forceClearPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (keepPasswordsInMemory) {
            cache.remove(key);
        }
        NbPreferences.forModule(PasswordManager.class).remove(STORE_PREFIX + key);
        Keyring.delete(KEY_PREFIX + key);
        Logger.getInstance().log(Level.FINEST, "PasswordManager.forceClearPassword({0})", execEnv); // NOI18N
    }

    /**
     * Remove passwords for hosts that are not in list.
     *
     * @param envs
     */
    public void setServerList(List<ExecutionEnvironment> envs) {
        Set<String> keys = new HashSet<>();
        for (ExecutionEnvironment env : envs) {
            String key = ExecutionEnvironmentFactory.toUniqueID(env);
            keys.add(KEY_PREFIX + key);
            keys.add(STORE_PREFIX + key);
        }
        try {
            String[] allKeys = NbPreferences.forModule(PasswordManager.class).keys();
            for (String aKey : allKeys) {
                if (!keys.contains(aKey)) {
                    if (aKey.startsWith(STORE_PREFIX)) {
                        if (keyringIsActivated) {
                            Keyring.delete(KEY_PREFIX + aKey.substring(STORE_PREFIX.length()));
                        }
                        if (keepPasswordsInMemory) {
                            cache.remove(aKey.substring(STORE_PREFIX.length()));
                        }
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * User intention of "remember password"
     *
     * @param execEnv
     * @return true if user checked "remember password" option.
     */
    public boolean isRememberPassword(ExecutionEnvironment execEnv) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        return stored;
    }

    /**
     * Store user intention of "remember password"
     *
     * @param execEnv
     * @param rememberPassword
     */
    public void setRememberPassword(ExecutionEnvironment execEnv, boolean rememberPassword) {
        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        if (!rememberPassword) {
            if (keyringIsActivated) {
                Keyring.delete(KEY_PREFIX + key);
            }
        }
        NbPreferences.forModule(PasswordManager.class).putBoolean(STORE_PREFIX + key, rememberPassword);
    }
}
