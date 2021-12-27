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
package org.netbeans.modules.gradle.queries;

import java.io.IOException;
import java.util.Random;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public class GenericProjectSourcesTest extends AbstractGradleProjectTestCase {
    public GenericProjectSourcesTest(String name) {
        super(name);
    }

    public void testRootProjectSourceGroup() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "");
        Project p = ProjectManager.getDefault().findProject(a);
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals(1, groups.length);
    }
    
}
