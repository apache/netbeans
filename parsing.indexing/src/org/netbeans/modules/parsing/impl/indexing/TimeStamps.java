/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class TimeStamps {

    // -J-Dorg.netbeans.modules.parsing.impl.indexing.TimeStamps.level=FINE
    private static final Logger LOG = Logger.getLogger(TimeStamps.class.getName());
    private static final String TIME_STAMPS_FILE = "timestamps.properties"; //NOI18N
    private static final String VERSION = "#v2"; //NOI18N

    
    private final Implementation impl;
    

    private TimeStamps(@NonNull final Implementation impl) throws IOException {
        assert impl != null;
        this.impl = impl;
    }
    
    public boolean checkAndStoreTimestamp(FileObject f, String relativePath) {
        return impl.checkAndStoreTimestamp(f, relativePath);
    }

    public Set<String> getUnseenFiles() {
        return impl.getUnseenFiles();
    }
    
    public void store () throws IOException {
        impl.store();
    }
    
    void resetToNow() {
        final long now = System.currentTimeMillis();
        impl.reset(now);
    }

    void remove(@NonNull final Iterable<? extends String> relativePaths) {
        Parameters.notNull("relativePaths", relativePaths); //NOI18N
        impl.remove(relativePaths);
    }

    @NonNull
    Collection<? extends String> getEnclosedFiles(@NonNull String folder) {
        Parameters.notNull("folder", folder);           //NOI18N
        if (!folder.isEmpty() && folder.charAt(folder.length()-1) != '/') {  //NOI18N
            folder = folder + '/';                      //NOI18N
        }
        Collection<? extends String> res = impl.getEnclosedFiles(folder);
        return res;
    }

    public static TimeStamps forRoot(
            @NonNull final URL root,
            final boolean detectDeletedFiles) throws IOException {
        return new TimeStamps(new RegularImpl(root, detectDeletedFiles));
    }
    
    public static TimeStamps changedTransient() throws IOException {
        return new TimeStamps(new AllChangedTransientImpl());
    }

    public static boolean existForRoot (final URL root) throws IOException {
        assert root != null;

        FileObject cacheDir = CacheFolder.getDataFolder(
            root,
            EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
            CacheFolderProvider.Mode.EXISTENT);
        if (cacheDir != null) {
            return new File (FileUtil.toFile(cacheDir),TIME_STAMPS_FILE).exists();
        }
        
        return false;
    }
    
    private static interface Implementation {
        boolean checkAndStoreTimestamp(FileObject root, String relativePath);
        Set<String> getUnseenFiles();
        void reset(long time);
        void remove(@NonNull Iterable<? extends String> relativePaths);
        @NonNull
        Collection<? extends String> getEnclosedFiles(@NonNull String relativePath);
        void store() throws IOException;
    }
    
    private static final class RegularImpl implements Implementation {
        
        private final URL root;
        private final LongHashMap<String> timestamps = new LongHashMap<String>();
        private final Set<String> unseen;
        private FileObject rootFoCache;
        
        private RegularImpl(
                @NonNull final URL root,
                final boolean detectDeletedFiles) throws IOException {
            assert root != null;
            this.root = root;
            this.unseen = detectDeletedFiles ? new HashSet<String>() : null;
            load();
        }

        @Override
        public boolean checkAndStoreTimestamp(FileObject f, String relativePath) {
            if (rootFoCache == null) {
                rootFoCache = URLMapper.findFileObject(root);
            }
            final String fileId = relativePath != null ? relativePath : URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm();
            if (fileId == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "The fileId == null, relativePath: {0}, FileObject: {1}, URL: {2}, external URL: {3}", //NOI18N
                    relativePath,
                    f,
                    f.toURL().toExternalForm(),
                    URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm()));
            }
            long fts = f.lastModified().getTime();
            long lts = timestamps.put(fileId, fts);
            if (lts == LongHashMap.NO_VALUE) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: lastTimeStamp=null, fileTimeStamp={1} is out of date", new Object [] { f.getPath(), fts }); //NOI18N
                }
                return false;
            }

            if (unseen != null) {
                unseen.remove(fileId);
            }
            boolean isUpToDate = lts == fts;
            if (!isUpToDate) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: lastTimeStamp={1}, fileTimeStamp={2} is out of date", new Object [] { f.getPath(), lts, fts }); //NOI18N
                }
            }

            return isUpToDate;
        }
        
        @Override
        public Set<String> getUnseenFiles() {
            return unseen;
        }
        
        @Override
        public void reset(final long value) {
            for (LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                entry.setValue(value);
            }
        }

        @Override
        public void remove(@NonNull final Iterable<? extends String> relativePaths) {
            for (String relPath : relativePaths) {
                timestamps.remove(relPath);
            }
        }

        @NonNull
        @Override
        public Collection<? extends String> getEnclosedFiles(@NonNull final String relativePath) {
            final Set<String> res = new HashSet<String>();
            for (String filePath : timestamps.keySet()) {
                if (filePath.startsWith(relativePath)) {
                    res.add(filePath);
                }
            }
            return res;
        }

        @Override
        public void store() throws IOException {
            final File cacheDir = FileUtil.toFile(CacheFolder.getDataFolder(
                root,
                EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
                CacheFolderProvider.Mode.CREATE));
            final File f = new File(cacheDir, TIME_STAMPS_FILE);
            assert f != null;
            try {
                final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8")); //NOI18N
                try {
                    if (unseen != null) {
                        timestamps.keySet().removeAll(unseen);
                    }

                    // write version
                    out.write(VERSION); //NOI18N
                    out.newLine();

                    // write data
                    for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                        out.write(entry.getKey());
                        out.write('='); //NOI18N
                        out.write(Long.toString(entry.getValue()));
                        out.newLine();
                    }

                    out.flush();
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                //In case of IOException props are not stored, everything is scanned next time
                LOG.log(Level.FINE, null, e);
            }
        }
        
        private void load () throws IOException {
            final FileObject cacheFolder = CacheFolder.getDataFolder(
                root,
                EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
                CacheFolderProvider.Mode.EXISTENT);
            if (cacheFolder != null) {
                final File cacheDir = FileUtil.toFile(cacheFolder);
                final File f = new File (cacheDir, TIME_STAMPS_FILE);
                if (f.exists()) {
                    try {
                        boolean readOldPropertiesFormat = false;
                        {
                            final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8")); //NOI18N
                            try {
                                String line = in.readLine();
                                if (line != null && line.startsWith(VERSION)) {
                                    // it's the new format
                                    LOG.log(Level.FINE, "{0}: reading {1} timestamps", new Object [] { f.getPath(), VERSION }); //NOI18N

                                    while (null != (line = in.readLine())) {
                                        int idx = line.indexOf('='); //NOI18N
                                        if (idx != -1) {
                                            try {
                                                final String path = line.substring(0, idx);
                                                if (!path.isEmpty() && path.charAt(0) != '/') {
                                                    final long ts = Long.parseLong(line.substring(idx + 1));
                                                    timestamps.put(path, ts);
                                                } else {
                                                    LOG.log(
                                                        Level.WARNING,
                                                        "Invalid timestamp entry {0} in {1}",   //NOI18N
                                                        new Object[]{
                                                            path,
                                                            f.getAbsolutePath()
                                                        });
                                                }
                                            } catch (NumberFormatException nfe) {
                                                LOG.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[] { line, f.getPath(), nfe }); //NOI18N
                                            }
                                        }
                                    }
                                } else {
                                    // it's the old format from Properties.store()
                                    readOldPropertiesFormat = true;
                                }
                            } finally {
                                in.close();
                            }
                        }

                        if (readOldPropertiesFormat) {
                            LOG.log(Level.FINE, "{0}: reading old Properties timestamps", f.getPath()); //NOI18N
                            final Properties p = new Properties();
                            final InputStream in = new FileInputStream(f);
                            try {
                                p.load(in);
                            } finally {
                                in.close();
                            }

                            for(Map.Entry<Object, Object> entry : p.entrySet()) {
                                try {
                                    final String fileId = (String) entry.getKey();
                                    if (fileId != null) {
                                        timestamps.put(fileId, Long.parseLong((String) entry.getValue()));
                                    }
                                } catch (NumberFormatException nfe) {
                                    LOG.log(Level.FINE, "Invalid timestamp: key={0}, value={1}, timestamps={2}, exception={3}", //NOI18N
                                            new Object[] { entry.getKey(), entry.getValue(), f, nfe });
                                }
                            }
                        }

                        if (unseen != null) {
                            for (Object k : timestamps.keySet()) {
                                unseen.add((String)k);
                            }
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Timestamps loaded from {0}:", f.getPath()); //NOI18N
                            for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                                LOG.log(Level.FINEST, "{0}={1}", new Object [] { entry.getKey(), Long.toString(entry.getValue()) }); //NOI18N
                            }
                            LOG.log(Level.FINEST, "---------------------------"); //NOI18N
                        }
                    } catch (Exception e) {
                        // #176001: catching all exceptions, because j.u.Properties can throw IllegalArgumentException
                        // from its load() method
                        // In case of any exception props are empty, everything is scanned
                        timestamps.clear();
                        LOG.log(Level.FINE, null, e);
                    }
                }
            }
        }
    }

    private static class AllChangedTransientImpl implements Implementation {

        @Override
        public boolean checkAndStoreTimestamp(FileObject root, String relativePath) {
            return false;
        }

        @Override
        public Set<String> getUnseenFiles() {
            return Collections.<String>emptySet();
        }

        @Override
        public void reset(long time) {
        }

        @Override
        public void remove(@NonNull final Iterable<? extends String> relativePaths) {
        }

        @NonNull
        @Override
        public Collection<? extends String> getEnclosedFiles(@NonNull final String relativePath) {
            return Collections.<String>emptySet();
        }

        @Override
        public void store() throws IOException {
        }
    }
    
}
