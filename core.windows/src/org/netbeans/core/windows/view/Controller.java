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


import java.awt.Rectangle;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.windows.TopComponent;


/**
 * Window system controller declaration.
 *
 * @author  Peter Zavadsky
 */
public interface Controller {

    public void userActivatedModeView(ModeView modeView);

    public void userActivatedModeWindow(ModeView modeView);

    public void userActivatedEditorWindow();

    public void userSelectedTab(ModeView modeView, TopComponent selected);
    
    public void userClosingMode(ModeView modeView);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userMovedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeView modeView, int frameState);
    
    public void userResizedModeBounds(ModeView modeView, Rectangle bounds);
    
    public void userMovedSplit(SplitView splitView, ViewElement[] childrenViews, double[] splitWeights);
    
    public void userClosedTopComponent(ModeView modeView, TopComponent tc);

    // DnD
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable);
    
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable, int index);
    
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponentDraggable draggable);
    
    public void userDroppedTopComponentsAround(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsAroundEditor(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponentDraggable draggable, Rectangle bounds);
    
    public void userStartedKeyboardDragAndDrop( TopComponentDraggable draggable );

    // Sliding
    public void userEnabledAutoHide(ModeView modeView, TopComponent tc);
    
    public void userDisabledAutoHide(ModeView modeView, TopComponent tc);
    
    public void userTriggeredSlideIn(ModeView modeView, SlideOperation operation);

    public void userTriggeredSlideOut(ModeView modeView, SlideOperation operation);
    
    public void userTriggeredSlideIntoEdge(ModeView modeView, SlideOperation operation);
    
    public void userTriggeredSlideIntoDesktop(ModeView modeView, SlideOperation operation);

    public void userResizedSlidingWindow(ModeView modeView, SlideOperation operation);

}

