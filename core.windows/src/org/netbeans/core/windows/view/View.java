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


package org.netbeans.core.windows.view;


import org.netbeans.core.windows.WindowSystemSnapshot;
import java.awt.*;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.openide.windows.TopComponent;


/**
 * Represents view part of window system, define types of changes which are relevant
 * for GUI, and method {@link #changeGUI} which implemenation does all the
 * view's task.
 *
 * @author  Peter Zavadsky
 */
public interface View {

    // Global (the highest) level changes.
    public int CHANGE_VISIBILITY_CHANGED                        = 0;
    public int CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED         = 1;
    public int CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED      = 2;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED    = 3;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED = 4;
    public int CHANGE_EDITOR_AREA_STATE_CHANGED                 = 5;
    public int CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED           = 6;
    public int CHANGE_EDITOR_AREA_BOUNDS_CHANGED                = 7;
    public int CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED           = 8;
    public int CHANGE_ACTIVE_MODE_CHANGED                       = 9;
    public int CHANGE_TOOLBAR_CONFIGURATION_CHANGED             = 10;
    public int CHANGE_MAXIMIZED_MODE_CHANGED                    = 11;
    public int CHANGE_MODE_ADDED                                = 12;
    public int CHANGE_MODE_REMOVED                              = 13;
    public int CHANGE_MODE_CONSTRAINTS_CHANGED                  = 14;

    
    // Mode level changes
    public int CHANGE_MODE_BOUNDS_CHANGED                = 20;
    public int CHANGE_MODE_FRAME_STATE_CHANGED           = 21;
    public int CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED = 22;
    public int CHANGE_MODE_TOPCOMPONENT_ADDED            = 23;
    public int CHANGE_MODE_TOPCOMPONENT_REMOVED          = 24;
    
    // TopComponent level changes
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED            = 31;
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED = 32;
    public int CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED                 = 33;
    public int CHANGE_TOPCOMPONENT_ICON_CHANGED                    = 34;

    // Compound changes
    public int CHANGE_TOPCOMPONENT_ATTACHED            = 41;
    public int CHANGE_TOPCOMPONENT_ARRAY_ADDED         = 42;
    public int CHANGE_TOPCOMPONENT_ARRAY_REMOVED       = 43;
    public int CHANGE_TOPCOMPONENT_ACTIVATED           = 44;
    public int CHANGE_MODE_CLOSED                      = 45;
    public int CHANGE_DND_PERFORMED                    = 46;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED   = 47;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED  = 48;
    
    // Others
    public int CHANGE_UI_UPDATE    = 61;
    
    public int TOPCOMPONENT_REQUEST_ATTENTION = 63;
    public int TOPCOMPONENT_CANCEL_REQUEST_ATTENTION = 64;
    /**
     * @since 2.54
     */
    public int TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON = 65;
    /**
     * @since 2.54
     */
    public int TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF = 66;
    public int CHANGE_MAXIMIZE_TOPCOMPONENT_SLIDE_IN = 67;

    //toggle TopComponent busy
    public int TOPCOMPONENT_SHOW_BUSY = 70;
    public int TOPCOMPONENT_HIDE_BUSY = 71;
    
    /** Provides GUI changes to manifest model changes to user. */
    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot);
    
    // XXX
    public boolean isDragInProgress();
    // XXX
    public Frame getMainWindow();
    
    public Component getEditorAreaComponent();
    
    public String guessSlideSide(TopComponent tc);

    public void userStartedKeyboardDragAndDrop( TopComponentDraggable draggable );
}

