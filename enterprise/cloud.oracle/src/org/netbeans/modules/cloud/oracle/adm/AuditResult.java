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
package org.netbeans.modules.cloud.oracle.adm;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ArtifactSpec;

/**
 *
 * @author sdedic
 */
public final class AuditResult {
    private String errorMessage;
    private Throwable error;
    private int dependencyCount;
    private int vulnerableCount;
    private String auditId;
    private List<ArtifactSpec> vulnerabilities;
    private String projectName;
    private Project project;

    public AuditResult() {
    }

    public AuditResult(Project project, String projectName, String errorMessage, Exception error) {
        this.project = project;
        this.projectName = projectName;
        this.errorMessage = errorMessage;
        this.error = error;
    }

    public AuditResult(Project project, String projectName, String auditId, int dependencyCount, int vulnerableCount, List<ArtifactSpec> vulnerabilities) {
        this.project = project;
        this.projectName = projectName;
        this.auditId = auditId;
        this.dependencyCount = dependencyCount;
        this.vulnerableCount = vulnerableCount;
        this.vulnerabilities = vulnerabilities;
    }

    public String getProjectName() {
        return projectName;
    }

    public Project getProject() {
        return project;
    }

    public String getAuditId() {
        return auditId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getDependencyCount() {
        return dependencyCount;
    }

    public void setDependencyCount(int dependencyCount) {
        this.dependencyCount = dependencyCount;
    }

    public int getVulnerableCount() {
        return vulnerableCount;
    }

    public void setVulnerableCount(int vulnerableCount) {
        this.vulnerableCount = vulnerableCount;
    }

    public List<ArtifactSpec> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<ArtifactSpec> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
