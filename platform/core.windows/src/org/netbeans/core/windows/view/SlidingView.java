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
import java.util.Map;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.slides.SlideBarContainer;
import org.openide.windows.TopComponent;


/**
 * Model of sliding mode element for GUI hierarchy.
 *
 * @author  Dafe Simonek
 */
public class SlidingView extends ModeView {

    /** Orientation of sliding view, means side where it is located */
    private final String side;
    private Rectangle slideBounds;
    private Map<TopComponent,Integer> slideInSizes;

    public SlidingView(Controller controller, WindowDnDManager windowDnDManager, 
                        TopComponent[] topComponents, 
                        TopComponent selectedTopComponent, 
                        String side, Map<TopComponent,Integer> slideInSizes) {
        super(controller);
        this.side = side;
        this.slideInSizes = slideInSizes;
        // mkleint - needs to be called after side is defined.
        this.container = new SlideBarContainer(this, windowDnDManager);
        setTopComponents(topComponents, selectedTopComponent);
    }
    
    public String getSide() {
        return side;
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return ((SlideBarContainer)this.container).getTabBounds(tabIndex);
    }

    public Rectangle getSlideBounds() {
        Rectangle res = slideBounds;
        
        TopComponent tc = getSelectedTopComponent();
        //check if the slided-in TopComponent has a custom size defined
        if( null != tc ) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            String tcID = wm.findTopComponentID( tc );
            if( wm.isTopComponentMaximizedWhenSlidedIn( tcID ) ) {
                //force maximum size when the slided-in window is maximized,
                //the DesktopImpl will adjust the size to fit the main window
                if( Constants.BOTTOM.equals( side ) || Constants.TOP.equals( side ) ) {
                    res.height = Integer.MAX_VALUE;
                } else {
                    res.width = Integer.MAX_VALUE;
                }
            } else {
                Integer prevSlideSize = slideInSizes.get( tc );
                if( null != prevSlideSize ) {
                    if( null == res )
                        res = tc.getBounds();
                    if( Constants.BOTTOM.equals( side ) || Constants.TOP.equals( side ) ) {
                        res.height = prevSlideSize.intValue();
                    } else {
                        res.width = prevSlideSize.intValue();
                    }
                }
            }
        }
        return res;
    }

    public void setSlideBounds(Rectangle slideBounds) {
        this.slideBounds = slideBounds;
    }
    
    public void setSlideInSizes(Map<TopComponent,Integer> slideInSizes) {
        this.slideInSizes = slideInSizes;
    }
}

