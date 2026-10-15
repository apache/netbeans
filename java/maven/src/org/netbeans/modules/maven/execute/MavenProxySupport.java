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
package org.netbeans.modules.maven.execute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.EntityReplacementMap;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.options.NetworkProxySettings;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.SaveAsCapable;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

/**
 * Checks the proxy settings and possibly reconfigures Maven settings. The class checks the effective proxy settings using {@link ProxySelector} API, then reads the current Maven
 * settings using {@link EmbedderFactory#getProjectEmbedder()}. If the maven settings do not contain proper proxy setup, maven options ({@link MavenSettings#getNetworkProxy()} is consulted:
 * <ul>
 * <li>IGNORE - no special handling
 * <li>NOTICE - a notice is printed into the notifications area informing about possible bad proxy setup, no other action taken
 * <li>UPDATE - maven configuration will be automatically updated (see below)
 * <li>OVERRIDE - persistent global configuration is not changed, but a new settings file will be generated in $nbuser/var/cache/maven and maven {@link BeanRunConfig} will be instructed to use it
 * <li>ASK - asks the user
 * </ul>
 * If maven settings are updated, the original contents is preserved as <code>settings.xml.old</code> or a numbered <code>settings.xml.old.N</code>, first unused N is selected. If the settings file contins
 * the proxy in effect, but not active, the <code>active</code> property of that proxy will be set to true, and the others to false. Otherwise a new proxy entry with the desired proxy host/port is created.
 * To disable proxies, all <code>active</code> entries are set to false.
 * <p>
 * The {@link ProxyResult#configure(org.netbeans.modules.maven.execute.BeanRunConfig)} should be run before each maven online invocation to potentially replace the global settings file with a customized one,
 * that specifies the correct proxy.
 * <p>
 * When creating customized settings.xml files, the files are named like <code>settings-[hashcode of the oroginal]-[sanitized proxy host].xml</code>. If the settings.xml file does not exist at all, "new" is used
 * instead of the hashcode. Proxy host sanitization just replaces weird characters by "_".
 * 
 * @author sdedic
 */
@ProjectServiceProvider(service = MavenProxySupport.class, projectType = NbMavenProject.TYPE)
public class MavenProxySupport {
    private static final Logger LOG = Logger.getLogger(MavenProxySupport.class.getName());
    /**
     * Sample probe URI - google's public DNS server
     */
    private static final String PROBE_URI_STRING = "http://search.maven.org"; // NOI18N
    
    private static final String FILENAME_SUFFIX_OLD = ".old"; // NOI18N

    private static final String ICON_MAVEN_PROJECT = "org/netbeans/modules/maven/resources/Maven2Icon.gif"; // NOi18N
        

    /**
     * Tag name in settings file.
     */
    private static final String TAG_SETTINGS = "settings"; // NOI18N

    /**
     * Tag name in settings file.
     */
    private static final String TAG_PROXIES = "proxies"; // NOI18N

    /**
     * Tag name in settings file.
     */
    private static final String TAG_NAME_ACTIVE = "active"; // NOI18N

    /**
     * Complete tag start/end. End tag should contain a newline.
     */
    private static final String TAG_ACTIVE_START = "<active>"; // NOI18N
    private static final String TAG_ACTIVE_END = "</active>\n"; // NOI18N

    /**
     * Tag name in settings file.
     */
    private static final String TAG_PROXY = "proxy"; // NOI18N

    /**
     * Suffix to add to customized settings filename when no proxy is configured (all disabled)
     */
    private static final String SUFFIX_NONE_PROXY = "-none"; // NOI18N
    
    /**
     * Suffix for custom settings filename if global settings is missing.
     */
    private static final String SUFFIX_NEW_PROXY = "-new";
    
    /**
     * Extension for the settings file
     */
    private static final String FILENAME_SETTINGS_EXT = ".xml"; // NOI18N
    
    /**
     * Global settings file name
     */
    private static final String FILENAME_BASE_SETTINGS = "settings"; // NOI18N
    private static final String FILENAME_SETTINGS = FILENAME_BASE_SETTINGS + FILENAME_SETTINGS_EXT;

    
    private static final int PORT_DEFAULT_HTTPS = 1080;
    private static final int PORT_DEFAULT_HTTP = 80;
    
