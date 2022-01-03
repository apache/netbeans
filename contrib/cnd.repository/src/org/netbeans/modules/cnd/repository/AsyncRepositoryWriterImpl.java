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
package org.netbeans.modules.cnd.repository;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static org.netbeans.modules.cnd.repository.RepositoryImpl.REMOVED_OBJECT;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.storage.StorageManager;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package*/ final class AsyncRepositoryWriterImpl implements AsyncRepositoryWriter {

    private static final int ncounters = 0x80;
    private static final int bits = 32;
    private static final int mcounters = ncounters * bits - 1;
    //private static final int lBound = 10000;
    //private static final int hBound = 70000;
    private final RequestProcessor RP = new RequestProcessor("Repository Writing Thread", 1); // NOI18N
    private final Map<Key, Persistent> map_small = new LinkedHashMap<Key, Persistent>(512, 0.75f, true);
    private final Map<Key, Persistent> map_large = new LinkedHashMap<Key, Persistent>(512, 0.75f, true);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition mapIsEmpty = lock.newCondition();
    private final Condition mapIsNotEmpty = lock.newCondition();
    private final Condition writerDone = lock.newCondition();
    private final int[] counters = new int[ncounters];
    private final AtomicBoolean flush = new AtomicBoolean(false);
    private boolean doneFlag;
    private final RemoveKeySupport removeKeySupport;
    private static final int WAIT_TIMEOUT;
    
    static {
        if (CndUtils.isUnitTestMode()) {
            WAIT_TIMEOUT = 10;
        } else {
            WAIT_TIMEOUT = 500;
        }
    }

    public AsyncRepositoryWriterImpl(final StorageManager storage, RemoveKeySupport removeKeySupport) {
        this.removeKeySupport = removeKeySupport;
        RP.scheduleAtFixedRate(new Worker(storage), 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void put(Key key, Persistent value) {
        boolean largeObject = key.getBehavior().equals(Key.Behavior.LargeAndMutable);

        int idx = key.hashCode() & mcounters;
        int idx1 = idx / bits;
        int idx2 = idx % bits;

        lock.lock();
        try {
            assert !doneFlag;
            if (largeObject) {
                // add in end of list
                map_large.remove(key);
                map_large.put(key, value);
            } else {
                // add in end of list
                map_small.remove(key);
                map_small.put(key, value);
            }
            counters[idx1] |= 1 << idx2;
            mapIsNotEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Persistent get(Key key) {
        boolean largeObject = key.getBehavior().equals(Key.Behavior.LargeAndMutable);

        lock.lock();
        try {
            if (largeObject) {
                return map_large.get(key);
            } else {
                return map_small.get(key);
            }
        } finally {
            lock.unlock();
        }
    }
    
    
    @Override
    public void removeUnit(int unitID) {
        lock.lock();
        try {
            for (Iterator<Map.Entry<Key, Persistent>> it = map_small.entrySet().iterator(); it.hasNext();) {
                if (it.next().getKey().getUnitId() == unitID) {
                    it.remove();
                }
            }
            for (Iterator<Map.Entry<Key, Persistent>> it = map_large.entrySet().iterator(); it.hasNext();) {
                if (it.next().getKey().getUnitId() == unitID) {
                    it.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void flush() throws IOException, InterruptedException {
        flush.set(true);
        lock.lock();
        try {
            while (!(map_large.isEmpty() && map_small.isEmpty())) {
                mapIsEmpty.await();
            }
        } finally {
            lock.unlock();
            flush.set(false);
        }
    }

    @Override
    public void flush(int unitID) throws IOException, InterruptedException {
        flush();
    }

    @Override
    public void shutdown() {        
        lock.lock();        
        try {
            if (doneFlag) {
                return;
            }
            
            doneFlag = true;
            try {
                flush();
            } catch (Exception ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            mapIsNotEmpty.signalAll();
            try {
                writerDone.await();
            } catch (InterruptedException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
        } finally {
            RP.shutdown();
            lock.unlock();
        }
    }

    private class Worker implements Runnable {

        private final StorageManager storage;

        public Worker(StorageManager storage) {
            this.storage = storage;
        }

        @Override
        public void run() {
            Key key;
            Persistent value = null;
            int idx = -1;
            int idx1 = -1;
            int idx2 = -1;
            boolean largeObject = false;
            boolean draining = false;
            int workSize = map_large.size() + map_small.size();
            while (true) {
                key = null;
                if (flush.get()) {
                    draining = true;
                } else {
                    if (workSize-- <= 0) {
                        workSize = map_large.size() + map_small.size();
                        draining = false;
                    } else {
                        draining = true;
                    }
                    //int size = map_large.size() + map_small.size();
                    //if (size > hBound) {
                    //    draining = true;
                    //} else if (size < lBound) {
                    //    draining = false;
                    //}
                }

                if (!draining) {
                    try {
                        Thread.sleep(WAIT_TIMEOUT);
                    } catch (InterruptedException ex) {
                        RepositoryExceptions.throwException(this, ex);
                    }
                }

                lock.lock();

                try {
                    if (map_large.isEmpty() && map_small.isEmpty()) {
                        mapIsEmpty.signalAll();
                        if (!doneFlag) {
                            waitReady();                            
                        } else {
                            writerDone.signalAll();
                            break;
                        }
                    }
                    maintenanceIsNeeded = true;
                    Iterator<Map.Entry<Key, Persistent>> it = map_small.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Key, Persistent> entry = it.next();
                        key = entry.getKey();
                        idx = key.hashCode() & mcounters;
                        idx1 = idx / bits;
                        idx2 = idx % bits;
                        if ((counters[idx1] & (1<<idx2)) != 0) {
                            counters[idx1] &= ~(1<<idx2);
                            key = null;
                            continue;
                        }
                        value = entry.getValue();
                        largeObject = false;
                        break;
                    }
                    if (key == null) {
                        it = map_large.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<Key, Persistent> entry = it.next();
                            key = entry.getKey();
                            idx = key.hashCode() & mcounters;
                            idx1 = idx / bits;
                            idx2 = idx % bits;
                            if ((counters[idx1] & (1<<idx2)) != 0) {
                                counters[idx1] &= ~(1<<idx2);
                                key = null;
                                continue;
                            }
                            value = entry.getValue();
                            largeObject = true;
                            break;
                        }
                    }
                } catch (Throwable th) {
                    RepositoryExceptions.throwException(this, th);
                } finally {
                    lock.unlock();
                }

                if (key != null && value != null) {
                    doWrite(key, value);

                    lock.lock();
                    try {
                        if ((counters[idx1] & (1<<idx2)) == 0) {
                            if (largeObject) {
                                map_large.remove(key);
                            } else {
                                map_small.remove(key);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }

        private void doWrite(Key key, Persistent value) {
            try {
                if (REMOVED_OBJECT.equals(value)) {
                    //it is time to remove from cache
                    removeKeySupport.removeKey(key);
                    return;
                }
            } catch (Throwable ex) {
                RepositoryExceptions.throwException(this, key, ex);
            }
            RepositoryDataOutput out = null;
            try {
                out = storage.getOutputStream(key);
                assert out != null;
                key.getPersistentFactory().write(out, REMOVED_OBJECT.equals(value) ? null : value);
            } catch (Throwable ex) {
                RepositoryExceptions.throwException(this, key, ex);
                out = null;
            } finally {
                try{
                    if (out != null) {
                        out.commit();
                    }
                } catch (Throwable ex) {
                    RepositoryExceptions.throwException(this, key, ex);
                }
            }
        }
        private boolean maintenanceIsNeeded = true;
        /**
         * while waiting map is not empty run maintainer 
         * @throws InterruptedException 
         */
        private void waitReady() throws InterruptedException {
            if( Stats.maintenanceInterval > 0 ) {
                while (true) {
                    if( Stats.allowMaintenance && maintenanceIsNeeded) {
                        lock.lock();
                        try {

                           if (!map_large.isEmpty() || !map_small.isEmpty()) {
                               return;
                           }
                            //try to invoke maintainence
                            maintenanceIsNeeded = storage.maintenance(Stats.maintenanceInterval);
                        } finally {
                            lock.unlock();
                        }                    
                    } else {
                        break;
                    }
                    if (!map_large.isEmpty() || !map_small.isEmpty()) {
                        return;
                    }              
                }                
                mapIsNotEmpty.await();
            } else {
                if (!map_large.isEmpty() || !map_small.isEmpty()) {
                     return;
                 }                
                mapIsNotEmpty.await();
            }
            
        }
    }
}
