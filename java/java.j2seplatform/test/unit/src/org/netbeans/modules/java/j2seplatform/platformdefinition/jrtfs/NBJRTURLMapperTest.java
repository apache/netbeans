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

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.net.URL;
import org.netbeans.ProxyURLStreamHandlerFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author lahvac
 */
public class NBJRTURLMapperTest extends NbTestCase {

    public NBJRTURLMapperTest(String name) {
        super(name);
    }

    public void testConversion() throws Exception {
        String jigsawHome = System.getProperty("jigsaw.home");
        if (jigsawHome == null)
            return;
        File jdkHome = new File(jigsawHome);
        URL path = new URL("nbjrt", null, jdkHome.toURI().toString() + "!/java.base/java/lang/Object.class");
        FileObject file = URLMapper.findFileObject(path);
        assertNotNull(file);
        assertSame(file, URLMapper.findFileObject(file.toURL()));
    }

    static {
        ProxyURLStreamHandlerFactory.register();
    }
}
