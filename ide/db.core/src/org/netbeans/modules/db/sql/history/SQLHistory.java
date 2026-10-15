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

package org.netbeans.modules.db.sql.history;

import java.util.*;

public class SQLHistory implements Set<SQLHistoryEntry> {
    // Public methods are synchronized to protect the backing set from concurrent
    // modifications. The performance penality is considered acceptable, as this
    // history logs SQL execution, so execution time is considered far greater
    // than the overhead for synchronisation.
    
    private int historyLimit = 100;
    private Set<SQLHistoryEntry> history;

    public SQLHistory() {
        history = new HashSet<>();
    }

    @Override
    public synchronized String toString() {
        return history.toString();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return history.toArray(a);
    }

    @Override
    public synchronized Object[] toArray() {
        return history.toArray();
    }

    @Override
    public synchronized int size() {
        return history.size();
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return history.retainAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return history.removeAll(c);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return history.remove(o);
    }

    @Override
    public synchronized Iterator<SQLHistoryEntry> iterator() {
        return history.iterator();
    }
     
    @Override
    public synchronized boolean isEmpty() {
        return history.isEmpty();
    }
    
    @Override
    public synchronized int hashCode() {
        return history.hashCode();
    }
    
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public synchronized boolean equals(Object o) {
        return history.equals(o);
    }
    
    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return history.containsAll(c);
    }

    @Override
    public synchronized boolean contains(Object o) {
        return history.contains(o);
    }

    @Override
    public synchronized void clear() {
        history.clear();
    }
     
    @Override
    public synchronized boolean addAll(Collection<? extends SQLHistoryEntry> c) {
        boolean changed = false;
        for(SQLHistoryEntry sqe: c) {
            changed |= this.add(sqe);
        }
        return changed;
    }
    
    @Override
    public synchronized boolean add(SQLHistoryEntry e) {
        boolean result = history.add(e);
        if(! result) {
            history.remove(e);
            result = history.add(e);
        }
        enforceLimit();
        return result;
    }
    
    private void enforceLimit() {
        if(size() > historyLimit) {
            List<SQLHistoryEntry> list = new ArrayList<>(history);
            list.sort(new Comparator<SQLHistoryEntry>() {
                @Override
                public int compare(SQLHistoryEntry o1, SQLHistoryEntry o2) {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });
            history.clear();
            history.addAll(list.subList(0, historyLimit));
        }
    }
    
    public synchronized int getHistoryLimit() {
        return historyLimit;
    }

    public synchronized void setHistoryLimit(int historyLimit) {
        this.historyLimit = historyLimit;
        enforceLimit();
    }
}
