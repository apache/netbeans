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
package org.netbeans.modules.team.spi;

/**
 *
 * @author Tomas Stupka
 * @param <Q>
 * @param <I>
 */
public interface NBRepositoryProvider<Q, I> {
   
    /** 
     * Provides owner (project/component/...) info to prefill the respective 
     * fields in an issue editor.
     * <p>
     * Note that this is currently meant only for a NB issue
     * 
     * @param i
     * @param info
     */
    public void setIssueOwnerInfo(I i, OwnerInfo info);   
    
    /**
     * Provides owner (project/component/...) info to prefill the respective 
     * fields in an query editor.
     * <p>
     * Note that this is currently meant only for a NB issue
     * 
     * @param q
     * @param info 
     */
    public void setQueryOwnerInfo(Q q, OwnerInfo info);   
}
