/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.spi;

import javax.swing.SwingUtilities;
import org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent;

/**
 * 
 * @author Martin Entlicher
 */
public final class ScreenshotUIManager {
    
    public static final String ACTION_TAKE_SCREENSHOT = "takeScreenshot";   // NOI18N
    
    private RemoteScreenshot rs;
    private ScreenshotComponent sc;
    
    ScreenshotUIManager(RemoteScreenshot rs) {
        this.rs = rs;
    }
    
    /**
     * Get an active opened remote screenshot.
     * @return The active screenshot or <code>null</code>.
     */
    public static ScreenshotUIManager getActive() {
        ScreenshotComponent activeComponent = ScreenshotComponent.getActive();
        if (activeComponent != null) {
            return activeComponent.getManager();
        }
        return null;
    }
    
    public RemoteScreenshot getScreenshot() {
        return rs;
    }
    
    public void open() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    open();
                }
            });
            return;
        }
        if (sc == null) {
            sc = new ScreenshotComponent(rs, this);
        }
        sc.open();
        sc.requestActive();
    }
    
    public void requestActive() {
        sc.requestActive();
    }
    
    public boolean close() {
        return sc.close();
    }
    
    /**
     * Get the component selected in the screenshot or <code>null</code>.
     * @return the component or <code>null</code>
     */
    public ComponentInfo getSelectedComponent() {
        if (sc == null) return null;
        return sc.getSelectedComponent();
    }
    
    public void markBreakpoint(ComponentInfo ci) {
        if (sc == null) return ;
        sc.markBreakpoint(ci);
    }
    
    public void unmarkBreakpoint(ComponentInfo ci) {
        if (sc == null) return ;
        sc.unmarkBreakpoint(ci);
    }
    
}
