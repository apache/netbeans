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
package org.netbeans.core.output2;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.IOProvider;

/**
 *
 * @author jhavlin
 */
public class ControllerTest extends NbTestCase {

    public ControllerTest(String name) {
        super(name);
    }

    public void testUpdaterRemovesUnavailableTabs() throws InterruptedException,
            InvocationTargetException {
        Controller c = new Controller();
        final Controller.CoalescedNameUpdater updater =
                c.new CoalescedNameUpdater();
        final NbIO io = (NbIO) IOProvider.getDefault().getIO(
                "test", true);                                          //NOI18N
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                OutputTab ot = new OutputTab(io);
                updater.add(ot);
                assertTrue(updater.contains(ot));
                updater.run();
                assertFalse(updater.contains(ot));
            }
        });
    }
}