    /**
     * Timeout for the network probe. The probe is done in case project settings mismatch with the autodetected ones.
     * If set to 0 or negative number, the project proxy configuration will not be probed.
     */
    private static final int PROXY_PROBE_TIMEOUT = Integer.getInteger("netbeans.networkProxy.timeout", 1000);

    /**
     * Past decisions made by the user during this session. The Map is used so the user si not bothered that often with questions.
     * If the user chooses 'override' or 'continue' (no action), the Map receives the public proxy spec and the result. If the same
     * effective proxy is detected, the user is not asked again.
     */
    // @GuardedBy(this)
    private static Map<String, ProxyResult>    acknowledgedResults = new HashMap<>();
    
    public MavenProxySupport(Project project) {
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
        private final Proxy proxy;
        private final String toolProxy;
        private final String proxyHost;
        private final String proxySpec;
        private final int proxyPort;
        private final Settings mavenSettings;
        private final boolean nonDefaultPort;
        private volatile Status status;
        TextInfo textInfo;
        FileObject settingsDir;
        String settingsFileName = FILENAME_SETTINGS;
        IOException exception;
        
        public ProxyResult(Status status, Proxy proxy) {
            this.status = status;
            this.proxy = proxy;
            this.toolProxy = null;
            this.proxySpec = null;
            this.proxyHost = null;
            this.proxyPort = -1;
            this.mavenSettings = null;
            this.nonDefaultPort = false;
        }
        
        public ProxyResult(Status status, Proxy proxy, String toolProxy, String proxySpec, String proxyHost, int proxyPort, boolean nonDefault, Settings mavenSettings) {
            this.status = status;
            this.proxy = proxy;
            this.toolProxy = toolProxy;
            this.proxySpec = proxySpec;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.mavenSettings = mavenSettings;
            this.nonDefaultPort = nonDefault;
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

        public IOException getException() {
            return exception;
        }

        @NbBundle.Messages({
            "# {0} - settings directory name",
            "ERR_CannotCreateSettingsDir=Could not create settings directory: {0}",
            "# {0} - settings file name",
            "ERR_CannoLocateSettings=Could not locate and open settings file: {0}",
            "# {0} - proxy id",
            "# {1} - proxy host",
            "ERR_CannotDisableProxy=Could not disable proxy id {0}, host {1}",
            "# {0} - proxy id",
            "# {1} - proxy host",
            "ERR_CannotEnableProxy=Could not enable proxy id {0}, host {1}",
            "# {0} - proxy host",
            "ERR_CannotInsertProxy=Could not add proxy host {0}"
        })
        private FileObject generateNewSettingsFile(Path settingsPath) throws IOException {
            FileObject settingsDir = this.settingsDir != null ? this.settingsDir : FileUtil.toFileObject(settingsPath.getParent().toFile());
            if (settingsDir == null) {
                Path dir = Files.createDirectory(settingsPath.getParent());
                settingsDir = FileUtil.toFileObject(dir.toFile());
                if (settingsDir == null) {
                    throw new IOException(Bundle.ERROR_ConfigUpdateFailed(settingsPath.getParent()));
                }
            }
            Map<String, Object> params = new HashMap<>();
            params.put("proxyHost", proxyHost);
            params.put("proxyPort", proxyPort);
            return FileBuilder.createFromTemplate(
                    FileUtil.getConfigFile("Templates/Project/Maven2/settings.xml"), settingsDir, settingsFileName, 
                    params, FileBuilder.Mode.FAIL);
        }
        
        private File getMavenSettings() {
            return new File(new File(System.getProperty("user.home"), ".m2"), FILENAME_SETTINGS); // NOI18N
        }
        
        public BeanRunConfig configure(BeanRunConfig config) throws IOException {
            if (status != Status.OVERRIDE) {
                return config;
            }
            // compute digest
            String uniqueString = "";
            try {
                Path p = getMavenSettings().toPath();
                if (!Files.exists(p)) {
                    uniqueString = SUFFIX_NEW_PROXY; // NOI18N
                } else {
                    MessageDigest dg = MessageDigest.getInstance("SHA1");
                    byte[] bytes = dg.digest(Files.readAllBytes(p));
                    StringBuilder sb = new StringBuilder("-");
                    for (int i = 0; i < bytes.length; i++) {
                        sb.append(Integer.toHexString(bytes[i] & 0xff));
                    }
                    uniqueString = sb.toString();
                }
            } catch (NoSuchAlgorithmException | IOException ex) {
                return config;
            }
            settingsDir = FileUtil.toFileObject(Places.getCacheSubdirectory("maven"));
            if (settingsDir == null) {
                return config;
            }
            if (proxyHost == null) {
                uniqueString += SUFFIX_NONE_PROXY;
            } else {
                uniqueString += "-" + proxySpec.replace(":", "_");
            }
            settingsFileName = FILENAME_BASE_SETTINGS + uniqueString + FILENAME_SETTINGS_EXT;
            FileObject alreadyDone = settingsDir.getFileObject(settingsFileName);
            if (alreadyDone != null) {
                config.setInternalProperty("NbIde.configOverride", alreadyDone.getPath());
                return config;
            }
            
            updateMavenProxy();
            alreadyDone = settingsDir.getFileObject(settingsFileName);
            config.setInternalProperty("NbIde.configOverride", alreadyDone.getPath());
            return config;
        }
        
        private void loadMavenTextInfo() throws IOException, IllegalArgumentException {
            Path settingsPath = getMavenSettings().toPath();
            if (!Files.isReadable(settingsPath) && Files.isRegularFile(settingsPath)) {
                generateNewSettingsFile(settingsPath);
                return;
            }
            XppDelegate del = new XppDelegate(EntityReplacementMap.defaultEntityReplacementMap);
            
            try (FileInputStream in = new FileInputStream(settingsPath.toFile())) {
                del.setInput( ReaderFactory.newXmlReader( in ));
                while (del.next() != XmlPullParser.END_DOCUMENT) {
                    // empty, just read
                }
                textInfo = del.textInfo;
            } catch (XmlPullParserException ex) {
                throw new IOException(ex);
            }
        }
        
        private AtomicLockDocument adoc;
        private LineDocument settingsLineDoc;
        private EditorCookie settingsEditor;
        private Lookup fileLookup;
        
        private void loadSettingsContents() throws IOException {
            if (settingsEditor != null) {
                return;
            }
            Path settingsPath = getMavenSettings().toPath();
            FileObject fo = FileUtil.toFileObject(settingsPath.toFile());
            if (fo == null) {
                return;
            }
            fileLookup = fo.getLookup();
            EditorCookie cake = fileLookup.lookup(EditorCookie.class);
            if (cake == null) {
                throw new IOException(Bundle.ERR_CannoLocateSettings(settingsPath));
            }
            StyledDocument doc = cake.openDocument();
            settingsLineDoc = LineDocumentUtils.as(doc, LineDocument.class);
            adoc = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
            if (settingsLineDoc == null) {
                throw new IOException(Bundle.ERR_CannoLocateSettings(settingsPath));
            }
            settingsEditor = cake;
            
            loadMavenTextInfo();
        }
        
        private String padding(int num) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < num; i++) {
                sb.append(' ');
            }
            return sb.toString();
        }
        
