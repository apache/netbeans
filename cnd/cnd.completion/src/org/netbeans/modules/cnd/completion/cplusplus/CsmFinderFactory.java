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
package org.netbeans.modules.cnd.completion.cplusplus;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;

/**
 * Factory producing misc JCFinders.
 *
 * <p>This class uses three synchronization objects:</p>
 * <ul>
 * <li><code>CsmFinderFactory.class instance</code> is used to synchronize creation of
 *      CsmFinderFactory singleton in getDefault method</li>
 * <li><code>CsmFinderFactory instance</code> is used for synchronization of getFinder and
 *     getGlobalFinder methods. The purpose is to synchronize situation when two
 *     threads are asking for global finder or for finder for the same file. Both
 *     methods are relatively fast: they #1) retrieve classpath(s) and ask for
 *     parsed DBs and #2) if parser DB does not exist it will schedule its
 *     parsing and continues. After scheduled parser DB was created the
 *     parser thread will notify this class by resetCache() method.</li>
 * <li><code>CACHE_LOCK instance</code> is used for synchronization of access to internal caches.
 *     This lock is much finer compared to CsmFinderFactory instance lock.
 *     Only reading/modifying operations of the cache can happed under this lock.
 *     Its purpose is to allow reseting of caches from listeners which was
 *     found as deadlock prone when synchronized on CsmFinderFactory instance lock.</li>
 * </ul>
 */
public final class CsmFinderFactory {

    private static CsmFinderFactory DEFAULT;
    /** Empty finder */
//    private static final JCBaseFinder EMPTY = new JCBaseFinder(CCKit.class);
    /** Cache of <FileObject, SoftReference<CsmFinder>>.
     * The FO is classpath root. Access to cache must be always synchronized
     * on CACHE_LOCK instance.
     */
    private Map<FileObject, Reference<CsmFinder>> cache = new HashMap<FileObject, Reference<CsmFinder>>();
    /** Weak map whose value is always null and only key is relevant.
     * The key is ClassPath on which we are already listening. The purpose
     * of this map is to not attach one listener on the classpath multiple times.
     */
    //private WeakHashMap cpListening = new WeakHashMap();
    /** Weak map whose value is always null and only key is relevant.
     * The key is fakeJCClass used in web/jsp => XXX
     */
    //private WeakHashMap fakeClasses = new WeakHashMap();
    /** This is property change listener listening on classpaths and
     * invalidating cache when cp has changed. It must be wrapped in weak
     * listener to allow cp to be garbage collected. */
    //private static PropertyChangeListener cpListener;
    /** Cached global finder. Access to this variable must be always
     * synchronized on CACHE_LOCK instance. */
    private SoftReference<CsmFinder> globalFinder;
//    private GlobalPathRegistryListener gpListener;
    /** Object used as lock for cache updating synchronization. */
    private final Object CACHE_LOCK = new Object();
    /** Was FakeFinder initialized? Use it in CompoundFinder? */
    //private boolean useFakeFinder = false;

    private CsmFinderFactory() {
//        cpListener = new ClassPathListener();
//        gpListener = new GlobalPathListener();
//        GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(gpListener);
    }

    public static synchronized CsmFinderFactory getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new CsmFinderFactory();
        }
        return DEFAULT;
    }

    /**
     * Invalidate cache of finders. This method is expected to be called for example
     * when ParserThread finished parsing of a request or when parser DB was
     * deleted in parser DB manager by user.
     */
    public void resetCache() {
        synchronized (CACHE_LOCK) {
            cache = new HashMap<FileObject, Reference<CsmFinder>>();
            invalidateGlobalFinderCache();
        }
    }

    /** Returns finder for the given file.
     *
     * @return finder; cannot be null;
     */
    public CsmFinder getFinder(FileObject fo) {
        // the file must be on a SOURCE classpath
//        ClassPath sourceCP = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        FileObject owner;
//        if (sourceCP == null) {
        owner = null;
//        }else{
//            owner = sourceCP.findOwnerRoot(fo);
//        }
        FileObject cacheKey = (owner != null) ? owner : fo;

        CsmFinder finder = retrieveFromCache(cacheKey);
        if (finder != null) {
            return finder;
        }

        CsmFile cf = CsmUtilities.getCsmFile(fo, true, false);

        List<CsmFinder> finders = new ArrayList<CsmFinder>();
//        ArrayList fileObjects = new ArrayList();
        if (cf != null) {
//            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            addClasspathFinders(finders, fileObjects, cp, false);
//            cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//            addClasspathFinders(finders, fileObjects, cp, true);
//            cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
//            addClasspathFinders(finders, fileObjects, cp, true);
            finder = new CsmFinderImpl(cf, getMimeType());
            return finder;
//            finders.add();
        } else {
            finders.add(getGlobalFinder());
        }
//
//        // XXX - appending fake finder
//        if (useFakeFinder){
//            JCBaseFinder fakeFinder = new FakeFinder(getKitClass());
//            finders.add(fakeFinder);
//        }
//
//        finder = new CompoundFinder(finders, getKitClass());
        finder = getGlobalFinder();
        synchronized (CACHE_LOCK) {
            cache.put(cacheKey, new SoftReference<CsmFinder>(finder));
        }

        return finder;
    }

