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
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.openide.filesystems.FileObject;

/**
 * Interface for a test session.
 */
public interface TestSession {

    /**
     * Add new test suite to this test session.
     * @param name name of the test suite
     * @param location location of the test suite, can be {@code null}
     * @return new test suite
     * @see TestSuite#finish(long)
     * @since 0.2
     */
    TestSuite addTestSuite(@NonNull String name, @NullAllowed FileObject location);

    /**
     * Set line handler to use for printing while running tests.
     * <p>
     * This method should be called before first test suite is {@link #addTestSuite(String, FileObject) added}.
     * @param outputLineHandler line handler to use for printing while running tests
     * @since 0.2
     */
    void setOutputLineHandler(@NonNull OutputLineHandler outputLineHandler);

    /**
     * Print message.
     * @param message message that is print, can be empty but never {@code null}
     * @param error {@code true} if the given message is an error message
     * @since 0.2
     */
    void printMessage(@NonNull String message, boolean error);

    /**
     * Set code coverage data, compulsory to call if
     * {@link org.netbeans.modules.php.spi.testing.PhpTestingProvider#isCoverageSupported(org.netbeans.modules.php.api.phpmodule.PhpModule) supported} by this testing provider.
     * @param coverage code coverage data, can be {@code null} if any error occured
     * @see org.netbeans.modules.php.spi.testing.PhpTestingProvider#isCoverageSupported(org.netbeans.modules.php.api.phpmodule.PhpModule)
     * @since 0.2
     */
    void setCoverage(@NullAllowed Coverage coverage);

}
