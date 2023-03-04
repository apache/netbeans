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
 * Class of group config properties for communication with persistence management.
 * It keeps data which are read/written from/in .wsgrp xml file.
 *
 * @author  Peter Zavadsky
 */
public class GroupConfig {

    /** Unique name of group. */
    public String name;

    /** Is group opened or not. */
    public boolean opened;


    /** Array of TCGroupConfigs */
    public TCGroupConfig[] tcGroupConfigs;

    /** Creates a new instance of GroupConfig */
    public GroupConfig() {
        name = ""; // NOI18N
        tcGroupConfigs = new TCGroupConfig[0];
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GroupConfig)) {
            return false;
        }
        GroupConfig groupCfg = (GroupConfig) obj;
        if (!name.equals(groupCfg.name)) {
            return false;
        }
        if (opened != groupCfg.opened) {
            return false;
        }
        //Order of tcGroupConfigs array is NOT defined
        if (tcGroupConfigs.length != groupCfg.tcGroupConfigs.length) {
            return false;
        }
        for (int i = 0; i < tcGroupConfigs.length; i++) {
            TCGroupConfig tcGroupCfg = null;
            for (int j = 0; j < groupCfg.tcGroupConfigs.length; j++) {
                if (tcGroupConfigs[i].tc_id.equals(groupCfg.tcGroupConfigs[j].tc_id)) {
                    tcGroupCfg = groupCfg.tcGroupConfigs[j];
                    break;
                }
            }
            if (tcGroupCfg == null) {
                return false;
            }
            if (!tcGroupConfigs[i].equals(tcGroupCfg)) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        for (int i = 0; i < tcGroupConfigs.length; i++) {
            hash = 37 * hash + tcGroupConfigs[i].hashCode();
        }
        return hash;
    }
    
}
