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

package org.netbeans.core.startup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.Stamps;
import org.netbeans.Util;
import org.netbeans.core.startup.preferences.RelPaths;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ability to locate NBM-installed files.
 * Looks in ${netbeans.user} then each component of ${netbeans.dirs}
 * and finally ${netbeans.home}.
 * @author Jesse Glick
 */
@ServiceProvider(service=InstalledFileLocator.class)
public final class InstalledFileLocatorImpl extends InstalledFileLocator {
    
    private static final Logger LOG = Logger.getLogger(InstalledFileLocatorImpl.class.getName());
    
    private final File[] dirs;
    public InstalledFileLocatorImpl() {
        List<File> _dirs = computeDirs();
        dirs = _dirs.toArray(new File[0]);
    }
    
    private static void addDir(List<File> _dirs, String d) {
        if (d != null) {
            File f = new File(d).getAbsoluteFile();
            if (f.isDirectory()) {
                _dirs.add(FileUtil.normalizeFile(f));
            }
        }
    }
    
    /**
     * Cache of installed files (if present).
     * Keys are directory prefixes, e.g. "" or "x/" or "x/y/" ('/' is sep).
     * The values are nested maps; keys are entries in {@link #dirs}
     * (not all entries need have keys, only those for which the dir exists),
     * and values are unqualified file names which exist in that dir.
     */
    private static Map<String,Map<File,Set<String>>> fileCache = null;
    /**
     * Cache of cluster location(s) of modules.
     * Keys are code name bases; values are subsets of {@link #dirs}
     * in which the module appears to be installed.
     */
    private static Map<String,List<File>> clusterCache = null;
    /** tells the system that previous cache was not correct */
    private static boolean cacheMiss;
    
