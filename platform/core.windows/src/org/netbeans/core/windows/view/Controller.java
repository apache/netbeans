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

