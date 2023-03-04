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


import java.util.Collection;

import java.awt.*;
import java.util.List;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
interface ModeModel {

    // Mutators
    /** Change name of the mode */
    public void setName(String name);
    /** Sets state. */
    public void setState(int state);
    /** Sets bounds. */
    public void setBounds(Rectangle bounds);
    /** */
    public void setBoundsSeparatedHelp(Rectangle bounds);
    /** Sets frame state. */
    public void setFrameState(int frameState);
    /** Sets selected TopComponent. */
    public void setSelectedTopComponent(TopComponent selected);
    /** Set top component that was selected before switching to/from maximized mode */
    public void setPreviousSelectedTopComponentID(String prevSelectedId);
    /** Adds opened TopComponent. */
    public void addOpenedTopComponent(TopComponent tc);
    /** Inserts opened TopComponent. */
    public void insertOpenedTopComponent(TopComponent tc, int index);
    /** Adds closed TopComponent. */
    public void addClosedTopComponent(TopComponent tc);
    // XXX
    public void addUnloadedTopComponent(String tcID, int index);
    // XXX
    public void setUnloadedSelectedTopComponent(String tcID);
    /** Set top component that was selected before switching to/from maximized mode */
    public void setUnloadedPreviousSelectedTopComponent(String tcID);
    /** Removes TopComponent from mode. */
    public void removeTopComponent(TopComponent tc, TopComponent recentTc);
    // XXX
    public void removeClosedTopComponentID(String tcID);
    
    // Info about previous top component context, used by sliding kind of modes
    
    /** Sets information of previous mode top component was in. */
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex);
    /** Sets information of previous constraints of mode top component was in. */
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints);

    // Accessors
    /** Gets programatic name of mode. */
    public String getName();
    /** Gets bounds. */
    public Rectangle getBounds();
    /** */
    public Rectangle getBoundsSeparatedHelp();
    /** Gets state. */
    public int getState();
    /** Gets kind. */
    public int getKind();
    /** Gets frame state. */
    public int getFrameState();
    /** Gets whether it is permanent. */
    public boolean isPermanent();
    /** Makes the mode permanent 
     * @since 2.30
     */
    public void makePermanent();
    /** */
    public boolean isEmpty();
    /** */
    public boolean containsTopComponent(TopComponent tc);
    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent();
    /** Gets the ID of top component that was selected before switching to/from maximized mode */
    public String getPreviousSelectedTopComponentID();
    /** Gets list of top components in mode. */
    public List<TopComponent> getTopComponents();
    /** Gets list of top components in mode. */
    public List<TopComponent> getOpenedTopComponents();
    // XXX
    public List<String> getOpenedTopComponentsIDs();
    public List<String> getClosedTopComponentsIDs();
    public List<String> getTopComponentsIDs();    
    
    // Info about previous top component context, used by sliding kind of modes
    
    public ModeImpl getTopComponentPreviousMode(String tcID);
    /** Gets the tab index of the top component in its previous mode */
    public int getTopComponentPreviousIndex(String tcID);
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID);
    
    /** Gets position of opened top component in this mode */
    public int getOpenedTopComponentTabPosition (TopComponent tc);
    /** @since 2.30 */
    public boolean isMinimized();
    /** @since 2.30 */
    public void setMinimized( boolean minimized );
    /** @since 2.30 */
    public Collection<String> getOtherNames();
    /** @since 2.30 */
    public void addOtherName( String otherModeName );
}

