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
 * Class of reference of TopComponent in mode config properties for communication
 * with persistence management.
 * It keeps data which are read/written from/in .wstcref xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCRefConfig {

    /** Reference to TopComponent by its unique Id. */
    public String tc_id;

    /** Is TopComponent opened. */
    public boolean opened;

    public String previousMode;
    /** tab index in the previous mode */
    public int previousIndex;
    
    /** True if this TopComponent is docked when the editor is maximized, 
     * false (default) if it should slide out */
    public boolean dockedInMaximizedMode;
    /** True (default) if this TopComponent is docked in the default mode, 
     * false if it is slided out */
    public boolean dockedInDefaultMode;
    /** True if this TopComponent is maximized when slided-in (covers the whole main window) */
    public boolean slidedInMaximized;

    /** Creates a new instance of TCRefConfig */
    public TCRefConfig() {
        tc_id = ""; // NOI18N
        dockedInMaximizedMode = false;
        dockedInDefaultMode = true;
        slidedInMaximized = false;
        previousIndex = -1;
    }
    
    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCRefConfig) {
            TCRefConfig tcRefCfg = (TCRefConfig) obj;
            return (tc_id.equals(tcRefCfg.tc_id)
                   && (opened == tcRefCfg.opened)
                   && (dockedInMaximizedMode == tcRefCfg.dockedInMaximizedMode)
                   && (dockedInDefaultMode == tcRefCfg.dockedInDefaultMode)
                   && (slidedInMaximized == tcRefCfg.slidedInMaximized)
                   && (previousIndex == tcRefCfg.previousIndex));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        hash = 37 * hash + (dockedInMaximizedMode ? 0 : 1);
        hash = 37 * hash + (dockedInDefaultMode ? 0 : 1);
        hash = 37 * hash + (slidedInMaximized ? 0 : 1);
        hash = 37 * hash + previousIndex;
        return hash;
    }
    
    @Override
    public String toString () {
        return "TCRefConfig: tc_id=" + tc_id + ", opened=" + opened 
                + ", maximizedMode=" + dockedInMaximizedMode
                + ", defaultMode=" + dockedInDefaultMode
                + ", slidedInMaximized=" + slidedInMaximized
                + ", previousMode=" + previousMode;
    }
    
}
