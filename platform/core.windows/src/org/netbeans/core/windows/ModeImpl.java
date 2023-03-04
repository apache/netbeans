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

package org.netbeans.core.windows;


import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.core.windows.persistence.ModeConfig;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.openide.util.Exceptions;


/** This class is an implementation of Mode interface.
 * It designates 'place' on screen, at wich TopComponent can occure.
 *
 * @author Peter Zavadsky
 */
public final class ModeImpl implements Mode.Xml {

    /** Name constant as a base for nonamed modes. */
    private static final String MODE_ANONYMOUS_NAME = "anonymousMode"; // NOI18N
    
    /** asociated property change support for firing property changes */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(ModeImpl.class);
    
    
    /** Construct new mode with given properties */
    private ModeImpl(String name, int state, int kind, boolean permanent) {
        getCentral().createModeModel(this, name, state, kind, permanent);
    }
    
    
    /** Factory method which creates <code>ModeImpl</code> instances. */
    public static ModeImpl createModeImpl(String name, int state, int kind, boolean permanent) {
        return new ModeImpl(name, state, kind, permanent);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    // Start of org.openide.windows.Mode interface implementation.
    ///////////////////////////////////////////////////////////////////
    /** Gets the programmatic name of this mode.
     * This name should be unique, as it is used to find modes etc.
     * Implements <code>Mode</code> interface method.
     * @return programmatic name of this mode */
    @Override
    public String getName () {
        WindowManagerImpl.warnIfNotInEDT();
        
        return getCentral().getModeName(this);
    }
    
    /**
     * @return A list of Mode's additional names.
     * @since 2.30
     */
    Collection<String> getOtherNames() {
        WindowManagerImpl.warnIfNotInEDT();
        
        return getCentral().getModeOtherNames(this);
    }
    
    /**
     * Add another mode name.
     * @param modeOtherName 
     * @since 2.30
     */
    void addOtherName( String modeOtherName ) {
        WindowManagerImpl.warnIfNotInEDT();
        
        getCentral().addModeOtherName(this, modeOtherName);
    }

    /** Gets display name of this mode.
     ** Implements <code>Mode</code> interface method.
     * @return Human presentable name of this mode implementation
     * @deprecated It is not used anymore. This impl delegated to {@link #getName} method.  */
    @Deprecated
    @Override
    public String getDisplayName () {
        WindowManagerImpl.warnIfNotInEDT();

        return getName();
    }

    /** Gets icon for this mode.
     * Implements <code>Mode</code> interface method.
     * @return null
     * @deprecated It is not used anymore. */
    @Deprecated
    @Override
    public Image getIcon () {
        WindowManagerImpl.warnIfNotInEDT();

        return null;
    }

    /** Indicates whether specified <code>TopComponent</code> can be docked
     * into this <code>Mode</code>.
     * Implements <code>Mode</code> interface method. 
     * @return <code>true</code> */
    @Override
    public boolean canDock(TopComponent tc) {
        WindowManagerImpl.warnIfNotInEDT();
        
        return true;
    }
    
    /** Attaches a component to a mode for this workspace.
     * If the component is in different mode on this desktop, it is 
     * removed from the original and moved to this one.
     * Implements <code>Mode</code> interface method.
     *
     * @param tc top component to dock into this mode
     * @return true if top component was succesfully docked to this
     * mode, false otherwise */
    @Override
    public boolean dockInto(TopComponent tc) {
        WindowManagerImpl.warnIfNotInEDT();
        
        return dockIntoImpl(tc, true);
    }
    
    /** Sets bounds of this mode.
     * Implements <code>Mode</code> interface method.
     * @param rect bounds for the mode */
    @Override
    public void setBounds (Rectangle bounds) {
        WindowManagerImpl.warnIfNotInEDT();
        
        getCentral().setModeBounds(this, bounds);
    }

    /** Getter for current bounds of the mode.
     * Implements <code>Mode</code> interface method.
     * @return the bounds of the mode
     */
    @Override
    public Rectangle getBounds () {
        WindowManagerImpl.warnIfNotInEDT();
        
        return getCentral().getModeBounds(this);
    }
    

    /** Getter for asociated workspace.
     * Implements <code>Mode</code> interface method.
     * @return The workspace instance to which is this mode asociated.
     * @deprecated XXX Don't use anymore.
     */
    @Deprecated
    @Override
    public Workspace getWorkspace () {
        WindowManagerImpl.warnIfNotInEDT();
        
        // Here is the only fake workspace.
        return WindowManagerImpl.getInstance();
    }
    
    /** Gets array of <code>TopComponent</code>S in this mode.
     * Implements <code>Mode</code> interface method.
     * @return array of top components which are currently
     * docked in this mode. May return empty array if no top component
     * is docked in this mode.
     */
    @Override
    public TopComponent[] getTopComponents() {
        WindowManagerImpl.warnIfNotInEDT();
        
        return getCentral().getModeTopComponents(this).toArray(new TopComponent[0]);
    }

    /** Adds listener to the property changes.
     * Implements <code>Mode</code> interface support. */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }

