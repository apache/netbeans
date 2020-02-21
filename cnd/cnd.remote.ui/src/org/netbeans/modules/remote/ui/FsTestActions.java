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
