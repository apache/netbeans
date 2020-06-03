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

package org.netbeans.modules.remote.api.ui;

import java.io.File;
import javax.swing.JFileChooser;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 */
public class RemoteFileChooserBuilderTestCase extends NativeExecutionBaseTestCase {

    public RemoteFileChooserBuilderTestCase(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    public RemoteFileChooserBuilderTestCase(String name) {
        super(name);
    }

    @ForAllEnvironments(section="RemoteFileChooserBuilderTestCase")
    public void testRemoteFileChoser() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        FileChooserBuilder fcb = new FileChooserBuilder(env);
        JFileChooser chooser = fcb.createFileChooser();
        int ret = chooser.showDialog(null, "Choose file at " + env.getDisplayName());
        switch (ret) {
            case JFileChooser.CANCEL_OPTION:
                System.err.printf("Canclled\n");
                break;
            case JFileChooser.APPROVE_OPTION:
                System.err.printf("Approved\n");
                File file = chooser.getSelectedFile();
                System.err.printf("Selected: %s\n", file);
                break;
            case JFileChooser.ERROR_OPTION:
                System.err.printf("Error\n");
                break;
        }

    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileChooserBuilderTestCase.class);
    }
}
