/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;


import java.awt.EventQueue;
import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import org.netbeans.modules.openide.loaders.DataNodeUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildren extends Children.Keys<FolderChildrenPair>
implements PropertyChangeListener, ChangeListener, FileChangeListener {
    /** the folder */
    private FolderList folder;
    /** filter of objects */
    private final DataFilter filter;
    /** listener on changes in nodes */
    private PropertyChangeListener listener;
    /** file change listener */
    private FileChangeListener fcListener;
    /** change listener */
    private ChangeListener changeListener;
    /** logging, if needed */
    @SuppressWarnings("NonConstantLogger")
    private final Logger err;
    /** last refresh task */
    private volatile Collection<FolderChildrenPair> pairs;
    private volatile Task refTask = Task.EMPTY;
    private static final boolean DELAYED_CREATION_ENABLED;
    static {
        DELAYED_CREATION_ENABLED = !"false".equals( // NOI18N
            System.getProperty("org.openide.loaders.FolderChildren.delayedCreation") // NOI18N
        );
    }

    /**
    * @param f folder to display content of
    * @param map map to use for holding of children
    */
    public FolderChildren (DataFolder f) {
        this (f, DataFilter.ALL);
    }

    /**
    * @param f folder to display content of
    * @param filter filter of objects
    */
    @SuppressWarnings("LeakingThisInConstructor")
    public FolderChildren(DataFolder f, DataFilter filter) {
        super(true);
        String log;
        if (f.getPrimaryFile().isRoot()) {
            log = "org.openide.loaders.FolderChildren"; // NOI18N
        } else {
            log = "org.openide.loaders.FolderChildren." + f.getPrimaryFile().getPath().replace('/', '.'); // NOI18N
        }
        err = Logger.getLogger(log);
        this.folder = FolderList.find(f.getPrimaryFile(), true);
        this.filter = filter;
        this.listener = org.openide.util.WeakListeners.propertyChange(this, folder);
        this.fcListener = org.openide.filesystems.FileUtil.weakFileChangeListener(this, folder.getPrimaryFile());
    }

    /** used from DataFolder */
    DataFilter getFilter () {
        return filter;
    }
    
    void applyKeys(Collection<FolderChildrenPair> pairs) {
        setKeys(pairs);
        this.pairs = pairs;
    }

    /** If the folder changed its children we change our nodes.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent ev) {
        err.log(Level.FINE, "Got a change {0}", ev.getPropertyName());
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // Filtering changed need to recompute children
        Object source = e.getSource();
        FileObject fo = null;
        if (source instanceof DataObject) {
            DataObject dobj = (DataObject) source;
            fo = dobj.getPrimaryFile();
        } else if (source instanceof FileObject) {
            fo = (FileObject) source;
        }
        boolean doRefresh;
        if (fo != null) {
            FileObject folderFO = folder.getPrimaryFile();
            if (!fo.isFolder()) {
                doRefresh = (fo.getParent() == folderFO);
            } else {
                doRefresh = (fo == folderFO);
            }
        } else {
            doRefresh = true;
        }
        if (doRefresh) {
            refreshChildren(RefreshMode.DEEP);
        }
    }

    private enum RefreshMode {SHALLOW, SHALLOW_IMMEDIATE, DEEP, DEEP_LATER, CLEAR}
    private void refreshChildren(RefreshMode operation) {
        class R implements Runnable {
            List<FolderChildrenPair> positioned = null;
            RefreshMode op;
            Task prevTask = null;
            @Override
            public void run() {
                if (prevTask != null) {
                    // We need to ensure that refresh tasks for one
                    // FolderChildren do not run in parallel. And because the
                    // tasks can be processed by different request processors if
                    // the folder is moved to another filesystem, we have to
                    // wait for previously posted task here.
                    prevTask.waitFinished();
                    prevTask = null;
                }
                if (op == RefreshMode.DEEP) {
                    positioned = getPositionedFolderChildrenPairs(); //#229746
                    op = RefreshMode.DEEP_LATER;
                    MUTEX.postWriteRequest(this);
                    return;
                }
                err.log(Level.FINE, "refreshChildren {0}", op);

                try {
                    if (op == RefreshMode.CLEAR) {
                        applyKeys(Collections.<FolderChildrenPair>emptyList());
                    } else if (op == RefreshMode.DEEP_LATER) {
                        assert positioned != null : "positioned not prepared"; //NOI18N
                        applyKeys(Collections.<FolderChildrenPair>emptyList());
                        applyKeys(positioned);
                    } else if (op == RefreshMode.SHALLOW) {
                        applyKeys(getPositionedFolderChildrenPairs());
                    } else {
                        throw new IllegalStateException("Unknown op: " + op);  // NOI18N
                    }
                } finally {
                    err.log(Level.FINE, "refreshChildren {0}, done", op);
                }
            }

            private List<FolderChildrenPair> getPositionedFolderChildrenPairs() {
                final FileObject[] arr = folder.getPrimaryFile().getChildren();
                FolderOrder order = FolderOrder.findFor(folder.getPrimaryFile());
                Arrays.sort(arr, order);
                List<FolderChildrenPair> list
                        = new ArrayList<FolderChildrenPair>(arr.length);
                for (FileObject fo : FileUtil.getOrder(Arrays.asList(arr),
                        false)) {
                    if (filter instanceof DataFilter.FileBased) {
                        DataFilter.FileBased f = (DataFilter.FileBased) filter;
                        if (!f.acceptFileObject(fo)) {
                            continue;
                        }
                    }
                    list.add(new FolderChildrenPair(fo));
                }
                return list;
            }
        }
        R run = new R();
        if (operation == RefreshMode.SHALLOW_IMMEDIATE) {
            refTask.waitFinished();
            run.op = RefreshMode.SHALLOW;
            run.run();
        } else {
            run.op = operation;
            synchronized (this) {
                run.prevTask = refTask;
                refTask = DataNodeUtils.reqProcessor(folder.getPrimaryFile()).post(run);
            }
        }
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    @Override
    protected Node[] createNodes(FolderChildrenPair pair) {
        boolean delayCreation = 
            DELAYED_CREATION_ENABLED && 
            EventQueue.isDispatchThread() &&
            !pair.primaryFile.isFolder();
        Node ret;
        if (delayCreation) {
            ret = new DelayedNode(pair);
        } else {
            ret = createNode(pair);
        }
        return ret == null ? null : new Node[] { ret };
    }
    
    final Node createNode(FolderChildrenPair pair) {
        DataObject obj;
        long time = System.currentTimeMillis();
        Node ret = null;
        try {
            FileObject pf = pair.primaryFile;
            obj = DataObject.find (pf);
            if (
                obj.isValid() &&
                pf.equals(obj.getPrimaryFile()) &&
                (filter == null || filter.acceptDataObject (obj))
            ) {
                ret = obj.getClonedNodeDelegate (filter);
                if (!obj.isValid()) {
                    // #153008 - DataObject became invalid meanwhile
                    ret = null;
                }
            } 
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(FolderChildren.class.getName()).log(Level.FINE, null, e);
        } finally {
            long took = System.currentTimeMillis() - time;
            if (err.isLoggable(Level.FINE)) {
                err.log(Level.FINE, "createNodes: {0} took: {1} ms", new Object[]{pair, took});
                err.log(Level.FINE, "  returning: {0}", ret);
            }
        }
        return ret;
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        Node[] arr;
        Level previous = null;
        final int limit = 1000;
        for (int round = 0; ; round++) {
            if (optimalResult) {
                waitOptimalResult();
            }
            arr = getNodes();
            boolean stop = true;
            for (Node n : arr) {
                if (n instanceof DelayedNode) {
                    DelayedNode dn = (DelayedNode)n;
                    if (checkChildrenMutex() && dn.waitFinished()) {
                        err.log(Level.FINE, "Waiting for delayed node {0}", dn);
                        stop = false;
                        if (round > 600) {
                            err.log(Level.WARNING, "Scheduling additional refresh for {0}", dn);
                            dn.scheduleRefresh("fallback"); // NOI18N
                        }
                    }
                }
            }
            if (stop) {
                break;
            }
            if (round == 500) {
                err.warning("getNodes takes ages, turning on logging");
                previous = err.getLevel();
                err.setLevel(Level.FINE);
            }
            if (round == limit) {
                err.warning(threadDump());
                err.setLevel(previous);
                boolean thrw = false;
                assert thrw = true;
                if (thrw) {
                    throw new IllegalStateException("Too many repetitions in getNodes(true). Giving up.");
                }
                break;
            }
        }
        if (previous != null) {
            err.setLevel(previous);
        }
        return arr;
    }
    
    private static void appendThread(StringBuffer sb, String indent, Thread t, java.util.Map<Thread,StackTraceElement[]> data) {
        sb.append(indent).append("Thread ").append(t.getName()).append('\n');
        StackTraceElement[] stack = data.get(t);
        if (stack != null) {
        for (StackTraceElement e : stack) {
            sb.append("\tat ").append(e.getClassName()).append('.').append(e.getMethodName())
                    .append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(")\n");
        }
        }
    }
    
    private static void appendGroup(StringBuffer sb, String indent, ThreadGroup tg, java.util.Map<Thread,StackTraceElement[]> data) {
        sb.append(indent).append("Group ").append(tg.getName()).append('\n');
        indent = indent.concat("  ");

        int groups = tg.activeGroupCount();
        ThreadGroup[] chg = new ThreadGroup[groups];
        tg.enumerate(chg, false);
        for (ThreadGroup inner : chg) {
            if (inner != null) {
                appendGroup(sb, indent, inner, data);
            }
        }

        int threads = tg.activeCount();
        Thread[] cht= new Thread[threads];
        tg.enumerate(cht, false);
        for (Thread t : cht) {
            if (t != null) {
                appendThread(sb, indent, t, data);
            }
        }
    }
    
    private static String threadDump() {
        java.util.Map<Thread,StackTraceElement[]> all = Thread.getAllStackTraces();
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }

        StringBuffer sb = new StringBuffer();
        appendGroup(sb, "", root, all);
        return sb.toString();
    }
    

    @Override
    public Node findChild(String name) {
        if (checkChildrenMutex()) {
            waitOptimalResult();
        }
        int i = 0;
        final Collection<FolderChildrenPair> tmp = pairs;
        if (tmp != null) {
            for (FolderChildrenPair p : tmp) {
                final FileObject pf = p.primaryFile;
                if (pf.getNameExt().startsWith(name)) {
                    try {
                        Node original = DataObject.find(pf).getNodeDelegate();
                        if (!original.getName().equals(name)) {
                            continue;
                        }
                        Node candidate = getNodeAt(i);
                        if (candidate != null && candidate.getName().equals(name)) {
                            return candidate;
                        }
                    } catch (DataObjectNotFoundException ex) {
                        err.log(Level.INFO, "Can't find object for " + pf, ex);
                    }
                }
                i++;
            }
        }
        return super.findChild(name);
    }

    private void waitOptimalResult() {
        if (checkChildrenMutex()) {
            err.fine("waitOptimalResult"); // NOI18N
            if (!isInitialized()) {
                refreshChildren(RefreshMode.SHALLOW);
            }
            folder.waitProcessingFinished();
            refTask.waitFinished();
            err.fine("waitOptimalResult: waitProcessingFinished"); // NOI18N
        } else {
            Logger.getLogger(FolderChildren.class.getName()).log(Level.WARNING, null,
                    new java.lang.IllegalStateException("getNodes(true) called while holding the Children.MUTEX"));
        }
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        if (optimalResult) {
            waitOptimalResult();
        }
        return getNodesCount();
    }

    /**
     * @return true if it is safe to wait (our thread is
     *         not in Children.MUTEX.readAccess
     */
    static boolean checkChildrenMutex() {
        return !Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess ();
    }

    /** Initializes the children.
    */
    @Override
    protected void addNotify () {
        err.fine("addNotify begin");
        // add as a listener for changes on nodes
        folder.addPropertyChangeListener(listener);
        folder.getPrimaryFile().addFileChangeListener(fcListener);
        // add listener to the filter
        if ( filter instanceof ChangeableDataFilter ) {
            ChangeableDataFilter chF = (ChangeableDataFilter)filter;
            changeListener = WeakListeners.change(this, chF);
            chF.addChangeListener( changeListener );
        }
        // #159628, #189979: do not block EQ loading this folder's children.
        refreshChildren(RefreshMode.SHALLOW);
        err.fine("addNotify end");
    }

    /** Deinitializes the children.
    */
    @Override
    protected void removeNotify () {
        err.fine("removeNotify begin");
        // removes the listeners
        folder.getPrimaryFile().removeFileChangeListener(fcListener);
        folder.removePropertyChangeListener(listener);
        // remove listener from filter
        if ( filter instanceof ChangeableDataFilter ) {
            ((ChangeableDataFilter)filter).removeChangeListener( changeListener );
            changeListener = null;
        }

        // we need to clear the children now
        List<FolderChildrenPair> emptyList = Collections.emptyList();
        applyKeys(emptyList);
        err.fine("removeNotify end");
    }

    /** Display name */
    @Override
    public String toString () {
        return (folder != null) ? folder.getPrimaryFile ().toString () : super.toString();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (DataObject.EA_ASSIGNED_LOADER.equals(fe.getName())) {
            // make sure this event is processed by the data system
            DataObjectPool.checkAttributeChanged(fe);
            refreshKey(new FolderChildrenPair(fe.getFile()));
            refreshChildren(RefreshMode.SHALLOW_IMMEDIATE);
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
         refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }
    
    private final class DelayedNode extends FilterNode implements Runnable {
        final FolderChildrenPair pair;
        /** @GuardedBy("this") */
        private RequestProcessor.Task task;

        public DelayedNode(FolderChildrenPair pair) {
            this(pair, new DelayedLkp(new InstanceContent()));
        }
        
        private DelayedNode(FolderChildrenPair pair, DelayedLkp lkp) {
            this(pair, new AbstractNode(Children.LEAF, lkp));
            lkp.ic.add(pair.primaryFile);
            lkp.node = this;
        }
        
        private DelayedNode(FolderChildrenPair pair, AbstractNode an) {
            super(an);
            this.pair = pair;
            an.setName(pair.primaryFile.getNameExt());
            an.setIconBaseWithExtension("org/openide/loaders/unknown.gif"); // NOI18N
            scheduleRefresh("constructor"); // NOI18N
        }
        
        @Override
        public void run() {
            Node n = createNode(pair);
            if (n != null) {
                changeOriginal(n, !n.isLeaf());
            } else {
                refreshKey(pair);
            }
            synchronized (this) {
                task = null;
            }
            err.log(Level.FINE, "delayed node refreshed {0} original: {1}", new Object[]{this, n});
        }
        
        /* @return true if there was some change in the node while waiting */
        public final boolean waitFinished() {
            RequestProcessor.Task t;
            synchronized (this) {
                t = task;
                if (t == null) {
                    return false;
                }
            }
            err.log(Level.FINE, "original before wait: {0}", getOriginal());
            t.waitFinished();
            err.log(Level.FINE, "original after wait: {0}", getOriginal());
            err.log(Level.FINE, "task after waitFinished {0}", task);
            return true;
        }

        final synchronized void scheduleRefresh(String by) {
            task = DataNodeUtils.reqProcessor(pair.primaryFile).post(this);
            err.log(Level.FINE, "Task initialized by {0} to {1} for {2}", new Object[] { by, task, this });
        }
    }
    
    private final class DelayedLkp extends AbstractLookup {
        DelayedNode node;
        final InstanceContent ic;
        
        public DelayedLkp(InstanceContent content) {
            super(content);
            ic = content;
        }
        
        @Override
        protected void beforeLookup(Template<?> template) {
            Class<?> type = template.getType();
            if (DataObject.class.isAssignableFrom(type)) {
                final DataObject obj = convert(node);
                if (obj != null) {
                    ic.add(obj);
                }
            }
        }
        
        public DataObject convert(DelayedNode obj) {
            final FolderChildrenPair pair = obj.pair;
            if (EventQueue.isDispatchThread()) {
                err.log(Level.WARNING, "Attempt to obtain DataObject for {0} from EDT", pair.primaryFile);
                boolean assertsOn = false;
                assert assertsOn = true;
                if (assertsOn) {
                    err.log(Level.INFO, "Ineffective since #199391 was implemented", new Exception("Find for " + pair.primaryFile));
                }
            }
            try {
                return DataObject.find(pair.primaryFile);
            } catch (DataObjectNotFoundException ex) {
                err.log(Level.INFO, "Cannot convert " + pair.primaryFile, ex);
                return null;
            }
        }
    }
}
