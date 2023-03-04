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

package org.netbeans.modules.db;

import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseModuleTest extends TestBase {

    // TODO should also test that connections are disconnected

    // TODO should also test that no errors are only logged to ErrorManager with EM.notify(INFORMATIONAL, e)
    
    public DatabaseModuleTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }

    public void testRuntimesAreStopped() throws Exception {
        FileObject runtimeFolder = FileUtil.getConfigFile("Databases/Runtimes");
        FileObject runtime1 = FileUtil.createData(runtimeFolder, "runtime1.instance");
        runtime1.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime1.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        FileObject runtime2 = FileUtil.createData(runtimeFolder, "runtime2.instance");
        runtime2.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime2.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        new DatabaseModule().close();
        
        int checked = 0;
        for (int i = 0; i < runtimes.length; i++) {
            if (runtimes[i] instanceof DatabaseRuntimeImpl) {
                assertTrue(((DatabaseRuntimeImpl)runtimes[i]).stopped);
                checked++;
            }
        }
        // check we have really tested our DatabaseRuntime implementations
        assertTrue(checked == 2);
    }
    
    static final class DatabaseRuntimeImpl implements DatabaseRuntime {
        
        boolean stopped;
        
        public boolean acceptsDatabaseURL(String url) {
            return true;
        }

        public void stop() {
            stopped = true;
        }

        public void start() {
        }

        public boolean isRunning() {
            return true;
        }

        public String getJDBCDriverClass() {
            return null;
        }

        public boolean canStart() {
            return true;
        }
    }
}
