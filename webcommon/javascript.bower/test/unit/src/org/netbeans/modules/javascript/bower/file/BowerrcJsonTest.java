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
package org.netbeans.modules.javascript.bower.file;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

public class BowerrcJsonTest extends NbTestCase {

    public BowerrcJsonTest(String name) {
        super(name);
    }

    public void testBowerComponentsDir() {
        BowerrcJson bowerrcJson = new BowerrcJson(FileUtil.toFileObject(getDataDir()), "bowerrc-bower-components");
        assertTrue(bowerrcJson.getFile().getAbsolutePath(), bowerrcJson.exists());
        assertEquals(new File(getDataDir(), BowerrcJson.DEFAULT_BOWER_COMPONENTS_DIR), bowerrcJson.getBowerComponentsDir());
    }

    public void testLibsDir() {
        BowerrcJson bowerrcJson = new BowerrcJson(FileUtil.toFileObject(getDataDir()), "bowerrc-libs");
        assertTrue(bowerrcJson.getFile().getAbsolutePath(), bowerrcJson.exists());
        assertEquals(new File(getDataDir(), "public_html/libs"), bowerrcJson.getBowerComponentsDir());
    }

    public void testDefaultBowerComponentsDir() {
        BowerrcJson bowerrcJson = new BowerrcJson(FileUtil.toFileObject(getDataDir()), "nonexisting-bowerrc");
        assertFalse(bowerrcJson.getFile().getAbsolutePath(), bowerrcJson.exists());
        assertEquals(new File(getDataDir(), BowerrcJson.DEFAULT_BOWER_COMPONENTS_DIR), bowerrcJson.getBowerComponentsDir());
    }

}
