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

