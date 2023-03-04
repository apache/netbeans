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
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service = ActionProvider.class)
public class TestNGActionProvider implements ActionProvider {

    private static final Logger LOGGER = Logger.getLogger(TestNGActionProvider.class.getName());
    private static final String[] SUPPORTED_ACTIONS = {
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD
    };

    public TestNGActionProvider() {
    }

    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return context.lookup(SingleMethod.class) != null;
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        TestNGSupport.Action action = SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command)
                ? TestNGSupport.Action.RUN_TESTMETHOD
                : TestNGSupport.Action.DEBUG_TESTMETHOD;
        SingleMethod sm = context.lookup(SingleMethod.class);
        FileObject fo = sm.getFile();

        TestClassInfoTask task = new TestClassInfoTask(0);
        JavaSource js = JavaSource.forFileObject(fo);
        try {
            js.runUserActionTask(task, true);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        Project p = FileOwnerQuery.getOwner(fo);
        TestNGSupportImplementation.TestExecutor exec = TestNGSupport.findTestNGSupport(p).createExecutor(p);
        TestConfig conf = TestNGUtils.getTestConfig(fo, false, task.getPackageName(), task.getClassName(), sm.getMethodName());
        try {
            exec.execute(action, conf);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
