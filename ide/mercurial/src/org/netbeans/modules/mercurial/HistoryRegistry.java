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
package org.netbeans.modules.mercurial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessageChangedPath;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryRegistry {
    private static HistoryRegistry instance;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry"); // NOI18N
    // package private for test purposes
    static final String PERSISTED_DATA_VERSION = "1.0";
    
    private Map<File, List<HgLogMessage>> logs = Collections.synchronizedMap(new HashMap<File, List<HgLogMessage>>());
    // let's keep only 100 items in memory
    private Map<String, List<HgLogMessageChangedPath>> changesets = Collections.synchronizedMap(new LinkedHashMap<String, List<HgLogMessageChangedPath>>() {

        @Override
        protected boolean removeEldestEntry (Entry<String, List<HgLogMessageChangedPath>> eldest) {
            return size() >= 100;
        }
        
    });
    
    private HistoryRegistry() {}
    
    public static synchronized HistoryRegistry getInstance() {
        if(instance == null) {
            instance = new HistoryRegistry();
        }
        return instance;
    }
    
    public HgLogMessage[] getLogs(File repository, File[] files, String fromRevision, String toRevision) {
        HgLogMessage[] history = 
                HgCommand.getLogMessages(
                    repository,
                    new HashSet<File>(Arrays.asList(files)), 
                    fromRevision, 
                    toRevision, 
                    false, // show merges
                    false, // get files info
                    false, // get parents
                    -1,    // limit 
                    Collections.<String>emptyList(),                          // branch names
                    OutputLogger.getLogger(repository), // logger
                    false); // asc order
        if(history.length > 0) {
            for (File f : files) {
                logs.put(f, Arrays.asList(history));
            }
        }
        return history;
    }

    HgLogMessage getLog (File repository, File file, String changesetId) {
        List<HgLogMessage> knownLogs = logs.get(file);
        if (knownLogs != null) {
            for (HgLogMessage logMessage : knownLogs) {
                if (logMessage.getCSetShortID().equals(changesetId)) {
                    return logMessage;
                }
            }
        }
        HgLogMessage[] history = HgCommand.getRevisionInfo(repository, Collections.singletonList(changesetId), OutputLogger.getLogger(repository));
        return history == null || history.length == 0 ? null : history[0];
    }
    
    public File getHistoryFile(final File repository, final File originalFile, final String revision, final boolean dryTry) {
        long t = System.currentTimeMillis();
        String originalPath = HgUtils.getRelativePath(originalFile);
        try {
            final List<HgLogMessage> history = logs.get(originalFile);
            final String path = originalPath;
            final String[] ret = new String[] {null};
            if(history != null) {
                HgProgressSupport support = new HgProgressSupport(NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUp"), null) { // NOI18N
                    @Override
                    protected void perform() {
                        ret[0] = getRepositoryPathIntern(history, revision, repository, originalFile, path, dryTry, this);
                    }
                };
                support.start(Mercurial.getInstance().getRequestProcessor(repository)).waitFinished();
            }
            if(ret[0] != null && !ret[0].equals(originalPath)) {
                return new File(repository, ret[0]);
            }
            return null;

        } finally { 
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " resolving historyFile for {0} took {1}", new Object[]{originalPath, System.currentTimeMillis() - t}); // NOI18N
            }
        }
    }

    private String getRepositoryPathIntern(List<HgLogMessage> history, String revision, File repository, File originalFile, final String path, boolean dryTry, HgProgressSupport support) {
        int count = 0;
        String historyPath = path;
        Iterator<HgLogMessage> it = history.iterator();
        while(it.hasNext() && !revision.equals(it.next().getHgRevision().getChangesetId())) {
            count++;
        }
        support.getProgressHandle().switchToDeterminate(count);
        
        // XXX try dry first, might be it will lead to the in in the revision
        for (int i = 0; i < history.size() && !support.isCanceled(); i ++) {
            HgLogMessage lm = history.get(i);
            String historyRevision = lm.getHgRevision().getChangesetId();
            if(historyRevision.equals(revision)) {
                break;
            }
            support.getProgressHandle().progress(NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUpAtRevision", originalFile.getName(), historyRevision), i); // NOI18N
            List<HgLogMessageChangedPath> changePaths = lm.getChangedPaths().length == 0 ? 
                    initializeChangePaths(repository, new DefaultChangePathCollector(
                    repository, OutputLogger.getLogger(repository), lm.getCSetShortID()), lm, dryTry)
                    : Arrays.asList(lm.getChangedPaths());
            if(changePaths != null) {
                for (HgLogMessageChangedPath cp : changePaths) {
                    String copy = cp.getCopySrcPath();
                    if(copy != null) {
                        if(historyPath.equals(cp.getPath())) {
                            historyPath = copy;
                            break;
                        }
                    }
                }
            }
        }
        // XXX check if found path exists in the revision we search for ...
        return support.isCanceled() ? path : historyPath;
    }

    public List<HgLogMessageChangedPath> initializeChangePaths (File repository, ChangePathCollector collector, HgLogMessage lm, boolean onlyCached) {
        assert lm.getChangedPaths().length == 0 : "Why refreshing already loaded change paths??? length=" + lm.getChangedPaths().length;
        String changesetId = lm.getCSetShortID();
        List<HgLogMessageChangedPath> changePaths = changesets.get(changesetId);
        if (changePaths == null) {
            long t1 = System.currentTimeMillis();
            boolean persist = true;
            if (!"false".equals(System.getProperty("versioning.mercurial.historycache.enable", "true"))) { //NOI18N
                LOG.log(Level.FINE, "loading changePaths from disk cache for {0}", new Object[] { changesetId }); //NOI18N
                persist = false;
                changePaths = loadCachedChangePaths(repository, lm.getCSetShortID());
                if (changePaths == null) {
                    persist = true;
                    LOG.log(Level.FINE, "loading changePaths from disk cache failed for {0}", new Object[] { changesetId }); //NOI18N
                }
            }
            if (changePaths == null && !onlyCached) {
                // not in mem cache and not stored on disk, let's call hg command
                LOG.log(Level.FINE, "loading changePaths via hg for {0}", new Object[] { changesetId }); //NOI18N
                HgLogMessageChangedPath[] cps = collector.getChangePaths();
                changePaths = Arrays.asList(cps == null ? new HgLogMessageChangedPath[0] : cps);
            }
            if (changePaths != null) {
                lm.refreshChangedPaths(changePaths.toArray(new HgLogMessageChangedPath[0]));
                changesets.put(changesetId, changePaths);
                if (persist && !changePaths.isEmpty() && !"false".equals(System.getProperty("versioning.mercurial.historycache.enable", "true"))) { //NOI18N
                    persistPaths(repository, changePaths, lm.getCSetShortID());
                }
            }
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " loading changePaths for {0} took {1}", new Object[] { changesetId, System.currentTimeMillis() - t1}); // NOI18N
            }
        } else {
            lm.refreshChangedPaths(changePaths.toArray(new HgLogMessageChangedPath[0]));
        }
        return changePaths;
    }

    private void persistPaths (File repository, List<HgLogMessageChangedPath> changePaths, String revision) {
        Storage storage = StorageManager.getInstance().getStorage(repository.getAbsolutePath());
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(content);
        try {
            dos.writeUTF(PERSISTED_DATA_VERSION);
            dos.writeInt(changePaths.size());
            for (HgLogMessageChangedPath path : changePaths) {
                dos.writeUTF(path.getPath());
                dos.writeChar(path.getAction());
                dos.writeUTF(path.getCopySrcPath() == null ? "" : path.getCopySrcPath());
            }
            dos.close();
            LOG.log(Level.FINE, "persisting changePaths to disk cache for {0}", new Object[] { revision }); //NOI18N
            storage.setRevisionInfo(revision, new ByteArrayInputStream(content.toByteArray()));
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot persist data", ex); //NOI18N
        }
    }

    private List<HgLogMessageChangedPath> loadCachedChangePaths (File repository, String revision) {
        Storage storage = StorageManager.getInstance().getStorage(repository.getAbsolutePath());
        byte[] buff = storage.getRevisionInfo(revision);
        if (buff == null) {
            return null;
        }
        List<HgLogMessageChangedPath> changePaths = null;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));
        try {
            String version = dis.readUTF();
            if (PERSISTED_DATA_VERSION.equals(version)) {
                int len = dis.readInt();
                changePaths = len == 0 ? null : new ArrayList<HgLogMessageChangedPath>(len); // do not care about empty paths, test for 0
                for (int i = 0; i < len; ++i) {
                    String path = dis.readUTF();
                    char action = dis.readChar();
                    String copyPath = dis.readUTF();
                    changePaths.add(new HgLogMessageChangedPath(path, copyPath.isEmpty() ? null : copyPath, action));
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "changePaths from disk cache corrupted {0}", new Object[] { revision }); //NOI18N
        }
        return changePaths;
    }
    
    public static interface ChangePathCollector {
        
        HgLogMessageChangedPath[] getChangePaths ();
        
    }
    
    public static final class DefaultChangePathCollector implements ChangePathCollector {

        private final File repositoryRoot;
        private final OutputLogger logger;
        private final String changesetId;

        public DefaultChangePathCollector (File repositoryRoot, OutputLogger logger, String changesetId) {
            this.repositoryRoot = repositoryRoot;
            this.logger = logger;
            this.changesetId = changesetId;
        }
        
        @Override
        public HgLogMessageChangedPath[] getChangePaths () {
            HgLogMessage[] messages = HgCommand.getLogMessages(repositoryRoot,
                    null, 
                    changesetId,
                    changesetId,
                    true,
                    true,
                    false,
                    1,
                    Collections.<String>emptyList(),
                    logger,
                    true);
            return messages == null || messages.length == 0 ? new HgLogMessageChangedPath[0] : messages[0].getChangedPaths();
        }
        
    }
    
    /**
     * Test purposes for now
     */
    List<HgLogMessageChangedPath> getCachedPaths (String revision) {
        return changesets.get(revision);
    }

    /**
     * Test purposes for now
     */
    void flushCached () {
        changesets.clear();
    }
}
