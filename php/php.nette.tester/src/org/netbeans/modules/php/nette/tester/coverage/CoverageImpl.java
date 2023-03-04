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

package org.netbeans.modules.php.nette.tester.coverage;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;

public final class CoverageImpl implements Coverage {

    @NullAllowed
    private final List<File> files;


    public CoverageImpl(List<File> files) {
        this.files = files;
    }

    @Override
    public List<File> getFiles() {
        return files != null ? Collections.unmodifiableList(files) : Collections.<File>emptyList();
    }

    //~ Inner classes

    static final class FileImpl implements File {

        private final String path;
        private final FileMetrics fileMetrics;
        private final List<Line> lines;


        public FileImpl(String path, FileMetrics fileMetrics, List<Line> lines) {
            this.path = path;
            this.fileMetrics = fileMetrics;
            this.lines = lines;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileMetrics getMetrics() {
            return fileMetrics;
        }

        @Override
        public List<Line> getLines() {
            return Collections.unmodifiableList(lines);
        }

    }

    static final class LineImpl implements Line {

        private final int number;
        private final int count;


        public LineImpl(int number, int count) {
            this.number = number;
            this.count = count;
        }

        @Override
        public int getNumber() {
            return number;
        }

        @Override
        public int getHitCount() {
            return count;
        }

    }

}
