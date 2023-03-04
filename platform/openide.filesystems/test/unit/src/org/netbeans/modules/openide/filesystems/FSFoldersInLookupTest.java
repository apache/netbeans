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

package org.netbeans.modules.openide.filesystems;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/** 
 * @author Jaroslav Tulach
 */
public class FSFoldersInLookupTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup.paths", "MyServices:YourServices");
    }

    private FileObject root;
    private Logger LOG;

    public FSFoldersInLookupTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        if (System.getProperty("netbeans.user") == null) {
            System.setProperty("netbeans.user", new File(getWorkDir(), "ud").getPath());
        }
        
        LOG = Logger.getLogger("Test." + getName());
        
        root = FileUtil.getConfigRoot();
        for (FileObject fo : root.getChildren()) {
            fo.delete();
        }
        
        super.setUp();
    }
    
    public void testInterfaceFoundInMyServices() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        FileObject fo = FileUtil.createData(root, "MyServices/sub/dir/2/" + Shared.class.getName().replace('.', '-') + ".instance");
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        fo.delete();
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }
    public void testInterfaceFoundInMyServices2() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        FileObject fo = FileUtil.createData(root, "YourServices/kuk/" + Shared.class.getName().replace('.', '-') + ".instance");
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        fo.delete();
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }

    public static final class Shared {}

}