    /**
     * Called from <code>Main.run</code> early in the startup sequence to indicate
     * that available files should be cached from now on. Should be matched by a call to
     * {@link #discardCache} since the cache will be invalid if the user
     * e.g. installs a new NBM without restarting.
     */
    public static synchronized void prepareCache() {
        assert fileCache == null;
        fileCache = new HashMap<String,Map<File,Set<String>>>();
        clusterCache = new HashMap<String,List<File>>();
        
        try {
            InputStream is = Stamps.getModulesJARs().asStream("all-files.dat");
            if (is == null) {
                return;
            }
            DataInputStream dis = new DataInputStream(is);
            int filesSize = dis.readInt();
            for (int i = 0; i < filesSize; i++) {
                String key = dis.readUTF();
                Map<File,Set<String>> fileToKids = new HashMap<File, Set<String>>();
                int filesToKids = dis.readInt();
                for (int j = 0; j < filesToKids; j++) {
                    final String read = RelPaths.readRelativePath(dis);
                    File f = new File(read);
                    int kidsSize = dis.readInt();
                    List<String> kids = new ArrayList<String>(kidsSize);
                    for (int k = 0; k < kidsSize; k++) {
                        kids.add(dis.readUTF());
                    }
                    fileToKids.put(f, new HashSet<String>(kids));
                }
                fileCache.put(key, fileToKids);
            }
            int clusterSize = dis.readInt();
            for (int i = 0; i < clusterSize; i++) {
                String key = dis.readUTF();
                int valueSize = dis.readInt();
                List<File> values = new ArrayList<File>(valueSize);
                for (int j = 0; j < valueSize; j++) {
                    values.add(new File(RelPaths.readRelativePath(dis)));
                }
                clusterCache.put(key, values);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
            fileCache.clear();
            clusterCache.clear();
        }
    }
    
    private static synchronized void persistCache(
        DataOutputStream os, 
        Map<String, Map<File, Set<String>>> fc,
        Map<String,List<File>> cc
    ) throws IOException {
        os.writeInt(fc.size());
        for (Map.Entry<String, Map<File, Set<String>>> entry : fc.entrySet()) {
            os.writeUTF(entry.getKey());
            final Map<File, Set<String>> map = entry.getValue();
            os.writeInt(map.size());
            for (Map.Entry<File, Set<String>> children : map.entrySet()) {
                String[] parts = RelPaths.findRelativePath(children.getKey().getPath());
                assert parts != null : "No relative for " + children.getKey();
                os.writeUTF(parts[0]);
                os.writeUTF(parts[1]);
                os.writeInt(children.getValue().size());
                for (String v : children.getValue()) {
                    os.writeUTF(v);
                }
            }
        }
        os.writeInt(cc.size());
        for (Map.Entry<String, List<File>> entry : cc.entrySet()) {
            os.writeUTF(entry.getKey());
            os.writeInt(entry.getValue().size());
            for (File file : entry.getValue()) {
                String[] parts = RelPaths.findRelativePath(file.getPath());
                os.writeUTF(parts[0]);
                os.writeUTF(parts[1]);
            }
        }
    }
    
    /**
     * Called after startup is essentially complete.
     * After this point, the list of files in the installation are not
     * cached, since they might change due to dynamic NBM installation.
     * Anyway the heaviest uses of {@link InstalledFileLocator} are
     * during startup so that is when the cache has the most effect.
     * XXX called somewhat too late, before all libraries are initialized.
     * Better might be to wait until a few seconds have passed since the last call.
     * Or check for changes in .lastModified files since these should change if
     * any NBM activity happens.
     */
    public static synchronized void discardCache() {
        assert fileCache != null;
        if (cacheMiss) {
            final Map<String, Map<File, Set<String>>> fc = fileCache;
            final Map<String, List<File>> cc = clusterCache;
            Stamps.getModulesJARs().scheduleSave(new Stamps.Updater() {
                @Override
                public void flushCaches(DataOutputStream os) throws IOException {
                    persistCache(os, fc, cc);
                }
                @Override
                public void cacheReady() {
                }
            }, "all-files.dat", false); // NOI18N
        }
        fileCache = null;
        clusterCache = null;
    }
    
    /**
     * Searches user dir and install dir(s).
     */
    public @Override File locate(String relativePath, String codeNameBase, boolean localized) {
        Set<File> files = doLocate(relativePath, localized, true, codeNameBase);
        return files.isEmpty() ? null : files.iterator().next();
    }
    
    public @Override Set<File> locateAll(String relativePath, String codeNameBase, boolean localized) {
        return doLocate(relativePath, localized, false, codeNameBase);
    }

    private Set<File> doLocate(String relativePath, boolean localized, boolean single, String codeNameBase) {
        String[] prefixAndName = prefixAndName(relativePath);
        String prefix = prefixAndName[0];
        String name = prefixAndName[1];
        synchronized (InstalledFileLocatorImpl.class) {
            if (localized) {
                int i = name.lastIndexOf('.');
                String baseName, ext;
                if (i == -1) {
                    baseName = name;
                    ext = "";
                } else {
                    baseName = name.substring(0, i);
                    ext = name.substring(i);
                }
                Set<File> files = null;
                for (String suffix : org.netbeans.Util.getLocalizingSuffixesFast()) {
                    String locName = baseName + suffix + ext;
                    Set<File> f = locateExactPath(prefix, locName, single, codeNameBase);
                    if (!f.isEmpty()) {
                        if (single) {
                            return f;
                        } else if (files == null) {
                            files = f;
                        } else {
                            files = new LinkedHashSet<File>(files);
                            files.addAll(f);
                        }
                    }
                }
                return files != null ? files : Collections.<File>emptySet();
            } else {
                return locateExactPath(prefix, name, single, codeNameBase);
            }
        }
    }

    /** Search all top dirs for a file. */
    private Set<File> locateExactPath(String prefix, String name, boolean single, String codeNameBase) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        Set<File> files = null;
        String path = prefix + name;
        if (fileCache != null) {
            Map<File,Set<String>> fileCachePerPrefix = fileCachePerPrefix(prefix);
            for (File dir : clustersFor(codeNameBase, path)) {
                Set<String> names = fileCachePerPrefix.get(dir);
                if (names != null && names.contains(name)) {
                    assert owned(codeNameBase, dir, path);
                    File f = makeFile(dir, path);
                    if (single) {
                        return Collections.singleton(f);
                    } else if (files == null) {
                        files = Collections.singleton(f);
                    } else {
                        files = new LinkedHashSet<File>(files);
                        files.add(f);
                    }
                }
            }
        } else {
            for (File dir : clustersFor(codeNameBase, path)) {
                File f = makeFile(dir, path);
                if (f.exists()) {
                    assert owned(codeNameBase, dir, path);
                    if (single) {
                        return Collections.singleton(f);
                    } else if (files == null) {
                        files = Collections.singleton(f);
                    } else {
                        files = new LinkedHashSet<File>(files);
                        files.add(f);
                    }
                }
            }
        }
        return files != null ? files : Collections.<File>emptySet();
    }
    
