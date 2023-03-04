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
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCIndexColumn extends IndexColumnImplementation {
    private final Index parent;
    private final String name;
    private final Column column;
    private final int position;
    private final Ordering ordering;

    public JDBCIndexColumn(Index parent, String name, Column column, int position, Ordering ordering) {
        this.parent = parent;
        this.name = name;
        this.column = column;
        this.position = position;
        this.ordering = ordering;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Ordering getOrdering() {
        return ordering;
    }

    @Override
    public Index getParent() {
        return parent;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "JDBCIndexColumn[name='" + name + "', ordering=" + ordering + ", position=" + position + ", column=" + column +"]";
    }

}
