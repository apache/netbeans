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
package org.netbeans.modules.php.spi.testing.locate;

import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * This interface serves for finding:
 * <ul>
 * <li>a test file for a tested (source) file, and</li>
 * <li>a tested (source) file for a test file.</li>
 * </ul>
 */
public interface TestLocator {

    /**
     * Find all source files with their offsets (use {@code -1} if not known)
     * for the given test file. If more files are found, user can select the proper
     * one from a dialog.
     * @param testFile test file to search source files for
     * @return all source files for the given test file
     */
    Set<Locations.Offset> findSources(FileObject testFile);

    /**
     * Find all test files with their offsets (use {@code -1} if not known)
     * for the given tested (source) file. If more files are found, user can select the proper
     * one from a dialog.
     * @param testedFile tested (source) file to search test files for
     * @return all test files for the given tested (source) file
     */
    Set<Locations.Offset> findTests(FileObject testedFile);

}