        /**
         * Insert a new proxy:
         * - if at least one 'proxy' block exists, insert the new definition before all the other ones
         * - if "proxies" element exists, but no proxy definition, insert right after "proxies"
         * - if no proxies element exists insert at the position of the first element including the 'proxies' clause.
         */
        private void insertNewProxy() throws IOException {
            int startColumn = 0;
            LineAndColumn insertAt;
            
            StringBuilder textBuilder = new StringBuilder();
            if (textInfo.proxyTags.isEmpty()) {
                if (textInfo.proxiesEndTag != null) {
                    // TODO: obey IDE formatting for xml files ?
                    insertAt = textInfo.proxiesEndTag;
                    startColumn = insertAt.column - 1 + 4;
                    // add 4 spaces, indent between <proxies> and nested <proxy>, we're using padding of the </proxies> here.
                    textBuilder.append("    "); // NOI18N
                } else {
                    insertAt = textInfo.firstTag;
                    startColumn = insertAt.column - 1;
                }
            } else {
                insertAt = textInfo.proxyTags.get(0).tags.get(TAG_PROXY).startTag;
                startColumn = insertAt.column - 1;
            }
            
            // inserting at the position of first "proxy" element: no padding in front, add padding at the end.
            // inserting at the position of </proxies> closing tag: 4-space padding at the start, 
            try (
                InputStream inputStream = getClass().getResourceAsStream(textInfo.proxiesEndTag != null ? "proxy.template.xml" : "proxies.template.xml");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                String s;
                boolean first = true;
                
                while ((s = reader.readLine()) != null) {
                    if (!first) {
                        textBuilder.append(padding(startColumn));
                    }
                    first = false;
                    textBuilder.append(s).append("\n");
                }
            }
            // add necessary padding
            if (!textInfo.proxyTags.isEmpty()) {
                textBuilder.append(padding(startColumn));
            } else if (textInfo.proxiesEndTag != null) {
                textBuilder.append(padding(textInfo.proxiesEndTag.column - 1));
            } else {
                textBuilder.append(padding(startColumn));
            }
            Map<String, String> args = new HashMap<>();
            args.put(FMT_PROXY_HOST, proxyHost);
            args.put(FMT_PROXY_PORT, Integer.toString(proxyPort));
            
            MapFormat fmt = new MapFormat(args);
            fmt.setLeftBrace("${");
            String contents = fmt.format(textBuilder.toString());
            
            IOException[] err = new IOException[1];
            adoc.runAtomic(() -> {
                try {
                    int lineOffset = LineDocumentUtils.getLineStartFromIndex(settingsLineDoc, insertAt.line - 1);
                    int startOffset = lineOffset + insertAt.column - 1;

                    settingsLineDoc.insertString(startOffset, contents, null);
                } catch (BadLocationException ex) {
                    err[0] =  new IOException(Bundle.ERR_CannotInsertProxy(proxyHost));
                }
            });

            if (err[0] != null) {
                throw err[0];
            }
        }
        private static final String FMT_PROXY_PORT = "proxyPort"; // NOI18N
        private static final String FMT_PROXY_HOST = "proxyHost"; // NOI18N
        
