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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.BaseUtilities;

/**
 * @author Radek Matous
 */
public final class FileObjectFactory {
    public static final Map<File, FileObjectFactory> AllFactories = new HashMap<File, FileObjectFactory>();
    public static boolean WARNINGS = true;
    //@GuardedBy("allIBaseLock")
    final Map<Integer, Object> allIBaseFileObjects = new WeakHashMap<Integer, Object>();
    final ReadWriteLock allIBaseLock = new ReentrantReadWriteLock();
    private BaseFileObj root;
    private static final Logger LOG_REFRESH = Logger.getLogger("org.netbeans.modules.masterfs.REFRESH"); // NOI18N

    public static enum Caller {
        ToFileObject, GetFileObject, GetChildern, GetParent, Refresh, Others;

        boolean asynchFire() {
            if (this == Refresh || this == Others) {
                return false;
            } else {
                return true;
            }
        }
    }
        
    private FileObjectFactory(final File rootFile) {
        this(new FileInfo(rootFile));
    }

    private FileObjectFactory(final FileInfo fInfo) {
        this(fInfo, null);
    }
    
    private FileObjectFactory(FileInfo fInfo, Object msg) {
        final BaseFileObj realRoot = create(fInfo);
        if (realRoot == null) { // #252580
            Logger.getLogger(FileObjectFactory.class.getName()).log(
                    Level.SEVERE, "No fo for " + fInfo + " queried for " + msg,
                    new NullPointerException());
        }
        assert realRoot != null : "No fo for " + fInfo + " queried for " + msg;
        root = realRoot;
    }
    
    public static FileObjectFactory getInstance(final File file) {
        return getInstance(file, true);
    }

    public static FileObjectFactory getInstance(final File file, boolean addMising) {
        FileObjectFactory retVal = null;
        final FileInfo rootInfo = new FileInfo(file).getRoot();
        final File rootFile = rootInfo.getFile();

        synchronized (FileObjectFactory.AllFactories) {
            retVal = FileObjectFactory.AllFactories.get(rootFile);
        }
        if (retVal == null && addMising) {
            if (rootInfo.isConvertibleToFileObject()) {
                synchronized (FileObjectFactory.AllFactories) {
                    retVal = FileObjectFactory.AllFactories.get(rootFile);
                    if (retVal == null) {
                        retVal = new FileObjectFactory(new FileInfo(rootFile), file);
                        FileObjectFactory.AllFactories.put(rootFile, retVal);
                    }
                }
            }
        }
        return retVal;
    }

    public static Collection<FileObjectFactory> getInstances() {
        synchronized (FileObjectFactory.AllFactories) {
            return new ArrayList<FileObjectFactory>(AllFactories.values());
        }
    }
    

    public final BaseFileObj getRoot() {
        return root;
    }

    public static int getFactoriesSize() {
        synchronized (FileObjectFactory.AllFactories) {
            return AllFactories.size();
        }
    }

    private List<FileObject> existingFileObjects() {
        List<Object> list = new ArrayList<Object>();
        allIBaseLock.readLock().lock();
        try {
            list.addAll(allIBaseFileObjects.values());
        } finally {
            allIBaseLock.readLock().unlock();
        }
        List<FileObject> res = new ArrayList<FileObject>();
        for (Object obj : list) {
            Collection<?> all;
            if (obj instanceof Reference<?>) {
                all = Collections.singleton(obj);
            } else {
                all = (List<?>)obj;
            }
            
            for (Object r : all) {
                Reference<?> ref = (Reference<?>)r;
                Object fo = ref == null ? null : ref.get();
                if (fo instanceof FileObject) {
                    res.add((FileObject)fo);
                }
            }
        }
        return res;
    }
    
    public int getSize() {
        List<FileObject> real = existingFileObjects();
        return real.size();
    }
    
    
    public BaseFileObj getFileObject(FileInfo fInfo, Caller caller, boolean onlyExisting) {
        File file = fInfo.getFile();
        FolderObj parent = BaseFileObj.getExistingParentFor(file, this);
        FileNaming child = null;
        boolean isInitializedCache = true;
        if (parent != null) {
            final ChildrenCache childrenCache = parent.getChildrenCache();
            final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();
            Runnable[] task = new Runnable[1];
            for (int i = 0; i < 2; i++) {
                if (i == 1) {
                    if (task[0] != null) {
                        task[0].run(); // some computation off the lock needed
                    } else {
                        break;
                    }
                }
                mutexPrivileged.enterReadAccess();
                try {
                    final String nameExt = BaseFileObj.getNameExt(file);
                    isInitializedCache = childrenCache.isCacheInitialized();
                    child = childrenCache.getChild(nameExt, false, task);
                } finally {
                    mutexPrivileged.exitReadAccess();
                }
            }
        }
        int initTouch = (isInitializedCache) ? -1 : (child != null ? 1 : 0);        
        if (initTouch == -1  && FileBasedFileSystem.isModificationInProgress()) {
            initTouch = file.exists() ? 1 : 0;
        }
        return issueIfExist(file, caller, parent, child, initTouch, caller.asynchFire(), onlyExisting);
    }


