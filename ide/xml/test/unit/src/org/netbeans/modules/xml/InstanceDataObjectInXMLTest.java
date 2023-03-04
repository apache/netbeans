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
package org.netbeans.modules.xml;

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;

public class InstanceDataObjectInXMLTest extends NbTestCase {
    public InstanceDataObjectInXMLTest(String name) {
        super(name);
    }
    public static Test suite() {
        return NbModuleSuite.createConfiguration(InstanceDataObjectInXMLTest.class).gui(false).suite();
    }
    public void testInstanceDataObjectCreate() throws Exception {
        final FileObject fo = FileUtil.createMemoryFileSystem().getRoot();
        final DataFolder folder = DataFolder.findFolder(fo);
        final ArrayList<Object> obj = new ArrayList<Object>();
        InstanceDataObject dataObj = InstanceDataObject.create(folder, null, obj, null);
        assertSame("Instance is preserved", dataObj.instanceCreate(), obj);
    }
}
