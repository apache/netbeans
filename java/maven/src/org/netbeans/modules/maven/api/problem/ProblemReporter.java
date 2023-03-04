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

package org.netbeans.modules.maven.api.problem;

import java.util.Collection;

/**
 * resides in project lookup, allows to append problems with the project 
 * that are identified.
 * @author mkleint
 * @deprecated use <code>ProjectProblemsProvider</code> instead. <code>ProblemReporter</code>'s problems are converted to the new API.
 * The synchronization is one way only, it's not guaranteed that <code>getReports()</code> contains all problems.
 * 
 */
@Deprecated
public interface ProblemReporter {
    
    public void addReport(ProblemReport report);
    
    public void addReports(ProblemReport[] report);
    
    public void removeReport(ProblemReport report);
    
    public Collection<ProblemReport> getReports();
    
}
