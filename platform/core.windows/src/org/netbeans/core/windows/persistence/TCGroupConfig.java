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
 * Class of config properties of reference of TopComponent in group for communication
 * with persistence management.
 * It keeps data which are read/written from/in .wstcgrp xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCGroupConfig {

    /** Reference to TopComponent by its unique Id. */
    public String tc_id;

    /** Should TopComponent be opened when group is being opened. */
    public boolean open;

    /** Should TopComponent be closed when group is being closed. */
    public boolean close;
    
    /** Whether the TopComponent was opened at the time the group was opening.
     * It is relevant only in case group state opened is true. */
    public boolean wasOpened;
    
    
    /** Creates a new instance of TCGroupConfig */
    public TCGroupConfig() {
        tc_id = ""; // NOI18N
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCGroupConfig) {
            TCGroupConfig tcGroupCfg = (TCGroupConfig) obj;
            return tc_id.equals(tcGroupCfg.tc_id) &&
                   (open == tcGroupCfg.open) &&
                   (close == tcGroupCfg.close) &&
                   (wasOpened == tcGroupCfg.wasOpened);
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (open ? 0 : 1);
        hash = 37 * hash + (close ? 0 : 1);
        hash = 37 * hash + (wasOpened ? 0 : 1);
        return hash;
    }
    
}
