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
package test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.openide.util.Utilities;

public final class SampleMain {
    int i = 42;
    String when = "Now";
    String utf16 = "5 €";
    Object[] arr = new String[] { when };
    Object obj = SampleMain.class;
    

    private SampleMain() {
    }

    public static void main(String... args) {
        var sample = new SampleMain();
        sample.check();
    }
    
    public void check() {
        throw new IllegalStateException(when + Arrays.toString(arr) + obj + i);
    }

    public static String findSourcePath() throws IOException, URISyntaxException {
        URI url = SampleMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File curDir = Utilities.toFile(url);
        for (;;) {
            var testSrc = new File(new File(new File(curDir, "test"), "unit"), "src");
            if (testSrc.isDirectory()) {
                return testSrc.getPath();
            }
            curDir = curDir.getParentFile();
        }
    }
}
