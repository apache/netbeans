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

package org.openide.loaders;

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

/** This is the same test as DataLoaderPoolTest, just intead of
 * calling setPreferredLoader, it only changes the attribute of filesystem
 * to see if listening on attributes works fine.
 *
 * @author Jaroslav Tulach
 */
public class DataLoaderPoolOnlyEventsTest extends DataLoaderPoolTest {

    public DataLoaderPoolOnlyEventsTest(String name) {
        super(name);
    }
    
    /** Changes directly the filesystem attribute.
     */
    protected void doSetPreferredLoader(FileObject fo, DataLoader loader) throws IOException {
        if (loader == null) {
            fo.setAttribute("NetBeansAttrAssignedLoader", null);
        } else {
            Class c = loader.getClass();
            fo.setAttribute ("NetBeansAttrAssignedLoader", c.getName ());
        }
    }
    
    
}
