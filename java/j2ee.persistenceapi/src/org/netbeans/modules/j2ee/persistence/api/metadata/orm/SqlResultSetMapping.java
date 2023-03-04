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

public interface SqlResultSetMapping {

    public void setName(String value);

    public String getName();
    
    public void setEntityResult(int index, EntityResult value);
    
    public EntityResult getEntityResult(int index);
    
    public int sizeEntityResult();
    
    public void setEntityResult(EntityResult[] value);
    
    public EntityResult[] getEntityResult();
    
    public int addEntityResult(EntityResult value);
    
    public int removeEntityResult(EntityResult value);
    
    public EntityResult newEntityResult();
    
    public void setColumnResult(int index, ColumnResult value);
    
    public ColumnResult getColumnResult(int index);
    
    public int sizeColumnResult();
    
    public void setColumnResult(ColumnResult[] value);
    
    public ColumnResult[] getColumnResult();
    
    public int addColumnResult(ColumnResult value);
    
    public int removeColumnResult(ColumnResult value);
    
    public ColumnResult newColumnResult();
    
}