    /** 
     * Check cache consistence. It is called only from assertion, so it can be disabled
     * from command line. Given file should or shouldn't exist according to given 
     * expectedExists parameter. If the state is different than expected, it prints
     * warning to console. It can happen when some module doesn't use filesystems API
     * to handle files but directly uses java.io.File.
     * @param expectedExists whether given file is expected to exist
     * @param file checked file
     * @param caller caller of this method
     * @return always true.
     */
    private boolean checkCacheState(boolean expectedExists, File file, Caller caller) {
        if (!expectedExists && (caller == Caller.GetParent || caller == Caller.ToFileObject)) {
            return true;
        }
        if (isWarningEnabled() && caller != null && caller != Caller.GetChildern) {
            boolean realExists = file.exists();
            boolean notsame = expectedExists != realExists;
            if (notsame) {
                if (!realExists) {
                    //test for broken symlinks
                    File p = file.getParentFile();
                    if (p != null) {
                        File[] children = p.listFiles();
                        if (children != null && Arrays.asList(children).contains(file)) {
                            return true;
                        }
                    }                    
                }
                printWarning(file);
            }
        }
        return true;
    }

    private void printWarning(File file) {
        StringBuilder sb = new StringBuilder("WARNING(please REPORT):  Externally ");
        sb.append(file.exists() ? "created " : "deleted "); //NOI18N
        sb.append(file.isDirectory() ? "folder: " : "file: "); //NOI18N
        sb.append(file.getAbsolutePath());
        sb.append(" (Possibly not refreshed FileObjects when external command finished.");//NOI18N
        sb.append(" For additional information see: http://wiki.netbeans.org/wiki/view/FileSystems)");//NOI18N
        IllegalStateException ise = new IllegalStateException(sb.toString());
        // set to INFO level to be notified about possible problems.
        Logger.getLogger(getClass().getName()).log(Level.FINE, ise.getMessage(), ise);
    }

