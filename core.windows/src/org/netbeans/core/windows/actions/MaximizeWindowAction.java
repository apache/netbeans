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

package org.netbeans.core.windows.actions;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.windows.Switches;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;


/** An action that can toggle maximized window system mode for specific window.
 *
 * @author   Peter Zavadsky
 */
public final class MaximizeWindowAction extends AbstractAction implements Presenter.Popup, Presenter.Menu {

    private final PropertyChangeListener propListener;
    private Reference<TopComponent> topComponent;
    
    private JCheckBoxMenuItem menuItem;
    private boolean state = true;
    
    public MaximizeWindowAction() {
        String label = NbBundle.getMessage(MaximizeWindowAction.class, "CTL_MaximizeWindowAction"); //NOI18N
        putValue(Action.NAME, label);
        propListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if(WindowManagerImpl.PROP_MAXIMIZED_MODE.equals(propName)
                || WindowManagerImpl.PROP_EDITOR_AREA_STATE.equals(evt.getPropertyName())
                || WindowManager.PROP_MODES.equals(evt.getPropertyName())
                || WindowManagerImpl.PROP_ACTIVE_MODE.equals(evt.getPropertyName())) {
                    updateState();
                }
                // #64876: correctly initialize after startup 
                if (TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
                    updateState();
                }
            }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.addPropertyChangeListener(WeakListeners.propertyChange(propListener, wm));
        
        updateState();
    }
    /**
     * Alternate constructor to maximize given specific TopComponent.
     * For use in the context menu and maximization on demand,
     * invoked from ActionUtils and TabbedHandler.
     *
     * see #38801 for details
     */
    public MaximizeWindowAction (TopComponent tc) {
        String label = NbBundle.getMessage(MaximizeWindowAction.class, "CTL_MaximizeWindowAction"); //NOI18N
        putValue(Action.NAME, label);
        topComponent = new WeakReference<TopComponent>(tc);
        propListener = null;
        updateState();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    @Override
    public void actionPerformed (java.awt.event.ActionEvent ev) {
        state = !state;
        getMenuItem().setSelected(state);
        
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent curTC = getTCToWorkWith();
        
        if(wm.isDocked(curTC)) {
            // inside main window
            ModeImpl mode = (ModeImpl)wm.findMode(curTC);
            String tcID = wm.findTopComponentID( curTC );
            
            if( mode.getKind() == Constants.MODE_KIND_SLIDING ) {
                //maximize/restore slided-in window
                wm.userToggledTopComponentSlideInMaximize( tcID );
            } else if( null != mode ) {
                ModeImpl previousMax = wm.getCurrentMaximizedMode();
                if( null != previousMax ) {
                    if( previousMax.getKind() == Constants.MODE_KIND_EDITOR && mode.getKind() == Constants.MODE_KIND_VIEW ) {
                        wm.switchMaximizedMode( mode );
                    } else {
                        wm.switchMaximizedMode( null );
                    }
                } else {
                    wm.switchMaximizedMode( mode );
                }
            } else {
                wm.switchMaximizedMode( null );
            }
        } else {
            // separate windows
            ModeImpl curMax = (ModeImpl)wm.findMode(curTC);
            if (curMax != null) {
                if(curMax.getFrameState() == Frame.NORMAL) {
                    curMax.setFrameState(Frame.MAXIMIZED_BOTH);
                } else {
                    curMax.setFrameState(Frame.NORMAL);
                }
            }
        }
        
        updateState();
    }

    /** Updates state of this action, may be called from non-AWT thread.
     * #44825 - Shortcuts folder can call our constructor from non-AWT thread.
     */
    private void updateState() {
        Mutex.EVENT.readAccess( new Runnable() {
            @Override
            public void run() {
                doUpdateState();
            }
        });
    }
    
    /** Updates state and text of this action.
     */
    private void doUpdateState() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent active = getTCToWorkWith();
        boolean maximize;
        ModeImpl activeMode = (ModeImpl)wm.findMode(active);
        if (activeMode == null || !Switches.isTopComponentMaximizationEnabled() || !Switches.isMaximizationEnabled(active)
                || (!wm.isDocked( active ) && !wm.isEditorMode( activeMode )) ) {
            getMenuPresenter().setSelected( false );
            getPopupPresenter().setSelected( false );
            setEnabled(false);
            return;
        }

        if (wm.isDocked(active)) {
            maximize = wm.getCurrentMaximizedMode() != activeMode;
        } else {
            maximize = activeMode.getFrameState() == Frame.NORMAL;
        }
        
        if (activeMode != null && activeMode.getKind() == Constants.MODE_KIND_SLIDING) {
            maximize = null != active && !wm.isTopComponentMaximizedWhenSlidedIn( wm.findTopComponentID( active ) );
        }

        state = !maximize;
        getMenuPresenter().setSelected( state );
        getPopupPresenter().setSelected( state );
        
        setEnabled(activeMode != null /*&& activeMode.getKind() != Constants.MODE_KIND_SLIDING*/);
    }
    
    private TopComponent getTCToWorkWith () {
        if (topComponent != null) {
            TopComponent tc = topComponent.get();
            if (tc != null) {
                return tc;
            }
        }
        return TopComponent.getRegistry().getActivated();
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("MaximizeWindow", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("MaximizeWindow");
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuItem();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getMenuItem();
    }
    
    private JMenuItem getMenuItem() {
        if (menuItem == null) {
            menuItem = new JCheckBoxMenuItem( this );
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
            menuItem.setState( state );
        }
        return menuItem;
    }
    
}

