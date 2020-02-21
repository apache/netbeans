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

package org.netbeans.modules.remote.test;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.remote.impl.fs.*;

/**
 *
 */
public class RemoteApiTest extends NativeExecutionBaseTestSuite {

    public static final boolean TRACE_LISTENERS = Boolean.getBoolean("trace.listener.tests"); //NOI18N

    @SuppressWarnings("unchecked")
    public RemoteApiTest() {
        this("Remote API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        return new Class[] {
           // some tests moved to RemoteApiLinksAndListenersTest
           AdeMockupTestCase.class,
           RemoteFileSystemTestCase.class,
           CopyTestCase.class,
           RemotePathTestCase.class,
           RemoteURLTestCase.class,
           RenameTestCase.class,
           EscapeWindowsNameTestCase.class,
           CaseSensivityTestCase.class,
           DirectoryStorageTestCase.class,
           DirectoryReaderTestCase.class,
           RefreshTestCase.class,
           FastRefreshTestCase.class,
           RefreshTestCase_IZ_210125.class,
           MoveTestCase.class,
           RefreshNonInstantiatedTestCase.class,
           RefreshDirSyncCountTestCase.class,
           CanonicalTestCase.class,
           CreateDataAndFolderTestCase.class,
           NormalizationTestCase.class,
           ReadOnlyDirTestCase.class,
           ScheduleRefreshParityTestCase.class,
           WritingQueueTestCase.class,
           RemoteFileSystemParallelReadTestCase.class,
           RemoteFileSystemParallelLsTestCase.class,
           RemoteFileSystemOffilneTestCase.class,
           TempFileRelatedExceptionsIZ_258285_testCase.class,
           DeleteOnExitTestCase.class,
           CyclicLinksTestCase.class
        };
    }
    
    @SuppressWarnings("unchecked")
    public static RemoteApiTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass) {
        return new RemoteApiTest(testClass.getName(), testClass);
    }

    @SuppressWarnings("unchecked")
    public static RemoteApiTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass, int timesToRepeat) {
        Class[] classes = new Class[timesToRepeat];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = testClass;            
        }
        return new RemoteApiTest(testClass.getName(), classes);
    }
    
    public RemoteApiTest(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new RemoteApiTest();
    }
}
