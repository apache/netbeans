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


import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.DefaultSeparateContainer;
import org.netbeans.core.windows.view.ui.DefaultSplitContainer;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;


/**
 * Class which represents model of mode element for GUI hierarchy. 
 *
 * @author  Peter Zavadsky
 */
public class ModeView extends ViewElement {
    //mkleint - made protected non-final because of SlidingView constructor..
    protected ModeContainer container;

    // PENDING it is valid only for separate mode, consider create of two classes
    // of view, one for split, and second one for separate mode type.
    private int frameState;

    // PENDING devide into two subclasses?
    // Split mode view constructor
    public ModeView(Controller controller, WindowDnDManager windowDnDManager, 
    double resizeWeight, int kind, TopComponent[] topComponents, TopComponent selectedTopComponent) {
        super(controller, resizeWeight);
        
        container = new DefaultSplitContainer(this, windowDnDManager, kind);
        
        setTopComponents(topComponents, selectedTopComponent);
    }
    
    // Separate mode view constructor
    public ModeView(Controller controller, WindowDnDManager windowDnDManager, Rectangle bounds, int kind, int frameState,
    TopComponent[] topComponents, TopComponent selectedTopComponent) {
        super(controller, 0D);
        
        this.frameState = frameState;
        
        container = new DefaultSeparateContainer(this, windowDnDManager, bounds, kind);
        
        setTopComponents(topComponents, selectedTopComponent);
    }

    /** Specialized constructor for SlidingView.
     */
    protected ModeView(Controller controller) {
    super(controller, 0D);
        //mkleint - moved to SlidingView - "side" needs to be initialized first..
//        this.container = new SlideBarContainer(this, windowDnDManager);
        
//        setTopComponents(topComponents, selectedTopComponent);
    }
    
    
    public void setFrameState(int frameState) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
        this.frameState = frameState;
        Component comp = container.getComponent();
        if(comp instanceof Frame) {
            if ((frameState & Frame.ICONIFIED) == Frame.ICONIFIED) {
                timeStamp = System.currentTimeMillis();
            } else {
                timeStamp = 0;
            }
        }
    }

    
//    public void addTopComponent(TopComponent tc) {
//        if(getTopComponents().contains(tc)) {
//            return;
//        }
//        container.addTopComponent(tc);
//    }
    
    public void removeTopComponent(TopComponent tc) {
        if(!getTopComponents().contains(tc)) {
            return;
        }
        container.removeTopComponent(tc);
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent select) {
        container.setTopComponents(tcs, select);
    }
    
//    public void setSelectedTopComponent(TopComponent tc) {
//        container.setSelectedTopComponent(tc);
//    }
    
    public TopComponent getSelectedTopComponent() {
        return container.getSelectedTopComponent();
    }
    
    public void setActive(boolean active) {
        container.setActive(active);
    }
    
    public boolean isActive() {
        return container.isActive();
    }
    
    public List<TopComponent> getTopComponents() {
        return new ArrayList<TopComponent>(Arrays.asList(container.getTopComponents()));
    }
    
    public void focusSelectedTopComponent() {
        container.focusSelectedTopComponent();
    }
    
    public Component getComponent() {
        return container.getComponent();
    }
    
    public void updateName(TopComponent tc) {
        container.updateName(tc);
    }
    
    public void updateToolTip(TopComponent tc) {
        container.updateToolTip(tc);
    }
    
    public void updateIcon(TopComponent tc) {
        container.updateIcon(tc);
    }
    
    public void requestAttention (TopComponent tc) {
        container.requestAttention(tc);
    }

    public void cancelRequestAttention (TopComponent tc) {
        container.cancelRequestAttention(tc);
    }

    public void setAttentionHighlight (TopComponent tc, boolean highlight) {
        container.setAttentionHighlight(tc, highlight);
    }

    public void makeBusy(TopComponent tc, boolean busy) {
        container.makeBusy(tc, busy);
    }

    // XXX
    public void updateFrameState() {
        Component comp = container.getComponent();
        if(comp instanceof Frame) {
            ((Frame)comp).setExtendedState(frameState);
        }
    }
    
    private long timeStamp = 0; 
    
    public void setUserStamp(long stamp) {
        timeStamp = stamp;
    }
    
    public long getUserStamp() {
        return timeStamp;
    }
    
    private long mainWindowStamp = 0;
    
    public void setMainWindowStamp(long stamp) {
        mainWindowStamp = stamp;
    }
    
    public long getMainWindowStamp() {
        return mainWindowStamp;
    }
    
    public String toString() {
        TopComponent selected = container.getSelectedTopComponent();
        return super.toString() + " [selected=" // NOI18N
            + (selected == null ? null : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)) + "]"; // NOI18N
    }

    public boolean updateAWTHierarchy(Dimension availableSpace) {
        // nothing needs to be done here?
//        System.out.println("ModeView:updateAWTHierarchy=" + availableSpace);
        Component comp = container.getComponent();
        boolean result = false;
        if (comp instanceof JComponent) {
            //We don't want to force the component to *calculate* its preferred
            //size, as that is often expensive and we're not interested in the
            //result anyway
            Dimension d = (Dimension) ((JComponent) comp).getClientProperty ("lastAvailableSpace"); //NOI18N
            Dimension currDim = comp.getPreferredSize();
            if (!availableSpace.equals(d) || !availableSpace.equals(currDim)) {
                //We will only return true if we actually did something
                ((JComponent)comp).setPreferredSize(availableSpace);
                ((JComponent)comp).putClientProperty("lastAvailableSpace", availableSpace); //NOI18N
                result = true;
            }
        }
        return result;
    }    
    
    
}

