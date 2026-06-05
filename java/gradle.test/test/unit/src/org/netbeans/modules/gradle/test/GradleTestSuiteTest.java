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
package org.netbeans.modules.gradle.test;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestKind;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author William Price
 */
public class GradleTestSuiteTest {

    @Test
    public void suiteNameTestDescriptorClassName() {
      StubTestDescriptor op = new StubTestDescriptor();
      op.className = this.getClass().getName();
      assertEquals(this.getClass().getName(), GradleTestSuite.suiteName(op));
    }

    @Test
    public void suiteNameTestDescriptorUnknownClass() {
      StubTestDescriptor op = new StubTestDescriptor();
      op.className = null;
      assertEquals(op.suiteName, GradleTestSuite.suiteName(op));
    }

    @Test
    public void suiteNameTestDescriptorUnknownClassAndSuite() {
      StubTestDescriptor op = new StubTestDescriptor();
      op.className = null;
      op.suiteName = null;
      assertEquals(op.displayName, GradleTestSuite.suiteName(op));
    }

    @Test
    public void suiteNameNotATestDescriptor() {
      StubOpDescriptor op = new StubOpDescriptor();
      assertEquals(op.displayName, GradleTestSuite.suiteName(op));
    }

    @Test
    public void suiteNameNotATestDescriptorNoDisplayName() {
      StubOpDescriptor op = new StubOpDescriptor();
      op.displayName = null;
      assertEquals(op.name, GradleTestSuite.suiteName(op));
    }

    private static class StubOpDescriptor implements OperationDescriptor {
        String displayName = "display name";
        String name = "op name";

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public OperationDescriptor getParent() {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubTestDescriptor extends StubOpDescriptor implements JvmTestOperationDescriptor {
        String className = "class name";
        String suiteName = "suite name";

        @Override
        public JvmTestKind getJvmTestKind() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public String getSuiteName() {
            return suiteName;
        }

        @Override
        public String getMethodName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTestDisplayName() {
            throw new UnsupportedOperationException();
        }
    }
}
