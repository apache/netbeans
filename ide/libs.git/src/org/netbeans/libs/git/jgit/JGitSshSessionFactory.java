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
package org.netbeans.libs.git.jgit;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.ProxyData;
import org.eclipse.jgit.transport.sshd.SshdSession;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.agent.ConnectorFactory;
import org.eclipse.jgit.util.FS;

public class JGitSshSessionFactory extends SshdSessionFactory {
    private static SshSessionFactory INSTANCE;
    private static final Logger LOG = Logger.getLogger(JGitSshSessionFactory.class.getName());
    private static final boolean USE_PROXY_TUNNELING = Boolean.getBoolean("git.lib.proxyHttpTunneling"); //NOI18N
    private volatile boolean disableAgent;
    private volatile Path identityFile;

    public static synchronized SshSessionFactory getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new JGitSshSessionFactory();
        }
        return INSTANCE;
    }

    @Override
    protected String getDefaultPreferredAuthentications() {
        if(identityFile != null) {
            return "publickey";
        } else {
            return "password,keyboard-interactive";
        }
    }

    @Override
    protected ConnectorFactory getConnectorFactory() {
        if(disableAgent) {
            return null;
        } else {
            return super.getConnectorFactory();
        }
    }

    @Override
    protected List<Path> getDefaultIdentities(File sshDir) {
        return identityFile == null ? List.of() : List.of(identityFile);
    }

    private JGitSshSessionFactory() {
        super(new JGitKeyCache(), (InetSocketAddress isa) -> {
            try {
                List<Proxy> proxies = ProxySelector.getDefault().select(
                        new URI("socket",
                                null,
                                isa.getHostString(),
                                isa.getPort() == -1 ? 22 : isa.getPort(),
                                null, null, null));
                if (!proxies.isEmpty()) {
                    Proxy p = proxies.iterator().next();
                    if (p.type() == Proxy.Type.DIRECT) {
                        return null;
                    } else if (USE_PROXY_TUNNELING) {
                        return new ProxyData(new Proxy(Proxy.Type.HTTP, p.address()));
                    } else {
                        return new ProxyData(new Proxy(Proxy.Type.SOCKS, p.address()));
                    }
                }
                return null;
            } catch (URISyntaxException ex) {
                LOG.log(Level.SEVERE, "Failed to determine proxy for " + isa, ex);
                return new ProxyData(Proxy.NO_PROXY);
            }
        });
    }


    @Override
    public synchronized SshdSession getSession (URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms) throws TransportException {
        // The call sequence assumes, that super#getSession will initialize
        // the session immediately. This method is sychronized so that each
        // client receives the correct configuration (see
        // #getDefaultPreferredAuthentications, #getConnectorFactory,
        // #getDefaultIdentities
        boolean agentUsed = false;
        String host = uri.getHost();
        CredentialItem.StringType identityFile = null;
        this.disableAgent = true;
        this.identityFile = null;
        if (credentialsProvider != null) {
            identityFile = new JGitCredentialsProvider.IdentityFileItem("Identity file for " + host, false);
            if (credentialsProvider.isInteractive() && credentialsProvider.get(uri, identityFile) && identityFile.getValue() != null) {
                LOG.log(Level.FINE, "Identity file for {0}: {1}", new Object[] { host, identityFile.getValue() }); //NOI18N
                this.disableAgent = false;
                this.identityFile = Path.of(identityFile.getValue());
                LOG.log(Level.FINE, "Setting cert auth for {0}, agent={1}", new Object[] { host, agentUsed }); //NOI18N
            }
        }
        LOG.log(Level.FINE, "Trying to connect to {0}, agent={1}", new Object[] { host, agentUsed }); //NOI18N
        return super.getSession(uri, credentialsProvider, fs, tms);
    }
}
