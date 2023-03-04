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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author pzajac
 */
public class RecursiveDepsTest extends TestBase {
    
    public RecursiveDepsTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ModuleListParserTest.deleteCaches();
    }
    
    public void testDepsTest () throws Exception {
      // create test
      File projectxml = extractFile(RecursiveDepsTest.class.getResourceAsStream("RecursiveDepsProject.xml"),"project.xml");  
      execute ("RecursiveDeps.xml", new String[] { "-verbose", "-Dproject.file=" + projectxml, "recursive-deps" });
    }

    private File extractFile(InputStream is, String fileName) throws IOException {
        File f = new File(getWorkDir(),fileName);
        byte bytes[] = new byte[50000];
        try (FileOutputStream fos = new FileOutputStream(f)) {
            int len = is.read(bytes);
            fos.write(bytes,0,len);
        }
        return f;
    }
    
    
}
