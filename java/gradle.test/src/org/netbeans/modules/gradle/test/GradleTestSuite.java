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

package org.netbeans.modules.gradle.test;

import java.util.Objects;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleTestSuite extends TestSuite {

    private final JvmTestOperationDescriptor operation;

    public GradleTestSuite(JvmTestOperationDescriptor op) {
        super(suiteName(op));
        this.operation = op;
    }

    public JvmTestOperationDescriptor getOperation() {
        return operation;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.operation);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GradleTestSuite other = (GradleTestSuite) obj;
        return Objects.equals(this.getName(), other.getName());
    }

    static String suiteName(OperationDescriptor op) {
        assert op != null;

        if (op instanceof JvmTestOperationDescriptor) {
            JvmTestOperationDescriptor desc = (JvmTestOperationDescriptor)op;
            return desc.getSuiteName() != null ? desc.getSuiteName() : desc.getClassName();
        } else {
            return op.getDisplayName() != null ? op.getDisplayName() : op.getName();
        }
    }

}
