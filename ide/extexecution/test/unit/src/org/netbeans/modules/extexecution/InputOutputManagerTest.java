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

package org.netbeans.modules.extexecution;

import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class InputOutputManagerTest extends NbTestCase {

    public InputOutputManagerTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        InputOutputManager.clear();
        super.tearDown();
    }

    public void testGet() {
        InputOutput io = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertNull(data.getRerunAction());
        assertNull(data.getStopAction());

        data = InputOutputManager.getInputOutput("test", true, null);
        assertNull(data);

        data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));
        data = InputOutputManager.getInputOutput("test", false, null);
        assertNotNull(data);
    }

    public void testGetActions() {
        StopAction stopAction = new StopAction();
        RerunAction rerunAction = new RerunAction();

        InputOutput io = IOProvider.getDefault().getIO("test", new Action[] {rerunAction, stopAction});
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", stopAction, rerunAction, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);

        data = InputOutputManager.getInputOutput("test", true, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertEquals(rerunAction, data.getRerunAction());
        assertEquals(stopAction, data.getStopAction());

        data = InputOutputManager.getInputOutput("test", true, null);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", stopAction, rerunAction, null));
        data = InputOutputManager.getInputOutput("test", true, null);
        assertNotNull(data);
    }

    public void testGetRequired() {
        InputOutput io = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput(io);
        assertEquals("test", data.getDisplayName());
        assertEquals(io, data.getInputOutput());
        assertNull(data.getRerunAction());
        assertNull(data.getStopAction());

        data = InputOutputManager.getInputOutput(io);
        assertNull(data);

        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(io, "test", null, null, null));
        data = InputOutputManager.getInputOutput(io);
        assertNotNull(data);
    }

    public void testOrder() {
        InputOutput firstIO = IOProvider.getDefault().getIO("test", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(firstIO, "test", null, null, null));
        InputOutput secondIO = IOProvider.getDefault().getIO("test #1", true);
        InputOutputManager.addInputOutput(
                new InputOutputManager.InputOutputData(secondIO, "test #1", null, null, null));

        InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test", data.getDisplayName());
        assertEquals(firstIO, data.getInputOutput());

        data = InputOutputManager.getInputOutput("test", false, null);
        assertEquals("test #1", data.getDisplayName());
        assertEquals(secondIO, data.getInputOutput());

        data = InputOutputManager.getInputOutput("test", false, null);
        assertNull(data);
    }
}
