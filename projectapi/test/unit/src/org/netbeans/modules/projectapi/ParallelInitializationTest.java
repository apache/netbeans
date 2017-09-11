/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
