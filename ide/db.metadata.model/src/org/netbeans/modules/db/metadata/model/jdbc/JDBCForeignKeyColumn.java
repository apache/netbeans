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

import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation;

/**
 *
 * @author David Van Couvering
 */
public final class JDBCForeignKeyColumn extends ForeignKeyColumnImplementation {
    private final ForeignKey parent;
    private final String name;
    private final Column referringColumn;
    private final Column referredColumn;
    private final int position;

    public JDBCForeignKeyColumn(ForeignKey parent, String name, Column referringColumn, Column referredColumn, int position) {
        this.parent = parent;
        this.name = name;
        this.referringColumn = referringColumn;
        this.referredColumn = referredColumn;
        this.position = position;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return "JDBCForeignKeyColumn[name='" + name + "', position=" + position + "referringColumn=" + referringColumn +", referredColumn=" + referredColumn + "]";
    }

    @Override
    public ForeignKey getParent() {
        return parent;
    }

    @Override
    public Column getReferredColumn() {
        return referredColumn;
    }

    @Override
    public Column getReferringColumn() {
        return referringColumn;
    }

    @Override
    public int getPosition() {
        return position;
    }


}
