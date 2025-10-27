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

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

import java.io.File;
import java.util.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.util.Mutex;

/**
 * @author Radek Matous
 */
public class ChildrenSupport {

    static final int NO_CHILDREN_CACHED = 0;
    static final int SOME_CHILDREN_CACHED = 1;
    static final int ALL_CHILDREN_CACHED = 2;

    private Set<FileNaming> notExistingChildren;
    private Set<FileNaming> existingChildren;
    private int status = ChildrenSupport.NO_CHILDREN_CACHED;
    private static final Mutex.Privileged mutexPrivileged = new Mutex.Privileged();
    private static final Mutex mutex = new Mutex(mutexPrivileged);

    public ChildrenSupport() {
    }

    public final Mutex.Privileged getMutexPrivileged() {
        return mutexPrivileged;
    }

    public static boolean isLock() {
        return mutex.isReadAccess() || mutex.isWriteAccess();
    }

    public synchronized Set<FileNaming> getCachedChildren() {
        return new HashSet<FileNaming>(getExisting(false));
    }

    public synchronized Set<FileNaming> getChildren(final FileNaming folderName, final boolean rescan, Runnable[] task) {
        if (rescan || !isStatus(ChildrenSupport.ALL_CHILDREN_CACHED))  {
            if (rescanChildren(folderName, false, task) == null) {
                return null;
            }
            setStatus(ChildrenSupport.ALL_CHILDREN_CACHED);
        } /*else if (!isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {

        }*/

        //assert status == ChildrenSupport.ALL_CHILDREN_CACHED;
        return getExisting(false);
    }

    public boolean isCacheInitialized() {
        return !isStatus(ChildrenSupport.NO_CHILDREN_CACHED);
    }

    public synchronized FileNaming getChild(final String childName, final FileNaming folderName, final boolean rescan) {
        return getChild(childName, folderName, rescan, null);
    }

