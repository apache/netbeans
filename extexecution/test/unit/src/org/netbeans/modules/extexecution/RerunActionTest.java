/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
