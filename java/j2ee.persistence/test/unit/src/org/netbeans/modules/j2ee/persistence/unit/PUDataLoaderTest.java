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

package org.netbeans.modules.j2ee.persistence.unit;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObjectTestBase.Lkp;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * @author Martin Krauskopf
 */
public class PUDataLoaderTest extends PUDataObjectTestBase {
    
    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp) Lookup.getDefault()).setLookups(new Object[] {
            new PUDataObjectTestBase.PUMimeResolver(),
            new PUDataObjectTestBase.Pool(),
        });
    }
    
    public PUDataLoaderTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    public void testPUWithoutProjectOwnerIsRecognized() throws Exception {
        String persistenceFile = getDataDir().getAbsolutePath() + "/persistence.xml";
        FileObject puFO = FileUtil.toFileObject(new File(persistenceFile));
        assertTrue("persistence unit without project owner is not recongnized." +
                " Project owner: " + FileOwnerQuery.getOwner(puFO),
                DataObject.find(puFO) instanceof PUDataObject);
    }
    
}
