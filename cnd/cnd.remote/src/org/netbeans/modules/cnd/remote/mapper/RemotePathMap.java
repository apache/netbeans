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
package org.netbeans.modules.cnd.remote.mapper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Timer;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.spi.remote.setup.MirrorPathProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * An implementation of PathMap which returns remote path information.
 *
 */
public abstract class RemotePathMap extends PathMap {

    private final static Map<ExecutionEnvironment, Map<String, RemotePathMap>> fixedPathMaps =
            new HashMap<>();

    private final static Map<ExecutionEnvironment, Map<String, RemotePathMap>> customPathMaps =
            new HashMap<>();


    public static RemotePathMap getPathMap(ExecutionEnvironment env) {
        return getPathMap(env, ServerList.get(env).getSyncFactory().isPathMappingCustomizable());
    }

    public static RemotePathMap getPathMap(ExecutionEnvironment env, boolean customizable) {

        Map<ExecutionEnvironment, Map<String, RemotePathMap>> pmtable =
                customizable ? customPathMaps : fixedPathMaps;

        String syncID = getEnvSyncID(env);
        Map<String, RemotePathMap> pathmaps = pmtable.get(env);
        RemotePathMap pathmap = null;
        if (pathmaps == null) {
            synchronized (pmtable) {
                pathmaps = new HashMap<>();
                pmtable.put(env, pathmaps);
            }
        }
        pathmap = pathmaps.get(syncID);
        if (pathmap == null) {
            synchronized (pmtable) {
                pathmap = customizable ? new CustomizableRemotePathMap(env) : new FixedRemotePathMap(env);
                pathmaps.put(syncID, pathmap);
            }
        }
        pathmap.initIfNeeded();
        return pathmap;
    }

    protected final HashMap<String, String> map = new HashMap<>();
    protected final ExecutionEnvironment execEnv;
    
    protected RemotePathMap(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    protected final boolean loadFromPrefs() {
        synchronized (map) {
            String list = getPreferences(execEnv);

            if (list == null) {
                // 1. Developers entry point
                String pmap = System.getProperty("cnd.remote.pmap");
                if (pmap != null) {
                    String line;
                    File file = CndFileUtils.createLocalFile(pmap);

                    if (file.exists() && file.canRead()) {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // NOI18N
                            try {
                                while ((line = in.readLine()) != null) {
                                    int pos = line.indexOf(' ');
                                    if (pos > 0) {
                                        map.put(line.substring(0, pos), line.substring(pos + 1).trim());
                                    }
                                }
                            } finally {
                                in.close();
                            }
                        } catch (IOException ioe) {
                        }
                    }
                } else {
                    return false;
                }
            } else {
                // 3. Deserialization
                String[] paths = list.split(DELIMITER);
                for (int i = 0; i < paths.length; i += 2) {
                    if (i + 1 < paths.length) { //TODO: only during development
                        map.put(paths[i], paths[i + 1]);
                    } else {
                        System.err.println("mapping serialization flaw. Was found: " + list);
                    }
                }
            }
            return true;
        }
    }
    /**
     *
     * Initialization the path map here:
     */
    public abstract void initIfNeeded();

