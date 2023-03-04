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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;

/**
 * Factory for possible types of sliding operations with asociated effect.
 *
 * Operations are designed to be sent to winsys to be runned.
 *
 * @author Dafe Simonek
 */
public final class SlideOperationFactory {

    private static final SlidingFx slideInFx = new ScaleFx(0.1f, 0.9f, true);
    private static final SlidingFx slideOutFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoEdgeFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoDesktopFx = new ScaleFx(1.0f, 1.0f, true);

    /** true when slide effects should be applied, false otherwise */
    static final boolean EFFECTS_ENABLED = Boolean.getBoolean("nb.winsys.sliding.effects"); //NOI18N
    
    private SlideOperationFactory() {
        // no need to instantiate
    }
    
    public static SlideOperation createSlideIn(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_IN, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideInFx : null,
                requestActivation);
                
        return result;
    }

    public static SlideOperation createSlideOut(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_OUT, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideOutFx : null,
                requestActivation);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoEdge(Component component, 
        String side, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_EDGE,
                component, side, useEffect && EFFECTS_ENABLED ? slideIntoEdgeFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoDesktop(Component component, 
        int orientation, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_DESKTOP,
                component, orientation, useEffect && EFFECTS_ENABLED ? slideIntoDesktopFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, int orientation) {
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_RESIZE,
                component, orientation, null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, String side) {
        return createSlideResize( component, SlideOperationImpl.side2Orientation( side ) );
    }
}