        private void enableProxy(org.apache.maven.settings.Proxy p, int proxyIndex, boolean enable) throws IOException {
            String failureMsg = enable ? Bundle.ERR_CannotEnableProxy(p.getId(), p.getHost()) : Bundle.ERR_CannotDisableProxy(p.getId(), p.getHost());
            if (textInfo.proxyTags.size() < proxyIndex) {
                throw new IOException(failureMsg);
            }
            ProxyInfo pi = textInfo.proxyTags.get(proxyIndex);
            TagInfo activeTag = pi.tags.get(TAG_NAME_ACTIVE);
            IOException[] err = new IOException[1];
            
            if (activeTag != null) {
                adoc.runAtomic(() -> {
                    try {
                        int lineOffset = LineDocumentUtils.getLineStartFromIndex(settingsLineDoc, activeTag.startTag.line - 1);
                        int startOffset = lineOffset + activeTag.startTag.column - 1;
                        
                        int endLineOffset = LineDocumentUtils.getLineStartFromIndex(settingsLineDoc, activeTag.endTag.line - 1);
                        int endOffset = endLineOffset + activeTag.endTag.column - 1;
                        
                        String content = settingsLineDoc.getText(startOffset, endOffset - startOffset);
                        int endStartTagPos = content.indexOf(">");
                        if (endStartTagPos == -1 || endStartTagPos >= (endOffset - startOffset)) {
                            // cannot find end of start tag
                            err[0] =  new IOException(Bundle.ERR_CannotDisableProxy(p.getId(), p.getHost()));
                            return;
                        }
                        int from = startOffset + endStartTagPos + 1;
                        settingsLineDoc.remove(from, endOffset - from);
                        settingsLineDoc.insertString(from, enable ? "true" : "false", null); // NOI18N
                    } catch (BadLocationException ex) {
                        err[0] =  new IOException(failureMsg);
                    }
                });
            } else {
                if (pi.firstTag == null) {
                    throw new IOException(Bundle.ERR_CannotDisableProxy(p.getId(), p.getHost()));
                }
                String toInsert = padding(pi.firstTag.column) + TAG_ACTIVE_START + enable + TAG_ACTIVE_END; // NOI18N
                adoc.runAtomic(() -> {
                    try {
                        int lineOffset = LineDocumentUtils.getLineStartFromIndex(settingsLineDoc, pi.firstTag.line - 1);
                        settingsLineDoc.insertString(lineOffset, toInsert, null);
                    } catch (BadLocationException ex) {
                        err[0] =  new IOException(failureMsg);
                    }
                });
            }
            
            if (err[0] != null) {
                throw err[0];
            }
        }