    private BaseFileObj issueIfExist(File file, Caller caller, final FileObject parent, FileNaming child, int initTouch, boolean asyncFire, boolean onlyExisting) {
        boolean exist = false;
        BaseFileObj foForFile = null;
        Integer realExists = initTouch;
        final FileChangedManager fcb = FileChangedManager.getInstance();

        //use cached info as much as possible + do refresh if something is wrong
        //exist = (parent != null) ? child != null : (((foForFile = get(file)) != null && foForFile.isValid()) || touchExists(file, realExists));
        foForFile = getCachedOnly(file);
        if (caller == Caller.Refresh && foForFile != null && !foForFile.isValid()) {
            // clear the impeach state, we know the file exists
            fcb.impeachExistence(file, true);
            foForFile = null;
        }
        if (parent != null && parent.isValid()) {
            if (child != null) {
                if (foForFile == null) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            refreshFromGetter(parent,asyncFire);
                        }
                    }
                    assert checkCacheState(true, file, caller);
                } else if (foForFile.isValid()) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            refreshFromGetter(parent,asyncFire);
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                } else {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist  && !Files.isSymbolicLink(file.toPath())) {
                        refreshFromGetter(parent,asyncFire);
                    }
                }
            } else {
                if (foForFile == null) {
                    exist = (realExists == -1) ? false : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        if (exist != touchExists(file, realExists)) {
                            exist = !exist;
                            refreshFromGetter(parent, asyncFire);
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                } else if (foForFile.isValid()) {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist) {
                        refreshFromGetter(foForFile,asyncFire);
                    }
                } else {
                    exist = touchExists(file, realExists);
                    if (exist) {
                        refreshFromGetter(parent,asyncFire);
                    }
                }
            }
        } else {
            if (foForFile == null) {
                if (caller == Caller.GetParent) {
                    exist = true;
                } else {
                    exist = touchExists(file, realExists);
                }
            } else if (foForFile.isValid()) {
                if (parent == null) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            refreshFromGetter(foForFile,asyncFire);
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                } else {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist) {
                        refreshFromGetter(foForFile,asyncFire);
                    }
                }
            } else {
                exist = (realExists == -1) ? false : touchExists(file, realExists);
                if (fcb.impeachExistence(file, exist)) {
                    exist = touchExists(file, realExists);
                }
                assert checkCacheState(exist, file, caller);
            }
        }
        if (!exist) {
            switch (caller) {
                case GetParent:
                    //guarantee issuing parent
                    BaseFileObj retval = null;
                    if (foForFile != null && !foForFile.isRoot()) {
                        retval = foForFile;
                    } else {
                        retval = getOrCreate(new FileInfo(file, 1));
                    }
                    if (retval instanceof BaseFileObj && retval.isValid()) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            //parent is exception must be issued even if not valid
                            invalidateSubtree(retval, false, false);
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                    return retval;
                case ToFileObject:
                    //guarantee issuing for existing file
                    exist = touchExists(file, realExists);
                    if (exist && parent != null && parent.isValid()) {
                        refreshFromGetter(parent,asyncFire);
                    }
                    assert checkCacheState(exist, file, caller);
                    break;
            }
        }
        //ratio 59993/507 (means 507 touches for 59993 calls)
        if (onlyExisting) {
            return (exist) ? getOrCreate(new FileInfo(file, 1)) : null;
        } else {
            BaseFileObj ret = getOrCreate(new FileInfo(file, 1));
            if (!exist) {
                ret.setValid(false);
            }
            return ret;
        }
    }

    private static boolean touchExists(File f, Integer state) {
        if (state == -1) {
            state = FileChangedManager.getInstance().exists(f) ? 1 : 0;
        }
        assert state != -1;
        return (state == 1) ? true : false;
    }

    private BaseFileObj getOrCreate(final FileInfo fInfo) {
        BaseFileObj retVal = null;
        File f = fInfo.getFile();

        boolean issue45485 = fInfo.isWindows() && f.getName().endsWith(".") && !f.getName().matches("[.]{1,2}");//NOI18N
        if (issue45485) {
            File f2 = FileUtil.normalizeFile(f);
            issue45485 = !f2.getName().endsWith(".");
            if (issue45485) {
                return null;
            }
        }
        allIBaseLock.writeLock().lock();
        try {
            retVal = this.getCachedOnly(f);
            if (retVal == null || !retVal.isValid()) {
                final File parent = f.getParentFile();
                if (parent != null) {
                    retVal = this.create(fInfo);
                } else {
                    retVal = this.getRoot();
                }

            }
            return retVal;
        } finally {
            allIBaseLock.writeLock().unlock();
        }
    }

    void invalidateSubtree(BaseFileObj root, boolean fire, boolean expected) {
        List<BaseFileObj> notify = fire ? new ArrayList<BaseFileObj>() : Collections.<BaseFileObj>emptyList();
        allIBaseLock.writeLock().lock();
        try {
            for (FileNaming fn : NamingFactory.findSubTree(root.getFileName())) {
                BaseFileObj cached = getCachedOnly(fn.getFile());
                if (cached != null && cached.isValid()) {
                    cached.invalidateFO(false, expected, false);
                    if (fire) {
                        notify.add(cached);
                    }
                }
            }
        } finally {
            allIBaseLock.writeLock().unlock();
        }
        for (BaseFileObj fo : notify) {
            fo.notifyDeleted(expected);
        }
    }
    
    public final String dumpObjects() {
        StringBuilder sb = new StringBuilder();
        for (FileObject fileObject : existingFileObjects()) {
            sb.append(fileObject).append("\n");
        }
        return sb.toString();
    }

    
    private BaseFileObj create(final FileInfo fInfo) {
        if (!fInfo.isConvertibleToFileObject()) {
            return null;
        }

        final File file = fInfo.getFile();
        FileNaming name = fInfo.getFileNaming();
        name = (name == null) ? NamingFactory.fromFile(file) : name;

        if (name == null) {
            return null;
        }

        if (name.isFile() && !name.isDirectory()) {
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (!name.isFile() && name.isDirectory()) {
            final FolderObj realRoot = new FolderObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }

    final void refreshAll(RefreshSlow slow, boolean ignoreRecursiveListeners, boolean expected) {
        Set<BaseFileObj> all2Refresh = collectForRefresh(ignoreRecursiveListeners);
        refresh(all2Refresh, slow, expected);
    }

    private Set<BaseFileObj> collectForRefresh(boolean noRecListeners) {
        final Set<BaseFileObj> all2Refresh;
        allIBaseLock.writeLock().lock();
        try {
            all2Refresh = Collections.newSetFromMap(new WeakHashMap<>(allIBaseFileObjects.size() * 3 + 11));
            final Iterator<Object> it = allIBaseFileObjects.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                if (obj instanceof List<?>) {
                    for (Iterator<?> iterator = ((List<?>) obj).iterator(); iterator.hasNext();) {
                        @SuppressWarnings("unchecked")
                        WeakReference<BaseFileObj> ref = (WeakReference<BaseFileObj>) iterator.next();
                        BaseFileObj fo = shallBeChecked(
                            ref != null ? ref.get() : null, noRecListeners
                        );
                        if (fo != null) {
                            all2Refresh.add(fo);
                        }
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    final WeakReference<BaseFileObj> ref = (WeakReference<BaseFileObj>) obj;
                    BaseFileObj fo = shallBeChecked(
                        ref != null ? ref.get() : null, noRecListeners
                    );
                    if (fo != null) {
                        all2Refresh.add(fo);
                    }
                }
            }
        } finally {
            allIBaseLock.writeLock().unlock();
        }
        all2Refresh.remove(root); // #182793
        return all2Refresh;
    }

    private BaseFileObj shallBeChecked(BaseFileObj fo, boolean noRecListeners) {
        if (fo != null && noRecListeners) {
            FolderObj p = (FolderObj) (fo instanceof FolderObj ? fo : fo.getExistingParent());
            if (p != null && Watcher.isWatched(fo)) {
                LOG_REFRESH.log(Level.FINER, "skip: {0}", fo);
                fo = null;
            }
        }
        return fo;
    }

    private boolean refresh(final Set<BaseFileObj> all2Refresh, RefreshSlow slow, File... files) {
        return refresh(all2Refresh, slow, true, files);
    }

    private static boolean isInFiles(BaseFileObj fo, File[] files) {
        if (fo == null) {
            return false;
        }
        if (files == null) {
            return true;
        }
        for (File file : files) {
            if (isParentOf(file, fo.getFileName().getFile())) {
                return true;
            }
        }
        return false;
    }

    private boolean refresh(final Set<BaseFileObj> all2Refresh, RefreshSlow slow, final boolean expected) {
        return refresh(all2Refresh, slow, expected, null);
    }

    private boolean refresh(final Set<BaseFileObj> all2Refresh, RefreshSlow slow, final boolean expected, File[] files) {
        int add = 0;
        Iterator<BaseFileObj> it = all2Refresh.iterator();
        while (it.hasNext()) {
            BaseFileObj fo = null;
            if (slow != null) {
                BaseFileObj pref = slow.preferrable();
                if (pref != null && all2Refresh.remove(pref)) {
                    LOG_REFRESH.log(Level.FINER, "Preferring {0}", pref);
                    fo = pref;
                    it = all2Refresh.iterator();
                }
            }
            if (fo == null) {
                fo = it.next();
                it.remove();
            }
            add++;
            if (!isInFiles(fo, files)) {
                continue;
            }
            if (slow != null) {
                if (!slow.refreshFileObject(fo, expected, add)) {
                    return false;
                }
                add = 0;
            } else {
                fo.refresh(expected);
            }
        }
        return true;
    }

    public static boolean isParentOf(final File dir, final File file) {
        File tempFile = file;
        while (tempFile != null && !Utils.equals(tempFile, dir)) {
            tempFile = tempFile.getParentFile();
        }
        return tempFile != null;
    }

    public final void rename(Set<BaseFileObj> changeId) {
        final Map<Integer, Object> toRename = new HashMap<Integer, Object>();
        allIBaseLock.writeLock().lock();
        try {
            final Iterator<Map.Entry<Integer, Object>> it = allIBaseFileObjects.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Object> entry = it.next();
                final Object obj = entry.getValue();
                final Integer key = entry.getKey();
                if (!(obj instanceof List<?>)) {
                    @SuppressWarnings("unchecked")
                    final WeakReference<BaseFileObj> ref = (WeakReference<BaseFileObj>) obj;

                    final BaseFileObj fo = (ref != null) ? ref.get() : null;
                    if (changeId.contains(fo)) {
                        toRename.put(key, fo);
                    }
                } else {
                    for (Iterator<?> iterator = ((List<?>) obj).iterator(); iterator.hasNext();) {
                        @SuppressWarnings("unchecked")
                        WeakReference<BaseFileObj> ref = (WeakReference<BaseFileObj>) iterator.next();
                        final BaseFileObj fo = (ref != null) ? ref.get() : null;
                        if (changeId.contains(fo)) {
                            toRename.put(key, ref);
                        }
                    }

                }
            }

            for (Map.Entry<Integer, Object> entry : toRename.entrySet()) {
                Integer key = entry.getKey();
                Object previous = allIBaseFileObjects.remove(key);
                if (previous instanceof List<?>) {
                    List<?> list = (List<?>) previous;
                    list.remove(entry.getValue());
                    allIBaseFileObjects.put(key, previous);
                } else {
                    BaseFileObj bfo = (BaseFileObj) entry.getValue();
                    putInCache(bfo, bfo.getFileName().getId());
                }
            }
        } finally {
            allIBaseLock.writeLock().unlock();
        }
    }

    public final BaseFileObj getCachedOnly(final File file) {
        return getCachedOnly(file, true);
    }
    public final BaseFileObj getCachedOnly(final File file, boolean checkExtension) {
        BaseFileObj retval;
        final Integer id = NamingFactory.createID(file);
        allIBaseLock.readLock().lock();
        try {
            final Object value = allIBaseFileObjects.get(id);
            if (value instanceof Reference<?>) {
                retval = getReference(Collections.nCopies(1, value), file);
            } else {
                retval = getReference((List<?>) value, file);
            }
        } finally {
            allIBaseLock.readLock().unlock();
        }
        if (retval != null && checkExtension) {
            if (!file.getName().equals(retval.getNameExt())) {
                if (!Utils.equals(file, retval.getFileName().getFile())) {
                    retval = null;
                }
            }
        }
        return retval;
    }

    private static BaseFileObj getReference(final List<?> list, final File file) {
        BaseFileObj retVal = null;
        if (list != null) {
            for (int i = 0; retVal == null && i < list.size(); i++) {
                Object item = list.get(i);
                if (!(item instanceof Reference<?>)) {
                    continue;
                }
                final Object ce = ((Reference<?>)item).get();
                if (!(ce instanceof BaseFileObj)) {
                    continue;
                }
                final BaseFileObj cachedElement = (BaseFileObj)ce;
                if (cachedElement != null && cachedElement.getFileName().getFile().compareTo(file) == 0) {
                    retVal = cachedElement;
                }
            }
        }
        return retVal;
    }

    private BaseFileObj putInCache(final BaseFileObj newValue, final Integer id) {
        allIBaseLock.writeLock().lock();
        try {
            final WeakReference<BaseFileObj> newRef = new WeakReference<BaseFileObj>(newValue);
            final Object listOrReference = allIBaseFileObjects.put(id, newRef);

            if (listOrReference != null) {
                if (listOrReference instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Reference<BaseFileObj>> list = (List<Reference<BaseFileObj>>) listOrReference;
                    list.add(newRef);
                    allIBaseFileObjects.put(id, listOrReference);
                } else {
                    assert (listOrReference instanceof WeakReference<?>);
                    @SuppressWarnings("unchecked")
                    final Reference<BaseFileObj> oldRef = (Reference<BaseFileObj>) listOrReference;
                    BaseFileObj oldValue = (oldRef != null) ? oldRef.get() : null;

                    if (oldValue != null && !newValue.getFileName().equals(oldValue.getFileName())) {
                        final List<Reference<BaseFileObj>> l = new ArrayList<Reference<BaseFileObj>>();
                        l.add(oldRef);
                        l.add(newRef);
                        allIBaseFileObjects.put(id, l);
                    }
                }
            }
        } finally {
            allIBaseLock.writeLock().unlock();
        }

        return newValue;
    }

    @Override
    public String toString() {
        List<Object> list = new ArrayList<Object>();
        allIBaseLock.readLock().lock();
        try {
            list.addAll(allIBaseFileObjects.values());
        } finally {
            allIBaseLock.readLock().unlock();
        }
        List<String> l2 = new ArrayList<String>();
        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            @SuppressWarnings("unchecked")
            Reference<FileObject> ref = (Reference<FileObject>) it.next();
            FileObject fo = (ref != null) ? ref.get() : null;
            if (fo != null) {
                l2.add(fo.getPath());
            }
        }
        return l2.toString();
    }

    public static synchronized Map<File, FileObjectFactory> factories() {
        return new HashMap<File, FileObjectFactory>(AllFactories);
    }

    public boolean isWarningEnabled() {
        return WARNINGS && !BaseUtilities.isMac();
    }

    //only for tests purposes
    public static void reinitForTests() {
        FileObjectFactory.AllFactories.clear();
    }



    public final BaseFileObj getValidFileObject(final File f, FileObjectFactory.Caller caller, boolean onlyExisting) {
        final BaseFileObj retVal = (getFileObject(new FileInfo(f), caller, onlyExisting));
        if (onlyExisting) {
            return (retVal != null && retVal.isValid()) ? retVal : null;
        } else {
            return retVal;
        }
    }

    public void refresh(boolean expected) {
        refresh(null, expected);
    }
    void refresh(RefreshSlow slow, boolean expected) {
        refresh(slow, false, expected);
    }
    final void refresh(final RefreshSlow slow, final boolean ignoreRecursiveListeners, final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                refreshAll(slow, ignoreRecursiveListeners, expected);
            }            
        };        
        
        stopWatch.start();
        try {
            if (slow != null) {
                FileBasedFileSystem.runAsInconsistent(r);
            } else {
                FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        FileBasedFileSystem.runAsInconsistent(r);
                    }
                });
            }
        } catch (IOException iex) {/*method refreshAll doesn't throw IOException*/

        }
        stopWatch.stop();

        // print refresh stats unconditionally in trunk
        if (LOG_REFRESH.isLoggable(Level.FINE)) {
            LOG_REFRESH.log(
                Level.FINE,
                "FS.refresh statistics ({0}FileObjects):\n  {1}\n  {2}\n  {3}\n  {4}\n",
                new Object[]{
                    Statistics.fileObjects(),
                    Statistics.REFRESH_FS.toString(),
                    Statistics.LISTENERS_CALLS.toString(),
                    Statistics.REFRESH_FOLDER.toString(),
                    Statistics.REFRESH_FILE.toString()
                }
            );
        }

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }

    final void refreshFor(final RefreshSlow slow, final boolean ignoreRecursiveListeners, final File... files) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                Set<BaseFileObj> all2Refresh = collectForRefresh(ignoreRecursiveListeners);
                refresh(all2Refresh, slow, files);
                if (LOG_REFRESH.isLoggable(Level.FINER)) {
                    LOG_REFRESH.log(Level.FINER, "Refresh for {0} objects", all2Refresh.size());
                    for (BaseFileObj baseFileObj : all2Refresh) {
                        LOG_REFRESH.log(Level.FINER, "  {0}", baseFileObj);
                    }
                }
            }
        };        
        stopWatch.start();
        try {
            if (slow != null) {
                FileBasedFileSystem.runAsInconsistent(r);
            } else {
                FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        FileBasedFileSystem.runAsInconsistent(r);
                    }
                });
            }
        } catch (IOException iex) {/*method refreshAll doesn't throw IOException*/

        }
        stopWatch.stop();

        // print refresh stats unconditionally in trunk
        if (LOG_REFRESH.isLoggable(Level.FINE)) {
            LOG_REFRESH.log(
                Level.FINE,
                "FS.refresh statistics ({0}FileObjects):\n  {1}\n  {2}\n  {3}\n  {4}\n",
                new Object[]{
                    Statistics.fileObjects(),
                    Statistics.REFRESH_FS.toString(),
                    Statistics.LISTENERS_CALLS.toString(),
                    Statistics.REFRESH_FOLDER.toString(),
                    Statistics.REFRESH_FILE.toString()
                }
            );
        }

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }
    
    private static class AsyncRefreshAtomicAction implements  FileSystem.AtomicAction {
        private FileObject fo;
        AsyncRefreshAtomicAction(FileObject fo) {
            this.fo = fo;
        }
        @Override
        public void run() throws IOException {
            this.fo.refresh();
        }
        
    }

    private void refreshFromGetter(final FileObject parent,boolean asyncFire)  {
        try {
            if (asyncFire) {
                FileUtil.runAtomicAction(new AsyncRefreshAtomicAction(parent));
            } else {
                parent.refresh();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
