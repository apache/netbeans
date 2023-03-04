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
package org.netbeans.modules.projectapi;

import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

public class ParallelInitializationTest extends NbTestCase {

    public ParallelInitializationTest(String name) {
        super(name);
    }

    public void testProjectWithProjectServiceProvider() throws Exception {
        int instances = MySupport.INSTANCES.get();
        class P implements Project {
            final Lookup l = LookupProviderSupport.createCompositeLookup(
                Lookups.singleton(this), 
                "Projects/org-netbeans-test-parallel/Lookup"
            );
            @Override
            public FileObject getProjectDirectory() {
                return null;
            }

            @Override
            public Lookup getLookup() {
                return l;
            }
        }
        Project project = new P();
        MySupport.query = project;
        MySupport mySupport = project.getLookup().lookup(MySupport.class);
        assertNotNull(mySupport);

        mySupport.task.waitFinished();
        assertSame("Instance created in parallel is the same", mySupport, mySupport.parallel);

        assertEquals(instances + 1, MySupport.INSTANCES.get());
        assertSame(project, mySupport.getProject());
    }
    
    @ProjectServiceProvider(service = MySupport.class, projectType = "org-netbeans-test-parallel")
    public static final class MySupport implements Runnable {

        public static final AtomicInteger INSTANCES = new AtomicInteger();
        public static Project query;

        private final Project project;
        MySupport parallel;
        RequestProcessor.Task task;


        public MySupport(Project project) throws Exception {
            INSTANCES.incrementAndGet();
            this.project = project;
            task = RequestProcessor.getDefault().post(this);
            task.waitFinished(1000);
        }

        public Project getProject() {
            return project;
        }

        @Override
        public void run() {
            parallel = query.getLookup().lookup(MySupport.class);
        }

    }
    
}
