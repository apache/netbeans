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

package org.netbeans.test.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public abstract class BasicTokensTest extends JellyTestCase {

    private String goldenFilePath;

    public BasicTokensTest(String name) {
        super(name);
    }

    protected abstract boolean generateGoldenFiles();
    
    @Override
    public void tearDown() {
        if (generateGoldenFiles()) {
            fail("GENERATING GOLDEN FILES TO " + goldenFilePath);
        } else {
            compareReferenceFiles();
        }
    }

    protected void testRun(String fileName) {
        String result = null;
        File dir = new File(getDataDir(), "tokens");
        File file = new File(dir, fileName);
        try {
            result = new DumpTokens(file).getTokenString();
        } catch (Throwable t) {
            System.out.println(t);
            t.printStackTrace();
        }
        if (generateGoldenFiles()) {
            try {
                goldenFilePath = getGoldenFile().getPath().replace("build/", "");
                File gFile = new File(goldenFilePath);
                gFile.createNewFile();
                FileWriter writer = new FileWriter(gFile);
                writer.write(result + "\n");
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                fail("IO EXCEPTION");
            }
        } else {
            ref(result);
        }
    }
}
