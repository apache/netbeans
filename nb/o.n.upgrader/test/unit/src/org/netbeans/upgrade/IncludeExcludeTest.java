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

package org.netbeans.upgrade;

import java.util.Set;
import org.netbeans.junit.NbTestCase;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class IncludeExcludeTest extends NbTestCase {
    private Set includeExclude;

    public IncludeExcludeTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String reader = "# ignore comment\n" +
        "include one/file.txt\n" +
        "include two/dir/.*\n" +
        "\n" +
        "exclude two/dir/sub/.*\n";
        
        includeExclude = IncludeExclude.create (new java.io.StringReader (reader));
    }    

    public void testOneFileIsThere () {
        assertTrue (includeExclude.contains ("one/file.txt"));
    }
    
    public void testDoesNotContainRoot () {
        assertFalse (includeExclude.contains (""));
    }
    
    public void testContainsSomethingInDir () {
        assertTrue (includeExclude.contains ("two/dir/a.file"));
    }
    
    public void testContainsSomethingUnderTheDir () {
        assertTrue (includeExclude.contains ("two/dir/some/folder/a.file"));
    }
    
    public void testDoesNotContainSubDir () {
        assertFalse (includeExclude.contains ("two/dir/sub/not.there"));
    }
    
    public void testWrongContentDetected () {
        try {
            IncludeExclude.create (new java.io.StringReader ("some strange line"));
            fail ("Should throw exception");
        } catch (java.io.IOException ex) {
        }
    }
 }
