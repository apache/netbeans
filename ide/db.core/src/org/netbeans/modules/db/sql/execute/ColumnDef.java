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

package org.netbeans.modules.db.sql.execute;

/**
 * Describes a column in the TableModel.
 * A ResultSetTableModel is composed of a list of ColumnDefs and the data.
 *
 * @author Andrei Badea
 */
public class ColumnDef {

    /**
     * The physical column name.
     */
    private String name;
    
    /** 
     * The label for the column, which may be different from the name
     * if aliases are used
     */
    private String label;

    /**
     * Whether we can write to this column.
     * A column is writable if its ColumnTypeDef says so and the
     * ResultSet is updateable.
     */
    private boolean writable;

    /**
     * The class used to display this column in the table.
     */
    private Class clazz;

    public ColumnDef(String name, String label, boolean writable, Class clazz) {
        this.label = label;
        this.name = name;
        this.writable = writable;
        this.clazz = clazz;
    }

    public String getLabel() {
        return label;
    }
    
    public String getName() {
        return name;
    }

    public boolean isWritable() {
        return writable;
    }

    public Class getDisplayClass() {
        return clazz;
    }
}
