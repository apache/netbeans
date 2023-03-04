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
package org.netbeans.modules.welcome;

import java.util.Set;
import org.openide.modules.ModuleInstall;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Checks the feedback survey.
 */
public class Installer extends ModuleInstall implements Runnable {

    @Override public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(this);
        WindowManager.getDefault().addWindowSystemListener( new WindowSystemListener() {

            @Override
            public void beforeLoad( WindowSystemEvent event ) {
            }

            @Override
            public void afterLoad( WindowSystemEvent event ) {
            }

            @Override
            public void beforeSave( WindowSystemEvent event ) {
                WindowManager.getDefault().removeWindowSystemListener( this);
                WelcomeComponent topComp = null;
                boolean isEditorShowing = false;
                Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
                for (Mode mode : WindowManager.getDefault().getModes()) {
                    TopComponent tc = mode.getSelectedTopComponent();
                    if (tc instanceof WelcomeComponent) {                
                        topComp = (WelcomeComponent) tc;               
                    }
                    if( null != tc && WindowManager.getDefault().isEditorTopComponent( tc ) )
                        isEditorShowing = true;
                }
                if( WelcomeOptions.getDefault().isShowOnStartup() && isEditorShowing ) {
                    if(topComp == null){            
                        topComp = WelcomeComponent.findComp();
                    }
                    //activate welcome screen at shutdown to avoid editor initialization
                    //before the welcome screen is activated again at startup
                    topComp.open();
                    topComp.requestActive();
                } else if( topComp != null ) {
                    topComp.close();
                }
            }

            @Override
            public void afterSave( WindowSystemEvent event ) {
            }
        });
    }

    @Override
    public void run() {
        FeedbackSurvey.start();
    }
}
