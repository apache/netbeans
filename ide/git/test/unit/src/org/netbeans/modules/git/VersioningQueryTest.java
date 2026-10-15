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
package org.netbeans.modules.git;

import java.io.File;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.openide.util.Utilities;

/**
 *
 * @author ondra
 */
public class VersioningQueryTest extends AbstractGitTestCase {


    public VersioningQueryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsManaged () throws Exception {
        // unversioned file
        File file = new File(testBase, "unversionedfile");
        file.createNewFile();

        boolean versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertFalse(versioned);

        commit();

        // metadata folder
        file = new File(repositoryLocation, ".git");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // metadata file
        file = new File(new File(repositoryLocation, ".git"), "index");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // versioned file
        file = new File(repositoryLocation, "attrfile");
        file.createNewFile();

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);
    }

}
