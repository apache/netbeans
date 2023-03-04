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
package org.netbeans.modules.nbcode.integration;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class VerifyPresentUpdateCentersTest extends NbTestCase {

    public VerifyPresentUpdateCentersTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(VerifyPresentUpdateCentersTest.class).
            gui(false).
            suite();
    }

    public void testUpdateCenters() {
        FileObject services = FileUtil.getConfigRoot().getFileObject("Services");
        assertNotNull("Services found", services);
        FileObject au = services.getFileObject("AutoupdateType");
        assertNotNull("AutoUpdate folder found", au);
        FileObject[] arr = au.getChildren();
        assertEquals("Two AutoUpdate center registrations: " + Arrays.toString(arr), 2, arr.length);

        Set<String> names = new TreeSet<>();
        names.add(arr[0].getNameExt());
        names.add(arr[1].getNameExt());

        String[] arrNames = names.toArray(new String[0]);
        assertEquals("3rdparty.instance", arrNames[0]);
        assertEquals("distribution-update-provider.instance", arrNames[1]);
    }
}
