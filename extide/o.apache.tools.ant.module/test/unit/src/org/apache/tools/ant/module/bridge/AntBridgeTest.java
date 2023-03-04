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
package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.AntTargetExecutor.Env;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AntBridgeTest extends NbTestCase {
    
    public AntBridgeTest(String name) {
        super(name);
    }
    
    public void testJavacCompiler() throws IOException {
        File compileDir = new File(this.getDataDir(), "compile");
        FileObject buildXML = FileUtil.toFileObject(new File(compileDir, "build.xml"));
        AntProjectCookie apc = buildXML.getLookup().lookup(AntProjectCookie.class);
        final ExecutorTask task = AntTargetExecutor.createTargetExecutor(new Env())
                                                   .execute(apc, new String[] {"all"});
        task.waitFinished();
        assertEquals(0, task.result());
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(AntBridgeTest.class)
                            .gui(false)
                            .suite();
    }
}
