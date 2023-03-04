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
package org.netbeans.api.queries;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Alexander Simon
 */
public class CollocationQueryTest extends NbTestCase {
    
    public CollocationQueryTest(String testMethod) {
        super (testMethod);
    }
    
    @Override
    public void setUp() throws IOException {
    }
    
    @SuppressWarnings("deprecation")
    public void testAreCollocated() throws Exception {
        clearWorkDir();
        File base = getWorkDir();
        File proj1 = new File(base, "proj1");
        proj1.mkdirs();
        File proj3 = new File(proj1, "proj3");
        proj3.mkdirs();
        File proj2 = new File(base, "proj2");
        proj2.mkdirs();
        assertTrue("Must be collocated", CollocationQuery.areCollocated(proj1, proj3));
        assertTrue("Must be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj1), BaseUtilities.toURI(proj3)));
        assertTrue("Must be collocated", CollocationQuery.areCollocated(proj3, proj1));
        assertTrue("Must be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj3), BaseUtilities.toURI(proj1)));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj1, proj2));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj1), BaseUtilities.toURI(proj2)));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj2, proj1));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj2), BaseUtilities.toURI(proj1)));
        
        // folder does not exist:
        File proj4 = new File(base, "proj");
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj1, proj4));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj1), BaseUtilities.toURI(proj4)));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj4, proj1));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj4), BaseUtilities.toURI(proj1)));
        proj4.mkdirs();
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj1, proj4));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj1), BaseUtilities.toURI(proj4)));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(proj4, proj1));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(proj4), BaseUtilities.toURI(proj1)));
        
        // files do not exist:
        File file1 = new File(base, "file1.txt");
        File file2 = new File(base, "file1");
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(file1, file2));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(file1), BaseUtilities.toURI(file2)));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(file2, file1));
        assertFalse("Cannot be collocated", CollocationQuery.areCollocated(BaseUtilities.toURI(file2), BaseUtilities.toURI(file1)));
        
        // passing the same parameter
        assertTrue("A file must be collocated with itself", CollocationQuery.areCollocated(proj1, proj1));
        assertTrue("A file must be collocated with itself", CollocationQuery.areCollocated(BaseUtilities.toURI(proj1), BaseUtilities.toURI(proj1)));
    }

}
