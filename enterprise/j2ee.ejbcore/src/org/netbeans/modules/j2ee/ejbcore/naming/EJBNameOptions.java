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

package org.netbeans.modules.j2ee.ejbcore.naming;

import java.util.prefs.Preferences;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * EJB naming preferences.
 * {@link http://java.sun.com/blueprints/code/namingconventions.html}
 * 
 * @author Martin Adamek
 */
public final class EJBNameOptions extends AdvancedOption {
    
    private static final String SESSION_EJBCLASS_PREFIX = "sessionEjbClassPrefix"; // NOI18N
    private static final String SESSION_EJBCLASS_SUFFIX = "sessionEjbClassSuffix"; // NOI18N
    private static final String SESSION_LOCAL_PREFIX = "sessionLocalPrefix"; // NOI18N
    private static final String SESSION_LOCAL_SUFFIX = "sessionLocalSuffix"; // NOI18N
    private static final String SESSION_REMOTE_PREFIX = "sessionRemotePrefix"; // NOI18N
    private static final String SESSION_REMOTE_SUFFIX = "sessionRemoteSuffix"; // NOI18N
    private static final String SESSION_LOCALHOME_PREFIX = "sessionLocalHomePrefix"; // NOI18N
    private static final String SESSION_LOCALHOME_SUFFIX = "sessionLocalHomeSuffix"; // NOI18N
    private static final String SESSION_REMOTEHOME_PREFIX = "sessionRemoteHomePrefix"; // NOI18N
    private static final String SESSION_REMOTEHOME_SUFFIX = "sessionRemoteHomeSuffix"; // NOI18N
    private static final String SESSION_EJBNAME_PREFIX = "sessionEjbNamePrefix"; // NOI18N
    private static final String SESSION_EJBNAME_SUFFIX = "sessionEjbNameSuffix"; // NOI18N
    private static final String SESSION_DISPLAYNAME_PREFIX = "sessionDisplayNamePrefix"; // NOI18N
    private static final String SESSION_DISPLAYNAME_SUFFIX = "sessionDisplayNameSuffix"; // NOI18N
    
    private static final String ENTITY_EJBCLASS_PREFIX = "entityEjbClassPrefix"; // NOI18N
    private static final String ENTITY_EJBCLASS_SUFFIX = "entityEjbClassSuffix"; // NOI18N
    private static final String ENTITY_LOCAL_PREFIX = "entityLocalPrefix"; // NOI18N
    private static final String ENTITY_LOCAL_SUFFIX = "entityLocalSuffix"; // NOI18N
    private static final String ENTITY_REMOTE_PREFIX = "entityRemotePrefix"; // NOI18N
    private static final String ENTITY_REMOTE_SUFFIX = "entityRemoteSuffix"; // NOI18N
    private static final String ENTITY_LOCALHOME_PREFIX = "entityLocalHomePrefix"; // NOI18N
    private static final String ENTITY_LOCALHOME_SUFFIX = "entityLocalHomeSuffix"; // NOI18N
    private static final String ENTITY_REMOTEHOME_PREFIX = "entityRemoteHomePrefix"; // NOI18N
    private static final String ENTITY_REMOTEHOME_SUFFIX = "entityRemoteHomeSuffix"; // NOI18N
    private static final String ENTITY_EJBNAME_PREFIX = "entityEjbNamePrefix"; // NOI18N
    private static final String ENTITY_EJBNAME_SUFFIX = "entityEjbNameSuffix"; // NOI18N
    private static final String ENTITY_DISPLAYNAME_PREFIX = "entityDisplayNamePrefix"; // NOI18N
    private static final String ENTITY_DISPLAYNAME_SUFFIX = "entityDisplayNameSuffix"; // NOI18N
    private static final String ENTITY_PKCLASS_PREFIX = "entityPkClassPrefix"; // NOI18N
    private static final String ENTITY_PKCLASS_SUFFIX = "entityPkClassSuffix"; // NOI18N
    
    private static final String MESSAGEDRIVEN_EJBCLASS_PREFIX = "messageDrivenEjbClassPrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBCLASS_SUFFIX = "messageDrivenEjbClassSuffix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBNAME_PREFIX = "messageDrivenEjbNamePrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_EJBNAME_SUFFIX = "messageDrivenEjbNameSuffix"; // NOI18N
    private static final String MESSAGEDRIVEN_DISPLAYNAME_PREFIX = "messageDrivenDisplayNamePrefix"; // NOI18N
    private static final String MESSAGEDRIVEN_DISPLAYNAME_SUFFIX = "messageDrivenDisplayNameSuffix"; // NOI18N
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EJBNameOptions.class, "AdvancedOption_DisplayName");
    }
    
    @Override
    public String getTooltip() {
        return NbBundle.getMessage(EJBNameOptions.class, "AdvancedOption_Tooltip");
    }

    @Override
    public OptionsPanelController create() {
        return new EJBNameOptionsPanelController();
    }
    
    // Session
    
    public String getSessionEjbClassPrefix() { return prefs().get(SESSION_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setSessionEjbClassPrefix(String prefix) { prefs().put(SESSION_EJBCLASS_PREFIX, prefix); }
    
    public String getSessionEjbClassSuffix() { return prefs().get(SESSION_EJBCLASS_SUFFIX, ""); } // NOI18N
    public void setSessionEjbclassSuffix(String suffix) { prefs().put(SESSION_EJBCLASS_SUFFIX, suffix); }
    
    public String getSessionLocalPrefix() { return prefs().get(SESSION_LOCAL_PREFIX, ""); } // NOI18N
    public void setSessionLocalPrefix(String prefix) { prefs().put(SESSION_LOCAL_PREFIX, prefix); }
    
    public String getSessionLocalSuffix() { return prefs().get(SESSION_LOCAL_SUFFIX, "Local"); } // NOI18N
    public void setSessionLocalSuffix(String suffix) { prefs().put(SESSION_LOCAL_SUFFIX, suffix); }
    
    public String getSessionRemotePrefix() { return prefs().get(SESSION_REMOTE_PREFIX, ""); } // NOI18N
    public void setSessionRemotePrefix(String prefix) { prefs().put(SESSION_REMOTE_PREFIX, prefix); }
    
    public String getSessionRemoteSuffix() { return prefs().get(SESSION_REMOTE_SUFFIX, "Remote"); } // NOI18N
    public void setSessionRemoteSuffix(String suffix) { prefs().put(SESSION_REMOTE_SUFFIX, suffix); }
    
    public String getSessionLocalHomePrefix() { return prefs().get(SESSION_LOCALHOME_PREFIX, ""); } // NOI18N
    public void setSessionLocalHomePrefix(String prefix) { prefs().put(SESSION_LOCALHOME_PREFIX, prefix); }
    
    public String getSessionLocalHomeSuffix() { return prefs().get(SESSION_LOCALHOME_SUFFIX, "LocalHome"); } // NOI18N
    public void setSessionLocalHomeSuffix(String suffix) { prefs().put(SESSION_LOCALHOME_SUFFIX, suffix); }
    
    public String getSessionRemoteHomePrefix() { return prefs().get(SESSION_REMOTEHOME_PREFIX, ""); } // NOI18N
    public void setSessionRemoteHomePrefix(String prefix) { prefs().put(SESSION_REMOTEHOME_PREFIX, prefix); }
    
    public String getSessionRemoteHomeSuffix() { return prefs().get(SESSION_REMOTEHOME_SUFFIX, "RemoteHome"); } // NOI18N
    public void setSessionRemoteHomeSuffix(String suffix) { prefs().put(SESSION_REMOTEHOME_SUFFIX, suffix); }
    
    public String getSessionEjbNamePrefix() { return prefs().get(SESSION_EJBNAME_PREFIX, ""); } // NOI18N
    public void setSessionEjbNamePrefix(String prefix) { prefs().put(SESSION_EJBNAME_PREFIX, prefix); }
    
    public String getSessionEjbNameSuffix() { return prefs().get(SESSION_EJBNAME_SUFFIX, ""); } // NOI18N
    public void setSessionEjbNameSuffix(String suffix) { prefs().put(SESSION_EJBNAME_SUFFIX, suffix); }
    
    public String getSessionDisplayNamePrefix() { return prefs().get(SESSION_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setSessionDisplayNamePrefix(String prefix) { prefs().put(SESSION_DISPLAYNAME_PREFIX, prefix); }
    
    public String getSessionDisplayNameSuffix() { return prefs().get(SESSION_DISPLAYNAME_SUFFIX, "SB"); } // NOI18N
    public void setSessionDisplayNameSuffix(String suffix) { prefs().put(SESSION_DISPLAYNAME_SUFFIX, suffix); }
    
    // Entity
    
    public String getEntityEjbClassPrefix() { return prefs().get(ENTITY_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setEntityEjbClassPrefix(String prefix) { prefs().put(ENTITY_EJBCLASS_PREFIX, prefix); }
    
    public String getEntityEjbClassSuffix() { return prefs().get(ENTITY_EJBCLASS_SUFFIX, ""); } // NOI18N
    public void setEntityEjbClassSuffix(String suffix) { prefs().put(ENTITY_EJBCLASS_SUFFIX, suffix); }
    
    public String getEntityLocalPrefix() { return prefs().get(ENTITY_LOCAL_PREFIX, ""); } // NOI18N
    public void setEntityLocalPrefix(String prefix) { prefs().put(ENTITY_LOCAL_PREFIX, prefix); }
    
    public String getEntityLocalSuffix() { return prefs().get(ENTITY_LOCAL_SUFFIX, "Local"); } // NOI18N
    public void setEntityLocalSuffix(String suffix) { prefs().put(ENTITY_LOCAL_SUFFIX, suffix); }
    
    public String getEntityRemotePrefix() { return prefs().get(ENTITY_REMOTE_PREFIX, ""); } // NOI18N
    public void setEntityRemotePrefix(String prefix) { prefs().put(ENTITY_REMOTE_PREFIX, prefix); }
    
    public String getEntityRemoteSuffix() { return prefs().get(ENTITY_REMOTE_SUFFIX, "Remote"); } // NOI18N
    public void setEntityRemoteSuffix(String suffix) { prefs().put(ENTITY_REMOTE_SUFFIX, suffix); }
    
    public String getEntityLocalHomePrefix() { return prefs().get(ENTITY_LOCALHOME_PREFIX, ""); } // NOI18N
    public void setEntityLocalHomePrefix(String prefix) { prefs().put(ENTITY_LOCALHOME_PREFIX, prefix); }
    
    public String getEntityLocalHomeSuffix() { return prefs().get(ENTITY_LOCALHOME_SUFFIX, "LocalHome"); } // NOI18N
    public void setEntityLocalHomeSuffix(String suffix) { prefs().put(ENTITY_LOCALHOME_SUFFIX, suffix); }
    
    public String getEntityRemoteHomePrefix() { return prefs().get(ENTITY_REMOTEHOME_PREFIX, ""); } // NOI18N
    public void setEntityRemoteHomePrefix(String prefix) { prefs().put(ENTITY_REMOTEHOME_PREFIX, prefix); }
    
    public String getEntityRemoteHomeSuffix() { return prefs().get(ENTITY_REMOTEHOME_SUFFIX, "RemoteHome"); } // NOI18N
    public void setEntityHomeRemoteSuffix(String suffix) { prefs().put(ENTITY_REMOTEHOME_SUFFIX, suffix); }
    
    public String getEntityEjbNamePrefix() { return prefs().get(ENTITY_EJBNAME_PREFIX, ""); } // NOI18N
    public void setEntityEjbNamePrefix(String prefix) { prefs().put(ENTITY_EJBNAME_PREFIX, prefix); }
    
    public String getEntityEjbNameSuffix() { return prefs().get(ENTITY_EJBNAME_SUFFIX, ""); } // NOI18N
    public void setEntityEjbNameSuffix(String suffix) { prefs().put(ENTITY_EJBNAME_SUFFIX, suffix); }
    
    public String getEntityDisplayNamePrefix() { return prefs().get(ENTITY_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setEntityDisplayNamePrefix(String prefix) { prefs().put(ENTITY_DISPLAYNAME_PREFIX, prefix); }
    
    public String getEntityDisplayNameSuffix() { return prefs().get(ENTITY_DISPLAYNAME_SUFFIX, "EB"); } // NOI18N
    public void setEntityDisplayNameSuffix(String suffix) { prefs().put(ENTITY_DISPLAYNAME_SUFFIX, suffix); }
    
    public String getEntityPkClassPrefix() { return prefs().get(ENTITY_PKCLASS_PREFIX, ""); } // NOI18N
    public void setEntityPkClassPrefix(String prefix) { prefs().put(ENTITY_PKCLASS_PREFIX, prefix); }
    
    public String getEntityPkClassSuffix() { return prefs().get(ENTITY_PKCLASS_SUFFIX, "PK"); } // NOI18N
    public void setEntityPkClassSuffix(String suffix) { prefs().put(ENTITY_PKCLASS_SUFFIX, suffix); }
    
    // MessageDriven
    
    public String getMessageDrivenEjbClassPrefix() { return prefs().get(MESSAGEDRIVEN_EJBCLASS_PREFIX, ""); } // NOI18N
    public void setMessageDrivenEjbClassPrefix(String prefix) { prefs().put(MESSAGEDRIVEN_EJBCLASS_PREFIX, prefix); }
    
    public String getMessageDrivenEjbClassSuffix() { return prefs().get(MESSAGEDRIVEN_EJBCLASS_SUFFIX, ""); } // NOI18N
    public void setMessageDrivenEjbClassSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_EJBCLASS_SUFFIX, suffix); }
    
    public String getMessageDrivenEjbNamePrefix() { return prefs().get(MESSAGEDRIVEN_EJBNAME_PREFIX, ""); } // NOI18N
    public void setMessageDrivenEjbNamePrefix(String prefix) { prefs().put(MESSAGEDRIVEN_EJBNAME_PREFIX, prefix); }
    
    public String getMessageDrivenEjbNameSuffix() { return prefs().get(MESSAGEDRIVEN_EJBNAME_SUFFIX, ""); } // NOI18N
    public void setMessageDrivenEjbNameSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_EJBNAME_SUFFIX, suffix); }

    public String getMessageDrivenDisplayNamePrefix() { return prefs().get(MESSAGEDRIVEN_DISPLAYNAME_PREFIX, ""); } // NOI18N
    public void setMessageDrivenDisplayNamePrefix(String prefix) { prefs().put(MESSAGEDRIVEN_DISPLAYNAME_PREFIX, prefix); }
    
    public String getMessageDrivenDisplayNameSuffix() { return prefs().get(MESSAGEDRIVEN_DISPLAYNAME_SUFFIX, "MDB"); } // NOI18N
    public void setMessageDrivenDisplayNameSuffix(String suffix) { prefs().put(MESSAGEDRIVEN_DISPLAYNAME_SUFFIX, suffix); }
    
    // helpers
    
    private Preferences prefs() {
        return NbPreferences.forModule(EJBNameOptions.class);
    }
    
}
