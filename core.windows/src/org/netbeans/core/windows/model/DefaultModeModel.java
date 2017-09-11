/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.windows.model;



import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.core.windows.Constants;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModeModel implements ModeModel {


    /** Programatic name of mode. */
    private String name;
    
    private final Set<String> otherNames = new HashSet<String>(3);

    private final Rectangle bounds = new Rectangle();

    private final Rectangle boundsSeparetedHelp = new Rectangle();

    /** State of mode: split or separate. */
    private /*final*/ int state;
    /** Kind of mode: editor or view. */
    private final int kind;
    
    /** Frame state. */
    private int frameState;

    /** Permanent property. */
    private boolean permanent;
    
    private boolean minimized;

    /** Sub model which manages TopComponents stuff. */
    private final TopComponentSubModel topComponentSubModel;
    
    /** Context of tcx. Lazy initialization, because this will be used only by
     * sliding kind of modes */
    private TopComponentContextSubModel topComponentContextSubModel = null;

    // Locks>>
    /** */
    private final Object LOCK_STATE = new Object();
    /** */
    private final Object LOCK_BOUNDS = new Object();
    /** */
    private final Object LOCK_BOUNDS_SEPARATED_HELP = new Object();
    /** Locks frameState. */
    private final Object LOCK_FRAMESTATE = new Object();
    /** Locks top components. */
    private final Object LOCK_TOPCOMPONENTS = new Object();
    /** Locks tc contexts */
    private final Object LOCK_TC_CONTEXTS = new Object();
    
    
    public DefaultModeModel(String name, int state, int kind, boolean permanent) {
        this.name = name;
        this.state = state;
        this.kind = kind;
        this.permanent = permanent;
        this.topComponentSubModel = new TopComponentSubModel(kind);
    }
    
    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    @Override
    public void setState(int state) {
        synchronized(LOCK_STATE) {
            this.state = state;
        }
    }
    
    @Override
    public void removeTopComponent(TopComponent tc, TopComponent recentTc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeTopComponent(tc, recentTc);
        }
    }
    
    // XXX
    @Override
    public void removeClosedTopComponentID(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeClosedTopComponentID(tcID);
        }
    }
    
    /** Adds opened TopComponent. */
    @Override
    public void addOpenedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addOpenedTopComponent(tc);
            sortOpenedTopComponents();
        }
    }
    
    @Override
    public void insertOpenedTopComponent(TopComponent tc, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.insertOpenedTopComponent(tc, index);
            sortOpenedTopComponents();
        }
    }
    
    private void sortOpenedTopComponents() {
        if( getKind() != Constants.MODE_KIND_SLIDING )
            return;
        if( !Switches.isModeSlidingEnabled() )
            return;
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        List<TopComponent> opened = topComponentSubModel.getOpenedTopComponents();
        final List<String> prevModes = new ArrayList<String>( opened.size() );
        final Map<TopComponent, String> tc2modeName = new HashMap<TopComponent, String>( opened.size() );
        for( TopComponent tc : opened ) {
            String tcId = wm.findTopComponentID( tc );
            if( null == tcId )
                continue;
            ModeImpl prevMode = getTopComponentPreviousMode( tcId );
            if( null == prevMode )
                continue;
            if( !prevModes.contains( prevMode.getName() ) )
                prevModes.add( prevMode.getName() );
            tc2modeName.put( tc, prevMode.getName() );
        }
        
        if( prevModes.isEmpty() )
            return; //nothing to sort by (shouldn't really happen)
        
        Collections.sort( opened, new Comparator<TopComponent>() {
            @Override
            public int compare( TopComponent o1, TopComponent o2 ) {
                String mode1 = tc2modeName.get( o1 );
                String mode2 = tc2modeName.get( o2 );
                if( null == mode1 && null != mode2 ) {
                    return 1;
                } else if( null != mode1 && null == mode2 ) {
                    return -1;
                }
                return prevModes.indexOf( mode1 ) - prevModes.indexOf( mode2 );
            }
        });
        topComponentSubModel.setOpenedTopComponents( opened );
    }
    
    @Override
    public void addClosedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addClosedTopComponent(tc);
        }
    }
    
    @Override
    public void addUnloadedTopComponent(String tcID, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addUnloadedTopComponent(tcID, index);
        }
    }
    
    @Override
    public void setUnloadedSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    @Override
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedPreviousSelectedTopComponent(tcID);
        }
    }
    
    /** Sets seleted TopComponent. */
    @Override
    public void setSelectedTopComponent(TopComponent selected) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setSelectedTopComponent(selected);
        }
    }
    
    @Override
    public void setPreviousSelectedTopComponentID(String prevSelectedId) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setPreviousSelectedTopComponentID(prevSelectedId);
        }
    }

    /** Sets frame state */
    @Override
    public void setFrameState(int frameState) {
        synchronized(LOCK_FRAMESTATE) {
            this.frameState = frameState;
        }
    }

    @Override
    public void setBounds(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS) {
            this.bounds.setBounds(bounds);
        }
    }
    
    @Override
    public void setBoundsSeparatedHelp(Rectangle boundsSeparatedHelp) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            this.boundsSeparetedHelp.setBounds(boundsSeparatedHelp);
        }
    }
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////


    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Rectangle getBounds() {
        synchronized(LOCK_BOUNDS) {
            return (Rectangle)this.bounds.clone();
        }
    }
    
    @Override
    public Rectangle getBoundsSeparatedHelp() {
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)this.boundsSeparetedHelp.clone();
        }
    }
    
    @Override
    public int getState() {
        synchronized(LOCK_STATE) {
            return this.state;
        }
    }
    
    @Override
    public int getKind() {
        return this.kind;
    }
    
    /** Gets frame state. */
    @Override
    public int getFrameState() {
        synchronized(LOCK_FRAMESTATE) {
            return this.frameState;
        }
    }
    
    @Override
    public boolean isPermanent() {
        return this.permanent;
    }
    
    @Override
    public void makePermanent() {
        this.permanent = true;
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.isEmpty();
        }
    }
    
    @Override
    public boolean containsTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.containsTopComponent(tc);
        }
    }

    /** Gets list of top components in this workspace. */
    @Override
    public List<TopComponent> getTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponents();
        }
    }


    /** Gets selected TopComponent. */
    @Override
    public TopComponent getSelectedTopComponent() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getSelectedTopComponent();
        }
    }
    /** Gets the ID of top component that was selected before switching to/from maximized mode */
    @Override
    public String getPreviousSelectedTopComponentID() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getPreviousSelectedTopComponentID();
        }
    }

    /** Gets list of top components. */
    @Override
    public List<TopComponent> getOpenedTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponents();
        }
    }

    @Override
    public final void setName(String name) {
        this.name = name;
    }
    
    // XXX
    @Override
    public List<String> getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentsIDs();
        }
    }
    
    @Override
    public List<String> getClosedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getClosedTopComponentsIDs();
        }
    }
    
    @Override
    public List<String> getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponentsIDs();
        }
    }
    
    @Override
    public int getOpenedTopComponentTabPosition (TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentTabPosition(tc);
        }
    }
    
    @Override
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousConstraints(tcID);
        }
    }
    
    @Override
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousMode(tcID);
        }
    }
    /** Gets the tab index of the top component in its previous mode */
    @Override
    public int getTopComponentPreviousIndex(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousIndex(tcID);
        }
    }
    
    @Override
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousConstraints(tcID, constraints);
        }
    }
    
    @Override
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousMode(tcID, mode, prevIndex);
            sortOpenedTopComponents();
        }
    }
    
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    private TopComponentContextSubModel getContextSubModel() {
        if (topComponentContextSubModel == null) {
            topComponentContextSubModel = new TopComponentContextSubModel();
        }
        return topComponentContextSubModel;
    }

    @Override
    public boolean isMinimized() {
        return minimized;
    }

    @Override
    public void setMinimized( boolean minimized ) {
        this.minimized = minimized;
    }

    @Override
    public Collection<String> getOtherNames() {
        return Collections.unmodifiableSet( otherNames );
    }

    @Override
    public void addOtherName( String otherModeName ) {
        otherNames.add( otherModeName );
    }
}

