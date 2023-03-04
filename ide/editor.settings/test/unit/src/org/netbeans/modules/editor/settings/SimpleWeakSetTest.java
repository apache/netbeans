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

package org.netbeans.modules.editor.settings;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;

/**
 * Test of SimpleWeakSet functionality.
 *
 *  @author Miloslav Metelka
 */
public class SimpleWeakSetTest extends NbTestCase {

    public SimpleWeakSetTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testRandom() throws Exception {
        RandomTestContainer container = SimpleWeakSetTesting.createContainer();
        container.setLogOp(true);
        int opCount = 1000;
        SimpleWeakSetTesting.addRoundPreferAdd(container, opCount);
        SimpleWeakSetTesting.addRoundPreferRemove(container, opCount);
        container.runInit(1274381066314L);
        container.runOps(20);
//        container.runOps(1);
        container.runOps(0); // till end

        container.run(0L); // Run till end
    }

}
