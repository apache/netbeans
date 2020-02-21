/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.repository.impl.spi;

/**
 *
 */
public interface LayerListener {
     /**
     * You can also register this listener as a service
     */
    public static final String PATH = "CND/RepositoryLayerListener"; //NOI18N
/**
     * Invoked once a repository is created.
     * 
     * Use case is as follows. 
     * Indexing resides in the same directory repository resides;
     * and we need to check index consistency when we open a repository:
     * if index is corrupted, then repository is invalid either 
     * 
     * @param layerDescriptor
     *
     * @return true if it is OK to open repository,
     * false if repository data should be considered corrupted
     */
    boolean layerOpened(LayerDescriptor layerDescriptor);        
}
