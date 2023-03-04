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
package org.netbeans.modules.testng.spi;

import org.netbeans.modules.testng.TestConfigAccessor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lukas
 */
public final class TestConfig {

    private final boolean rerun;
    private final String pkgName;
    private final String className;
    private final String methodName;
    private final FileObject test;


    static {
        TestConfigAccessor.setDefault(new TestConfigAccessor() {

            @Override
            public TestConfig createTestConfig(FileObject test, boolean rerun, String pkgName, String className, String methodName) {
                return new TestConfig(test, rerun, pkgName, className, methodName);
            }
        });
    }

    private TestConfig(FileObject test, boolean rerun, String pkgName, String className, String methodName) {
        this.test = test;
        this.rerun = rerun;
        this.pkgName = pkgName;
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPackageName() {
        return pkgName;
    }

    public boolean doRerun() {
        return rerun;
    }

    public FileObject getTest() {
        return test;
    }
}
