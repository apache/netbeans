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
package org.netbeans.modules.javascript.cdtdebug;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceRequest;
import org.netbeans.modules.javascript2.debug.sources.SourceContent;
import org.netbeans.modules.javascript2.debug.sources.SourceFilesCache;
import org.netbeans.modules.web.common.sourcemap.SourceMapsScanner;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class ScriptsHandler {

    private static final Logger LOG = Logger.getLogger(ScriptsHandler.class.getName());
    private static final boolean USE_SOURCE_MAPS
            = Boolean.parseBoolean(System.getProperty("javascript.debugger.useSourceMaps", "true"));

    private final Map<String, CDTScript> scriptsById = new HashMap<>();
    private final Map<URL, CDTScript> scriptsByURL = new HashMap<>();
    private final SourceMapsTranslator smt;
    private final String remotePathPrefix;
    private final boolean doPathTranslation;
    private final String[] localPathPrefixes;
    private final String[] serverPathPrefixes;
    @NullAllowed
    private final FileObject[] localRoots;
    @NullAllowed
    private final FileObject[] localPathExclusionFilters;
    private final CDTDebugger dbg;

    ScriptsHandler(@NullAllowed List<String> localPaths,
            @NullAllowed List<String> serverPaths,
            Collection<String> localPathExclusionFilters,
            @NullAllowed CDTDebugger dbg) {
        if (dbg != null) {
            this.remotePathPrefix = dbg.getWebSocketDebuggerUrl().getHost() + "_" + dbg.getWebSocketDebuggerUrl().getPort() + "/";
        } else {
            // dbg can be null in tests
            this.remotePathPrefix = "";
        }
        if (localPaths != null && !localPaths.isEmpty()
                && serverPaths != null && !serverPaths.isEmpty()
                && !localPaths.equals(serverPaths)) {
            this.doPathTranslation = true;
            int n = localPaths.size();
            this.localPathPrefixes = new String[n];
            this.serverPathPrefixes = new String[n];
            for (int i = 0; i < n; i++) {
                this.localPathPrefixes[i] = toUrl(localPaths.get(i));
            }
            for (int i = 0; i < n; i++) {
                this.serverPathPrefixes[i] = toUrl(serverPaths.get(i));
            }
        } else {
            this.doPathTranslation = false;
            this.localPathPrefixes = null;
            this.serverPathPrefixes = null;
        }
        if (localPaths != null && !localPaths.isEmpty()) {
            FileObject[] lroots = new FileObject[localPaths.size()];
            int i = 0;
            for (String localPath : localPaths) {
                FileObject localRoot = FileUtil.toFileObject(FileUtil.normalizeFile(new File(localPath)));
                if (localRoot != null) {
                    lroots[i++] = localRoot;
                }
            }
            if (i < localPaths.size()) {
                lroots = Arrays.copyOf(lroots, i);
            }
            this.localRoots = lroots;
            if (USE_SOURCE_MAPS) {
                this.smt = SourceMapsScanner.getInstance().scan(this.localRoots);
            } else {
                this.smt = null;
            }
        } else {
            this.localRoots = null;
            if (USE_SOURCE_MAPS) {
                this.smt = SourceMapsTranslator.create();
            } else {
                this.smt = null;
            }
        }
        if (!localPathExclusionFilters.isEmpty()) {
            FileObject[] lpefs = new FileObject[localPathExclusionFilters.size()];
            int i = 0;
            for (String lpef : localPathExclusionFilters) {
                FileObject localRoot = FileUtil.toFileObject(new File(lpef));
                if (localRoot != null) {
                    lpefs[i++] = localRoot;
                } else {
                    lpefs = Arrays.copyOf(lpefs, lpefs.length - 1);
                }
            }
            this.localPathExclusionFilters = (lpefs.length > 0) ? lpefs : null;
        } else {
            this.localPathExclusionFilters = null;
        }
        LOG.log(Level.FINE,
                "ScriptsHandler: doPathTranslation = {0}, localPathPrefixes = {1},"
                + "serverPathPrefixes = {2},"
                + "localRoots = {3}, localPathExclusionFilters = {4}.",
                new Object[]{doPathTranslation, Arrays.toString(localPathPrefixes),
                    Arrays.toString(serverPathPrefixes),
                    Arrays.toString(this.localRoots),
                    Arrays.toString(this.localPathExclusionFilters)});
        this.dbg = dbg;
    }


    void add(CDTScript script) {
        synchronized (scriptsById) {
            scriptsById.put(script.getScriptId(), script);
        }
    }

    @CheckForNull
    public SourceMapsTranslator getSourceMapsTranslator() {
        return smt;
    }

    @CheckForNull
    public CDTScript getScript(String id) {
        synchronized (scriptsById) {
            return scriptsById.get(id);
        }
    }

    @NonNull
    public Collection<CDTScript> getScripts() {
        synchronized (scriptsById) {
            return new ArrayList<>(scriptsById.values());
        }
    }

    public boolean containsLocalFile(FileObject fo) {
        if (fo == null) {
            return false;
        }
        if (SourceFilesCache.URL_PROTOCOL.equals(fo.toURL().getProtocol())) {
            // virtual file created from source content
            return true;
        }
        if (localPathExclusionFilters != null) {
            for (FileObject lpef : localPathExclusionFilters) {
                if (FileUtil.isParentOf(lpef, fo)) {
                    return false;
                }
            }
        }
        if (localRoots == null) {
            return true;
        }
        for (FileObject localRoot : localRoots) {
            if (FileUtil.isParentOf(localRoot, fo)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsRemoteFile(URL url) {
        if (!SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
            return false;
        }
        String path;
        try {
            path = url.toURI().getPath();
        } catch (URISyntaxException usex) {
            return false;
        }

        int l = path.length();
        int index = 0;
        while (index < l && path.charAt(index) == '/') {
            index++;
        }
        int begin = path.indexOf('/', index);
        if (begin > 0) {
            // path.substring(begin + 1).startsWith(remotePathPrefix)
            return path.regionMatches(begin + 1, remotePathPrefix, 0, remotePathPrefix.length());
        } else {
            return false;
        }
    }

    @CheckForNull
    public FileObject getFile(String scriptId) {
        CDTScript script = getScript(scriptId);
        if (script == null) {
            return null;
        } else {
            return getFile(script);
        }
    }

    public FileObject getFile(@NonNull CDTScript script) {
        String url = script.getUrl();
        if(url == null) {
            return null;
        }

        String lp = getLocalPath(url);
        if (lp != null) {
            try {
                FileObject localFile = URLMapper.findFileObject(new URI(lp).toURL());
                if (localFile != null) {
                    synchronized (scriptsByURL) {
                        scriptsByURL.put(localFile.toURL(), script);
                    }
                    return localFile;
                }
            } catch (URISyntaxException | MalformedURLException ex) {
                // Ignore
            }
        }
        // prepend <host>_<port>/ to the name.
        String name = remotePathPrefix + url;
        URL sourceURL = SourceFilesCache.getDefault().getSourceFile(name, script.getHash(), new ScriptContentLoader(script, dbg));
        synchronized (scriptsByURL) {
            scriptsByURL.put(sourceURL, script);
        }
        return URLMapper.findFileObject(sourceURL);
    }

    @CheckForNull
    public String getServerPath(@NonNull FileObject fo) {
        if(! doPathTranslation) {
            return toTripleSlashUri(fo.toURI()).toString();
        }
        String serverPath;
        URL url = fo.toURL();
        if (SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
            String path = fo.getPath();
            int begin = path.indexOf('/');
            if (begin > 0) {
                path = path.substring(begin + 1);
                // subtract <host>_<port>/ :
                if (path.startsWith(remotePathPrefix)) {
                    serverPath = path.substring(remotePathPrefix.length());
                } else {
                    serverPath = null;
                }
            } else {
                serverPath = null;
            }
        } else {
            String fileUri = toTripleSlashUri(fo.toURI()).toString();
            for (int i = 0; i < localPathPrefixes.length; i++) {
                if (fileUri.startsWith(localPathPrefixes[i])) {
                    return serverPathPrefixes[i] + fileUri.substring(localPathPrefixes[i].length());
                }
            }
        }
        return null;
    }


    public String getLocalPath(@NonNull String serverPath) {
        if (!doPathTranslation) {
            return serverPath;
        } else {
            for (int i = 0; i < serverPathPrefixes.length; i++) {
                if (serverPath.startsWith(serverPathPrefixes[i])) {
                    return localPathPrefixes[i] + serverPath.substring(serverPathPrefixes[i].length());
                }
            }
        }
        return null;
    }

    public File[] getLocalRoots() {
        if (localRoots == null) {
            return new File[]{};
        }
        int l = localRoots.length;
        File[] roots = new File[l];
        for (int i = 0; i < l; i++) {
            roots[i] = FileUtil.toFile(localRoots[i]);
        }
        return roots;
    }

    private static String toUrl(String path) {
        // Assume Windows path if not starts with slash
        if (! path.startsWith("/")) {
                path = "/" + path;
        }
        try {
            URI fileUri = new URI("file", "", path.replace("\\", "/"), null);
            String fileString = fileUri.toString();
            if(! fileString.endsWith("/")) {
                return fileString + "/";
            } else {
                return fileString;
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }


    private URI toTripleSlashUri(URI inputUri) {
        // Only handle file URIs and file uris with a null host (single slash variant)
        if((! "file".equals(inputUri.getScheme())) || inputUri.getHost() != null) {
            return inputUri;
        } else {
            try {
                return new URI(
                        inputUri.getScheme(),
                        "",
                        inputUri.getRawPath(),
                        null
                );
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
    }

    private static final class ScriptContentLoader implements SourceContent {

        private final CDTScript script;
        private final CDTDebugger dbg;
        private String content;
        private final Object contentLock = new Object();
        private String contentLoadError;

        public ScriptContentLoader(CDTScript script, CDTDebugger dbg) {
            this.script = script;
            this.dbg = dbg;
        }

        @NbBundle.Messages({ "ERR_Interrupted=Interrupted",
                             "ERR_ScriptFailedToLoad=The script failed to load.",
                             "ERR_ScriptHasNoSource=The script has no source."})
        @Override
        public String getContent() throws IOException {
            if (content != null) {
                return content;
            }

            GetScriptSourceRequest gssr = new GetScriptSourceRequest();
            gssr.setScriptId(script.getScriptId());
            dbg.getConnection().getDebugger().getScriptSource(gssr)
                    .handle((res, thr) -> {
                        synchronized (contentLock) {
                            if (thr != null) {
                                contentLoadError = Bundle.ERR_ScriptFailedToLoad();
                            } else {
                                if (res == null || res.getScriptSource() == null) {
                                    contentLoadError = Bundle.ERR_ScriptHasNoSource();
                                } else {
                                    content = res.getScriptSource();
                                }
                            }
                            contentLock.notifyAll();
                        }
                        return null;
                    });

            synchronized (contentLock) {
                if (content == null && contentLoadError == null) {
                    try {
                        contentLock.wait();
                    } catch (InterruptedException iex) {
                        throw new IOException(Bundle.ERR_Interrupted(), iex);
                    }
                }
                if (contentLoadError != null) {
                    throw new IOException(contentLoadError);
                } else {
                    return content;
                }
            }
        }

        @Override
        public long getLength() {
            if(content != null) {
                return content.length();
            } else {
                return script.getLength();
            }
        }
    }
}
