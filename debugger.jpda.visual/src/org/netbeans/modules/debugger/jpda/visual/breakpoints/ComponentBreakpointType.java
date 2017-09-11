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

package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;

import org.netbeans.spi.debugger.ui.BreakpointType;

import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;


/**
* @author Martin Entlicher
*/
@BreakpointType.Registration(displayName="#CTL_AWTComponent_breakpoint_type_name")
public class ComponentBreakpointType extends BreakpointType {

    private Reference<ComponentBreakpointPanel> customizerRef = new WeakReference<ComponentBreakpointPanel>(null);

    @Override
    public String getCategoryDisplayName () {
        return NbBundle.getMessage (
            ComponentBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    @Override
    public JComponent getCustomizer () {
        ComponentBreakpointPanel panel = new ComponentBreakpointPanel();
        customizerRef = new WeakReference<ComponentBreakpointPanel>(panel);
        return panel;
    }

    @Override
    public Controller getController() {
        ComponentBreakpointPanel panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeDisplayName () {
        return NbBundle.getMessage (ComponentBreakpointType.class, 
            "CTL_AWTComponent_breakpoint_type_name"
        );
    }
    
    @Override
    public boolean isDefault () {
        ScreenshotUIManager activeScreenshotManager = ScreenshotUIManager.getActive();
        if (activeScreenshotManager != null) {
            ComponentInfo ci = activeScreenshotManager.getSelectedComponent();
            return (ci instanceof AWTComponentInfo);
        }
        return false;
    }
}
