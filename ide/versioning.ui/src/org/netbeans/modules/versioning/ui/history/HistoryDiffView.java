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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.swing.*;

import org.netbeans.api.diff.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.diff.DiffViewModeSwitcher;
import org.netbeans.modules.versioning.ui.history.HistoryComponent.CompareMode;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryDiffView implements PropertyChangeListener {
           
    private final HistoryComponent tc;
    private DiffPanel panel;
    private Component diffComponent;
    private DiffController diffView;                
    private DiffTask diffTask;
    
    private final Object VIEW_LOCK = new Object();
    private DiffViewModeSwitcher diffViewModeSwitcher;
    
    /** Creates a new instance of HistoryDiffView */
    public HistoryDiffView(HistoryComponent tc) {
        this.tc = tc;
        panel = new DiffPanel();                                                              
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, "MSG_DiffPanel_NoVersion"));  // NOI18N                
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            tc.disableNavigationButtons();
            refresh(((Node[]) evt.getNewValue()));
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            synchronized(VIEW_LOCK) {
                if(diffView != null) {
                    tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
                }
            }
        } 
    }
      
    JPanel getPanel() {
        return panel;
    }    
    
    private void refresh(Node[] newSelection) {
        if(newSelection != null) {
            if (newSelection.length == 1) {
                HistoryEntry entry1 = newSelection[0].getLookup().lookup(HistoryEntry.class);
                if (entry1 != null) {
                    VCSFileProxy file1 = getFile(newSelection[0], entry1);
                    
                    CompareMode mode = tc.getMode();
                    switch(mode) {
                        case TOCURRENT:
                            refreshCurrentDiffPanel(entry1, file1);
                            return;

                        case TOPARENT:    
                            refreshRevisionDiffPanel(null, entry1, null, file1);
                            return;
                            
                        default:
                            throw new IllegalStateException("Wrong mode selected: " + mode); // NOI18N
                    }
                    
                }
                
            } else if (newSelection.length == 2) {
                HistoryEntry entry1 = newSelection[0].getLookup().lookup(HistoryEntry.class);
                VCSFileProxy file1 = null;
                if (entry1 != null) {
                    file1 = getFile(newSelection[0], entry1);
                }
                
                VCSFileProxy file2 = null;
                HistoryEntry entry2 = newSelection[1].getLookup().lookup(HistoryEntry.class);
                if (entry2 != null) {
                    file2 = file1 = getFile(newSelection[1], entry2);
                }
                
                if (entry1 != null && entry2 != null && file1 != null && file2 != null) {
                    if (entry1.getDateTime().getTime() > entry2.getDateTime().getTime()) {
                        refreshRevisionDiffPanel(entry1, entry2, file1, file2);
                    } else {
                        refreshRevisionDiffPanel(entry2, entry1, file2, file1);
                    }
                    return;
                }
            }
        } 
        
        String msgKey = (newSelection == null) || (newSelection.length == 0)
                        ? "MSG_DiffPanel_NoVersion"                     //NOI18N
                        : "MSG_DiffPanel_IllegalSelection";             //NOI18N
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, msgKey));
    }           
    
    private void refreshRevisionDiffPanel(HistoryEntry entry1, HistoryEntry entry2, VCSFileProxy file1, VCSFileProxy file2) { 
        onSelectionLastDifference = false;
        scheduleTask(new RevisionDiffPrepareTask(entry1, entry2, file1, file2, onSelectionLastDifference));
    } 
    
    private void refreshCurrentDiffPanel(HistoryEntry entry, VCSFileProxy file) {  
        onSelectionLastDifference = false;
        scheduleTask(new CurrentDiffPrepareTask(entry, file, onSelectionLastDifference));
    }        

    private void scheduleTask(DiffTask newTask) {          
        if(diffTask != null) {
            diffTask.cancel();
        }
        diffTask = newTask;
        diffTask.schedule();        
    }

    private VCSFileProxy getFile(Node node, HistoryEntry entry) {
        Collection<? extends VCSFileProxy> proxies = node.getLookup().lookupAll(VCSFileProxy.class);
        if(proxies != null && proxies.size() == 1) {
            return proxies.iterator().next();
        } else {
            VCSFileProxy[] files = entry.getFiles();
            // HACK ensure that for form files .java is returned as default
            if(files.length == 2) {
                if((files[0].getName().endsWith(".java") &&                     // NOI18N
                    files[1].getName().endsWith(".form")))                      // NOI18N
                {
                    return files[0];
                }
                if((files[1].getName().endsWith(".java") &&                     // NOI18N
                    files[0].getName().endsWith(".form")))                      // NOI18N
                {
                    return files[1];
                }
            } 
            return files[0];
        }
    }

    private boolean onSelectionLastDifference = false;
    void onSelectionLastDifference() {
        onSelectionLastDifference = true;
    }

    void componentClosed () {
        DiffViewModeSwitcher.release(this);
        diffViewModeSwitcher = null;
    }

    private class CurrentDiffPrepareTask extends DiffTask {
        
        private final HistoryEntry entry;
        private final VCSFileProxy file;
        private final boolean selectLast;

        public CurrentDiffPrepareTask(final HistoryEntry entry, VCSFileProxy file, boolean selectLast) {
            this.entry = entry;
            this.file = file;
            this.selectLast = selectLast;
        }

        @Override
        public void run() {
            
            History.LOG.log(
                Level.FINE, 
                "preparing current diff for: {0} - {1}", // NOI18N        
                new Object[]{entry, file}); 
            
            DiffController dv = getView(entry, file);
            if(isCancelled()) {
                return;
            }            
            if(dv != null) {
                History.LOG.log(Level.FINE, "setting cached diff view for {0} - {1}", new Object[]{entry.getRevision(), file});
                setDiffView(dv, selectLast);
                return;
            }
            
            File tmpFile;
            if(isCancelled()) {
                return;
            }   
            startPrepareProgress();
            FileObject tmpFo;
            try {
                File tempFolder = Utils.getTempFolder();
                tmpFile = new File(tempFolder, file.getName()); // XXX
                entry.getRevisionFile(file, VCSFileProxy.createFileProxy(tmpFile));
                tmpFo = FileUtil.toFileObject(tmpFile);
                History.LOG.log(Level.FINE, "retrieved revision file for {0} {1}", new Object[]{entry.getRevision(), file});
                if(isCancelled()) {
                    return;
                }                
            } finally {
                finishPrepareProgress();
            }
            String title1 = getTitle(entry, file);
            String title2;
            if(file.exists()) {
                title2 = "<html><b>" + NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_CurrentFile") + "</b></html>"; // NOI18N
            } else {
                title2 = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_FileDeleted"); // NOI18N
            }            
            
            dv = prepareDiffView(tmpFo, file.toFileObject(), true, file.exists(), title1, title2, true, selectLast);
            if(isCancelled()) {
                return;
            }            
            if(dv != null) {
                History.LOG.log(Level.FINE, "setting diff view for {0} - {1}", new Object[]{entry.getRevision(), file});
                setDiffView(dv, selectLast);
                putView(dv, entry, file);
            }
        }
    }        

    private Map<String, DiffController> views = new ConcurrentHashMap<String, DiffController>();
    private DiffController getView(HistoryEntry entry, VCSFileProxy file) {
        assert entry != null;
        if(entry == null) {
            return null;
        }
        return views.get(getKey(entry, file));
    }
    
    private DiffController getView(HistoryEntry entry1, VCSFileProxy file1, HistoryEntry entry2) {
        assert entry1 != null && entry2 != null;
        if(entry1 == null && entry2 == null) {
            return null;
        }
        return views.get(getKey(entry1, file1, entry2));
    }
    
    private void putView(DiffController dv, HistoryEntry entry1, VCSFileProxy file1, HistoryEntry entry2) {
        views.put(getKey(entry1, file1, entry2), dv);
    }
    
    private void putView(DiffController dv, HistoryEntry entry, VCSFileProxy file) {
        views.put(getKey(entry, file), dv);
    }

    private String getKey(HistoryEntry entry1, VCSFileProxy file1, HistoryEntry entry2) {
        return getKey(entry1, file1) + "_" +                                    // NOI18N
               entry2.getRevision() + "_" +                                     // NOI18N
               entry2.getDateTime().getTime();
    }

    private String getKey(HistoryEntry entry, VCSFileProxy file) {
        return entry.getRevision() + "_" +                                      // NOI18N
               entry.getDateTime().getTime() + "_" +
               file;
    }
    
    private class RevisionDiffPrepareTask extends DiffTask {
        
        private HistoryEntry entry1;
        private final HistoryEntry entry2;
        private VCSFileProxy file1;
        private final VCSFileProxy file2;
        private final boolean selectLast;

        public RevisionDiffPrepareTask(final HistoryEntry entry1, HistoryEntry entry2, VCSFileProxy file1, VCSFileProxy file2, boolean selectLast) {
            this.entry1 = entry1;
            this.entry2 = entry2;
            this.file1 = file1;
            this.file2 = file2;
            this.selectLast = selectLast;
        }

        @Override
        public void run() {
            
            History.LOG.log(
                Level.FINE, 
                "preparing previous diff for: {0} - {1} and {2} - {3}", // NOI18N        
                new Object[]{entry1, file1, entry2, file2}); 
            
            startPrepareProgress();

            FileObject revisionFo1;
            FileObject revisionFo2;
            try {
                if(entry1 == null && file1 == null) {
                    entry1 = entry2.getParent(file2);
                    if(entry1 == null) {
                        entry1 = tc.getParentEntry(entry2);
                        if(isCancelled()) {
                            return;
                        }
                    }
                    file1 = file2;
                    if (entry1 == null) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                showNoContent(NbBundle.getMessage(HistoryDiffView.class, "MSG_DiffPanel_NoVersionToCompare")); // NOI18N                                
                            }
                        });
                        return;
                    }                
                }
                DiffController dv = getView(entry2, file2, entry1);
                if(isCancelled()) {
                    return;
                }                
                if(dv != null) {
                    History.LOG.log(
                        Level.FINE, 
                        "setting cached diff view for: {0} - {1} and {2} - {3}", // NOI18N        
                        new Object[]{entry1, file1, entry2, file2}); 
                    setDiffView(dv, selectLast);
                    return;
                }
                
                revisionFo1 = getRevisionFile(entry1, file1);
                History.LOG.log(Level.FINE, "retrieved revision file for {0} - {1}", new Object[]{entry1.getRevision(), file1});
                if(isCancelled()) {
                    return;
                }                
                revisionFo2 = getRevisionFile(entry2, file2);
                History.LOG.log(Level.FINE, "retrieved revision file for {0} - {1}", new Object[]{entry2.getRevision(), file2});
                if(isCancelled()) {
                    return;
                }                
            } finally {
                finishPrepareProgress();
            }
            
            String title1 = getTitle(entry1, file1);
            String title2 = getTitle(entry2, file2);
            DiffController dv = prepareDiffView(revisionFo1, revisionFo2, true, true, title1, title2, false, selectLast);
            if(isCancelled()) {
                return;
            }            
            if(dv != null) {
                History.LOG.log(
                    Level.FINE, 
                    "setting diff view for: {0} - {1} and {2} - {3}", // NOI18N        
                    new Object[]{entry1, file1, entry2, file2}); 
                setDiffView(dv, selectLast);
                putView(dv, entry1, file1, entry2);
            }    
        }

        private FileObject getRevisionFile(HistoryEntry entry, VCSFileProxy file) {
            File tempFolder = Utils.getTempFolder();
            File revFile = new File(tempFolder, file.getName()); // XXX
            entry.getRevisionFile(file, VCSFileProxy.createFileProxy(revFile));
            return FileUtil.toFileObject(revFile);
        }

    }  
    
    private void setDiffView(final DiffController dv, final boolean selectLast) {
        final int diffCount = dv.getDifferenceCount();
        final int diffIdx = dv.getDifferenceIndex();
        synchronized(VIEW_LOCK) {
            diffView = dv;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {            
                History.LOG.finer("invoked set diff view"); // NOI18N
                getDiffViewModeSwitcher().setupMode(dv);
                JComponent c = dv.getJComponent();
                setDiffComponent(c);
                tc.setDiffView(c);

                // in case the diffview listener did not fire
                if(dv.getDifferenceCount() > 0) {
                    setCurrentDifference(selectLast ? diffCount - 1 : 0);
                } else {
                    tc.refreshNavigationButtons(diffIdx, diffCount);
                }

                panel.revalidate();
                panel.repaint();
            }
        });
    }    

    private DiffViewModeSwitcher getDiffViewModeSwitcher () {
        if (diffViewModeSwitcher == null) {
            diffViewModeSwitcher = DiffViewModeSwitcher.get(this);
        }
        return diffViewModeSwitcher;
    }

    private String getTitle(HistoryEntry entry, VCSFileProxy file) {
        String title1;
        if(file.exists()) {
            if(entry.isLocalHistory()) {
                title1 = "<html>" + file.getName() + " (<b>" + RevisionNode.getFormatedDate(entry) + "</b>)</html>"; // NOI18N
            } else {
                title1 = "<html>" + file.getName() + " (<b>" + entry.getRevisionShort() + "</b>)</html>"; // NOI18N
            } 
        } else {
            title1 = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_FileDeleted"); // NOI18N
        }
        return title1;
    }

    private DiffController prepareDiffView(final FileObject file1, final FileObject file2, boolean file1Exists, boolean file2Exists, final String title1, final String title2, final boolean editable, final boolean selectLast) {
        
        History.LOG.log(
                Level.FINE, 
                "preparing diff view for: {0} - {1} and {2} - {3}", // NOI18N        
                new Object[]{title1, file1, title2, file2}); // NOI18N
        
        StreamSource ss1;
        if(file1Exists) {
            ss1 = new LHStreamSource(file1, title1, getMimeType(file2), editable);
        } else {
            ss1 = StreamSource.createSource("currentfile", title1, getMimeType(file1), new StringReader("")); // NOI18N
        }

        StreamSource ss2;                        
        if(file2Exists) {
            ss2 = new LHStreamSource(file2, title2, getMimeType(file2), editable);
        } else {
            ss2 = StreamSource.createSource("currentfile", title2, getMimeType(file2), new StringReader("")); // NOI18N
        }

        final DiffController dv;
        try {   
            dv = DiffController.createEnhanced(ss1, ss2);
        } catch (IOException ioe)  {
            History.LOG.log(Level.SEVERE, null, ioe);
            return null;
        }                            
        dv.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
                    dv.removePropertyChangeListener(this);
                    int diffCount = dv.getDifferenceCount();
                    synchronized(VIEW_LOCK) {
                        // diffView may already be a completely different view
                        if (dv == diffView && diffCount > 0) {
                            setCurrentDifference(selectLast ? diffCount - 1 : 0);
                        }
                    }
                }
            }
        });
        dv.addPropertyChangeListener(HistoryDiffView.this);
        return dv;
    }
    
    private String getMimeType(FileObject file) {
        FileObject fo = file;
        if(fo != null) {
            return fo.getMIMEType();   
        } else {
            return "content/unknown"; // NOI18N
        }                
    }        

    private void showNoContent(String s) {
        setDiffComponent(new NoContentPanel(s));
    }

    private void setDiffComponent(Component component) {
        if(diffComponent != null) {
            panel.diffPanel.remove(diffComponent);     
            History.LOG.log(Level.FINEST, "replaced current diff component {0}", diffComponent); // NOI18N
        } 
        panel.diffPanel.add(component, BorderLayout.CENTER);
        diffComponent = component;   
        History.LOG.log(Level.FINEST, "added diff component {0}", diffComponent); // NOI18N
        panel.diffPanel.revalidate();
        panel.diffPanel.repaint();
    }       
    
    void onNextButton() {
        synchronized(VIEW_LOCK) {
            if(diffView == null) {
                return;
            }          
            int nextDiffernce = diffView.getDifferenceIndex() + 1;        
            if(nextDiffernce < diffView.getDifferenceCount()) {
                setCurrentDifference(nextDiffernce);    
            }                   
        }
    }

    void onPrevButton() {
        synchronized(VIEW_LOCK) {
            if(diffView == null) {
                return;
            }
            int prevDiffernce = diffView.getDifferenceIndex() - 1;
            if(prevDiffernce > -1) {
                setCurrentDifference(prevDiffernce);                
            }
        }
    }    
    
    void modeChanged() {
        refresh(tc.getSelectedNodes());        
    }
    
    private void setCurrentDifference(int idx) {
        synchronized(VIEW_LOCK) {
            if(diffView == null) {
                return;
            }
            diffView.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx);    
            tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
        }
    }
    
    private class LHStreamSource extends StreamSource {
        
        private final FileObject file;
        private final String title;
        private final String mimeType;
        private final boolean editable;

        public LHStreamSource(FileObject file, String title, String mimeType, boolean editable) {
            this.file = file;
            this.title = title;
            this.mimeType = mimeType;
            this.editable = editable;
        }
        @Override
        public boolean isEditable() {
            return editable && isPrimary(file);
        }
        
        private boolean isPrimary(FileObject fo) {            
            if (fo != null) {
                try {
                    DataObject dao = DataObject.find(fo);
                    return fo.equals(dao.getPrimaryFile());
                } catch (DataObjectNotFoundException e) {
                    // no dataobject, never mind
                }
            }
            return true;
        }
    
        @Override
        public Lookup getLookup() {
            if (file != null && isPrimary(file)) {
                return Lookups.fixed(file);                 
            } else {
                return Lookups.fixed(); 
            }
        }

        @Override
        public String getName() {
            return title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            if(file != null) {
                return new InputStreamReader(file.getInputStream());
            }
            return new StringReader(""); // NOI18N
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }

    private abstract class DiffTask implements Runnable, Cancellable {
        private Task task = null;
        private boolean cancelled = false;
        private PreparingDiffHandler preparingDiffPanel;
            
        void startPrepareProgress() {
            preparingDiffPanel = new PreparingDiffHandler();
            preparingDiffPanel.startPrepareProgress();
        }
        
        void finishPrepareProgress() {
            preparingDiffPanel.finishPrepareProgress();
        }
        
        @Override
        public synchronized boolean cancel() {
            cancelled = true;
            if(preparingDiffPanel != null) {
                preparingDiffPanel.finishPrepareProgress();
            }
            if(task != null) {
                task.cancel();
            }
            History.LOG.finer("cancelling DiffTask"); // NOI18N
            return true;
        }

        synchronized void schedule() {          
            task = History.getInstance().getRequestProcessor().create(this);
            task.schedule(500);        
        }
        
        protected synchronized boolean isCancelled() {
            if(cancelled) {
                History.LOG.finer("DiffTask is cancelled"); // NOI18N
            }
            return cancelled;
        }
        
        private class PreparingDiffHandler extends JPanel implements ActionListener {
            private JLabel label = new JLabel();
            private Component progressComponent;
            private ProgressHandle handle;
            
            private final Timer timer = new Timer(0, this);
            private final Object TIMER_LOCK = new Object();
            
            public PreparingDiffHandler() {
                label.setText(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff")); // NOI18N
                this.setBackground(UIManager.getColor("TextArea.background")); // NOI18N

                setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                add(label, c);
                label.setEnabled(false);
                timer.setRepeats(false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(isCancelled()) {
                    return;
                }
                synchronized(TIMER_LOCK) {
                    handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff")); // NOI18N
                    setProgressComponent(ProgressHandleFactory.createProgressComponent(handle));
                    handle.start();
                    handle.switchToIndeterminate();   
                    setDiffComponent(PreparingDiffHandler.this);
                }
            }        

            void startPrepareProgress() {
                History.LOG.fine("starting prepare diff handler"); // NOI18N
                synchronized(TIMER_LOCK) {
                    timer.start();
                }
            }

            void finishPrepareProgress() {
                History.LOG.fine("finishing prepare diff handler"); // NOI18N
                synchronized(TIMER_LOCK) {
                    timer.stop();
                    if(handle != null) {
                        handle.finish();
                    }
                }
            }

            private void setProgressComponent(Component component) {
                if(progressComponent != null) remove(progressComponent);
                if(component != null) {
                    this.progressComponent = component;
                    GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
                    add(component, gridBagConstraints);
                } 
            }
        }        
    }
}
