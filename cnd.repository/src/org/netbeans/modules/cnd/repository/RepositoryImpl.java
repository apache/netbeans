/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.RepositoryImplementation;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.storage.StorageManager;
import org.openide.util.Exceptions;

/**
 * REPOSITORY Service Implementation.
 * <p>
 * The function of repository is to provides services: <ul>
 * <li><code>Persistent get(Key key)</code></li>
 * <li><code>put(Key key, Persistent obj)</code></li> </ul> </p>
 * <p>
 * This implementation has a single in-memory cache and several storages (that
 * are hidden behind a StorageManager).</p>
 * <p>
 * Cache stores everything (weakly) that was already requested by a client. If
 * no answer is found in the cache, StorageManager is queried.</p>
 * <p>
 * There is two important things: <ol>
 * <li>This class is the ONLY class that 'instantiates' Persistent objects.</li>
 * <li>Clients know nothing about any layers.</li> </ol> </p>
 *
 * @see StorageManager
 */
public final class RepositoryImpl implements RepositoryImplementation, RemoveKeySupport {

    /* package */ static final Persistent REMOVED_OBJECT = new RemovedObject();
    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentHashMap<Integer, RepositoryCache> caches = new ConcurrentHashMap<Integer, RepositoryCache>();
    private final StorageManager storage;
    private final AsyncRepositoryWriter writer;
    private static final java.util.logging.Logger log = Logger.getInstance();

    public RepositoryImpl(int persistMechanismVersion) {
        storage = new StorageManager(persistMechanismVersion);
        writer = new AsyncRepositoryWriterImpl(storage, this);
    }

    /**
     * Returns an instantiated Persistent object by a Key.
     *
     * @param key - a key to get Persistent for. <code>key.getUnitId()</code> is
     * a 'long' id - i.e. 10001.
     * @return instantiated (read-out) Persistent object.
     */
    @Override
    public Persistent get(final Key key) {
        try {
            final RepositoryCache unitCache = getCache(key.getUnitId());
            Persistent result = unitCache.get(key);
            if (result == REMOVED_OBJECT) {
                return null;
            }
            
            if (result == null) {
                result = writer.get(key);
            }
            if (result == null) {
                RepositoryDataInput in = storage.getInputStream(key);
                if (in == null) {
                    return null;
                }
                try{
                    result = key.getPersistentFactory().read(in);
                }catch (IllegalArgumentException ex) {
                    CharSequence unitName = null;
                    try {
                        unitName = storage.getUnitName(key.getUnitId());
                    } catch (Throwable th) {
                        // skip
                    }
                    storage.getInputStream(key);
                    throw new IllegalArgumentException(ex.getMessage()+". Occured for the key: "+key.getClass().getName() + // NOI18N
                            " with unit id: "+key.getUnitId()+" "+unitName+" and behaviour: "+key.getBehavior(), ex); // NOI18N
                }
                if (result == null) {
                    result = REMOVED_OBJECT;
                }
                Persistent old = unitCache.putIfAbsent(key, result);
                if (old != null) {
                    result = old;
                }
            }

            if (REMOVED_OBJECT.equals(result)) {
                return null;
            }

            return result;
        } catch (Throwable th) {
            RepositoryExceptions.throwException(this, key, th);
        }
        return null;
    }

    @Override
    public void put(final Key key, final Persistent obj) {
        try {
            RepositoryCache unitCache = getCache(key.getUnitId());
            lock.lock();
            try {
                if (obj != REMOVED_OBJECT) {
                    unitCache.put(key, obj);
                }
                putImpl(key, obj);
                
                
            } finally {
                lock.unlock();
            }
        } catch (Throwable th) {
            RepositoryExceptions.throwException(this, key, th);
        }
    }

    @Override
    public void removeKey(Key key) {
        RepositoryCache unitCache = getCache(key.getUnitId());
        try {
            lock.lock();
            storage.remove(key);
            unitCache.removePhysically(key);
        } finally {
            lock.unlock();
        }
        
    }
    
    

    @Override
    public void remove(final Key key) {
        //remove from cache
        RepositoryCache unitCache = getCache(key.getUnitId());
        //remove from cache, without lock
        unitCache.remove(key);   
        //put removed object, when it will be written on the disk,
        //remove from disk should be invoked
        //and need to remove from cache
        put(key, REMOVED_OBJECT);        
    }

