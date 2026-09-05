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

package org.netbeans.modules.nativeexecution.test;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * This class test test framework as such -
 * NativeExecutionBaseTestSuite, NativeExecutionBaseTestCase classes,
 * ForAllEnvironments annotation, etc
 * @author Vladimir Kvashin
 */
public class NativeExecutionTestFrameworkTestCase extends NativeExecutionBaseTestCase {

    public NativeExecutionTestFrameworkTestCase(String name) {
        super(name);
    }

    public NativeExecutionTestFrameworkTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    public void testNamedSingle() {
    }

    @org.junit.Test
    public void annotatedSingle() {
    }

    @org.junit.Test
    @ForAllEnvironments(section="remote.platforms")
    public void annotatedForAllRemotePlatforms() {
    }

    @ForAllEnvironments(section="remote.platforms")
    public void testForAllRemotePlatforms() {
    }

    @org.junit.Test
    @ForAllEnvironments
    public void annotatedForAllDefault() {
    }

    @ForAllEnvironments
    public void testForAllDefault() {
    }

    @org.junit.Test
    @ForAllEnvironments(section="test.framework.test.platforms")
    public void annotatedForAllTestPlatforms() {
    }

    @ForAllEnvironments(section="test.framework.test.platforms")
    public void testForAllTestPlatforms() {
    }

    @If(section="test.conditional", key="cond-true")
    public void testConditionalTrue() {
    }

    @If(section="test.conditional", key="cond-false")
    public void testConditionalFalse() {
    }

    @If(section="test.conditional", key="cond-err")
    public void testConditionalErrValue() {
    }

    @If(section="test.conditional.inexistent", key="cond-err")
    public void testConditionalInexistentKey() {
    }

    @If(key="inexistent", section="inexistent", defaultValue=false)
    public void testConditionalDefault() {
    }

    @Ifdef(section="ifdef.section", key="ifdef.key")
    public void testIfdef() {
    }

    @Ifdef(section="ifdef.section", key="ifdef.key")
    @If(section="ifdef.section", key="ifdef.and.if.key")
    public void testIfdefAndIf() {
    }

    public static junit.framework.Test suite() {
        Class<NativeExecutionTestFrameworkTestCase> testClass = NativeExecutionTestFrameworkTestCase.class;
        return new NativeExecutionBaseTestSuite(
                testClass.getSimpleName(),
                "test.framework.test.default.platforms", testClass);
    }
}
