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
package org.netbeans.modules.testng.ui.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation.TestExecutor;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author lukas
 */
@NbBundle.Messages("CTL_DebugTestMethodAction=Debug")
public class DebugTestMethodAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(RunTestMethodAction.class.getName());

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Lookup l = activatedNodes[0].getLookup();
        FileObject fileObject = l.lookup(FileObject.class);
        if (fileObject != null) {
            Project p = FileOwnerQuery.getOwner(fileObject);
            return TestNGSupport.isActionSupported(Action.DEBUG_TESTMETHOD, p);
        }
        SingleMethod sm = l.lookup(SingleMethod.class);
        if (sm != null) {
            Project p = FileOwnerQuery.getOwner(sm.getFile());
            return TestNGSupport.isActionSupported(Action.DEBUG_TESTMETHOD, p);
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Lookup l = activatedNodes[0].getLookup();
        EditorCookie ec = l.lookup(EditorCookie.class);
        SingleMethod sm = l.lookup(SingleMethod.class);
        if (ec == null && sm == null) {
            //should not happen
            throw new UnsupportedOperationException();
        }
        FileObject fo = null;
        String testMethod = null;
        TestClassInfoTask task = new TestClassInfoTask(0);
        if (ec != null) {
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes.length > 0) {
                final int cursor = panes[0].getCaret().getDot();
                JavaSource js = JavaSource.forDocument(panes[0].getDocument());
                task = new TestClassInfoTask(cursor);
                try {
                    js.runUserActionTask(task, true);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
                if (task.getMethodName() == null) {
                    //TODO - cursor is outside of a method or a given method is not a test
                    //so let allow user to choose any available method within given class
                    //using some UI
                }
                fo = l.lookup(FileObject.class);
                testMethod = task.getMethodName();
            }
        }
        if (sm != null) {
            fo = sm.getFile();
            testMethod = sm.getMethodName();
            JavaSource js = JavaSource.forFileObject(fo);
            if(js != null) {
                try {
                    js.runUserActionTask(task, true);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        assert fo != null;
        Project p = FileOwnerQuery.getOwner(fo);
        TestExecutor exec = TestNGSupport.findTestNGSupport(p).createExecutor(p);
        TestConfig conf = TestNGUtils.getTestConfig(fo, false, task.getPackageName(), task.getClassName(), testMethod);
        try {
            exec.execute(Action.DEBUG_TESTMETHOD, conf);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public String getName() {
        return Bundle.CTL_DebugTestMethodAction();
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