    @Override
    public void shutdown() {
        storage.flush();
        try {
            writer.flush();
            writer.shutdown();
        } catch (InterruptedException ex) {
            RepositoryExceptions.throwException(this, ex);
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
        storage.shutdown();
    }

    /**
     * A request to persist unit-related information.
     */
    @Override
    public void closeUnit(int unitID, boolean cleanRepository, Set<Integer> requiredUnits) {
        Set<RepositoryCache.Pair<Key, Persistent>> hung = new HashSet<RepositoryCache.Pair<Key, Persistent>>();
        final RepositoryCache cache = getCache(unitID);
        hung.addAll(cache.clearHungObjects());
        for (RepositoryCache.Pair<Key, Persistent> cachePair : hung) {
            putImpl(cachePair.first, cachePair.second);
        }
        try {
            writer.flush(unitID);
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        } catch (InterruptedException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
        cache.clearSoftRefs();
        storage.close(unitID, cleanRepository, requiredUnits);
    }

    @Override
    public void openUnit(int unitID) {
        storage.open(unitID);
    }

    @Override
    public void removeUnit(int unitID) {
        writer.removeUnit(unitID);
        storage.removeUnit(unitID);
    }

    @Override
    public void hang(Key key, Persistent obj) {
        getCache(key.getUnitId()).hang(key, obj);
    }

    @Override
    public void debugDistribution() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void debugDump(Key key) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private RepositoryCache getCache(int unitID) {
        RepositoryCache result = caches.get(unitID);
        if (result == null) {
            result = new RepositoryCache();
            RepositoryCache old = caches.putIfAbsent(unitID, result);
            if (old != null) {
                result = old;
            }
        }
        return result;
    }

    /**
     *
     * @param unitID - 100001
     * @param fileName
     * @return
     */
    @Override
    public int getFileIdByName(int unitID, CharSequence fileName) {
        return storage.getFileIdByName(unitID, fileName);
    }

    /**
     *
     * @param unitID - 100001
     * @param fileID
     * @return
     */
    @Override
    public CharSequence getFileNameById(int unitID, int fileID) {
        return storage.getFileNameByIdx(unitID, fileID);
    }

    @Override
    public CharSequence getFileNameByIdSafe(int unitId, int fileID) {
        return getFileNameById(unitId, fileID);
    }

    @Override
    public CharSequence getUnitName(int unitID) {
        return storage.getUnitName(unitID);
    }

    /**
     * Returns a UnitID of the 'root'-level Unit (described by the
     * {@link UnitDescriptor}).
     * <p/>
     * <i>Implementation notes:</i> <br/> UnitID is an integer that encodes two
     * things - StorageID and In-terms-of-storage-UnitID.<br/> Storage is
     * identified by a set of repository layers associated with the 'root'-level
     * Unit (see
     * {@link org.netbeans.modules.cnd.repository.support.LayerProvider}). <br/>
     * The same 'physical' layer could be shared between several Storages. </p>
     *
     * @param unitDescriptor - descriptor of a Unit to get ID for.
     * @return ID of descriptor - a 'long' ID. i.e. 100001
     * @See LayerProvider
     */
    @Override
    public int getUnitID(UnitDescriptor unitDescriptor) {
        return storage.getUnitID(unitDescriptor);
    }

    /**
     * Returns a UnitID of the 'dependent' Unit (described by UnitDescriptor).
     *
     * @param unitDescriptor - descriptor of a Unit to get ID for.
     * @param storageID - an ID of the 'root' Unit that this Unit relates to.
     * @return ID of descriptor - a 'long' ID. i.e. 100001
     */
    @Override
    public int getUnitID(UnitDescriptor unitDescriptor, int storageID) {
        return storage.getUnitID(unitDescriptor, storageID);
    }

    @Override
    public int getRepositoryID(int sourceUnitId) {
        return storage.getStorageID(sourceUnitId);
    }

    private void putImpl(Key key, Persistent obj) {
        writer.put(key, obj);
    }

    @Override
    public LayeringSupport getLayeringSupport(int clientUnitID) {
        return storage.getLayeringSupport(clientUnitID);
    }
    
    private static final class RemovedObject implements Persistent {

        @Override
        public String toString() {
            return "RemovedObject"; // NOI18N
        }
    }
}
