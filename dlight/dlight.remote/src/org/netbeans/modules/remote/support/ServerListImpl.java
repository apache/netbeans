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

package org.netbeans.modules.remote.support;

import org.netbeans.modules.remote.api.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Stores the list of hosts;
 * each host is represented by ServerRecord instance
 */
public final class ServerListImpl {

    private static ServerListImpl instance = new ServerListImpl();

    private final static List<WeakReference<PropertyChangeListener>> listeners =
            new ArrayList<WeakReference<PropertyChangeListener>>();

    private final List<ServerRecord> records;
    private ServerRecord defaultRecord;
    private final Object lock = new Object();

    public static ServerListImpl getDefault() {
        return instance;
    }

    /** Just prevents external creation */
    private ServerListImpl() {
        records = new ArrayList<ServerRecord>();
        // TODO: restore from persistence
        defaultRecord = null;
    }

    public Collection<ServerRecord> getRecords() {
        synchronized (lock) {
            return Collections.unmodifiableCollection(new ArrayList<ServerRecord>(records));
        }
    }

    public ServerRecord get(ExecutionEnvironment env) {
        synchronized (lock) {
            for (ServerRecord record : records) {
                if (record.getExecutionEnvironment().equals(env)) {
                    return record;
                }
            }
        }
        return null;
    }

    public ServerRecord getDefaultRecord() {
        return defaultRecord;
    }

    public void setDefaultRecord(ServerRecord newDefaultRecord) {
        ServerRecord oldDefaultRecord = null;
        boolean fire = false;
        synchronized (lock) {
            ServerRecord found = get(newDefaultRecord.getExecutionEnvironment());
            RemoteLogger.assertTrue(found != null, "the record to be set as default is absent in the list"); //NOI18N
            if (found != null) {
                newDefaultRecord = found;
                oldDefaultRecord = this.defaultRecord;
                this.defaultRecord = newDefaultRecord;
            }
        }
        if (fire) {
            firePropertyChange(ServerList.PROP_DEFAULT_RECORD, oldDefaultRecord, newDefaultRecord);
        }
    }

    public void adRecord(ServerRecord record) {
        ServerRecord found = get(record.getExecutionEnvironment());
        RemoteLogger.assertTrue(found == null, "the record to be added already exists in the list"); //NOI18N
        List<ServerRecord> oldRecords, newRecords;
        synchronized (lock) {
            oldRecords = Collections.unmodifiableList(new ArrayList<ServerRecord>(records));
            records.add(record);
            newRecords = Collections.unmodifiableList(new ArrayList<ServerRecord>(records));
        }
        firePropertyChange(ServerList.PROP_RECORD_LIST, oldRecords, newRecords);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                for (WeakReference<PropertyChangeListener> ref : listeners) {
                    if (listener.equals(ref.get())) {
                        return;
                    }
                }
                listeners.add(new WeakReference<PropertyChangeListener>(listener));
            }
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                for (WeakReference<PropertyChangeListener> ref : listeners) {
                    if (listener.equals(ref.get())) {
                        listeners.remove(ref);
                        return;
                    }
                }
            }
        }
    }

    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        List<PropertyChangeListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<PropertyChangeListener>(listeners.size());
            for (WeakReference<PropertyChangeListener> ref : listeners) {
                PropertyChangeListener listener = ref.get();
                if (listener != null) {
                    listenersCopy.add(listener);
                }
            }
        }
        for (PropertyChangeListener listener : listenersCopy) {
            listener.propertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
        }
    }
}
