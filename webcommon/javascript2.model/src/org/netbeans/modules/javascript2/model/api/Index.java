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
package org.netbeans.modules.javascript2.model.api;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.model.spi.IndexChangeSupport;
import org.netbeans.modules.javascript2.model.spi.QuerySupportFactory;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public final class Index {

    public static final String FIELD_BASE_NAME = "bn"; //NOI18N
    /**
     * The same as FIELD_BASE_NAME, but case insensitive. 
     */
    public static final String FIELD_BASE_NAME_INSENSITIVE = "bni"; // NOI18N
    /**
     * In this field is in the lucene also coded, whether the object is anonymous (last char is 'A')
     * or normal object (last char is 'O'). If someone needs to access this field
     * directly, then has to be count with this.
     */
    public static final String FIELD_FQ_NAME = "fqn"; //NOI18N
    public static final String FIELD_OFFSET = "offset"; //NOI18N
    public static final String FIELD_ASSIGNMENTS = "assign"; //NOI18N
    public static final String FIELD_RETURN_TYPES = "return"; //NOI18N
    public static final String FIELD_PARAMETERS = "param"; //NOI18N
    public static final String FIELD_FLAG = "flag"; //NOI18N
    public static final String FIELD_ARRAY_TYPES = "array"; //NOI18N
    public static final String FIELD_USAGE = "usage"; //NOI18N

    private static final String PROPERTIES_PATTERN = "\\.[^\\.]*[^" + IndexedElement.PARAMETER_POSTFIX + "]";
    
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_MUTABLE_ARRAY")
    public static final String[] TERMS_BASIC_INFO = new String[] { FIELD_BASE_NAME, FIELD_FQ_NAME, FIELD_OFFSET,
        FIELD_RETURN_TYPES, FIELD_PARAMETERS, FIELD_FLAG, FIELD_ASSIGNMENTS, FIELD_ARRAY_TYPES};

    private static final Logger LOG = Logger.getLogger(Index.class.getName());

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final Lock READ_LOCK = LOCK.readLock();

    private static final Lock WRITE_LOCK = LOCK.writeLock();

    private static final WeakHashMap<FileObject, Index> INDEX_CACHE = new WeakHashMap<FileObject, Index>();

    // empirical values (update if index is changed)
    private static final int MAX_ENTRIES_CACHE_INDEX_RESULT = 2000;

    private static final int MAX_CACHE_VALUE_SIZE = 1000000;

    private static final int AVERAGE_BASIC_INFO_SIZE = 60;

    // cache to keep latest index results. The cache is cleaned if a file is saved
    // or a file has to be reindexed due to an external change

    /* GuardedBy(LOCK) */
    private static final Map<CacheKey, SoftReference<CacheValue>> CACHE_INDEX_RESULT_SMALL = new LinkedHashMap<CacheKey, SoftReference<CacheValue>>(
            MAX_ENTRIES_CACHE_INDEX_RESULT + 1, 0.75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES_CACHE_INDEX_RESULT;
        }
    };

    /* GuardedBy(LOCK) */
    private static final Map<CacheKey, SoftReference<CacheValue>> CACHE_INDEX_RESULT_LARGE = new LinkedHashMap<CacheKey, SoftReference<CacheValue>>(
            (MAX_ENTRIES_CACHE_INDEX_RESULT / 4) + 1, 0.75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > (MAX_ENTRIES_CACHE_INDEX_RESULT / 4);
        }
    };

    private static final Map<StatsKey, StatsValue> QUERY_STATS = new HashMap<StatsKey, StatsValue>();
    
    private static final ChangeListener INVALIDATE_LISTENER = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            WRITE_LOCK.lock();
            try {
                CACHE_INDEX_RESULT_SMALL.clear();
                CACHE_INDEX_RESULT_LARGE.clear();
                INDEX_CACHE.clear();
                LOG.log(Level.FINEST, "Cache cleared");
            } finally {
                WRITE_LOCK.unlock();
            }
        }
    };
    
    static {
        // FIXME listen for lookup changes ?
        IndexChangeSupport changeSupport = Lookup.getDefault().lookup(IndexChangeSupport.class);
        if (changeSupport != null) {
            changeSupport.addChangeListener(INVALIDATE_LISTENER);
        }
    }

    /* GuardedBy(QUERY_STATS) */
    private static int cacheHit;

    /* GuardedBy(QUERY_STATS) */
    private static int cacheMiss;

    private final QuerySupport querySupport;

    private final boolean updateCache;

    private Index(QuerySupport querySupport, boolean updateCache) {
        this.querySupport = querySupport;
        this.updateCache = updateCache;
    }

    public static Index get(Collection<FileObject> roots) {
        // XXX no cache - is it needed?
        LOG.log(Level.FINE, "JsIndex for roots: {0}", roots); //NOI18N
        QuerySupportFactory f = Lookup.getDefault().lookup(QuerySupportFactory.class);
        return new Index(f != null ? f.get(roots) : null, false);
    }

    public static Index get(FileObject fo) {
        Index index = INDEX_CACHE.get(fo);
        if (index == null) {
            LOG.log(Level.FINE, "Creating JsIndex for FileObject: {0}", fo); //NOI18N
            QuerySupportFactory f = Lookup.getDefault().lookup(QuerySupportFactory.class);
            index = new Index(f != null
                    ? f.get(QuerySupport.findRoots(fo, null, null, Collections.<String>emptySet()))
                    : null, true);
            INDEX_CACHE.put(fo, index);
        }
        return index;
    }

    public Collection<? extends IndexResult> query(final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad) {

        if (querySupport == null) {
            return Collections.<IndexResult>emptySet();
        }

        try {
            
            CacheKey key = new CacheKey(this, fieldName, fieldValue, kind);
            CacheValue value = getCachedValue(key, fieldsToLoad);

            if (value != null) {
                logStats(value.getResult(), true, fieldsToLoad);
                return value.getResult();
            }

            Collection<? extends IndexResult> result = querySupport.query(
                    fieldName, fieldValue, kind, fieldsToLoad);
            
            
            if (updateCache) {
                WRITE_LOCK.lock();
                try {
                    value = getCachedValue(key, fieldsToLoad);
                    if (value != null) {
                        logStats(value.getResult(), false, fieldsToLoad);
                        return value.getResult();
                    }

                    value = new CacheValue(fieldsToLoad, result);
                    if ((result.size() * AVERAGE_BASIC_INFO_SIZE) < MAX_CACHE_VALUE_SIZE) {
                        CACHE_INDEX_RESULT_SMALL.put(key, new SoftReference<>(value));
                    } else {
                        CACHE_INDEX_RESULT_LARGE.put(key, new SoftReference<>(value));
                    }
                    logStats(result, false, fieldsToLoad);
                    return value.getResult();
                } finally {
                    WRITE_LOCK.unlock();
                }
            }

            logStats(result, false, fieldsToLoad);
            return result;
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }

        return Collections.<IndexResult>emptySet();
    }

    public Collection<IndexedElement> getGlobalVar(String prefix) {
        prefix = prefix == null ? "" : prefix; //NOI18N
        ArrayList<IndexedElement> globals = new ArrayList<IndexedElement>();
        long start = System.currentTimeMillis();
        String indexPrefix = escapeRegExp(prefix) + "[^\\.]*[" + IndexedElement.OBJECT_POSFIX + "]";   //NOI18N
        Collection<? extends IndexResult> globalObjects = query(Index.FIELD_FQ_NAME, indexPrefix, QuerySupport.Kind.REGEXP, TERMS_BASIC_INFO); //NOI18N
        for (IndexResult indexResult : globalObjects) {
            IndexedElement indexedElement = IndexedElement.create(indexResult);
            globals.add(indexedElement);
        }
        long end = System.currentTimeMillis();
        LOG.log(Level.FINE, "Obtaining globals from the index took: {0}", (end - start)); //NOI18N
        return globals;
    }

    private static CacheValue getCachedValue(CacheKey key, String... fieldsToLoad) {
        READ_LOCK.lock();
        try {
            CacheValue value = null;
            SoftReference<CacheValue> currentReference = CACHE_INDEX_RESULT_SMALL.get(key);
            if (currentReference != null) {
                value = currentReference.get();
            }
            if (value == null || !value.contains(fieldsToLoad)) {
                currentReference = CACHE_INDEX_RESULT_LARGE.get(key);
                if (currentReference != null) {
                    value = currentReference.get();
                }
                if (value == null || !value.contains(fieldsToLoad)) {
                    return null;
                } else {
                    return value;
                }
            } else {
                return value;
            }
        } finally {
            READ_LOCK.unlock();
        }
    }

    private static void logStats(Collection<? extends IndexResult> result, boolean hit, String... fieldsToLoad) {
        if (!LOG.isLoggable(Level.FINEST)) {
            return;
        }
        int size = 0;
        for (String field : fieldsToLoad) {
            for (IndexResult r : result) {
                String val = r.getValue(field);
                size += val == null ? 0 : val.length();
            }
        }

        synchronized (QUERY_STATS) {
            if (hit) {
                cacheHit++;
            } else {
                cacheMiss++;
            }

            StatsKey statsKey = new StatsKey(fieldsToLoad);
            StatsValue statsValue = QUERY_STATS.get(statsKey);
            if (statsValue == null) {
                QUERY_STATS.put(statsKey,
                        new StatsValue(1, result.size(), size));
            } else {
                QUERY_STATS.put(statsKey,
                        new StatsValue(statsValue.getRequests() + 1,
                            statsValue.getCount() + result.size(), statsValue.getSize() + size));
            }

            if ((cacheHit + cacheMiss) % 500 == 0) {
                LOG.log(Level.FINEST, "Cache hit: " + cacheHit + ", Cache miss: "
                        + cacheMiss + ", Ratio: " + (cacheHit  / cacheMiss));
                for (Map.Entry<StatsKey, StatsValue> entry : QUERY_STATS.entrySet()) {
                    LOG.log(Level.FINEST, entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    private static Collection<IndexedElement> getElementsByPrefix(String prefix, Collection<IndexedElement> items) {
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        for (IndexedElement indexedElement : items) {
            if (indexedElement.getName().startsWith(prefix)) {
                result.add(indexedElement);
            }
        }
        return result;
    }

    public Collection <IndexedElement> getPropertiesWithPrefix(String fqn, String prexif) {
        return getElementsByPrefix(prexif, getProperties(fqn));
    }

    
    public Collection <IndexedElement> getProperties(String fqn) {
        return getProperties(fqn, 0, new ArrayList<String>());
    }
    
    public Collection <IndexedElement> getUsagesFromExpression(final List<String> expChain) {
        if (expChain == null || expChain.isEmpty()) {
            return Collections.emptyList();
        }
        String searchText = expChain.get(0) + ':';
        Collection<? extends IndexResult> results = query(Index.FIELD_USAGE, searchText, QuerySupport.Kind.PREFIX, Index.FIELD_USAGE); //NOI18N
        ArrayList<IndexedElement> usages = new ArrayList<IndexedElement>();
        Set<String> alreadyUsed = new HashSet<String>();
        for (IndexResult indexResult : results) {
            String[] fields = indexResult.getValues(Index.FIELD_USAGE);
            FileObject fo = indexResult.getFile();
            for (String field : fields) {
                if (field.startsWith(searchText)) {
                    String[] parts = field.split(":");
                    for (String property : parts) {
                        String[] split = property.split("#");
                        if(split.length == 2 && !alreadyUsed.contains(split[0])) {
                            alreadyUsed.add(split[0]);
                            IndexedElement element = new IndexedElement(fo, split[0], split[0], false, false, split[1].equals("F") ? JsElement.Kind.FUNCTION : JsElement.Kind.OBJECT, 
                                OffsetRange.NONE, Collections.singleton(Modifier.PUBLIC), Collections.emptyList(), false);
                            usages.add(element);
                        }
                    }
                    
                }
            }
        }
        return usages;
    }
            

    private final int MAX_FIND_PROPERTIES_RECURSION = 15;
    
    private Collection <IndexedElement> getProperties(String fqn, int deepLevel, Collection<String> resolvedTypes) { 
        if (deepLevel > MAX_FIND_PROPERTIES_RECURSION) {
            return Collections.emptyList();
        }
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        if (!resolvedTypes.contains(fqn)) {
            resolvedTypes.add(fqn);
            deepLevel = deepLevel + 1;
            Collection<? extends IndexResult> results = findByFqn(fqn, Index.FIELD_ASSIGNMENTS);
            for (IndexResult indexResult : results) {
                // find assignment to for the fqn
                Collection<TypeUsage> assignments = IndexedElement.getAssignments(indexResult);
                if (!assignments.isEmpty()) {
                    TypeUsage type = assignments.iterator().next();
                    if (!resolvedTypes.contains(type.getType())) {                    
                        result.addAll(getProperties(type.getType(), deepLevel, resolvedTypes));
                    }
                }
            }
            // find properties of the fqn
//            String pattern = escapeRegExp(fqn) + PROPERTIES_PATTERN; //NOI18N
            Hashtable<String, Collection<? extends IndexResult>> fqnToResults = new Hashtable<String, Collection<? extends IndexResult>>();
            fqnToResults.put(fqn, query(Index.FIELD_FQ_NAME, fqn + ".", QuerySupport.Kind.PREFIX, TERMS_BASIC_INFO)); //NOI18N
            if (fqn.indexOf('.') == -1) {
                Collection<? extends IndexResult> tmpResults = query(Index.FIELD_BASE_NAME, fqn, QuerySupport.Kind.EXACT, Index.FIELD_FQ_NAME);
                for (IndexResult indexResult : tmpResults) {
                    String value = IndexedElement.getFQN(indexResult);
                    fqnToResults.put(value, query(Index.FIELD_FQ_NAME, value + ".", QuerySupport.Kind.PREFIX, TERMS_BASIC_INFO)); //NOI18N 
                }
            }
            for (Map.Entry<String, Collection<? extends IndexResult>> entry : fqnToResults.entrySet()) {
                String fqnKey = entry.getKey();
                Collection<? extends IndexResult> properties = entry.getValue();
                for (IndexResult indexResult : properties) {
                    String value = indexResult.getValue(Index.FIELD_FQ_NAME);
                    if (!value.isEmpty() && value.charAt(value.length() - 1) != IndexedElement.PARAMETER_POSTFIX) {
                        value = value.substring(fqnKey.length());
                        if (value.lastIndexOf('.') == 0) {
                            IndexedElement property = IndexedElement.create(indexResult);
                            if (!property.getModifiers().contains(Modifier.PRIVATE)) {
                                result.add(property);
                            }
                        }
                    }
                }
            }

        }
        return result;
    }

    public Collection<? extends IndexResult> findByFqn(String fqn, String... fields) {
        Collection<IndexResult> results = new ArrayList<IndexResult>();
        results.addAll(query(Index.FIELD_FQ_NAME, fqn + IndexedElement.ANONYMOUS_POSFIX, QuerySupport.Kind.EXACT, fields)); //NOI18N
        results.addAll(query(Index.FIELD_FQ_NAME, fqn + IndexedElement.OBJECT_POSFIX, QuerySupport.Kind.EXACT, fields)); //NOI18N
        results.addAll(query(Index.FIELD_FQ_NAME, fqn + IndexedElement.PARAMETER_POSTFIX, QuerySupport.Kind.EXACT, fields)); //NOI18N
        return results;
    }
    
    private String escapeRegExp(String text) {
        return Pattern.quote(text);
    }

    private static class CacheKey {

        private final Index index;

        private final String fieldName;

        private final String fieldValue;

        private final QuerySupport.Kind kind;

        public CacheKey(Index index, String fieldName, String fieldValue, QuerySupport.Kind kind) {
            this.index = index;
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.kind = kind;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.index != null ? this.index.hashCode() : 0);
            hash = 41 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
            hash = 41 * hash + (this.fieldValue != null ? this.fieldValue.hashCode() : 0);
            hash = 41 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if (this.index != other.index && (this.index == null || !this.index.equals(other.index))) {
                return false;
            }
            if ((this.fieldName == null) ? (other.fieldName != null) : !this.fieldName.equals(other.fieldName)) {
                return false;
            }
            if ((this.fieldValue == null) ? (other.fieldValue != null) : !this.fieldValue.equals(other.fieldValue)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "CacheKey{" + "index=" + index + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", kind=" + kind + '}';
        }
    }

    private static class CacheValue {

        private final Set<String> fields;

        private final Collection<? extends IndexResult> result;

        public CacheValue(String[] fields, Collection<? extends IndexResult> result) {
            this.fields = new HashSet<String>(Arrays.asList(fields));
            this.result = result;
        }

        public Collection<? extends IndexResult> getResult() {
            return result;
        }

        public boolean contains(String... fieldsToLoad) {
            return fields.containsAll(Arrays.asList(fieldsToLoad));
        }
    }

    private static class StatsKey {

        private final String[] fields;

        public StatsKey(String[] fields) {
            this.fields = fields.clone();
            Arrays.sort(this.fields);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.fields);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StatsKey other = (StatsKey) obj;
            if (!Arrays.deepEquals(this.fields, other.fields)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return Arrays.deepToString(fields);
        }
    }

    private static class StatsValue {

        private final int requests;

        private final int count;

        private final long size;

        public StatsValue(int requests, int count, long size) {
            this.requests = requests;
            this.count = count;
            this.size = size;
        }

        public int getRequests() {
            return requests;
        }

        public int getCount() {
            return count;
        }

        public long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "StatsValue{" + "requests=" + requests + ", average=" + (count != 0 ? (size / count) : 0)
                    + ", count=" + count + ", size=" + size + '}';
        }

    }
}
