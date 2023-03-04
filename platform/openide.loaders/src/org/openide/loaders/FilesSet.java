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

import java.util.*;

import org.openide.filesystems.FileObject;

/** This class is a lazy initialized set of FileObjects representing entries
 * in MultiDataObject class. Primary file object is returned as the first from
 * this set, the secondary fileobjects are sorted by <code>getNameExt()</code>
 * method result alphabetically.
 * <p>
 * This class is an implementation of performance enhancement #16396.
 *
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=16396">Issue #16396</a>
 *
 * @author  Petr Hamernik
 */
final class FilesSet implements Set<FileObject> {

    /** MDO which created this set */
    private final MultiDataObject mymdo;
    
    /** A flag */
    private boolean lazyWorkDone;

    /** Primary file. It is returned first. */
    private FileObject primaryFile;
    
    /** The link to secondary variable of MultiDataObject. Reading of the content
     * must be synchronized on this map.
     */
    private Map<FileObject,MultiDataObject.Entry> secondary;

    /** The set containing all files. It is <code>null</code> and is lazy initialized
     * when necessary.
     */
    private TreeSet<FileObject> delegate;
    
    /** Creates a new instance of FilesSet for the MultiDataObject.
     * @param primaryFile The primary file - this object is returned first.
     * @param secondary the map of secondary file objects. It is used 
     *     for initialization <code>delegate</code> variable when necessary.
     */
    public FilesSet(MultiDataObject mdo) {
        this.mymdo = mdo;
    }

    /** Does the work which was originally done in MDO.files() method. */
    private void doLazyWork() {
        synchronized (this) {
            if (!lazyWorkDone) {
                lazyWorkDone = true;

                synchronized ( mymdo.synchObjectSecondary() ) {
                    // cleans up invalid entries
                    Object res = mymdo.secondaryEntries(false);
                    assert res == null;
                    primaryFile = mymdo.getPrimaryFile();
                    secondary = mymdo.getSecondary();
                }
            }
        }
    }
    
    /** Perform lazy initialization of delegate TreeSet.
     */
    private Set<FileObject> getDelegate() {
        doLazyWork();
        // This synchronized block was moved from MultiDataObject.files() method,
        // because of lazy initialization of delegate TreeSet.
        // Hopefully won't cause threading problems.
        synchronized (secondary) {
            if (delegate == null) {
                delegate = new TreeSet<FileObject>(new FilesComparator());
                delegate.add(primaryFile);
                delegate.addAll(secondary.keySet());
            }
        }
        return delegate;
    }

    // =====================================================================
    //   Implementation of Set interface methods
    // =====================================================================
    
    public boolean add(FileObject obj) {
        return getDelegate().add(obj);
    }
    
    public boolean addAll(Collection<? extends FileObject> collection) {
        return getDelegate().addAll(collection);
    }
    
    public void clear() {
        getDelegate().clear();
    }
    
    public boolean contains(Object obj) {
        return getDelegate().contains(obj);
    }
    
    public boolean containsAll(Collection<?> collection) {
        return getDelegate().containsAll(collection);
    }
    
    public boolean isEmpty() {
        doLazyWork();
        synchronized (secondary) {
            return (delegate == null) ? false : delegate.isEmpty();
        }
    }
    
    public Iterator<FileObject> iterator() {
        doLazyWork();
        synchronized (secondary) {
            return (delegate == null) ? new FilesIterator() : delegate.iterator();
        }
    }
    
    public boolean remove(Object obj) {
        return getDelegate().remove(obj);
    }
    
    public boolean removeAll(Collection<?> collection) {
        return getDelegate().removeAll(collection);
    }
    
    public boolean retainAll(Collection<?> collection) {
        return getDelegate().retainAll(collection);
    }
    
    public int size() {
        doLazyWork();
        synchronized (secondary) {
            return (delegate == null) ? (secondary.size() + 1) : delegate.size();
        }
    }
    
    public Object[] toArray() {
        return getDelegate().toArray();
    }
    
    public <T> T[] toArray(T[] obj) {
        return getDelegate().toArray(obj);
    }

    public boolean equals(Object obj) {
        return getDelegate().equals(obj);
    }

    public String toString() {
        return getDelegate().toString();
    }

    public int hashCode() {
        return getDelegate().hashCode();
    }

    /** Iterator for FilesSet. It returns the primaryFile first and 
     * then initialize the delegate iterator for secondary files.
     */
    private final class FilesIterator implements Iterator<FileObject> {
        /** Was the first element (primary file) already returned?
         */
        private boolean first = true;
        
        /** Delegation iterator for secondary files. It is lazy initialized after
         * the first element is returned.
         */
        private Iterator<FileObject> itDelegate = null;
        
        FilesIterator() {}
        
        public boolean hasNext() {
            return first ? true : getIteratorDelegate().hasNext();
        }
        
        public FileObject next() {
            if (first) {
                first = false;
                return FilesSet.this.primaryFile;
            }
            else {
                return getIteratorDelegate().next();
            }
        }

        @Override
        public void remove() {
            getIteratorDelegate().remove();
        }

        /** Initialize the delegation iterator.
         */
        private Iterator<FileObject> getIteratorDelegate() {
            if (itDelegate == null) {
                // this should return iterator of all files of the MultiDataObject...
                itDelegate = FilesSet.this.getDelegate().iterator();
                // ..., so it is necessary to skip the primary file
                itDelegate.next();
            }
            return itDelegate;
        }
    }
    
    /** Comparator for file objects. The primary file is less than any other file,
     * so it is returned first. Other files are compared by getNameExt() method
     * result.
     */
    private final class FilesComparator implements Comparator<FileObject> {
        FilesComparator() {}
        public int compare(FileObject f1, FileObject f2) {
            if (f1 == f2)
                return 0;
            
            if (f1 == primaryFile)
                return -1;
            
            if (f2 == primaryFile)
                return 1;
            
            int res = f1.getNameExt().compareTo(f2.getNameExt());
            
            if (res == 0) {
                // check whether they both live on the same fs
                try {
                    if (f1.getFileSystem() == f2.getFileSystem()) {
                        return 0;
                    }
                    // different fs --> compare the fs names
                    return f1.getFileSystem().getSystemName().compareTo(
                        f2.getFileSystem().getSystemName()); 
                } catch (org.openide.filesystems.FileStateInvalidException fsie) {
                    // should not happen - but the names were the same
                    // so we declare they are the same (even if the filesystems
                    // crashed meanwhile)
                    return 0;
                }
            }
            
            return res;
        }
    }
}
