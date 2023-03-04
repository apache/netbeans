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
package org.netbeans.modules.java.j2seembedded.platform;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.Parameters;


/**
 *
 * @author Tomas Zezula
 */
public final class ConnectionMethod {

    private static final Logger LOG = Logger.getLogger(ConnectionMethod.class.getName());

    private static final String PLAT_PROP_HOST = "platform.host";                       //NOI18N
    private static final String PLAT_PROP_PORT = "platform.port";                       //NOI18N

    public abstract static class Authentification {

        private static final String PLAT_PROP_AUTH_KIND = "platform.auth.kind";         //NOI18N
        private static final String PLAT_PROP_AUTH_USER = "platform.auth.username";     //NOI18N


        public static enum Kind {
            PASSWORD {
                @NonNull
                @Override
                Authentification create(@NonNull final Map<String,String> props) {
                    String user = props.get(PLAT_PROP_AUTH_USER);
                    if (user == null) {
                        throw new IllegalStateException("No user"); //NOI18N
                    }
                    char[] passwd = Keyring.read(RemotePlatformProvider.createPropertyName(
                        props.get(RemotePlatform.PLAT_PROP_ANT_NAME),
                        Password.PLAT_PROP_AUTH_PASSWD));
                    final String passwdStr;
                    if (passwd == null) {
                        LOG.log(
                            Level.WARNING,
                            "No password for: {0} for platform: {1}",   //NOI18N
                            new Object[]{
                                user,
                                props.get(RemotePlatform.PLAT_PROP_ANT_NAME)
                            });
                        passwdStr = ""; //NOI18N
                    } else {
                        passwdStr = String.valueOf(passwd);
                    }
                    return new Password(
                        user,
                        passwdStr);
                }
            },
            KEY {
                @NonNull
                @Override
                Authentification create(@NonNull final Map<String,String> props) {
                    String user = props.get(PLAT_PROP_AUTH_USER);
                    if (user == null) {
                        throw new IllegalStateException("No user"); //NOI18N
                    }
                    final String keyStore = props.get(Key.PLAT_PROP_AUTH_KEYSTORE);
                    if (keyStore == null) {
                        throw new IllegalStateException("No key store");    //NOI18N
                    }
                    final char[] passPhrase = Keyring.read(RemotePlatformProvider.createPropertyName(
                        props.get(RemotePlatform.PLAT_PROP_ANT_NAME),
                        Key.PLAT_PROP_AUTH_PASSPHRASE));
                    final String passPhraseStr;
                    if (passPhrase == null) {
                        LOG.log(
                            Level.WARNING,
                            "No passprase for: {0} for platform: {1}",   //NOI18N
                            new Object[]{
                                user,
                                props.get(RemotePlatform.PLAT_PROP_ANT_NAME)
                            });
                        passPhraseStr = "";
                    } else {
                        passPhraseStr = String.valueOf(passPhrase);
                    }
                    return new Key(
                        user,
                        new File(keyStore),
                        passPhraseStr);
                }
            };

            @NonNull
            abstract Authentification create(@NonNull final Map<String,String> props);
        }

        private final Kind kind;
        private final String userName;

        private Authentification(
            @NonNull final Kind kind,
            @NonNull final String userName) {
            Parameters.notNull("kind", kind);   //NOI18N
            Parameters.notNull("userName", userName);   //NOI18N
            this.kind = kind;
            this.userName = userName;
        }

        @NonNull
        public Kind getKind() {
            return kind;
        }

        @NonNull
        public String getUserName() {
            return userName;
        }
        
        void store(@NonNull final Map<String,String> props) {
            props.put(PLAT_PROP_AUTH_KIND, getKind().toString());
            props.put(PLAT_PROP_AUTH_USER, getUserName());
        }

        @NonNull
        Collection<String> getGlobalPropertyNames() {
            final Set<String> result = new HashSet<>();
            result.add(PLAT_PROP_AUTH_KIND);
            result.add(PLAT_PROP_AUTH_USER);
            return Collections.unmodifiableSet(result);
        }
        
        @NonNull
        static Authentification load(@NonNull final Map<String,String> props) {
            final String _kind = props.get(PLAT_PROP_AUTH_KIND);
            if (_kind == null) {
                throw new IllegalStateException("No authentification kind");
            }
            final Kind kind = Kind.valueOf(_kind);
            return kind.create(props);
        }

        static void clear(@NonNull final String antPlatformName) {
            Password.clear(antPlatformName);
            Key.clear(antPlatformName);
        }        

        public static final class Password extends Authentification {

            private static final String PLAT_PROP_AUTH_PASSWD = "platform.auth.passwd";         //NOI18N

            private final String password;  //TODO: Store in keystore

            private Password(
                @NonNull final String userName,
                @NonNull final String password) {
                super(Kind.PASSWORD, userName);
                Parameters.notNull("password", password);   //NOI18N
                this.password = password;
            }

