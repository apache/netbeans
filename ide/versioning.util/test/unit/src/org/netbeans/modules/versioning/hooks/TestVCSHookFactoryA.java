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

package org.netbeans.modules.versioning.hooks;

import java.io.File;
import javax.swing.JPanel;
import org.netbeans.modules.versioning.hooks.VCSHookTest.TestVCSHook;

/**
 *
 * @author tomas
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.hooks.VCSHookFactory.class)
public class TestVCSHookFactoryA extends VCSHookFactory<TestVCSHook> {

    @Override
    public TestVCSHook createHook() {
        return new TestVCSHookImplA();
    }

    @Override
    public Class<TestVCSHook> getHookType() {
        return TestVCSHook.class;
    }

    public class TestVCSHookImplA<TestVCSHookContextImplA> extends TestVCSHook {
        @Override
        public JPanel createComponent(VCSHookContext t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public class TestVCSHookContextImplA extends VCSHookContext {
        public TestVCSHookContextImplA(File[] files) {
            super(files);
        }
    }
}
