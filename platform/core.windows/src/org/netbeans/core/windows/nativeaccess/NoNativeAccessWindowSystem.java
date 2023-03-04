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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.IllegalComponentStateException;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.Icon;

/**
 * Dummy implementation of NativeWindowSystem that does not support any native access.
 * @author S. Aubrecht
 */
class NoNativeAccessWindowSystem extends NativeWindowSystem {

    @Override
    public boolean isWindowAlphaSupported() {
        return false;
    }

    @Override
    public void setWindowAlpha(Window w, float alpha) {
        GraphicsConfiguration gc = w.getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        if (gc.getDevice().getFullScreenWindow() == w) {
            return;
        }
        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            return;
        }
        w.setOpacity(alpha);
    }

    @Override
    public void setWindowMask(Window w, Shape mask) {
        GraphicsConfiguration gc = w.getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        if (gc.getDevice().getFullScreenWindow() == w) {
            return;
        }
        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            return;
        }
        w.setShape(mask);
    }

    @Override
    public void setWindowMask(Window w, Icon mask) {
        //NOOP
    }

    @Override
    public boolean isUndecoratedWindowAlphaSupported() {
        return true;
    }

}
