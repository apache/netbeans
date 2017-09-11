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


package org.netbeans.core.windows.persistence;


import java.awt.Rectangle;
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
    
}