    private List<File> clustersFor(String codeNameBase, String path) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        if (codeNameBase == null) {
            return Arrays.asList(dirs);
        }
        String codeNameBaseDashes = codeNameBase.replace('.', '-');
        if (path.matches("(modules/(locale/)?)?" + codeNameBaseDashes + "(_[^/]+)?[.]jar")) { // NOI18N
            // Called very commonly during startup; cannot afford to do exact check each time.
            // Anyway if the module is there it is almost certainly installed in the same cluster.
            return Arrays.asList(dirs);
        }
        List<File> clusters = clusterCache != null ? clusterCache.get(codeNameBase) : null;
        if (clusters == null) {
            clusters = new ArrayList<File>(1);
            String rel = "update_tracking/" + codeNameBaseDashes + ".xml"; // NOI18N
            for (File dir : dirs) {
                File tracking = new File(dir, rel);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "checking {0} due to {1} cache={2}", new Object[] {tracking, path, clusterCache != null});
                }
                if (tracking.isFile()) {
                    clusters.add(dir);
                }
            }
            if (clusterCache != null) {
                clusterCache.put(codeNameBase, clusters);
                scheduleSave();
            }
        }
        if (clusters.isEmpty()) {
            // Perhaps running without update_tracking, so just search everything.
            return Arrays.asList(dirs);
        }
        return clusters;
    }

    private static String[] prefixAndName(String relativePath) {
        if (relativePath.length() == 0) {
            throw new IllegalArgumentException("Cannot look up \"\" in InstalledFileLocator.locate"); // NOI18N
        }
        if (relativePath.charAt(0) == '/') {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not start with '/': " + relativePath); // NOI18N
        }
        int slashIdx = relativePath.lastIndexOf('/');
        if (slashIdx == relativePath.length() - 1) {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not end in '/': " + relativePath); // NOI18N
        }

        String prefix, name;
        if (slashIdx != -1) {
            prefix = relativePath.substring(0, slashIdx + 1);
            name = relativePath.substring(slashIdx + 1);
            assert name.length() > 0;
        } else {
            prefix = "";
            name = relativePath;
        }
        return new String[] {prefix, name};
    }

    private Map<File,Set<String>> fileCachePerPrefix(String prefix) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        Map<File,Set<String>> fileCachePerPrefix = fileCache.get(prefix);
        if (fileCachePerPrefix == null) {
            fileCachePerPrefix = new HashMap<File,Set<String>>(dirs.length * 2);
            for (int i = 0; i < dirs.length; i++) {
                File root = dirs[i];
                File d;
                boolean isDir;
                if (prefix.length() > 0) {
                    assert prefix.charAt(prefix.length() - 1) == '/';
                    d = new File(root, prefix.substring(0, prefix.length() - 1).replace('/', File.separatorChar));
                    isDir = d.isDirectory();
                } else {
                    d = root;
                    isDir = true;
                }
                if (isDir) {
                    String[] kids = d.list();
                    if (kids != null) {
                        fileCachePerPrefix.put(root, new HashSet<String>(Arrays.asList(kids)));
                    } else {
                        Util.err.log(Level.WARNING, "could not read files in {0} at {1}", new Object[] {d, findCaller()});
                    }
                }
            }
            fileCache.put(prefix, fileCachePerPrefix);
            scheduleSave();
        }
        return fileCachePerPrefix;
    }
    
    private static File makeFile(File dir, String path) {
        return new File(dir, path.replace('/', File.separatorChar));
    }
    
    private static synchronized boolean owned(String codeNameBase, File dir, String path) {
        if (codeNameBase == null) {
            LOG.log(Level.WARNING, "no code name base passed when looking up {0} at {1}", new Object[] {path, findCaller()});
            return true;
        }
        if (path.lastIndexOf('_') > path.lastIndexOf('/')) {
            // Probably a locale variant. Permit these to be owned by any module -
            // otherwise it would be difficult to contribute branding.
            return true;
        }
        String codeNameBaseDashes = codeNameBase.replace('.', '-');
        if (path.equals("modules/" + codeNameBaseDashes + ".jar")) { // NOI18N
            // Very common case, no need to waste time checking this.
            return true;
        }
        if (path.equals("update_tracking/" + codeNameBaseDashes + ".xml")) { // NOI18N
            // Technically illegitimate - no one owns this metadata - but used by
            // org.netbeans.modules.autoupdate.services.Utilities.locateUpdateTracking
            // and probably harmless since this module would not be used with other impls.
            return true;
        }
        Map<String,Set<String>> ownershipByModule = ownershipByModuleByCluster.get(dir);
        File updateDir = new File(dir, "update_tracking");
        if (ownershipByModule == null) {
            if (!updateDir.isDirectory()) {
                LOG.log(Level.FINE, "No update tracking found in {0}", dir);
                return true;
            }
            ownershipByModule = new HashMap<String,Set<String>>();
            ownershipByModuleByCluster.put(dir, ownershipByModule);
        }
        Set<String> ownership = ownershipByModule.get(codeNameBase);
        if (ownership == null) {
            File list = new File(updateDir, codeNameBaseDashes + ".xml"); // NOI18N
            if (!list.isFile()) {
                LOG.log(Level.WARNING, "no such module {0} at {1}", new Object[] {list, findCaller()});
                return true;
            }
            ownership = new HashSet<String>();
            try {
                // Could do a proper XML parse but likely too slow.
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Parsing {0} due to {1}", new Object[] {list, path});
                }
                Reader r = new FileReader(list);
                try {
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        Matcher m = FILE_PATTERN.matcher(line);
                        if (m.matches()) {
                            ownership.add(m.group(1));
                        }
                    }
                    br.close();
                } finally {
                    r.close();
                }
            } catch (IOException x) {
                LOG.log(Level.INFO, "could not parse " + list, x);
                return true;
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "parsed {0} -> {1}", new Object[] {list, ownership});
            }
            ownershipByModule.put(codeNameBase, ownership);
        }
        if (!ownership.contains(path)) {
            boolean found = false;
            if (makeFile(dir, path).isDirectory()) {
                String pathSlash = path + "/"; // NOI18N
                for (String owned : ownership) {
                    if (owned.startsWith(pathSlash)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                LOG.log(Level.WARNING, "module {0} in {1} does not own {2} at {3}", new Object[] {codeNameBase, dir, path, findCaller()});
            }
        }
        return true;
    }
    private static final Pattern FILE_PATTERN = Pattern.compile("\\s*<file.+name=[\"']([^\"']+)[\"'].*/>");
    private static final Map<File,Map<String,Set<String>>> ownershipByModuleByCluster = new HashMap<File,Map<String,Set<String>>>();

    private static String findCaller() {
        for (StackTraceElement line : Thread.currentThread().getStackTrace()) {
            if (!line.getClassName().matches(".*InstalledFileLocator.*|java[.].+")) { // NOI18N
                return line.toString();
            }
        }
        return "???"; // NOI18N
    }

    private static synchronized void scheduleSave() {
        cacheMiss = true;
    }

    static List<File> computeDirs() {
        List<File> _dirs = new ArrayList<File>();
        addDir(_dirs, System.getProperty("netbeans.user")); // NOI18N
        String nbdirs = System.getProperty("netbeans.dirs"); // #27151
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                addDir(_dirs, tok.nextToken());
            }
        }
        addDir(_dirs, System.getProperty("netbeans.home"));
        return _dirs;
    }
}
