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
package org.netbeans.modules.cnd.remote.fs;

import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 */
public class RemoteInvalidFileObjectSupportTest extends RemoteTestBase {

    public RemoteInvalidFileObjectSupportTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testRemoteInvalidFileObject() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        final FileSystem fs = FileSystemProvider.getFileSystem(env);
        String path = "/tmp";
        final FileObject invalidFo1 = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        URI uri = invalidFo1.toURI(); // just to check that there is no assertions
        URL url = uri.toURL();
        FileObject fo = URLMapper.findFileObject(url);
        // Thw below means as following: if a file that was once created as invalid, appears, 
        // and somebody saved its URL and then tries to get this file by url, 
        // then a valid file should be returned.
        assertNotNull("If the file is valid then fo > url > fo should retirn not null", fo);
        assertTrue("File should be valid", fo.isValid());
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteInvalidFileObjectSupportTest.class);
    }
}
