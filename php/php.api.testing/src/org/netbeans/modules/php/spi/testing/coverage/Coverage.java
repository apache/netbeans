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
package org.netbeans.modules.php.spi.testing.coverage;

import java.util.List;

/**
 * Interface representing code coverage.
 */
public interface Coverage {

    /**
     * Get list of code coverage data for individual files.
     * @return list of code coverage data for individual files
     */
    List<File> getFiles();

    /**
     * Code coverage data for individual file.
     */
    interface File {

        /**
         * Get file path.
         * @return file path
         */
        String getPath();

        /**
         * Get file metrics.
         * @return file metrics
         */
        FileMetrics getMetrics();

        /**
         * Get line data.
         * @return line data
         */
        List<Line> getLines();

    }

    /**
     * Code coverage data for individual line of a file.
     */
    interface Line {

        /**
         * Get line number.
         * @return line number
         */
        int getNumber();

        /**
         * Get number of test hits for this line.
         * @return number of test hits for this line
         */
        int getHitCount();

    }

}
