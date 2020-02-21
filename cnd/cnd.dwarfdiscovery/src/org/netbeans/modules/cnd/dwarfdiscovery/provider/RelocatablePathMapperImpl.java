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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class RelocatablePathMapperImpl implements RelocatablePathMapper {

    public static final Logger LOG = Logger.getLogger(RelocatablePathMapperImpl.class.getName());
    private static final boolean TEST = false;
    private final List<MapperEntry> mapper = new ArrayList<MapperEntry>();
    private final ProjectProxy project;
    private final Map<String,Boolean> fileCache = new ConcurrentHashMap<String, Boolean>();
    private boolean isAtomatic = true;

    /**
     * Local path mapper file is located in the file
     * nbproject/private/LocaPathMapper.properties.
     *
     * If file exist and does not have automatic signature, discovery will uses
     * it for mapping build artifacts.: File format is:
     * <pre>
     * beginning_of_path_of_build_artifacts_1=beginning_of_sources_path_2
     * beginning_of_path_of_build_artifacts_2=beginning_of_sources_path_2
     * ...
     * </pre> Order is important.
     */
    private static final String AUTO_SIGNATURE = "# Automatic path mapper. CRC = "; // NOI18N
    private static final String PATH_TO_MAPPER = "nbproject/private/CodeAssistancePathMapper.properties"; // NOI18N

    /**
     * By default IDE tries to discover path mapper by analyzing source root. If
     * IDE automatically discovers wrong path mapper, you can provide own path
     * mapper see
     * {@link org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl#PATH_TO_MAPPER)}
     * or forbid discovering by this flag:
     * <pre>
     * -J-Dmakeproject.pathMapper.forbid_auto=true
     * </pre> To trace path mapper logic use flag:
     * <pre>
     * -J-Dorg.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.level=FINE
     * or
     * -J-Dorg.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.level=FINER
     * </pre>
     */
    private final boolean FORBID_AUTO_PATH_MAPPER = Boolean.getBoolean("makeproject.pathMapper.forbid_auto"); // NOI18N

    public RelocatablePathMapperImpl(ProjectProxy project) {
        this.project = project;
        if (project != null) {
            Project makeProject = project.getProject();
            List<String> list = null;
            FileObject fo = null;
            if (makeProject != null) {
                fo = makeProject.getProjectDirectory().getFileObject(PATH_TO_MAPPER);
            }
            if (fo != null && fo.isValid()) {
                BufferedReader in = null;
                long parseLong = 0;
                isAtomatic = true;
                try {
                    list = new ArrayList<String>();
                    in = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                    while (true) {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.startsWith("#")) { // NOI18N
                            // comment
                            if (line.startsWith(AUTO_SIGNATURE)) {
                                String s = line.substring(AUTO_SIGNATURE.length()).trim();
                                try {
                                    parseLong = Long.parseLong(s);
                                } catch (NumberFormatException ex) {
                                    //
                                }
                            }
                            continue;
                        }
                        line = line.trim();
                        int i = line.indexOf('='); // NOI18N
                        if (i > 0) {
                            list.add(line.substring(0, i));
                            list.add(line.substring(i + 1));
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Cannot read mapper file {0}", fo); // NOI18N
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
                if (parseLong != 0) {
                    Checksum checksum = new Adler32();
                    for(String s : list) {
                        for (char c : s.toCharArray()) {
                            checksum.update(c);
                        }
                    }
                    if (parseLong != checksum.getValue()) {
                        isAtomatic = false;
                    }
                }
            }
            if (isAtomatic) {
                list = null;
            }
            if (list != null) {
                for (int i = 0; i < list.size(); i += 2) {
                    if (i + 1 < list.size()) {
                        mapper.add(new MapperEntry(list.get(i), list.get(i + 1)));
                        LOG.log(Level.FINE, "Init path map {0} -> {1}", new Object[]{list.get(i), list.get(i + 1)}); // NOI18N
                    }
                }
            }
        }
    }

    public void save() {
        if (project != null && project.getProject() != null && isAtomatic) {
            Project nbProject = project.getProject();
            FileObject projectDirectory = nbProject.getProjectDirectory();
            OutputStreamWriter os = null;
            try {
                Checksum checksum = new Adler32();
                for (MapperEntry entry : mapper) {
                    for (char c : entry.from.toCharArray()) {
                        checksum.update(c);
                    }
                    for (char c : entry.to.toCharArray()) {
                        checksum.update(c);
                    }
                }
                FileObject fo = FileUtil.createData(projectDirectory, PATH_TO_MAPPER);
                os = new OutputStreamWriter(fo.getOutputStream());
                os.write(AUTO_SIGNATURE+checksum.getValue());
                os.write('\n'); // NOI18N
                for (MapperEntry entry : mapper) {
                    os.write(entry.from + "=" + entry.to); // NOI18N
                    os.write('\n'); // NOI18N
                }
            } catch (IOException ex) {
                System.err.println(ex);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    @Override
    public ResolvedPath getPath(String path) {
        path = path.replace('\\', '/'); //NOI18N
        synchronized (mapper) {
            for (MapperEntry entry : mapper) {
                if (path.startsWith(entry.from)) {
                    if (path.equals(entry.from)) {
                        return new ResolvedPathImpl(entry.to, entry.to);
                    } else {
                        if (path.charAt(entry.from.length()) == '/') { //NOI18N
                            return new ResolvedPathImpl(entry.to, entry.to + path.substring(entry.from.length()));
                        }
                    }
                }
            }
        }
        return null;
    }

    List<MapperEntry> dump() {
        List<MapperEntry> res;
        synchronized (mapper) {
            res = new ArrayList<MapperEntry>(mapper);
        }
        return res;
    }

    private boolean isExists(FS fs, String path) {
        Boolean res = fileCache.get(path);
        if (res != null) {
            return res;
        }
        if (!isExistsFolder(fs, path)) {
            return false;
        }
        res = fs.exists(path);
        fileCache.put(path, res);
        return res;
    }

    private boolean isExistsFolder(FS fs, String path) {
        String dir = CndPathUtilities.getDirName(path);
        if (dir != null) {
            Boolean res = fileCache.get(dir);
            if (res != null) {
                return res;
            }
            int sep = 0;
            for(int i = 0; i < dir.length(); i++) {
                if (dir.charAt(i) == '/' || dir.charAt(i) == '\\') {
                    sep++;
                }
            }
            if (sep <= 1) {
                res = fs.exists(dir);
                fileCache.put(dir, res);
                return res;
            } else {
                if (isExistsFolder(fs, dir)) {
                    res = fs.exists(dir);
                    fileCache.put(dir, res);
                    return res;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean discover(FS fs, String root, String unknown) {
        if (FORBID_AUTO_PATH_MAPPER) {
            return false;
        }
        MapperEntry mapperEntry = getMapperEntry(fs, root, unknown);
        if (mapperEntry == null) {
            LOG.log(Level.FINER, "Cannot discover path map of root {0} and canidate {1}", new Object[]{root, unknown}); // NOI18N
            return false;
        }
        String to = null;
        if (unknown.startsWith(mapperEntry.from)) {
            if (unknown.equals(mapperEntry.from)) {
                to = mapperEntry.to;
            } else {
                if (unknown.charAt(mapperEntry.from.length()) == '/') { //NOI18N
                    to = mapperEntry.to + unknown.substring(mapperEntry.from.length());
                }
            }
        }
        if (to != null && isExists(fs, unknown) && isExists(fs, to)) {
            // need to check contents
            boolean isEquals = true;
            List<String> list1 = fs.list(unknown);
            List<String> list2 = fs.list(to);
            for(String s : list1) {
                String name = CndPathUtilities.getBaseName(s);
                boolean found = false;
                for(String x : list2) {
                    if (name.equals(CndPathUtilities.getBaseName(x))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    isEquals = false;
                    break;
                }
            }
            if (!isEquals) {
                mapperEntry = new MapperEntry(unknown, unknown);
            }
        }
        LOG.log(Level.FINE, "Discover path map of root {0} and canidate {1}", new Object[]{root, unknown}); // NOI18N
        LOG.log(Level.FINE, "Found path map {0} -> {1}", new Object[]{mapperEntry.from, mapperEntry.to}); // NOI18N
        synchronized (mapper) {
            if (!mapper.contains(mapperEntry)) {
                mapper.add(mapperEntry);
            }
        }
        return true;
    }

    @Override
    public boolean add(String from, String to) {
        MapperEntry mapperEntry = new MapperEntry(from, to);
        synchronized (mapper) {
            mapper.add(mapperEntry);
        }
        return true;
    }

    private MapperEntry getMapperEntry(FS fs, String root, String unknown) {
        root = root.replace('\\', '/'); //NOI18N
        boolean driverRoot = false;
        if (root.startsWith("/")) { //NOI18N
            root = root.substring(1);
        } else {
            driverRoot = true;
        }
        boolean driverPath = false;
        unknown = unknown.replace('\\', '/'); //NOI18N
        if (unknown.startsWith("/")) { //NOI18N
            unknown = unknown.substring(1);
        } else {
            driverPath = true;
        }
        String[] rootSegments = root.split("/"); //NOI18N
        String[] unknownSegments = unknown.split("/"); //NOI18N
        int min = 0;
        for (int k = 0; k < Math.min(unknownSegments.length, rootSegments.length); k++) {
            if (!unknownSegments[k].equals(rootSegments[k])) {
                break;
            }
            min = k;
        }
        if (min > 2) {
            return null;
        }
        for (int k = 1; k < unknownSegments.length; k++) {
            loop:
            for (int i = rootSegments.length - 1; i > 1; i--) {
                StringBuilder buf = new StringBuilder();
                for (int j = 0; j < i; j++) {
                    buf.append('/'); //NOI18N
                    buf.append(rootSegments[j]);
                }
                if (TEST) {
                    buf.append('|'); //NOI18N
                }
                for (int j = k; j < unknownSegments.length; j++) {
                    buf.append('/'); //NOI18N
                    buf.append(unknownSegments[j]);
                }
                String path = driverRoot ? buf.substring(1) : buf.toString();
                if (TEST) {
                    System.out.println(path);
                    path = path.substring(0, path.indexOf('|')) + path.substring(path.indexOf('|') + 1); //NOI18N
                }
                if (isExists(fs, path)) {
                    if (k == i) {
                        boolean startEquals = true;
                        for (int l = 0; l < k; l++) {
                            if (!unknownSegments[l].equals(rootSegments[l])) {
                                startEquals = false;
                                break;
                            }
                        }
                        if (startEquals) {
                            continue loop;
                        }
                    }
                    if (k < 2 && k < unknownSegments.length - 1 && i < rootSegments.length - 1) {
                        if (unknownSegments[k].equals(rootSegments[i])) {
                            k++;
                            i++;
                        }
                    }
                    StringBuilder from = new StringBuilder();
                    for (int j = 0; j < k; j++) {
                        from.append('/'); //NOI18N
                        from.append(unknownSegments[j]);
                    }
                    StringBuilder to = new StringBuilder();
                    for (int j = 0; j < i; j++) {
                        to.append('/'); //NOI18N
                        to.append(rootSegments[j]);
                    }
                    String aFrom = driverPath ? from.substring(1) : from.toString();
                    String aTo = driverRoot ? to.substring(1) : to.toString();
                    return new MapperEntry(aFrom, aTo);
                }
            }
        }
        return null;
    }

    static final class MapperEntry {

        final String from;
        final String to;

        MapperEntry(String from, String to) {
            this.to = to;
            this.from = from;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + (this.from != null ? this.from.hashCode() : 0);
            hash = 71 * hash + (this.to != null ? this.to.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MapperEntry other = (MapperEntry) obj;
            if ((this.from == null) ? (other.from != null) : !this.from.equals(other.from)) {
                return false;
            }
            if ((this.to == null) ? (other.to != null) : !this.to.equals(other.to)) {
                return false;
            }
            return true;
        }
    }

    static final class ResolvedPathImpl implements ResolvedPath {

        private final String root;
        private final String path;

        ResolvedPathImpl(String root, String path) {
            this.root = root;
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getRoot() {
            return root;
        }
    }
}
