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

package org.netbeans.modules.db.metadata.model.jdbc.mysql;

import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;


/**
 *
 * @author Andrei Badea
 */
public class MySQLCatalog extends JDBCCatalog {

    private static final Logger LOGGER = Logger.getLogger(MySQLCatalog.class.getName());

    public MySQLCatalog(MySQLMetadata metadata, String name, boolean _default, String defaultSchemaName) {
        super(metadata, name, _default, defaultSchemaName);
    }

    @Override
    public String toString() {
        return "MySQLCatalog[name='" + getName() + "']"; // NOI18N
    }

    @Override
    protected JDBCSchema createJDBCSchema(String name, boolean _default, boolean synthetic) {
        return new MySQLSchema(this, name, _default, synthetic);
    }

}
