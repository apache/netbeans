/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