            @NonNull
            public String getPassword() {
                return password;
            }

            @NonNull
            @Override
            void store(@NonNull final Map<String,String> props) {
                super.store(props);
                Keyring.save(
                    RemotePlatformProvider.createPropertyName(
                        props.get(RemotePlatform.PLAT_PROP_ANT_NAME),
                        PLAT_PROP_AUTH_PASSWD),
                    getPassword().toCharArray(),
                    null);
            }

            static void clear(@NonNull final String antPlatformName) {
                Keyring.delete(RemotePlatformProvider.createPropertyName(antPlatformName, PLAT_PROP_AUTH_PASSWD));
            }
        }

        public static final class Key extends Authentification {

            private static final String PLAT_PROP_AUTH_KEYSTORE = "platform.auth.keystore";         //NOI18N
            private static final String PLAT_PROP_AUTH_PASSPHRASE = "platform.auth.passphrase";     //NOI18N

            private final File keyStore;
            private final String passPhrase;    //TODO: Store in keystore

            private Key(
                @NonNull final String userName,
                @NonNull final File keyStore,
                @NonNull final String passPhrase) {
                super(Kind.KEY, userName);
                Parameters.notNull("keyStore", keyStore);   //NOI18N
                Parameters.notNull("passphrase", passPhrase);   //NOI18N
                this.keyStore = keyStore;
                this.passPhrase = passPhrase;
            }

            @NonNull
            public File getKeyStore() {
                return keyStore;
            }

            @NonNull
            public String getPassPhrase() {
                return passPhrase;
            }

            @Override
            void store(@NonNull final Map<String,String> props) {
                super.store(props);
                props.put(PLAT_PROP_AUTH_KEYSTORE, getKeyStore().getAbsolutePath());
                Keyring.save(
                    RemotePlatformProvider.createPropertyName(
                        props.get(RemotePlatform.PLAT_PROP_ANT_NAME),
                        PLAT_PROP_AUTH_PASSPHRASE),
                    getPassPhrase().toCharArray(),
                    null);
            }

            static void clear(@NonNull final String antPlatformName) {
                Keyring.delete(RemotePlatformProvider.createPropertyName(antPlatformName, PLAT_PROP_AUTH_PASSPHRASE));
            }

            @NonNull
            @Override
            Collection<String> getGlobalPropertyNames() {
                final Set<String> result = new HashSet<>();
                result.addAll(super.getGlobalPropertyNames());
                result.add(PLAT_PROP_AUTH_KEYSTORE);
                return  Collections.unmodifiableSet(result);
            }
        }
    }
    
    private final String host;
    private final int port;
    private final Authentification auth;

    private ConnectionMethod(
        @NonNull final String host,
        final int port,
        final Authentification auth) {
        Parameters.notNull("host", host);   //NOI18N
        Parameters.notNull("auth", auth);   //NOI18N
        this.host = host;
        this.port = port;
        this.auth = auth;

    }

    @NonNull
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @NonNull
    public Authentification getAuthentification() {
        return auth;
    }

    void store(@NonNull final Map<String,String> props) {
        props.put(PLAT_PROP_HOST,getHost());
        props.put(PLAT_PROP_PORT,Integer.toString(getPort()));
        auth.store(props);
    }

    @NonNull
    Collection<String> getGlobalPropertyNames() {
        final Set<String> result = new HashSet<>();
        result.add(PLAT_PROP_HOST);
        result.add(PLAT_PROP_PORT);
        result.addAll(auth.getGlobalPropertyNames());
        return Collections.unmodifiableSet(result);
    }

    @NonNull
    public static ConnectionMethod sshPassword(
        @NonNull final String host,
        @NonNull final int port,
        @NonNull final String username,
        @NonNull final String password) {
        return new ConnectionMethod(host, port, new Authentification.Password(username, password));
    }

    @NonNull
    public static ConnectionMethod sshKey(
       @NonNull final String host,
       @NonNull final int port,
       @NonNull final String username,
       @NonNull final File keyFile,
       @NonNull final String passphrase) {
        return new ConnectionMethod(host, port, new Authentification.Key(username, keyFile, passphrase));
    }

    @NonNull
    public static ConnectionMethod load(@NonNull final Map<String,String> props) {
        final String host = props.get(PLAT_PROP_HOST);
        if (host == null) {
            throw new IllegalStateException("No platform host");    //NOI18N
        }
        final String _port = props.get(PLAT_PROP_PORT);
        if (_port == null) {
            throw new IllegalStateException("No platfrom port");    //NOI18N
        }
        try {
            final int port = Integer.parseInt(_port);
            return new ConnectionMethod(host, port, Authentification.load(props));
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Invalid platfrom port: " + _port);    //NOI18N
        }
    }

}
