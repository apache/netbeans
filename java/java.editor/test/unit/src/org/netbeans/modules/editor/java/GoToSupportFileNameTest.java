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
package org.netbeans.modules.editor.java;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.lang.model.element.Element;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class GoToSupportFileNameTest extends NbTestCase {
    public GoToSupportFileNameTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
    }

    public void testFindFileName() throws Exception {
        URL u = getClass().getResource("/org/netbeans/modules/editor/java/GoToSupportFileNameTest.class");
        assertNotNull("Find u", u);
        FileObject fo = URLMapper.findFileObject(u);
        assertNotNull("Class found", fo);
        JavaSource s = JavaSource.forFileObject(fo);
        assertNotNull("Source found", s);

        CountDownLatch cdl = new CountDownLatch(1);
        List<Element> arr = new ArrayList<>();
        s.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController c) {
                arr.addAll(c.getTopLevelElements());
                cdl.countDown();
            }
        }, true);

        cdl.await(1, TimeUnit.SECONDS);

        String name = GoToSupport.findFileName(arr.get(0));
        assertEquals("GoToSupportFileNameTest.java", name);
    }
}
