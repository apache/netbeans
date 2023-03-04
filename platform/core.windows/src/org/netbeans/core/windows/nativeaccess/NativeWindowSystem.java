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

package org.netbeans.core.windows.nativeaccess;

import java.awt.Frame;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.Icon;
import org.openide.util.Lookup;

/**
 * Support for access to native, window-related routines in operating system.
 * 
 * @author S. Aubrecht
 */
public abstract class NativeWindowSystem {
    
    /** Instance of dummy window manager. */
    private static NativeWindowSystem dummyInstance;
    
    /**
     * Singleton.
     * 
     * @return Instance of NativeWindowSystem implementation installed in the system.
     */
    public static final NativeWindowSystem getDefault() {
        NativeWindowSystem wmInstance = Lookup.getDefault().lookup(NativeWindowSystem.class);

        return (wmInstance != null) ? wmInstance : getDummyInstance();
    }

    private static synchronized NativeWindowSystem getDummyInstance() {
        if (dummyInstance == null) {
            dummyInstance = new NoNativeAccessWindowSystem();
        }

        return dummyInstance;
    }
    
    /**
     * Check whether it is possible to make top-level windows transparent.
     * 
     * @return True if top-level windows (Frame, Window, Dialog) can be transparent.
     */
    public abstract boolean isWindowAlphaSupported();
    
    /**
     * Check whether it is possible to make top-level UNDECORATED windows transparent.
     * @return  True if top-level undecorated windows (Frame, Window, Dialog) can be transparent.
     * @see Frame#isUndecorated() 
     * @since 2.69
     */
    public abstract boolean isUndecoratedWindowAlphaSupported();
    
    /**
     * Adjust transparency of the given window.
     * 
     * @param w Window, cannot be null.
     * @param alpha Desired transparency, 1.0f - window is fully opaque, 
     * 0.0f - window is fully transparent.
     */
    public abstract void setWindowAlpha( Window w, float alpha );
    
    /**
     *  Adjust the shape of the given window.
     * @param w Window, cannot be null.
     * @param mask New window shape.
     */
    public abstract void setWindowMask( Window w, Shape mask );
    
    /**
     *  Adjust the shape of the given window.
     * @param w Window, cannot be null.
     * @param mask New window shape.
     */
    public abstract void setWindowMask( Window w, Icon mask );
}
