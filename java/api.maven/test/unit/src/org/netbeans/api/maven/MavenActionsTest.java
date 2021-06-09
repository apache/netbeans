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
package org.netbeans.api.maven;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author sdedic
 */
public class MavenActionsTest extends NbTestCase {

    public MavenActionsTest(String name) {
        super(name);
    }
    
    /**
     * Checks that the custom provider is included when the project contains appropriate
     * plugin and trashed when the plugin vanishes from the model.
     */
    public void testCustomActionsProvider() throws Exception {
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        
        try (OutputStream o = wd.createAndOpen("pom.xml");
            OutputStreamWriter w = new OutputStreamWriter(o)) {
            w.write(
                "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>jar</packaging>"
                + "<build><plugins><plugin><artifactId>test.plugin</artifactId><groupId>org.ntebeans.modules.maven</groupId></plugin></plugins></build>"
                + "</project>");
        }
        
        Project p = FileOwnerQuery.getOwner(wd);
        
        // must open with OpenProjects, so that the project starts to listen on files:
        OpenProjects.getDefault().open(new Project[] { p } , false);
        OpenProjects.getDefault().openProjects().get();
        
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains("extra"));
        
        CountDownLatch change = new CountDownLatch(1);
        
        p.getLookup().lookupResult(MavenActionsProvider.class).addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                change.countDown();
            }
        });
        
        FileObject pom = wd.getFileObject("pom.xml");

        try (OutputStream o = pom.getOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(o)) {
            w.write(
                "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>jar</packaging>"
                + "</project>");
        }
        
        // wait for the set of providers to refresh. PROP_PROJECT comes first, but Lookup takes some
        // additional time.
        change.await();
   
        assertFalse(Arrays.asList(ap.getSupportedActions()).contains("extra"));
    }
}
