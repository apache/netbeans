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

package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.ReferenceType;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDAMethodChooserFactory;
import org.netbeans.modules.debugger.jpda.actions.StepIntoActionProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.MethodChooser;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession/Java",
                             types = JPDAMethodChooserFactory.class)
public class JPDAMethodChooserFactoryUIImpl implements JPDAMethodChooserFactory {
    
    private MethodChooser currentMethodChooser;

    @Override
    public boolean initChooserUI(JPDADebuggerImpl debugger, String url, ReferenceType clazz, int methodLine) {
        if (GraphicsEnvironment.isHeadless()) {
            // Not supported in headless environment
            return false;
        }
        final MethodChooserSupport cSupport = new MethodChooserSupport(debugger, url, clazz, methodLine);
        boolean continuedDirectly = cSupport.init();
        if (cSupport.getSegmentsCount() == 0) {
            return false;
        }
        if (continuedDirectly) {
            return true;
        }
        MethodChooser.ReleaseListener releaseListener = new MethodChooser.ReleaseListener() {
            @Override
            public void released(boolean performAction) {
                synchronized (JPDAMethodChooserFactoryUIImpl.this) {
                    currentMethodChooser = null;
                    cSupport.tearDown();
                    if (performAction) {
                        cSupport.doStepInto();
                    }
                }
            }
        };
        MethodChooser chooser = cSupport.createChooser();
        chooser.addReleaseListener(releaseListener);
        boolean success = chooser.showUI();
        if (success && chooser.isUIActive()) {
            synchronized (this) {
                cSupport.tearUp(chooser);
                currentMethodChooser = chooser;
            }
        } else {
            chooser.removeReleaseListener(releaseListener);
        }
        return success;
    }

    @Override
    public boolean cancelUI() {
        synchronized (this) {
            if (currentMethodChooser != null) {
                // perform action
                currentMethodChooser.releaseUI(true);
                return true;
            }
        }
        return false;
    }
    
}
