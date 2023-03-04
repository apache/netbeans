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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.NbPlatformListModel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;

/**
 * @author Martin Krauskopf
 */
public class PlatformComponentFactoryTest extends NbTestCase {

    public PlatformComponentFactoryTest(String testName) {
        super(testName);
    }

    public void testNbPlatformListModelSorting() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir(), null);

        File first = new File(getWorkDir(), "first");
        TestBase.makePlatform(first);
        NbPlatform.addPlatform("first", first, "AAA first");

        File between = new File(getWorkDir(), "between");
        TestBase.makePlatform(between);
        NbPlatform.addPlatform("between", between, "KKK between");

        File last = new File(getWorkDir(), "last");
        TestBase.makePlatform(last);
        NbPlatform.addPlatform("last", last, "ZZZ last");
        
        NbPlatform.reset();
        
        NbPlatformListModel model = new NbPlatformListModel();

        int size = model.getSize();
        List<String> actual = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            actual.add(((NbPlatform) model.getElementAt(i)).getLabel());
        }
        assertEquals(Arrays.asList(
                "AAA first",
                "Development IDE",
                "KKK between",
                "ZZZ last"
                ), actual);
    }
    
}