//    private class FakeFinder extends JCBaseFinder{
//        public FakeFinder(Class kitClass){
//            super(kitClass);
//            Set keySet = fakeClasses.keySet();
//            Iterator iter = keySet.iterator();
//            while (iter.hasNext()){
//                CsmClass cls = (CsmClass) iter.next();
//                appendClass(cls);
//            }
//        }
//    }
    /** Returns global finder which uses GlobalPathRegistry to learn
     * all ClassPaths in use and returns finder on top of all these classpaths.
     */
    public synchronized CsmFinder getGlobalFinder() {
        CsmFinder finder;
        synchronized (CACHE_LOCK) {
            finder = globalFinder != null ? globalFinder.get() : null;
        }
        if (finder != null) {
            return finder;
        }
//        ArrayList finders = new ArrayList();
//        ArrayList fileObjects = new ArrayList();
//        Iterator it = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE).iterator();
//        while (it.hasNext()) {
//            ClassPath cp = (ClassPath)it.next();
//            addClasspathFinders(finders, fileObjects, cp, false);
//        }
//        ArrayList allCPs = new ArrayList();
//        allCPs.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE));
//        allCPs.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT));
//        it = allCPs.iterator();
//        while (it.hasNext()) {
//            ClassPath cp = (ClassPath)it.next();
//            addClasspathFinders(finders, fileObjects, cp, true);
//        }

//        finder = new CompoundFinder(finders, CCKit.class);
        finder = new CsmFinderImpl((FileObject) null, getMimeType());
        synchronized (CACHE_LOCK) {
            globalFinder = new SoftReference<CsmFinder>(finder);
        }
        return finder;
    }

    /**
     * Invalidate global finder. This method is expected to be called for example
     * when list of globally registered ClassPaths has changed.
     */
    private void invalidateGlobalFinderCache() {
        synchronized (CACHE_LOCK) {
            globalFinder = null;
        }
    }

    private String getMimeType() {
        return MIMENames.SOURCES_MIME_TYPE;
    }

    private CsmFinder retrieveFromCache(FileObject fo) {
        synchronized (CACHE_LOCK) {
            SoftReference<CsmFinder> ref = (SoftReference<CsmFinder>) cache.get(fo);
            return ref != null ? ref.get() : null;
        }
    }
    /**
     * This method is called when classpath has changed.
     */
//    private void updateCache(ClassPath cp) {
//        List keys;
//        synchronized (CACHE_LOCK) {
//            keys = new ArrayList(cache.keySet());
//        }
//        Iterator it = keys.iterator();
//        while (it.hasNext()) {
//            FileObject fo = (FileObject)it.next();
//
//            // first check that this item from cache is still valid
//            CsmFinder finder = retrieveFromCache(fo);
//            if (finder == null) {
//                // the finder was garbage collected -> remove the key
//                removeFromCache(fo);
//                continue;
//            }
//
//            ClassPath c = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//            c = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//            c = ClassPath.getClassPath(fo, ClassPath.BOOT);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//        }
//        // the global finder is affected too: invalidate it
//        invalidateGlobalFinderCache();
//    }
//    private class ClassPathListener implements PropertyChangeListener {
//
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
//                assert evt != null && evt.getSource() instanceof ClassPath;
//                updateCache((ClassPath)evt.getSource());
//            }
//        }
//
//    }
//
//    private class GlobalPathListener implements GlobalPathRegistryListener {
//
//        public void pathsAdded(GlobalPathRegistryEvent event) {
//            invalidateGlobalFinderCache();
//
//            // any change (e.g.: a project was open) will trigger parser DB creation
//            // if it does not exist yet
//            // Post it to RP and do not block event processing. The getGlobalFinder()
//            // method is relatively fast and result is cached, but opening
//            // multiple projects at once is visibly slower if global finder
//            // is refreshed directly here.
//            RequestProcessor.getDefault().postRequest(new Runnable() {
//                public void run() {
//                    CsmFinderFactory.getDefault().getGlobalFinder();
//                }
//            }, 1000); // meaning of 1 sec is just to slightly delay this operation
//        }
//
//        public void pathsRemoved(GlobalPathRegistryEvent event) {
//            invalidateGlobalFinderCache();
//        }
//
//    }
}
