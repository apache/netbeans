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
package org.netbeans.modules.remote.ui;

import java.util.ArrayList;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 */
public class FsTestActions {

    private FsTestActions() {}
    
    @ActionID(id = "org.netbeans.modules.remote.ui.TestPerformanceAction", category = "Window")
    @ActionRegistration(displayName = "#ActionTestPerf", lazy = true)
    @ActionReference(position = 900, path = "UI/ToolActions/Files")
    public static ContextAwareAction add() { 
        return TEST_PERF_ACTION; 
    }
    
    private static class TestPerfAction extends NodeAction {
        
        static final long serialVersionUID =-6471281373153172312L;

        public TestPerfAction() {
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        }
        
        @Override
        public boolean enable (Node[] arr) {
            if (!TEST || arr == null || arr.length == 0) {
                return false;
            }
            for (int i = 0; i < arr.length; i++) {
                DataObject dataObject = arr[i].getCookie(DataObject.class);
                if (! isAllowed(dataObject)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isAllowed(DataObject dataObject) {
            return dataObject != null && dataObject.getPrimaryFile().isFolder();
        }
        
        @Override
        public String getName() {
            return NbBundle.getMessage (FsTestActions.class, "ActionTestPerf"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected void performAction (final Node[] activatedNodes) {
            if (activatedNodes == null) {
                return;
            }
            List<FileObject> fileObjects = new ArrayList<>();
            for (Node node : activatedNodes) {
                FileObject fo = node.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    fileObjects.add(fo);
                }
            }
            if (fileObjects.size() > 0) {
                StringBuilder title = new StringBuilder("PERFTEST "); //NOI18N
                title.append(fileObjects.get(0).getPath());
                if (fileObjects.size() > 1) {
                    title.append("..."); // NOI18N
                }
                InputOutput io = IOProvider.getDefault().getIO(title.toString(), true);
                FsTests.testLs(fileObjects, io.getOut(), io.getOut());
            }
        }
    }
    


    
    private static final TestPerfAction TEST_PERF_ACTION = new TestPerfAction();
    private static final boolean TEST = Boolean.getBoolean("cnd.remote.test.perf");
}
