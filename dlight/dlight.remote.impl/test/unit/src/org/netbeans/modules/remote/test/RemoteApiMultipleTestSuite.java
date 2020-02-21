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

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemOffilneTestCase;
import org.netbeans.modules.remote.impl.fs.ScheduleRefreshParityTestCase;

/**
 *
 */
public class RemoteApiMultipleTestSuite extends RemoteTestSuiteBase {

    @SuppressWarnings("unchecked")
    public RemoteApiMultipleTestSuite() {
        this("Remote API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        Class[] orig = RemoteApiTest.getTestClasses();
        int mul = 4;
        List<Class<? extends NativeExecutionBaseTestCase>> res = new ArrayList<>();
        for (int i = 0; i < mul; i++) {
            for (int j = 0; j < orig.length; j++) {
                if (orig[j] != RemoteFileSystemOffilneTestCase.class && orig[j] != ScheduleRefreshParityTestCase.class) {
                    res.add(orig[j]);
                }
            }
        }
        return res.toArray(new Class[res.size()]);
    }
    
    @SuppressWarnings("unchecked")
    public static RemoteApiMultipleTestSuite createSuite(Class<? extends NativeExecutionBaseTestCase> testClass) {
        return new RemoteApiMultipleTestSuite(testClass.getName(), testClass);
    }

    public RemoteApiMultipleTestSuite(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new RemoteApiMultipleTestSuite();
    }
}
