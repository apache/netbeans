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
