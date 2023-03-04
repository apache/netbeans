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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CoreBridgeTest extends NbTestCase {
    private File jre;
    private File lib;
    private File ext;
    
    public CoreBridgeTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        jre = new File(getWorkDir(), "jre");
        lib = new File(jre, "lib");
        ext = new File(lib, "ext");
        ext.mkdirs();
        
        assertTrue("Dirs created", ext.isDirectory());
    }
    
    public void testJDK8FX() throws IOException {
        new File(ext, "jfxrt.jar").createNewFile();
        assertTrue("fx rt.jar found", CoreBridge.isJavaFX(jre));
    }
    
}
