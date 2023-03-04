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

public interface JoinTable {

    public void setName(String value);

    public String getName();
    
    public void setCatalog(String value);
    
    public String getCatalog();
    
    public void setSchema(String value);
    
    public String getSchema();
    
    public void setJoinColumn(int index, JoinColumn value);
    
    public JoinColumn getJoinColumn(int index);
    
    public int sizeJoinColumn();
    
    public void setJoinColumn(JoinColumn[] value);
    
    public JoinColumn[] getJoinColumn();
    
    public int addJoinColumn(JoinColumn value);
    
    public int removeJoinColumn(JoinColumn value);
    
    public JoinColumn newJoinColumn();
    
    public void setInverseJoinColumn(int index, JoinColumn value);
    
    public JoinColumn getInverseJoinColumn(int index);
    
    public int sizeInverseJoinColumn();
    
    public void setInverseJoinColumn(JoinColumn[] value);
    
    public JoinColumn[] getInverseJoinColumn();
    
    public int addInverseJoinColumn(JoinColumn value);
    
    public int removeInverseJoinColumn(JoinColumn value);
    
    public void setUniqueConstraint(int index, UniqueConstraint value);
    
    public UniqueConstraint getUniqueConstraint(int index);
    
    public int sizeUniqueConstraint();
    
    public void setUniqueConstraint(UniqueConstraint[] value);
    
    public UniqueConstraint[] getUniqueConstraint();
    
    public int addUniqueConstraint(UniqueConstraint value);
    
    public int removeUniqueConstraint(UniqueConstraint value);
    
    public UniqueConstraint newUniqueConstraint();
    
}
