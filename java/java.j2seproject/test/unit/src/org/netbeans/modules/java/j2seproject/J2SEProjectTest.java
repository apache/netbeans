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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class J2SEProjectTest extends NbTestCase {

    public J2SEProjectTest(String n) {
        super(n);
    }

    /**
     * Regarding projectimport.eclipse.core subpatch in #146582.
     */
    public void testGetLookup() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        Project p = ProjectManager.getDefault().findProject(J2SEProjectGenerator.createProject(
                getWorkDir(), "test", null, null, null, false).getProjectDirectory());
        assertNotNull(p.getLookup().lookup(AuxiliaryConfiguration.class));
        Callable<?> call = p.getLookup().lookup(Callable.class);
        assertNotNull(call);
        assertTrue(call.call() instanceof AuxiliaryConfiguration);
    }

    public void testJVMProperties() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        Project p = ProjectManager.getDefault().findProject(J2SEProjectGenerator.createProject(
                getWorkDir(), "test", null, null, null, false).getProjectDirectory());
        assertNotNull(p);
        final J2SEProject j2sep = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(j2sep);
        final String javaHome = j2sep.evaluator().getProperty("java.home"); //NOI18N
        assertNotNull(javaHome);
        assertEquals(
            JavaPlatform.getDefault().getInstallFolders().iterator().next(),
            FileUtil.toFileObject(FileUtil.normalizeFile(new File(javaHome))));

    }

    @LookupProvider.Registration(projectType="org-netbeans-modules-java-j2seproject")
    public static class TestLookupProvider implements LookupProvider {

        public Lookup createAdditionalLookup(Lookup lkp) {
            Project p = lkp.lookup(Project.class);
            assertNotNull(p);
            Lookup base = p.getLookup();
            assertNotNull(base);
            final AuxiliaryConfiguration aux = base.lookup(AuxiliaryConfiguration.class);
            assertNotNull(aux);
            return Lookups.singleton(new Callable<AuxiliaryConfiguration>() {
                public AuxiliaryConfiguration call() throws Exception {
                    return aux;
                }
            });
        }

    }

}
