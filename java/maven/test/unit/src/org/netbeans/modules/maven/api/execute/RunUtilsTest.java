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
package org.netbeans.modules.maven.api.execute;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public class RunUtilsTest {
    
    public RunUtilsTest() {
    }
    
    @Test
    public void testIsCompileOnSaveEnabledByDefault() {
        NbBundle.setBranding(null);
        boolean result = RunUtils.isCompileOnSaveEnabled(new MockPrj());
        assertTrue("By default use CoS in NetBeans IDE", result);
    }
    
    @Test
    public void testIsCompileOnSaveEnabledWithBranding() {
        NbBundle.setBranding("test");
        boolean result = RunUtils.isCompileOnSaveEnabled(new MockPrj());
        assertFalse("Allow branding to disable CoS", result);
    }

    private static class MockPrj implements Project {
        public MockPrj() {
        }

        @Override
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(new EmptyAuxProps());
        }

        private static class EmptyAuxProps implements AuxiliaryProperties {
            public EmptyAuxProps() {
            }

            @Override
            public String get(String key, boolean shared) {
                return null;
            }

            @Override
            public void put(String key, String value, boolean shared) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterable<String> listKeys(boolean shared) {
                throw new UnsupportedOperationException();
            }
        }
    }

    // @start region="GeneralPrerequisiteChecker"
    /**
     * Registers a service provider in project's lookup, for all packaging types.
     */
    @ProjectServiceProvider(service= PrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
    public static class GeneralPrerequisiteChecker implements PrerequisitesChecker {
        @Override
        public boolean checkRunConfig(RunConfig config) {
            return true;
        }
    }
    // @end region="GeneralPrerequisiteChecker"

    // @start region="SpecificPrerequisiteChecker"
    /**
     * Registers a service provider for "jar" packaging type only.
     */
    @ProjectServiceProvider(service= PrerequisitesChecker.class, projectType="org-netbeans-modules-maven/jar")
    public static class SpecificPrerequisiteChecker implements PrerequisitesChecker {
        @Override
        public boolean checkRunConfig(RunConfig config) {
            return true;
        }
    }
    // @end region="SpecificPrerequisiteChecker"

    // @start region="FallbackPrerequisiteChecker"
    /**
     * Registers a service fallback, which will be run after all generics and services specific for a packaging type.
     */
    @ProjectServiceProvider(service= PrerequisitesChecker.class, projectType="org-netbeans-modules-maven/_any")
    public static class FallbackPrerequisiteChecker implements PrerequisitesChecker {
        @Override
        public boolean checkRunConfig(RunConfig config) {
            return true;
        }
    }
    // @end region="FallbackPrerequisiteChecker"
}
