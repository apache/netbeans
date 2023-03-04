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
package org.netbeans.modules.php.spi.testing.create;

import java.util.Collection;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.Parameters;

// package private class
final class EmptyTestCreatorConfiguration extends TestCreatorConfiguration {

    private final String framework;
    private final CreateTestsSupport createTestsSupport;


    private EmptyTestCreatorConfiguration(String framework, CreateTestsSupport createTestsSupport) {
        assert framework != null;
        assert createTestsSupport != null;
        this.framework = framework;
        this.createTestsSupport = createTestsSupport;
    }

    static TestCreatorConfiguration create(String framework, CreateTestsSupport createTestsSupport) {
        Parameters.notEmpty("framework", framework); // NOI18N
        Parameters.notNull("createTestsSupport", createTestsSupport); // NOI18N
        return new EmptyTestCreatorConfiguration(framework, createTestsSupport);
    }

    @Override
    public boolean canHandleProject(String framework) {
        return this.framework.equals(framework);
    }

    @Override
    public void persistConfigurationPanel(Context context) {
        // noop
    }

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        return createTestsSupport.getTestSourceRoots(createdSourceRoots, fo);
    }

    @Override
    public boolean showClassNameInfo() {
        return false;
    }

    @Override
    public boolean showClassToTestInfo() {
        return false;
    }

    @Override
    public Pair<String, String> getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium) {
        // we are hidden so noop
        return Pair.of("whatever", "donotcareTest"); // NOI18N
    }

}
