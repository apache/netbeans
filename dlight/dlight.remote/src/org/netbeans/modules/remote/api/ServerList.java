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

package org.netbeans.modules.remote.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.support.ServerListImpl;
import org.openide.util.Utilities;

/**
 * Stores the list of hosts;
 * each host is represented by ServerRecord instance
 */
public final class ServerList {

    public static final String PROP_DEFAULT_RECORD = "DEFAULT_RECORD"; //NOI18N
    public static final String PROP_RECORD_LIST = "RECORD_LIST"; //NOI18N

    public static Collection<ServerRecord> getRecords() {
        return ServerListImpl.getDefault().getRecords();
    }

    public static ServerRecord get(ExecutionEnvironment env) {
        return ServerListImpl.getDefault().get(env);
    }

    public static ServerRecord getDefaultRecord() {
        return ServerListImpl.getDefault().getDefaultRecord();
    }

    public static void setDefaultRecord(ServerRecord defaultRecord) {
        ServerListImpl.getDefault().setDefaultRecord(defaultRecord);
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        ServerListImpl.getDefault().addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        ServerListImpl.getDefault().removePropertyChangeListener(listener);
    }

    /**
     * Gets record that is currently selected in the UI
     * (e.g. in explorer that shows servers),
     * or default record if nothing is currently selected
     */
    public static ServerRecord getActiveRecord() {
        ServerRecord record = Utilities.actionsGlobalContext().lookup(ServerRecord.class);
        if (record == null) {
            record = getDefaultRecord();
        }
        return record;
    }

    private ServerList() {
    }
}
