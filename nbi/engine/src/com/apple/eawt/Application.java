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

/* this is stub code written based on Apple EAWT package javadoc published at
 * http://developer.apple.com. It makes compiling code which uses Apple EAWT
 * on non-Mac platforms possible.
 */

package com.apple.eawt;

import java.awt.Point;

public class Application {
    public void addAboutMenuItem() {
        // does nothing
    }
    
    public void addApplicationListener(final ApplicationListener listener) {
        // does nothing
    }
    
    public void addPreferencesMenuItem() {
        // does nothing
    }
    
    public static Application getApplication() {
        return null;
    }
    
    public boolean getEnabledAboutMenu() {
        return false;
    }
    
    public boolean getEnabledPreferencesMenu() {
        return false;
    }
    
    public static Point getMouseLocationOnScreen() {
        return null;
    }
    
    public boolean isAboutMenuItemPresent() {
        return false;
    }
    
    public boolean isPreferencesMenuItemPresent() {
        return false;
    }
    
    public void removeAboutMenuItem() {
        // does nothing
    }
    
    public void removeApplicationListener(final ApplicationListener listener) {
        // does nothing
    }
    
    public void removePreferencesMenuItem() {
        // does nothing
    }
    
    public void setEnabledAboutMenu(final boolean enable) {
        // does nothing
    }
    
    public void setEnabledPreferencesMenu(final boolean enable) {
        // does nothing
    }
}
