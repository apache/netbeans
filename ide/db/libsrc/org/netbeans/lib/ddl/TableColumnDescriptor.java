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

package org.netbeans.lib.ddl;

import java.sql.*;
import org.netbeans.lib.ddl.*;

/**
* Interface of class describing table column.
* @author Slavek Psenicka
*/
public interface TableColumnDescriptor
{
    /** Returns name of column */
    public String getColumnName();
    /** Sets name of column */
    public void setColumnName(String columnName);

    /** Returns type of column */
    public int getColumnType();
    /** Sets type of column */
    public void setColumnType(int columnType);

    /** Returns column size */
    public int getColumnSize();
    /** Sets size of column */
    public void setColumnSize(int size);

    /** Returns decimal digits of column */
    public int getDecimalSize();
    /** Sets decimal digits of column */
    public void setDecimalSize(int size);

    /** Nulls allowed? */
    public boolean isNullAllowed();
    /** Sets null property */
    public void setNullAllowed(boolean flag);
}

/*
* <<Log>>
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
