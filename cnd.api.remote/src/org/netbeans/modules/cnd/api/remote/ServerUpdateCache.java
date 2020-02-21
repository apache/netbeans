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

package org.netbeans.modules.cnd.api.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Since the ServerList is updated from the Tools->Options panel, changes must be cached
 * until the OK button is pressed (T->O updates aren't immediately applied).
 * 
 */
public final class ServerUpdateCache {

    private List<ServerRecord> hosts;
    private ServerRecord defaultRecord;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public ServerUpdateCache() {
        hosts = null;
        defaultRecord = null;
    }
    
    public synchronized List<ServerRecord> getHosts() {
        List<ServerRecord> h = hosts;
        if (h == null) {
            throw new IllegalStateException("hosts should not be null"); //NOI18N
        }
        return new ArrayList<ServerRecord>(hosts);
    }

    public synchronized void setHosts(Collection<? extends ServerRecord> newHosts) {
        hosts = new ArrayList<ServerRecord>(newHosts);
        fixDefaultRecordIfNeed();
    }

    private void fixDefaultRecordIfNeed() {
        if (defaultRecord == null || !hosts.contains(defaultRecord)) {
            if (!hosts.isEmpty()) {
                defaultRecord = hosts.get(0);
            }
        }
    }

    public synchronized ServerRecord getDefaultRecord() {
        fixDefaultRecordIfNeed();
        return defaultRecord;
    }

    public synchronized void setDefaultRecord(ServerRecord record) {
        assert hosts.contains(record);
        defaultRecord = record;
    }
}
