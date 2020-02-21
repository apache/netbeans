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
package org.netbeans.modules.cnd.indexing.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class DiskTextIndexLayer implements TextIndexLayer {
    private static final boolean CHECK_KEY;
    static {
        boolean check;
        if (System.getProperty("org.netbeans.modules.cnd.indexing.check.key") != null) {// NOI18N
            check = Boolean.getBoolean("org.netbeans.modules.cnd.indexing.check.key");// NOI18N
        } else {
            boolean debug = false;
            assert debug = true;
            check = debug;
        }
        CHECK_KEY = check;
    }
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final static Logger LOG = Logger.getLogger("CndTextIndexImpl"); // NOI18N
    private final DocumentIndex index;
    private final ConcurrentLinkedQueue<StoreQueueEntry> unsavedQueue = new ConcurrentLinkedQueue<StoreQueueEntry>();
    private static final RequestProcessor RP = new RequestProcessor("CndTextIndexImpl saver", 1); //NOI18N
    private static final int STORE_DELAY = 3000;
    private final ScheduledFuture<?> scheduleAtFixedRate = RP.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
            store();
        }
    }, STORE_DELAY, STORE_DELAY, TimeUnit.MILLISECONDS);
    private final LayerDescriptor layerDescriptor;

    public DiskTextIndexLayer(LayerDescriptor layerDescriptor, DocumentIndex index) {
        this.layerDescriptor = layerDescriptor;
        this.index = index;
    }

    @Override
    public void put(CndTextIndexKey indexKey, Set<CharSequence> ids) {
        if (closed.get()) {
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            if (indexKey.getFileNameIndex() < 2) {
                LOG.log(Level.FINE, "Cnd Text Index put for {0}:\n\t{1}", new Object[]{indexKey, ids});
            } else {
                LOG.log(Level.FINE, "Cnd Text Index put for {0}:{1}", new Object[]{indexKey, ids.size()});
            }
        }
        unsavedQueue.add(new StoreQueueEntry(indexKey, ids));
    }

    @Override
    public Collection<CndTextIndexKey> query(final CharSequence text) {
        // force store
        store();

        try {
            // load light weight document with primary key field _sn only
            // it's enough to restore CndTextIndexKey, but reduces memory by not loading FIELD_IDS set
            Collection<? extends IndexDocument> queryRes = index.query(TextIndexStorageManager.FIELD_IDS, text.toString(), Queries.QueryKind.EXACT, "_sn"); // NOI18N
            HashSet<CndTextIndexKey> res = new HashSet<CndTextIndexKey>(queryRes.size());
            for (IndexDocument doc : queryRes) {
                res.add(fromPrimaryKey(doc.getPrimaryKey()));
            }
            LOG.log(Level.FINE, "Cnd Text Index query for {0}:\n\t{1}", new Object[]{text, res});
            return res;
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return Collections.<CndTextIndexKey>emptyList();
    }

    @Override
    public void remove(CndTextIndexKey indexKey) {
        put(indexKey, Collections.<CharSequence>emptySet());
    }

    @Override
    public LayerDescriptor getDescriptor() {
        return layerDescriptor;
    }
            
    @Override
    public boolean isValid() {
        try {
            return index.getStatus() != Index.Status.INVALID;
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        return false;
    }    

    @Override
    public synchronized void unitRemoved(int unitId) {
         if (unitId < 0) {
            return;
        }
        try {
            String unitPrefix = toPrimaryKeyPrefix(unitId);
            Collection<? extends IndexDocument> queryRes = index.query(TextIndexStorageManager.FIELD_UNIT_ID, unitPrefix, Queries.QueryKind.EXACT, "_sn"); // NOI18N
            TreeSet<String> keys = new TreeSet<String>();
            for (IndexDocument doc : queryRes) {
                keys.add(doc.getPrimaryKey());
            }
            for (String pk : keys) {
                index.removeDocument(pk);
            }
            index.store(false);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException ex) {
            // don't report interrupted exception
        }      
    }    
    
    @Override
    public void shutdown() {
        closed.set(true);
        store();
        scheduleAtFixedRate.cancel(false);
    }

    private static class StoreQueueEntry {

        private final CndTextIndexKey key;
        private final Collection<CharSequence> ids;

        public StoreQueueEntry(CndTextIndexKey key, Collection<CharSequence> ids) {
            this.key = key;
            this.ids = ids;
        }
    }

    synchronized void store() {
        if (unsavedQueue.isEmpty() || closed.get()) {
            return;
        }
        long start = System.currentTimeMillis();
        StoreQueueEntry entry = unsavedQueue.poll();
        while (entry != null) {
            final CndTextIndexKey key = entry.key;
            // use unitID+fileID for primary key, otherwise indexed files from different projects overwrite each others
            IndexDocument doc = IndexManager.createDocument(toPrimaryKey(key));
            for (CharSequence id : entry.ids) {
                doc.addPair(TextIndexStorageManager.FIELD_IDS, id.toString(), true, false);
            }
            index.addDocument(doc);
            entry = unsavedQueue.poll();
        }
        try {
            index.store(false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (closed.get()) {
            try {
                index.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        LOG.log(Level.FINE, "Cnd Text Index store took {0}ms", System.currentTimeMillis() - start);
    }
    
    private String toPrimaryKeyPrefix(int unitId) {
        //return String.valueOf(unitCodec.unmaskRepositoryID(unitId));
        return String.valueOf(((long) (unitId) << 32));
    }    

    private String toPrimaryKey(CndTextIndexKey key) {
//        return String.valueOf(((long) unitCodec.unmaskRepositoryID(key.getUnitId()) << 32) + (long) key.getFileNameIndex());
        String result = String.valueOf(((long) (key.getUnitId()) << 32) + (long) key.getFileNameIndex());
        if (CHECK_KEY) {
            CndTextIndexKey fromPrimaryKey = fromPrimaryKey(result);
//            System.out.println("fromKey=" + fromPrimaryKey + " originalKey:" + key);
            assert fromPrimaryKey.getUnitId() == key.getUnitId() && fromPrimaryKey.getFileNameIndex() == key.getFileNameIndex();
        }
        return result;
    }

    private CndTextIndexKey fromPrimaryKey(String ext) {
        long value = Long.parseLong(ext);
        int unitId = (int) (value >> 32);
//        unitId = unitCodec.maskByRepositoryID(unitId);
        int fileNameIndex = (int) (value & 0xFFFFFFFF);
        return new CndTextIndexKey(unitId, fileNameIndex);
    }
}