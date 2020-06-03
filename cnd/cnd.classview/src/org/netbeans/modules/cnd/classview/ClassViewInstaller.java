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

package org.netbeans.modules.cnd.classview;

import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmModelStateListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.util.NbPreferences;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.CsmModelStateListener.class)
public class ClassViewInstaller implements CsmModelStateListener {
    
    public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
	switch( newState ) {
	    case ON:
		ProjectListener.getInstance().startup();
		break;
	    case CLOSING:
		ProjectListener.getInstance().shutdown();
		break;
	}
    }
    
    private static class ProjectListener implements CsmModelListener {
        private static ProjectListener instance = new ProjectListener();
        private static ProjectListener getInstance() {
            return instance;
        }

        private void shutdown() {
            CsmListeners.getDefault().removeModelListener(this);
        }

        private void startup() {
            CsmListeners.getDefault().addModelListener(this);
        }

        private boolean isDefaultBehavior(){
            Preferences ps = NbPreferences.forModule(ClassViewTopComponent.class);
            return !ps.getBoolean(ClassViewTopComponent.OPENED_PREFERENCE, false);
        }
        
        public void projectOpened(CsmProject project) {
            if (isDefaultBehavior()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ClassViewTopComponent tc = ClassViewTopComponent.findDefault();
                        if (!tc.isOpened()) {
                            tc.open();
                        }
                    }
                });
            }
        }

        public void projectClosed(CsmProject project) {
            if (isDefaultBehavior()) {
                if (CsmModelAccessor.getModel().projects().isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ClassViewTopComponent tc = ClassViewTopComponent.findDefault();
                            if (tc.isOpened()) {
                                tc.closeImplicit();
                            }
                        }
                    });
                }
            }
        }

        public void modelChanged(CsmChangeEvent e) {
        }
        
    }
}
