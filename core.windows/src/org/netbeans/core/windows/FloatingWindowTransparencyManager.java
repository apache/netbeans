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

package org.netbeans.core.windows;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Tracks activated TopComponents and makes floating windows semi-transparent
 * when they're not activated.
 * 
 * @author S. Aubrecht
 */
public class FloatingWindowTransparencyManager {

    private static FloatingWindowTransparencyManager theInstance;
    private static final RequestProcessor RP = new RequestProcessor("FloatingWindowTransparencyManager"); //NOI18N
    
    private PropertyChangeListener topComponentRegistryListener;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object LOCK = new Object();
    
    private FloatingWindowTransparencyManager() {
        
    }
    
    public static synchronized  FloatingWindowTransparencyManager getDefault() {
        if( null == theInstance ) {
            theInstance = new FloatingWindowTransparencyManager();
        }
        return theInstance;
    }
    
    public void start() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                synchronized( LOCK ) {
                    initialized.set(true);
                    if( !NativeWindowSystem.getDefault().isWindowAlphaSupported() )
                        return;
                    if( null == topComponentRegistryListener ) {
                        topComponentRegistryListener = new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                SwingUtilities.invokeLater( new Runnable() {
                                    @Override
                                    public void run() {
                                        toggleFloatingWindowTransparency();
                                    }
                                });
                            }
                        };
                        TopComponent.getRegistry().addPropertyChangeListener(topComponentRegistryListener);
                    }
                }
            }
        } ).start();
    }
    
    public void stop() {
        synchronized( LOCK ) {
            if( null != topComponentRegistryListener ) {
                TopComponent.getRegistry().removePropertyChangeListener(topComponentRegistryListener);
                topComponentRegistryListener = null;
            }
        }
    }
    
    public void update() {
        toggleFloatingWindowTransparency();
    }
    
    protected void toggleFloatingWindowTransparency() {
        if( !initialized.get() )
            return;
        
        if( !NativeWindowSystem.getDefault().isWindowAlphaSupported() )
            return;
        
        if( WinSysPrefs.HANDLER.getBoolean( WinSysPrefs.TRANSPARENCY_FLOATING, false) ) {
            TopComponent currentActive = TopComponent.getRegistry().getActivated();
            if( null != currentActive ) {
                final WindowManagerImpl wm = WindowManagerImpl.getInstance();
                //turn off transparency for active floating window
                ModeImpl currentActiveMode = (ModeImpl)wm.findMode( currentActive );
                if( null != currentActiveMode 
                        && currentActiveMode.getState() == Constants.MODE_STATE_SEPARATED
                        && currentActiveMode.getKind() != Constants.MODE_KIND_EDITOR) {

                    Window w = SwingUtilities.windowForComponent(currentActive);
                    if( null != w ) {
                        NativeWindowSystem.getDefault().setWindowAlpha( w, 1.0f );
                    }
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if( !SwingUtilities.isEventDispatchThread() ) {
                            SwingUtilities.invokeLater( this );
                            return;
                        }
                        TopComponent activeTc = TopComponent.getRegistry().getActivated();
                        if( null == activeTc )
                            return;

                        ModeImpl activeMode = (ModeImpl)wm.findMode( activeTc );
                        makeFloatingWindowsTransparent( activeMode );
                    }
                };
                RP.post(runnable, WinSysPrefs.HANDLER.getInt(WinSysPrefs.TRANSPARENCY_FLOATING_TIMEOUT, 1000));
            }
        } else {
            //floating window transparency is disabled, so turn it off for all floating windows
            turnTransparencyOff();
        }
    }
    
    private void turnTransparencyOff() {
        NativeWindowSystem nws = NativeWindowSystem.getDefault();
        for( ModeImpl m : WindowManagerImpl.getInstance().getModes() ) {
            if( m.getState() != Constants.MODE_STATE_SEPARATED
                    || m.getKind() == Constants.MODE_KIND_EDITOR )
                continue;
            TopComponent tc = m.getSelectedTopComponent();
            if( null != tc ) {
                Window w = SwingUtilities.windowForComponent(tc);
                if( null != w ) {
                    nws.setWindowAlpha( w, 1.0f );
                }
            }
        }
    }

    private void makeFloatingWindowsTransparent( ModeImpl activeMode ) {
        float alpha = WinSysPrefs.HANDLER.getFloat(WinSysPrefs.TRANSPARENCY_FLOATING_ALPHA, 0.5f);
        NativeWindowSystem nws = NativeWindowSystem.getDefault();
        for( ModeImpl m : WindowManagerImpl.getInstance().getModes() ) {
            if( m.getState() != Constants.MODE_STATE_SEPARATED 
                    || m.equals( activeMode )
                    || m.getKind() == Constants.MODE_KIND_EDITOR )
                continue;
            TopComponent tc = m.getSelectedTopComponent();
            if( null != tc ) {
                Window w = SwingUtilities.windowForComponent(tc);
                if( null != w ) {
                    nws.setWindowAlpha( w, alpha );
                }
            }
        }

    }
}
