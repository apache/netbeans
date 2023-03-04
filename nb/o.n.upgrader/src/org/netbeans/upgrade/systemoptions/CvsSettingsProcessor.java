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
package org.netbeans.upgrade.systemoptions;

import java.util.*;

/**
 * Imports CVS root settings: external SSH command
 *
 * @author Maros Sandor
 */
public class CvsSettingsProcessor extends PropertyProcessor {

    private final String FIELD_SEPARATOR = "<~>";
    
    public CvsSettingsProcessor() {
        super("org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings.PersistentMap");
    }

    void processPropertyImpl(String propertyName, Object value) {
        if ("rootsMap".equals(propertyName)) { // NOI18N
            List mapData = ((SerParser.ObjectWrapper) value).data;
            int n = 0;
            int idx = 3;
            if (mapData.size() > 3) {
                for (;;) {
                    if (idx + 2 > mapData.size()) break;
                    String root = (String) mapData.get(idx);
                    List rootData = ((SerParser.ObjectWrapper) mapData.get(idx + 1)).data;
                    try {
                        List extSettingsData = ((SerParser.ObjectWrapper) ((SerParser.NameValue) rootData.get(0)).value).data;
                        Boolean extRememberPassword = (Boolean) ((SerParser.NameValue) extSettingsData.get(0)).value;
                        Boolean extUseInternalSSH = (Boolean) ((SerParser.NameValue) extSettingsData.get(1)).value;
                        String extCommand = (String) ((SerParser.NameValue) extSettingsData.get(2)).value;
                        String extPassword = (String) ((SerParser.NameValue) extSettingsData.get(3)).value;
                        String setting = root + FIELD_SEPARATOR + extUseInternalSSH + FIELD_SEPARATOR + extRememberPassword + FIELD_SEPARATOR + extCommand;
                        if (extPassword != null && !extPassword.equals("null")) setting += FIELD_SEPARATOR + extPassword; 
                        addProperty("cvsRootSettings" + "." + n, setting);
                        n++;
                    } catch (Exception e) {
                        // the setting is not there => nothing to import
                    }
                    idx += 2;
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
}
