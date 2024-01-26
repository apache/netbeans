/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.tooling.internal;

import org.netbeans.modules.gradle.tooling.Model;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
public interface NbProjectInfo extends Model, org.gradle.tooling.model.Model {
    
    /**
     * Project information which shall be cached.
     * @return the important project data.
     */
    Map<String, Object> getInfo();
    
    /**
     * Additional project information which could be thrown away.
     * @return the not-that-important project data.
     */    
    Map<String, Object> getExt();
    Set<String> getProblems();

    /**
     * @since 2.23
     */
    Set<Report> getReports();
    
    boolean getMiscOnly();
    
    /**
     * @since 2.23
     */
    interface Report {
        /**
         * Severity of the report.
         */
        enum Severity {
            EXCEPTION, ERROR, WARNING, INFO
        }
        
        public Severity getSeverity();
        public String getErrorClass();
        public String getScriptLocation();
        public int getLineNumber();
        public String getMessage();
        public String getDetail();
        public Report getCause();
    }
}
