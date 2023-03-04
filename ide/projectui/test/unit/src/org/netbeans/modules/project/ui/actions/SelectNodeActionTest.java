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

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport.ChangeableLookup;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * @author Jan Lahoda
 */
public class SelectNodeActionTest extends NbTestCase {

    private ChangeableLookup contextLookup;
    private FileObject scratch;
    private FileObject test;
    private DataObject testDO;

    public SelectNodeActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        contextLookup = new ChangeableLookup();
        scratch = TestUtil.makeScratchDir(this);
        test =  scratch.createData("test", "txt");
        testDO = DataObject.find(test);
    }

    public void testEnabledUpdated() throws Exception {
        Action a = SelectNodeAction.inProjects().createContextAwareInstance(contextLookup);

        assertFalse(a.isEnabled());
        contextLookup.change(testDO);
        assertTrue(a.isEnabled());
        contextLookup.change();
        assertFalse(a.isEnabled());
        contextLookup.change(testDO);
        assertTrue(a.isEnabled());
        contextLookup.change(test);
        assertTrue(a.isEnabled());
        contextLookup.change(testDO);
        assertTrue(a.isEnabled());
        contextLookup.change(test);
        assertTrue(a.isEnabled());
        contextLookup.change();
        assertFalse(a.isEnabled());
    }

    @Override
    public boolean runInEQ() {
        return true;
    }

}
