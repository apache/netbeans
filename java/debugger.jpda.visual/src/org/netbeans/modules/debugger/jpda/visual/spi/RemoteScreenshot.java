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
package org.netbeans.modules.debugger.jpda.visual.spi;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;

/**
 * Represents screenshot of a remote application.
 * 
 * @author Martin Entlicher
 */
public final class RemoteScreenshot {
    
    //private static final Logger logger = Logger.getLogger(RemoteScreenshot.class.getName());
    
    //private static final RemoteScreenshot[] NO_SCREENSHOTS = new RemoteScreenshot[] {};

    private DebuggerEngine engine;
    private String title;
    private Image image;
    private ComponentInfo componentInfo;
    private ScreenshotUIManager uiManager;
    
    public RemoteScreenshot(DebuggerEngine engine, String title, int width, int height,
                            Image image, ComponentInfo componentInfo) {
        this.engine = engine;
        this.title = title;
        this.image = image;
        this.componentInfo = componentInfo;
    }
    
    public DebuggerEngine getDebuggerEngine() {
        return engine;
    }
    
    public String getTitle() {
        return title;
    }
    
    public Image getImage() {
        return image;
    }
    
    public ComponentInfo getComponentInfo() {
        return componentInfo;
    }
    
    /** The component info or <code>null</code> */
    public ComponentInfo findAt(int x, int y) {
        return componentInfo.findAt(x, y);
    }
    
    public ScreenshotUIManager getScreenshotUIManager() {
        if (uiManager == null) {
            uiManager = new ScreenshotUIManager(this);
        }
        return uiManager;
    }
    
}
