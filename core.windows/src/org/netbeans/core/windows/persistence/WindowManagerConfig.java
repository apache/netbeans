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


import org.netbeans.core.windows.SplitConstraint;

import java.awt.*;


/**
 * Class of window manager config properties for communication with persistence management.
 * It keeps data which are read/written from/in .wsmgr xml file.
 *
 * @author  Peter Zavadsky
 */
public class WindowManagerConfig {
    /////////////////////
    // SEPARATED STATE
    //////////
    // BEGIN Values specific for initial main window position
    // Note: Only absolute bounds are saved [x,y,w,h].
    //////////
    /** Flag to control horizontal position of main window. If it is true x is
     undefined. */
    public boolean centeredHorizontallySeparated;
    
    /** Flag to control vertical position of main window . If it is true y is
     undefined. */
    public boolean centeredVerticallySeparated;
    
    /** Horizontal absolute position of main window. */
    public int xSeparated;
    
    /** Vertical absolute position of main window */
    public int ySeparated;
    
    /** Width of main window. -1 means not set. Either width or relative width
     should be set. */
    public int widthSeparated;
    
    /** Height of main window. -1 means not set. Either height or relative height
     should be set. */
    public int heightSeparated;
    
    /** Relative x of main window. -1 means not set. */
    public float relativeXSeparated;
    
    /** Relative y of main window. -1 means not set. */
    public float relativeYSeparated;
    
    /** Relative width of main window. -1 means not set. */
    public float relativeWidthSeparated;
    
    /** Relative height of main window. -1 means not set. */
    public float relativeHeightSeparated;
    //////////
    // END
    //////////
    
    //////////////
    // JOINED STATE
    //////////
    // BEGIN Values specific for initial main window position
    // Note: Only absolute bounds are saved [x,y,w,h].
    //////////
    /** Flag to control horizontal position of main window. If it is true x is
     undefined. */
    public boolean centeredHorizontallyJoined;
    
    /** Flag to control vertical position of main window . If it is true y is
     undefined. */
    public boolean centeredVerticallyJoined;
    
    /** Horizontal absolute position of main window. */
    public int xJoined;
    
    /** Vertical absolute position of main window */
    public int yJoined;
    
    /** Width of main window. -1 means not set. Either width or relative width
     should be set. */
    public int widthJoined;
    
    /** Height of main window. -1 means not set. Either height or relative height
     should be set. */
    public int heightJoined;
    
    /** Relative x of main window. -1 means not set. */
    public float relativeXJoined;
    
    /** Relative y of main window. -1 means not set. */
    public float relativeYJoined;
    
    /** Relative width of main window. -1 means not set. */
    public float relativeWidthJoined;
    
    /** Relative height of main window. -1 means not set. */
    public float relativeHeightJoined;
    
    /** Minimum allowed width of main window for computing initial size */
    public int maximizeIfWidthBelowJoined;
    
    /** Minimum allowed height of main window for computing initial size */
    public int maximizeIfHeightBelowJoined;
    //////////
    // END
    //////////
    /** Frame state of main window when in tiled(joined) state.
     * Possible values are taken from java.awt.Frame:
     * NORMAL
     * ICONIFIED
     * MAXIMIZED_HORIZ
     * MAXIMIZED_VERT
     * MAXIMIZED_BOTH
     */
    public int mainWindowFrameStateJoined;
    /** Frame state of main window when in separated state.
     * Possible values are taken from java.awt.Frame:
     * NORMAL
     * ICONIFIED
     * MAXIMIZED_HORIZ
     * MAXIMIZED_VERT
     * MAXIMIZED_BOTH
     */
    public int mainWindowFrameStateSeparated;
    
    /** State of editor area: 0 = joined, 1 = separated. */
    public int editorAreaState;
    /** Constraints of editor area - path in tree model.
     * List of <code>Item</code>S. */
    public SplitConstraint[] editorAreaConstraints;
    /** Bounds of editor area. */
    public Rectangle editorAreaBounds;
    /** Relative bounds of editor area. */
    public Rectangle editorAreaRelativeBounds;
    /** Frame state of editor area when in separated 'state'.
     * Possible values are taken from java.awt.Frame:
     * NORMAL
     * ICONIFIED
     * MAXIMIZED_HORIZ
     * MAXIMIZED_VERT
     * MAXIMIZED_BOTH
     */
    public int editorAreaFrameState;

    /** Size of screen. */
    public Dimension screenSize;
    /** Name of active mode. */
    public String activeModeName;
    /** Name of maximized mode (editor). */
    public String editorMaximizedModeName;
    /** Name of maximized mode (view). */
    public String viewMaximizedModeName;
    /** Name of toolbar configuration. */
    public String toolbarConfiguration;
    /** Preferred size of toolbar icons. */
    public int preferredToolbarIconSize;
    /** List of <code>ModeConfig</code>S. */
    public ModeConfig[] modes;
    /** List of <code>GroupConfig</code>S. */
    public GroupConfig[] groups;
    /** List of TopComponent Ids from recent view list. */
    public String[] tcIdViewList;
    
