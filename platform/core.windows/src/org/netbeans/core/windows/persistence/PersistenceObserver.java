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


package org.netbeans.core.windows.persistence;


/**
 * Interface which defines observer of persistence changes.
 *
 * @author  Peter Zavadsky
 */
public interface PersistenceObserver {


    /** Handles adding mode to model.
     * @param modeConfig configuration data of added mode
     */
    public void modeConfigAdded(ModeConfig modeConfig);

    /** Handles removing mode from model.
     * @param modeName unique name of removed mode
     */
    public void modeConfigRemoved(String modeName);

    /** Handles adding tcRef to model. 
     * @param modeName unique name of parent mode.
     * @param tcRefConfig configuration data of added tcRef
     * @param tcRefNames array of tcIds to pass ordering of new tcRef,
     * if there is no ordering defined tcRef is appended to end of array
     */
    public void topComponentRefConfigAdded(
    String modeName, TCRefConfig tcRefConfig, String [] tcRefNames);
    
    /** Handles removing tcRef from model. 
     * @param tc_id unique id of removed tcRef
     */
    public void topComponentRefConfigRemoved(String tc_id);
    
    /** Handles adding group to model.
     * @param groupConfig configuration data of added group
     */
    public void groupConfigAdded(GroupConfig groupConfig);
    
    /** Handles removing group from model.
     * @param groupName unique name of removed group
     */
    public void groupConfigRemoved(String groupName);
    
    /** Handles adding tcGroup to model. 
     * @param groupName unique name of parent group
     * @param tcGroupConfig configuration data of added tcGroup
     */
    public void topComponentGroupConfigAdded(String groupName, TCGroupConfig tcGroupConfig);
    
    /** Handles removing tcGroup from model. 
     * @param groupName unique name of parent group.
     * @param tc_id unique id of removed tcGroup
     */
    public void topComponentGroupConfigRemoved(String groupName, String tc_id);
}

