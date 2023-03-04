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

package org.netbeans.core.windows.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;

/**
 * This class stores the snapshot of the docking status (docked/slided-out) of TopComponents 
 * when switching to or from maximized mode.
 *
 * @author S. Aubrecht
 */
public class DockingStatus {
    
    protected Model model;
    protected List<String> docked = new ArrayList<String>(10);
    protected List<String> slided = new ArrayList<String>(10);
    private boolean marked = false;
    
    /** Creates a new instance of DockingStatus */
    DockingStatus( Model model ) {
        this.model = model;
    }
    
    /**
     * Remember which TopComponents are docked and which are slided.
     */
    public void mark() {
        Set<ModeImpl> modes = model.getModes();
        for( Iterator<ModeImpl> i=modes.iterator(); i.hasNext(); ) {
            ModeImpl modeImpl = i.next();
            if( modeImpl.getState() == Constants.MODE_STATE_SEPARATED )
                continue;
            
            List<String> views = model.getModeOpenedTopComponentsIDs( modeImpl );
            if( modeImpl.getKind() == Constants.MODE_KIND_VIEW ) {
                docked.addAll( views );
                slided.removeAll( views );
            } else if( modeImpl.getKind() == Constants.MODE_KIND_SLIDING ) {
                docked.removeAll( views );
                slided.addAll( views );
            }
        }
        marked = true;
    }
    
    /**
     * @return True if the TopComponent should switch to docked status
     * (Used when switching to/from maximized mode)
     */
    public boolean shouldDock( String tcID ) {
        return null != tcID && docked.contains( tcID ) 
                && marked; //when maximizing for the first time keep TC slided-out
                           //even if it is docked by default in maximized mode
    }
    
    /**
     * @return True if the TopComponent should slide-out
     * (Used when switching to/from maximized mode)
     */
    public boolean shouldSlide( String tcID ) {
        return null != tcID && (slided.contains( tcID )
                                //special case for TopComponents not declared in XML layer
                                || (!slided.contains( tcID ) && !docked.contains( tcID )));
    }
    
    /**
     * Adds 'docked' TopComponent (used when the window system loads)
     */
    public void addDocked( String tcID ) {
        if( null != tcID ) {
            docked.add( tcID );
            slided.remove( tcID );
        }
    }
    
    /**
     * Adds 'slided-out' TopComponent (used when the window system loads)
     */
    public void addSlided( String tcID ) {
        if( null != tcID ) {
            slided.add( tcID );
            docked.remove( tcID );
        }
    }
    
    /**
     * (Used when the window system gets stored)
     * @return True if the given TopComponent was docked when its snapshot was taken.
     */
    public boolean isDocked( String tcID ) {
        return null != tcID && docked.contains( tcID );
    }
    
    /**
     * (Used when the window system gets stored)
     * @return True if the given TopComponent was slided when its snapshot was taken.
     */
    public boolean isSlided( String tcID ) {
        return null != tcID && slided.contains( tcID );
    }
    
    /**
     * Reset to defaults
     */
    void clear() {
        docked.clear();
        slided.clear();
    }
}
