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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class TableSourceTest extends NbTestCase {

    public TableSourceTest(String testName) {
        super(testName);
    }

    public void testNoLeak() {
        Project project = new ProjectImpl();

        TableSource source = new TableSource("jndi/foo", TableSource.Type.DATA_SOURCE);
        TableSource.put(project, source);

        assertSame(source, TableSource.get(project));

        Reference<Project> projectRef = new WeakReference<>(project);
        project = null;
        assertGC("Should be possible to GC project", projectRef);
    }

    private static final class ProjectImpl implements Project {

        @Override
        public FileObject getProjectDirectory() {
            return null;
        }

        @Override
        public Lookup getLookup() {
            return null;
        }
    }
}
