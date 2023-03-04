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
package org.netbeans.modules.jumpto.type;

import java.io.IOException;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;

/**
 *
 * @author Pavel Flaska
 */
public class GoToTypeActionTest extends TestCase {
    private static GoToPanel panel;
    
    public GoToTypeActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestTypeProvider.class);
    }

    public void test1() throws IOException {
        GoToTypeAction action = new GoToTypeAction();
        Iterable<? extends TypeDescriptor> desc = action.getSelectedTypes(false);
        panel = action.panel;
        panel.nameField.setText("Pepik");
        action.waitSearchFinished();
        assertEquals("Provider queried once", 1, TestTypeProvider.count);
    }

    public void testPendingResults() throws IOException {
        TestTypeProvider.count = 10;
        GoToTypeAction action = new GoToTypeAction();
        Iterable<? extends TypeDescriptor> desc = action.getSelectedTypes(false);
        panel = action.panel;
        panel.nameField.setText("Pepik");
        action.waitSearchFinished();
        assertEquals("Provider queried once", 12, TestTypeProvider.count);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static class TestTypeProvider implements TypeProvider {
        private static int count;

        public String name() {
            return "Test";
        }

        public String getDisplayName() {
            return "Test";
        }

        public void computeTypeNames(Context context, Result result) {
            final String text = panel.messageLabel.getText();
            assertTrue(text, text.startsWith("Searching"));
            count++;
            if (count == 10) result.pendingResult();
        }

        public void cancel() {
        }

        public void cleanup() {
        }
        
    }

}
