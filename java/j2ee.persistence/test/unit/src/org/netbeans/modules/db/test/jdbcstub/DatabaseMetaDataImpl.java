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

package org.netbeans.modules.db.test.jdbcstub;

import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseMetaDataImpl extends StubDelegate {
    
    public String getURL() {
        return null;
    }
    
    public String getUserName() {
        return null;
    }
    
    public String getDatabaseProductName() {
        return "DBStub";
    }
    
    public String getDatabaseProductVersion() {
        return "1.0";
    }
    
    public String getDriverName() {
        return "DBStub JDBC Driver";
    }
    
    public String getDriverVersion() {
        return "1.0";
    }
}
