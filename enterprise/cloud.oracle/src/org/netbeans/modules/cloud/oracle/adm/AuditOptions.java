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
package org.netbeans.modules.cloud.oracle.adm;

import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;

/**
 *
 * @author sdedic
 */
public class AuditOptions {
    private boolean forceAuditExecution;
    private boolean runIfNotExists;
    private String auditName;
    private OCISessionInitiator session;
    
    public static AuditOptions makeNewAudit() {
        return new AuditOptions().setForceAuditExecution(true).setRunIfNotExists(true);
    }

    public boolean isForceAuditExecution() {
        return forceAuditExecution;
    }

    public AuditOptions setForceAuditExecution(boolean forceAuditExecution) {
        this.forceAuditExecution = forceAuditExecution;
        return this;
    }

    public boolean isRunIfNotExists() {
        return runIfNotExists;
    }

    public AuditOptions setRunIfNotExists(boolean runIfNotExists) {
        this.runIfNotExists = runIfNotExists;
        return this;
    }

    public String getAuditName() {
        return auditName;
    }

    public AuditOptions setAuditName(String auditName) {
        this.auditName = auditName;
        return this;
    }
    
    public AuditOptions useSession(OCISessionInitiator session) {
        this.session = session;
        return this;
    }

    public OCISessionInitiator getSession() {
        return session != null ? session : OCIManager.getDefault().getActiveSession();
    }
}
