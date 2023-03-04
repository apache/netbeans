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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 *
 * @author girix
 */
public interface CatalogFileWrapper {
    
    public List<CatalogEntry> getSystems();
    public void setSystem(int index, CatalogEntry catEnt) throws IOException;
    public void deleteSystem(int index) throws IOException;
    public void addSystem(CatalogEntry catEnt) throws IOException;
    
    public List<CatalogEntry> getRewriteSystems();
    public void setRewriteSystem(int index, CatalogEntry catEnt) throws IOException;
    public void deleteRewriteSystem(int index) throws IOException;
    public void addRewriteSystem(CatalogEntry catEnt) throws IOException;
    
    
    public List<CatalogEntry> getDelegateSystems() ;
    public void setDelegateSystem(int index, CatalogEntry catEnt) throws IOException ;
    public void deleteDelegateSystem(int index) throws IOException;
    public void addDelegateSystem(CatalogEntry catEnt) throws IOException ;
    
    //for listening to the state of the wraper object
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);


    public void close();
    
    public void cleanInstance();

    public Model.State getCatalogState();

    public List<CatalogEntry> getNextCatalogs();
    public void addNextCatalog(CatalogEntry catEnt)throws IOException ;
    public void deleteNextCatalog(int index)throws IOException ;
    
    
}
