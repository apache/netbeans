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
