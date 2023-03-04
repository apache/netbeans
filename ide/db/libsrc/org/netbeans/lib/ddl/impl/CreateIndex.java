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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*/

public class CreateIndex extends ColumnListCommand {
    /** Index name */
    private String indexname;

    /** Index type */
    private String unique;

    static final long serialVersionUID =1899024699690380782L;    
    public String getIndexName()
    {
        return indexname;
    }

    public void setIndexName(String iname)
    {
        indexname = iname;
    }

    public String getIndexType()
    {
        return unique;
    }

    public void setIndexType(String idx_type)
    {
        unique = idx_type;
    }

    public TableColumn specifyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.CREATE_INDEX, false, false);
    }
    
    public TableColumn specifyNewColumn(String name)
            throws ClassNotFoundException, IllegalAccessException, 
            InstantiationException {
        return specifyColumn(TableColumn.COLUMN, name, 
                Specification.CREATE_INDEX, false, true);
    }

    public Map getCommandProperties() throws DDLException {
        Map args = super.getCommandProperties();
        args.put("index.name", indexname); // NOI18N
        args.put("index.unique", unique); // NOI18N
        
        return args;
    }
}
