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
package org.netbeans.modules.php.spi.testing.run;

import org.netbeans.api.annotations.common.NonNull;

/**
 * Interface for a test suite.
 * <p>
 * Every test suite <b>must be</b> {@link #finish(long) finished}.
 */
public interface TestSuite {

    /**
     * Add new test case to this test suite.
     * @param name name of the test case
     * @param type type of the test case
     * @return new test case
     * @since 0.2
     */
    TestCase addTestCase(@NonNull String name, @NonNull String type);

    /**
     * Finish this test suite and set time of this suite run, in milliseconds.
     * @param time time of this suite run, in milliseconds
     * @since 0.2
     */
    void finish(long time);

}
