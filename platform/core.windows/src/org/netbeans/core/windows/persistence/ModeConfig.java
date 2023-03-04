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


import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.core.windows.SplitConstraint;

import java.util.Iterator;
import java.util.Map;


/**
 * Class of mode config properties for communication with persistence management.
 * It keeps data which are read/written from/in .wsmode xml file.
 *
 * @author  Peter Zavadsky
 */
public class ModeConfig {

    /** Name of mode. Supposed to be internally for mode identification. */
    public String name;
    
    public Collection<String> otherNames;

    /** State of mode: 0 = split, 1 = separate. */
    public int state;

    /** Kind of mode: 0 = editor, 1 = view, 2 - sliding */
    public int kind;
    
    /** side for sliding kind*/
    public String side;
    
    /** Constraints of mode - path in tree model */
    public SplitConstraint[] constraints;
    
    //Part for separate state
    public Rectangle bounds;
    public Rectangle relativeBounds;
    
    public int frameState;
    
    //Common part
    /** Id of selected top component. */
    public String selectedTopComponentID;
    
    public boolean permanent = true;
    
    public boolean minimized = false;
    
    /** Array of TCRefConfigs. */
    public TCRefConfig[] tcRefConfigs;
    
    /** TopComponent ID -> slided-in size (width or height) - applies to sliding modes only*/
    public Map<String,Integer> slideInSizes;
    
    /** ID of top component that was selected before switching to/from maximized mode */
    public String previousSelectedTopComponentID;
    
    /** Creates a new instance of ModeConfig */
    public ModeConfig() {
        name = ""; // NOI18N
        constraints = new SplitConstraint[0];
        selectedTopComponentID = ""; // NOI18N
        tcRefConfigs = new TCRefConfig[0];
        previousSelectedTopComponentID = ""; // NOI18N
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModeConfig)) {
            return false;
        }
        ModeConfig modeCfg = (ModeConfig) obj;
        if (!name.equals(modeCfg.name)) {
            return false;
        }
        if ((state != modeCfg.state) || (kind != modeCfg.kind)) {
            return false;
        }
        if (null != side && !side.equals( modeCfg.side ) ) {
            return false;
        } else if( null == side && null != modeCfg.side ) {
            return false;
        }
        //Order of constraints array is defined
        if (constraints.length != modeCfg.constraints.length) {
            return false;
        }
        for (int i = 0; i < constraints.length; i++) {
            if (!constraints[i].equals(modeCfg.constraints[i])) {
                return false;
            }
        }
        if ((bounds != null) && (modeCfg.bounds != null)) {
            if (!bounds.equals(modeCfg.bounds)) {
                return false;
            }
        } else if ((bounds != null) || (modeCfg.bounds != null)) {
            return false;
        }
        if ((relativeBounds != null) && (modeCfg.relativeBounds != null)) {
            if (!relativeBounds.equals(modeCfg.relativeBounds)) {
                return false;
            }
        } else if ((relativeBounds != null) || (modeCfg.relativeBounds != null)) {
            return false;
        }
        if (frameState != modeCfg.frameState) {
            return false;
        }
        if (!selectedTopComponentID.equals(modeCfg.selectedTopComponentID)) {
            return false;
        }
        if (permanent != modeCfg.permanent) {
            return false;
        }
        if( minimized != modeCfg.minimized )
            return false;
        //Order of tcRefConfigs is defined
        if (tcRefConfigs.length != modeCfg.tcRefConfigs.length) {
            return false;
        }
        for (int i = 0; i < tcRefConfigs.length; i++) {
            if (!tcRefConfigs[i].equals(modeCfg.tcRefConfigs[i])) {
                return false;
            }
        }
        if( null != slideInSizes && null != modeCfg.slideInSizes ) {
            if( slideInSizes.size() != modeCfg.slideInSizes.size() )
                return false;
            for (Iterator<String> i=slideInSizes.keySet().iterator(); i.hasNext(); ) {
                String tcId = i.next();
                if( !slideInSizes.get(tcId).equals(modeCfg.slideInSizes.get(tcId)) )
                    return false;
            }
        } else if( null != slideInSizes || null != modeCfg.slideInSizes ) {
            return false;
        }
        if( null != otherNames && null != modeCfg.otherNames ) {
            if( otherNames.size() != modeCfg.otherNames.size() )
                return false;
            if( !otherNames.containsAll( modeCfg.otherNames ) )
                return false;
        } else if( null != otherNames || null != modeCfg.otherNames ) {
            return false;
        }
        if (!previousSelectedTopComponentID.equals(modeCfg.previousSelectedTopComponentID)) {
            return false;
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + state;
        hash = 37 * hash + kind;
        if (side != null) {
            hash = 37 * hash + side.hashCode();
        }
        for (int i = 0; i < constraints.length; i++) {
            hash = 37 * hash + constraints[i].hashCode();
        }
        if (bounds != null) {
            hash = 37 * hash + bounds.hashCode();
        }
        if (relativeBounds != null) {
            hash = 37 * hash + relativeBounds.hashCode();
        }
        hash = 37 * hash + frameState;
        hash = 37 * hash + selectedTopComponentID.hashCode();
        hash = 37 * hash + (permanent ? 0 : 1);
        hash = 37 * hash + (minimized ? 0 : 1);
        for (int i = 0; i < tcRefConfigs.length; i++) {
            hash = 37 * hash + tcRefConfigs[i].hashCode();
        }
        if( null != slideInSizes ) {
            for (Iterator<String> i=slideInSizes.keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                hash = 37 * hash + key.hashCode();
                hash = 37 * hash + slideInSizes.get(key).hashCode();
            }
        }
        if( null != otherNames ) {
            for( String n : otherNames ) {
                hash = 37 * hash + n.hashCode();
            }
        }
        hash = 37 * hash + previousSelectedTopComponentID.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ModeConfig{" + "name=" + name + ", otherNames=" + otherNames + ", state=" + state + ", kind=" + kind + ", side=" + side + ", constraints=" + Arrays.asList(constraints).toString() + ", bounds=" + bounds + ", relativeBounds=" + relativeBounds + ", frameState=" + frameState + ", selectedTopComponentID=" + selectedTopComponentID + ", permanent=" + permanent + ", minimized=" + minimized + ", tcRefConfigs=" + Arrays.asList(tcRefConfigs).toString() + ", slideInSizes=" + slideInSizes + ", previousSelectedTopComponentID=" + previousSelectedTopComponentID + '}';
    }
    
}