    public synchronized FileNaming getChild(final String childName, final FileNaming folderName, final boolean rescan, Runnable[] task) {
        FileNaming retval = null;
        if (rescan || isStatus(ChildrenSupport.NO_CHILDREN_CACHED)) {
            retval = rescanChild(folderName, childName, rescan, task);
        } else if (isStatus(ChildrenSupport.SOME_CHILDREN_CACHED)) {
            retval = lookupChildInCache(folderName, childName, true);
            if (retval == null && lookupChildInCache(folderName, childName, false) == null) {
                retval = rescanChild(folderName, childName, rescan, task);
            }
        } else if (isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {
            retval = lookupChildInCache(folderName, childName, true);
        }
        setStatus(ChildrenSupport.SOME_CHILDREN_CACHED);
        return retval;
    }

    /*public boolean existsldInCache(final FileNaming folder, final String childName) {
        return lookupChildInCache(folder, childName) != null;
    }*/

    public synchronized void removeChild(final FileNaming folderName, final FileNaming childName) {
        assert childName != null;
        getExisting().remove(childName);
        if (childName.getParent().equals(folderName)) {
            getNotExisting().add(childName);
        }
    }

    private synchronized void addChild(final FileNaming folderName, final FileNaming childName) {
        assert childName != null;
        assert childName.getParent().equals(folderName) : "childName: " + childName.getFile() + " folderName: " + folderName.getFile();
        getExisting().add(childName);
        getNotExisting().remove(childName);
    }



    public synchronized Map<FileNaming, Integer> refresh(final FileNaming folderName, Runnable[] task) {
        Map<FileNaming, Integer> retVal = new HashMap<FileNaming, Integer>();
        Set<FileNaming> e = new HashSet<FileNaming>(getExisting(false));
        Set<FileNaming> nE = new HashSet<FileNaming>(getNotExisting(false));

        if (isStatus(ChildrenSupport.SOME_CHILDREN_CACHED)) {
            Set<FileNaming> existingToCheck = new HashSet<FileNaming>(e);
            for (FileNaming fnToCheck : existingToCheck) {
                FileNaming fnRescanned = rescanChild(folderName, fnToCheck.getName(), true, true);
                if (fnRescanned == null) {
                    retVal.put(fnToCheck, ChildrenCache.REMOVED_CHILD);
                }
            }

            Set<FileNaming> notExistingToCheck = new HashSet<FileNaming>(nE);
            for (FileNaming fnToCheck : notExistingToCheck) {
                assert fnToCheck != null;
                FileNaming fnRescanned = rescanChild(folderName, fnToCheck.getName(), true, false);
                if (fnRescanned != null) {
                    retVal.put(fnToCheck, ChildrenCache.ADDED_CHILD);
                }
            }
        } else if (isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {
            retVal = rescanChildren(folderName, true, task);
        }
        return retVal;
    }

    @Override
    public String toString() {
        return getExisting(false).toString();
    }

    boolean isStatus(int status) {
        return this.status == status;
    }

    private void setStatus(int status) {
        if (this.status < status) {
            this.status = status;
        }
    }

    private FileNaming rescanChild(final FileNaming folderName,
            final String childName, boolean ignoreCache, Runnable[] task) {
        return rescanChild(folderName, childName, ignoreCache, true, task);
    }

    private FileNaming rescanChild(final FileNaming folderName,
            final String childName, boolean ignoreCache, boolean canonicalName) {
        return rescanChild(folderName, childName, ignoreCache, canonicalName, null);
    }

    /**
     * @param canonicalName True if the name should be considered as canonical -
     * it is not the case when rescanning items from
     * {@link #notExistingChildren} list (see bug 240156).
     */
    private FileNaming rescanChild(final FileNaming folderName,
            final String childName, final boolean ignoreCache,
            final boolean canonicalName, Runnable[] task) {

        final File folder = folderName.getFile();
        final File child = new File(folder, childName);
        final FileInfo fInfo = new FileInfo(child);

        class IOJob implements Runnable {

            private FileNaming fileNaming = null;

            @Override
            public void run() {
                boolean convertibleToFO = fInfo.isConvertibleToFileObject();
                this.fileNaming = convertibleToFO
                        ? NamingFactory.fromFile(folderName, child, ignoreCache, canonicalName)
                        : null;
            }
        }

        IOJob job;
        if (task != null && task[0] == null) {
            task[0] = new IOJob();
            return null;
        } else if (task == null) {
            job = new IOJob();
            job.run();
        } else {
            job = ((IOJob) task[0]);
        }

        if (job.fileNaming != null) {
            addChild(folderName, job.fileNaming);
        } else {
            FileName fChild = new FileName(folderName, child, null) {
                @Override
                public boolean isDirectory() {
                    return false;
                }

                @Override
                public boolean isFile() {
                    return false;
                }
            };

            removeChild(folderName,  fChild);
        }

        return job.fileNaming;
    }

    private Map<FileNaming, Integer> rescanChildren(final FileNaming folderName, final boolean ignoreCache, Runnable[] task) {
        final Map<FileNaming, Integer> retval = new IdentityHashMap<FileNaming, Integer>();

        final File folder = folderName.getFile();
        assert folderName.getFile().getAbsolutePath().equals(folderName.toString());
        
        class IOJob implements Runnable {
            boolean folderExists;
            Set<FileNaming> newChildren;
            @Override
            public void run() {
                final File[] children = folder.listFiles();
                if (children != null) {
                    newChildren = new LinkedHashSet<FileNaming>();
                    for (int i = 0; i < children.length; i++) {
                        final FileInfo fInfo = new FileInfo(children[i], 1);
                        if (fInfo.isConvertibleToFileObject()) {
                            FileNaming child = NamingFactory.fromFile(folderName, children[i], ignoreCache);
                            newChildren.add(child);
                        }
                    }
                } else {
                    folderExists = folder.exists();
                    // #150009 - children == null -> folder does not exists, or an I/O error occurs
                    // folder.listFiles() failed with I/O exception - do not remove children
                }
            }
        }
        IOJob job;
        if (task[0] instanceof IOJob) {
            job = (IOJob)task[0];
        } else {
            task[0] = new IOJob();
            return null;
        }

        if (job.newChildren == null) {
            if (job.folderExists) { // #150009 - children == null -> folder does not exists, or an I/O error occurs
                // folder.listFiles() failed with I/O exception - do not remove children
                return retval;
            }
            job.newChildren = new LinkedHashSet<FileNaming>();
        }

        Set<FileNaming> deleted = deepMinus(getExisting(false), job.newChildren);
        for (FileNaming fnRem : deleted) {
            removeChild(folderName, fnRem);
            retval.put(fnRem, ChildrenCache.REMOVED_CHILD);
        }

        Set<FileNaming> added = deepMinus(job.newChildren, getExisting(false));
        for (FileNaming fnAdd : added) {
            addChild(folderName, fnAdd);
            retval.put(fnAdd, ChildrenCache.ADDED_CHILD);
        }

        return retval;
    }

    private static Set<FileNaming> deepMinus(Set<FileNaming> base, Set<FileNaming> minus) {
        HashMap<FileNaming, FileNaming> detract = new HashMap<FileNaming, FileNaming>(base.size() * 2);
        for (FileNaming fn : base) {
            detract.put(fn, fn);
        }
        assert minus != null;
        for (FileNaming mm : minus) {
            FileNaming orig = detract.remove(mm);
            if (orig != null && orig.isFile() != mm.isFile()) {
                detract.put(orig, orig);
            }
        }
        return detract.keySet();
    }

    private FileNaming lookupChildInCache(final FileNaming folder, final String childName, boolean lookupExisting) {
        final File f = new File(folder.getFile(), childName);
        final FileNaming.ID id = NamingFactory.createID(f);

        class FakeNaming implements FileNaming {
            public FileNaming lastEqual;

            public  String getName() {
                return childName;
            }
            public FileNaming getParent() {
                return folder;
            }
            public boolean isRoot() {
                return false;
            }

            public File getFile() {
                return f;
            }

            public FileNaming.ID getId() {
                return id;
            }
            public FileNaming rename(String name, ProvidedExtensions.IOHandler h) {
                // not implemented, as it will not be called
                throw new IllegalStateException();
            }

            @Override
            public boolean equals(Object obj) {
                if (hashCode() == obj.hashCode() && getName().equals(((FileNaming)obj).getName())) {
                    assert lastEqual == null : "Just one can be there"; // NOI18N
                    if (obj instanceof FileNaming) {
                        lastEqual = (FileNaming)obj;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return id.value();
            }

            public boolean isFile() {
                return this.getFile().isFile();
            }

            public boolean isDirectory() {
                return !isFile();
            }
        }
        FakeNaming fake = new FakeNaming();

        final Set<FileNaming> cache = (lookupExisting) ? getExisting(false) : getNotExisting(false);
        if (cache.contains(fake)) {
            assert fake.lastEqual != null : "If cache contains the object, we set lastEqual"; // NOI18N
            assert fake.lastEqual.getName().equals(childName) : "childName: " + childName + " equals: " + fake.lastEqual;
            return fake.lastEqual;
        } else {
            return null;
        }
    }

    private synchronized Set<FileNaming> getExisting() {
        return getExisting(true);
    }

    private synchronized Set<FileNaming> getExisting(boolean init) {
        if (init && existingChildren == null) {
            existingChildren = new HashSet<FileNaming>();
        }
        return existingChildren != null ? existingChildren : Collections.<FileNaming>emptySet();
    }

    private synchronized Set<FileNaming> getNotExisting() {
        return getNotExisting(true);
    }

    private synchronized Set<FileNaming> getNotExisting(boolean init) {
        if (init && notExistingChildren == null) {
            notExistingChildren = new HashSet<FileNaming>();
        }
        return notExistingChildren != null ? notExistingChildren : Collections.<FileNaming>emptySet();
    }
    
    protected final synchronized void copyTo(ChildrenSupport newCache, FileNaming name) {
        assert newCache != this && newCache.existingChildren == null && newCache.notExistingChildren == null;
        for (FileNaming fn : getExisting(false)) {
            newCache.getChild(fn.getName(), name, true);
        }
        for (FileNaming fn : getNotExisting(false)) {
            newCache.getChild(fn.getName(), name, true);
        }
        newCache.status = status;
    }
}
