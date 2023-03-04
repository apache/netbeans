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

package org.netbeans.modules.tasklist.trampoline;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class EmptyScanningScopeTest extends NbTestCase {
    
    static {
        IDEInitializer.setup(new String[0],new Object[0]);
    }

    public EmptyScanningScopeTest (String name) {
        super (name);
    }

    public void testIterator() {
        EmptyScanningScope scope = new EmptyScanningScope();
        
        Iterator<FileObject> iterator = scope.iterator();
        
        assertNotNull( iterator );
        assertFalse( iterator.hasNext() );
        try {
            iterator.next();
            fail( "iterator must be empty" );
        } catch( NoSuchElementException e ) {
            //that's what we want
        }
    }

    public void testGetLookup() {
        EmptyScanningScope scope = new EmptyScanningScope();
        
        assertEquals( Lookup.EMPTY, scope.getLookup() );
    }

    public void testIsInScope() throws IOException {
        EmptyScanningScope scope = new EmptyScanningScope();
        FileObject fo = FileUtil.getConfigRoot();
        assertNotNull( fo );
        assertFalse( scope.isInScope( fo ) );
    }
}

