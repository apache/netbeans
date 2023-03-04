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

package org.netbeans.modules.dbschema.jdbcimpl;

import org.netbeans.modules.dbschema.*;

public class UniqueKeyElementImpl extends KeyElementImpl implements UniqueKeyElement.Impl {

    private boolean _primary;

    /** Creates new UniqueKeyElementImpl */
    public UniqueKeyElementImpl() {
		this(null, false);
    }

    public UniqueKeyElementImpl(String name, boolean primary) {
        super(name);   //the same as index name
        _primary = primary;
    }

    /** Get the primary key flag of the unique key.
     * @return true if this unique key is a primary key, false otherwise
     */
    public boolean isPrimaryKey() {
        return _primary;
    }
  
    /** Set the primary key flag of the unique key.
     * @param flag the flag
     * @throws DBException if impossible
     */
    public void setPrimaryKey(boolean primary) throws DBException {
        _primary = primary;
    }
}
