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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;

/**
 *
 * @author ak119685
 */
public final class Authentication {

    public static final MethodList PASSWORD_METHODS =
            new MethodList(List.of(
                    Pair.of(Method.GssapiWithMic, true),
                    Pair.of(Method.PublicKey, false),
                    Pair.of(Method.KeyboardInteractive, true),
                    Pair.of(Method.Password, true)));

    public static final MethodList SSH_KEY_METHODS  =
            new MethodList(List.of(
                    Pair.of(Method.GssapiWithMic, true),
                    Pair.of(Method.PublicKey, true),
                    Pair.of(Method.KeyboardInteractive, true),
                    Pair.of(Method.Password, true)));

    public static final MethodList DEFAULT_METHODS  = PASSWORD_METHODS;
    
    private static final Preferences prefs = NbPreferences.forModule(Authentication.class);
    private static final String METHODS_SUFFIX = ".methods"; // NOI18N
    private static final String TIMEOUT_SUFFIX = ".timeout"; // NOI18N
    private static final boolean isUnitTest = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private static final String knownHostsFile;
    private static String lastSSHKeyFile;
    private final ExecutionEnvironment env;
    private final String pref_key;
    private String sshKeyFile;
    private Type type = Type.UNDEFINED;
    private MethodList authenticationMethods = DEFAULT_METHODS;
    private int timeout = Integer.getInteger("jsch.connection.timeout", 10000)/1000; // NOI18N

    static {
        String hosts = System.getProperty("ssh.knonwhosts.file", null); // NOI18N

        if (hosts == null || !isValidKnownHostsFile(hosts)) {
            hosts = System.getProperty("user.home") + "/.ssh/known_hosts"; // NOI18N
            if (!isValidKnownHostsFile(hosts)) {
                hosts = System.getProperty("netbeans.user") + "/.ssh/known_hosts"; // NOI18N
                if (!isValidKnownHostsFile(hosts)) {
                    hosts = null;
                }
            }
        }

        knownHostsFile = hosts;

        String key = System.getProperty("user.home") + "/.ssh/id_dsa"; // NOI18N

        if (!isValidSSHKeyFile(key)) {
            key = System.getProperty("user.home") + "/.ssh/id_rsa"; // NOI18N
            if (!isValidSSHKeyFile(key)) {
                key = null;
            }
        }

        lastSSHKeyFile = key;
    }

    private Authentication(ExecutionEnvironment env) {
        pref_key = env == null ? null : Authentication.class.getName() + '_' + ExecutionEnvironmentFactory.toUniqueID(env);
        this.env = env;
    }

