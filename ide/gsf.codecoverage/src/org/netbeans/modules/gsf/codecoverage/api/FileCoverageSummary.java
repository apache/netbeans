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
package org.netbeans.modules.gsf.codecoverage.api;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class FileCoverageSummary implements Comparable<FileCoverageSummary> {

    private final FileObject file;
    private final String displayName;
    private final int lineCount;
    private final int executedLineCount;
    private final int inferredCount;
    private final int partialCount;
    //private final int executableLines;

    /**
     * Create a code coverage summary for a file.
     *
     * @param file The file we collected data from
     * @param displayName A display name for the file (often the path itself)
     * @param lineCount The total number of lines in the file
     * @param executedLineCount The total number of lines that were executed (including inferred and
     * partial)
     * @param inferredCount The lines not recorded but inferred to be executed (such as comments and
     * whitespace between executed statements) Return 0 for "unknown/not recorded".
     * @param partialCount The lines that were partially executed. Return 0 for "unknown/not
     * recorded".
     */
    public FileCoverageSummary(FileObject file, String displayName, int lineCount, int executedLineCount,
        int inferredCount, int partialCount) {
        this.file = file;
        this.displayName = displayName;
        this.lineCount = lineCount;
        this.executedLineCount = executedLineCount;
        this.inferredCount = inferredCount;
        this.partialCount = partialCount;
    }

    public float getCoveragePercentage() {
        if (lineCount == 0) {
            //return 100.0f;
            return 0f;
        } else {
            return (100.0f * executedLineCount) / lineCount;
        }
    }

    @Override
    public int compareTo(FileCoverageSummary other) {
        float cov = getCoveragePercentage();
        float otherCov = other.getCoveragePercentage();
        if (cov == otherCov) {
            return 0;
        } else {
            return cov < otherCov ? -1 : 1;
        }
    }

    public FileObject getFile() {
        return file;
    }

    public int getExecutedLineCount() {
        return executedLineCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getInferredCount() {
        return inferredCount;
    }

    public int getPartialCount() {
        return partialCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName + ": " + getCoveragePercentage() + "%";
    }
}
