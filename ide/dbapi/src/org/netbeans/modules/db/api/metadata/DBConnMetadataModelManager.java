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

package org.netbeans.modules.db.api.metadata;

import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.metadata.MetadataModelManager;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;

/**
 * Provides access to the database model for DB Explorer database connections.
 * This class is temporary, as such access should be provided directly by
 * the DB Explorer through a {@code DatabaseConnection.getMetadataModel()} method.
 *
 * @author Andrei Badea
 */
public class DBConnMetadataModelManager {
    private static final Logger LOGGER = Logger.getLogger(DBConnMetadataModelManager.class.getName());

    private MetadataModelManager mgr;

    private DBConnMetadataModelManager() {}

    public static MetadataModel get(DatabaseConnection dbconn) {
        return MetadataModelManager.get(dbconn);
    }
}
