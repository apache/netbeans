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
package org.netbeans.modules.cnd.repository.api;

/**
 *
 */
public interface RepositoryListener {
    
    /**
     * You can also register this listener as a service
     */
    public static final String PATH = "CND/RepositoryListener"; //NOI18N
   

    /**
     * invoked once an access to not yet opened unit happens
     *
     * @param unitId
     * @return 
     */
    boolean unitOpened(int unitId);

    /**
     * invoked once a unit is closed
     *
     * @param unitId
     */
    void unitClosed(int unitId);
    
    /**
     * invoked once a unit is removed
     *
     * @param unitId
     */
    void unitRemoved(int unitId);        
}
