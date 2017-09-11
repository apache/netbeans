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

package org.netbeans.modules.profiler.ppoints.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.BorderLayout;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.modules.profiler.ProfilerTopComponent;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.windows.Mode;


/**
 * Top class of the Profiling points view.
 *
 * @author Maros Sandor
 */
@NbBundle.Messages({
    "ProfilingPointsWindow_ComponentName=Profiling Points",
    "ProfilingPointsWindow_ComponentAccessDescr=List of defined profiling points",
    "#NOI18N",
    "ProfilingPointsWindow_WindowMode=output"
})
public class ProfilingPointsWindow extends ProfilerTopComponent {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "ProfilingPointsWindow.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static final long serialVersionUID = 1L;
    private static final String ID = "profiler_pp"; // NOI18N // for winsys persistence
    private static ProfilingPointsWindow defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ProfilingPointsWindowUI windowUI;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilingPointsWindow() {
        setName(Bundle.ProfilingPointsWindow_ComponentName());
        setIcon(Icons.getImage(ProfilingPointsIcons.PPOINT));
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(Bundle.ProfilingPointsWindow_ComponentAccessDescr());
        windowUI = new ProfilingPointsWindowUI();
        add(windowUI, BorderLayout.CENTER);
        setFocusable(true);
        
        defaultInstance = this; // Bug 256689, called by window system persistence outside of getDefault()
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }
    
    public static synchronized boolean hasDefault() {
        return defaultInstance != null;
    }
    
    public static synchronized ProfilingPointsWindow getDefault() {
        if (defaultInstance == null) {
            CommonUtils.runInEventDispatchThreadAndWait(new Runnable() {
                public void run() {
                    defaultInstance = (ProfilingPointsWindow) WindowManager.getDefault().findTopComponent(ID);
                    if (defaultInstance == null) defaultInstance = new ProfilingPointsWindow();
                }
            });
        }

        return defaultInstance;
    }
    
    public static synchronized void closeIfOpened() {
        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                if (defaultInstance != null && defaultInstance.isOpened()) defaultInstance.close();
            }
        });
    }
    
    public boolean needsDocking() {
        return WindowManager.getDefault().findMode(this) == null;
    }

    public void open() {
        if (needsDocking()) { // needs docking

            Mode mode = WindowManager.getDefault().findMode(Bundle.ProfilingPointsWindow_WindowMode());
            if (mode != null) {
                mode.dockInto(this);
            }
        }

        super.open();
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public void notifyProfilingStateChanged() {
        windowUI.notifyProfilingStateChanged();
    }

    protected String preferredID() {
        return ID;
    }
}