    /** Creates a new instance of WindowManagerConfig */
    public WindowManagerConfig() {
        editorAreaConstraints = new SplitConstraint[0];
        activeModeName = ""; // NOI18N
        editorMaximizedModeName = ""; // NOI18N
        viewMaximizedModeName = ""; // NOI18N
        toolbarConfiguration = ""; // NOI18N
        modes = new ModeConfig[0];
        groups = new GroupConfig[0];
        tcIdViewList = new String[0];
        preferredToolbarIconSize = 24;
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WindowManagerConfig)) {
            return false;
        }
        WindowManagerConfig wmCfg = (WindowManagerConfig) obj;
        if ((centeredHorizontallySeparated != wmCfg.centeredHorizontallySeparated) ||
            (centeredVerticallySeparated != wmCfg.centeredVerticallySeparated)) {
            return false;
        }
        if ((xSeparated != wmCfg.xSeparated) ||
            (ySeparated != wmCfg.ySeparated) ||
            (widthSeparated != wmCfg.widthSeparated) ||
            (heightSeparated != wmCfg.heightSeparated)) {
            return false;
        }
        if ((relativeXSeparated != wmCfg.relativeXSeparated) ||
            (relativeYSeparated != wmCfg.relativeYSeparated)) {
            return false;
        }
        if ((relativeWidthSeparated != wmCfg.relativeWidthSeparated) || 
            (relativeHeightSeparated != wmCfg.relativeHeightSeparated)) {
            return false;
        }
        
        if ((centeredHorizontallyJoined != wmCfg.centeredHorizontallyJoined) ||
            (centeredVerticallyJoined != wmCfg.centeredVerticallyJoined)) {
            return false;
        }
        if ((xJoined != wmCfg.xJoined) ||
            (yJoined != wmCfg.yJoined) ||
            (widthJoined != wmCfg.widthJoined) ||
            (heightJoined != wmCfg.heightJoined)) {
            return false;
        }
        if ((relativeXJoined != wmCfg.relativeXJoined) ||
            (relativeYJoined != wmCfg.relativeYJoined)) {
            return false;
        }
        if ((relativeWidthJoined != wmCfg.relativeWidthJoined) || 
            (relativeHeightJoined != wmCfg.relativeHeightJoined)) {
            return false;
        }
        if ((maximizeIfWidthBelowJoined != wmCfg.maximizeIfWidthBelowJoined) || 
            (maximizeIfHeightBelowJoined != wmCfg.maximizeIfHeightBelowJoined)) {
            return false;
        }
        
        if (mainWindowFrameStateJoined != wmCfg.mainWindowFrameStateJoined) {
            return false;
        }
        if (mainWindowFrameStateSeparated != wmCfg.mainWindowFrameStateSeparated) {
            return false;
        }
        if (editorAreaState != wmCfg.editorAreaState) {
            return false;
        }
        //Order of editorAreaConstraints array is defined
        if (editorAreaConstraints.length != wmCfg.editorAreaConstraints.length) {
            return false;
        }
        for (int i = 0; i < editorAreaConstraints.length; i++) {
            if (!editorAreaConstraints[i].equals(wmCfg.editorAreaConstraints[i])) {
                return false;
            }
        }
        if ((editorAreaBounds != null) && (wmCfg.editorAreaBounds != null)) {
            if (!editorAreaBounds.equals(wmCfg.editorAreaBounds)) {
                return false;
            }
        } else if ((editorAreaBounds != null) || (wmCfg.editorAreaBounds != null)) {
            return false;
        }
        if ((editorAreaRelativeBounds != null) && (wmCfg.editorAreaRelativeBounds != null)) {
            if (!editorAreaRelativeBounds.equals(wmCfg.editorAreaRelativeBounds)) {
                return false;
            }
        } else if ((editorAreaRelativeBounds != null) || (wmCfg.editorAreaRelativeBounds != null)) {
            return false;
        }
        if (editorAreaFrameState != wmCfg.editorAreaFrameState) {
            return false;
        }
        if ((screenSize != null) && (wmCfg.screenSize != null)) {
            if (!screenSize.equals(wmCfg.screenSize)) {
                return false;
            }
        } else if ((screenSize != null) || (wmCfg.screenSize != null)) {
            return false;
        }
        if (!activeModeName.equals(wmCfg.activeModeName)) {
            return false;
        }
        if (!editorMaximizedModeName.equals(wmCfg.editorMaximizedModeName)) {
            return false;
        }
        if (!viewMaximizedModeName.equals(wmCfg.viewMaximizedModeName)) {
            return false;
        }
        if (!toolbarConfiguration.equals(wmCfg.toolbarConfiguration)) {
            return false;
        }
        if (preferredToolbarIconSize != wmCfg.preferredToolbarIconSize) {
            return false;
        }
        //Order of modes array is NOT defined
        if (modes.length != wmCfg.modes.length) {
            return false;
        }
        for (int i = 0; i < modes.length; i++) {
            ModeConfig modeCfg = null;
            for (int j = 0; j < wmCfg.modes.length; j++) {
                if (modes[i].name.equals(wmCfg.modes[j].name)) {
                    modeCfg = wmCfg.modes[j];
                    break;
                }
            }
            if (modeCfg == null) {
                return false;
            }
            if (!modes[i].equals(modeCfg)) {
                return false;
            }
        }
        //Order of groups array is NOT defined
        if (groups.length != wmCfg.groups.length) {
            return false;
        }
        for (int i = 0; i < groups.length; i++) {
            GroupConfig groupCfg = null;
            for (int j = 0; j < wmCfg.groups.length; j++) {
                if (groups[i].name.equals(wmCfg.groups[j].name)) {
                    groupCfg = wmCfg.groups[j];
                    break;
                }
            }
            if (groupCfg == null) {
                return false;
            }
            if (!groups[i].equals(groupCfg)) {
                return false;
            }
        }
        //Order of tcRefConfigs is defined
        if (tcIdViewList.length != wmCfg.tcIdViewList.length) {
            return false;
        }
        for (int i = 0; i < tcIdViewList.length; i++) {
            if (!tcIdViewList[i].equals(wmCfg.tcIdViewList[i])) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 17;
        
        hash = 37 * hash + (centeredHorizontallySeparated ? 0 : 1);
        hash = 37 * hash + (centeredVerticallySeparated ? 0 : 1);
        hash = 37 * hash + xSeparated;
        hash = 37 * hash + ySeparated;
        hash = 37 * hash + widthSeparated;
        hash = 37 * hash + heightSeparated;
        hash = 37 * hash + Float.floatToIntBits(relativeXSeparated);
        hash = 37 * hash + Float.floatToIntBits(relativeYSeparated);
        hash = 37 * hash + Float.floatToIntBits(relativeWidthSeparated);
        hash = 37 * hash + Float.floatToIntBits(relativeHeightSeparated);
        
        hash = 37 * hash + (centeredHorizontallyJoined ? 0 : 1);
        hash = 37 * hash + (centeredVerticallyJoined ? 0 : 1);
        hash = 37 * hash + xJoined;
        hash = 37 * hash + yJoined;
        hash = 37 * hash + widthJoined;
        hash = 37 * hash + heightJoined;
        hash = 37 * hash + Float.floatToIntBits(relativeXJoined);
        hash = 37 * hash + Float.floatToIntBits(relativeYJoined);
        hash = 37 * hash + Float.floatToIntBits(relativeWidthJoined);
        hash = 37 * hash + Float.floatToIntBits(relativeHeightJoined);
        hash = 37 * hash + maximizeIfWidthBelowJoined;
        hash = 37 * hash + maximizeIfHeightBelowJoined;

        hash = 37 * hash + mainWindowFrameStateJoined;
        hash = 37 * hash + mainWindowFrameStateSeparated;
        hash = 37 * hash + editorAreaState;
        for (int i = 0; i < editorAreaConstraints.length; i++) {
            hash = 37 * hash + editorAreaConstraints[i].hashCode();
        }
        if (editorAreaBounds != null) {
            hash = 37 * hash + editorAreaBounds.hashCode();
        }
        if (editorAreaRelativeBounds != null) {
            hash = 37 * hash + editorAreaRelativeBounds.hashCode();
        }
        hash = 37 * hash + editorAreaFrameState;
        if (screenSize != null) {
            hash = 37 * hash + screenSize.hashCode();
        }
        hash = 37 * hash + activeModeName.hashCode();
        hash = 37 * hash + editorMaximizedModeName.hashCode();
        hash = 37 * hash + viewMaximizedModeName.hashCode();
        hash = 37 * hash + toolbarConfiguration.hashCode();
        hash = 37 * hash + preferredToolbarIconSize;
        for (int i = 0; i < modes.length; i++) {
            hash = 37 * hash + modes[i].hashCode();
        }
        for (int i = 0; i < groups.length; i++) {
            hash = 37 * hash + groups[i].hashCode();
        }
        for (int i = 0; i < tcIdViewList.length; i++) {
            hash = 37 * hash + tcIdViewList[i].hashCode();
        }
        return hash;
    }
    
}
