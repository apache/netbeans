/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.nativeaccess;

import com.sun.jna.platform.WindowUtils;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;

/**
 * Implementation of NativeWindowSystem based on JNA library.
 * 
 * @author S. Aubrecht
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.windows.nativeaccess.NativeWindowSystem.class)
public class NativeWindowSystemImpl extends NativeWindowSystem {

    private static final Logger LOG = Logger.getLogger(NativeWindowSystemImpl.class.getName());

    public NativeWindowSystemImpl() {
    }

    @Override
    public boolean isWindowAlphaSupported() {
        boolean res = false;
        try {
            res = WindowUtils.isWindowAlphaSupported();
        } catch (UnsatisfiedLinkError e) {
            // E.g. "Unable to load library 'X11': libX11.so: cannot open shared object file: No such file or directory"
            // on headless build machine (missing libx11-dev.deb)
            LOG.log(Level.FINE, null, e);
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
        return res;
    }

    @Override
    public void setWindowAlpha(Window w, float alpha) {
        GraphicsConfiguration gc = w.getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        //check if JDK APIs are supported
        if (gc.getDevice().getFullScreenWindow() != w && isUndecorated(w)) {
            if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT) ) {
                try {
                    w.setOpacity(alpha);
                    return;
                } catch( Exception e ) {
                    //ignore, we'll try JNA
                }
            }
        }
        // Test isWindowAlphaSupported first to avoid an unnecessary log message.
        if (WindowUtils.isWindowAlphaSupported()) {
            //try the JNA way
            try {
                WindowUtils.setWindowAlpha(w, alpha);
            } catch( Throwable e ) {
                LOG.log(Level.INFO, null, e);
            }
        }
    }
    
    private static boolean isUndecorated( Window w ) {
        if( w instanceof Dialog ) {
            return ((Dialog)w).isUndecorated();
        }
        if( w instanceof Frame ) {
            return ((Frame)w).isUndecorated();
        }            
        return true;
    }

    @Override
    public void setWindowMask(Window w, Shape mask) {
        GraphicsConfiguration gc = w.getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        //check if JDK APIs are supported
        if (gc.getDevice().getFullScreenWindow() != w) {
            if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                try {
                    w.setShape(mask);
                    return;
                } catch( Exception e ) {
                    //ignore, we'll try JNA
                }
            }
        }
        //try the JNA way
        if( true )
            return;
        try {
            WindowUtils.setWindowMask(w, mask);
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
    }

    @Override
    public void setWindowMask(Window w, Icon mask) {
        try {
            WindowUtils.setWindowMask(w, mask);
        } catch( Throwable e ) {
            LOG.log(Level.INFO, null, e);
        }
    }

    @Override
    public boolean isUndecoratedWindowAlphaSupported() {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        return config.getDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    }
}
