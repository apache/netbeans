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

import java.awt.Dimension;
import javax.swing.JSplitPane;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.windows.TopComponent;

/**
 * Constants in window system.
 *
 * @author  Peter Zavadsky
 */
public abstract class Constants {

    /** Constant that identifies state of editor area */
    public static final int EDITOR_AREA_JOINED    = 0;
    public static final int EDITOR_AREA_SEPARATED = 1;

    /** Constant that identifies mode state. */
    public static final int MODE_STATE_JOINED    = 0;
    public static final int MODE_STATE_SEPARATED = 1;
    
    /** Constant that identifies mode kind */
    public static final int MODE_KIND_EDITOR = TabbedContainer.TYPE_EDITOR;
    public static final int MODE_KIND_VIEW   = TabbedContainer.TYPE_VIEW;
    public static final int MODE_KIND_SLIDING = TabbedContainer.TYPE_SLIDING;

    /** Vertical orientation constant used in constraint. */
    public static final int VERTICAL   = JSplitPane.VERTICAL_SPLIT;
    /** Horizontal orientation constant used in constraint. */
    public static final int HORIZONTAL = JSplitPane.HORIZONTAL_SPLIT;
    
    /** Sides of attaching, used both for regular modes and sliding modes */
    public static final String TOP    = JSplitPane.TOP;
    public static final String BOTTOM = JSplitPane.BOTTOM;
    public static final String LEFT   = JSplitPane.LEFT;
    public static final String RIGHT  = JSplitPane.RIGHT;

    /** Default value when value is not provided by UIManager */
    public static final int DIVIDER_SIZE_VERTICAL   = 4;
    /** Default value when value is not provided by UIManager */
    public static final int DIVIDER_SIZE_HORIZONTAL = 4;

    /** Sets size of drop area (when splitting mode and around area). */
    public static final int DROP_AREA_SIZE = 20;
    
    /** How many pixels is necessary to drag to start the DnD. */ 
    public static final int DRAG_GESTURE_START_DISTANCE = 10;
    /** What time in milliseconds is necessary to hold dragging mouse button for 
     & DnD to be started */
    public static final int DRAG_GESTURE_START_TIME = 200;
    
    // DnD drop ratios.
    /** How big portion of the original mode has to be taken (range from 0.0 to 1.0). */
    public static final double DROP_TO_SIDE_RATIO = 0.5D;
    /** How big portion of the editor area has to be taken (range from 0.0 to 1.0). */
    public static final double DROP_AROUND_EDITOR_RATIO = 0.25D;
    /** How big portion should take the new mode from each one (between which is dropped) (range from 0.0 to 1.0). */
    public static final double DROP_BETWEEN_RATIO = 1.0D/3;
    /** How big portion of entire area should take the dropped mode (range from 0.0 to 1.0). */
    public static final double DROP_AROUND_RATIO = 0.25D;
    
    // XXX
    /** Size of new separated mode when creating during DnD (separated mode). */
    public static final Dimension DROP_NEW_MODE_SIZE = new Dimension(300, 200);

    
    /** Name of client property (of Boolean type) which says whether the TopComponent is allowed
     * to be docked anywhere (even crossing view-editor border). */
    public static final String TOPCOMPONENT_ALLOW_DOCK_ANYWHERE = "TopComponentAllowDockAnywhere"; // NOI18N
    
    /** Name of client property (of Boolean type) which says whether position in model
     * of the TopComponent which is nonpersistent when closed should be kept. */
    public static final String KEEP_NON_PERSISTENT_TC_IN_MODEL_WHEN_CLOSED = "KeepNonPersistentTCInModelWhenClosed"; // NOI18N
    
    /**
     * Name of TopComponent's Boolean client property which forces the window system
     * to respect TopComponent's preferred size when it is slided-in from left/right/bottom 
     * sliding bar when set to Boolean.TRUE. Otherwise the slided-in TopComponent
     * will fill the entire width/length of the IDE window (the default behavior).
     * This switch is intended for tools/palette windows like e.g. color chooser, 
     * tool picker etc.
     * 
     * @since 6.22
     */
    public static final String KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN = TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN;
    
