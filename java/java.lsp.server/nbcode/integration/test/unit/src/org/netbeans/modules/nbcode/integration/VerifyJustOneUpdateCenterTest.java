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
package org.netbeans.modules.nbcode.integration;

import java.util.Arrays;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class VerifyJustOneUpdateCenterTest extends NbTestCase {

    public VerifyJustOneUpdateCenterTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(VerifyJustOneUpdateCenterTest.class).
            gui(true).
            suite();
    }

    public void testUpdateCenters() {
        FileObject services = FileUtil.getConfigRoot().getFileObject("Services");
        assertNotNull("Services found", services);
        FileObject au = services.getFileObject("AutoupdateType");
        assertNotNull("AutoUpdate folder found", au);
        FileObject[] arr = au.getChildren();
        assertEquals("Just one AutoUpdate center registration: " + Arrays.toString(arr), 1, arr.length);
        assertEquals("3rdparty.instance", arr[0].getNameExt());
    }
}
