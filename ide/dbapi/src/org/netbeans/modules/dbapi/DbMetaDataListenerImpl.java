/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.dbapi;

import java.util.Iterator;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.explorer.MetaDataListener;
import org.netbeans.modules.db.explorer.DbMetaDataListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.db.explorer.DbMetaDataListener.class)
public class DbMetaDataListenerImpl implements DbMetaDataListener {

    /** 
     * Not private because used in the tests.
     */
    static final String REFRESH_LISTENERS_PATH = "Databases/MetaDataListeners"; // NOI18N

    private final Lookup.Result listeners = getListeners();

    public void tablesChanged(DatabaseConnection dbconn) {
        for (Iterator<MetaDataListener> i = listeners.allInstances().iterator(); i.hasNext();) {
            i.next().tablesChanged(dbconn);
        }
    }

    public void tableChanged(DatabaseConnection dbconn, String tableName) {
        for (Iterator<MetaDataListener> i = listeners.allInstances().iterator(); i.hasNext();) {
            i.next().tableChanged(dbconn, tableName);
        }
    }

    private static Lookup.Result getListeners() {
        return Lookups.forPath(REFRESH_LISTENERS_PATH).lookupResult(MetaDataListener.class);
    }
}
