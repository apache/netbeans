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


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.model.ModelElement;
import org.openide.windows.TopComponent;

import java.awt.*;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;


/**
 * Class which handles controller requests.
 *
 * @author  Peter Zavadsky
 */
public interface ControllerHandler {

    public void userActivatedMode(ModeImpl mode);

    public void userActivatedModeWindow(ModeImpl mode);

    public void userActivatedEditorWindow();
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState);
    
    public void userChangedSplit( ModelElement[] snapshots, double[] splitWeights );
    
    public void userClosedTopComponent(ModeImpl mode, TopComponent tc);
    
    public void userClosedMode(ModeImpl mode);
    
    // Helpers>>
    public void userResizedMainWindowBoundsSeparatedHelp(Rectangle bounds);
    
    public void userResizedEditorAreaBoundsHelp(Rectangle bounds);
    
    public void userResizedModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    // Helpers<<
    
    // DnD>>
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable, int index);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponentDraggable draggable);
    
    public void userDroppedTopComponentsAround(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsAroundEditor(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponentDraggable draggable, Rectangle bounds);
    // DnD<<

    // undock/dock
    public void userUndockedTopComponent(TopComponent tc, ModeImpl mode);

    public void userDockedTopComponent(TopComponent tc, ModeImpl mode);

    // Sliding>>
    public void userEnabledAutoHide(TopComponent tc, ModeImpl source, String target);
    
    public void userDisabledAutoHide(TopComponent tc, ModeImpl source);
    
    public void userResizedSlidingMode(ModeImpl mode, Rectangle rect);
    // Sliding<<
    
}

