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
import java.sql.*;
import org.netbeans.lib.ddl.*;

/**
* Implementation of ForeignKey constraint.
*/
public class ForeignKeyConstraint extends AbstractTableColumn implements ForeignKeyConstraintDescriptor {
    /** Refernced table */
    String tname;

    /** Referenced column */
    String cname;

    static final long serialVersionUID =9183651896170854492L;
    /** Returns name of Referenced table */
    public String getReferencedTableName()
    {
        return tname;
    }

    /** Sets name of Referenced table */
    public void setReferencedTableName(String name)
    {
        tname = name;
    }

    /** Returns name of Referenced column */
    public String getReferencedColumnName()
    {
        return cname;
    }

    /** Sets name of Referenced column */
    public void setReferencedColumnName(String name)
    {
        cname = name;
    }

    /**
    * Returns properties and it's values supported by this object.
    * object.name	Name of the object; use setObjectName() 
    * object.owner	Name of the object; use setObjectOwner() 
    * fkobject.name	Specification of foreign table 
    * fkcolumn.name	Specification of foreign column 
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        Map args = super.getColumnProperties(cmd);
        args.put("fkobject.name", cmd.quote(tname)); // NOI18N
        args.put("fkcolumn.name", cmd.quote(cname)); // NOI18N
        
        return args;
    }
}
