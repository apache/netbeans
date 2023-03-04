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
package org.netbeans.api.lsp;

/**
 * An operation that affects files, not their content.
 *
 * @since 1.3
 */
public class ResourceOperation {

    /**
     * An operation that instructs the IDE to create a file.
     */
    public static final class CreateFile extends ResourceOperation {
        private final String newFile;

        /**
         * Create a new instance.
         *
         * @param newFile the file that should be created.
         */
        public CreateFile(String newFile) {
            this.newFile = newFile;
        }

        /**
         * The file that should be created
         *
         * @return the file that should be created
         */
        public String getNewFile() {
            return newFile;
        }
    }
}
