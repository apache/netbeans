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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface Column {

    public void setName(String value);

    public String getName();
    
    public void setUnique(boolean value);
    
    public boolean isUnique();
    
    public void setNullable(boolean value);
    
    public boolean isNullable();
    
    public void setInsertable(boolean value);
    
    public boolean isInsertable();
    
    public void setUpdatable(boolean value);
    
    public boolean isUpdatable();
    
    public void setColumnDefinition(String value);
    
    public String getColumnDefinition();
    
    public void setTable(String value);
    
    public String getTable();
    
    public void setLength(int value);
    
    public int getLength();
    
    public void setPrecision(int value);
    
    public int getPrecision();
    
    public void setScale(int value);
    
    public int getScale();
    
}
