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
package org.netbeans.modules.java.openjdk.jtreg;

import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.openide.filesystems.FileObject;

public class TestRootDescription {
    public final FileObject testProperties;
    public final FileObject testRoot;
    public final FileObject testRootFile;

    private TestRootDescription(FileObject testProperties, FileObject testRoot, FileObject testRootFile) {
        this.testProperties = testProperties;
        this.testRoot = testRoot;
        this.testRootFile = testRootFile;
    }

    public static TestRootDescription findRootDescriptionFor(FileObject file) {
        FileObject search = file.getParent();
        FileObject testProperties = null;

        while (search != null) {
            if (testProperties == null) {
                testProperties =  BuildUtils.getFileObject(search, "TEST.properties");
            }

            FileObject testRoot = BuildUtils.getFileObject(search, "TEST.ROOT");

            if (testRoot != null) {
                return new TestRootDescription(testProperties, search, testRoot);
            }

            if (search.getNameExt().equals("lib") && search.getFileObject("../jdk/TEST.ROOT") != null) {
                return new TestRootDescription(null, search, null);
            }

            search = search.getParent();
        }

        return null;
    }
}
