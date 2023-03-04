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
