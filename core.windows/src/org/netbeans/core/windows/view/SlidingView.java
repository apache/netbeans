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

