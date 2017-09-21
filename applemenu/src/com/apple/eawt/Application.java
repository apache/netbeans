/**
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
 * http://developer.apple.com.  It makes compiling code which uses Apple EAWT
 * on non-Mac platforms possible.  The compiled stub classes should never be
 * included in the final product.
 */

package com.apple.eawt;

import java.awt.Image;
import java.awt.PopupMenu;
import javax.swing.JMenuBar;

public class Application
{
    public static Application getApplication() { return null; }
    public void addAppEventListener(final AppEventListener listener) {}
    public void removeAppEventListener(final AppEventListener listener) {}
    public void setAboutHandler(final AboutHandler aboutHandler) {}
    public void setPreferencesHandler(final PreferencesHandler preferencesHandler) {}
    public void setOpenFileHandler(final OpenFilesHandler openFileHandler) {}
    public void setPrintFileHandler(final PrintFilesHandler printFileHandler) {}
    public void setOpenURIHandler(final OpenURIHandler openURIHandler) {}
    public void setQuitHandler(final QuitHandler quitHandler) {}
    public void setQuitStrategy(final QuitStrategy strategy) {}
    public void enableSuddenTermination() {}
    public void disableSuddenTermination() {}
    public void requestForeground(final boolean allWindows) {}
    public void requestUserAttention(final boolean critical) {}
    public void openHelpViewer() {}
    public void setDockMenu(final PopupMenu menu) {}
    public PopupMenu getDockMenu() { return null; }
    public void setDockIconImage(final Image image) {}
    public Image getDockIconImage() { return null; }
    public void setDockIconBadge(final String badge) {}
    public void setDefaultMenuBar(final JMenuBar menuBar) {}
}

