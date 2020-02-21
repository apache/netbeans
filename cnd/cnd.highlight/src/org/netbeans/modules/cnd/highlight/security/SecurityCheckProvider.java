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
package org.netbeans.modules.cnd.highlight.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
@ServiceProviders({
    @ServiceProvider(service = CsmErrorProvider.class, position = 1200),
    @ServiceProvider(service = CodeAuditProvider.class, position = 1300)
})
public final class SecurityCheckProvider extends CsmErrorProvider implements CodeAuditProvider {
    public static final String WARNING_SECURITY_ANNOTATION_TYPE = "org-netbeans-modules-cnd-highlight-security-warning";  // NOI18N
    public static final String ERROR_SECURITY_ANNOTATION_TYPE = "org-netbeans-modules-cnd-highlight-security-error";  // NOI18N
    public static final String NAME = "Security"; //NOI18N
    private Collection<CodeAudit> audits;
    private final AuditPreferences myPreferences;
    
    public static CodeAuditProvider getInstance() {
        for(CodeAuditProvider provider : Lookup.getDefault().lookupAll(CodeAuditProvider.class)) {
            if (NAME.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }
    
    public SecurityCheckProvider() {
         myPreferences = new AuditPreferences(AuditPreferences.AUDIT_PREFERENCES_ROOT.node(NAME));
    }
    
    SecurityCheckProvider(Preferences preferences) {        
        try {
            if (preferences.nodeExists(NAME)) {
                preferences = preferences.node(NAME);
            }
        } catch (BackingStoreException ex) {
        }   
        if (preferences.absolutePath().endsWith("/"+NAME)) { //NOI18N
            myPreferences = new AuditPreferences(preferences);
        } else {
            myPreferences = new AuditPreferences(preferences.node(NAME));
        }
    }
    
    @Override
    protected boolean validate(Request request) {
        CsmFile file = request.getFile();
        if (file == null){
            return false;
        }
        for(CodeAudit audit : getAudits()) {
            if (audit.isEnabled()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasHintControlPanel() {
        return true;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SecurityCheckProvider.class, "SecurityCheck_NAME"); //NOI18N
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(SecurityCheckProvider.class, "SecurityCheck_DESCRIPTION"); //NOI18N
    }
    
    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            CsmCacheManager.enter();
            try {
                for(CodeAudit audit : getAudits()) {
                    if (request.isCancelled()) {
                        return;
                    }
                    AbstractCodeAudit engine = (AbstractCodeAudit)audit;
                    if (engine.isEnabled() && engine.isSupportedEvent(request.getEvent())) {
                        engine.doGetErrors(request, response);
                    }
                }
            } finally {
                CsmCacheManager.leave();
            }
        }
    }
    
    @Override
    public synchronized Collection<CodeAudit> getAudits() {
        if (audits == null || audits.isEmpty()) {
            FunctionsXmlService service = FunctionsXmlService.getInstance();
            List<CodeAudit> result = new ArrayList<>(service.getChecksCount());
            for (FunctionsXmlService.Category category : service.getCategories(FunctionsXmlService.Level.UNSAFE)) {
                String id = FunctionsXmlService.Level.UNSAFE.getLevel() + category.getName(); // NOI18N
                String name = "(" + FunctionsXmlService.Level.UNSAFE.name().toUpperCase(Locale.getDefault()) + ") " + category.getName(); // NOI18N
                String description = NbBundle.getMessage(FunctionUsageAudit.class, "FunctionUsageAudit."+category.getName()+".description"); // NOI18N
                result.add(new FunctionUsageAudit(FunctionsXmlService.Level.UNSAFE, category, id, name, description, "error", ERROR_SECURITY_ANNOTATION_TYPE, true, myPreferences)); // NOI18N
            }
            for (FunctionsXmlService.Category category : service.getCategories(FunctionsXmlService.Level.AVOID)) {
                String id = FunctionsXmlService.Level.AVOID.getLevel() + category.getName(); // NOI18N
                String name = "(" + FunctionsXmlService.Level.AVOID.name().toUpperCase(Locale.getDefault()) + ") " + category.getName(); // NOI18N
                String description = NbBundle.getMessage(FunctionUsageAudit.class, "FunctionUsageAudit."+category.getName()+".description"); // NOI18N
                result.add(new FunctionUsageAudit(FunctionsXmlService.Level.AVOID, category, id, name, description, "warning", WARNING_SECURITY_ANNOTATION_TYPE, true, myPreferences)); // NOI18N
            }
            for (FunctionsXmlService.Category category : service.getCategories(FunctionsXmlService.Level.CAUTION)) {
                String id = FunctionsXmlService.Level.CAUTION.getLevel() + category.getName(); // NOI18N
                String name = "(" + FunctionsXmlService.Level.CAUTION.name().toUpperCase(Locale.getDefault()) + ") " + category.getName(); // NOI18N
                String description = NbBundle.getMessage(FunctionUsageAudit.class, "FunctionUsageAudit."+category.getName()+".description"); // NOI18N
                result.add(new FunctionUsageAudit(FunctionsXmlService.Level.CAUTION, category, id, name, description, "warning", WARNING_SECURITY_ANNOTATION_TYPE, false, myPreferences)); // NOI18N
            }
            
            Collections.sort(result, new Comparator<CodeAudit>(){

                @Override
                public int compare(CodeAudit o1, CodeAudit o2) {
                    return o1.getID().compareTo(o2.getID());
                }
            });
            
            audits = result;
        }
        return audits;
    }
    
    @Override
    public AuditPreferences getPreferences() {
        return myPreferences;
    }

    @Override
    public String getMimeType() {
        return MIMENames.SOURCES_MIME_TYPE;
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        for(CodeAudit audit : getAudits()) {
            AbstractCodeAudit engine = (AbstractCodeAudit)audit;
            if (engine.isSupportedEvent(kind)) {
                return true;
            }
        }
        return false;
    }
    
}
