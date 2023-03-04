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

package org.netbeans.modules.xml.xam;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponentUpdater3 implements ComponentUpdater<TestComponent3> {
    @Override
    public void update(TestComponent3 target, TestComponent3 child, ComponentUpdater.Operation operation) {
        update(target, child, -1, operation);
    }

    @Override
    public void update(TestComponent3 target, TestComponent3 child, int index, ComponentUpdater.Operation operation) {
        if (operation.equals(ComponentUpdater.Operation.ADD)) {
            //
            // See description of method NsPrefixCreationUndoTest.testInterruptedComponentUpdater()
            if (child instanceof TestComponent3.Err) {
                throw new RuntimeException("Test synch crashed.");
            }
            //
            target.insertAtIndex("ChildComponentAdded", child, index, TestComponent3.class);
        } else {
            target.removeChild("ChildComponentRemoved", child);
        }
    }
}
