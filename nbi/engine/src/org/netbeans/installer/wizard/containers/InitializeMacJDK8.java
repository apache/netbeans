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

package org.netbeans.installer.wizard.containers;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 *
 * @author christian.oyarzun
 */
public class InitializeMacJDK8 {
    public static void initialize(SwingFrameContainer frameContainer)
    {
        final Application application = Application.getApplication();
        if(application == null) {
            // e.g. running OpenJDK port via X11 on Mac OS X
            return;
        }
        application.removeAboutMenuItem();
        application.removePreferencesMenuItem();
        application.addApplicationListener(new ApplicationAdapter() {

            @Override
            public void handleQuit(ApplicationEvent event) {
                frameContainer.cancelContainer();
            }
        });
        
    }
}
