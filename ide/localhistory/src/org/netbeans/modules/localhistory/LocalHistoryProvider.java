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
package org.netbeans.modules.localhistory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.*;
import org.netbeans.modules.versioning.history.HistoryActionVCSProxyBased;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.LifecycleManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryProvider implements VCSHistoryProvider, VersioningListener {

    private final List<HistoryChangeListener> listeners = new LinkedList<HistoryChangeListener>();
    private Action[] actions;
    public LocalHistoryProvider() {
        LocalHistory.getInstance().getLocalHistoryStore().addVersioningListener(this);
    }
    
    @Override
    public void addHistoryChangeListener(HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeHistoryChangeListener(HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public HistoryEntry[] getHistory(VCSFileProxy[] files, Date fromDate) {
        if(files == null || files.length == 0) {
            LocalHistory.LOG.log(Level.FINE, "LocalHistory requested for no files {0}", files != null ? files.length : null);
            return new HistoryEntry[0];
        }
        logFiles(files);
        
        Map<Long, HistoryEntry> storeEntries = new HashMap<Long, HistoryEntry>();
        for (VCSFileProxy f : files) {
            StoreEntry[] ses = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntries(f);
            for(StoreEntry se : ses) {
                if(!storeEntries.containsKey(se.getTimestamp())) {
                    HistoryEntry e = 
                        new HistoryEntry(
                            files, 
                            se.getDate(), 
                            se.getLabel(), 
                            "",                                                             // username         NOI18N
                            "",                                                             // username short   NOI18N 
                            NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision         NOI18N
                            NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision short   NOI18N
                            getActions(), 
                            new RevisionProviderImpl(se),
                            new MessageEditImpl(se));
                    storeEntries.put(se.getTimestamp(), e);
                }
            }
            
        }
        logEntries(storeEntries.values());
        return storeEntries.values().toArray(new HistoryEntry[storeEntries.size()]);
    }
    
    @Override
    public Action createShowHistoryAction(VCSFileProxy[] proxies) {
        return null;
    }
    
    public void fireHistoryChange(VCSFileProxy proxy) {
        HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[0]);
        }
        for (HistoryChangeListener l : la) {
            l.fireHistoryChanged(new HistoryEvent(this, new VCSFileProxy[] {proxy}));
        }
    }

    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new RevertFileAction(),
                new DeleteAction()
            };
        }
        return actions;
    }

    @Override
    public void versioningEvent(VersioningEvent event) {
        Object[] params = event.getParams();
        if(params[0] != null) {
            fireHistoryChange((VCSFileProxy)params[0]);
        }
    }
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        private final StoreEntry se;

        public RevisionProviderImpl(StoreEntry se) {
            this.se = se;
        }
        
        @Override
        public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
            assert originalFile != null;
            if(originalFile == null) {
                LocalHistory.LOG.log(Level.FINE, "revision {0} requested for null file", se.getDate().getTime()); // NOI18N
                return;
            }
            LocalHistory.LOG.log(Level.FINE, "revision {0} requested for file {1}", new Object[]{se.getDate().getTime(), FileUtils.getPath(originalFile)}); // NOI18N
            try {
                // we won't use the member store entry as that might have been 
                // set for e.g. a stored .form while this is the according .java
                // file beeing requested. In case the storage can't find a revision it 
                // returns the next nearest in time 
                long ts = se.getTimestamp();
                StoreEntry storeEntry = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntry(originalFile, ts);
                if(storeEntry != null) {
                    FileUtils.copy(storeEntry.getStoreFileInputStream(), revisionFile.toFile()); 
                } else {
                    LocalHistory.LOG.log(Level.WARNING, "No entry in Local History for file {0} {1} {2}", new Object[]{originalFile, new Date(ts), ts}); // NOI18N
                }
            } catch (IOException e) {
                LocalHistory.LOG.log(Level.WARNING, "Error while retrieving history for file {0} stored as {1}", new Object[]{se.getFile(), se.getStoreFile()}); // NOI18N
            }
        }
    }
    
    private class MessageEditImpl implements VCSHistoryProvider.MessageEditProvider {
        private final StoreEntry se;
        public MessageEditImpl(StoreEntry se) {
            this.se = se;
        }
        @Override
        public void setMessage(String message) throws IOException {
            LocalHistory.getInstance().getLocalHistoryStore().setLabel(se.getFile(), se.getTimestamp(), message);
        }
    }

    private void logFiles(VCSFileProxy[] files) {
        if(LocalHistory.LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("LocalHistory requested for files: "); // NOI18N
            sb.append(toString(files));
            LocalHistory.LOG.fine(sb.toString());
        }
    }

    private void logEntries(Collection<HistoryEntry> entries) {
        LocalHistory.LOG.log(Level.FINE, "LocalHistory returns {0} entries", entries.size()); // NOI18N
        if(LocalHistory.LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            Iterator<HistoryEntry> it = entries.iterator();
            while(it.hasNext()) {
                HistoryEntry entry = it.next();
                sb.append("["); // NOI18N
                sb.append(DateFormat.getDateTimeInstance().format(entry.getDateTime()));
                sb.append(",["); // NOI18N
                sb.append(toString(entry.getFiles()));
                sb.append("]]"); // NOI18N
                if(it.hasNext()) sb.append(","); // NOI18N
            }
            LocalHistory.LOG.finest(sb.toString());
        }
    }    
    
    private String toString(VCSFileProxy[] files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i] != null ? FileUtils.getPath(files[i]) : "null"); // NOI18N
            if(i < files.length -1 ) sb.append(","); // NOI18N
        }
        return sb.toString();
    }
    
    private class RevertFileAction extends LHAction {
        
        @Override
        void perform(StoreEntry se) {
            org.netbeans.modules.localhistory.utils.Utils.revert(se);
        }   

        @Override
        protected void perform(final HistoryEntry entry, final Set<VCSFileProxy> files) {
            // XXX try to save files in invocation context only
            // list somehow modified file in the context and save
            // just them.
            // The same (global save) logic is in CVS, no complaint
            LifecycleManager.getDefault().saveAll();  
            
            super.perform(entry, files);
        }

        @Override
        protected boolean isMultipleHistory() {
            return false;
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.localhistory.ui.view.RevertFileAction"); // NOI18N
        }    

        @Override
        public String getName() {
            return NbBundle.getMessage(LocalHistoryProvider.class, "LBL_RevertFileAction"); // NOI18N
        }
    }
    
    private class DeleteAction extends LHAction {

        @Override
        void perform(StoreEntry se) {
            LocalHistory.getInstance().getLocalHistoryStore().deleteEntry(se.getFile(), se.getTimestamp());
        }    
    
        @Override
        public String getName() {
            return NbBundle.getMessage(LocalHistoryProvider.class, "LBL_DeleteAction"); // NOI18N
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.localhistory.ui.view.DeleteAction"); // NOI18N
        }

    }    

    private abstract class LHAction extends HistoryActionVCSProxyBased {
        @Override
        protected void perform(final HistoryEntry entry, final Set<VCSFileProxy> files) {
            LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
                @Override
                public void run() { 
                    for (VCSFileProxy file : files) {
                        StoreEntry se = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntry(file, entry.getDateTime().getTime());
                        if(se != null) {
                            perform(se);
                        }
                    }   
                }
            });
        }
        abstract void perform(StoreEntry se);
    }

}    