        private void updateMavenProxy() throws IOException {
            loadSettingsContents();
            
            if (textInfo == null) {
                if (settingsDir != null) {
                    generateNewSettingsFile(FileUtil.toFile(settingsDir).toPath().resolve(settingsFileName));
                } else {
                    generateNewSettingsFile(getMavenSettings().toPath());
                }
                return;
            }
            
            // case 1: proxy should not be set, let's deactivate all the proxies
            int pos = 0;
            if (proxyHost == null) {
                for (org.apache.maven.settings.Proxy p : mavenSettings.getProxies()) {
                    if (p.isActive()) {
                        enableProxy(p, pos, false);
                    }
                    pos++;
                }
            } else {

                // case 2: there MAY be a matching proxy already defined, but not active.
                org.apache.maven.settings.Proxy existingProxy = null;

                for (org.apache.maven.settings.Proxy p : mavenSettings.getProxies()) {
                    if (proxyHost.equals(p.getHost())) {
                        if (!nonDefaultPort || proxyPort == p.getPort()) {
                            existingProxy = p;
                            break;
                        }
                    }
                }

                for (org.apache.maven.settings.Proxy p : mavenSettings.getProxies()) {
                    if (p != existingProxy) {
                        enableProxy(p, pos, false);
                    }
                    pos++;
                }
                if (existingProxy != null) {
                    if (!existingProxy.isActive()) {
                        int proxyIndex = mavenSettings.getProxies().indexOf(existingProxy);
                        if (proxyIndex == -1) {
                            throw new IOException(Bundle.ERR_CannotEnableProxy(existingProxy.getId(), existingProxy.getHost()));
                        }
                        enableProxy(existingProxy, proxyIndex, true);
                    }
                } else {
                    insertNewProxy();
                }
            }
            
            if (settingsEditor.isModified()) {
                if (settingsDir != null) {
                    SaveAsCapable saa = fileLookup.lookup(SaveAsCapable.class);
                    if (saa != null) {
                        saa.saveAs(settingsDir, settingsFileName);
                    } else {
                        // FIXME: log a warning
                    }
                } else {
                    File settings = getMavenSettings();
                    String base = settings + FILENAME_SUFFIX_OLD;
                    File f = new File(settings.getParentFile(), base);
                    int n = 1;
                    while (f.exists()) {
                        f = new File(settings.getParentFile(), base + "." + n); // NOI18N
                        n++;
                    }
                    settings.renameTo(f);
                    settingsEditor.saveDocument();
                }
            }
        }
    }

    @NbBundle.Messages({
        "TITLE_MavenProxyMismatch=Possible Network Proxy Issue",
        "# {0} - maven proxy",
        "MSG_ProxyMisconfiguredDirect=Maven is configured for a proxy {0}, but the system does not require a proxy for network connections. Proxy settings should be removed from maven settings.xml.",
        "# {0} - system proxy",
        "MSG_ProxyMisconfiguredMissing=Maven is not configured to use a network proxy, but the proxy {0} seems to be required for network communication. Maven settings.xml should be updated to specify a proxy.",
        "# {0} - system proxy",
        "# {1} - maven proxy",
        "MSG_ProxyMisconfiguredOther=Maven is configured to use a network proxy {1}, but the proxy {0} seems to be required for network communication. Proxy settings should be updated in maven settings.xml.",
        "MSG_AppendAskUpdate=\nUpdate Maven configuration ? Choose \"Override\" to apply detected proxy only to IDE operations.",
        "MSG_AppendAskUpdate2=\nUpdate Maven configuration ?",
        "ACTION_Override=Override",
        "ACTION_Continue=Keep settings",
        "# {0} - date/time of the update",
        "COMMENT_CreatedByNetBeans=# This proxy configuration has been updated by Apache NetBeans on {0}",
        "TITLE_ConfigUpdateFailed=Configuration update failed",
        "# {0} - error message",
        "ERROR_ConfigUpdateFailed=Failed to modify Maven user properties: {0}",
        "# {0} - proxy specification",
        "MSG_ProxySetTo=Maven Network proxy set to: {0}",
        "MSG_ProxyCleared=Maven Network proxy removed",
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
        
        Settings mavenSettings;
        
        public CompletableFuture<ProxyResult> checkProxy() {
            boolean supportOverride = NetworkProxySettings.allowProxyOverride();
            NetworkProxySettings action = MavenSettings.getDefault().getNetworkProxy();
            if (action == NetworkProxySettings.IGNORE) {
                return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
            }
            
            obtainPublicProxy();
            loadProjectProxy();
            
            boolean direct = publicProxy == null || publicProxy.type() == Proxy.Type.DIRECT;
            
            if (direct && proxyAuthority == null) {
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
                        return CompletableFuture.completedFuture(new ProxyResult(Status.CONTINUE, probeProxy, proxyAuthority, publicProxySpec, publicProxyHost, publicProxyPort, publicProxyNonDefaultPort > 0, mavenSettings));
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
                    NotificationDisplayer.getDefault().notify(Bundle.TITLE_MavenProxyMismatch(),
                            ImageUtilities.loadImageIcon(ICON_MAVEN_PROJECT, false),
                            userMessage, null, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.WARNING);
                    return CompletableFuture.completedFuture(createResult(Status.CONTINUE));
                
                case OVERRIDE:
                    return CompletableFuture.completedFuture(createResult(Status.OVERRIDE));
                    
                case UPDATE:
                    try {
                        result = createResult(Status.RECONFIGURED);
                        result.updateMavenProxy();
                    } catch (IOException ex) {
                        result = createResult(ex);
                    }
                    return CompletableFuture.completedFuture(result);
                    
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
                            promptMsg, Bundle.TITLE_MavenProxyMismatch(),
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

        ProxyResult createResult(IOException ex) {
            ProxyResult r = createResult(Status.ABORT);
            r.exception = ex;
            LOG.log(Level.WARNING, "Failed to configure proxy", ex);
            return r;
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
            ProxyResult r = new ProxyResult(s, publicProxy, proxyAuthority, publicProxySpec, publicProxyHost, publicProxyPort, publicProxyNonDefaultPort > 0, mavenSettings);
            if (keep) {
                synchronized (this) {
                    acknowledgedResults.put(publicProxySpec, r);
                }
            }
            return r;
        }
        
        @NbBundle.Messages({
            "TITLE_ProxyUpdateFailed=Update of proxy configuration failed"
        })
        ProxyResult processUserConfirmation(NotifyDescriptor desc) {
            Object val = desc.getValue();
            if (val == NotifyDescriptor.CANCEL_OPTION) {
                return createResult(Status.ABORT);
            } else if (val == Bundle.ACTION_Continue()) {
                return createResult(Status.CONTINUE);
            } else if (val == Bundle.ACTION_Override()) {
                return createResult(Status.OVERRIDE);
            } else if (val == NotifyDescriptor.OK_OPTION) {
                try {
                    ProxyResult result = createResult(Status.RECONFIGURED);
                    result.updateMavenProxy();
                } catch (IOException ex) {
                    return createResult(ex);
                }
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

        private void loadProjectProxy() {
            mavenSettings = EmbedderFactory.getProjectEmbedder().getSettings();
            org.apache.maven.settings.Proxy activeProxy = mavenSettings.getActiveProxy();
            if (activeProxy != null) {
                proxyHost = activeProxy.getHost();
                proxyPort = activeProxy.getPort();
                
                if (proxyPort > 0) {
                    proxyAuthority = proxyHost + ":" + proxyPort;
                }
            } else {
                proxyAuthority = null;
                proxyHost = null;
                proxyPort = -1;
            }
        }
        
    }
    
    static class LineAndColumn {
        int line;
        int column;

        public LineAndColumn(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }
    
    static class TagInfo {
        String tagName;
        LineAndColumn  startTag;
        String  content;
        LineAndColumn  endTag;

        public TagInfo(String tagName, LineAndColumn start) {
            this.tagName = tagName;
            this.startTag = start;
        }
    }
    
    static class ProxyInfo {
        LineAndColumn firstTag;
        Map<String, TagInfo> tags = new HashMap<>();
    }
    
    static class TextInfo {
        LineAndColumn firstTag;
        LineAndColumn firstProxyTag;
        LineAndColumn proxiesEndTag;
        List<ProxyInfo> proxyTags = new ArrayList<>();
    }

    static class XppDelegate extends MXParser {
        private static final int UNKNOWN = 0;
        private static final int PROXIES = 1;
        private static final int PROXY = 2;
        private static final int INSIDE_PROXY = 3;
        
        private TextInfo textInfo = new TextInfo();
        private LinkedList<TagInfo> tagStack = new LinkedList<>();
        private ProxyInfo current;
        private int state = UNKNOWN;

        private final Field posStartField;
        private final Field posEndField;

        public XppDelegate(EntityReplacementMap entityReplacementMap) {
            super(entityReplacementMap);
            
            try {
                posEndField = MXParser.class.getDeclaredField("posEnd");
                posEndField.setAccessible(true);

                posStartField = MXParser.class.getDeclaredField("posStart");
                posStartField.setAccessible(true);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("code changed", ex);
            }
        }
        
        TextInfo getTextInfo() {
            return textInfo;
        }
        
        @Override
        public int nextTag() throws XmlPullParserException, IOException {
            int t = super.nextTag(); 
            return processToken(t);
        }

        @Override
        public int next() throws XmlPullParserException, IOException {
            int t = super.next();
            return processToken(t);
        }
        
        private LineAndColumn startPos() {
            int ln = getLineNumber();
            int col = getColumnNumber();
            try {
                col -= (posEndField.getInt(this) - posStartField.getInt(this));
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new LineAndColumn(ln, col);
        }

        private LineAndColumn pos() {
            return new LineAndColumn(getLineNumber(), getColumnNumber());
        }
        
        private StringBuilder tagText = new StringBuilder();
        
        private int processToken(int token) {
            switch (token) {
                case XmlPullParser.TEXT:
                    tagText.append(getText());
                    break;
                case XmlPullParser.END_TAG:
                    String en = getName();
                    if (state >= PROXY) {
                        if (TAG_PROXY.equals(en)) {
                            state = PROXIES;
                            current = null;
                            tagStack.clear();
                            break;
                        }
                        if (!tagStack.isEmpty()) {
                            TagInfo ti = tagStack.removeLast();
                            ti.content = tagText.toString().trim();
                            ti.endTag = startPos();
                        }
                    } else if (state == PROXIES && TAG_PROXIES.equals(en)) {
                        state = UNKNOWN;
                        textInfo.proxiesEndTag = startPos();
                        break;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    String n = getName();
                    tagText = new StringBuilder();
                    if (state == UNKNOWN) {
                        if (TAG_PROXIES.equals(n)) {
                            state = 1;
                        } else if (!TAG_SETTINGS.equals(n) && textInfo.firstTag == null) {
                            textInfo.firstTag = startPos();
                        }
                        break;
                    }
                    if (state == PROXIES && TAG_PROXY.equals(n)) {
                        textInfo.firstProxyTag = pos();
                        state = INSIDE_PROXY;
                        current = new ProxyInfo();
                        textInfo.proxyTags.add(current);
                        // fall through, so proxy is recorded
                    }
                    if (state >= PROXY) {
                        TagInfo ti = new TagInfo(n, startPos());
                        if (current != null) {
                            if (current.firstTag == null) {
                                current.firstTag = ti.startTag;
                            }
                            current.tags.putIfAbsent(ti.tagName, ti);
                        }
                        tagStack.add(ti);
                        break;
                    }
            }
            return token;
        }
    }
}
