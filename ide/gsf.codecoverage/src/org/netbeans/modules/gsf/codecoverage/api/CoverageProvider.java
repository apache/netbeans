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

import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;

/**
 * A code coverage provider provides code coverage information for source files.
 * Currently only intended for C0 usage.
 *
 * @todo Handle C1 and C2 types later
 *
 * @author Tor Norbye
 */
public interface CoverageProvider {
    /**
     * Return true iff this provider support custom hit counts. (If false, it only
     * supports whether a line is covered or not, e.g. hit count={zero, nonzero}.
     */
    boolean supportsHitCounts();

    /**
     * Return true iff this provider supports aggregation.
     */
    boolean supportsAggregation();

    /**
     * Is coverage enabled for the given project?
     */
    boolean isEnabled();

    /**
     * Is coverage data being aggregated from run to run?
     */
    boolean isAggregating();

    void setAggregating(boolean aggregating);

    /**
     * Mime types supported by this provider
     */
    public Set<String> getMimeTypes();

    /**
     * This method is called to enable or disable code coverage for the project
     */
    public void setEnabled(boolean enabled);

    /**
     * The user has requested that the coverage data should be cleared out.
     */
    void clear();
    
    /**
     * Get the specific coverage information per line.
     */
    @CheckForNull FileCoverageDetails getDetails(FileObject fo, Document doc);

    /**
     * Get a summary of all the coverage data in the given project, one per measured file.
     */
    @CheckForNull List<FileCoverageSummary> getResults();
    
    /**
     * Gets the name of action that the Test All button in the code coverage bar
     * should invoke. May return <code>null</code>, in which case  if the default 
     * test action will be used.
     * 
     * @return the name of test action or <code>null</code>.
     */
    @CheckForNull String getTestAllAction();
}
