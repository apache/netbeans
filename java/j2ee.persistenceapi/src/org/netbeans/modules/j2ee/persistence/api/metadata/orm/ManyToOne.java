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

public interface ManyToOne {

    public void setName(String value);

    public String getName();
    
    public void setTargetEntity(String value);
    
    public String getTargetEntity();
    
    public void setFetch(String value);
    
    public String getFetch();
    
    public void setOptional(boolean value);
    
    public boolean isOptional();
    
    public void setJoinColumn(int index, JoinColumn value);
    
    public JoinColumn getJoinColumn(int index);
    
    public int sizeJoinColumn();
    
    public void setJoinColumn(JoinColumn[] value);
    
    public JoinColumn[] getJoinColumn();
    
    public int addJoinColumn(JoinColumn value);
    
    public int removeJoinColumn(JoinColumn value);
    
    public JoinColumn newJoinColumn();
    
    public void setJoinTable(JoinTable value);
    
    public JoinTable getJoinTable();
    
    public JoinTable newJoinTable();
    
    public void setCascade(CascadeType value);
    
    public CascadeType getCascade();
    
    public CascadeType newCascadeType();
    
}
