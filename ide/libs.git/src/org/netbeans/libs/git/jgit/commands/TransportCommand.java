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

package org.netbeans.libs.git.jgit.commands;

import com.jcraft.jsch.JSchException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportProtocol;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
abstract class TransportCommand extends GitCommand {
    private static final String PROP_ENV_GIT_SSH = "GIT_SSH"; //NOI18N
    private static final String PROP_GIT_SSH_SYSTEM_CLIENT = "versioning.git.library.useSystemSSHClient"; //NOI18N
    private CredentialsProvider credentialsProvider;
    private static final CredentialsProvider DEFAULT_PROVIDER = new CredentialsProvider() {

        @Override
        public boolean isInteractive () {
            return false;
        }

        @Override
        public boolean supports (CredentialItem... items) {
            for (CredentialItem i : items) {
                if (!(i instanceof CredentialItem.Username
                        || i instanceof CredentialItem.Password)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean get (URIish uriish, CredentialItem... items) throws UnsupportedCredentialItem {
            String user = uriish.getUser();
            if (user == null) {
                user = "";
            }
            String password = uriish.getPass();
            if (password == null) {
                password = "";
            }
            for (CredentialItem i : items) {
                if (i instanceof CredentialItem.Username) {
                    ((CredentialItem.Username) i).setValue(user);
                    continue;
                }
                if (i instanceof CredentialItem.Password) {
                    ((CredentialItem.Password) i).setValue(password.toCharArray());
                    continue;
                }
                if (i instanceof CredentialItem.StringType) {
                    if (i.getPromptText().equals("Password: ")) { //NOI18N
                        ((CredentialItem.StringType) i).setValue(password);
                        continue;
                    }
                }
                throw new UnsupportedCredentialItem(uriish, i.getClass().getName()
                        + ":" + i.getPromptText()); //NOI18N
            }
            return true;
        }
    };
    
    private final String remote;
    private static final Logger LOG = Logger.getLogger(TransportCommand.class.getName());

    public TransportCommand (Repository repository, GitClassFactory gitFactory, String remote, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remote;
    }

    protected final URIish getUri (boolean pushUri) throws URISyntaxException {
        RemoteConfig config = getRemoteConfig();
        List<URIish> uris;
        if (config == null) {
            uris = Collections.emptyList();
        } else {
            if (pushUri) {
                uris = config.getPushURIs();
                if (uris.isEmpty()) {
                    uris = config.getURIs();
                }
            } else {
                uris = config.getURIs();
            }
        }
        if (uris.isEmpty()) {
            return new URIish(remote);
        } else {
            return uris.get(0);
        }
    }

    protected final URIish getUriWithUsername (boolean pushUri) throws URISyntaxException {
        URIish uri = getUri(pushUri);
        if (credentialsProvider != null) {
            CredentialItem.Username itm = new CredentialItem.Username();
            credentialsProvider.get(uri, itm);
            if (itm.getValue() != null) {
                if (itm.getValue().isEmpty()) {
                    uri = uri.setUser(null);
                } else {
                    uri = uri.setUser(itm.getValue());
                }
            }
        }
        return uri;
    }

    public final void setCredentialsProvider (CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider == null ? DEFAULT_PROVIDER : credentialsProvider;
    }
    
    @Override
    public final void run () throws GitException {
        SystemReader original = SystemReader.getInstance();
        String externalTool = original.getenv(PROP_ENV_GIT_SSH);
        boolean replace = externalTool != null;
        if ("true".equals(System.getProperty(PROP_GIT_SSH_SYSTEM_CLIENT, "false"))) { // NOI18N
            replace = false;
        }
        try {
            if (replace) {
                LOG.log(Level.WARNING, "{0} set to {1}, ignoring and using the default implementation via JSch", new Object[] { PROP_ENV_GIT_SSH, externalTool }); //NOI18N
                SystemReader.setInstance(new DelegatingSystemReader(original));
            }
            runTransportCommand();
        } finally {
            if (replace) {
                SystemReader.setInstance(original);
            }
        }
    }
    
    protected final CredentialsProvider getCredentialsProvider () {
        return credentialsProvider;
    }
    
    protected final RemoteConfig getRemoteConfig () throws URISyntaxException {
        RemoteConfig config = new RemoteConfig(getRepository().getConfig(), remote);
        if (config.getURIs().isEmpty() && config.getPushURIs().isEmpty()) {
            return null;
        } else {
            return config;
        }
    }
    
    protected Transport openTransport (boolean openPush) throws URISyntaxException, NotSupportedException, TransportException {
        URIish uri = getUriWithUsername(openPush);
        // WA for #200693, jgit fails to initialize ftp protocol
        for (TransportProtocol proto : Transport.getTransportProtocols()) {
            if (proto.getSchemes().contains("ftp")) { //NOI18N
                Transport.unregister(proto);
            }
        }
        try {
            Transport transport = Transport.open(getRepository(), uri);
            RemoteConfig config = getRemoteConfig();
            if (config != null) {
                transport.applyConfig(config);
            }
            if (transport.getTimeout() <= 0) {
                transport.setTimeout(45);
            }
            transport.setCredentialsProvider(getCredentialsProvider());
            return transport;
        } catch (IllegalArgumentException ex) {
            throw new TransportException(ex.getLocalizedMessage(), ex);
        }
    }
    
    protected final void handleException (TransportException e, URIish uri) throws GitException.AuthorizationException, GitException {
        String message = e.getMessage();
        int pos;
        if (message == null) {
            throw new GitException(e);
        } else if ((pos = message.indexOf(": not authorized")) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if ((pos = message.indexOf(": " + HttpURLConnection.HTTP_UNAUTHORIZED + " ")) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if (message.contains("not authorized")) { //NOI18N
            throw new GitException.AuthorizationException(uri.toString(), message, e);
        } else if ((pos = message.toLowerCase().indexOf(": auth cancel")) != -1) { //NOI18N
            String repositoryUrl = message.substring(0, pos);
            throw new GitException.AuthorizationException(repositoryUrl, message, e);
        } else if (e.getCause() instanceof JSchException) {
            if (message.contains("timeout:") || message.contains("ProxyHTTP")
                    || message.contains("ProxySOCKS4")
                    || message.contains("ProxySOCKS5")) { //NOI18N
                throw new GitException(message, e);
            } else {
                throw new GitException.AuthorizationException(uri.toString(), message, e);
            }
        } else {
            throw new GitException(message, e);
        }
    }

    protected abstract void runTransportCommand () throws GitException;
    
    private static class DelegatingSystemReader extends SystemReader {
        private final SystemReader instance;

        public DelegatingSystemReader (SystemReader sr) {
            this.instance = sr;
        }

        @Override
        public String getHostname () {
            return instance.getHostname();
        }

        @Override
        public String getenv (String string) {
            if (PROP_ENV_GIT_SSH.equals(string)) {
                return null;
            }
            return instance.getenv(string);
        }

        @Override
        public String getProperty (String string) {
            return instance.getProperty(string);
        }

        @Override
        public FileBasedConfig openUserConfig (Config config, FS fs) {
            return instance.openUserConfig(config, fs);
        }

        @Override
        public FileBasedConfig openSystemConfig (Config config, FS fs) {
            return instance.openSystemConfig(config, fs);
        }

        @Override
        public long getCurrentTime () {
            return instance.getCurrentTime();
        }

        @Override
        public int getTimezone (long l) {
            return instance.getTimezone(l);
        }

        @Override
        public FileBasedConfig openJGitConfig(Config config, FS fs) {
            return instance.openJGitConfig(config, fs);
        }
    }
}
