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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.ConfigurableLauncher;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.gradle.options.NetworkProxySettings;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for proxy autodetection or autoconfiguration. The class works with {@link GradleExperimentalSettings} and {@link NetworkProxySettings} to determine
 * the behaviour:
 * <ul>
 * <li>{@link NetworkProxySettings#IGNORE} - skip autodetection at all
 * <li>{@link NetworkProxySettings#NOTICE} - just note mismatch, do not update configuration and continue building
 * <li>{@link NetworkProxySettings#OVERRIDE} - override gradle.properties by explicit system properties
 * <li>{@link NetworkProxySettings#UPDATE} - automatically update settings
 * {@link NetworkProxySettings#ASK} - ask the user
 * </ul>
 * The user choice is remembered so for the same project and detected proxy, the question is not asked again. Also notice is displayed just once for project+detected proxy,
 * so the log is not full of reminders.
 * 
 * @author sdedic
 */
@ProjectServiceProvider(service = GradleNetworkProxySupport.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleNetworkProxySupport {
    private static final Logger LOG = Logger.getLogger(GradleNetworkProxySupport.class.getName());
    /**
     * Sample probe URI - google's public DNS server
     */
    private static final String PROBE_URI_STRING = "http://search.maven.org"; // NOI18N
    
    private static final String FILENAME_SUFFIX_OLD = ".old"; // NOI18N
    private static final String SYSTEMPROP_HTTPS_PROXYPORT = "systemProp.https.proxyPort"; // NOI18N
    private static final String SYSTEMPROP_HTTP_PROXYPORT = "systemProp.http.proxyPort"; // NOI18N
    private static final String SYSTEMPROP_HTTPS_PROXYHOST = "systemProp.https.proxyHost"; // NOI18N
    private static final String SYSTEMPROP_HTTP_PROXYHOST = "systemProp.http.proxyHost"; // NOI18N

    private static final String JVM_HTTPS_PROXYPORT = "https.proxyPort"; // NOI18N
    private static final String JVM_HTTP_PROXYPORT = "http.proxyPort"; // NOI18N
    private static final String JVM_HTTPS_PROXYHOST = "https.proxyHost"; // NOI18N
    private static final String JVM_HTTP_PROXYHOST = "http.proxyHost"; // NOI18N
    
    private static final int PORT_DEFAULT_HTTPS = 1080;
    private static final int PORT_DEFAULT_HTTP = 80;
    
    /**
     * Timeout for the network probe. The probe is done in case project settings mismatch with the autodetected ones.
     * If set to 0 or negative number, the project proxy configuration will not be probed.
     */
    private static final int PROXY_PROBE_TIMEOUT = Integer.getInteger("netbeans.networkProxy.timeout", 1000);
    
    private final Project project;
    
    /**
     * Past decisions made by the user during this session. The Map is used so the user si not bothered that often with questions.
     * If the user chooses 'override' or 'continue' (no action), the Map receives the public proxy spec and the result. If the same
     * effective proxy is detected, the user is not asked again.
     */
    // @GuardedBy(this)
    private static Map<String, ProxyResult>    acknowledgedResults = new HashMap<>();
    
    public GradleNetworkProxySupport(Project project) {
        this.project = project;
    }
    
    public CompletableFuture<ProxyResult> checkProxySettings() {
        return new Processor().checkProxy();
    }
    
    public enum Status {
        UNKNOWN,
        CONTINUE,
        RECONFIGURED,
        OVERRIDE,
        ABORT
    }
    
    public static final class ProxyResult {
        private final Status status;
        private final Proxy proxy;
        private final String toolProxy;
        private final String proxyHost;
        private final String proxySpec;
        private final int proxyPort;

        public ProxyResult(Status status, Proxy proxy) {
            this.status = status;
            this.proxy = proxy;
            this.toolProxy = null;
            this.proxySpec = null;
            this.proxyHost = null;
            this.proxyPort = -1;
        }
        
        public ProxyResult(Status status, Proxy proxy, String toolProxy, String proxySpec, String proxyHost, int proxyPort) {
            this.status = status;
            this.proxy = proxy;
            this.toolProxy = toolProxy;
            this.proxySpec = proxySpec;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }

        public Status getStatus() {
            return status;
        }

        public Proxy getProxy() {
            return proxy;
        }

        public String getToolProxy() {
            return toolProxy;
        }

        public String getProxySpec() {
            return proxySpec;
        }
        
        public <T > BuildActionExecuter<T> configure(BuildActionExecuter<T> executor) {
            configure((ConfigurableLauncher)executor);
            return executor;
        }
        
        public <T extends ConfigurableLauncher> T configure(T executor) {
            if (status != Status.OVERRIDE) {
                return executor;
            }
            addSystemProperty(executor, JVM_HTTP_PROXYHOST, proxyHost);
            addSystemProperty(executor, JVM_HTTP_PROXYPORT, Integer.toString(proxyPort));
            addSystemProperty(executor, JVM_HTTPS_PROXYHOST, proxyHost);
            addSystemProperty(executor, JVM_HTTPS_PROXYPORT, Integer.toString(proxyPort));
            
            return executor;
        }
        
        private void addSystemProperty(ConfigurableLauncher<?> executer, String propName, String value) {
            executer.addJvmArguments("-D" + propName + "=" + (value == null ? "" : value));
        }
    }

    @NbBundle.Messages({
        "TITLE_GradleProxyMismatch=Possible Network Proxy Issue",
        "# {0} - gradle proxy",
        "MSG_ProxyMisconfiguredDirect=Gradle is configured for a proxy {0}, but the system does not require a proxy for network connections. Proxy settings should be removed from user gradle.properties.",
        "# {0} - system proxy",
        "MSG_ProxyMisconfiguredMissing=Gradle is not configured to use a network proxy, but the proxy {0} seems to be required for network communication. User gradle.properties should be updated to specify a proxy.",
        "# {0} - system proxy",
        "# {1} - gradle proxy",
        "MSG_ProxyMisconfiguredOther=Gradle is configured to use a network proxy {1}, but the proxy {0} seems to be required for network communication. Proxy settings should be updated in user gradle.properties.",
        "MSG_AppendAskUpdate=\nUpdate Gradle configuration ? Choose \"Override\" to apply detected proxy only to IDE operations.",
        "MSG_AppendAskUpdate2=\nUpdate Gradle configuration ?",
        "ACTION_Override=Override",
        "ACTION_Continue=Keep settings",
        "# {0} - date/time of the update",
        "COMMENT_CreatedByNetBeans=# This proxy configuration has been updated by Apache NetBeans on {0}",
        "TITLE_ConfigUpdateFailed=Configuration update failed",
        "# {0} - error message",
        "ERROR_ConfigUpdateFailed=Failed to modify Gradle user properties: {0}",
        "# {0} - proxy specification",
        "MSG_ProxySetTo=Gradle Network proxy set to: {0}",
        "MSG_ProxyCleared=Gradle Network proxy removed",
        
        "# Branding API: change to false to disable suggestion to override proxies in Gradle invocation",
        "CTRL_SuggestProxyOverride=true"
    })
    /**
     * Encapsulates a single check to avoid an enormous method or a ton of parameters passed through
     * a method chain. Should be constructed for each new check separately.
     */
    private class Processor {
        Proxy publicProxy;
        String publicProxyHost;
        int publicProxyPort;
        int publicProxyNonDefaultPort;
        
        String proxyAuthority;
        String proxyHost;
        String publicProxySpec;
        
        int proxyPort;
        GradleFiles gradleFiles;
        
        public CompletableFuture<ProxyResult> checkProxy() {
            boolean supportOverride = NetworkProxySettings.allowProxyOverride();
            NetworkProxySettings action = GradleExperimentalSettings.getDefault().getNetworkProxy();
            if (action == NetworkProxySettings.IGNORE) {
                return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
            }
            
            obtainPublicProxy();
            loadProjectProxy();
            
            boolean direct = publicProxy == null || publicProxy.type() == Proxy.Type.DIRECT;
            
            if (direct && proxyAuthority == null || gradleFiles == null) {
                LOG.log(Level.FINE, "Project does not specify a proxy and none is needed");
                return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
            }
            
            if (publicProxy != null) {
                if (publicProxyHost == null) {
                    // unable to decipher proxy address
                    LOG.log(Level.WARNING, "Unable to decipher proxy: {0}", publicProxy);
                    return CompletableFuture.completedFuture(new ProxyResult(Status.UNKNOWN, null));
                }
                if (publicProxyHost.equals(proxyHost) && proxyPort == publicProxyPort) {
                    LOG.log(Level.FINE, "Project specifies detected proxy: {0}", publicProxySpec);
                    return CompletableFuture.completedFuture(new ProxyResult(Status.CONTINUE, publicProxy));
                }
            }
            
            // at this point, it's obvious that 
            
            String userMessage;
            
            if (direct) {
                userMessage = Bundle.MSG_ProxyMisconfiguredDirect(proxyAuthority);
            } else if (proxyAuthority == null) {
                userMessage = Bundle.MSG_ProxyMisconfiguredMissing(publicProxySpec);
            } else {
                userMessage = Bundle.MSG_ProxyMisconfiguredOther(publicProxySpec, proxyAuthority);
            }
            
            ProxyResult result;
            synchronized (this) {
                result = acknowledgedResults.get(publicProxySpec);
            }
            if (result != null) {
                LOG.log(Level.FINE, "Reusing previous decision: {0} with proxy {1}", new Object[] { result.getStatus(), result.proxySpec });
                switch (result.getStatus()) {
                    case CONTINUE:
                        // includes noth NOTICE and IGNORE settings !
                        action = NetworkProxySettings.IGNORE;
                        break;
                    case OVERRIDE: 
                        action = NetworkProxySettings.OVERRIDE;
                        break;
                    case RECONFIGURED:
                        action = NetworkProxySettings.UPDATE;
                        break;
                }
            }
            // TODO: because of some strange gradle tooling API behaviour, it is not possible to 
            // override ~/.gradle/gradle.properties system properties with values passed on the commandline or -D ...
            // ... but ./gradlew works for some strange reason.
            // See https://github.com/gradle/gradle/issues/22856
            if (proxyHost != null) {
                supportOverride = false;
                if (action == NetworkProxySettings.OVERRIDE) {
                    action = NetworkProxySettings.NOTICE;
                }
            }
            if (action != NetworkProxySettings.IGNORE && PROXY_PROBE_TIMEOUT > 0) {
                // last check: make an outbound connection to a public site
                URL probeUrl;
                P: try {
                    Proxy probeProxy;
                    
                    if (proxyHost != null) {
                        LOG.log(Level.FINE, "Trying to probe with proxy {0}", proxyAuthority);
                        InetSocketAddress sa = new InetSocketAddress(proxyHost, proxyPort);
                        if (!sa.isUnresolved()) {
                            probeProxy = new Proxy(Proxy.Type.HTTP, sa);
                        } else {
                            LOG.log(Level.FINE, "Tool proxy {0} probe not resolvable", proxyAuthority);
                            break P;
                        }
                    } else {
                        probeProxy = Proxy.NO_PROXY;
                    }
                    probeUrl = new URL(PROBE_URI_STRING);
                    HttpURLConnection c = null;
                    try {
                        c = (HttpURLConnection)probeUrl.openConnection(probeProxy);
                        c.setReadTimeout(PROXY_PROBE_TIMEOUT);
                        c.setConnectTimeout(PROXY_PROBE_TIMEOUT);
                        c.setRequestMethod("HEAD");
                        c.connect();
                        // force something through
                        c.getLastModified();
                        return CompletableFuture.completedFuture(new ProxyResult(Status.CONTINUE, probeProxy, proxyAuthority, publicProxySpec, publicProxyHost, publicProxyPort));
                    } catch (IOException ex) {
                        // the probe has failed
                        LOG.log(Level.FINE, "Tool proxy {0} probe failed", proxyAuthority);
                    } finally {
                        if (c != null) {
                            c.disconnect();
                        }
                    }
                } catch (MalformedURLException ex) {
                    // this is competely unexpected
                    Exceptions.printStackTrace(ex);
                }
            }
            switch (action) {
                case IGNORE:
                    return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
                    
                case NOTICE:
                    NotificationDisplayer.getDefault().notify(
                            Bundle.TITLE_GradleProxyMismatch(),
                            NbGradleProject.getIcon(),
                            userMessage, null, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.WARNING);
                    return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
                
                case OVERRIDE:
                    return CompletableFuture.completedFuture(createResult(Status.OVERRIDE));
                    
                case UPDATE:
                    return CompletableFuture.completedFuture(updateGradleConfiguration(false));
                    
                case ASK:
                    if (result != null) {
                        return CompletableFuture.completedFuture(result);
                    }
                    String promptMsg;
                    
                    if (supportOverride) {
                        promptMsg = userMessage + Bundle.MSG_AppendAskUpdate();
                    } else {
                        promptMsg = userMessage + Bundle.MSG_AppendAskUpdate2();
                    }
                    NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                            promptMsg, Bundle.TITLE_GradleProxyMismatch(),
                            NotifyDescriptor.OK_CANCEL_OPTION,  NotifyDescriptor.WARNING_MESSAGE);
                    if (supportOverride) {
                        desc.setAdditionalOptions(new Object[] { Bundle.ACTION_Continue(), Bundle.ACTION_Override() });
                    } else {
                        desc.setAdditionalOptions(new Object[] { Bundle.ACTION_Continue() });
                    }
                    desc.setValue(NotifyDescriptor.OK_OPTION);
                    
                    return DialogDisplayer.getDefault().notifyFuture(desc).thenApply(this::processUserConfirmation).exceptionally(t -> {
                        if ((t instanceof CompletionException) && (t.getCause() instanceof CancellationException)) {
                            return createResult(Status.ABORT);
                        } else {
                            return createResult(Status.UNKNOWN);
                        }
                    });
            }
            
            return null;
        }

        ProxyResult createResult(Status s) {
            boolean keep = false;
            switch (s) {
                case OVERRIDE:
                    keep = true;
                    LOG.log(Level.FINE, "Will override proxy to {0}", publicProxy);
                    break;
                case ABORT:
                    LOG.log(Level.FINE, "Will abort operation");
                    break;
                case CONTINUE:
                    keep = true;
                    LOG.log(Level.FINE, "No action will be taken");
                    break;
                case RECONFIGURED:
                    LOG.log(Level.FINE, "User properties were reconfigured to {0}", publicProxy);
                    break;
            }
            ProxyResult r = new ProxyResult(s, publicProxy, proxyAuthority, publicProxySpec, publicProxyHost, publicProxyPort);
            if (keep) {
                synchronized (this) {
                    acknowledgedResults.put(publicProxySpec, r);
                }
            }
            return r;
        }
        
        ProxyResult updateGradleConfiguration(boolean interactive) {
            EditableProperties eprops = new EditableProperties(true);
            
            File userProps = gradleFiles.getFile(GradleFiles.Kind.USER_PROPERTIES);
            
            // TODO: would be better if, when removing the proxy, the support would only comment out the keys. But EditableProperties is not suitable for that
            // now.
            if (userProps.exists()) {
                try (FileInputStream is = new FileInputStream(userProps)) {
                    eprops.load(is);
                } catch (IOException ex) {
                    NotificationDisplayer.getDefault().notify(
                            Bundle.TITLE_ConfigUpdateFailed(), 
                            NbGradleProject.getWarningIcon(),
                            Bundle.ERROR_ConfigUpdateFailed(ex.getLocalizedMessage()), null,
                            NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.ERROR);
                    return createResult(Status.UNKNOWN);
                }
            } else {
                if (publicProxyHost == null) {
                    return createResult(Status.CONTINUE);
                }
            }
            
            if (publicProxy != null) {
                eprops.put(SYSTEMPROP_HTTP_PROXYHOST, publicProxyHost);
                eprops.put(SYSTEMPROP_HTTPS_PROXYHOST, publicProxyHost);
                if (publicProxyNonDefaultPort > 0) {
                    eprops.put(SYSTEMPROP_HTTP_PROXYPORT, Integer.toString(publicProxyNonDefaultPort));
                    eprops.put(SYSTEMPROP_HTTPS_PROXYPORT, Integer.toString(publicProxyNonDefaultPort));
                } else {
                    eprops.remove(SYSTEMPROP_HTTP_PROXYPORT);
                    eprops.remove(SYSTEMPROP_HTTPS_PROXYPORT);
                }
                eprops.setComment(SYSTEMPROP_HTTP_PROXYHOST, new String[] {
                        Bundle.COMMENT_CreatedByNetBeans(DateFormat.getDateTimeInstance().format(new Date()))
                    }, true );
            } else {
                eprops.remove(SYSTEMPROP_HTTP_PROXYHOST);
                eprops.remove(SYSTEMPROP_HTTP_PROXYPORT);
                eprops.remove(SYSTEMPROP_HTTPS_PROXYHOST);
                eprops.remove(SYSTEMPROP_HTTPS_PROXYPORT);
            }
            
            if (userProps.exists()) {
                String base = userProps.getName() + FILENAME_SUFFIX_OLD;
                File f = new File(userProps.getParentFile(), base);
                int n = 1;
                while (f.exists()) {
                    f = new File(userProps.getParentFile(), base + "." + n); // NOI18N
                    n++;
                }
                userProps.renameTo(f);
            }
            try (FileOutputStream os = new FileOutputStream(userProps)) {
                eprops.store(os);
                StatusDisplayer.getDefault().setStatusText(
                        proxyHost == null ?
                                Bundle.MSG_ProxyCleared() :
                                Bundle.MSG_ProxySetTo(proxyAuthority)
                );
            } catch (IOException ex) {
                NotificationDisplayer.getDefault().notify(
                        Bundle.TITLE_ConfigUpdateFailed(), 
                        NbGradleProject.getWarningIcon(), 
                        Bundle.ERROR_ConfigUpdateFailed(ex.getLocalizedMessage()), null,
                        NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.ERROR);
                return createResult(Status.ABORT);
            }
            return createResult(Status.RECONFIGURED);
        }
        
        ProxyResult processUserConfirmation(NotifyDescriptor desc) {
            Object val = desc.getValue();
            if (val == NotifyDescriptor.CANCEL_OPTION) {
                return createResult(Status.ABORT);
            } else if (val == Bundle.ACTION_Continue()) {
                return createResult(Status.CONTINUE);
            } else if (val == Bundle.ACTION_Override()) {
                return createResult(Status.OVERRIDE);
            } else if (val == NotifyDescriptor.OK_OPTION) {
                return updateGradleConfiguration(true);
            }
            return createResult(Status.UNKNOWN);
        }
        
        private void obtainPublicProxy() {
            URI probeUri;
            try {
                probeUri = new URI(PROBE_URI_STRING);
            } catch (URISyntaxException ex) {
                // this is competely unexpected
                Exceptions.printStackTrace(ex);
                return;
            }
            List<Proxy> proxies = ProxySelector.getDefault().select(probeUri);
            LOG.log(Level.FINER, "Detected proxies for URI {0}: {1}", new Object[] { probeUri, proxies });
            for (Proxy p : proxies) {
                if (p.type() == Proxy.Type.HTTP) {
                    publicProxy = p;
                    LOG.log(Level.FINE, "Selected HTTP proxy: {0}", p);
                    break;
                } else if (p.type() == Proxy.Type.SOCKS) {
                    if (publicProxy == null) {
                        LOG.log(Level.FINE, "Found SOCKS proxy: {0}", p);
                        publicProxy = p;
                    }
                }
            }
            if (publicProxy != null) {
                SocketAddress proxyAddress = publicProxy.address();
                if (proxyAddress instanceof InetSocketAddress) {
                    InetSocketAddress iaddr = (InetSocketAddress)proxyAddress;
                    int port = iaddr.getPort();
                    int defPort = -1;

                    switch(publicProxy.type()) {
                        case HTTP:
                            defPort = PORT_DEFAULT_HTTP; 
                            break;
                        case SOCKS:
                            defPort = PORT_DEFAULT_HTTPS; 
                            break;
                    }
                    
                    if (port > 1) {
                        publicProxyPort = port;
                        if (publicProxyPort != defPort) {
                            publicProxyNonDefaultPort = port;
                        }
                    }
                    publicProxyHost = ((InetSocketAddress) proxyAddress).getHostString();
                    publicProxySpec = publicProxyHost + ((publicProxyNonDefaultPort == 0) ? "" : ":" + publicProxyNonDefaultPort);
                    LOG.log(Level.FINE, "Detected proxy: {0}", publicProxySpec);
                }
            }
        }

        private boolean extractNetworkProxy(Properties props) {
            proxyHost = props.getProperty(SYSTEMPROP_HTTP_PROXYHOST);
            String portKey;
            int defPort;
            
            if (proxyHost == null || proxyHost.isEmpty()) {
                proxyHost = props.getProperty(SYSTEMPROP_HTTPS_PROXYHOST);
                if (proxyHost == null || proxyHost.isEmpty()) {
                    proxyHost = null;
                    proxyPort = -1;
                    return false;
                } else {
                    LOG.log(Level.FINER, "Found https proxy: ", proxyHost);
                }
                portKey = SYSTEMPROP_HTTPS_PROXYPORT;
                defPort = 443;
            } else {
                LOG.log(Level.FINER, "Found http proxy: ", proxyHost);
                defPort = 80;
                portKey = SYSTEMPROP_HTTP_PROXYPORT;
            }
            
            String port = props.getProperty(portKey);
            if (port != null && !port.trim().isEmpty()) {
                proxyAuthority = proxyHost + ":" + port;
                try {
                    proxyPort = Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    // expected ?
                    proxyPort = defPort;
                    proxyAuthority = proxyHost;
                }
            } else {
                proxyPort = defPort;
                proxyAuthority = proxyHost;
            }
            return true;
        }
        
        
        private void loadProjectProxy() {
            File f = FileUtil.toFile(project.getProjectDirectory());
            if (f == null || !f.exists()) {
                LOG.log(Level.WARNING, "Project has no directory: {0}", project);
                return;
            }
            GradleFiles gf = new GradleFiles(f);
            gradleFiles = gf;
            // system properties are only read from the root project's directory, not from subprojects.
            File rootDir = gf.getRootDir();
            LOG.log(Level.FINE, "Project directory: {0}, root directory: {1}", new Object[] { f, rootDir });
            if (!rootDir.equals(f)) {
                gf = new GradleFiles(rootDir);
            }
            
            Properties props = new Properties();
            
            File userProperties = gf.getFile(GradleFiles.Kind.USER_PROPERTIES);
            File projectProperties = gf.getFile(GradleFiles.Kind.PROJECT_PROPERTIES);
            
            if (projectProperties != null && projectProperties.exists()) {
                try (FileInputStream fis = new FileInputStream(projectProperties)) {
                    LOG.log(Level.FINER, "Loading project properties from {0}", projectProperties);
                    props.load(fis);
                } catch (IOException ex) {
                    // TBD: log
                }
            }
            // override project properties with user properties; see Gradle manual for precedence.
            if (userProperties != null && userProperties.exists()) {
                try (FileInputStream fis = new FileInputStream(userProperties)) {
                    LOG.log(Level.FINER, "Loading user properties from {0}", userProperties);
                    props.load(fis);
                } catch (IOException ex) {
                    // TBD: log
                }
            }
            
            extractNetworkProxy(props);
        }
        
    }
    
}
