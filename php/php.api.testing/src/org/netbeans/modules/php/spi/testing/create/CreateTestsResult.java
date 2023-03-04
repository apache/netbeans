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
package org.netbeans.modules.php.spi.testing.create;

import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * Result of creating tests.
 */
public final class CreateTestsResult {

    private final Set<FileObject> succeeded;
    private final Set<FileObject> failed;


    /**
     * Create new result.
     * @param succeeded set of files tests were created for
     * @param failed set of files tests were NOT created for
     */
    public CreateTestsResult(Set<FileObject> succeeded, Set<FileObject> failed) {
        this.succeeded = succeeded;
        this.failed = failed;
    }

    /**
     * Get set of files tests were created for.
     * @return set of files tests were created for
     */
    public Set<FileObject> getSucceeded() {
        return succeeded;
    }

    /**
     * Get set of files tests were NOT created for.
     * @return set of files tests were NOT created for
     */
    public Set<FileObject> getFailed() {
        return failed;
    }

}
