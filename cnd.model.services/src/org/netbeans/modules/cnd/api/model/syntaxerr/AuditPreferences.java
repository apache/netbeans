/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