    /** Removes listener to the property changes.
     * Implements <code>Mode</code> interface method. */
    @Override
    public void removePropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    ///////////////////////////////////////////////////////////////////
    // End of org.openide.windows.Mode interface implementation.
    ///////////////////////////////////////////////////////////////////

    
    /** Actually performs the docking operation.
     * @param tc top component to dock into this mode
     * @param orderWeight weight for ordering. Smaller weight number means
     * smaller position index, which means closer to the top or start in
     * visual representations 
     * @param select <code>true</code> if the docked <code>TopComponent</code>
     * will be selected afterwards
     * @return true if top component was succesfully docked to this */
    private boolean dockIntoImpl(final TopComponent tc, final boolean select) {
        if(DEBUG) {
            Debug.log(ModeImpl.class, "Docking tc=" + tc.getName() + " into mode=" + this); // NOI18N
            Debug.dumpStack(ModeImpl.class);
        }
        
        boolean opened = false;
        // PENDING
        // Preferably all in one step.
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode != null && mode != this) {
            // XXX if only closin (mode.close(tc)) there could happen,
            // there is the same TopComponent as closed in two modes. Revise.
            opened = tc.isOpened();
            mode.removeTopComponent(tc);
        }
        
        if( opened ) {
            //don't close the TopComponent if it was opened in the previous mode
            addOpenedTopComponent( tc );
        } else {
            addClosedTopComponent(tc);
        }
        return true;
    }
    
    /** Closes given top component. */
    public void close(TopComponent tc) {
        if(!getOpenedTopComponents().contains(tc)) {
            return;
        }
        if (PersistenceHandler.isTopComponentPersistentWhenClosed(tc)) {
            addClosedTopComponent(tc);
        } else {
            if (Boolean.TRUE.equals(tc.getClientProperty(Constants.KEEP_NON_PERSISTENT_TC_IN_MODEL_WHEN_CLOSED))) {
                addClosedTopComponent(tc);
            } else {
                removeTopComponent(tc);
            }
        }
    }

    /** Gets list of opened TopComponentS. */
    public List<TopComponent> getOpenedTopComponents() {
        return getCentral().getModeOpenedTopComponents(this);
    }
    
    /** Sets selected TopComponent. */
    public void setSelectedTopComponent(TopComponent tc) {
        if(!getOpenedTopComponents().contains(tc)) {
            return;
        }
        
        TopComponent old = getSelectedTopComponent();
        if(tc == old) {
            return;
        }
        
        getCentral().setModeSelectedTopComponent(this, tc);
    }
    
    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent() {
        WindowManagerImpl.assertEventDispatchThread();
        
        return getCentral().getModeSelectedTopComponent(this);
    }
    
    /**
     * Remember which top component was previously the selected one.
     * Used when switching to/from maximized mode.
     */
    public void setPreviousSelectedTopComponentID(String tcId) {
        String old = getPreviousSelectedTopComponentID();
        if(null != tcId && tcId.equals(old)) {
            return;
        }
        getCentral().setModePreviousSelectedTopComponentID(this, tcId);
    }
    
    /**
     * @return The top component that was the selected one before switching to/from 
     * the maximized mode.
     */
    public TopComponent getPreviousSelectedTopComponent() {
        String tcId = getPreviousSelectedTopComponentID();
        TopComponent res = null;
        if( null != tcId )
            res = WindowManagerImpl.getInstance().findTopComponent(tcId);
        WindowManagerImpl.assertEventDispatchThread();
        
        return res;
    }
    
    /**
     * @return The ID top component that was the selected one before switching to/from 
     * the maximized mode.
     */
    public String getPreviousSelectedTopComponentID() {
        WindowManagerImpl.assertEventDispatchThread();
        
        return getCentral().getModePreviousSelectedTopComponentID(this);
    }
    
    public void addOpenedTopComponent(TopComponent tc) {
        getCentral().addModeOpenedTopComponent(this, tc);
    }
    
    public void addOpenedTopComponentNoNotify(TopComponent tc) {
        getCentral().addModeOpenedTopComponentNoNotify(this, tc);
    }

    public void addOpenedTopComponent(TopComponent tc, int index) {
        getCentral().insertModeOpenedTopComponent( this, tc, index );
    }
    
    public void addClosedTopComponent(TopComponent tc) {
        getCentral().addModeClosedTopComponent(this, tc);
    }
    
    public void addUnloadedTopComponent(String tcID) {
        addUnloadedTopComponent( tcID, -1 );
    }
    
    public void addUnloadedTopComponent(String tcID, int index) {
        getCentral().addModeUnloadedTopComponent(this, tcID, index);
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        getCentral().setUnloadedSelectedTopComponent(this, tcID);
    }
    
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        getCentral().setUnloadedPreviousSelectedTopComponent(this, tcID);
    }
    
    public List<String> getOpenedTopComponentsIDs() {
        return getCentral().getModeOpenedTopComponentsIDs(this);
    }
    
    public List<String> getClosedTopComponentsIDs() {
        return getCentral().getModeClosedTopComponentsIDs(this);
    }
    
    public List<String> getTopComponentsIDs() {
        return getCentral().getModeTopComponentsIDs(this);
    }
    
    /** Gets opened top component position in the mode */
    public int getTopComponentTabPosition(TopComponent tc) {
        return getCentral().getModeTopComponentTabPosition(this, tc);
    }
    
    /** Sets and updates the state of associated frame, if frame exists.
     * Otherwise remembers state for futher use
     */
    public void setFrameState(int state) {
        getCentral().setModeFrameState(this, state);
    }
    
    /** @return state of the frame
     * If frame exists, its real state is returned. 
     * Last remembered frame state is returned if frame currently
     * doesn't exist. FrameType.NORMAL is returned as default if state cannot be
     * obtained by mentioned procedures.
     */
    public int getFrameState () {
        return getCentral().getModeFrameState(this);
    }
    
    /** Indicates whether this mode is permanent, it means it is kept in model
     * even in case it becomes empty. */
    public boolean isPermanent () {
        return getCentral().isModePermanent(this);
    }
    
    /** Indicates whether this mode has no TopComponents. */
    public boolean isEmpty() {
        return getCentral().isModeEmpty(this);
    }

    public boolean containsTopComponent(TopComponent tc) {
        return getCentral().containsModeTopComponent(this, tc);
    }
    
    /** Gets state of mode. Either split or separate. */
    public int getState() {
        return getCentral().getModeState(this);
    }
    
    /** Gets kind, either editor or view. */
    public int getKind() {
        return getCentral().getModeKind(this);
    }
    /** Gets side, either null for view and editor kinds, a side constant for sliding kind.. */
    public String getSide() {
        return getCentral().getModeSide(this);
    }
    
    // Contstraints and split weights are saved in split structure at wm model level.
    /** Sets constraints for mode. */
    public void setConstraints(SplitConstraint[] constraints) {
        WindowManagerImpl.getInstance().setModeConstraints(this, constraints);
    }

    /** @return Current constraints of this mode, null by default */
    public SplitConstraint[] getConstraints() {
        return WindowManagerImpl.getInstance().getModeConstraints(this);
    }
    
    /**
     * @return True if this mode is minimized.
     * @since 2.30
     */
    public boolean isMinimized() {
        return getCentral().isModeMinimized( this );
    }
    
    /**
     * Mark this mode as minimized or docked.
     * @param minimized 
     * @since 2.30
     */
    public void setMinimized( boolean minimized ) {
        getCentral().setModeMinimized( this, minimized );
    }
    
    /** Removes TopComponent from this mode. */
    public void removeTopComponent(TopComponent tc) {
        getCentral().removeModeTopComponent(this, tc);
    }
    
    public void removeTopComponents(Set topComponentSet) {
        for(Iterator it = topComponentSet.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            removeTopComponent(tc);
        }
    }
    
    // XXX Only use for yet unloaded components, for PersistenceHandler only.
    public void removeClosedTopComponentID(String tcID) {
        getCentral().removeModeClosedTopComponentID(this, tcID);
    }
    
    // XXX It is used for user actions only, to prohibit mixing
    // of view and editor components.
    /** Indicates whether this mode can contain specified TopComponent. */
    public boolean canContain(TopComponent tc) {
        if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
        || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(tc)
        || Switches.isMixingOfEditorsAndViewsEnabled()) {
            return true;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode == null) {
            return true;
        }
        // allow mixing of view and sliding modes
        int myKind = getKind();
        int otherKind = mode.getKind();
        
        return (myKind == otherKind) ||
               (myKind != Constants.MODE_KIND_EDITOR && otherKind != Constants.MODE_KIND_EDITOR);
    }
    
    void doFirePropertyChange(final String propName,
    final Object oldValue, final Object newValue) {
        // PENDING When #37529 finished, then uncomment the next row and move the
        // checks of AWT thread away.
        //  WindowManagerImpl.assertEventDispatchThread();
        if(SwingUtilities.isEventDispatchThread()) {
            changeSupport.firePropertyChange(propName, oldValue, newValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.firePropertyChange(propName, oldValue, newValue);
                }
            });
        }
    }
    
    /** @return string description of this mode */
    @Override
    public String toString () {
        // #42995 - don't scream when toString called from non-AWT thread
        return super.toString () + "[" + getCentral().getModeName(this) + "]"; // NOI18N
    }
    
    /** Accessor to central unit. Helper method. */
    private static Central getCentral() {
        return WindowManagerImpl.getInstance().getCentral();
    }
    
    
    ////////////////////
    // Utility methods>>
    /*private*/ static String getUnusedModeName() {
        String base = MODE_ANONYMOUS_NAME;
        
        // don't allow base to be too long, because will act as file name too
        // PENDING Maximal length is 20.
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        
        // add numbers to the name
        String result;
        int modeNumber = 1;
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        while(wm.findMode(result = base + "_" + modeNumber) != null) { // NOI18N
            modeNumber++;
        }
        return result;
    }
    // Utility methods<<
    ////////////////////

    public void setModeName(String text) {
        getCentral().setModeName(this, text);
    }

    @Override
    public String toXml() {
        ModeConfig config = PersistenceHandler.getDefault().getConfigFromMode(this);
        try {
            return PersistenceManager.getDefault().createXmlFromMode(config);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

}