    public static Authentication getFor(ExecutionEnvironment env) {
        Authentication result = new Authentication(env);
        result.restore();

        if (isUnitTest) {
            result.setPassword();
        } else {
            if (result.sshKeyFile == null || result.sshKeyFile.trim().length() == 0) {
                result.sshKeyFile = lastSSHKeyFile;
            }
        }

        return result;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setAuthenticationMethods(MethodList methods) {
        authenticationMethods = methods;
    }

    public MethodList getAuthenticationMethods() {
        return authenticationMethods;
    }

    public boolean isDefined() {
        return type != Type.UNDEFINED;
    }

    public void setPassword() {
        if (type == Type.PASSWORD) {
            return;
        }

        type = Type.PASSWORD;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public void setSSHKeyFile(String filename) throws IllegalArgumentException {
        if (!isValidSSHKeyFile(filename)) {
            throw new IllegalArgumentException("Invalid ssh key file " + filename); // NOI18N
        }

        type = Type.SSH_KEY;
        sshKeyFile = filename;
    }

    public static boolean isValidSSHKeyFile(String filename) {
        JSch test = new JSch();

        try {
            test.addIdentity(filename);
        } catch (JSchException ex) {
            return false;
        }

        return true;
    }

    private static boolean isValidKnownHostsFile(String knownHostsFile) {
        JSch test = new JSch();

        try {
            test.setKnownHosts(knownHostsFile);
        } catch (JSchException ex) {
            return false;
        }

        return true;
    }

    public Type getType() {
        return type;
    }

    public void store() {
        if (env == null) {
            return;
        }

        if (type == Type.SSH_KEY) {
            prefs.put(pref_key, sshKeyFile);
            lastSSHKeyFile = sshKeyFile;
        } else {
            prefs.put(pref_key, type.name());
        }
        prefs.put(pref_key+METHODS_SUFFIX, authenticationMethods.toStorageString());
        prefs.putInt(pref_key+TIMEOUT_SUFFIX, timeout);
    }

    private void restore() {
        if (env == null) {
            return;
        }

        String typeOrKey = prefs.get(pref_key, Type.UNDEFINED.name());

        if (Type.UNDEFINED.name().equals(typeOrKey)) {
            type = Type.UNDEFINED;
        } else if (Type.PASSWORD.name().equals(typeOrKey)) {
            type = Type.PASSWORD;
        } else {
            if (isValidSSHKeyFile(typeOrKey)) {
                type = Type.SSH_KEY;
                sshKeyFile = typeOrKey;
            } else {
                type = Type.UNDEFINED;
            }
        }
        String storedText = prefs.get(pref_key+METHODS_SUFFIX, null);
        if (storedText != null && ! storedText.isEmpty()) {
            authenticationMethods = MethodList.fromStorageString(storedText);
        }
        timeout = prefs.getInt(pref_key+TIMEOUT_SUFFIX, timeout);
    }

    public ExecutionEnvironment getEnv() {
        return env;
    }

    public void remove() {
        if (pref_key != null) {
            prefs.remove(pref_key);
        }
    }

    public void apply() {
        if (env == null) {
            return;
        }

        store();
        ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();
        access.changeAuth(env, this);
    }

    public String getKey() {
        return getSSHKeyFile();
    }

    public String getSSHKeyFile() {
        return sshKeyFile;
    }

    public static enum Type {
        UNDEFINED(),
        PASSWORD(),
        SSH_KEY();
    }

    /**
     * Represents one of authentication methods.
     * The ID should be exactly the same as used in jsch
     */
    public static enum Method {

        GssapiWithMic("gssapi-with-mic", false), // NOI18N
        PublicKey("publickey", true), // NOI18N
        KeyboardInteractive("keyboard-interactive", false), // NOI18N
        Password("password", false); // NOI18N

        private final String id;
        private final boolean hasKeyFile;

        private Method(String name, boolean hasKeyFile) {
            this.id = name;
            this.hasKeyFile = hasKeyFile;
        }

        public String getID() {
            return id;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(Authentication.class, "Authentication." + id);
        }

        public boolean hasKeyFile() {
            return hasKeyFile;
        }

        private static Method byID(String id) {
            for (Method m : values()) {
                if (m.id.equals(id)) {
                    return m;
                }
            }
            return null;
        }
    }

    /**
     * The immutable list of ssh authentication methods.
     * Presents enabled/disabled state
     * and also methods order (which includes even disabled methods)
     */
    public static class MethodList {

        private final Method[] methods;
        private final boolean[] enabled;

        public MethodList(List<Pair<Method, Boolean>> pairs) {
            this.methods = new Method[pairs.size()];
            this.enabled = new boolean[pairs.size()];
            for (int i = 0; i < pairs.size(); i++) {
                this.methods[i] = pairs.get(i).first();
                this.enabled[i] = pairs.get(i).second();
            }
        }

        /**
         * @param pairs
         * @deprecated use {@link #MethodList(java.util.List)}
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public MethodList(Pair<Method, Boolean> ... pairs) {
            this.methods = new Method[pairs.length];
            this.enabled = new boolean[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                methods[i] = pairs[i].first();
                enabled[i] = pairs[i].second();
            }
        }

        public String toJschString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < methods.length; i++) {
                if (enabled[i]) {
                    if (sb.length() > 0) {
                        sb.append(','); //NOI18N
                    }
                    sb.append(methods[i].getID());
                }
            }
            return sb.toString();
        }


        public String toStorageString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < methods.length; i++) {
                if (sb.length() > 0) {
                    sb.append(','); //NOI18N
                }
                sb.append(methods[i].getID());
                sb.append('#'); //NOI18N
                sb.append(enabled[i] ? '1' : '0'); //NOI18N
            }
            return sb.toString();
        }

        public boolean isEmpty() {
            return methods.length == 0;
        }

        public Method[] getMethods() {
            return methods.clone();
        }

        public boolean isEnabled(Method method) {
            for (int i = 0; i < methods.length; i++) {
                if (methods[i] == method) {
                    return enabled[i];
                }
            }
            return false;
        }

        public boolean hasKeyFile() {
            for (int i = 0; i < methods.length; i++) {
                if (enabled[i] && methods[i].hasKeyFile()) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public String toString() {
            return toStorageString();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Arrays.deepHashCode(this.methods);
            hash = 89 * hash + Arrays.hashCode(this.enabled);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MethodList other = (MethodList) obj;
            if (!Arrays.deepEquals(this.methods, other.methods)) {
                return false;
            }
            if (!Arrays.equals(this.enabled, other.enabled)) {
                return false;
            }
            return true;
        }



        private static MethodList fromStorageString(String methodsList) {
            List<Pair<Method, Boolean>> pairs = new ArrayList<>();
            String[] parts = methodsList.split(","); // NOI18N
            for(int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (!part.isEmpty()) {
                    String name;
                    boolean enabled;
                    if (part.endsWith("#1")) { // NOI18N
                        name = part.substring(0, part.length() - 2);
                        enabled = true;
                    } else if (part.endsWith("#0")) { // NOI18N
                        name = part.substring(0, part.length() - 2);
                        enabled = false;
                    } else {
                        name = part;
                        enabled = true;
                    }
                    Method method = Method.byID(name);
                    if (method != null) {
                        pairs.add(Pair.of(method, enabled));
                    }
                }
            }
            return new MethodList(pairs);
        }
    }
}

