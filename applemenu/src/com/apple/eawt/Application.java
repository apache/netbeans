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

