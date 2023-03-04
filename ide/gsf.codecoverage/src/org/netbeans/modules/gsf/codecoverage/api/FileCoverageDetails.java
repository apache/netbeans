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
 * Specific information about coverage of a certain file
 *
 * @author Tor Norbye
 */
public interface FileCoverageDetails {

    public FileObject getFile();
    
    /**
     * Get the total line count for this file 
     */
    int getLineCount();

    /**
     * Does this FileCoverage provide individual hit counts for lines, or
     * just yes/no answers for each line?
     */
    boolean hasHitCounts();

    /**
     * Timestamp (similar and comparable to File.lastModified) when the
     * code coverage results were last updated. Return 0 or -1 if you cannot
     * obtain this information.
     */
    long lastUpdated();

    /**
     * Return a summary of the file coverage
     */
    FileCoverageSummary getSummary();

    /**
     * Get the type of coverage for the given line.
     * The line numbers should be 0 based (e.g. the first line in the file
     * is lineNo=0.)
     */
    CoverageType getType(int lineNo);

    /**
     * Return the hit count for the given line, if this result supports
     * hit counts (see {@link #hasHitCounts}.
     * The line numbers should be 0 based (e.g. the first line in the file
     * is lineNo=0.)
     */
    int getHitCount(int lineNo);
}
