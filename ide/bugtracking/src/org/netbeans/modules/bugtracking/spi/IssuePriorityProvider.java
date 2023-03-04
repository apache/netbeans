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

package org.netbeans.modules.bugtracking.spi;

/**
 * Provides Issue Priority information for a particular remote repository 
 * as well as for a particular Issue from that repository.
 * <p>
 * Note that an implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. 
 * </p>
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 * @since 1.85
*/
public interface IssuePriorityProvider<I> {
    
    /**
     * Determines the Priority of an Issue, where the returned id is expected 
     * to match with exactly one {@link IssuePriorityInfo#getID()} which was 
     * returned via {@link #getPriorityInfos}.
     * 
     * @param i an implementation specific issue instance
     * @return the priority ID
     * @see IssuePriorityInfo
     * @since 1.85
     */
    public String getPriorityID(I i);
    
    /**
     * Provides information about Issue Priorities given by a 
     * remote server. The order in the returned array also determines the rank 
     * of a particular priority, where the PriorityInfo with the lowest index stands for 
     * the highest Priority and so on.
     * <p>
     * For example a default Bugzilla setup would return an array of five PriorityInfo-s, 
     * where the first would be P1, the second P2, etc.
     * </p>
     * 
     * @return priority infos
     * @see IssuePriorityInfo
     * @since 1.85
     */
    public IssuePriorityInfo[] getPriorityInfos();
    
}
