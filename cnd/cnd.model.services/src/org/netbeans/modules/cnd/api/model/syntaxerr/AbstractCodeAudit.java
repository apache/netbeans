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

package org.netbeans.modules.cnd.api.model.syntaxerr;

import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.EditorEvent;

/**
 *
 */
public abstract class AbstractCodeAudit implements CodeAudit {
    private final String id;
    private final String name;
    private final String description;
    private final String defaultSeverity;
    private final boolean defaultEnabled;
    private final AuditPreferences myPreferences;

    protected AbstractCodeAudit(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultSeverity = defaultSeverity;
        this.defaultEnabled = defaultEnabled;
        this.myPreferences = myPreferences;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public final boolean isEnabled() {
        String defValue = getDefaultEnabled() ? "true" : "false"; //NOI18N
        return !"false".equals(getPreferences().get(getID(), "enabled", defValue)); //NOI18N
    }

    @Override
    public boolean getDefaultEnabled() {
        return defaultEnabled;
    }
    
    @Override
    public final String minimalSeverity() {
        String defValue = getDefaultSeverity();
        return getPreferences().get(getID(), "severity", defValue); //NOI18N
    }
    
    @Override
    public String getDefaultSeverity() {
        return defaultSeverity;
    }
    
    @Override
    public String getKind() {
        return "inspection"; //NOI18N
    }

    public static CsmErrorInfo.Severity toSeverity(String severity){
        if ("error".equals(severity)) { // NOI18N
            return CsmErrorInfo.Severity.ERROR;
        } else if ("warning".equals(severity)) { // NOI18N
            return CsmErrorInfo.Severity.WARNING;
        } else {
            return CsmErrorInfo.Severity.HINT;
        }
    }
    
    @Override
    public AuditPreferences getPreferences() {
        return myPreferences;
    }

    public abstract boolean isSupportedEvent(EditorEvent kind);
    
    public abstract void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response);
}
