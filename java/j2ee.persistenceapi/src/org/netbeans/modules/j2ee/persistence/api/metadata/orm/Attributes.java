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

public interface Attributes {

    public void setId(int index, Id value);

    public Id getId(int index);
    
    public int sizeId();
    
    public void setId(Id[] value);
    
    public Id[] getId();
    
    public int addId(Id value);
    
    public int removeId(Id value);
    
    public Id newId();
    
    public void setEmbeddedId(EmbeddedId value);
    
    public EmbeddedId getEmbeddedId();
    
    public EmbeddedId newEmbeddedId();
    
    public void setBasic(int index, Basic value);
    
    public Basic getBasic(int index);
    
    public int sizeBasic();
    
    public void setBasic(Basic[] value);
    
    public Basic[] getBasic();
    
    public int addBasic(Basic value);
    
    public int removeBasic(Basic value);
    
    public Basic newBasic();
    
    public void setVersion(int index, Version value);
    
    public Version getVersion(int index);
    
    public int sizeVersion();
    
    public void setVersion(Version[] value);
    
    public Version[] getVersion();
    
    public int addVersion(Version value);
    
    public int removeVersion(Version value);
    
    public Version newVersion();
    
    public void setManyToOne(int index, ManyToOne value);
    
    public ManyToOne getManyToOne(int index);
    
    public int sizeManyToOne();
    
    public void setManyToOne(ManyToOne[] value);
    
    public ManyToOne[] getManyToOne();
    
    public int addManyToOne(ManyToOne value);
    
    public int removeManyToOne(ManyToOne value);
    
    public ManyToOne newManyToOne();
    
    public void setOneToMany(int index, OneToMany value);
    
    public OneToMany getOneToMany(int index);
    
    public int sizeOneToMany();
    
    public void setOneToMany(OneToMany[] value);
    
    public OneToMany[] getOneToMany();
    
    public int addOneToMany(OneToMany value);
    
    public int removeOneToMany(OneToMany value);
    
    public OneToMany newOneToMany();
    
    public void setOneToOne(int index, OneToOne value);
    
    public OneToOne getOneToOne(int index);
    
    public int sizeOneToOne();
    
    public void setOneToOne(OneToOne[] value);
    
    public OneToOne[] getOneToOne();
    
    public int addOneToOne(OneToOne value);
    
    public int removeOneToOne(OneToOne value);
    
    public OneToOne newOneToOne();
    
    public void setManyToMany(int index, ManyToMany value);
    
    public ManyToMany getManyToMany(int index);
    
    public int sizeManyToMany();
    
    public void setManyToMany(ManyToMany[] value);
    
    public ManyToMany[] getManyToMany();
    
    public int addManyToMany(ManyToMany value);
    
    public int removeManyToMany(ManyToMany value);
    
    public ManyToMany newManyToMany();
    
    public void setEmbedded(int index, Embedded value);
    
    public Embedded getEmbedded(int index);
    
    public int sizeEmbedded();
    
    public void setEmbedded(Embedded[] value);
    
    public Embedded[] getEmbedded();
    
    public int addEmbedded(Embedded value);
    
    public int removeEmbedded(Embedded value);
    
    public Embedded newEmbedded();
    
    public void setTransient(int index, Transient value);
    
    public Transient getTransient(int index);
    
    public int sizeTransient();
    
    public void setTransient(Transient[] value);
    
    public Transient[] getTransient();
    
    public int addTransient(Transient value);
    
    public int removeTransient(Transient value);
    
    public Transient newTransient();
    
}