    /**
     * Name of TopComponent's Boolean client property which instructs the window system to activate
     * the given TopComponent at startup time regardless which TopComponent was active at
     * shutdown time. So it's usuable for welcome screen-like behavior. 
     * If more than one TopComponent has this property set to Boolean.TRUE then
     * an arbitrary one will be selected and activated.
     * @since 6.22
     */
    public static final String ACTIVATE_AT_STARTUP = "netbeans.winsys.tc.activate_at_startup"; //NOI18N

    /** Client property to distinguish JWindows/JDialogs used as ui elements
     * for separate modes - floating windows.
     */
    public static final String SEPARATE_WINDOW_PROPERTY = "SeparateWindow";

    // System properties (switches):
    /** Allows user to move <code>TopComponent</code>S between document and view modes, 
     * which is restricted otherwise. */
    public static final boolean SWITCH_MODE_ADD_NO_RESTRICT = Boolean.getBoolean("netbeans.winsys.allow.dock.anywhere"); // NOI18N
    /** Disables DnD of <code>TopComponent</code>S. */
    public static final boolean SWITCH_DND_DISABLE          = Boolean.getBoolean("netbeans.winsys.disable_dnd"); // NOI18N
    /** During DnD it provides nicer feedback (fading of possible drop), however performance is worsen in that case. */
    public static final boolean SWITCH_DROP_INDICATION_FADE = Boolean.getBoolean("netbeans.winsys.dndfade.on"); //NOI18N
    /** Shows the status line at the end of menu bar instead of at the bottom of main window. */
    public static final boolean SWITCH_STATUSLINE_IN_MENUBAR = Boolean.getBoolean("netbeans.winsys.statusLine.in.menuBar"); // NOI18N

    /** Gets the image resource to be used in the empty editor area. */
    public static final String  SWITCH_IMAGE_SOURCE         = System.getProperty("netbeans.winsys.imageSource"); // NOI18N
    
    // XXX #37999
    /** For view, do not show emty documents area, i.e. when no document is opened. */
    public static final boolean SWITCH_HIDE_EMPTY_DOCUMENT_AREA = Boolean.getBoolean("netbeans.winsys.hideEmptyDocArea"); // NOI18N
    
    /** Allowing complete removal of toolbars. */
    public static final boolean NO_TOOLBARS = Boolean.getBoolean("netbeans.winsys.no_toolbars"); // NOI18N

    /** File name whose InstanceCookie can contain custom menu bar component.*/
    public static final String CUSTOM_MENU_BAR_PATH = System.getProperty("netbeans.winsys.menu_bar.path"); // NOI18N

    /** File name whose InstanceCookie can contain custom status line component.*/
    public static final String CUSTOM_STATUS_LINE_PATH = System.getProperty("netbeans.winsys.status_line.path"); // NOI18N

    /** If set to true the help button will not be shown in the dialogs.*/
    public static final boolean DO_NOT_SHOW_HELP_IN_DIALOGS = Boolean.getBoolean("netbeans.winsys.no_help_in_dialogs"); // NOI18N
    
    /** True means automatic iconification/deiconification of all separate frames if main window is iconified/deiconified */ 
    public static final boolean AUTO_ICONIFY = Boolean.getBoolean("netbeans.winsys.auto_iconify"); // NOI18N
    
    /** Determines whether windows should receive focus when being shown or when moved to the front.
     *  By default, auto-focus is true, compatible with Java default.
     *  When JDK-8163591 is fixed, the default may be revisited.
     *  @see java.awt.Window#isAutoRequestFocus() */
    public static final boolean AUTO_FOCUS = System.getProperty("netbeans.winsys.auto_focus") == null || Boolean.getBoolean("netbeans.winsys.auto_focus"); // NOI18N

    private Constants() {}
}
