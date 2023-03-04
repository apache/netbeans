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


import java.awt.*;


/**
 * Class which is used as an access point to data wchih View is responsible
 * to process.
 *
 * @author  Peter Zavadsky
 */
interface WindowSystemAccessor {

    /** Gets bounds of main window for joined(tiled) state. */
    public Rectangle getMainWindowBoundsJoined();

    /** Gets bounds of main window for separated state. */
    public Rectangle getMainWindowBoundsSeparated();

    /** Gets frame state of main window when in joined state. */
    public int getMainWindowFrameStateJoined();
    
    /** Gets frame state of main window when in separated state. */
    public int getMainWindowFrameStateSeparated();
    
    /** Gets editor area bounds. */
    public Rectangle getEditorAreaBounds();
    
    /** Gets editor area state. */
    public int getEditorAreaState();
    
    /** Gets frame state of editor area when in separated state. */
    public int getEditorAreaFrameState();
    
    /** Toolbar config name. */
    public String getToolbarConfigurationName();
    
    /** Gets active mode. */
    public ModeAccessor getActiveModeAccessor();
    
    /** Gets maximized mode. */
    public ModeAccessor getMaximizedModeAccessor();
    
    public ModeStructureAccessor getModeStructureAccessor();
    
    public ModeAccessor findModeAccessor(String modeName);
    
}

