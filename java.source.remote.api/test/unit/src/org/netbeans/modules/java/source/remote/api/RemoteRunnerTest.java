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
package org.netbeans.modules.java.source.remote.api;

import java.io.File;
import javax.swing.event.ChangeListener;
import org.junit.Test;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.remote.api.Parser.Config;
import org.netbeans.modules.java.source.remoteapi.RemoteProvider;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class RemoteRunnerTest extends NbTestCase {
    
    public RemoteRunnerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        RemoteProvider.extraClassPathElements = new File(TestRemoteResource.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        super.setUp();
    }
    
    @Test
    public void testGetRemoteURL() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File userDir = new File(wd, "ud");
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
        File root = new File(wd, "src");
        File src  = new File(root, "Test.java");
        assertTrue(src.getParentFile().mkdirs());
        TestUtilities.copyStringToFile(src, "public class Test { }");
        RemoteRunner runner = RemoteRunner.create(FileUtil.toFileObject(src));
        assertNotNull(runner);
        String actual = runner.readAndDecode(Config.create(FileUtil.toFileObject(src)), "/test", String.class);
        
        assertEquals("good: Test/RELEASE_7", actual);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteProvider.stopAll();
    }

    static {
        System.setProperty("java.use.remote.platform", "true");
    }
    
    @ServiceProvider(service=SourceLevelQueryImplementation2.class, position=1)
    public static final class TestSourceLevelQueryImpl implements SourceLevelQueryImplementation2 {

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            return new Result() {
                @Override
                public String getSourceLevel() {
                    return "7";
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }
        
    }
}
