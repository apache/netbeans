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

import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class AuditPreferences {
    public static final Preferences AUDIT_PREFERENCES_ROOT = NbPreferences.root().node("org/netbeans/modules/cnd/analysis"); // NOI18N
    private final Preferences preferences;


    public AuditPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String get(String audit, String key, String defValue) {
        String old = preferences.get(audit, ""); //NOI18N
        StringTokenizer st = new StringTokenizer(old,";"); //NOI18N
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            int i = token.indexOf('='); //NOI18N
            if (i > 0) {
                String rv = token.substring(0,i);
                if (key.equals(rv)) {
                    return token.substring(i+1);
                }
            }
        }
        return defValue;
    }

    public void put(String audit, String key, String value, String defValue) {
        String old = preferences.get(audit, ""); //NOI18N
        StringBuilder buf = new StringBuilder();
        StringTokenizer st = new StringTokenizer(old,";"); //NOI18N
        boolean found = false;
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            int i = token.indexOf('='); //NOI18N
            if (i > 0) {
                String rv = token.substring(0,i);
                if (key.equals(rv)) {
                    if (!value.equals(defValue)) {
                        if (buf.length() > 0) {
                            buf.append(';'); //NOI18N
                        }
                        buf.append(key);
                        buf.append('='); //NOI18N
                        buf.append(value);
                    }
                    found = true;
                } else {
                    if (buf.length() > 0) {
                        buf.append(';'); //NOI18N
                    }
                    buf.append(token);
                }
            }
        }
        if (!found && !value.equals(defValue)) {
            if (buf.length() > 0) {
                buf.append(';'); //NOI18N
            }
            buf.append(key);
            buf.append('='); //NOI18N
            buf.append(value);
        }
        if (buf.length() == 0) {
            preferences.remove(audit);
        } else {
            preferences.put(audit, buf.toString());
        }
    }

    @Override
    public String toString() {
        return preferences.toString();
    }
}
