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
package org.netbeans.modules.versioning.core;

import java.awt.EventQueue;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.awt.Mnemonics;

import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationEvent;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningAnnotationProvider {
    
    private static final VersioningAnnotationProvider instance = new VersioningAnnotationProvider();
    
    private static final Logger LOG = Logger.getLogger(VersioningAnnotationProvider.class.getName());
    private static final int CACHE_INITIAL_SIZE = 500;
    private static final long CACHE_ITEM_MAX_AGE = getMaxAge();
    private static final boolean ANNOTATOR_ASYNC = !"false".equals(System.getProperty("versioning.asyncAnnotator", "true")); //NOI18N
    private static final Image EMPTY_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private VersioningAnnotationProvider() {
    }
    
    public static VersioningAnnotationProvider getDefault() {
        return instance;
    }
    
    private VersioningSystem getOwner(VCSFileProxy file, Boolean isFile) {
        return file == null ? null : VersioningManager.getInstance().getOwner(file, isFile);
    }

    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        Image annotatedIcon;
        if (ANNOTATOR_ASYNC) {
            // at first annotate the empty icon and cache this merge icon.
            // otherwise the cached value would be the final merged icon and there might be problems when acquiring the cached value
            // in another annotate round - if the caller decides to annotate a different icon than the one earlier
            annotatedIcon = iconCache.getValue(iconCache.new ItemKey<Image, String>(files, "", files instanceof NonRecursiveFolder, EMPTY_ICON)); //NOI18N
            // and finally merge the cached value with the original icon
            annotatedIcon = annotatedIcon == null ? icon : ImageUtilities.mergeImages(icon, annotatedIcon, 0, 0);
        } else {
            // fallback to the old implementation
            annotatedIcon = iconCache.getValue(iconCache.new ItemKey<Image, String>(files, "", files instanceof NonRecursiveFolder, icon)); //NOI18N
        }
        return annotatedIcon;
    }

    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        String annotatedName = labelCache.getValue(labelCache.new ItemKey<String, String>(files, name, files instanceof NonRecursiveFolder, name));
        return annotatedName == null ? htmlEncode(name) : annotatedName;
    }

    public Action[] actions(Set files) {
        if (files.isEmpty()) return new Action[0];

        if(!VersioningManager.isInitialized()) {
            return new Action[] {
                SystemAction.get(InitVersioningSystemAction.class),
                SystemAction.get(InitLHSystemAction.class)
            };
        }
        
        List<Action> actions = new ArrayList<Action>();
        LocalHistoryActions localHistoryAction = null;

        // group all given files by owner
        Map<VersioningSystem, List<VCSFileProxy>> owners = new HashMap<VersioningSystem, java.util.List<VCSFileProxy>>(3);
        for (FileObject fo : (Set<FileObject>) files) {
            VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
            if (file != null) {

                // check if there is at least ine file managed by local hisotry
                VersioningSystem localHistory = VersioningManager.getInstance().getLocalHistory(file, !fo.isFolder());
                if(localHistoryAction == null && localHistory != null && localHistory.getVCSAnnotator() != null) {
                    localHistoryAction = SystemAction.get(LocalHistoryActions.class);
                    localHistoryAction.setVersioningSystem(localHistory);
                    actions.add(localHistoryAction);
                }

                VersioningSystem owner = getOwner(file, !fo.isFolder());
                if(owner != null) {
                    List<VCSFileProxy> fileList = owners.get(owner);
                    if(fileList == null) {
                        fileList = new ArrayList<VCSFileProxy>();
                    }
                    fileList.add(file);
                    owners.put(owner, fileList);
                }
            }
        }

        VersioningSystem vs = null;
        if(owners.keySet().size() == 1) {
            vs = owners.keySet().iterator().next();
        } else {
            return actions.toArray(new Action [0]);
        } 
        
        VCSAnnotator an = null;
        if (vs != null) {
            an = vs.getVCSAnnotator();
        }
        if (an != null) {
            VersioningSystemActions action = SystemAction.get(VersioningSystemActions.class);
            action.setVersioningSystem(vs);
            actions.add(action);
        }

        return actions.toArray(new Action [0]);
    }
    
    public static class VersioningSystemActions extends AbstractVersioningSystemActions {               
    }

    public static class LocalHistoryActions extends AbstractVersioningSystemActions {
    }
    
    public static class InitLHSystemAction extends InitVersioningSystemAction {
        public InitLHSystemAction() {
            super();
        }
        @Override
        public String getName() {
            return NbBundle.getMessage(VersioningAnnotationProvider.class, "CTL_MenuItem_LocalHistory");
        }
    }
    
    public static class InitVersioningSystemAction extends SystemAction implements Presenter.Popup {

        @Override
        public void actionPerformed(ActionEvent ae) { }
        @Override
        public JMenuItem getPopupPresenter() {
            return NoVCSMenuItem.createInitializingMenu(getName());            
        }
        @Override
        public String getName() {
            return NbBundle.getMessage(VersioningAnnotationProvider.class, "CTL_MenuItem_VersioningMenu");
        }
        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    public abstract static class AbstractVersioningSystemActions extends SystemAction implements ContextAwareAction {
        
        private VersioningSystem system;

        @Override
        public String getName() {
            return system.getDisplayName();
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(system.getClass());
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            // this item does nothing, this is not a real action
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new RealVersioningSystemActions(system, actionContext);
        }

        public void setVersioningSystem(VersioningSystem system) {
            this.system = system;
        }
    }
    
    private static class RealVersioningSystemActions extends AbstractAction implements Presenter.Popup {

        private final VersioningSystem system;
        private final Lookup lkp;

        public RealVersioningSystemActions(VersioningSystem system, Lookup lkp) {
            super(system.getDisplayName());
            this.system = system;
            this.lkp = lkp;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // this item does also nothing, it displays a popup ;)
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return new VersioningSystemMenuItem();
        }
        
        @NbBundle.Messages("LBL_PopupMenu_Initializing=Initializing...")
        private class VersioningSystemMenuItem extends JMenu {
        
            private boolean popupContructed;

            public VersioningSystemMenuItem() {
                Mnemonics.setLocalizedText(this, Utils.getSystemMenuName(system));
            }

            @Override
            public void setSelected(boolean selected) {
                if (selected && popupContructed == false) {
                    popupContructed = true;
                    JMenuItem item = new JMenuItem(Bundle.LBL_PopupMenu_Initializing());
                    item.setEnabled(false);
                    add(item);
                    Utils.postParallel(new Runnable() {
                        @Override
                        public void run () {
                            VCSContext context = Utils.contextForLookup(lkp);
                            final Action [] actions = system.getVCSAnnotator().getActions(context, VCSAnnotator.ActionDestination.PopupMenu);
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run () {
                                    JPopupMenu popup = getPopupMenu();
                                    boolean display = popup.isVisible();
                                    popup.setVisible(false);
                                    removeAll();
                                    if (isShowing()) {
                                        for (int i = 0; i < actions.length; i++) {
                                            Action action = actions[i];
                                            if (action == null) {
                                                addSeparator();
                                            } else {
                                                JMenuItem item = Utils.toMenuItem(action);
                                                add(item);
                                            }
                                        }
                                        popup.setVisible(display);
                                    }
                                }
                            });
                        }
                    });
                }
                super.setSelected(selected);
            }
        }
    }

    static void refreshAllAnnotations() {
        if (instance != null) {
            instance.refreshAnnotations(null);
        }
    }
                   
    /**
     * Refreshes annotations for all given files and all parent folders of those files.
     *
     * @param filesToRefresh files to refresh
     */
    void refreshAnnotations(Set<VCSFileProxy> files) {
        refreshAnnotations(files, true);
    }

    void refreshAnnotations(Set<VCSFileProxy> files, boolean removeFromCache) {
        if (files == null) {            
            LOG.log(Level.FINE, "refreshing all annotations"); //NOI18N
            refreshAllAnnotationsTask.schedule(2000);
            return;
        }
        
        if (removeFromCache) {
            LOG.log(Level.FINE, "refreshing annotations for {0}", files); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "refreshing annotations called from:", new Exception()); //NOI18N
            }
        }
        
        for (VCSFileProxy file : files) {
            // try to limit the number of normalizeFile calls:
            // let's find the closest existent FO, then list it's parents with FileObject.getParent();
            FileObject fo = file.toFileObject();
            if (fo == null) {
                fo = getExistingParent(file);
            } else {
                // file exists, plan it to refresh
                addToMap(filesToRefresh, fo, removeFromCache);
                fo = fo.getParent();
            }

            if (removeFromCache) {
                // fo is the closest existing parent
                for (FileObject parent = fo; parent != null; parent = parent.getParent()) {
                    // plan parent to refresh
                    addToMap(parentsToRefresh, parent, removeFromCache);
                }
            }
        }
        
        fireFileStatusChangedTask.schedule(2000);
    }
    
    /**
     * Stores all files which have to be refreshed 
     */
    private final Map<FileSystem, Set<FileObject>> filesToRefresh = new HashMap<FileSystem, Set<FileObject>>();
    
    /**
     * Stores all parents from files which have to be refreshed 
     */
    private final Map<FileSystem, Set<FileObject>> parentsToRefresh = new HashMap<FileSystem, Set<FileObject>>();        
    
    private RequestProcessor rp = new RequestProcessor("Versioning fire FileStatusChanged", 1, true);
    
    /**
     * Refreshes all annotations and clears the maps holding all files and their parents which have to be refreshed
     */
    private RequestProcessor.Task refreshAllAnnotationsTask = rp.create(new Runnable() {        
        @Override
        public void run() {            
            clearMap(filesToRefresh);
            clearMap(parentsToRefresh);
            labelCache.removeAll();
            iconCache.removeAll();
            
            VersioningManager.deliverStatusEvent(new VCSAnnotationEvent(true, true));
        }
    });    
    
    /**
     * Refreshes all files stored in filesToRefresh and parentsToRefresh
     */ 
    private RequestProcessor.Task fireFileStatusChangedTask = rp.create(new Runnable() {        
        @Override
        public void run() {
            
            // createInitializingMenu and fire for all files which have to be refreshed
            List<VCSAnnotationEvent> fileEvents = new ArrayList<VCSAnnotationEvent>(); 
            List<VCSAnnotationEvent> folderEvents = new ArrayList<VCSAnnotationEvent>(); 

            synchronized(filesToRefresh) {
                if (filesToRefresh.isEmpty()) {
                    return;
                }
                for (Map.Entry<FileSystem, Set<FileObject>> e : filesToRefresh.entrySet()) {
                    Set<FileObject> files = new HashSet<FileObject>();
                    Set<FileObject> folders = new HashSet<FileObject>();
                    Set<FileObject> set = e.getValue();
                    for(FileObject fo : set) {
                        if(fo.isFolder()) {
                            folders.add(fo);
                        } else {
                            files.add(fo);
                        }
                    }        
                    set.clear();
                    e.setValue(new HashSet<FileObject>());
                    if(files.size() > 0) {
                        fileEvents.add(new VCSAnnotationEvent(files, true, true));
                    }
                    if(folders.size() > 0) {
                        folderEvents.add(new VCSAnnotationEvent(folders, true,  true));
                    }
                }        
            }    

            fireFileStatusEvents(fileEvents);
            fireFileStatusEvents(folderEvents);

            // createInitializingMenu and fire events for all parent from each file which has to be refreshed
            List<VCSAnnotationEvent> parentEvents = new ArrayList<VCSAnnotationEvent>(); 
            synchronized(parentsToRefresh) {
                for (Map.Entry<FileSystem, Set<FileObject>> e : parentsToRefresh.entrySet()) {
                    Set<FileObject> set = e.getValue();
                    Set<FileObject> files = new HashSet<FileObject>(set);
                    parentEvents.add(new VCSAnnotationEvent(files, true, false));                                        
                    e.setValue(new HashSet<FileObject>()); 
                    set.clear();                    
                }                                
            }       
            fireFileStatusEvents(parentEvents);            
        }    
        
        private void fireFileStatusEvents(Collection<VCSAnnotationEvent> events) {
            for(VCSAnnotationEvent event : events) {
                VersioningManager.deliverStatusEvent(event);
            }
        }          
    });    
    
    private void clearMap(Map<FileSystem, Set<FileObject>> map)  {
        synchronized(map) {
            if(map.size() > 0) {                
                map.clear();
            }
        }                    
    }
    
    private void addToMap(Map<FileSystem, Set<FileObject>> map, FileObject fo, boolean removeFromCache) {
        if(fo == null) {
            return;
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException e) {
            // ignore files in invalid filesystems
            return;
        }        
        synchronized (map) {                        
            Set<FileObject> set = map.get(fs);
            if(set == null) {
                set = new HashSet<FileObject>();
                map.put(fs, set);
            }
            set.add(fo);
            if (removeFromCache) {
                if (LOG.isLoggable(Level.FINER)) {
                    // TODO: remove after fix
                    LOG.log(Level.FINER, "addToMap(): removing from cache {0}", new Object[] {fo}); //NOI18N
                }
                labelCache.removeAllFor(fo);
                iconCache.removeAllFor(fo);
            }
        }
    }

    /**
     * Finds and return the closest existing ancestor FO for the given file
     * @param file file to get an ancestor for
     * @return an ancestor fileobject or null if no such exist
     */
    private FileObject getExistingParent (VCSFileProxy file) {
        FileObject fo = null;
        for (VCSFileProxy parent = file; parent != null && fo == null; parent = parent.getParentFile()) {
            // find the fileobject
            fo = parent.toFileObject();
        }
        return fo;
    }

    private final Cache<Image, String> iconCache = new Cache<Image, String>(Cache.ANNOTATION_TYPE_ICON);
    private final Cache<String, String> labelCache = new Cache<String, String>(Cache.ANNOTATION_TYPE_LABEL);

    /**
     * Keeps cached annotations
     */
    private class Cache<T, KEY> {
        private static final String ANNOTATION_TYPE_ICON = "IconCache"; //NOI18N
        private static final String ANNOTATION_TYPE_LABEL = "LabelCache"; //NOI18N

        private final Object writeLock = new Object();
        private final Object LOCK_VALUES = new Object();
        private int peekCount;
        private LinkedHashMap<ItemKey<T, KEY>, Item<T>> cachedValues = new LinkedHashMap<ItemKey<T, KEY>, Item<T>>(CACHE_INITIAL_SIZE);
        private WeakHashMap<FileObject, Set<ItemKey<T, KEY>>> index = new WeakHashMap<FileObject, Set<ItemKey <T, KEY>>>(CACHE_INITIAL_SIZE);
        private final LinkedHashSet<ItemKey<T, KEY>> filesToAnnotate;
        private final RequestProcessor.Task annotationRefreshTask;
        private final String type;
        private HashSet<FileObject> refreshedFiles = new HashSet<FileObject>();
        private boolean allCleared;

        Cache(String type) {
            this.annotationRefreshTask = new RequestProcessor("VersioningAnnotator.annotationRefresh", 1, false, false).create(new AnnotationRefreshTask()); //NOI18N
            this.filesToAnnotate = new LinkedHashSet<ItemKey<T, KEY>>();
            assert ANNOTATION_TYPE_ICON.equals(type) || ANNOTATION_TYPE_LABEL.equals(type);
            this.type = type;
        }

        /**
         * Immediately returns cached value, which can be null, and starts a background call to the annotator which owns the set of files
         * @param files set of files to annotate
         * @param initialValue initial value to annotate
         * @return cached value for files or null
         */
        T getValue (ItemKey<T, KEY> key) {
            if (!ANNOTATOR_ASYNC) {
                return annotate(key.getInitialValue(), key.getFiles());
            }
            T cachedValue;
            boolean itemCached = false;
            synchronized (LOCK_VALUES) {
                Item<T> cachedItem = cachedValues.get(key);
                cachedValue = cachedItem == null ? null : cachedItem.getValue();
                if (cachedValue != null || cachedValues.containsKey(key)) {
                    itemCached = true;
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0}.getValue() cached: {1} for {2}", //NOI18N
                        new Object[] {type, cachedValue, key.getFiles()});
            }
            if (!itemCached) {
                // not cached yet, plan the annotation
                addFilesToAnnotate(key);
            }
            return cachedValue;
        }

        /**
         * Stores the new value in the cache. Also checks if an event shall be fired to refresh files' annotations.
         * @param files files for which the value will be kept
         * @param value cached value
         * @return true if the event should be fired - it means the old cached value differs from the new one
         */
        private boolean setValue (ItemKey<T, KEY> key, T value) {
            synchronized (writeLock) {
                if (allCleared) {
                    return false;
                }
                Set<? extends FileObject> files = key.getFiles();
                for (FileObject fo : files) {
                    if (refreshedFiles.contains(fo)) {
                        return false;
                    }
                }
                if (LOG.isLoggable(Level.FINEST)) {
                    // TODO: remove after fix
                    LOG.log(Level.FINEST, "{0}.setValue(): inserting for {1}:{2}", new Object[]{type, key.getFiles(), value}); //NOI18N
                }
                synchronized (LOCK_VALUES) {
                    cachedValues.put(key, new Item(value));
                    peekCount = Math.max(peekCount, cachedValues.size() + 1);
                }
                // reference to the key must be added to index for every file in the set
                // so the key can be quickly found when refresh annotations event comes
                for (FileObject fo : files) {
                    Set<ItemKey<T, KEY>> sets = index.get(fo);
                    if (sets == null) {
                        sets = new HashSet<ItemKey<T, KEY>>();
                        index.put(fo, sets);
                    }
                    sets.add(key);
                }
                removeOldValues();
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0}.setValue(): {1} for {2}", new Object[] {type, value, key}); //NOI18N
            }
            return true;
        }

        private void removeOldValues () {
            assert Thread.holdsLock(writeLock);
            for (Iterator<Map.Entry<ItemKey<T, KEY>, Item<T>>> it = cachedValues.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<ItemKey<T, KEY>, Item<T>> e = it.next();
                if (!e.getValue().isValid()) {
                    if (LOG.isLoggable(Level.FINER)) {
                        // TODO: remove after fix
                        LOG.log(Level.FINER, "{0}.removeOldValues(): {1}", new Object[]{type, e.getKey().getFiles()}); //NOI18N
                    }
                    removeFromIndex(e.getKey());
                    synchronized (LOCK_VALUES) {
                        it.remove();
                    }
                } else {
                    // do not search on, next entries are newer
                    break;
                }
            }
            shrinkMaps();
        }

        /**
         * Plan annotation scan for these files.
         * @param files
         * @param initialValue
         */
        private void addFilesToAnnotate (ItemKey<T, KEY> key) {
            boolean start;
            synchronized (filesToAnnotate) {
                start = filesToAnnotate.add(key);
            }
            if (start) {
                annotationRefreshTask.schedule(0);
            }
        }

        private ItemKey<T, KEY> getNextFilesToAnnotate () {
            ItemKey<T, KEY> retval = null;
            synchronized (filesToAnnotate) {
                Iterator<ItemKey<T, KEY>> it = filesToAnnotate.iterator();
                if (it.hasNext()) {
                    retval = it.next();
                    it.remove();
                }
            }
            return retval;
        }

        private T annotate(VCSAnnotator annotator, T initialValue, VCSContext context) {
            if (ANNOTATION_TYPE_LABEL.equals(type)) {
                return (T) annotator.annotateName((String) initialValue, context);
            } else if (ANNOTATION_TYPE_ICON.equals(type)) {
                return (T) annotator.annotateIcon((Image) initialValue, context);
            } else {
                LOG.log(Level.WARNING, "{0}.annotate unsupported", type); //NOI18N
                assert false;
                return null;
            }
        }
        
        private T annotate (T initialValue, Set<? extends FileObject> files) {
            long ft = System.currentTimeMillis();
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0}.annotate for {1}", new Object[] {type, files}); //NOI18N
            }
            VCSAnnotator an = null;

            try {
                if (files.isEmpty()) {
                    return initialValue;
                }
                VersioningSystem vs = getCommonOwner(files);

                if (vs == null) {
                    return null;
                }
                an = vs.getVCSAnnotator();
                if (an == null) {
                    return null;
                }

                VCSContext context = Utils.contextForFileObjects(files);
                return annotate(an, initialValue, context);
            } finally {
                if (LOG.isLoggable(Level.FINEST)) {
                    long t = System.currentTimeMillis();
                    if (an != null) {
                        LOG.log(Level.FINEST, "{0}.annotate in {1} returns in " + (t - ft) + " millis", //NOI18N
                                new Object[] {type, an.getClass().getName()});
                    } else {
                        LOG.log(Level.FINEST, "{0}.annotate returns in " + (t - ft) + " millis", //NOI18N
                                new Object[] {type});
                    }
                }
            }
        }

        private void removeAllFor (FileObject fo) {
            synchronized (writeLock) {
                refreshedFiles.add(fo);
                LOG.log(Level.FINER, "{0}.removeAllFor(): {1}", new Object[] {type, fo.getPath()}); //NOI18N
                Set<ItemKey<T, KEY>> keys = index.get(fo);
                if (keys != null) {
                    for (ItemKey<T, KEY> key : keys) {
                        if (LOG.isLoggable(Level.FINER)) {
                            // TODO: remove after fix
                            LOG.log(Level.FINER, "{0}.removeAllFor(): remove from cache: {1}", new Object[]{type, key.getFiles()}); //NOI18N
                        }
                        synchronized (LOCK_VALUES) {
                            cachedValues.remove(key);
                        }
                    }
                    ItemKey<T, KEY>[] keysArray = keys.toArray(new ItemKey[0]);
                    for (ItemKey<T, KEY> key : keysArray) {
                        if (LOG.isLoggable(Level.FINEST)) {
                            // TODO: remove after fix
                            LOG.log(Level.FINEST, "{0}.removeAllFor(): remove from index: {1}", new Object[]{type, key.getFiles()}); //NOI18N
                        }
                        removeFromIndex(key);
                    }
                }
                shrinkMaps();
            }
        }

        private void removeFromIndex (ItemKey<T, KEY> key) {
            assert Thread.holdsLock(writeLock);
            Set<? extends FileObject> files = key.getFiles();
            // remove all references for every file in the key from the index
            for (FileObject fo : files) {
                Set<ItemKey<T, KEY>> sets = index.get(fo);
                if (sets != null) {
                    sets.remove(key);
                    if (sets.isEmpty()) {
                        // remove the whole entry
                        index.remove(fo);
                    }
                }
            }
        }

        private void removeAll() {
            synchronized (writeLock) {
                allCleared = true;
                synchronized (LOCK_VALUES) {
                    cachedValues.clear();
                }
                index.clear();
                shrinkMaps();
            }
        }

        private void shrinkMaps () {
            assert Thread.holdsLock(writeLock);
            if (peekCount > CACHE_INITIAL_SIZE && peekCount > cachedValues.size() * 4) {
                LOG.log(Level.FINER, "{0}.shrinkMaps(): last peek was {1}", new Object[] { type, peekCount }); //NOI18N
                synchronized (LOCK_VALUES) {
                    cachedValues = new LinkedHashMap<ItemKey<T, KEY>, Item<T>>(cachedValues);
                    index = new WeakHashMap<FileObject, Set<ItemKey<T, KEY>>>(index);
                    peekCount = cachedValues.size();
                }
            }
        }

        private VersioningSystem getCommonOwner (Set<? extends FileObject> files) {
            VersioningSystem vs = null;
            for (FileObject fo : files) {
                VersioningSystem vcs = getOwner(VCSFileProxy.createFileProxy(fo), !fo.isFolder());
                if (vs == null) {
                    vs = vcs;
                } else if (vcs != null && vs != vcs) {
                    // we do not support annotate for different owners
                    vs = null;
                    break;
                }
            }
            return vs;
        }

        private class AnnotationRefreshTask implements Runnable {
            @Override
            public void run() {
                ItemKey<T, KEY> refreshCandidate;
                while ((refreshCandidate = getNextFilesToAnnotate()) != null) {
                    T initialValue = refreshCandidate.getInitialValue();
                    Set<? extends FileObject> files = refreshCandidate.getFiles();
                    assert files != null;
                    clearEvents();
                    T newValue = annotate(initialValue, files);
                    boolean isNonRecursive = files instanceof NonRecursiveFolder;
                    files = new HashSet<FileObject>(files);
                    boolean fireEvent = setValue(new ItemKey<T, KEY>(files, refreshCandidate.keyPart, isNonRecursive, initialValue), newValue);
                    if (fireEvent) {
                        Set<VCSFileProxy> filesToRefresh = new HashSet<VCSFileProxy>(files.size());
                        for (FileObject fo : files) {
                            VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
                            if(proxy != null) {
                                filesToRefresh.add(proxy);
                            }
                        }
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "{0}.AnnotationRefreshTask.run(): firing refresh event for {1}", //NOI18N
                                    new Object[] {type, filesToRefresh});
                        }
                        refreshAnnotations(filesToRefresh, false);
                    }
                }
            }
        }

        private void clearEvents() {
            synchronized (writeLock) {
                refreshedFiles = new HashSet<FileObject>();
                allCleared = false;
            }
        }

        private class Item<T> {
            private final T value;
            private final long timeStamp;

            public Item(T value) {
                this.value = value;
                this.timeStamp = System.currentTimeMillis();
            }

            public T getValue () {
                return value;
            }

            public boolean isValid () {
                return CACHE_ITEM_MAX_AGE == -1 || System.currentTimeMillis() - timeStamp < CACHE_ITEM_MAX_AGE;
            }
        }

        private class ItemKey<T, KEY> {
            private final T initialValue;
            private final KEY keyPart;
            private final Set<? extends FileObject> files;
            private Integer hashCode;
            private final boolean nonRecursiveFolders;

            public ItemKey(Set<? extends FileObject> files, KEY keyPart, boolean nonRecursiveFolders, T initialValue) {
                assert keyPart != null;
                this.initialValue = initialValue;
                this.keyPart = keyPart;
                this.files = files;
                this.nonRecursiveFolders = nonRecursiveFolders;
            }

            public T getInitialValue () {
                return initialValue;
            }

            public Set<? extends FileObject> getFiles () {
                return files;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof ItemKey) {
                    ItemKey other = (ItemKey) obj;
                    return nonRecursiveFolders == other.nonRecursiveFolders
                            && files.equals(other.files) && (keyPart.equals(other.keyPart));
                }
                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                // hashCode should allways return the same value during the lifetime of the instance
                if (hashCode == null) {
                    int hash = 5;
                    hash = 29 * hash + (this.keyPart != null ? this.keyPart.hashCode() : 0);
                    hash = 29 * hash + (this.files != null ? this.files.hashCode() : 0);
                    if (hashCode == null) {
                        hashCode = hash;
                    }
                    hashCode = hash;
                }
                return hashCode;
            }

            @Override
            public String toString() {
                return files.toString() + ": " + keyPart.toString() + "(" + (initialValue == null ? "null" : initialValue.toString()) + ")"; //NOI18N
            }
        }
    }

    private static long getMaxAge () {
        String cacheItemAgeProp = System.getProperty("versioning.annotator.cacheItem.maxAge", "600000"); //NOI18N - 10 minutes is the default value
        long cacheItemAge = 0;
        try {
            if (cacheItemAgeProp != null) {
                cacheItemAge = Long.parseLong(cacheItemAgeProp);
            }
        } catch (NumberFormatException ex) {
            LOG.log(Level.INFO, "Max cache item age: " + cacheItemAgeProp, ex); //NOI18N
            cacheItemAge = 0;
        }
        if (cacheItemAge != -1 && cacheItemAge < 60000) { // 1 minute is the minimal value
            cacheItemAge = 10 * 60 * 1000; // 10 minutes as default
        }
        LOG.log(Level.FINE, "getMaxAge(): {0}", cacheItemAge); //NOI18N
        return cacheItemAge;
    }

    private static final java.util.regex.Pattern lessThan = java.util.regex.Pattern.compile("<"); //NOI18N
    private String htmlEncode (String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;");               //NOI18N
    }
}
