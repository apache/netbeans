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
package org.netbeans.modules.gradle.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The ProblemsProvider impl could be simpler, but the extraction of system proxies using PAC may be quite time-consuming,
 * so it should not be done after each query for project problems. Rather the impl monitors project reloads / info changes
 * and processes reports. Known reports are recorded at the start of the processing, so subsequent project reload with the
 * same reports will not trigger another round.
 * 
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class ProxyAlertProvider implements ProjectProblemsProvider, PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger(ProxyAlertProvider.class.getName());
    private static final RequestProcessor CHECKER_RP = new RequestProcessor(ProxyAlertProvider.class);
    
    private final Project owner;
    private final RequestProcessor.Task checkTask = CHECKER_RP.create(new ReportChecker(), true);
    private final GradlePropertiesEditor gradlePropertiesEditor;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    
    // @GuardedBy(this)
    private Collection<GradleReport> knownReports = Collections.emptySet();
    // @GuardedBy(this)
    private List<ProjectProblem> problems = null;
    
    public ProxyAlertProvider(Project owner) {
        this.owner = owner;
        NbGradleProject.addPropertyChangeListener(owner, this);
        this.gradlePropertiesEditor = new GradlePropertiesEditor(owner);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }
    
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        synchronized (this) {
            if (problems == null) {
                checkTask.schedule(0);
                this.problems = Collections.emptyList();
                return Collections.emptyList();
            } else {
                return new ArrayList<>(this.problems);
            }
        }
    }
    
    void updateProblemList(List<ProjectProblem> newProblems) {
        List<ProjectProblem> old;
        
        synchronized (this) {
            if (this.problems != null && this.problems.equals(newProblems)) {
                return;
            }
            old = this.problems;
            if (old == null) {
                old = Collections.emptyList();
            }
            this.problems = newProblems;
        }
        propSupport.firePropertyChange(PROP_PROBLEMS, old, newProblems);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
            return;
        }
        synchronized (this) {
            if (reports().equals(this.knownReports)) {
                return;
            }
        }
        checkTask.schedule(0);
    }
    
    private Set<GradleReport> reports() {
        GradleBaseProject gbp = GradleBaseProject.get(owner);
        return gbp.getProblems();
    }
    
    private static final String CLASS_UNKNOWN_HOST = "java.net.UnknownHostException"; // NOI18N
    private static final String CLASS_CONNECT_TIMEOUT = "org.apache.http.conn.ConnectTimeoutException"; // NOI18N
    
    /**
     * Checks the proxy settings asynchronously.
     */
    @NbBundle.Messages({
        "Title_GradleProxyNeeded=Proxy is missing",
        "# {0} - proxy host",
        "ProxyProblemMissing=Gradle is missing proxy settings, but it seems that {0} should be used as a proxy."
    })
    class ReportChecker implements Runnable {
        private Set<GradleReport> processReports;
        private List<String> systemProxies;
        
        @Override
        public void run() {
            systemProxies = null;
            gradlePropertiesEditor.loadGradleProperties();
            processReports = reports();
            synchronized (ProxyAlertProvider.this) {
                knownReports = processReports;
            }
            Set<String> gradleProxies = findProxyHostNames();
            AtomicReference<String> proxyName = new AtomicReference<>();

            findReport(processReports, (r) -> {
                if (CLASS_UNKNOWN_HOST.equals(r.getErrorClass()) || 
                    CLASS_CONNECT_TIMEOUT.equals(r.getErrorClass())) {
                    for (String s : gradleProxies) {
                        if (r.getMessage().contains(s + ":")) {
                            proxyName.set(s);
                            return true;
                        }
                    }
                }
                return false;
            });
            List<ProjectProblem> problems = new ArrayList<>();
            String offendingProxy = proxyName.get();
            
            // configured proxy reported as unreachable, make a report, suggest a fix
            // to remove the proxy name:
            if (offendingProxy != null) {
                maybeChangeProxyFix(offendingProxy, problems);
            }
            
            if (gradleProxies.isEmpty()) {
                GradleReport connectTimeout = findReport(processReports, (r) ->
                    CLASS_CONNECT_TIMEOUT.equals(r.getErrorClass())
                );
                List<String> proxies = findSystemProxies();
                if (connectTimeout != null && !proxies.isEmpty()) {
                    // the proxy seems to be missing, but we know there are some proxies:
                    String suggestion = proxies.get(0);
                    PropertiesEditor editor = gradlePropertiesEditor.getEditor(null, GradleFiles.Kind.USER_PROPERTIES);
                    problems.add(ProjectProblem.createError(Bundle.Title_GradleProxyNeeded(), 
                            Bundle.ProxyProblemMissing(suggestion),
                            changeOrRemoveResolver(editor, suggestion)
                    ));
                }
            }

            updateProblemList(problems);
        }
        
        List<String> findSystemProxies() {
            if (systemProxies != null) {
                return systemProxies;
            }
            return systemProxies = findSystemProxyHosts();
        }
        
        @NbBundle.Messages({
            "Title_ProxyNotNeeded=Proxy not needed for Gradle.",
            "# {0} - proxy name",
            "ProxyProblemRemoveProxy=The proxy {0} is unusable, and it seems that no proxies are needed to access the global network. The proxy setting should be removed.",
            "ProxyProblemRemoveProxy2=The configured proxy is unusable, and it seems that no proxies are needed to access the global network. The proxy setting should be removed.",
            "Title_ProxyMisconfigured=Gradle proxy misconfigured",
            "# {0} - offending proxy name",
            "# {1} - suggested proxy name",
            "ProxyProblemMisconfigured=The gradle proxy {0} is unusable, and a different proxy {1} is in use in the system. The proxy setting should be updated.",
            "# {0} - suggested proxy name",
            "ProxyProblemMisconfigured2=The configured gradle proxy is unusable, and a different proxy {0} is in use in the system. The proxy setting should be updated.",
            "ProxyProblemMisconfigured3=The configured gradle proxy is unusable. Please check project or gradle user settings."
        })
        private void maybeChangeProxyFix(String offendingProxy, Collection<ProjectProblem> problems) {

            PropertiesEditor editor = null;
            for (String s : ProxyAlertProvider.GRADLE_PROXY_PROPERTIES) {
                PropertiesEditor candidate = gradlePropertiesEditor.getEditorFor(s);
                if (candidate != null) {
                    if (editor == null) {
                        editor = candidate;
                    } else {
                        if (candidate != editor) {
                            LOG.log(Level.FINE, "Multiple property definition sources found: {0}, {1}", new Object[] {
                                candidate.getFilePath(), editor.getFilePath()
                            });
                            // too complex to handle - issue a generic warning and stop
                            problems.add(
                                    ProjectProblem.createError(Bundle.Title_ProxyMisconfigured(), Bundle.ProxyProblemMisconfigured3())
                            );
                            return;
                        }
                    }
                }
            }
            
            Set<String> hosts = new HashSet<>();
            for (String pn : SYSTEM_PROXY_PROPERTIES) {
                String v = System.getProperty(pn);
                if (v == null) {
                    continue;
                }
                String host = proxyHost(v.trim());
                if (host != null) {
                    String portNo = System.getProperty(pn.replace("Host", "Port")); // MOI18N
                    if (portNo != null) {
                        try {
                            host = host + ":" + Integer.parseInt(portNo); // MOI18N
                        } catch (NumberFormatException ex) {
                            // expected
                        }
                    }
                    hosts.add(host);
                }
            }
            if (hosts.isEmpty()) {
                List<String> systemHosts = findSystemProxies();
                if (!systemHosts.isEmpty()) {
                    hosts.add(systemHosts.iterator().next());
                }
            }
            
            if (hosts.isEmpty()) {
                // no proxies are probably required; suggest to remove the proxy
                if (editor == null) {
                    // but the proxy setting is nowhere to be found: just report.
                    problems.add(
                            ProjectProblem.createError(Bundle.Title_ProxyMisconfigured(), Bundle.ProxyProblemMisconfigured3())
                    );
                } else {
                    if (editor == null) {
                        // obtain user properties, possibly not existing
                        editor = gradlePropertiesEditor.getEditor(null, GradleFiles.Kind.USER_PROPERTIES);
                    }
                    problems.add(
                        ProjectProblem.createError(Bundle.Title_ProxyNotNeeded(), 
                                offendingProxy != null ?
                                        Bundle.ProxyProblemRemoveProxy(offendingProxy) :
                                        Bundle.ProxyProblemRemoveProxy2(), 
                                new ChangeOrRemovePropertyResolver(owner, editor, null, -1))
                    );
                }
                return;
            }
            if (hosts.size() == 1) {
                if (editor == null) {
                    // get the editor for user properties - even in the case the file does not exist at all
                    editor = gradlePropertiesEditor.getEditor(null, GradleFiles.Kind.USER_PROPERTIES);
                }
                // if there's !1 host, we can define gradle properties to that host
                // otherwise, we do not know what proxy host to use
                String suggestion = hosts.iterator().next();
                problems.add(
                    ProjectProblem.createError(Bundle.Title_ProxyMisconfigured(), 
                            offendingProxy != null ?
                                Bundle.ProxyProblemMisconfigured(offendingProxy, suggestion) : 
                                Bundle.ProxyProblemMisconfigured2(suggestion), 
                                changeOrRemoveResolver(editor, suggestion)
                    )
                );
                return;
            } else {
                // we can just offer to open the properties file so the user can use
                // his brain.
            }
        }
        
        private ProjectProblemResolver changeOrRemoveResolver(PropertiesEditor editor, String suggestion) {
            if (suggestion == null) {
                // remove
                return new ChangeOrRemovePropertyResolver(owner, editor, null, -1);
            }
            int i = suggestion.indexOf(':');
            String h;
            int port = -1;
            if (i > 0) {
                h = suggestion.substring(0, i);
                port = Integer.parseInt(suggestion.substring(i + 1));
            } else {
                h = suggestion;
            }
            return new ChangeOrRemovePropertyResolver(owner, editor, h, port);
        }
    }
    
    /**
     * Uses ProxySelector to guess a proxy for an external network. If the selector uses PAC,
     * it may take significant time to initialize & run the JS scripts, so the method should not
     * be run in EDT or some time-bound thread.
     * @return list of proxy hosts.
     */
    static List<String> findSystemProxyHosts() {
        List<String> hosts = new ArrayList<>();
        try {
            // try to detect the proxy from ProxySelector. Use well-known root DNS address
            // to increase the chance to get to an 'external' network.
            List<Proxy> proxies = ProxySelector.getDefault().select(new URI("https", "8.8.8.8", "/", null)); // MOI18N
            for (Proxy p : proxies) {
                SocketAddress ad = p.address();
                if (ad instanceof InetSocketAddress) {
                    InetSocketAddress ipv4 = (InetSocketAddress)ad;
                    if (ipv4.getPort() > 0) {
                        hosts.add(ipv4.getHostString() + ":" + ipv4.getPort()); // MOI18N
                    } else {
                        hosts.add(ipv4.getHostString());
                    }
                    // PENDING: if the failure repeats, maybe try a different proxy ?
                    break;
                }
            }
        } catch (URISyntaxException ex) {
            LOG.log(Level.WARNING, "Unexpected syntax ex", ex);
        }
        return hosts;
    }
    
    private static GradleReport findReport(Collection<GradleReport> reports, Predicate<GradleReport> predicate) {
        for (GradleReport root : reports) {
            GradleReport cause = root;
            GradleReport previous;
            do {
                previous = cause;
                if (predicate.test(previous)) {
                    return previous;
                }
                cause = previous.getCause();
            } while (cause != null && cause != previous);
        }
        return null;
    }

    private Set<String> findProxyHostNames() {
        Properties props = gradlePropertiesEditor.ensureGetProperties();
        return findProxyHostNames(props);
    }
    
    private Set<String> findProxyHostNames(Properties props) {
        Set<String> hosts = new HashSet<>();
        for (String pn : GRADLE_PROXY_PROPERTIES) {
            String v = props.getProperty(pn);
            if (v == null) {
                continue;
            }
            String host = proxyHost(v.trim());
            if (host != null) {
                hosts.add(host);
            }
        }
        return hosts;
    }
    
    private static String proxyHost(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        try {
            URI u = new URI(value);
            String h = u.getHost();
            if (h != null) {
                return h;
            }
        } catch (URISyntaxException ex) {
            // expected
        }
        int s = value.indexOf('/'); // NOI18N
        int e = value.indexOf(':'); // NOI18N
        if (e == -1) {
            e = value.length();
        }
        if (s == -1 && e == value.length()) {
            return value;
        } else {
            return value.substring(s + 1, e);
        }
    }
    
    private static final String SOCKS_PROXY_HOST = "systemProp.socks.proxyHost"; // NOI18N
    
    static final String[] GRADLE_PROXY_PROPERTIES = {
        "systemProp.http.proxyHost", // NOI18N
        "systemProp.https.proxyHost", // NOI18N
        SOCKS_PROXY_HOST
    };
    
    static final String[] SYSTEM_PROXY_PROPERTIES = {
        "http.proxyHost", // NOI18N
        "https.proxyHost", // NOI18N
        "socks.proxyHost", // NOI18N
    };
    
    static class CachedProperties extends Properties {
        private final Map<File, Long> timestamps;
        
        public CachedProperties(Map<File, Long> timestamps) {
            this.timestamps = timestamps;
        }
        
        boolean valid(Collection<File> files) {
            if (!timestamps.keySet().containsAll(files) || timestamps.size() != files.size()) {
                return false;
            }
            for (File k : files) {
                Long l = timestamps.get(k);
                if (l == null || l.longValue() != k.lastModified()) {
                    return false;
                }
            }
            return true;
        }
    }
}
