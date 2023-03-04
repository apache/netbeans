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

package org.netbeans.modules.server.ui.node;

import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RootNodeHiddenActionTest extends NbTestCase {

    public RootNodeHiddenActionTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }



    public void testGetActions() throws Exception {
        RootNode rn = RootNode.getInstance();
        FileObject fo = FileUtil.getConfigFile("Servers/Actions");
        assertNotNull("Folder for actions precreated", fo);
        FileObject x = fo.createData(MyAction.class.getName().replace('.', '-') + ".instance");
        x.setAttribute("position", 37);
        Action[] arr = rn.getActions(true);
        assertEquals("Just one action and two separators found: " + Arrays.asList(arr), 3, arr.length);
        MyAction a = MyAction.get(MyAction.class);
        if (a == arr[0] || a == arr[1] || a == arr[2]) {
            fail("My action shall not be present as it is hidden: " + arr[0] + " 2nd: " + arr[1] + " 3rd: " + arr[2]);
        }
    }

    public static final class MyAction extends CallableSystemAction {
        static int cnt;

        @Override
        protected void initialize() {
            super.initialize();
            putValue("serverNodeHidden", Boolean.TRUE);
        }

        @Override
        public void performAction() {
            cnt++;
        }

        @Override
        public String getName() {
            return "My";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }
}
