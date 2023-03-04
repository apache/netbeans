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

package org.netbeans.modules.properties;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class PropertiesDataObjectTest extends NbTestCase {

    public PropertiesDataObjectTest(String name) {
        super(name);
    }

    public void testLookup() throws Exception {
        clearWorkDir();
        File propFile = new File(getWorkDir(), "foo.properties");
        propFile.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        Lookup lookup = propDO.getLookup();
        PropertiesEncoding encoding = lookup.lookup(PropertiesEncoding.class);
        assertNotNull(encoding);
        assertSame(encoding, lookup.lookup(FileEncodingQueryImplementation.class));
    }
}
