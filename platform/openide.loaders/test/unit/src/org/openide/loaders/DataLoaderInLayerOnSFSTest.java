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

package org.openide.loaders;


import java.io.IOException;
import org.openide.filesystems.*;

/** Check what can be done when registering loaders in layer. And how it works
 * on system file system.
 * @author Jaroslav Tulach
 */
public class DataLoaderInLayerOnSFSTest extends DataLoaderInLayerTest {

    public DataLoaderInLayerOnSFSTest(String name) {
        super(name);
    }

    @Override
    protected FileSystem createFS(String... resources) throws IOException {
        for (String s : resources) {
            FileObject fo = FileUtil.getConfigFile(s.replaceAll("/.*", ""));
            if (fo != null) {
                fo.delete();
            }
        }
        FileSystem sfs = FileUtil.getConfigRoot().getFileSystem();
        for (String s : resources) {
            assertNotNull("creating: " + s, FileUtil.createData(sfs.getRoot(), s));
        }
        return sfs;
    }
    
}
