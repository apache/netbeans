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

/* this is stub code written based on Apple EAWT package javadoc published at
 * http://developer.apple.com. It makes compiling code which uses Apple EAWT
 * on non-Mac platforms possible.
 */

package com.apple.eawt;

import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;

/**
 * Note: A lot of methods which used to be in here were deprecated as of java 6 and got removed
 * in java 11. Thus this stub classes have been adjusted accordingly. If your code fails to compile
 * have a look at the javadoc for Application on Java 6 and follow the deprecation hints.
 * It might be necessary to add additional handlers and events to this class.
 * In addition to that, lot of apple specific code has been adopted to the java.awt.desktop packages natively.
 */
public class Application {

    public static Application getApplication() {
        return null;
    }

    /**
     * @since Java for Mac OS X 10.6 Update 3, Java for Mac OS X 10.5 Update 8
     */
    public void setQuitHandler(java.awt.desktop.QuitHandler quitHandler) {
        // does nothing
    }

    /**
     * Installs a handler to show a custom About window for your application.<br/>
     * Setting the {@link AboutHandler} to <code>null</code> reverts it to the default Cocoa About window.
     *
     * @param aboutHandler the handler to respond to the AboutHandler#handleAbout() message
     * @since Java for Mac OS X 10.6 Update 3, Java for Mac OS X 10.5 Update 8
     */
    public void setAboutHandler(java.awt.desktop.AboutHandler aboutHandler) {
        // does nothing
    }

    /**
     * Installs a handler to create the Preferences menu item in your application's app menu.<br/>
     * Setting the {@link PreferencesHandler} to <code>null</code> will remove the Preferences item from the app menu.
     *
     * @param preferencesHandler
     * @since Java for Mac OS X 10.6 Update 3, Java for Mac OS X 10.5 Update 8
     */
    public void setPreferencesHandler(java.awt.desktop.PreferencesHandler preferencesHandler) {
        // does nothing
    }

}