    // PathMap
    @Override
    public String getRemotePath(String lpath, boolean useDefault) {
        if (lpath == null) {
            CndUtils.assertUnconditional("local path should not be null"); // nOI18N
            return null;
        }
        String ulpath = unifySeparators(lpath); // NB: adds a trailing slash
        String rpath = null;
        int max = 0;
        // search for the *longest* key that starts with lpath
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = unifySeparators(entry.getKey());
            if (ulpath.startsWith(key)) {
                if (rpath == null || key.length() > max) {
                    max = key.length();
                    String mpoint = entry.getValue();
                    String rest = key.length() > lpath.length() ? "" : lpath.substring(key.length()).replace('\\', '/'); //NOI18N
                    if (!mpoint.endsWith("/")) { // NOI18N
                        mpoint += '/';
                    }
                    rpath = mpoint + rest;
                }
            }
        }
        if (rpath != null) {
            return rpath;
        } else {
            return useDefault ? lpath : null;
        }
    }

    @Override
    public String getLocalPath(String rpath, boolean useDefault) {
        if (rpath == null) {
            CndUtils.assertUnconditional("remote path should not be null"); // nOI18N
            return null;
        }
        String urpath = unifySeparators(rpath); // NB: adds a trailing slash
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = unifySeparators(entry.getValue());
            if (urpath.startsWith(value)) {
                String mpoint = entry.getKey();
                String rest = (value.length() > rpath.length()) ? "" : rpath.substring(value.length()); //NOI18N
                if (mpoint.length() > 0 && !(mpoint.endsWith("/") || mpoint.endsWith("\\"))) { //NOI18N
                    mpoint += '/';
                }
                return mpoint + rest;
            }
        }
        return null;
    }

    @Override
    public String getTrueLocalPath(String rpath) {
        return getLocalPath(rpath, false);
    }

    @Override
    public boolean checkRemotePaths(File[] localFiles, boolean fixMissingPaths) {
        List<String> localPaths = new ArrayList<>();
        for (File file : localFiles) {
            if (file.isDirectory()) {
                localPaths.add(file.getAbsolutePath());
            } else {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    localPaths.add(parentFile.getAbsolutePath());
                }
            }
        }
        // sort local paths so that if there are parent paths, they go first
        Collections.sort(localPaths);
        List<String> invalidLocalPaths = new ArrayList<>();
        for (String lPath : localPaths) {
            if (!checkRemotePath(lPath)) {
                invalidLocalPaths.add(lPath);
            }
        }
        if (invalidLocalPaths.isEmpty()) {
            return true;
        } else if (fixMissingPaths) {
            boolean isFixed = FixRemotePathMapper.getInstance().fixRemotePath(execEnv, invalidLocalPaths);
            if (!isFixed) {
                return false;
            }
//            if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
//                return false;
//            }
//            if (EditPathMapDialog.showMe(execEnv, invalidLocalPaths)) {
            // EditPathMapDialog doesn't perform check
            for (String lPath : invalidLocalPaths) {
                if (!checkRemotePath(lPath)) {
                    return false;
                }
            }
            return true;
//            } else {
//                return false;
//            }
        } else {
            return false;
        }
    }


    private boolean checkRemotePath(String lpath) {
        if (lpath == null) {
            CndUtils.assertUnconditional("local path should not be null"); // nOI18N
            return false;
        }
        String ulpath = unifySeparators(lpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mpoint = unifySeparators(entry.getKey());
            if (ulpath.startsWith(mpoint)) {
                return true;
            }
        }

        for (String mpoint : map.keySet()) {
            if (ulpath.startsWith(unifySeparators(mpoint))) {
                return true;
            }
        }

        try {
            // check if local path is mirrored by remote path
            if (validateMapping(execEnv, lpath, CndFileUtils.createLocalFile(lpath))) {
                synchronized (map) {
                    map.put(lpath, lpath);
                }
                return true;
            }
        } catch (InterruptedException ex) {
            return false;
        }
        return false;
    }

    public void addMapping(String localParent, String remoteParent) {
        addMappingImpl(localParent, remoteParent);
    }

    protected void addMappingImpl(String localParent, String remoteParent) {
        CndUtils.assertNotNull(localParent, "local path shouldn't be null"); //NOI18N
        CndUtils.assertNotNull(remoteParent, "remote path shouldn't be null"); //NOI18N
        if (localParent == null || remoteParent == null) {
            return;
        }
        synchronized( map ) {
            Map<String, String> clone = new LinkedHashMap<>(map);
            clone.put(localParent,remoteParent);
            updatePathMapImpl(clone);
        }
    }


    // Utility
    public void updatePathMap(Map<String, String> newPathMap) {
        updatePathMapImpl(newPathMap);
    }

    protected void updatePathMapImpl(Map<String, String> newPathMap) {
        synchronized( map ) {
            map.clear();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : newPathMap.entrySet()) {
                String remotePath = fixEnding(entry.getValue());
                String path = fixEnding(entry.getKey());
                map.put(path, remotePath);
                sb.append( fixEnding(path) );
                sb.append(DELIMITER);
                sb.append( remotePath );
                sb.append(DELIMITER);
            }
            setPreferences(sb.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMap() {
        return (Map<String, String>)map.clone();
    }

    private static String fixEnding(String path) {
        //TODO: system dependent separator?
        if (path.charAt(path.length()-1)!='/' && path.charAt(path.length()-1)!='\\') {
            return path + "/"; //NOI18N
        } else {
            return path;
        }
    }
    // inside path mapper we use only / and lowercase
    // TODO: lowercase should be only windows issue -- possible flaw
    private static String unifySeparators(String path) {
        String result = path.replace('\\', '/');
        if (!CndFileUtils.isSystemCaseSensitive()) {
            result = result.toLowerCase(Locale.getDefault());
        }
        if (!result.endsWith("/")) { //NOI18N
            result = result + "/"; //NOI18N
        }
        return result;
    }

    public static boolean isSubPath(String path, String pathToValidate) {
        CndUtils.assertNotNull(path, "path should not be null"); // nOI18N
        CndUtils.assertNotNull(pathToValidate, "pathToValidate should not be null"); // nOI18N
        if (path == null || pathToValidate == null) {
            return false;
        }
        return unifySeparators(pathToValidate).startsWith(unifySeparators(path));
    }

    private static final String REMOTE_PATH_MAP = "remote-path-map"; // NOI18N
    private static final String DELIMITER = "\n"; // NOI18N

    private static String getEnvSyncID(ExecutionEnvironment env) {
        return ServerList.get(env).getSyncFactory().getID();
    }

    private static String getPreferences(ExecutionEnvironment execEnv) {
        return NbPreferences.forModule(RemotePathMap.class).get(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv) + getEnvSyncID(execEnv), null);
    }

    private void setPreferences(String newValue) {
        NbPreferences.forModule(RemotePathMap.class).put(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv) + getEnvSyncID(execEnv), newValue);
    }

    private static boolean validateMapping(ExecutionEnvironment execEnv,
            String rpath, File lpath) throws InterruptedException {
        if (!PlatformInfo.getDefault(execEnv).isWindows() && !PlatformInfo.getDefault(ExecutionEnvironmentFactory.getLocal()).isWindows()) {
            return isTheSame(execEnv, rpath, lpath);
        }
        return false;
    }

    /**
     * Determines whether local and remote directories coincide,
     * i.e. map to the same physical directory
     * @param execEnv remote environment
     * @param localDir local path
     * @param remoteDir remote path
     * @return
     */
    // TODO: move to a more appropriate place
    @org.netbeans.api.annotations.common.SuppressWarnings("RV") // FindBugs warns that validationFile.delete() ret. value is ignored
    public static boolean isTheSame(ExecutionEnvironment execEnv, String rpath, File path) throws InterruptedException {
        if (path.exists() && path.isDirectory()) {
            File validationFile = null;
            BufferedWriter out = null;
            try {
                // create file
                validationFile = File.createTempFile("cnd", ".pathmap", path); // NOI18N
                if (validationFile.exists()) {
                    out = Files.newBufferedWriter(validationFile.toPath(), Charset.forName("UTF-8")); //NOI18N
                    String validationLine = Double.toString(Math.random());
                    out.write(validationLine);
                    out.close();
                    out = null;

                    ProcessUtils.ExitStatus rcs = ProcessUtils.execute(
                            execEnv, "grep", // NOI18N
                            validationLine,
                            rpath + "/" + validationFile.getName()); // NOI18N

                    if (rcs.isOK()) {
                        return true;
                    }
                    if (rcs.exitCode== -100) { // there is no official way to check for cancelled
                        throw new InterruptedException();
                    }
                }
            } catch (IOException ex) {
                // directory is write protected
            } finally {
                if (validationFile != null && validationFile.exists()) {
                    validationFile.delete(); // it isn\t worth removing RV FindBugs violation here
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return false;
    }

    private final static class CustomizableRemotePathMap extends RemotePathMap {

        private static final int TIMEOUT = Integer.getInteger("remote.path.map.analyzer.timeout", 10000); // NOI18N

        private final Object lock = new Object();
        private boolean initialized = false;

        private CustomizableRemotePathMap(ExecutionEnvironment exc) {
            super(exc);
        }

        @Override
        public void initIfNeeded() {
            synchronized (lock) {
                if (initialized) {
                    return;
                }
                if (loadFromPrefs()) {
                    initialized = true;
                } else {
                    if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                        return;
                    }
                    final AtomicReference<Boolean> cancelled = new AtomicReference<>(Boolean.FALSE);
                    // 2. Automated mappings gathering entry point
                    final HostMappingsAnalyzer ham = new HostMappingsAnalyzer(execEnv);
                    Timer timer = new Timer(TIMEOUT, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cancelled.set(Boolean.TRUE);
                            ham.cancel();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    Map<String, String> mappings = ham.getMappings();
                    synchronized( map ) {
                        map.putAll(mappings);
                    }
                    if (!cancelled.get().booleanValue()) {
                        initialized = true;
                    }
                }
            }
        }
    }

    private static final String NO_MAPPING_PREFIX = "///"; // NOI18N
    private final static class FixedRemotePathMap extends RemotePathMap {

        private volatile String remoteBase;

        private FixedRemotePathMap(ExecutionEnvironment exc) {
            super(exc);
            initRemoteBase(false);
        }

        @Override
        public void initIfNeeded() {
            // Fix for noIZ: IDE fails to build if -J-Dcnd.remote.sync.root is specified
            // Loading from prefs leads to incompatibility with MirrorPathProvider.getRemoteMirror,
            // which is widely used
            //if (!loadFromPrefs()) {
                if (remoteBase != null) {
                    super.addMappingImpl("/", remoteBase); // NOI18N
                }
            //}
        }

        @Override
        public String getRemotePath(String lpath, boolean useDefault) {
            if (lpath == null) {
                CndUtils.assertUnconditional("local path should not be null"); // nOI18N
                return null;
            }
            initRemoteBase(true);
            if (remoteBase == null) {
                return useDefault ? lpath : null;
            }
            String remotePath = lpath;
            // for IZ#175198
            if (remotePath.startsWith(NO_MAPPING_PREFIX)) {
                return remotePath;
            }
            if (!isSubPath(remoteBase, lpath)) {
                if (lpath != null && Utilities.isWindows() && !"/".equals(lpath)) { // NOI18N
                    lpath = WindowsSupport.getInstance().convertToMSysPath(lpath);
                }
                remotePath = super.getRemotePath(lpath, useDefault);
            }
            return remotePath;
        }

        @Override
        public String getLocalPath(String rpath, boolean useDefault) {
            initRemoteBase(true);
            // for IZ#175198
            if (rpath.startsWith(NO_MAPPING_PREFIX)) {
                return rpath;
            }
            String res;
            if (isSubPath(remoteBase, rpath)) {
                res = super.getLocalPath(rpath, useDefault);
                if (res != null && Utilities.isWindows() && !"/".equals(res)) { // NOI18N
                    res = WindowsSupport.getInstance().convertFromMSysPath(res);
                }
            } else {
                res = null;
            }
            return res;
        }

        @Override
        public String getTrueLocalPath(String rpath) {
            initRemoteBase(true);
            // for IZ#175198
            if (rpath.startsWith(NO_MAPPING_PREFIX)) {
                return rpath;
            }
            String res = null;
            if (isSubPath(remoteBase, rpath)) {
                res = super.getLocalPath(rpath, false);
                if (res != null && Utilities.isWindows() && !"/".equals(res)) { // NOI18N
                    res = WindowsSupport.getInstance().convertFromMSysPath(res);
                }
            }
            return res;
        }

        private void initRemoteBase(boolean addMapping) {
            if (remoteBase == null) {
                remoteBase = getRemoteSyncRoot(super.execEnv);
                if (addMapping && remoteBase != null) {
                    addMappingImpl("/", remoteBase); // NOI18N
                }
            }
        }

        @Override
        public void addMapping(String localParent, String remoteParent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePathMap(Map<String, String> newPathMap) {
            CndUtils.assertTrue(false, "Should never be called for " + getClass().getSimpleName()); //NOI18N
        }
    }

    public static String getRemoteSyncRoot(ExecutionEnvironment executionEnvironment) {
        for (MirrorPathProvider mpp : Lookup.getDefault().lookupAll(MirrorPathProvider.class)) {
            try {
                String result = mpp.getRemoteMirror(executionEnvironment);
                if (result != null) {
                    return result;
                }
            } catch (ConnectException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (CancellationException ex) {
                // don't report CancellationException
            }
        }
        return null;
    }

    public static String getLocalSyncRoot(ExecutionEnvironment executionEnvironment) {
        for (MirrorPathProvider mpp : Lookup.getDefault().lookupAll(MirrorPathProvider.class)) {
            String result = mpp.getLocalMirror(executionEnvironment);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}

