/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.ReferenceType;
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
