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
package org.netbeans.modules.versioning.core.spi.testvcs;

import java.io.File;
import java.net.URI;
import org.netbeans.spi.queries.CollocationQueryImplementation2;

/**
 *
 * @author Tomas Stupka
 */
public class TestVCSCollocationQuery implements CollocationQueryImplementation2 {

    public static String COLLOCATED_FILENAME_SUFFIX = "_iscollocated";
    @Override
    public boolean areCollocated(URI file1, URI file2) {
        String name1 = file1.getPath();
        String name2 = file2.getPath();
        
        return name1.endsWith(COLLOCATED_FILENAME_SUFFIX) && name2.endsWith(COLLOCATED_FILENAME_SUFFIX);
    }

    @Override
    public URI findRoot(URI uri) {
        File root = getRoot(new File(uri));
        return root != null ? root.toURI() : null;
    }
    
    private File getRoot(File file) {
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if (file.getName().endsWith(TestVCS.VERSIONED_FOLDER_SUFFIX)) {
                topmost = file;
            }
        }
        return topmost;
    }
    
}
