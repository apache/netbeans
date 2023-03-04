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

package org.netbeans.modules.db.metadata.model.jdbc;

import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author David Van Couvering
 */
public abstract class JDBCMetadataTestBase extends MetadataTestBase {
    @Override
    public abstract void setUp() throws Exception;

    public JDBCMetadataTestBase(String name) {
        super(name);
    }

    protected void checkForeignKeyColumn(ForeignKey key, Table referredTable, String referringColName, String referredColName,
            int position) throws Exception {
        ForeignKeyColumn col = key.getColumn(referringColName);
        assertEquals(referredTable.getColumn(referredColName), col.getReferredColumn());
        assertEquals(key.getParent().getColumn(referringColName), col.getReferringColumn());
        assertEquals(position, col.getPosition());
    }
}
