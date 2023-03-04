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
package org.netbeans.modules.php.project.ui;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String name) {
        super(name);
    }

    public void testValidUrl() throws Exception {
        final String[] correctUrls = new String[] {
            "http://localhost",
            "http://localhost/",
            "http://localhost/phpProject1",
            "http://localhost:8080/phpProject1",
            "http://localhost/phpProject1?a=b",
            "http://localhost/phpProject1?a=b#c",
            "http://www.swiz.cz/phpProject1#bb45",
            "https://localhost/phpProject1/subdir1/subdir2",
            "https://localhost/phpProject1/subdir1/subdir2/",
            "https://user:pwd@localhost/phpProject1",
            "http://[::1]/",
        };
        final String[] incorrectUrls = new String[] {
            null,
            "",
            "http:/localhost/test",
            "http://local host/test",
            " http://localhost/test",
            "http:/localhost/test ",
            "ftp://www:localhost/test",
            "aaa:/www:localhost/test",
            "test",
            "https://user : pwd @ localhost/phpProject1",
            "https://localhost/phpProject1/s u b d i r 1 / s u b  d i r 2 /",
        };

        for (String url : correctUrls) {
            assertTrue("should be correct url: [" + url + "]", Utils.isValidUrl(url));
        }
        for (String url : incorrectUrls) {
            assertFalse("should be incorrect url: [" + url + "]", Utils.isValidUrl(url));
        }
    }

    // #131023
    public void testValidateSourcesAndCopyTarget() throws Exception {
        final Map<String, String> correctDirs = new HashMap<>();
        correctDirs.put("/home/test/NetBeansProjects/PHPProject", "/home/test/NetBeansProjects/PHPProjectCopy");
        correctDirs.put("/home/test/NetBeansProjects/a", "/home/test/NetBeansProjects/b");
        correctDirs.put("/home/test/NetBeansProjects/PHPProject", "/var/www/PHPProject");
        correctDirs.put("/tmp/PHPProject", "/PHPProject");
        correctDirs.put("C:\\test", "D:\\test");
        final Map<String, String> incorrectDirs = new HashMap<>();
        incorrectDirs.put(FileUtil.normalizePath("/tmp/PHPProject"), FileUtil.normalizePath("/tmp/PHPProject/copy"));
        incorrectDirs.put(FileUtil.normalizePath("/tmp/PHPProject/web"),FileUtil.normalizePath("/tmp/PHPProject"));

        for (Map.Entry<String, String> entry : correctDirs.entrySet()) {
            String sources = entry.getKey();
            String copyTarget = entry.getValue();
            assertNull("incorrect entry: [" + sources + ", " + copyTarget + "]",
                    Utils.validateSourcesAndCopyTarget(sources, copyTarget));
        }
        for (Map.Entry<String, String> entry : incorrectDirs.entrySet()) {
            String sources = entry.getKey();
            String copyTarget = entry.getValue();
            assertNotNull("correct entry: [" + sources + ", " + copyTarget + "]",
                    Utils.validateSourcesAndCopyTarget(sources, copyTarget));
        }
    }
}
