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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.extexecution.ExecutionDescriptor.RerunCallback;
import org.netbeans.api.extexecution.ExecutionDescriptor.RerunCondition;
import org.netbeans.api.extexecution.ExecutionService;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class RerunActionTest extends NbTestCase {

    public RerunActionTest(String name) {
        super(name);
    }

    public void testReRun() {
        RerunAction action = new RerunAction();
        action.actionPerformed(null); // must pass

        // TODO test real run
    }

    public void testCondition() {
        RerunAction action = new RerunAction();
        TestCondition condition = new TestCondition(true);
        assertFalse(action.isEnabled());
        action.setEnabled(true);
        assertTrue(action.isEnabled());

        action.setRerunCondition(condition);
        assertTrue(action.isEnabled());
        condition.setRerunPossible(false);
        assertFalse(action.isEnabled());
        condition.setRerunPossible(true);
        assertTrue(action.isEnabled());

        action.setRerunCondition(null);
        assertTrue(action.isEnabled());

        action.setRerunCondition(condition);
        condition.setRerunPossible(false);
        assertFalse(action.isEnabled());
        action.setRerunCondition(null);
        assertTrue(action.isEnabled());

        action.setRerunCondition(condition);
        assertFalse(action.isEnabled());
        condition.setRerunPossible(true);
        action.setEnabled(false);
        assertFalse(action.isEnabled());
    }

    public void testCallback() throws InterruptedException {
        ExecutionDescriptor desc = new ExecutionDescriptor();
        ExecutionService service = ExecutionService.newService(new Callable<Process>() {

            @Override
            public Process call() throws Exception {
                return new TestProcess();
            }
        }, desc, "Test"); // NOI18N

        CountDownLatch latch = new CountDownLatch(1);
        TestCallback callback = new TestCallback(latch);
        RerunAction action = new RerunAction();
        action.setExecutionService(service);
        action.setRerunCallback(callback);
        action.actionPerformed(null);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    private static class TestCondition implements RerunCondition {

        private boolean rerunPossible;

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public TestCondition(boolean rerunPossible) {
            this.rerunPossible = rerunPossible;
        }

        public void setRerunPossible(boolean rerunPossible) {
            this.rerunPossible = rerunPossible;
            changeSupport.fireChange();
        }

        public boolean isRerunPossible() {
            return rerunPossible;
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
    }

    private static class TestProcess extends Process {

        private final InputStream is = new ByteArrayInputStream(new byte[]{});

        @Override
        public OutputStream getOutputStream() {
            return new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                }
            };
        }

        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public InputStream getErrorStream() {
            return is;
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }

    }

    private static class TestCallback implements RerunCallback {

        private final CountDownLatch latch;

        public TestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void performed(Future<Integer> task) {
            if (task != null) {
                latch.countDown();
            }
        }
    }
}
