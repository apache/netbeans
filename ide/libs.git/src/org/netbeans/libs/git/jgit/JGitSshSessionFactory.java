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

import com.jcraft.jsch.AgentConnector;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig.Host;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.jsch.agentproxy.ConnectorFactory;

/**
 *
 * @author ondra
 */
public class JGitSshSessionFactory extends JschConfigSessionFactory {
    static {
        // jgit modifies the configuration and relies on the config keys
        // signature.rsa and signature.dss to be present. In recent JSch
        // versions, these entries are not all present, so this patches
        // them in again
        if(JSch.getConfig("ssh-rsa") != null && JSch.getConfig("signature.rsa") == null) {
            JSch.setConfig("signature.rsa", JSch.getConfig("ssh-rsa"));
        }
        if (JSch.getConfig("ssh-dss") != null && JSch.getConfig("signature.dss") == null) {
            JSch.setConfig("signature.dss", JSch.getConfig("ssh-dss"));
        }
    }

    private OpenSshConfig sshConfig;
    private static SshSessionFactory INSTANCE;
    private static final Logger LOG = Logger.getLogger(JGitSshSessionFactory.class.getName());
    private static final boolean USE_PROXY_TUNNELING = Boolean.getBoolean("git.lib.proxyHttpTunneling"); //NOI18N

    public static synchronized SshSessionFactory getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new JGitSshSessionFactory();
        }
        return INSTANCE;
    }
    private JSch defaultJSch;
    private final Map<String, JSch> byHostName;

    public JGitSshSessionFactory () {
        byHostName = new HashMap<>();
    }

    @Override
    protected void configure (Host host, Session sn) {
        sn.setConfig("PreferredAuthentications", "publickey,password,keyboard-interactive"); //NOI18N
    }

    @Override
    public synchronized RemoteSession getSession (URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms) throws TransportException {
        boolean agentUsed = false;
        String host = uri.getHost();
        CredentialItem.StringType identityFile = null;
        if (credentialsProvider != null) {
            identityFile = new JGitCredentialsProvider.IdentityFileItem("Identity file for " + host, false);
            if (credentialsProvider.isInteractive() && credentialsProvider.get(uri, identityFile) && identityFile.getValue() != null) {
                LOG.log(Level.FINE, "Identity file for {0}: {1}", new Object[] { host, identityFile.getValue() }); //NOI18N
                agentUsed = setupJSch(fs, host, identityFile, uri, true);
                LOG.log(Level.FINE, "Setting cert auth for {0}, agent={1}", new Object[] { host, agentUsed }); //NOI18N
            }
        }
        try {
            LOG.log(Level.FINE, "Trying to connect to {0}, agent={1}", new Object[] { host, agentUsed }); //NOI18N
            return super.getSession(uri, credentialsProvider, fs, tms);
        } catch (Exception ex) {
            // catch rather all exceptions. In case jsch-agent-proxy is broken again we should
            // at least fall back on key/pasphrase
            if (agentUsed) {
                LOG.log(ex instanceof TransportException ? Level.FINE : Level.INFO, null, ex);
                setupJSch(fs, host, identityFile, uri, false);
                LOG.log(Level.FINE, "Trying to connect to {0}, agent={1}", new Object[] { host, false }); //NOI18N
                return super.getSession(uri, credentialsProvider, fs, tms);
            } else {
                LOG.log(Level.FINE, "Connection failed: {0}", host); //NOI18N
                throw ex;
            }
        }
    }

    @Override
    protected JSch getJSch (Host hc, FS fs) throws JSchException {
        // default jsch to gain known hosts from
        if (defaultJSch == null) {
            defaultJSch = createDefaultJSch(fs);
            final File home = fs.userHome();
            if (home != null) {
                File known_hosts = new File(new File(home, ".ssh"), "known_hosts"); //NOI18N
                defaultJSch.setKnownHosts(known_hosts.getAbsolutePath());
            }
            defaultJSch.removeAllIdentity();
        }
        String hostName = hc.getHostName();
        JSch jsch = byHostName.get(hostName);
        if (jsch == null) {
            jsch = new JSch();
            jsch.setHostKeyRepository(defaultJSch.getHostKeyRepository());
            byHostName.put(hostName, jsch);
        }
        return jsch;
    }

    @Override
    protected Session createSession (Host hc, String user, String host, int port, FS fs) throws JSchException {
        Session session = super.createSession(hc, user, host, port, fs);
        try {
            List<Proxy> proxies = ProxySelector.getDefault().select(new URI("socket",
                    null,
                    host,
                    port == -1 ? 22 : port,
                    null, null, null));
            if (!proxies.isEmpty()) {
                Proxy p = proxies.iterator().next();
                if (p.type() == Proxy.Type.DIRECT) {
                    session.setProxy(null);
                } else {
                    SocketAddress addr = p.address();
                    if (addr instanceof InetSocketAddress) {
                        InetSocketAddress inetAddr = (InetSocketAddress) addr;
                        String proxyHost = inetAddr.getHostName();
                        int proxyPort = inetAddr.getPort();
                        session.setProxy(createProxy(proxyHost, proxyPort));
                    }
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(JGitSshSessionFactory.class.getName()).log(Level.INFO, "Invalid URI: " + host + ":" + port, ex);
        }
        return session;
    }

    private boolean setupJSchIdentityRepository (JSch jsch, String identityFile, boolean preferAgent) throws JSchException {
        boolean agentUsed = false;
        if (preferAgent) {
            AgentConnector agentConnector = ConnectorFactory.getInstance().createConnector(ConnectorFactory.ConnectorKind.ANY);
            if (agentConnector != null) {
                IdentityRepository irepo = new AgentIdentityRepository(agentConnector);
                if (irepo.getStatus() == IdentityRepository.RUNNING) {
                    jsch.setIdentityRepository(irepo);
                    agentUsed = true;
                }
            }
        }
        if (!agentUsed) {
            jsch.setIdentityRepository(null);
            // remove all identity files
            jsch.removeAllIdentity();
            // and add the one specified by CredentialsProvider
            jsch.addIdentity(identityFile);
        }
        return agentUsed;
    }

    private boolean setupJSch (FS fs, String host, CredentialItem.StringType identityFile, URIish uri, boolean preferAgent) throws TransportException {
        boolean agentUsed;
        if (sshConfig == null) {
            sshConfig = OpenSshConfig.get(fs);
        }
        final OpenSshConfig.Host hc = sshConfig.lookup(host);
        try {
            JSch jsch = getJSch(hc, fs);
            agentUsed = setupJSchIdentityRepository(jsch, identityFile.getValue(), preferAgent);
        } catch (JSchException ex) {
            throw new TransportException(uri, ex.getMessage(), ex);
        }
        return agentUsed;
    }

    private com.jcraft.jsch.Proxy createProxy (String proxyHost, int proxyPort) {
        return USE_PROXY_TUNNELING
                ? new ProxyHTTP(proxyHost, proxyPort)
                : new ProxySOCKS5(proxyHost, proxyPort);
    }

}
