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

public interface EntityResult {

    public void setEntityClass(String value);

    public String getEntityClass();
    
    public void setDiscriminatorColumn(String value);
    
    public String getDiscriminatorColumn();
    
    public void setFieldResult(int index, FieldResult value);
    
    public FieldResult getFieldResult(int index);
    
    public int sizeFieldResult();
    
    public void setFieldResult(FieldResult[] value);
    
    public FieldResult[] getFieldResult();
    
    public int addFieldResult(FieldResult value);
    
    public int removeFieldResult(FieldResult value);
    
    public FieldResult newFieldResult();
    
}
