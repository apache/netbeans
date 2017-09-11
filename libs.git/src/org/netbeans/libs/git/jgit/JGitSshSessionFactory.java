/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.jgit;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.Buffer;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.Identity;
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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
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
        byHostName = new HashMap<String, JSch>();
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
            if (proxies.size() > 0) {
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
            Connector con = ConnectorFactory.getInstance().createConnector(ConnectorFactory.ConnectorKind.ANY);
            if (con != null) {
                IdentityRepository irepo = new IdentityRepositoryImpl(con);
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

    private static class IdentityRepositoryImpl implements IdentityRepository {
        private final Connector connector;
        private final AgentProxy proxy;

        public IdentityRepositoryImpl (Connector connector) {
            this.connector = connector;
            this.proxy = new AgentProxy(connector);
        }

        @Override
        public String getName () {
            return connector.getName();
        }

        @Override
        public int getStatus () {
            return connector.isAvailable() && proxy.isRunning()
                    ? IdentityRepository.RUNNING : IdentityRepository.UNAVAILABLE;
        }

        @Override
        public Vector getIdentities () {
            Identity[] identities = proxy.getIdentities();
            Vector<com.jcraft.jsch.Identity> result = new Vector<com.jcraft.jsch.Identity>(identities.length);
            for (final Identity identity : identities) {
                result.add(new com.jcraft.jsch.Identity() {
                    private byte[] publicKey;

                    @Override
                    public boolean setPassphrase (byte[] passphrase) throws JSchException {
                        return true;
                    }

                    @Override
                    public byte[] getPublicKeyBlob () {
                        if (publicKey == null) {
                            publicKey = identity.getBlob();
                        }
                        return identity.getBlob();
                    }

                    @Override
                    public byte[] getSignature (byte[] data) {
                        return proxy.sign(getPublicKeyBlob(), data);
                    }

                    @Override
                    public boolean decrypt () {
                        return true;
                    }

                    @Override
                    public String getAlgName () {
                        return new String((new Buffer(getPublicKeyBlob())).getString());
                    }

                    @Override
                    public String getName () {
                        return new String(identity.getComment());
                    }

                    @Override
                    public boolean isEncrypted () {
                        return false;
                    }

                    @Override
                    public void clear () {
                        
                    }
                });
            }
            return result;
        }

        @Override
        public boolean add (byte[] bytes) {
            // not supported now
            return false;
        }

        @Override
        public boolean remove (byte[] bytes) {
            // not supported now
            return false;
        }

        @Override
        public void removeAll () {
            // not supported now
        }
    }
    
}
