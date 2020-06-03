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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.test.RemoteTestSuiteBase;

/**
 *
 */
public class FileOperationsTest extends RemoteTestSuiteBase {
    
//    static {
//        System.setProperty("remote.fs_server.verbose", "1");
//        System.setProperty("remote.fs_server.log", "true");
//        //System.setProperty("remote.fs_server.suppress.stderr", "false");
//        //System.setProperty("remote.fs_server.refresh", "0");
//    }
    
    @SuppressWarnings("unchecked")
    public FileOperationsTest() {
        this("FileOperations API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        return new Class[] {
            FileOperationsTestCase.class
        };
    }
    
    @SuppressWarnings("unchecked")
    public static FileOperationsTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass) {
        return new FileOperationsTest(testClass.getName(), testClass);
    }

    @SuppressWarnings("unchecked")
    public static FileOperationsTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass, int timesToRepeat) {
        Class[] classes = new Class[timesToRepeat];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = testClass;            
        }
        return new FileOperationsTest(testClass.getName(), classes);
    }

    public FileOperationsTest(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new FileOperationsTest();
    }
}
