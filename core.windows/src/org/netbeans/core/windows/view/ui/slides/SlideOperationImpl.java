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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.openide.windows.TopComponent;


/**
 * Basic implementation of known types of SlideOperation.
 *
 * Isn't intended to be used directly, but through SlideOperationFactory.
 *
 * @author Dafe Simonek
 */
class SlideOperationImpl implements SlideOperation, ChangeListener {

    /** Type of slide operation */
    private final int type;
    /** Overall component that will be sliden, in winsys top component
     * surrounded by titlebar and border envelope */
    private final Component component;
    /** Slide effect */
    private final SlidingFx effect;
    /** true when component should be activated after slide */
    private final boolean requestsActivation;
    /** Desktop side where slide operation happens */
    private final String side;
    /** Bounds from where should effect start */    
    protected Rectangle startBounds;
    /** Bounds into which should effect finish */
    protected Rectangle finishBounds;
    /** Pane on which operation should take effect */
    private JLayeredPane pane;
    /** layer of layered pane to draw into */
    private Integer layer;
    
    /** Creates a new instance of SlideInOperation */
    SlideOperationImpl(int type, Component component, int orientation, 
         SlidingFx effect, boolean requestsActivation) {
        this(type, component, orientation2Side(orientation), effect, requestsActivation);
    }
    
    SlideOperationImpl(int type, Component component, String side, 
         SlidingFx effect, boolean requestsActivation) {
        this.type = type; 
        this.component = component;
        this.effect = effect;
        this.requestsActivation = requestsActivation;
        this.side = side;
    }

    public void run(JLayeredPane pane, Integer layer) {
        if (effect != null && effect.shouldOperationWait()) {
            // OK, effect is asynchronous and we should wait for effect finish,
            // so register and wait for stateChanged notification
            this.pane = pane;
            this.layer = layer;
            effect.setFinishListener(this);
            effect.showEffect(pane, layer, this);
        } else {
            if (effect != null) {
                effect.showEffect(pane, layer, this);
            }
            performOperation(pane, layer);
        }
    }

    /** Notification of effect finish is delivered here. Invokes operation */
    public void stateChanged(ChangeEvent e) {
        performOperation(pane, layer);
        pane = null;
        layer = null;
    }
    
    private void performOperation(JLayeredPane pane, Integer layer) {
        // XXX - TBD
        switch (type) {
            case SLIDE_IN:
                component.setBounds(finishBounds);
                pane.add(component, layer);
                if( isHeavyWeightShowing() ) {
                    repaintLayeredPane();
                }
                break;
            case SLIDE_OUT:
                pane.remove(component);
                break;
            case SLIDE_RESIZE:
                component.setBounds(finishBounds);
                ((JComponent)component).revalidate();
                if( isHeavyWeightShowing() ) {
                    repaintLayeredPane();
                }
                break;
        }
    }

    public void setFinishBounds(Rectangle bounds) {
        this.finishBounds = bounds;
    }

    public void setStartBounds(Rectangle bounds) {
        this.startBounds = bounds;
    }

    public String getSide() {
        return side;
    }

    public Component getComponent() {
        return component;
    }

    public Rectangle getFinishBounds() {
        return finishBounds;
    }

    public Rectangle getStartBounds() {
        return startBounds;
    }

    public boolean requestsActivation() {
        return requestsActivation;
    }

    protected static String orientation2Side (int orientation) {
        String side = Constants.LEFT; 
        if (orientation == SlideBarDataModel.WEST) {
            side = Constants.LEFT;
        } else if (orientation == SlideBarDataModel.EAST) {
            side = Constants.RIGHT;
        } else if (orientation == SlideBarDataModel.SOUTH) {
            side = Constants.BOTTOM;
        } else if (orientation == SlideBarDataModel.NORTH) {
            side = Constants.TOP;
        }
        return side;
    }

    public int getType () {
        return type;
    }

    public void prepareEffect() {
        if (effect != null) {
            effect.prepareEffect(this);
        }
    }        

    static int side2Orientation(String side) {
        int orientation = SlideBarDataModel.WEST; 
        if (Constants.LEFT.equals(side)) {
            orientation = SlideBarDataModel.WEST;
        } else if (Constants.RIGHT.equals(side)) {
            orientation = SlideBarDataModel.EAST;
        } else if (Constants.BOTTOM.equals(side)) {
            orientation = SlideBarDataModel.SOUTH;
        } else if (Constants.TOP.equals(side)) {
            orientation = SlideBarDataModel.NORTH;
        }
        return orientation;
    }

    private void repaintLayeredPane() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Frame f = WindowManagerImpl.getInstance().getMainWindow();
                if( f instanceof JFrame ) {
                    JLayeredPane lp = ((JFrame)f).getLayeredPane();
                    if( null != lp ) {
                        lp.invalidate();
                        lp.revalidate();
                        lp.repaint();
                    }
                }
            }
        });
    }

    private boolean isHeavyWeightShowing() {
        for( TopComponent tc : TopComponent.getRegistry().getOpened() ) {
            if( !tc.isShowing() )
                continue;
            if( containsHeavyWeightChild( tc ) )
                return true;
        }
        return false;
    }

    private boolean containsHeavyWeightChild( Container c ) {
        if( !c.isLightweight() )
            return true;
        for( Component child : c.getComponents() ) {
            if( null != child && !child.isLightweight() )
                return true;
            if( child instanceof Container && containsHeavyWeightChild((Container) child) )
                return true;
        }
        return false;
    }
}
