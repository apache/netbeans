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
    private final int numPrefixes;
    private final String[] localPathPrefixes;
    private final String[] serverPathPrefixes;
    private final char localPathSeparator;
    private final char serverPathSeparator;
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
        if (localPaths != null && !localPaths.isEmpty() && serverPaths != null && !serverPaths.isEmpty()) {
            this.doPathTranslation = true;
            int n = localPaths.size();
            this.numPrefixes = n;
            this.localPathPrefixes = new String[n];
            this.serverPathPrefixes = new String[n];
            for (int i = 0; i < n; i++) {
                this.localPathPrefixes[i] = stripSeparator(localPaths.get(i));
            }
            this.localPathSeparator = findSeparator(localPaths.get(0));
            for (int i = 0; i < n; i++) {
                this.serverPathPrefixes[i] = stripSeparator(serverPaths.get(i));
            }
            this.serverPathSeparator = findSeparator(serverPaths.get(0));
        } else {
            this.doPathTranslation = false;
            this.localPathPrefixes = null;
            this.serverPathPrefixes = null;
            this.localPathSeparator = 0;
            this.serverPathSeparator = 0;
            this.numPrefixes = 0;
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
                "ScriptsHandler: doPathTranslation = {0}, localPathPrefixes = {1}, separator = {2}, "
                + "serverPathPrefixes = {3}, separator = {4}, "
                + "localRoots = {5}, localPathExclusionFilters = {6}.",
                new Object[]{doPathTranslation, Arrays.toString(localPathPrefixes), localPathSeparator,
                    Arrays.toString(serverPathPrefixes), serverPathSeparator,
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
        String name = script.getUrl().getPath();
        if(name == null) {
            return null;
        }
        File localFile = null;
        if (doPathTranslation) {
            try {
                String lp = getLocalPath(name);
                localFile = new File(lp);
            } catch (OutOfScope oos) {
            }
        } else {
            File f = new File(name);
            if (f.isAbsolute()) {
                localFile = f;
            }
        }
        if (localFile != null) {
            FileObject fo = FileUtil.toFileObject(localFile);
            if (fo != null) {
                synchronized (scriptsByURL) {
                    scriptsByURL.put(fo.toURL(), script);
                }
                return fo;
            }
        }
        if (name == null) {
            name = "unknown.js";
        }
        // prepend <host>_<port>/ to the name.
        name = remotePathPrefix + name;
        URL sourceURL = SourceFilesCache.getDefault().getSourceFile(name, script.getHash(), new ScriptContentLoader(script, dbg));
        synchronized (scriptsByURL) {
            scriptsByURL.put(sourceURL, script);
        }
        return URLMapper.findFileObject(sourceURL);
    }

    /**
     * Find a known script by it's actual URL.
     * @param scriptURL Script's URL returned by {@link #getFile(org.netbeans.lib.v8debug.CDTScript)}
     * @return the script or <code>null</code> when not found.
     */
    @CheckForNull
    public CDTScript findScript(@NonNull URL scriptURL) {
        synchronized (scriptsByURL) {
            return scriptsByURL.get(scriptURL);
        }
    }

    @CheckForNull
    public String getServerPath(@NonNull FileObject fo) {
        String serverPath;
        File file = FileUtil.toFile(fo);
        if (file != null) {
            String localPath = file.getAbsolutePath();
            try {
                serverPath = getServerPath(localPath);
            } catch (ScriptsHandler.OutOfScope oos) {
                serverPath = null;
            }
        } else {
            URL url = fo.toURL();
            CDTScript script = findScript(url);
            if (script != null) {
                serverPath = script.getUrl().getPath();
            } else if (SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
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
                serverPath = null;
            }
        }
        return serverPath;
    }

    @CheckForNull
    public String getServerPath(@NonNull URL url) {
        if (!SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
            return null;
        }
        String path;
        try {
            path = url.toURI().getPath();
        } catch (URISyntaxException usex) {
            return null;
        }
        int l = path.length();
        int index = 0;
        while (index < l && path.charAt(index) == '/') {
            index++;
        }
        int begin = path.indexOf('/', index);
        if (begin > 0) {
            // path.substring(begin + 1).startsWith(remotePathPrefix)
            if (path.regionMatches(begin + 1, remotePathPrefix, 0, remotePathPrefix.length())) {
                path = path.substring(begin + 1 + remotePathPrefix.length());
                return path;
            } else {
                // Path with a different prefix
                return null;
            }
        } else {
            return null;
        }
    }

    public String getLocalPath(@NonNull String serverPath) throws OutOfScope {
        if (!doPathTranslation) {
            return serverPath;
        } else {
            for (int i = 0; i < numPrefixes; i++) {
                if (isChildOf(serverPathPrefixes[i], serverPath)) {
                    return translate(serverPath, serverPathPrefixes[i], serverPathSeparator,
                                     localPathPrefixes[i], localPathSeparator);
                }
            }
        }
        throw new OutOfScope(serverPath, Arrays.toString(serverPathPrefixes));
    }

    public String getServerPath(@NonNull String localPath) throws OutOfScope {
        if (!doPathTranslation) {
            return localPath;
        } else {
            for (int i = 0; i < numPrefixes; i++) {
                if (isChildOf(localPathPrefixes[i], localPath)) {
                    return translate(localPath, localPathPrefixes[i], localPathSeparator,
                                     serverPathPrefixes[i], serverPathSeparator);
                }
            }
        }
        throw new OutOfScope(localPath, Arrays.toString(localPathPrefixes));
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

    private static boolean isChildOf(String parent, String child) {
        if (!child.startsWith(parent)) {
            return false;
        }
        int l = parent.length();
        if (!isRootPath(parent)) { // When the parent is the root, do not do further checks.
            if (child.length() > l && !isSeparator(child.charAt(l))) {
                return false;
            }
        }
        return true;
    }

    private static String translate(String path, String pathPrefix, char pathSeparator, String otherPathPrefix, char otherPathSeparator) throws OutOfScope {
        if (!path.startsWith(pathPrefix)) {
            throw new OutOfScope(path, pathPrefix);
        }
        int l = pathPrefix.length();
        if (!isRootPath(pathPrefix)) { // When the prefix is the root, do not do further checks.
            if (path.length() > l && !isSeparator(path.charAt(l))) {
                throw new OutOfScope(path, pathPrefix);
            }
        }
        while (path.length() > l && isSeparator(path.charAt(l))) {
            l++;
        }
        String otherPath = path.substring(l);
        if (pathSeparator != otherPathSeparator) {
            otherPath = otherPath.replace(pathSeparator, otherPathSeparator);
        }
        if (otherPath.isEmpty()) {
            return otherPathPrefix;
        } else {
            if (isRootPath(otherPathPrefix)) { // Do not append further slashes to the root
                return otherPathPrefix + otherPath;
            } else {
                return otherPathPrefix + otherPathSeparator + otherPath;
            }
        }
    }

    private static char findSeparator(String path) {
        if (path.indexOf('/') >= 0) {
            return '/';
        }
        if (path.indexOf('\\') >= 0) {
            return '\\';
        }
        return '/';
    }

    private static boolean isSeparator(char c) {
        return c == '/' || c == '\\';
    }

    private static boolean isRootPath(String path) {
        if ("/".equals(path)) {
            return true;
        }
        if (path.length() == 4 && path.endsWith(":\\\\")) { // "C:\\"
            return true;
        }
        return false;
    }

    private static String stripSeparator(String path) {
        if (isRootPath(path)) { // Do not remove slashes the root
            return path;
        }
        while (path.length() > 1 && (path.endsWith("/") || path.endsWith("\\"))) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }


    public static final class OutOfScope extends Exception {

        private OutOfScope(String path, String scope) {
            super(path);
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
