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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.NamedServicesLookupTest;

/**
 *
 * @author sdedic
 */
public class SharedClassObjectFactoryTest extends NamedServicesLookupTest {
    private FileObject root;
    private Logger LOG;
    

    public SharedClassObjectFactoryTest(String name) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINEST;
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
    
    public void testSharedClassObject() throws Exception {
        Shared instance = SharedClassObject.findObject(Shared.class, true);
        FileObject data = FileUtil.createData(root, "dir/" + Shared.class.getName().replace('.', '-') + ".instance");
        Lookup l = Lookups.forPath("dir");
        assertSame(instance, l.lookup(Shared.class));
        
        Shared created = FileUtil.getConfigObject(data.getPath(), Shared.class);
        assertSame("Config file found", instance, created);
    }
    
    public static final class Shared extends SharedClassObject {}

}
