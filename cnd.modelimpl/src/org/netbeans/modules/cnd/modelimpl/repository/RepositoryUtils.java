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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDManager;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptionListener;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.api.RepositoryListener;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public final class RepositoryUtils {

    private static final Logger LOG = Logger.getLogger(RepositoryUtils.class.getName());
    private static final boolean TRACE_ARGS = CndUtils.getBoolean("cnd.repository.trace.args", false); //NOI18N;
    private static final boolean TRACE_REPOSITORY_ACCESS = TRACE_ARGS || DebugUtils.getBoolean("cnd.modelimpl.trace.repository", false);
    /**
     * the version of the persistency mechanism
     */
    private static final int CURRENT_VERSION_OF_PERSISTENCY = 190;

//    /** temporary flag, to be removed as soon as relocatable repository is achieved */
//    public static final boolean RELOCATABLE = true;

    /** Creates a new instance of RepositoryUtils */
    private RepositoryUtils() {
    }
    
    public static int getPersistenceVersion() {
        if (APTTraceFlags.USE_CLANK) {
            return CURRENT_VERSION_OF_PERSISTENCY + /*shift for new Clank mode*/10000;
        } else {
            return CURRENT_VERSION_OF_PERSISTENCY;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // repository access wrappers
    private static volatile int counter = 0;

    public static <T> T get(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        Persistent obj = get(key);
        assert obj == null || (obj instanceof CsmIdentifiable) : "unexpected object with class " + obj.getClass() + obj; // NOI18N
        // we are sure in type, because of uid type
        @SuppressWarnings("unchecked")
        T out = (T)obj;
        return out;
    }

    public static Persistent get(Key key) {
        assert key != null;
        if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
            long time = System.currentTimeMillis();
            int index = nextIndex();
            System.err.println(index + ": " + System.identityHashCode(key) + "@getting key " + key);
            Persistent out = Repository.get(key);
            time = System.currentTimeMillis() - time;
            System.err.println(index + ": " + System.identityHashCode(key) + "@got" + (out == null ? " - NULL":"") + " in " + time + "ms the key " + key);
            return out;
        }
        return Repository.get(key);
    }

    private static synchronized int nextIndex() {
        return counter++;
    }

    public static<T> void remove(CsmUID<T> uid, CsmObject obj) {
        Key key = UIDtoKey(uid);
        if (key != null) {
            try {
                if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                    long time = System.currentTimeMillis();
                    int index = nextIndex();
                    System.err.println(index + ": " + System.identityHashCode(key) + "@removing key " + key);
                    if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                        Repository.remove(key);
                    }
                    time = System.currentTimeMillis() - time;
                    System.err.println(index + ": " + System.identityHashCode(key) + "@removed in " + time + "ms the key " + key);
                    return;
                }
                if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                    Repository.remove(key);
                }
            } finally {
                disposeUID(uid, obj);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static<T> void disposeUID(CsmUID<T> uid, CsmObject obj) {
        if (uid instanceof KeyBasedUID<?>) {
            ((KeyBasedUID<CsmObject>)uid).dispose(obj);
        }
    }

    public static<T> void remove(Collection<? extends CsmUID<T>> uids) {
        if (uids != null) {
            for (CsmUID<T> uid : uids) {
                remove(uid, null);
            }
        }
    }

    public static <T> CsmUID<T> put(T csmObj) {
        CsmUID<T> uid = null;
        if (csmObj != null) {
            // during put we suppress check for null
            uid = UIDProviderIml.get(csmObj, false);
            assert uid != null;
            Key key = UIDtoKey(uid);
            put(key, (Persistent) csmObj);
            if (!((csmObj instanceof CsmNamespace)||(csmObj instanceof CsmProject)||(csmObj instanceof CsmInstantiation))){
                assert uid.getObject() != null;
            }
        }
        return uid;
    }

    public static void put(Key key, Persistent obj) {
        if (key != null) {
            if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ": " + System.identityHashCode(key) + "@putting key " + key);
                Repository.put(key, obj);
                time = System.currentTimeMillis() - time;
                System.err.println(index + ": " + System.identityHashCode(key) + "@put in " + time + "ms the key " + key);
                return;
            }
            Repository.put(key, obj);
        }
    }

    public static void hang(Object csmObj) {
        CsmUID<?> uid;
        if (csmObj != null) {
            // during hang we suppress check for null
            uid = UIDProviderIml.get(csmObj, false);
            assert uid != null;
            Key key = UIDtoKey(uid);
            hang(key, (Persistent) csmObj);
            if (!((csmObj instanceof CsmNamespace)||(csmObj instanceof CsmProject))){
                assert uid.getObject() != null;
            }
        }
    }

    public static void hang(Key key, Persistent obj) {
        if (key != null) {
            if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ": " + System.identityHashCode(key) + "@hanging key " + key);
                Repository.hang(key, obj);
                time = System.currentTimeMillis() - time;
                System.err.println(index + ": " + System.identityHashCode(key) + "@hung in " + time + "ms the key " + key);
                return;
            }
            Repository.hang(key, obj);
        }
    }

    public static <T> Collection<CsmUID<T>> put(Collection<T> decls) {
        assert decls != null;
        List<CsmUID<T>> uids = new ArrayList<>(decls.size());
        for (T decl : decls) {
            if (decl instanceof CsmIdentifiable) {
                CsmUID<T> uid = put(decl);
                uids.add(uid);
            }
        }
        return uids;
    }

    public static <T extends CsmObject> void setSelfUIDs(Collection<T> decls) {
        assert decls != null;
        for (T decl : decls) {
            Utils.setSelfUID(decl);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    public static<T> Key UIDtoKey(CsmUID<T> uid) {
        if (uid instanceof KeyHolder) {
            return ((KeyHolder) uid).getKey();
        } else {
            return null;
        }
    }

    public static<T> CharSequence getUnitName(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        assert key != null;
        CharSequence unitName = key.getUnit();

        return unitName;
    }

    public static void startup() {
        Repository.addRepositoryExceptionListener(getRepositoryListenerProxy());
        Repository.registerRepositoryListener(getRepositoryListenerProxy());        
        Repository.startup(getPersistenceVersion());
    }

    private static RepositoryListenerProxy myRepositoryListenerProxy;
    private static synchronized RepositoryListenerProxy getRepositoryListenerProxy() {
        if (myRepositoryListenerProxy == null) {
            myRepositoryListenerProxy = new RepositoryListenerProxy();
        }
        return myRepositoryListenerProxy;
    }
    
    public static int clientToLayer(UnitsConverter unitsConverter, int clientUnitID) {
        if (unitsConverter == null) {
            return clientUnitID;
        }
        return unitsConverter.clientToLayer(clientUnitID);
    }  
    
    public static void shutdown() {
        // we intentionally do not unregister listener here since it will be automatically
        // unregistered as soon as shutdown (which is async.) finishes
        Repository.removeRepositoryExceptionListener(getRepositoryListenerProxy());
        Repository.unregisterRepositoryListener(getRepositoryListenerProxy());        
        Repository.shutdown();
    }

//    public static void cleanCashes() {
//        Repository.cleanCaches();
//    }
//
//    public static void debugClear() {
//        Repository.debugClear();
//    }

    public static<T> void closeUnit(CsmUID<T> uid, Set<Integer> requiredUnits, boolean cleanRepository) {
        closeUnit(UIDtoKey(uid), requiredUnits, cleanRepository);
    }

    public static void closeUnit(int unitId, Set<Integer> requiredUnits, boolean cleanRepository) {
        RepositoryListenerImpl.instance().onExplicitClose(KeyUtilities.getUnitName(unitId));
        _closeUnit(unitId, requiredUnits, cleanRepository);
    }

    public static void closeUnit(Key key, Set<Integer> requiredUnits, boolean cleanRepository) {
        assert key != null;
        _closeUnit(key.getUnitId(), requiredUnits, cleanRepository);
        if (cleanRepository) {
            UIDManager.instance().clearProjectCache(key);
        }
    }

    private static void _closeUnit(int unitId, Set<Integer> requiredUnits, boolean cleanRepository) {        
        if (!cleanRepository) {
            int errors = getRepositoryListenerProxy().getErrorCount(unitId);
            if (errors > 0) {
                if (LOG.isLoggable(Level.INFO)) {
                    CharSequence unit = KeyUtilities.getUnitNameSafe(unitId);
                    LOG.log(Level.INFO, "Clean index for project {0} \"{1}\" because index was corrupted (was {1} errors).", new Object[]{unitId, unit, errors}); // NOI18N
                }
                cleanRepository = true;
            }
        }
        getRepositoryListenerProxy().cleanErrorCount(unitId);
        Repository.closeUnit(unitId, cleanRepository, requiredUnits);
    }

    public static int getRepositoryErrorCount(ProjectBase project){
        return getRepositoryListenerProxy().getErrorCount(project.getUnitId());
    }

    public static void registerRepositoryError(ProjectBase project, Exception e) {
        CsmUID<?> uid = project.getUID();
        assert uid != null;
        Key key = UIDtoKey(uid);        
        getRepositoryListenerProxy().anExceptionHappened(key.getUnitId(), key.getUnit(), new RepositoryException(e));
    }
    
    public static void onProjectDeleted(NativeProject nativeProject) {
        Key key = KeyUtilities.createProjectKey(nativeProject);
        Repository.removeUnit(key.getUnitId());
    }

    public static void openUnit(ProjectBase project) {
        CsmUID<?> uid = project.getUID();
        assert uid != null;
        Key key = UIDtoKey(uid);
        openUnit(key);
    }

    public static void openUnit(Key key) {
        openUnit(key.getUnitId(), key.getUnit());
    }

    private static void openUnit(int unitId, CharSequence unitName) {
        // TODO explicit open should be called here:
        RepositoryListenerImpl.instance().onExplicitOpen(unitId);
        Repository.openUnit(unitId);
    }

//    public static void unregisterRepositoryListener(RepositoryListener listener) {
//        Repository.unregisterRepositoryListener(listener);
//    }
//    
//    public static void removeRepositoryExceptionListener(RepositoryExceptionListener listener) {
//        Repository.removeRepositoryExceptionListener(listener);
//    }    

    private static boolean isTracingKey(Key key) {
        if (TRACE_ARGS) {
            if (key.getDepth() == 3 &&
                    ("argc".contentEquals(key.getAt(2)) || // NOI18N
                    "main".contentEquals(key.getAt(2)))) { // NOI18N
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private static class RepositoryListenerProxy implements RepositoryListener, RepositoryExceptionListener {
        private final RepositoryListenerImpl parent = RepositoryListenerImpl.instance();
        private final Map<Integer,Integer> wasErrors = new ConcurrentHashMap<>();
        private boolean fatalError = false;
        private RepositoryListenerProxy(){
        }
        public int getErrorCount(int unitId) {
            Integer i = wasErrors.get(unitId);
            if (i == null) {
                return fatalError ? 1 : 0;
            } else {
                return fatalError ? i.intValue() + 1 : i.intValue();
            }
        }
        public void cleanErrorCount(int unitId) {
            wasErrors.remove(unitId);
            fatalError = false;
        }
        @Override
        public boolean unitOpened(int unitId) {
            return parent.unitOpened(unitId);
        }

//        @Override
//        public boolean repositoryOpened(int repositoryId, CacheLocation cacheLocation) {
//            return parent.repositoryOpened(repositoryId, cacheLocation);
//        }

        @Override
        public void unitClosed(int unitId) {
            parent.unitClosed(unitId);
        }
        
        
        @Override
        public void unitRemoved(int unitId) {
            parent.unitRemoved(unitId);
        }

        @Override
        public void anExceptionHappened(final int unitId, CharSequence unitName, RepositoryException exc) {
            primitiveErrorStrategy(unitId, exc);
            parent.anExceptionHappened(unitId, unitName, exc);
        }

        /**
         * Strategy only count errors.
         * TODO enhancement:
         * Provide intelligence logic that take into account possibility to "fixing" errors.
         * For example error in "file" segment can be fixed by file reparse.
         */
        private void primitiveErrorStrategy(int unitId, RepositoryException exc){
            Integer i = wasErrors.get(unitId);
            if (i == null) {
                i = Integer.valueOf(1);
            } else {
                i = Integer.valueOf(i.intValue()+1);
            }
            wasErrors.put(unitId, i);
        }
    }
}

